package com.autocare.observer;

import com.autocare.model.entity.ServiceRecord;
import org.springframework.context.ApplicationEvent;

/**
 * OBSERVER PATTERN — Event (notification from Subject)
 * Published by ServiceRequestServiceImpl on service completion.
 * Spring ApplicationEvent = the event bus. No manual observer registration.
 */
public class ServiceCompletionEvent extends ApplicationEvent {

    private final ServiceRecord serviceRecord;

    public ServiceCompletionEvent(Object source, ServiceRecord serviceRecord) {
        super(source);
        this.serviceRecord = serviceRecord;
    }

    public ServiceRecord getServiceRecord() { return serviceRecord; }
}