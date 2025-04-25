package com.xployt.model;

public class ProjectPayment {
    private int paymentId;
    private int reportId;
    private int hackerId;
    private double amount;
    private String paymentDate;

    public ProjectPayment() {
    }

    public ProjectPayment(int paymentId, int reportId, int hackerId, double amount, String paymentDate) {
        this.paymentId = paymentId;
        this.reportId = reportId;
        this.hackerId = hackerId;
        this.amount = amount;
        this.paymentDate = paymentDate;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getHackerId() {
        return hackerId;
    }

    public void setHackerId(int hackerId) {
        this.hackerId = hackerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
} 