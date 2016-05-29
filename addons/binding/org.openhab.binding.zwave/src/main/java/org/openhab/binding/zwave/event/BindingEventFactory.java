package org.openhab.binding.zwave.event;

import java.util.Set;

import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;

import com.google.common.collect.Sets;

public class BindingEventFactory extends AbstractEventFactory {
    private static final String BINDING_EVENT_TOPIC = "smarthome/binding/{binding}/{entity}/{event}";

    public BindingEventFactory(Set<String> supportedEventTypes) {
        super(supportedEventTypes);
    }

    public BindingEventFactory() {
        super(Sets.newHashSet(BindingEvent.TYPE));
    }

    @Override
    protected Event createEventByType(String eventType, String topic, String payload, String source) throws Exception {
        if (eventType.equals(BindingEvent.TYPE)) {
            return new BindingEvent(topic, source, payload);
        }

        throw new IllegalArgumentException(
                eventType + " not supported by " + BindingEventFactory.class.getSimpleName());
    }

    /**
     *
     * @param binding
     * @param entity
     * @param event
     * @param properties
     * @return
     */
    public static BindingEvent createBindingEvent(String binding, String entity, String event, BindingEventDTO dto) {
        String topic = BINDING_EVENT_TOPIC;
        topic = topic.replace("{binding}", binding);
        topic = topic.replace("{entity}", entity);
        topic = topic.replace("{event}", event);

        String payload = serializePayload(dto);
        return new BindingEvent(topic, binding, payload);
    }
}