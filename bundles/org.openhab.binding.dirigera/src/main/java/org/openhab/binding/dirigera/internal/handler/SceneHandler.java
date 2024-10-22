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
package org.openhab.binding.dirigera.internal.handler;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

/**
 * The {@link SceneHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SceneHandler extends BaseHandler {
    private final TimeZoneProvider timeZoneProvider;

    private Optional<ScheduledFuture<?>> sceneObserver = Optional.empty();
    private Instant lastTrigger = Instant.MAX;
    private int undoDuration = 30;

    public SceneHandler(Thing thing, Map<String, String> mapping, TimeZoneProvider timeZoneProvider) {
        super(thing, mapping);
        super.setChildHandler(this);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.checkHandler()) {
            JSONObject values = gateway().api().readScene(config.id);
            handleUpdate(values);

            if (values.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED, "Scene not found");
            } else {
                updateStatus(ThingStatus.ONLINE);
                sceneObserver = Optional.of(scheduler.scheduleWithFixedDelay(this::checkScene, 5, 5, TimeUnit.MINUTES));
            }

            // check if different undo duration is configured
            if (values.has("undoAllowedDuration")) {
                undoDuration = values.getInt("undoAllowedDuration");
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        sceneObserver.ifPresent(job -> {
            job.cancel(false);
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else if (CHANNEL_TRIGGER.equals(channelUID.getIdWithoutGroup())) {
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
        }
    }

    private void checkScene() {
        JSONObject values = gateway().model().getAllFor(config.id, PROPERTY_SCENES);
        if (values.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED, "Scene not found");
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
