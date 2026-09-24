package com.company.ProxyTesting.Classes;

import com.company.ProxyTesting.Annotations.Timing;
import com.company.ProxyTesting.ForProxy.NotificationService;

import java.awt.*;
import java.util.Random;

@Timing
public class EmailNotification  implements NotificationService {
    int age ;

    public EmailNotification(int age) {
        this.age = age;
    }

    @Override
    public void send() {
        System.out.println(" i send to user the message");
    }

    @Override
    public void createMessage(String message) {
        System.out.println("create message to data base");
    }

    @Override
    public Integer getAllMessagesFromUserId(int id) {
        return age;
    }


}
