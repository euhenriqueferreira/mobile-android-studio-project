package com.henrique.teamsync;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Task {
    private String taskId;
    private String name;
    private String description;
    private String deadline;
    private String projectId;
    private boolean completed;

    public Task() {
    }

    public Task(String taskId, String name, String description, String deadline, String projectId) {
        this.taskId = taskId;
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.projectId = projectId;
        this.completed = false;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public String getDeadline() {
        return deadline;
    }

    public Date getDateDeadline(){
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        Date data = null;

        try {
            data = formato.parse(getDeadline());
            System.out.println("Data convertida: " + data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
    public String getProjectId() {
        return projectId;
    }
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    public boolean isCompleted() {
        return completed;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}