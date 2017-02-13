package org.openhab.binding.insteonplm.internal.device;

import org.eclipse.smarthome.core.events.AbstractEvent;

public class InsteonPLMEvent extends AbstractEvent {
    public InsteonPLMEvent(String topic, String payload, String source) {
        super(topic, payload, source);
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return null;
    }

}
