/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.internal.azureiothub;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class starts the cloud connection service and implements interface to communicate with the cloud.
 *
 * @author Niko Tanghe - Initial contribution
 *
 */
@Component(immediate = true, configurationPid = "org.openhab.azureiothub", property = {
        "service.pid=org.openhab.azureiothub", "service.config.description.uri=io:azureiothub",
        "service.config.label=Azure IoT Hub", "service.config.category=io" })
public class CloudService implements EventSubscriber {

    private static final String CFG_MODE = "mode";
    private static final String CFG_CONNECTIONSTRING = "connectionstring";

    private Logger logger = LoggerFactory.getLogger(CloudService.class);

    public static String clientVersion;
    private CloudClient cloudClient;

    protected ItemRegistry itemRegistry;
    protected EventPublisher eventPublisher;

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Collections.singleton(ItemStateEvent.TYPE);
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    @Reference
    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Reference
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Override
    public void receive(Event event) {
        ItemStateEvent ise = (ItemStateEvent) event;
        if (cloudClient != null) {
            cloudClient.sendItemUpdate(ise.getItemName(), ise.getItemState().toString());
        }
    }

    @Activate
    protected void activate(BundleContext context, Map<String, ?> config) {
        String connectionstring = "";
        if (config.get(CFG_CONNECTIONSTRING) != null) {
            connectionstring = (String) config.get(CFG_CONNECTIONSTRING);
        } else {
            logger.error("Azure IoT client not started, could not get connectionstring config setting");
        }

        boolean commandEnabled = false;
        if (config.get(CFG_MODE) != null) {
            commandEnabled = "remote".equals(config.get(CFG_MODE));
        } else {
            logger.debug("remoteAccessEnabled is not set, keeping value '{}'", commandEnabled);
        }

        if (connectionstring != null && !connectionstring.isEmpty()) {
            try {
                cloudClient = new CloudClient(connectionstring, commandEnabled, eventPublisher);
                logger.debug("Azure IoT Hub connector activated");
            } catch (Exception e) {
                logger.error("Failed to setup Azure IoT Hub client");
            }
        } else {
            logger.error("Azure IoT client not started, connectionstring config setting is empty");
        }
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Azure IoT Hub connector deactivated");
        if (cloudClient != null) {
            cloudClient.shutdown();
        }
    }
}
