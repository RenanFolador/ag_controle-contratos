package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationChannel;

public interface NotificationProvider {

    NotificationChannel getChannel();

    void send(Notification notification);
}
