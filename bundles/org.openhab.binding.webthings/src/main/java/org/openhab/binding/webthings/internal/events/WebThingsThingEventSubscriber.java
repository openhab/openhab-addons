/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.webthings.internal.events;

import static org.openhab.binding.webthings.internal.WebThingsBindingConstants.*;

import java.util.*;

import org.eclipse.smarthome.core.events.*;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.core.thing.events.*;
import org.openhab.binding.webthings.internal.handler.WebThingsServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Component;


/**
 * The {@link WebThingsThingEventSubscriber} is responsible to track thing events and restart the WebThingServer when new things have been added
 *
 * @author schneider_sven - Initial contribution
 */
@Component
public class WebThingsThingEventSubscriber implements EventSubscriber {
    private final Logger logger = LoggerFactory.getLogger(WebThingsThingEventSubscriber.class);

    private final Set<String> subscribedEventTypes = new HashSet<>();
    {
        subscribedEventTypes.add(ThingAddedEvent.TYPE);
        subscribedEventTypes.add(ThingRemovedEvent.TYPE);
        subscribedEventTypes.add(ThingUpdatedEvent.TYPE);
    }

    private final EventFilter eventFilter = new TopicEventFilter("smarthome/things/.*");

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public EventFilter getEventFilter() {
        return eventFilter;
    }

    @Override
    public void receive(Event event) {
        String topic = event.getTopic();
        String type = event.getType();

        logger.debug("\n-------------------------------- Received event - Type: {} - Topic: {} --------------------------------", type, topic);

        for(WebThingsServerHandler serverHandler: SERVER_HANDLER_LIST.values()){

            // Only restart server which host all WebThings whenever openHAB things are added, deleted or updated
            if(serverHandler.getHandlerConfig().allThings){
                // Server restart upon changes necessary because otherwise base is broken 
                // serverHandler.thingUpdated(serverHandler.getThing());

                // This code could prevent a server restart when new things are added
                //WebThingsHandler thingHandler = serverHandler.getWebThingsHandler();
                if (event instanceof ThingAddedEvent) {
                    ThingAddedEvent thingAddedEvent = (ThingAddedEvent) event;
                    ThingDTO ohThing = thingAddedEvent.getThing();
        
                    if(!ohThing.thingTypeUID.equals("webthings:connector") && !ohThing.thingTypeUID.equals("webthings:server")){       
                        serverHandler.thingUpdated(serverHandler.getThing());
                    }
                } else if (event instanceof ThingRemovedEvent) {
                    ThingRemovedEvent thingRemovedEvent = (ThingRemovedEvent) event;
                    ThingDTO ohThing = thingRemovedEvent.getThing();
        
                    if(!ohThing.thingTypeUID.equals("webthings:connector") && !ohThing.thingTypeUID.equals("webthings:server")){ 
                        serverHandler.thingUpdated(serverHandler.getThing());
                    }
                } else if (event instanceof ThingUpdatedEvent){
                    ThingUpdatedEvent thingUpdatedEvent = (ThingUpdatedEvent) event;
                    ThingDTO ohThing = thingUpdatedEvent.getThing();
                    
                    if(!ohThing.thingTypeUID.equals("webthings:connector") && !ohThing.thingTypeUID.equals("webthings:server")){
                        serverHandler.thingUpdated(serverHandler.getThing());
                    }
                }
            }
        }
    }
}
