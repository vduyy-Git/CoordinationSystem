package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.DonationItem;
import com.coordinationsystem.repository.DonationItemRepository;
import com.coordinationsystem.repository.DonationRepository;
import com.coordinationsystem.repository.ItemRepository;
import com.coordinationsystem.dtorequest.CreateDonationItemRequest;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
@Transactional
public class DonationItemService {
    private final DonationItemRepository repository;
    private final DonationRepository donations;
    private final ItemRepository items;

    public DonationItemService(DonationItemRepository repository, DonationRepository donations, ItemRepository items) {
        this.repository = repository;
        this.donations = donations;
        this.items = items;
    }

    public List<DonationItem> getAll() {
        return repository.findAll();
    }

    public DonationItem getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public DonationItem create(DonationItem entity) {
        throw new com.coordinationsystem.exception.BadRequestException("Thêm vật tư cần xác minh chủ quyên góp");
    }

    public DonationItem createForDonor(CreateDonationItemRequest command, String email) {
        DonationItem donationItem = new DonationItem();
        var donation = donations.findLockedById(command.donationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy quyên góp"));
        if (!donation.getDonor().getEmail().equalsIgnoreCase(email)) {
            throw new com.coordinationsystem.exception.ForBiddenException("Bạn không thể thêm vật tư vào quyên góp của người khác");
        }
        if (!"PENDING".equals(donation.getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Chỉ có thể thêm vật tư khi quyên góp đang chờ xử lý");
        }
        if (command.offeredQuantity() == null || command.offeredQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new com.coordinationsystem.exception.BadRequestException("Số lượng đề nghị phải lớn hơn 0");
        }
        donationItem.setDonation(donation);
        donationItem.setItem(items.findById(command.itemId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vật tư")));
        donationItem.setOfferedQuantity(command.offeredQuantity());
        donationItem.setAcceptedQuantity(BigDecimal.ZERO);
        donationItem.setReservedQuantity(BigDecimal.ZERO);
        donationItem.setDeliveredQuantity(BigDecimal.ZERO);
        donationItem.setItemCondition(command.itemCondition());
        donationItem.setStatus("PENDING");
        return repository.save(donationItem);
    }

    public DonationItem update(Long id, DonationItem entity) {
        DonationItem current = getById(id);
        if (!"PENDING".equals(current.getDonation().getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Không thể sửa vật tư sau khi duyệt quyên góp");
        }
        current.setOfferedQuantity(entity.getOfferedQuantity());
        current.setItemCondition(entity.getItemCondition());
        return repository.save(current);
    }

    public void delete(Long id) {
        DonationItem current = getById(id);
        if (!"PENDING".equals(current.getDonation().getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Không thể xóa vật tư sau khi duyệt quyên góp");
        }
        repository.delete(current);
    }

    private void setEntityId(DonationItem entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
