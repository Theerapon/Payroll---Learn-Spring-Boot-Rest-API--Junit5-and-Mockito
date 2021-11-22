package com.example.Payroll.Services;

import com.example.Payroll.Components.EmployeeModelAssembler;
import com.example.Payroll.Controllers.Exceptions.EmployeeNotFoundException;
import com.example.Payroll.Models.Employee;
import com.example.Payroll.Ropositories.EmployeeRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final EmployeeModelAssembler assembler;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeModelAssembler assembler) {
        this.employeeRepository = employeeRepository;
        this.assembler = assembler;
    }

    public CollectionModel<EntityModel<Employee>> getEmployees() {

        List<EntityModel<Employee>> employees = employeeRepository.findAll().stream() //
                .map((employee) -> assembler.toModel(employee)) //
                .collect(Collectors.toList());

        return assembler.toModel(employees);
    }

    public ResponseEntity<EntityModel<Employee>> newEmployee(Employee newEmployee) {

        EntityModel<Employee> entityModel = assembler.toModel(employeeRepository.save(newEmployee));

        return ResponseEntity //
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModel);
    }

    public EntityModel<Employee> getEmployee(@PathVariable Long id) {

        Employee employee = employeeRepository.findById(id) //
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        return assembler.toModel(employee);
    }

    public ResponseEntity<EntityModel<Employee>> replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {

        Employee updatedEmployee = employeeRepository.findById(id) //
                .map(employee -> {
                    employee.setName(newEmployee.getName());
                    employee.setRole(newEmployee.getRole());
                    return employeeRepository.save(employee);
                }) //
                .orElseGet(() -> {
                    newEmployee.setId(id);
                    return employeeRepository.save(newEmployee);
                });

        EntityModel<Employee> entityModel = assembler.toModel(updatedEmployee);

        return ResponseEntity //
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModel);
    }

    public ResponseEntity<EntityModel<Employee>> deleteEmployee(@PathVariable Long id) {

        employeeRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
