package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.repository.ShipmentItemRepository;
import com.coordinationsystem.repository.ShipmentRepository;
import com.coordinationsystem.repository.AllocationItemRepository;
import com.coordinationsystem.model.ShipmentItem;
import com.coordinationsystem.exception.BadRequestException;
import com.coordinationsystem.exception.ConflictException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
@Transactional
public class ShipmentItemService {
    private final ShipmentItemRepository repository;
    private final ShipmentRepository shipments;
    private final AllocationItemRepository allocationItems;

    public ShipmentItemService(ShipmentItemRepository repository, ShipmentRepository shipments,
                               AllocationItemRepository allocationItems) {
        this.repository = repository;
        this.shipments = shipments;
        this.allocationItems = allocationItems;
    }

    public List<ShipmentItem> getAll() {
        return repository.findAll();
    }

    public ShipmentItem getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public ShipmentItem create(ShipmentItem entity) {
        if (entity.getShipment() == null || entity.getShipment().getId() == null
                || entity.getAllocationItem() == null || entity.getAllocationItem().getId() == null
                || entity.getQuantity() == null || entity.getQuantity().compareTo(BigDecimal.ZERO) <= 0
                || entity.getQuantity().stripTrailingZeros().scale() > 2
                || entity.getQuantity().precision() - entity.getQuantity().scale() > 10) {
            throw new BadRequestException("Cần có shipment, dòng phân bổ và số lượng dương");
        }
        var shipment = shipments.findLockedById(entity.getShipment().getId())
                .orElseThrow(() -> new com.coordinationsystem.exception.NotFoundException("Không tìm thấy shipment"));
        var allocationItem = allocationItems.findLockedById(entity.getAllocationItem().getId())
                .orElseThrow(() -> new com.coordinationsystem.exception.NotFoundException("Không tìm thấy dòng phân bổ"));
        var request = allocationItem.getRequestItem().getRequest();
        if ("CANCELLED".equals(allocationItem.getAllocation().getStatus())) {
            throw new ConflictException("Không thể dùng dòng phân bổ đã hủy");
        }
        if (!request.getDeliveryAddress().equalsIgnoreCase(shipment.getDeliveryAddress())) {
            throw new BadRequestException("Địa chỉ shipment phải trùng địa chỉ yêu cầu hỗ trợ");
        }
        var existingItems = repository.findByShipment_Id(shipment.getId());
        if (!existingItems.isEmpty() && !existingItems.get(0).getAllocationItem().getRequestItem()
                .getRequest().getId().equals(request.getId())) {
            throw new BadRequestException("Một shipment chỉ có thể phục vụ một yêu cầu hỗ trợ");
        }
        if (!List.of("PENDING", "BOOKED").contains(shipment.getStatus())) {
            throw new ConflictException("Chỉ có thể thêm vật tư vào shipment chưa lấy hàng");
        }
        BigDecimal shipped = repository.sumShippedForAllocationItem(allocationItem.getId());
        if (shipped.add(entity.getQuantity()).compareTo(allocationItem.getQuantity()) > 0) {
            throw new ConflictException("Tổng số lượng giao vượt số lượng đã phân bổ");
        }
        entity.setShipment(shipment);
        entity.setAllocationItem(allocationItem);
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public ShipmentItem update(Long id, ShipmentItem entity) {
        throw new BadRequestException("Không hỗ trợ sửa trực tiếp vật tư trong shipment");
    }

    public void delete(Long id) {
        throw new BadRequestException("Không hỗ trợ xóa trực tiếp vật tư trong shipment");
    }

    private void setEntityId(ShipmentItem entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
