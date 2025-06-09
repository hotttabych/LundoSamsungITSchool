package io.whyscape.lundo.domain.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Todo {
    private int id;
    private String task;
    private boolean isCompleted;

    public Todo() {
        this(0, "", false);
    }

    public Todo(int id, String task) {
        this(id, task, false);
    }

    public Todo(int id, String task, boolean isCompleted) {
        this.id = id;
        this.task = task;
        this.isCompleted = isCompleted;
    }

    public int getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Todo)) return false;
        Todo todo = (Todo) o;
        return id == todo.id &&
                isCompleted == todo.isCompleted &&
                Objects.equals(task, todo.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, task, isCompleted);
    }

    @NonNull
    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", task='" + task + '\'' +
                ", isCompleted=" + isCompleted +
                '}';
    }
}
