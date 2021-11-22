package com.example.Payroll.Services;

import static com.sun.javaws.JnlpxArgs.verify;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.example.Payroll.Components.OrderModelAssembler;
import com.example.Payroll.Controllers.Exceptions.OrderNotFoundException;
import com.example.Payroll.Controllers.OrderController;
import com.example.Payroll.Models.Order;
import com.example.Payroll.Models.Status;
import com.example.Payroll.Ropositories.OrderRepository;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelExtensionsKt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class OrderServiceTest {



    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderModelAssembler assembler;


    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }


    // {{ getOrders
    @Test
    public void getOrdersSuccessful() {
        // give
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

        // give for mock assembler.toModel(any(Order.class))
        EntityModel<Order> orderEntityOne = EntityModel.of(orderOptionalOne.get(),
                    WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderIdOne)).withSelfRel(),
                    linkTo(methodOn(OrderController.class).all()).withRel("orders"));

        EntityModel<Order> orderEntityTwo = EntityModel.of(orderOptionalTwo.get(), //
                    WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderIdTwo)).withSelfRel(),
                    linkTo(methodOn(OrderController.class).all()).withRel("orders"));
        orderEntityTwo.add(linkTo(methodOn(OrderController.class).cancel(orderIdTwo)).withRel("cancel"));
        orderEntityTwo.add(linkTo(methodOn(OrderController.class).complete(orderIdTwo)).withRel("complete"));

        // give for expected
        List<EntityModel<Order>> orderEntityList = new ArrayList<>();
        orderEntityList.add(orderEntityOne);
        orderEntityList.add(orderEntityTwo);

        CollectionModel<EntityModel<Order>> expected = CollectionModel.of(orderEntityList, //
                linkTo(methodOn(OrderController.class).all()).withSelfRel());;


        // mock
        Mockito.when(orderRepository.findAll()).thenReturn(orderList);
        Mockito.when(assembler.toModel(any(Order.class)))
                .thenReturn(orderEntityOne)
                .thenReturn(orderEntityTwo);

        // response
        CollectionModel<EntityModel<Order>> response = orderService.getOrders();
        assertEquals(expected, response);

        Mockito.verify(orderRepository, Mockito.times(1)).findAll();
        Mockito.verify(assembler, Mockito.times(2)).toModel(any(Order.class));
    }
    // }}

    // {{ newOrder
    @Test
    public void newOrderSuccessful_Init_Status_InProgress() {

        Long orderId = 4L;
        // mock save
        Optional<Order> newOrderOptional = java.util.Optional.of(new Order("New Order", Status.IN_PROGRESS));
        newOrderOptional.get().setId(orderId);
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(newOrderOptional.get());

        // mock assembler toModel
        EntityModel<Order> orderEntityModel = EntityModel.of(newOrderOptional.get(),
                WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderId)).withSelfRel(),
                linkTo(methodOn(OrderController.class).all()).withRel("orders"));
        orderEntityModel.add(linkTo(methodOn(OrderController.class).cancel(orderId)).withRel("cancel"));
        orderEntityModel.add(linkTo(methodOn(OrderController.class).complete(orderId)).withRel("complete"));
        Mockito.when(assembler.toModel(any(Order.class))).thenReturn(orderEntityModel);

        // expected
        ResponseEntity<EntityModel<Order>> expected = ResponseEntity //
                .created(linkTo(methodOn(OrderController.class).one(orderId)).toUri()) //
                .body(orderEntityModel);

        // return response entity
        ResponseEntity<EntityModel<Order>> newOrder = orderService.newOrder(newOrderOptional.get());
        assertEquals(expected, newOrder);

        Mockito.verify(orderRepository, Mockito.times(1)).save(any(Order.class));
        Mockito.verify(assembler, Mockito.times(1)).toModel(any(Order.class));
    }

    @Test
    public void newOrderSuccessful_Init_Status_NotIn_InProgress() {

        Long orderId = 4L;
        // mock save
        Optional<Order> newOrderOptional = java.util.Optional.of(new Order("New Order", Status.COMPLETED));
        newOrderOptional.get().setId(orderId);
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(newOrderOptional.get());

        // mock assembler toModel
        EntityModel<Order> orderEntityModel = EntityModel.of(newOrderOptional.get(),
                WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderId)).withSelfRel(),
                linkTo(methodOn(OrderController.class).all()).withRel("orders"));
        orderEntityModel.add(linkTo(methodOn(OrderController.class).cancel(orderId)).withRel("cancel"));
        orderEntityModel.add(linkTo(methodOn(OrderController.class).complete(orderId)).withRel("complete"));
        Mockito.when(assembler.toModel(any(Order.class))).thenReturn(orderEntityModel);

        // expected
        ResponseEntity<EntityModel<Order>> expected = ResponseEntity //
                .created(linkTo(methodOn(OrderController.class).one(orderId)).toUri()) //
                .body(orderEntityModel);

        // return response entity
        ResponseEntity<EntityModel<Order>> newOrder = orderService.newOrder(newOrderOptional.get());
        assertEquals(expected, newOrder);

        Mockito.verify(orderRepository, Mockito.times(1)).save(any(Order.class));
        Mockito.verify(assembler, Mockito.times(1)).toModel(any(Order.class));
    }
    // }}


    // {{ get Order
    @Test
    public void getOrderSuccessful_STATUS_NOTIN_PROGRESS() {

        Long orderId = 1L;
        // mock find by id
        Optional<Order> orderOptional = java.util.Optional.of(new Order("MacBook Pro", Status.COMPLETED));
        orderOptional.get().setId(orderId);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(orderOptional);

        // mock assembler toModel and expected
        EntityModel<Order> expected = EntityModel.of(orderOptional.get(),
                WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderId)).withSelfRel(),
                linkTo(methodOn(OrderController.class).all()).withRel("orders"));
        Mockito.when(assembler.toModel(any(Order.class))).thenReturn(expected);


        // return response entity
        EntityModel<Order> orderEntityModel = orderService.getOrder(orderId);
        assertEquals(expected, orderEntityModel);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verify(assembler, Mockito.times(1)).toModel(any(Order.class));

    }

    @Test
    public void getOrderSuccessful_STATUS_IN_PROGRESS() {

        Long orderId = 2L;
        // mock find by id
        Optional<Order> orderOptional = java.util.Optional.of(new Order("iPhone", Status.IN_PROGRESS));
        orderOptional.get().setId(orderId);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(orderOptional);

        // mock assembler toModel and expected
        EntityModel<Order> expected = EntityModel.of(orderOptional.get(),
                WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderId)).withSelfRel(),
                linkTo(methodOn(OrderController.class).all()).withRel("orders"));
        expected.add(linkTo(methodOn(OrderController.class).cancel(orderId)).withRel("cancel"));
        expected.add(linkTo(methodOn(OrderController.class).complete(orderId)).withRel("complete"));

        Mockito.when(assembler.toModel(any(Order.class))).thenReturn(expected);


        // return response entity
        EntityModel<Order> orderEntityModel = orderService.getOrder(orderId);
        assertEquals(expected, orderEntityModel);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verify(assembler, Mockito.times(1)).toModel(any(Order.class));


    }

    @Test
    public void getOrderFailed() {

        Long orderId = 99L;
        Mockito.when(orderRepository.findById(orderId)).thenThrow(new OrderNotFoundException(orderId));
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(orderId));

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());
    }
    // }}

    // {{ cancel Order
    @Test
    public void cancelOrder_FindOrderSuccessful_ThenStatusNotInProgress_COMPLETED() {

        Long orderId = 1L;
        // mock find by id
        Optional<Order> orderOptional = java.util.Optional.of(new Order("MacBook Pro", Status.COMPLETED));
        orderOptional.get().setId(orderId);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(orderOptional);

        // expected
        ResponseEntity<Problem> expected = ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(Problem.create() //
                        .withTitle("Method not allowed") //
                        .withDetail("You can't cancel an order that is in the " + orderOptional.get().getStatus() + " status"));

        // return response entity
        ResponseEntity<?> orderResponseEntity = orderService.cancelOrder(orderId);
        assertEquals(expected, orderResponseEntity);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());

    }

    @Test
    public void cancelOrder_FindOrderSuccessful_ThenStatusNotInProgress_CANCEL() {

        Long orderId = 3L;
        // mock find by id
        Optional<Order> orderOptional = java.util.Optional.of(new Order("Nokia", Status.CANCELLED));
        orderOptional.get().setId(orderId);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(orderOptional);

        // expected
        ResponseEntity<Problem> expected = ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(Problem.create() //
                        .withTitle("Method not allowed") //
                        .withDetail("You can't cancel an order that is in the " + orderOptional.get().getStatus() + " status"));

        // return response entity
        ResponseEntity<?> orderResponseEntity = orderService.cancelOrder(orderId);
        assertEquals(expected, orderResponseEntity);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());

    }

    @Test
    public void cancelOrder_FindOrderFailedThenThrow() {

        Long orderId = 99L;
        Mockito.when(orderRepository.findById(orderId)).thenThrow(new OrderNotFoundException(orderId));
        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(orderId));

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());
    }

    @Test
    public void cancelOrder_FindOrderSuccessful_ThenStatus_InProgress() {

        Long orderId = 2L;
        // mock find by id
        Optional<Order> orderOptional = java.util.Optional.of(new Order("iPhone", Status.IN_PROGRESS));
        orderOptional.get().setId(orderId);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(orderOptional);

        // mock save
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(orderOptional.get());

        // mock assembler toModel
        EntityModel<Order> orderModel = EntityModel.of(orderOptional.get(),
                WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderId)).withSelfRel(),
                linkTo(methodOn(OrderController.class).all()).withRel("orders"));
        Mockito.when(assembler.toModel(any(Order.class))).thenReturn(orderModel);

        // expected
        ResponseEntity<EntityModel<Order>> expected = ResponseEntity.ok(orderModel);

        // return response entity
        ResponseEntity<?> orderResponseEntity = orderService.cancelOrder(orderId);
        assertEquals(expected, orderResponseEntity);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verify(orderRepository, Mockito.times(1)).save(any(Order.class));
        Mockito.verify(assembler, Mockito.times(1)).toModel(any(Order.class));

    }
    // }}

    // {{ complete Order
    @Test
    public void completeOrder_FindOrderSuccessful_ThenStatus_COMPLETED() {

        Long orderId = 1L;
        // mock find by id
        Optional<Order> orderOptional = java.util.Optional.of(new Order("MacBook Pro", Status.COMPLETED));
        orderOptional.get().setId(orderId);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(orderOptional);

        // expected
        ResponseEntity<Problem> expected = ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(Problem.create() //
                        .withTitle("Method not allowed") //
                        .withDetail("You can't complete an order that is in the " + orderOptional.get().getStatus() + " status"));

        // return response entity
        ResponseEntity<?> orderResponseEntity = orderService.completeOrder(orderId);
        assertEquals(expected, orderResponseEntity);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());

    }

    @Test
    public void completeOrder_FindOrderSuccessful_ThenStatus_CANCEL() {

        Long orderId = 3L;
        // mock find by id
        Optional<Order> orderOptional = java.util.Optional.of(new Order("Nokia", Status.CANCELLED));
        orderOptional.get().setId(orderId);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(orderOptional);

        // expected
        ResponseEntity<Problem> expected = ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(Problem.create() //
                        .withTitle("Method not allowed") //
                        .withDetail("You can't complete an order that is in the " + orderOptional.get().getStatus() + " status"));

        // return response entity
        ResponseEntity<?> orderResponseEntity = orderService.completeOrder(orderId);
        assertEquals(expected, orderResponseEntity);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());

    }

    @Test
    public void completeOrder_FindOrderSuccessful_ThenStatus_InProgress() {

        Long orderId = 2L;
        // mock find by id
        Optional<Order> orderOptional = java.util.Optional.of(new Order("iPhone", Status.IN_PROGRESS));
        orderOptional.get().setId(orderId);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(orderOptional);

        // mock save
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(orderOptional.get());

        // mock assembler toModel
        EntityModel<Order> orderModel = EntityModel.of(orderOptional.get(),
                WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).one(orderId)).withSelfRel(),
                linkTo(methodOn(OrderController.class).all()).withRel("orders"));
        Mockito.when(assembler.toModel(any(Order.class))).thenReturn(orderModel);

        // expected
        ResponseEntity<EntityModel<Order>> expected = ResponseEntity.ok(orderModel);

        // return response entity
        ResponseEntity<?> orderResponseEntity = orderService.completeOrder(orderId);
        assertEquals(expected, orderResponseEntity);

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verify(orderRepository, Mockito.times(1)).save(any(Order.class));
        Mockito.verify(assembler, Mockito.times(1)).toModel(any(Order.class));
    }

    @Test
    public void completeOrder_FindOrderFailedThenThrow() {

        Long orderId = 99L;
        Mockito.when(orderRepository.findById(orderId)).thenThrow(new OrderNotFoundException(orderId));
        assertThrows(OrderNotFoundException.class, () -> orderService.completeOrder(orderId));

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());
    }
    // }}

}
