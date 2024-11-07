/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.handler.scene;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneHandler} for triggering defined scenes
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SceneHandler extends BaseHandler {
    private final Logger logger = LoggerFactory.getLogger(SceneHandler.class);

    private TimeZoneProvider timeZoneProvider;
    private Instant lastTrigger = Instant.MAX;
    private int undoDuration = 30;

    public SceneHandler(Thing thing, Map<String, String> mapping, TimeZoneProvider timeZoneProvider) {
        super(thing, mapping);
        super.setChildHandler(this);
        this.timeZoneProvider = timeZoneProvider;
        // no link support for Scenes
        hardLinks = Arrays.asList();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.checkHandler()) {
            JSONObject values = gateway().api().readScene(config.id);
            handleUpdate(values);

            if (values.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                        "@text/dirigera.scene.status.scene-not-found");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }

            // check if different undo duration is configured
            if (values.has("undoAllowedDuration")) {
                undoDuration = values.getInt("undoAllowedDuration");
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("DIRIGERA SCENE handleCommand {} {}", channelUID, command);
        super.handleCommand(channelUID, command);
        if (CHANNEL_TRIGGER.equals(channelUID.getIdWithoutGroup())) {
            if (command instanceof DecimalType decimal) {
                int commandNumber = decimal.intValue();
                switch (commandNumber) {
                    case 0:
                        gateway().api().triggerScene(config.id, "trigger");
                        lastTrigger = Instant.now();
                        scheduler.schedule(this::countDown, 1, TimeUnit.SECONDS);
                        break;
                    case 1:
                        gateway().api().triggerScene(config.id, "undo");
                        lastTrigger = Instant.MAX;
                        updateState(new ChannelUID(thing.getUID(), CHANNEL_TRIGGER), UnDefType.UNDEF);
                        break;
                }
            }
        }
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);
        if (update.has("lastTriggered")) {
            Instant sunsetInstant = Instant.parse(update.getString("lastTriggered"));
            updateState(new ChannelUID(thing.getUID(), CHANNEL_LAST_TRIGGER),
                    new DateTimeType(sunsetInstant.atZone(timeZoneProvider.getTimeZone())));
        }
    }

    private void countDown() {
        long seconds = Duration.between(lastTrigger, Instant.now()).toSeconds();
        if (seconds >= 0 && seconds <= 30) {
            long countDown = undoDuration - seconds;
            updateState(new ChannelUID(thing.getUID(), CHANNEL_TRIGGER), new DecimalType(countDown));
            scheduler.schedule(this::countDown, 1, TimeUnit.SECONDS);
        } else {
            updateState(new ChannelUID(thing.getUID(), CHANNEL_TRIGGER), UnDefType.UNDEF);
        }
    }
}
