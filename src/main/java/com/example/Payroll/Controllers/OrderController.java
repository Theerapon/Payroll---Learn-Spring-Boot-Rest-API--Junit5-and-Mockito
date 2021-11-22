package com.example.Payroll.Controllers;

import com.example.Payroll.Models.Order;
import com.example.Payroll.Services.OrderService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public CollectionModel<EntityModel<Order>> all() {

        return orderService.getOrders();
    }

    @GetMapping("/orders/{id}")
    public EntityModel<Order> one(@PathVariable Long id) {

        return orderService.getOrder(id);
    }

    @PostMapping("/orders")
    ResponseEntity<EntityModel<Order>> newOrder(@RequestBody Order order) {

        return orderService.newOrder(order);
    }

    @DeleteMapping("/orders/{id}/cancel")
    public ResponseEntity<EntityModel<Order>> cancel(@PathVariable Long id) {

        return (ResponseEntity<EntityModel<Order>>) orderService.cancelOrder(id);
    }

    @PutMapping("/orders/{id}/complete")
    public ResponseEntity<EntityModel<Order>> complete(@PathVariable Long id) {

        return (ResponseEntity<EntityModel<Order>>) orderService.completeOrder(id);
    }
}
