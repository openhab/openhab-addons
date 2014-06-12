package org.openhab.core.events.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.events.EventConstants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;

/**
 * This class acts as a bridge between events from openHAB 1.x (using "openhab" as a topic prefix) and
 * Eclipse SmartHome (using "smarthome" as a topic prefix).
 * It simply duplicates events with an updated topic prefix and works both ways.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class EventBridge implements EventHandler {

    private static final String BRIDGEMARKER = "bridgemarker";
	private EventAdmin eventAdmin;

    public void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    public void unsetEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = null;
    }

	@Override
	public void handleEvent(Event event) {
		
		if(!Boolean.TRUE.equals(event.getProperty(BRIDGEMARKER))) {
			
			// map event from ESH to openHAB
			if(event.getTopic().startsWith(EventConstants.TOPIC_PREFIX)) {
				String topic = org.openhab.core.events.EventConstants.TOPIC_PREFIX +
						event.getTopic().substring(EventConstants.TOPIC_PREFIX.length());
				Map<String, Object> properties = constructProperties(event);
				eventAdmin.postEvent(new Event(topic, properties));
			}
		
			// map event from openHAB to ESH
			if(event.getTopic().startsWith(org.openhab.core.events.EventConstants.TOPIC_PREFIX)) {
				String topic = EventConstants.TOPIC_PREFIX + 
						event.getTopic().substring(org.openhab.core.events.EventConstants.TOPIC_PREFIX.length());
				Map<String, Object> properties = constructProperties(event);
				eventAdmin.postEvent(new Event(topic, properties));
			}
		}
	}

	private Map<String, Object> constructProperties(Event event) {
		String[] propertyNames = event.getPropertyNames();
		Map<String, Object> properties = new HashMap<>();
		for(String propertyName : propertyNames) {
			properties.put(propertyName, event.getProperty(propertyName));
		}
		properties.put(BRIDGEMARKER, true);
		return properties;
	}

}
