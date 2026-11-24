package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.Donation;
import com.coordinationsystem.repository.DonationRepository;
import com.coordinationsystem.repository.UserRepository;
import com.coordinationsystem.repository.DonationItemRepository;
import com.coordinationsystem.repository.SupplyMovementRepository;
import com.coordinationsystem.repository.ItemRepository;
import com.coordinationsystem.model.SupplyMovement;
import com.coordinationsystem.dtorequest.CreateDonationRequest;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class DonationService {
    private final DonationRepository repository;
    private final UserRepository users;
    private final DonationItemRepository donationItems;
    private final SupplyMovementRepository movements;
    private final ItemRepository items;

    public DonationService(DonationRepository repository, UserRepository users, DonationItemRepository donationItems,
                           SupplyMovementRepository movements, ItemRepository items) {
        this.repository = repository;
        this.users = users;
        this.donationItems = donationItems;
        this.movements = movements;
        this.items = items;
    }

    public List<Donation> getAll() {
        return repository.findAll();
    }

    public Donation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public Donation create(Donation entity) {
        throw new com.coordinationsystem.exception.BadRequestException("Tạo quyên góp cần xác định người dùng đang đăng nhập");
    }

    public Donation createForDonor(CreateDonationRequest command, String email) {
        Donation donation = new Donation();
        donation.setDonor(users.findByEmailIgnoreCase(email).orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản")));
        donation.setDonationCode("DN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        donation.setPickupAddress(command.pickupAddress().trim());
        donation.setDonationType(command.donationType().trim().toUpperCase());
        if (!List.of("DONATED", "SPONSORED", "PROVIDED").contains(donation.getDonationType())) {
            throw new com.coordinationsystem.exception.BadRequestException("Loại quyên góp không hợp lệ");
        }
        donation.setNote(command.note());
        donation.setStatus("PENDING");
        Donation savedDonation = repository.save(donation);
        if (command.items() != null) {
            var lines = command.items().stream().map(input -> {
                var line = new com.coordinationsystem.model.DonationItem();
                line.setDonation(savedDonation);
                line.setItem(items.findById(input.itemId()).orElseThrow(() -> new NotFoundException("Không tìm thấy vật tư: " + input.itemId())));
                line.setOfferedQuantity(input.offeredQuantity());
                line.setAcceptedQuantity(java.math.BigDecimal.ZERO);
                line.setReservedQuantity(java.math.BigDecimal.ZERO);
                line.setDeliveredQuantity(java.math.BigDecimal.ZERO);
                line.setItemCondition(input.itemCondition());
                line.setStatus("PENDING");
                return line;
            }).toList();
            donationItems.saveAll(lines);
        }
        return savedDonation;
    }

    public List<Donation> getMine(String email) {
        return repository.findByDonor_EmailIgnoreCaseOrderByCreatedAtDesc(email);
    }

    public Donation cancelByDonor(Long id, String donorEmail) {
        Donation donation = repository.findLockedById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
        if (!donation.getDonor().getEmail().equalsIgnoreCase(donorEmail)) {
            throw new com.coordinationsystem.exception.ForBiddenException("Bạn không thể hủy quyên góp của người khác");
        }
        if (!"PENDING".equals(donation.getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Chỉ có thể hủy quyên góp đang chờ xử lý");
        }
        donation.setStatus("CANCELLED");
        return repository.save(donation);
    }

    public Donation update(Long id, Donation entity) {
        Donation current = getById(id);
        if (!"PENDING".equals(current.getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Chỉ có thể sửa quyên góp đang chờ xử lý");
        }
        current.setPickupAddress(entity.getPickupAddress());
        String type = entity.getDonationType() == null ? "" : entity.getDonationType().trim().toUpperCase();
        if (!List.of("DONATED", "SPONSORED", "PROVIDED").contains(type)) {
            throw new com.coordinationsystem.exception.BadRequestException("Loại quyên góp không hợp lệ");
        }
        current.setDonationType(type);
        current.setNote(entity.getNote());
        return repository.save(current);
    }

    public void delete(Long id) {
        throw new com.coordinationsystem.exception.BadRequestException("Không hỗ trợ xóa quyên góp; hãy dùng thao tác từ chối hoặc hủy");
    }

    public Donation decide(Long id, String reviewerEmail, boolean approve) {
        Donation donation = repository.findLockedById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
        if (!"PENDING".equals(donation.getStatus())) {
            throw new com.coordinationsystem.exception.ConflictException("Chỉ có thể duyệt quyên góp đang chờ xử lý");
        }
        donation.setStatus(approve ? "APPROVED" : "REJECTED");
        donation.setApprovedBy(users.findByEmailIgnoreCase(reviewerEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người duyệt")));
        donation.setApprovedAt(LocalDateTime.now());
        if (approve) {
            var lines = donationItems.findByDonation_Id(id);
            if (lines.isEmpty()) throw new com.coordinationsystem.exception.ConflictException("Quyên góp chưa có vật tư");
            lines.forEach(line -> {
                line.setAcceptedQuantity(line.getOfferedQuantity());
                line.setStatus("ACCEPTED");
                SupplyMovement movement = new SupplyMovement();
                movement.setDonationItem(line);
                movement.setMovementType("ACCEPTED");
                movement.setQuantity(line.getOfferedQuantity());
                movement.setToLocationType("AVAILABLE");
                movement.setReferenceType("DONATION");
                movement.setReferenceId(id);
                movement.setCreatedBy(donation.getApprovedBy());
                movements.save(movement);
            });
            donationItems.saveAll(lines);
        }
        return repository.save(donation);
    }

    private void setEntityId(Donation entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
