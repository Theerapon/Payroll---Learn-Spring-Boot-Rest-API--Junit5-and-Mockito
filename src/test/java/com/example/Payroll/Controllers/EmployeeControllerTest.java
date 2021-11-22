package com.example.Payroll.Controllers;

import com.example.Payroll.Controllers.Exceptions.EmployeeNotFoundException;
import com.example.Payroll.Models.Employee;
import com.example.Payroll.Services.EmployeeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    // {{ get All Employee
    @Test
    public void getAllEmployees_Successful() throws Exception {

        // give
        Long employeeIdOne = 1L;
        Optional<Employee> employeeOptionalOne = java.util.Optional.of(new Employee("Bilbo", "Baggins", "burglar"));
        employeeOptionalOne.get().setId(employeeIdOne);

        Long employeeIdTwo = 2L;
        Optional<Employee> employeeOptionalTwo = java.util.Optional.of(new Employee("Frodo", "Baggins", "thief"));
        employeeOptionalTwo.get().setId(employeeIdTwo);

        List<Employee> employeeList = new ArrayList<Employee>();
        employeeList.add(employeeOptionalOne.get());
        employeeList.add(employeeOptionalTwo.get());

        List<EntityModel<Employee>> employeeEntityList = employeeList.stream().map((employee) -> {
            return EntityModel.of(employee, //
                    linkTo(methodOn(EmployeeController.class).one(employee.getId())).withSelfRel(),
                    linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
        }).collect(Collectors.toList());

        CollectionModel<EntityModel<Employee>> expected = CollectionModel.of(employeeEntityList, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());

        Mockito.when(employeeService.getEmployees()).thenReturn(expected);

        ResultActions response = this.mockMvc.perform(get("/employees"))
                .andDo(print())
                .andExpect(status().isOk());

    }
    // }}

    // {{ new Employee
    @Test
    public void newEmployee_Successful() throws Exception {

        Long employeeId = 3L;
        Optional<Employee> newEmployeeOptional = java.util.Optional.of(new Employee("fakie", "nanoi", "noob"));
        newEmployeeOptional.get().setId(employeeId);

        EntityModel<Employee> employeeEntityModel = EntityModel.of(newEmployeeOptional.get(), //
                linkTo(methodOn(EmployeeController.class).one(employeeId)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));

        ResponseEntity<EntityModel<Employee>> expected = ResponseEntity //
                .created(employeeEntityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(employeeEntityModel);

        Mockito.when(employeeService.newEmployee(any(Employee.class))).thenReturn(expected);

        ResultActions response = this.mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"fistName\":\"fakie\", \"lastName\":\"nanoi\", \"role\":\"noob\"}"))
                .andDo(print())
                .andExpect(status().isCreated());

    }

    @Test
    public void newEmployee_BadRequest() throws Exception {

        ResultActions response = this.mockMvc.perform(post("/employees"))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }
    // }}

    // {{ get Employee
    @Test
    public void getEmployee_Successful() throws Exception {

        Long employeeId = 1L;
        Optional<Employee> employeeOptional = java.util.Optional.of(new Employee("Bilbo", "Baggins", "burglar"));
        employeeOptional.get().setId(employeeId);

        // mock assembler toModel and expected
        EntityModel<Employee> expected = EntityModel.of(employeeOptional.get(), //
                linkTo(methodOn(EmployeeController.class).one(employeeId)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));

        Mockito.when(employeeService.getEmployee(employeeId)).thenReturn(expected);

        ResultActions response = this.mockMvc.perform(get("/employees/1")
                .param("id", employeeId.toString()))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    public void getEmployee_Notfound() throws Exception {

        Long employeeId = 99L;
        Mockito.when(employeeService.getEmployee(employeeId)).thenThrow(new EmployeeNotFoundException(employeeId));

        ResultActions response = this.mockMvc.perform(get("/employees/99")
                .param("id", employeeId.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    public void getEmployee_BadRequest() throws Exception {

        ResultActions response = this.mockMvc.perform(get("/employees/q"))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }
    // }}

    // {{ replaceEmployee
    @Test
    public void replaceEmployee_Successful() throws Exception {

        Long employeeId = 1L;
        Optional<Employee> employeeOptional = java.util.Optional.of(new Employee("fakie", "nanoi", "noob"));
        employeeOptional.get().setId(employeeId);

        EntityModel<Employee> entityModelEmployee = EntityModel.of(employeeOptional.get(), //
                linkTo(methodOn(EmployeeController.class).one(employeeId)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));

        // expected
        ResponseEntity<EntityModel<Employee>> expected = ResponseEntity //
                .created(entityModelEmployee.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModelEmployee);

        Mockito.when(employeeService.replaceEmployee(any(Employee.class), anyLong())).thenReturn(expected);

        ResultActions response = this.mockMvc.perform(put("/employees/1")
                .param("id", employeeId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"fistName\":\"fakie\", \"lastName\":\"nanoi\", \"role\":\"noob\"}"))
                .andDo(print())
                .andExpect(status().isCreated());

    }

    @Test
    public void replaceEmployee_BadRequest() throws Exception {

        ResultActions response = this.mockMvc.perform(put("/employees/1"))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }
    // }}

    // {{ delete Employee
    @Test
    public void deleteEmployee_Successful() throws Exception {

        Long employeeId = 1L;
        // expected
        ResponseEntity<EntityModel<Employee>> expected = ResponseEntity.noContent().build();

        Mockito.when(employeeService.deleteEmployee(anyLong())).thenReturn(expected);

        ResultActions response = this.mockMvc.perform(delete("/employees/1")
                .param("id", employeeId.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

    }

    @Test
    public void deleteEmployee_BadRequest() throws Exception {

        ResultActions response = this.mockMvc.perform(delete("/employees/s"))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }
    // }}
}
