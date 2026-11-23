package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.Allocation;
import com.coordinationsystem.model.AllocationItem;
import com.coordinationsystem.model.SupplyMovement;
import com.coordinationsystem.dtorequest.CreateAllocationRequest;
import com.coordinationsystem.exception.BadRequestException;
import com.coordinationsystem.exception.ConflictException;
import com.coordinationsystem.model.AssistanceRequest;
import com.coordinationsystem.model.DonationItem;
import com.coordinationsystem.model.RequestItem;
import com.coordinationsystem.repository.AllocationItemRepository;
import com.coordinationsystem.repository.AllocationRepository;
import com.coordinationsystem.repository.AssistanceRequestRepository;
import com.coordinationsystem.repository.DonationItemRepository;
import com.coordinationsystem.repository.RequestItemRepository;
import com.coordinationsystem.repository.SupplyMovementRepository;
import com.coordinationsystem.repository.UserRepository;
import com.coordinationsystem.repository.ShipmentItemRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class AllocationService {
    private final AllocationRepository repository;
    private final AllocationItemRepository allocationItems;
    private final AssistanceRequestRepository requests;
    private final RequestItemRepository requestItems;
    private final DonationItemRepository donationItems;
    private final SupplyMovementRepository movements;
    private final UserRepository users;
    private final ShipmentItemRepository shipmentItems;

    public AllocationService(AllocationRepository repository, AllocationItemRepository allocationItems,
                             AssistanceRequestRepository requests, RequestItemRepository requestItems,
                             DonationItemRepository donationItems, SupplyMovementRepository movements,
                             UserRepository users, ShipmentItemRepository shipmentItems) {
        this.repository = repository;
        this.allocationItems = allocationItems;
        this.requests = requests;
        this.requestItems = requestItems;
        this.donationItems = donationItems;
        this.movements = movements;
        this.users = users;
        this.shipmentItems = shipmentItems;
    }

    public List<Allocation> getAll() {
        return repository.findAll();
    }

    public Allocation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public Allocation create(Allocation entity) {
        throw new BadRequestException("Hãy tạo phân bổ qua API nghiệp vụ với requestId và danh sách vật tư");
    }

    public Allocation allocate(CreateAllocationRequest command, String coordinatorEmail) {
        AssistanceRequest request = requests.findById(command.requestId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy yêu cầu hỗ trợ"));
        if (!List.of("APPROVED", "FULFILLING").contains(request.getStatus())) {
            throw new ConflictException("Yêu cầu phải được duyệt trước khi phân bổ");
        }
        Allocation allocation = new Allocation();
        allocation.setAllocationCode("AL-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        allocation.setRequest(request);
        allocation.setCoordinator(users.findByEmailIgnoreCase(coordinatorEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy điều phối viên")));
        allocation.setStatus("IN_PROGRESS");
        allocation.setNote(command.note());
        allocation = repository.save(allocation);

        for (CreateAllocationRequest.AllocationLine line : command.items()) {
            if (line.quantity() == null || line.quantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Số lượng phân bổ phải lớn hơn 0");
            }
            RequestItem requestItem = requestItems.findLockedById(line.requestItemId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy dòng vật tư yêu cầu"));
            DonationItem donationItem = donationItems.findLockedById(line.donationItemId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy vật tư quyên góp"));
            if (!requestItem.getRequest().getId().equals(request.getId())) {
                throw new BadRequestException("Dòng vật tư không thuộc yêu cầu được phân bổ");
            }
            if (!"APPROVED".equals(donationItem.getDonation().getStatus())) {
                throw new ConflictException("Nguồn quyên góp chưa được duyệt");
            }
            if (!requestItem.getItem().getId().equals(donationItem.getItem().getId())) {
                throw new BadRequestException("Loại vật tư quyên góp không khớp yêu cầu");
            }
            BigDecimal remainingRequest = requestItem.getApprovedQuantity()
                    .subtract(allocationItems.sumAllocatedForRequestItem(requestItem.getId()));
            BigDecimal available = donationItem.getAcceptedQuantity()
                    .subtract(donationItem.getReservedQuantity())
                    .subtract(donationItem.getDeliveredQuantity());
            if (line.quantity().compareTo(remainingRequest) > 0) {
                throw new ConflictException("Số lượng phân bổ vượt phần yêu cầu còn thiếu");
            }
            if (line.quantity().compareTo(available) > 0) {
                throw new ConflictException("Số lượng phân bổ vượt tồn kho khả dụng");
            }
            AllocationItem allocationItem = new AllocationItem();
            allocationItem.setAllocation(allocation);
            allocationItem.setRequestItem(requestItem);
            allocationItem.setDonationItem(donationItem);
            allocationItem.setQuantity(line.quantity());
            allocationItems.save(allocationItem);
            donationItem.setReservedQuantity(donationItem.getReservedQuantity().add(line.quantity()));
            donationItems.save(donationItem);

            SupplyMovement movement = new SupplyMovement();
            movement.setDonationItem(donationItem);
            movement.setMovementType("RESERVED");
            movement.setQuantity(line.quantity());
            movement.setFromLocationType("AVAILABLE");
            movement.setToLocationType("ALLOCATION");
            movement.setToLocationId(allocation.getId());
            movement.setReferenceType("ALLOCATION");
            movement.setReferenceId(allocation.getId());
            movement.setCreatedBy(allocation.getCoordinator());
            movements.save(movement);
        }
        request.setStatus("FULFILLING");
        requests.save(request);
        return allocation;
    }

    public Allocation update(Long id, Allocation entity) {
        throw new BadRequestException("Không hỗ trợ sửa trực tiếp phân bổ; hãy dùng các thao tác nghiệp vụ");
    }

    public Allocation cancel(Long id, String actorEmail) {
        Allocation allocation = repository.findLockedById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
        if (!"IN_PROGRESS".equals(allocation.getStatus()) && !"APPROVED".equals(allocation.getStatus())) {
            throw new ConflictException("Chỉ có thể hủy phân bổ đang thực hiện");
        }
        var lines = allocationItems.findByAllocation_Id(id);
        if (lines.isEmpty()) throw new ConflictException("Phân bổ chưa có chi tiết vật tư");
        var actor = users.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người thao tác"));
        for (AllocationItem line : lines) {
            boolean hasActiveShipment = shipmentItems.findByAllocationItem_Id(line.getId()).stream()
                    .anyMatch(item -> !List.of("CANCELLED", "FAILED").contains(item.getShipment().getStatus()));
            if (hasActiveShipment) throw new ConflictException("Hãy hủy hoặc kết thúc shipment trước khi hủy phân bổ");
            DonationItem donationItem = donationItems.findLockedById(line.getDonationItem().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy vật tư tồn kho"));
            if (donationItem.getReservedQuantity().compareTo(line.getQuantity()) < 0) {
                throw new ConflictException("Số lượng đang giữ không khớp với phân bổ");
            }
            donationItem.setReservedQuantity(donationItem.getReservedQuantity().subtract(line.getQuantity()));
            donationItems.save(donationItem);
            SupplyMovement movement = new SupplyMovement();
            movement.setDonationItem(donationItem);
            movement.setMovementType("RELEASED");
            movement.setQuantity(line.getQuantity());
            movement.setFromLocationType("ALLOCATION");
            movement.setFromLocationId(id);
            movement.setToLocationType("AVAILABLE");
            movement.setReferenceType("ALLOCATION");
            movement.setReferenceId(id);
            movement.setCreatedBy(actor);
            movements.save(movement);
        }
        allocation.setStatus("CANCELLED");
        repository.save(allocation);
        boolean anyActive = repository.findByRequest_Id(allocation.getRequest().getId()).stream()
                .anyMatch(other -> !other.getId().equals(id) && !"CANCELLED".equals(other.getStatus()));
        if (!anyActive && "FULFILLING".equals(allocation.getRequest().getStatus())) {
            allocation.getRequest().setStatus("APPROVED");
            requests.save(allocation.getRequest());
        }
        return allocation;
    }

    public void delete(Long id) {
        throw new BadRequestException("Không hỗ trợ xóa trực tiếp phân bổ vì sẽ làm sai số lượng đã giữ");
    }

    private void setEntityId(Allocation entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
