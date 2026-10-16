package com.company.order;

public class Order {

    private final Long id;
    private final String customerEmail;
    private final Integer amount;
    private String status;

    public Order(Long id, String customerEmail, Integer amount, String status) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
