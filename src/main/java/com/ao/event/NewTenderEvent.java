package com.ao.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NewTenderEvent extends ApplicationEvent {

    private final int newCount;

    public NewTenderEvent(Object source, int newCount) {
        super(source);
        this.newCount = newCount;
    }
}
