package com.company.Day1.Practice;

public class PaymentServiceImpl  implements  PaymentService{
    @Override
    public void pay(int amount) {
        System.out.println("Some Logic to pay");
    }

    @Override
    public void refund(int amount) {
        System.out.println("Some Logic to refund");
    }
}
