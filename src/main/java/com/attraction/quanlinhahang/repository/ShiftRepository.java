package com.attraction.quanlinhahang.repository;

import com.attraction.quanlinhahang.model.Shift;
import com.attraction.quanlinhahang.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    Optional<Shift> findByUserAndStatus(User user, Shift.Status status);
}
