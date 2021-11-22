package com.example.Payroll.Ropositories;

import com.example.Payroll.Models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
