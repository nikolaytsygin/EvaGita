package com.eva.evagita.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectRequest {

    @NotBlank(message = "Project name must not be blank")
    @Size(max = 100, message = "Project name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Project description must not exceed 1000 characters")
    private String description;

    public ProjectRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
