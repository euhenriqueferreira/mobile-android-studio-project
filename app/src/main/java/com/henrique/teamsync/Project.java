package com.henrique.teamsync;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Project {
    private String projectId;
    private String projectName;
    private String projectDescription;
    private Timestamp projectDeadline;
    private String createdBy;
    public Project() {
    }
    public Project(String projectId, String projectName, String projectDescription, Timestamp projectDeadline, String createdBy) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectDeadline = projectDeadline;
        this.createdBy = createdBy;
    }

    public String getProjectId()
    {
        return projectId;
    }

    public void setProjectId(String projectId)
    {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public Timestamp getProjectDeadline() {
        return projectDeadline;
    }

    public void setProjectDeadline(Timestamp projectDeadline) {
        this.projectDeadline = projectDeadline;
    }

    public String getFormattedDeadline() {
        if (projectDeadline != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(projectDeadline.toDate()); // Converte Timestamp para Date antes de formatar
        } else {
            return "Data de entrega não definida";
        }
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}