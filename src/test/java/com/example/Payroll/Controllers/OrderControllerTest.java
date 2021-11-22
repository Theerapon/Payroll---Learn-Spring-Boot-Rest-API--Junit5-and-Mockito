package com.example.Payroll.Controllers;

import com.example.Payroll.Controllers.Exceptions.OrderNotFoundException;
import com.example.Payroll.Models.Order;
import com.example.Payroll.Models.Status;
import com.example.Payroll.Services.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    // {{ getOrder
    @Test
    public void getOrder_Successful() throws Exception {
        Long orderId = 1L;
        Optional<Order> orderOptional = java.util.Optional.of(new Order("MacBook Pro", Status.COMPLETED));
        orderOptional.get().setId(orderId);

        EntityModel<Order> expected = EntityModel.of(orderOptional.get(),
                WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderId)).withSelfRel(),
                linkTo(methodOn(OrderController.class).all()).withRel("orders"));

        Mockito.when(orderService.getOrder(orderId)).thenReturn(expected);

        ResultActions response = this.mockMvc.perform(get("/orders/1")
                .param("id", orderId.toString()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getOrder_BadRequest() throws Exception {
        Long orderId = 1L;
        Optional<Order> orderOptional = java.util.Optional.of(new Order("MacBook Pro", Status.COMPLETED));
        orderOptional.get().setId(orderId);

        EntityModel<Order> expected = EntityModel.of(orderOptional.get(),
                WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderId)).withSelfRel(),
                linkTo(methodOn(OrderController.class).all()).withRel("orders"));

        Mockito.when(orderService.getOrder(orderId)).thenReturn(expected);

        ResultActions response = this.mockMvc.perform(get("/orders/s"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getOrder_NotFound() throws Exception {

        Long orderId = 99L;
        Mockito.when(orderService.getOrder(orderId)).thenThrow(new OrderNotFoundException(orderId));

        ResultActions response = this.mockMvc.perform(get("/orders/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    // }}

    // {{ get All order
    @Test
    public void getAllOrders_Successful() throws Exception {

        Long orderIdOne = 1L;
        Optional<Order> orderOptionalOne = java.util.Optional.of(new Order("MacBook Pro", Status.COMPLETED));
        orderOptionalOne.get().setId(orderIdOne);

        Long orderIdTwo = 2L;
        Optional<Order> orderOptionalTwo = java.util.Optional.of(new Order("iPhone", Status.IN_PROGRESS));
        orderOptionalTwo.get().setId(orderIdTwo);

        // give for mock orderRepository.fineAll()
        List<Order> orderList = new ArrayList<>();
        orderList.add(orderOptionalOne.get());
        orderList.add(orderOptionalTwo.get());

        List<EntityModel<Order>> orderEntityList = orderList.stream().map((order) -> {
            EntityModel<Order> orderEntity = EntityModel.of(orderOptionalOne.get(),
                    linkTo(methodOn(OrderController.class).one(orderIdOne)).withSelfRel(),
                    linkTo(methodOn(OrderController.class).all()).withRel("orders"));

            if (order.getStatus() == Status.IN_PROGRESS) {
                orderEntity.add(linkTo(methodOn(OrderController.class).cancel(orderIdTwo)).withRel("cancel"));
                orderEntity.add(linkTo(methodOn(OrderController.class).complete(orderIdTwo)).withRel("complete"));
            }

            return orderEntity;
        }).collect(Collectors.toList());

        CollectionModel<EntityModel<Order>> expected = CollectionModel.of(orderEntityList, //
                linkTo(methodOn(OrderController.class).all()).withSelfRel());

        Mockito.when(orderService.getOrders()).thenReturn(expected);

        ResultActions response = this.mockMvc.perform(get("/orders"))
                .andDo(print())
                .andExpect(status().isOk());
    }
    // }}

    // {{ new Order
    @Test
    public void newOrder_Successful() throws Exception {

        Long orderId = 4L;
        Optional<Order> newOrderOptional = java.util.Optional.of(new Order("New Order", Status.IN_PROGRESS));
        newOrderOptional.get().setId(orderId);

        EntityModel<Order> orderEntityModel = EntityModel.of(newOrderOptional.get(),
                WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderId)).withSelfRel(),
                linkTo(methodOn(OrderController.class).all()).withRel("orders"));
        orderEntityModel.add(linkTo(methodOn(OrderController.class).cancel(orderId)).withRel("cancel"));
        orderEntityModel.add(linkTo(methodOn(OrderController.class).complete(orderId)).withRel("complete"));

        // expected
        ResponseEntity<EntityModel<Order>> expected = ResponseEntity //
                .created(linkTo(methodOn(OrderController.class).one(orderId)).toUri()) //
                .body(orderEntityModel);

        Mockito.when(orderService.newOrder(any(Order.class))).thenReturn(expected);

        ResultActions response = this.mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"description\":\"New Order\", \"status\":\"IN_PROGRESS\"}"))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void newOrder_BadRequest() throws Exception {

        ResultActions response = this.mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    // }}

    // {{ cancel Order
    @Test
    public void cancelOrder_Successful() throws Exception {

        Long orderId = 3L;
        Optional<Order> orderOptional = java.util.Optional.of(new Order("iPhone", Status.IN_PROGRESS));
        orderOptional.get().setId(orderId);
        orderOptional.get().setStatus(Status.CANCELLED);
        EntityModel<Order> orderModel = EntityModel.of(orderOptional.get(),
                WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderId)).withSelfRel(),
                linkTo(methodOn(OrderController.class).all()).withRel("orders"));

        ResponseEntity<EntityModel<Order>> expected = ResponseEntity.ok(orderModel);

        Mockito.when(orderService.cancelOrder(orderId)).thenReturn((ResponseEntity) expected);

        ResultActions response = this.mockMvc.perform(delete("/orders/3/cancel")
                .param("id", orderId.toString()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void cancelOrder_Not_Allowed() throws Exception {
        Long orderId = 1L;
        Optional<Order> orderOptional = java.util.Optional.of(new Order("MacBook Pro", Status.COMPLETED));
        orderOptional.get().setId(orderId);

        ResponseEntity<Problem> expected = ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(Problem.create() //
                        .withTitle("Method not allowed") //
                        .withDetail("You can't cancel an order that is in the " + orderOptional.get().getStatus() + " status"));

        Mockito.when(orderService.cancelOrder(orderId)).thenReturn((ResponseEntity) expected);

        ResultActions response = this.mockMvc.perform(delete("/orders/1/cancel")
                .param("id", orderId.toString()))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void cancelOrder_BadRequest() throws Exception {
        ResultActions response = this.mockMvc.perform(delete("/orders/s/cancel"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    //}}

    // complete Order
    @Test
    public void completeOrder_Successful() throws Exception {
        Long orderId = 3L;
        Optional<Order> orderOptional = java.util.Optional.of(new Order("iPhone", Status.IN_PROGRESS));
        orderOptional.get().setId(orderId);
        orderOptional.get().setStatus(Status.COMPLETED);
        EntityModel<Order> orderModel = EntityModel.of(orderOptional.get(),
                WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderId)).withSelfRel(),
                linkTo(methodOn(OrderController.class).all()).withRel("orders"));

        ResponseEntity<EntityModel<Order>> expected = ResponseEntity.ok(orderModel);

        Mockito.when(orderService.completeOrder(orderId)).thenReturn((ResponseEntity)expected);

        ResultActions response = this.mockMvc.perform(put("/orders/3/complete")
                .param("id", orderId.toString()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void completeOrder_Not_Allowed() throws Exception {
        Long orderId = 1L;
        Optional<Order> orderOptional = java.util.Optional.of(new Order("MacBook Pro", Status.COMPLETED));
        orderOptional.get().setId(orderId);

        ResponseEntity<Problem> expected = ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(Problem.create() //
                        .withTitle("Method not allowed") //
                        .withDetail("You can't complete an order that is in the " + orderOptional.get().getStatus() + " status"));

        Mockito.when(orderService.completeOrder(orderId)).thenReturn((ResponseEntity) expected);

        ResultActions response = this.mockMvc.perform(put("/orders/1/complete")
                .param("id", orderId.toString()))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void completeOrder_BadRequest() throws Exception {
        ResultActions response = this.mockMvc.perform(put("/orders/s/complete"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    // }}

}
