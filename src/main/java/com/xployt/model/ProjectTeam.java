package com.xployt.model;

import java.util.List;

public class ProjectTeam {
    private String projectId;
    private PublicUser client;
    private PublicUser projectLead;
    private List<PublicUser> projectValidators;
    private List<PublicUser> projectHackers;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public PublicUser getClient() {
        return client;
    }

    public void setClient(PublicUser client) {
        this.client = client;
    }

    public PublicUser getProjectLead() {
        return projectLead;
    }

    public void setProjectLead(PublicUser projectLead) {
        this.projectLead = projectLead;
    }

    public List<PublicUser> getProjectValidators() {
        return projectValidators;
    }

    public void setProjectValidators(List<PublicUser> projectValidators) {
        this.projectValidators = projectValidators;
    }

    public List<PublicUser> getProjectHackers() {
        return projectHackers;
    }

    public void setProjectHackers(List<PublicUser> projectHackers) {
        this.projectHackers = projectHackers;
    }
}
