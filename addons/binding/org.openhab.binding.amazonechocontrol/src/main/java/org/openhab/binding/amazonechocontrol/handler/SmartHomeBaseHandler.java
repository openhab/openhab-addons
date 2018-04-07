/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.handler;

import static org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_ENTITY_ID;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartHomeBaseHandler} is the base class for all smart home devices provided by alexa skills
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public abstract class SmartHomeBaseHandler extends BaseThingHandler {

    private final static HashMap<ThingUID, SmartHomeBaseHandler> instances = new HashMap<ThingUID, SmartHomeBaseHandler>();

    private final Logger logger = LoggerFactory.getLogger(SmartHomeBaseHandler.class);
    private @Nullable Connection connection;

    protected @Nullable Connection findConnection() {
        return this.connection;
    }

    protected SmartHomeBaseHandler(Thing thing) {
        super(thing);

    }

    @Override
    public void initialize() {
        logger.info("{} initialized", getClass().getSimpleName());
        synchronized (instances) {
            instances.put(this.getThing().getUID(), this);
        }
        if (this.connection != null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            Bridge bridge = this.getBridge();
            if (bridge != null) {
                AccountHandler account = (AccountHandler) bridge.getHandler();
                if (account != null) {
                    account.addSmartHomeHandler(this);
                }
            }
        }
    }

    public void initialize(Connection connection) {
        this.connection = connection;
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        synchronized (instances) {
            instances.remove(this.getThing().getUID());
        }
        super.dispose();
    }

    private String findEntityId() {
        String id = (String) getConfig().get(DEVICE_PROPERTY_ENTITY_ID);
        if (id == null) {
            return "";
        }
        return id;
    }

    public static @Nullable SmartHomeBaseHandler find(ThingUID uid) {
        synchronized (instances) {
            return instances.get(uid);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Connection connection = findConnection();
        if (connection == null) {
            return;
        }
        String entityId = findEntityId();
        if (entityId.isEmpty()) {
            return;
        }
        String channelId = channelUID.getId();
        if (StringUtils.isEmpty(channelId)) {
            return;
        }
        try {
            handleCommand(connection, entityId, channelId, command);
        } catch (IOException | URISyntaxException e) {
            logger.warn("handle command {} for {} failed", command, channelUID, e);
        }
    }

    protected abstract void handleCommand(Connection connection, String entityId, String channelId, Command command)
            throws IOException, URISyntaxException;
}
