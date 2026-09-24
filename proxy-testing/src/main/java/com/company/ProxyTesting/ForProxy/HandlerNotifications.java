package com.company.ProxyTesting.ForProxy;

import tools.jackson.databind.util.RecordUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

// Handler нужен для того чтобы внутри методов прокси дергать его и передвать парметры
public class HandlerNotifications implements InvocationHandler {
    private  final NotificationService notificationService;

    public HandlerNotifications(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var result =  method.invoke( notificationService, args );
        return result== null ? null: result;
    }

}
