/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.vthing;

import static org.openhab.binding.vthing.VLampBindingConstants.*;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VLampHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Juergen Weber - Initial contribution
 */
public class VLampHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(VLampHandler.class);

    private ThingUID uid;

    private VLampConfiguration config;

    OnOffType state = OnOffType.OFF;
    HSBType color = HSBType.BLUE;

    private long startTime = -1;

    private ScheduledFuture<?> updateJob = null;

    public VLampHandler(Thing thing) {
        super(thing);
        uid = getThing().getUID();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        switch (channelUID.getId()) {
            case CHANNEL_STATE:
                if (command instanceof OnOffType) {
                    state = (OnOffType) command;
                    startTime = state == OnOffType.ON ? System.currentTimeMillis() : -1;
                    logger.info("State: OnOffType: " + state.toFullString());

                    update(uid, state);
                }
                if (command instanceof RefreshType) {
                    updateState(channelUID, state);
                    update(uid, state);
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    color = (HSBType) command;
                    logger.info("color HSBType: " + color.toFullString());
                    update(uid, color);
                }
                if (command instanceof OnOffType) {
                    state = (OnOffType) command;
                    startTime = state == OnOffType.ON ? System.currentTimeMillis() : -1;
                    logger.info("color OnOffType: " + state.toFullString());
                    // synchonize state
                    updateState(CHANNEL_STATE, state);
                    update(uid, state);
                }
                if (command instanceof RefreshType) {
                    updateState(CHANNEL_COLOR, color);
                    update(uid, state);
                    update(uid, color);
                }
                break;
            case CHANNEL_ONTIME:
                if (command instanceof RefreshType) {
                    postOntimeUpdate();
                }
                break;
        }
    }

    void update(ThingUID uid, HSBType hsb) {

        int rgb = hsb.getRGB() & 0XFFFFFF;

        String color = String.format("color=#%06X", rgb);

        VLampHandlerFactory.onUpdate(uid, color);
    }

    void update(ThingUID uid, OnOffType state) {

        String s = state == OnOffType.ON ? "ON" : "OFF";

        String message = String.format("state=%s", s);

        VLampHandlerFactory.onUpdate(uid, message);
    }

    void triggerUpdate(ThingUID uid) {
        scheduler.schedule(() -> {
            update(uid, color);
            update(uid, state);
        }, 0, TimeUnit.SECONDS);
    }

    private void postOntimeUpdate() {
        String s;

        if (startTime != -1) {
            long d = System.currentTimeMillis() - startTime;

            Duration d1 = Duration.ofMillis(d);

            s = String.format("%sd %sh %sm %ss", d1.toDaysPart(), d1.toHoursPart(), d1.toMinutesPart(),
                    d1.toSecondsPart());
        } else {
            s = "";
        }
        updateState(CHANNEL_ONTIME, new StringType(s));
    }

    private void postUpdates() {
        postOntimeUpdate();
    }

    @Override
    public void initialize() {
        config = getConfigAs(VLampConfiguration.class);

        logger.info("initialize: {}", uid.getAsString());

        updateJob = scheduler.scheduleWithFixedDelay(this::postUpdates, 1, 1, TimeUnit.SECONDS);

        updateState(CHANNEL_STATE, state);

        triggerUpdate(uid);

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        updateJob.cancel(true);
    }
}
