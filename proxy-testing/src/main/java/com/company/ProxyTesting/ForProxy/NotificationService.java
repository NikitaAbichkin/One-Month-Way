package com.company.ProxyTesting.ForProxy;

import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;

public interface NotificationService {
    void send ();
    void createMessage(String message);
    Integer getAllMessagesFromUserId(int id);
}
