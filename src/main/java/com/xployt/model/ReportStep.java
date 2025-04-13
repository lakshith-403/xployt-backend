package com.xployt.model;

import java.util.List;

public class ReportStep {
    private final int stepNumber;
    private final String description;
    private List<Attachment> attachments;

    public ReportStep(int stepNumber, String description, List<Attachment> attachments) {
        this.stepNumber = stepNumber;
        this.description = description;
        this.attachments = attachments;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public String getDescription() {
        return description;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
