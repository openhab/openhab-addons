/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zipato.handler;

import static org.openhab.binding.zipato.ZipatoBindingConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZipatoThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Hartwig - Initial contribution
 */
public class ZipatoThingHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(ZipatoThingHandler.class);
    private ScheduledFuture<?> scheduledTask;
    private long lastModified = -1;

    public ZipatoThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_SENSOR_DEVICE)) {
        }
        if (channelUID.getId().equals(CHANNEL_SWITCH_DEVICE)) {
            logger.debug("thing status updated: " + getThing() + "::" + command.toString());
            if (isConnected()) {
                lastModified = System.currentTimeMillis();
                getController().getZipato().doSetDeviceValueBoolean(getThing().getUID().getId(), command.toString());
            }
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
    }

    private ZipatoControllerHandler getController() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof ZipatoControllerHandler) {
                ZipatoControllerHandler bridgeHandler = (ZipatoControllerHandler) handler;
                return bridgeHandler;
            }
        }
        return null;
    }

    @Override
    public void initialize() {
        logger.debug("Initialize thing: " + getThing().getLabel() + "::" + getThing().getUID());
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        scheduleUpdates();
        updateStatus(ThingStatus.OFFLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private void scheduleUpdates() {
        logger.debug("Schedule update at fixed rate {} s.", CONFIGURATION_POLLTIME_S);
        scheduledTask = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        }, 10, CONFIGURATION_POLLTIME_S, TimeUnit.SECONDS);
    }

    private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);

    private void refreshData() {
        if (isConnected() && System.currentTimeMillis() - lastModified > 5000) {
            if (refreshInProgress.compareAndSet(false, true)) {
                try {
                    for (Channel cx : getThing().getChannels()) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Will update: " + cx.getChannelTypeUID().getId() + "::" + getThing().getLabel()
                                    + "::" + getThing().getUID().getId());
                        }
                        try {
                            if (!cx.getChannelTypeUID().getId().equals(ZIPATO_THING_CHANNEL_CAMERA)) {
                                Object object = getController().getZipato()
                                        .doGetDeviceValue(getThing().getUID().getId()).get();
                                if (object != null) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("update value: " + getThing() + "::" + object);
                                    }
                                    updateState(cx.getUID(), getStateByChannel(cx.getChannelTypeUID().getId(), object));
                                    updateStatus(ThingStatus.ONLINE);
                                } else {
                                    logger.warn("update value is NULL: " + getThing());
                                    updateStatus(ThingStatus.OFFLINE);
                                }
                            } else {
                                final URL url = new URL(getThing().getProperties().get("url_snapshot"));
                                updateState(cx.getUID(), new RawType(readImage(url).toByteArray()));
                                updateStatus(ThingStatus.ONLINE);
                            }
                        } catch (Exception e) {
                            logger.warn("could not update value: " + getThing(), e);
                            updateStatus(ThingStatus.OFFLINE);
                        }
                    }
                } finally {
                    refreshInProgress.set(false);
                }
            }
        } else {
            logger.warn("controller not available or online: " + getThing().getLabel());
        }
    }

    private static ByteArrayOutputStream readImage(URL url) throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            is = url.openStream();
            byte[] bytebuff = new byte[4096];
            int n;
            while ((n = is.read(bytebuff)) > 0) {
                bis.write(bytebuff, 0, n);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception ignored) {
            }
        }
        return bis;
    }

    private State getStateByChannel(String channel, Object object) {
        switch (channel) {
            case ZIPATO_THING_CHANNEL_SWITCH:
                return String.valueOf(object).toLowerCase().matches("(true|on|1)") ? OnOffType.ON : OnOffType.OFF;
            default:
                break;
        }
        return new StringType(String.valueOf(object));
    }

    private boolean isConnected() {
        return getController() != null && getController().getZipato() != null
                && getController().getZipato().isConnected();
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        super.dispose();
        scheduledTask.cancel(false);
    }

}
