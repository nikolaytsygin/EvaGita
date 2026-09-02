package com.eva.evagita.dto;

public class TaskStatusStatisticsResponse {

    private Long todo;
    private Long inProgress;
    private Long done;

    public TaskStatusStatisticsResponse() {
    }

    public TaskStatusStatisticsResponse(
            Long todo,
            Long inProgress,
            Long done
    ) {
        this.todo = todo;
        this.inProgress = inProgress;
        this.done = done;
    }

    public Long getTodo() {
        return todo;
    }

    public void setTodo(Long todo) {
        this.todo = todo;
    }

    public Long getInProgress() {
        return inProgress;
    }

    public void setInProgress(Long inProgress) {
        this.inProgress = inProgress;
    }

    public Long getDone() {
        return done;
    }

    public void setDone(Long done) {
        this.done = done;
    }
}
