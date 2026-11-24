package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.AssistanceRequest;
import com.coordinationsystem.repository.AssistanceRequestRepository;
import com.coordinationsystem.repository.UserRepository;
import com.coordinationsystem.repository.RequestItemRepository;
import com.coordinationsystem.repository.ItemRepository;
import com.coordinationsystem.dtorequest.CreateAssistanceRequest;
import com.coordinationsystem.dtorequest.PartialApprovalRequest;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AssistanceRequestService {
    private final AssistanceRequestRepository repository;
    private final UserRepository users;
    private final RequestItemRepository requestItems;
    private final ItemRepository items;

    public AssistanceRequestService(AssistanceRequestRepository repository, UserRepository users, RequestItemRepository requestItems,
                                    ItemRepository items) {
        this.repository = repository;
        this.users = users;
        this.requestItems = requestItems;
        this.items = items;
    }

    public List<AssistanceRequest> getAll() {
        return repository.findAll();
    }

    public AssistanceRequest getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public AssistanceRequest create(AssistanceRequest entity) {
        throw new com.coordinationsystem.exception.BadRequestException("Tạo yêu cầu cần xác định người dùng đang đăng nhập");
    }

    public AssistanceRequest createForRecipient(CreateAssistanceRequest command, String email) {
        AssistanceRequest request = new AssistanceRequest();
        request.setRecipient(users.findByEmailIgnoreCase(email).orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản")));
        request.setRequestCode("RQ-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        request.setTitle(command.title().trim());
        request.setDescription(command.description());
        request.setDeliveryAddress(command.deliveryAddress().trim());
        request.setPriority(command.priority().trim().toUpperCase());
        if (!List.of("LOW", "NORMAL", "HIGH", "URGENT").contains(request.getPriority())) {
            throw new com.coordinationsystem.exception.BadRequestException("Mức độ ưu tiên không hợp lệ");
        }
        request.setStatus("PENDING");
        AssistanceRequest savedRequest = repository.save(request);
        if (command.items() != null) {
            var lines = command.items().stream().map(input -> {
                var line = new com.coordinationsystem.model.RequestItem();
                line.setRequest(savedRequest);
                line.setItem(items.findById(input.itemId()).orElseThrow(() -> new NotFoundException("Không tìm thấy vật tư: " + input.itemId())));
                line.setRequestedQuantity(input.requestedQuantity());
                line.setApprovedQuantity(java.math.BigDecimal.ZERO);
                line.setNote(input.note());
                return line;
            }).toList();
            requestItems.saveAll(lines);
        }
        return savedRequest;
    }

    public List<AssistanceRequest> getMine(String email) {
        return repository.findByRecipient_EmailIgnoreCaseOrderByCreatedAtDesc(email);
    }

    public AssistanceRequest cancelByRecipient(Long id, String recipientEmail) {
        AssistanceRequest request = repository.findLockedById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
        if (!request.getRecipient().getEmail().equalsIgnoreCase(recipientEmail)) {
            throw new com.coordinationsystem.exception.ForBiddenException("Bạn không thể hủy yêu cầu của người khác");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Chỉ có thể hủy yêu cầu đang chờ xử lý");
        }
        request.setStatus("CANCELLED");
        return repository.save(request);
    }

    public AssistanceRequest update(Long id, AssistanceRequest entity) {
        AssistanceRequest current = getById(id);
        if (!"PENDING".equals(current.getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Chỉ có thể sửa yêu cầu đang chờ xử lý");
        }
        current.setTitle(entity.getTitle());
        current.setDescription(entity.getDescription());
        current.setDeliveryAddress(entity.getDeliveryAddress());
        String priority = entity.getPriority() == null ? "" : entity.getPriority().trim().toUpperCase();
        if (!List.of("LOW", "NORMAL", "HIGH", "URGENT").contains(priority)) {
            throw new com.coordinationsystem.exception.BadRequestException("Mức độ ưu tiên không hợp lệ");
        }
        current.setPriority(priority);
        return repository.save(current);
    }

    public void delete(Long id) {
        throw new com.coordinationsystem.exception.BadRequestException("Không hỗ trợ xóa yêu cầu; hãy dùng thao tác từ chối hoặc hủy");
    }

    public AssistanceRequest decide(Long id, String reviewerEmail, boolean approve) {
        AssistanceRequest request = repository.findLockedById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
        if (!"PENDING".equals(request.getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Chỉ có thể duyệt yêu cầu đang chờ xử lý");
        }
        request.setStatus(approve ? "APPROVED" : "REJECTED");
        request.setApprovedBy(users.findByEmailIgnoreCase(reviewerEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người duyệt")));
        request.setApprovedAt(LocalDateTime.now());
        if (approve) {
            var lines = requestItems.findByRequest_Id(id);
            if (lines.isEmpty()) throw new com.coordinationsystem.exception.ConflictException("Yêu cầu chưa có vật tư");
            lines.forEach(line -> line.setApprovedQuantity(line.getRequestedQuantity()));
            requestItems.saveAll(lines);
        }
        return repository.save(request);
    }

    public AssistanceRequest partiallyApprove(Long id, String reviewerEmail, PartialApprovalRequest command) {
        AssistanceRequest request = repository.findLockedById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy yêu cầu hỗ trợ"));
        if (!"PENDING".equals(request.getStatus()))
            throw new com.coordinationsystem.exception.ConflictException("Chỉ có thể duyệt yêu cầu đang chờ xử lý");
        var lines = requestItems.findByRequest_Id(id);
        if (lines.isEmpty()) throw new com.coordinationsystem.exception.ConflictException("Yêu cầu chưa có vật tư");
        var quantities = command.items().stream().collect(java.util.stream.Collectors.toMap(
                PartialApprovalRequest.Line::requestItemId, PartialApprovalRequest.Line::approvedQuantity,
                (first, second) -> { throw new com.coordinationsystem.exception.BadRequestException("Dòng vật tư bị lặp"); }));
        if (quantities.size() != lines.size() || lines.stream().anyMatch(line -> !quantities.containsKey(line.getId())))
            throw new com.coordinationsystem.exception.BadRequestException("Cần gửi số lượng duyệt cho từng dòng vật tư của yêu cầu");
        boolean partial = false;
        boolean anyApproved = false;
        for (var line : lines) {
            var quantity = quantities.get(line.getId());
            if (quantity.signum() < 0 || quantity.compareTo(line.getRequestedQuantity()) > 0)
                throw new com.coordinationsystem.exception.BadRequestException("Số lượng duyệt phải từ 0 đến số lượng yêu cầu");
            line.setApprovedQuantity(quantity);
            anyApproved |= quantity.signum() > 0;
            partial |= quantity.compareTo(line.getRequestedQuantity()) < 0;
        }
        if (!anyApproved) throw new com.coordinationsystem.exception.BadRequestException("Phải duyệt ít nhất một số lượng lớn hơn 0");
        requestItems.saveAll(lines);
        request.setStatus(partial ? "PARTIALLY_APPROVED" : "APPROVED");
        request.setApprovedBy(users.findByEmailIgnoreCase(reviewerEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người duyệt")));
        request.setApprovedAt(LocalDateTime.now());
        return repository.save(request);
    }

    private void setEntityId(AssistanceRequest entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
