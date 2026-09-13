package com.company.Day1.Practice;

import java.lang.management.ThreadInfo;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    static void main() {
        PaymentService paymentService = new PaymentServiceImpl();
        PaymentHandler paymentHandler = new PaymentHandler(paymentService);

        PaymentService paymentServiceProxy =(PaymentService)
                Proxy.newProxyInstance(PaymentService.class.getClassLoader(), new Class[]{PaymentService.class}, paymentHandler);
        paymentServiceProxy.refund(10);


    }
}
