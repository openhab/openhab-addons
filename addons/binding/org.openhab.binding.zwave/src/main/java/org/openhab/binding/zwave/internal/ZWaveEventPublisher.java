package org.openhab.binding.zwave.internal;

import org.eclipse.smarthome.core.events.EventPublisher;

public class ZWaveEventPublisher {
    private static EventPublisher eventPublisher;

    public void setEventPublisher(EventPublisher eventPublisher) {
        ZWaveEventPublisher.eventPublisher = eventPublisher;
    }

    public void unsetEventPublisher(EventPublisher eventPublisher) {
        ZWaveEventPublisher.eventPublisher = null;
    }

    public static EventPublisher getEventPublisher() {
        return eventPublisher;
    }
}
