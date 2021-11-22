package com.example.Payroll.Controllers;

import com.example.Payroll.Models.Employee;
import com.example.Payroll.Services.EmployeeService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/employees")
    public CollectionModel<EntityModel<Employee>> all() {

        return employeeService.getEmployees();
    }


    @PostMapping("/employees")
    ResponseEntity<EntityModel<Employee>> newEmployee(@RequestBody Employee newEmployee) {

        return employeeService.newEmployee(newEmployee);
    }

    @GetMapping("/employees/{id}")
    public EntityModel<Employee> one(@PathVariable Long id) {

        return employeeService.getEmployee(id);
    }


    @PutMapping("/employees/{id}")
    public ResponseEntity<EntityModel<Employee>> replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {

        return employeeService.replaceEmployee(newEmployee, id);
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<EntityModel<Employee>> deleteEmployee(@PathVariable Long id) {

        return employeeService.deleteEmployee(id);
    }

}
