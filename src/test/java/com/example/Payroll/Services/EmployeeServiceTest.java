package com.example.Payroll.Services;

import static org.junit.jupiter.api.Assertions.*;

import com.example.Payroll.Components.EmployeeModelAssembler;
import com.example.Payroll.Controllers.EmployeeController;
import com.example.Payroll.Controllers.Exceptions.EmployeeNotFoundException;
import com.example.Payroll.Models.Employee;
import com.example.Payroll.Models.Order;
import com.example.Payroll.Ropositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class EmployeeServiceTest {

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeModelAssembler assembler;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    // {{ get Employees
    @Test
    public void getEmployeesSuccessful() {

        // give
        Long employeeIdOne = 1L;
        Optional<Employee> employeeOptionalOne = java.util.Optional.of(new Employee("Bilbo", "Baggins", "burglar"));
        employeeOptionalOne.get().setId(employeeIdOne);

        Long employeeIdTwo = 2L;
        Optional<Employee> employeeOptionalTwo = java.util.Optional.of(new Employee("Frodo", "Baggins", "thief"));
        employeeOptionalTwo.get().setId(employeeIdTwo);

        // give for mock employeeRepository.fineAll()
        List<Employee> employeeList = new ArrayList<Employee>();
        employeeList.add(employeeOptionalOne.get());
        employeeList.add(employeeOptionalTwo.get());

        List<EntityModel<Employee>> employeeEntityList = employeeList.stream().map((employee) -> {
            return EntityModel.of(employee, //
                    linkTo(methodOn(EmployeeController.class).one(employee.getId())).withSelfRel(),
                    linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
        }).collect(Collectors.toList());

        CollectionModel<EntityModel<Employee>> expected = CollectionModel.of(employeeEntityList, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());

        // mock assembler.toModel(anyList())
        Mockito.when(assembler.toModel(anyList())).thenReturn(expected);

        CollectionModel<EntityModel<Employee>> response = employeeService.getEmployees();
        assertEquals(expected, response);

        Mockito.verify(employeeRepository, Mockito.times(1)).findAll();
        Mockito.verify(assembler, Mockito.times(1)).toModel(anyList());

    }
    // }}


    // {{ new Employee
    @Test
    public void newEmployeeSuccessful() {

        Long employeeId = 3L;
        // mock save
        Optional<Employee> newEmployeeOptional = java.util.Optional.of(new Employee("fakie", "nanoi", "noob"));
        newEmployeeOptional.get().setId(employeeId);
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(newEmployeeOptional.get());

        // mock assembler toModel
        EntityModel<Employee> employeeEntityModel = EntityModel.of(newEmployeeOptional.get(), //
                linkTo(methodOn(EmployeeController.class).one(employeeId)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));

        Mockito.when(assembler.toModel(any(Employee.class))).thenReturn(employeeEntityModel);

        // expected
        ResponseEntity<EntityModel<Employee>> expected = ResponseEntity //
                .created(employeeEntityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(employeeEntityModel);

        // return response entity
        ResponseEntity<?> responseEntityEmployee = employeeService.newEmployee(newEmployeeOptional.get());
        assertEquals(expected, responseEntityEmployee);

        Mockito.verify(employeeRepository, Mockito.times(1)).save(any(Employee.class));
        Mockito.verify(assembler, Mockito.times(1)).toModel(any(Employee.class));
    }

    // }}

    // {{ get Employee
    @Test void getEmployeeSuccessful() {

        Long employeeId = 1L;
        Optional<Employee> employeeOptional = java.util.Optional.of(new Employee("Bilbo", "Baggins", "burglar"));
        employeeOptional.get().setId(employeeId);
        Mockito.when(employeeRepository.findById(anyLong())).thenReturn(employeeOptional);

        // mock assembler toModel and expected
        EntityModel<Employee> expected = EntityModel.of(employeeOptional.get(), //
                linkTo(methodOn(EmployeeController.class).one(employeeId)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
        Mockito.when(assembler.toModel(any(Employee.class))).thenReturn(expected);

        // return response entity
        EntityModel<Employee> employeeEntityModel = employeeService.getEmployee(employeeId);
        assertEquals(expected, employeeEntityModel);

        Mockito.verify(employeeRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verify(assembler, Mockito.times(1)).toModel(any(Employee.class));

    }

    @Test void getEmployeeFailed() {

        Long employeeId = 99L;
        Mockito.when(employeeRepository.findById(employeeId)).thenThrow(new EmployeeNotFoundException(employeeId));
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployee(employeeId));

        Mockito.verify(employeeRepository, Mockito.times(1)).findById(anyLong());

    }
    // }}

    // {{ replace Employee

    @Test void replaceEmployee_NewEmployee() {

        Long employeeId = 3L;
        Optional<Employee> employeeOptional = java.util.Optional.of(new Employee("New", "Employee", "Em"));
        employeeOptional.get().setId(employeeId);
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(employeeOptional.get());

        // mock assembler toModel and expected
        EntityModel<Employee> entityModelEmployee = EntityModel.of(employeeOptional.get(), //
                linkTo(methodOn(EmployeeController.class).one(employeeId)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
        Mockito.when(assembler.toModel(any(Employee.class))).thenReturn(entityModelEmployee);

        // expected
        ResponseEntity<EntityModel<Employee>> expected = ResponseEntity //
                .created(entityModelEmployee.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModelEmployee);

        // return response
        ResponseEntity<?> employeeResponse = employeeService.replaceEmployee(employeeOptional.get(), 3L);
        assertEquals(expected, employeeResponse);

        Mockito.verify(employeeRepository, Mockito.times(1)).save(any(Employee.class));
        Mockito.verify(assembler, Mockito.times(1)).toModel(any(Employee.class));

    }

    @Test void replaceEmployee_ExistEmployee() {

        Long employeeId = 1L;
        Optional<Employee> employeeOptional = java.util.Optional.of(new Employee("New", "Employee", "Em"));
        employeeOptional.get().setId(employeeId);
        // Find Employee Successful
        Mockito.when(employeeRepository.findById(anyLong())).thenReturn(employeeOptional);
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(employeeOptional.get());


        // mock assembler toModel and expected
        EntityModel<Employee> entityModelEmployee = EntityModel.of(employeeOptional.get(), //
                linkTo(methodOn(EmployeeController.class).one(employeeId)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
        Mockito.when(assembler.toModel(any(Employee.class))).thenReturn(entityModelEmployee);
//
        // expected
        ResponseEntity<EntityModel<Employee>> expected = ResponseEntity //
                .created(entityModelEmployee.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModelEmployee);
//
        // return response
        ResponseEntity<?> employeeResponse = employeeService.replaceEmployee(employeeOptional.get(), employeeId);
        assertEquals(expected, employeeResponse);

        Mockito.verify(employeeRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verify(employeeRepository, Mockito.times(1)).save(any(Employee.class));
        Mockito.verify(assembler, Mockito.times(1)).toModel(any(Employee.class));

    }

    // }}


    // {{ delete employee
    @Test
    public void deleteEmployee() {

        Long employeeId = 1L;

        // expected
        ResponseEntity<Object> expected = ResponseEntity.noContent().build();

        // return response
        ResponseEntity<?> responseEntity = employeeService.deleteEmployee(employeeId);

        assertEquals(expected, responseEntity);
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployee(employeeId));

        Mockito.verify(employeeRepository, Mockito.times(1)).deleteById(anyLong());


    }
    // }}

}
