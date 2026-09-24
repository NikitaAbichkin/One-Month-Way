package com.company.Day1.Practice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.TimeZone;
import java.util.Timer;

public class PaymentHandler implements InvocationHandler {
    private  final  Object paymentService ;

    public PaymentHandler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Instant start = Instant.now();
        System.out.println("Method is "+ method.getName());
        method.invoke(paymentService, args);
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        System.out.println( duration.toMillis() + "ms");
        return null;
    }
}

//method a  {
// paymentHandler.invoke(method,args)
// }
