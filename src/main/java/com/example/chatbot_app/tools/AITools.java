package com.example.chatbot_app.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AITools {
    @Tool(name = "getEmployee", description = "Get information about a given employee")
    public Employee getEmployee(@ToolParam(description = "The employee name") String name) {
        return new Employee(name, 12300, 4);
    }

    @Tool(description = "Get All Employees")
    public List<Employee> getAllEmployees() {
        return List.of(
                new Employee("Hassan", 12300, 4),
                new Employee("Mohamed", 34000, 1),
                new Employee("Imane", 23000, 10)
        );
    }
}

record  Employee(String name, double salary, int seniority){

}
