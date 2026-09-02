package com.eva.evagita.dto;

public class DashboardResponse {

    private Long totalTasks;
    private Long todoTasks;
    private Long inProgressTasks;
    private Long doneTasks;
    private Long lowPriorityTasks;
    private Long mediumPriorityTasks;
    private Long highPriorityTasks;
    private Long overdueTasks;

    public DashboardResponse() {
    }

    public Long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Long getTodoTasks() {
        return todoTasks;
    }

    public void setTodoTasks(Long todoTasks) {
        this.todoTasks = todoTasks;
    }

    public Long getInProgressTasks() {
        return inProgressTasks;
    }

    public void setInProgressTasks(Long inProgressTasks) {
        this.inProgressTasks = inProgressTasks;
    }

    public Long getDoneTasks() {
        return doneTasks;
    }

    public void setDoneTasks(Long doneTasks) {
        this.doneTasks = doneTasks;
    }

    public Long getLowPriorityTasks() {
        return lowPriorityTasks;
    }

    public void setLowPriorityTasks(Long lowPriorityTasks) {
        this.lowPriorityTasks = lowPriorityTasks;
    }

    public Long getMediumPriorityTasks() {
        return mediumPriorityTasks;
    }

    public void setMediumPriorityTasks(Long mediumPriorityTasks) {
        this.mediumPriorityTasks = mediumPriorityTasks;
    }

    public Long getHighPriorityTasks() {
        return highPriorityTasks;
    }

    public void setHighPriorityTasks(Long highPriorityTasks) {
        this.highPriorityTasks = highPriorityTasks;
    }

    public Long getOverdueTasks() {
        return overdueTasks;
    }

    public void setOverdueTasks(Long overdueTasks) {
        this.overdueTasks = overdueTasks;
    }
}
