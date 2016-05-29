package org.openhab.binding.zwave.event;

import org.eclipse.smarthome.core.events.AbstractEvent;

public class BindingEvent extends AbstractEvent {
    public final static String TYPE = BindingEvent.class.getSimpleName();

    public BindingEvent(String topic, String binding, String payload) {
        super(topic, payload, binding);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}