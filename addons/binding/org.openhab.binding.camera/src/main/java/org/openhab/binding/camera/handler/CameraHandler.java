/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.camera.handler;

import static org.openhab.binding.camera.CameraBindingConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CameraHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Hartwig - Initial contribution
 */
public class CameraHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(CameraHandler.class);
    private ScheduledFuture<?> scheduledTask;

    public CameraHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_IMAGE)) {
            if (command.toString().equals("REFRESH")) {
                refreshData();
            }
        }
    }

    private String urlSnapshot;

    @Override
    public void initialize() {
        updateStatus(ThingStatus.INITIALIZING);
        logger.debug("Initialize thing: " + getThing().getLabel() + "::" + getThing().getUID());
        Object param = getConfig().get("urlSnapshot");
        urlSnapshot = String.valueOf(param);
        try {
            param = getConfig().get("poll");
            CONFIGURATION_POLLTIME_S = (int) Double.parseDouble(String.valueOf(param));
        } catch (Exception e1) {
            logger.warn("could not read poll time", e1);
        }
        scheduleUpdates();
        updateStatus(ThingStatus.OFFLINE);
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);

    private void refreshData() {
        if (refreshInProgress.compareAndSet(false, true)) {
            try {
                for (Channel cx : getThing().getChannels()) {
                    if (cx.getAcceptedItemType().equals("Image")) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Will update: " + cx.getChannelTypeUID().getId() + "::" + getThing().getLabel()
                                    + "::" + getThing().getUID().getId());
                        }
                        if (urlSnapshot != null) {
                            try {
                                final URL url = new URL(urlSnapshot);
                                updateState(cx.getUID(), new RawType(readImage(url).toByteArray()));
                                updateStatus(ThingStatus.ONLINE);
                            } catch (MalformedURLException e) {
                                logger.warn("could not update value: " + getThing(), e);
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "snapshot url not valid: " + e.toString());
                            } catch (IOException e) {
                                logger.warn("could not update value: " + getThing(), e);
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "camera not reachable: " + e.toString());
                            } catch (Exception e) {
                                logger.warn("could not update value: " + getThing(), e);
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                                        "unknown error: " + e.toString());
                            }
                        } else {
                            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_PENDING);
                        }
                    }
                }
            } finally {
                refreshInProgress.set(false);
            }
        }
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
}
