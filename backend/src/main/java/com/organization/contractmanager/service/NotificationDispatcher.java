package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.NotificationSchedule;

@FunctionalInterface
public interface NotificationDispatcher {

    void dispatch(NotificationSchedule schedule);
}
