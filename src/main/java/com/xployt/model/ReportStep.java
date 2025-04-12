package com.xployt.model;

import java.util.List;

public class ReportStep {
    private final String stepNumber;
    private final String description;
    private final List<Attachment> attachments;

    public ReportStep(String stepNumber, int reportId, String description, List<Attachment> attachments) {
        this.stepNumber = stepNumber;
        this.description = description;
        this.attachments = attachments;
    }

    public String getStepNumber() {
        return stepNumber;
    }

    public String getDescription() {
        return description;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> filePaths) {
        for (int i = 0; i < filePaths.size(); i++) {
            this.attachments.get(i).setUrl(filePaths.get(i));
        }
    }
}
