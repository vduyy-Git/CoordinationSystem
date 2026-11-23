package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.RequestItem;
import com.coordinationsystem.repository.RequestItemRepository;
import com.coordinationsystem.repository.AssistanceRequestRepository;
import com.coordinationsystem.repository.ItemRepository;
import com.coordinationsystem.dtorequest.CreateRequestItemRequest;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
@Transactional
public class RequestItemService {
    private final RequestItemRepository repository;
    private final AssistanceRequestRepository requests;
    private final ItemRepository items;

    public RequestItemService(RequestItemRepository repository, AssistanceRequestRepository requests, ItemRepository items) {
        this.repository = repository;
        this.requests = requests;
        this.items = items;
    }

    public List<RequestItem> getAll() {
        return repository.findAll();
    }

    public RequestItem getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public RequestItem create(RequestItem entity) {
        throw new com.coordinationsystem.exception.BadRequestException("Thêm dòng yêu cầu cần xác minh chủ yêu cầu");
    }

    public RequestItem createForRecipient(CreateRequestItemRequest command, String email) {
        RequestItem requestItem = new RequestItem();
        var request = requests.findLockedById(command.requestId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy yêu cầu hỗ trợ"));
        if (!request.getRecipient().getEmail().equalsIgnoreCase(email)) {
            throw new com.coordinationsystem.exception.ForBiddenException("Bạn không thể sửa yêu cầu của người khác");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Chỉ có thể thêm vật tư khi yêu cầu đang chờ xử lý");
        }
        if (command.requestedQuantity() == null || command.requestedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new com.coordinationsystem.exception.BadRequestException("Số lượng yêu cầu phải lớn hơn 0");
        }
        requestItem.setRequest(request);
        requestItem.setItem(items.findById(command.itemId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vật tư")));
        requestItem.setRequestedQuantity(command.requestedQuantity());
        requestItem.setApprovedQuantity(BigDecimal.ZERO);
        requestItem.setNote(command.note());
        return repository.save(requestItem);
    }

    public RequestItem update(Long id, RequestItem entity) {
        RequestItem current = getById(id);
        if (!"PENDING".equals(current.getRequest().getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Không thể sửa dòng yêu cầu sau khi duyệt");
        }
        current.setRequestedQuantity(entity.getRequestedQuantity());
        current.setNote(entity.getNote());
        return repository.save(current);
    }

    public void delete(Long id) {
        RequestItem current = getById(id);
        if (!"PENDING".equals(current.getRequest().getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Không thể xóa dòng yêu cầu sau khi duyệt");
        }
        repository.delete(current);
    }

    private void setEntityId(RequestItem entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
