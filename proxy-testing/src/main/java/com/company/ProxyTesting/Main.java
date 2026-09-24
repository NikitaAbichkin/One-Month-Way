package com.company.ProxyTesting;

import com.company.ProxyTesting.Classes.EmailNotification;
import com.company.ProxyTesting.ForProxy.HandlerNotifications;
import com.company.ProxyTesting.ForProxy.NotificationService;

import java.lang.reflect.Proxy;

public class Main {
    static void main() {
        NotificationService notificationService = new EmailNotification(10);
        NotificationService notificationService1 = new EmailNotification(111);

        // создал прокси который
        notificationService = (NotificationService) Proxy.newProxyInstance
                (EmailNotification.class.getClassLoader(),
                        new Class[]{NotificationService.class},
                        new HandlerNotifications( notificationService)
                        );


       System.out.println( notificationService.getAllMessagesFromUserId(10));
    }
}

// могу положить в handler положить любой обьект который имплементирует NotficationService
