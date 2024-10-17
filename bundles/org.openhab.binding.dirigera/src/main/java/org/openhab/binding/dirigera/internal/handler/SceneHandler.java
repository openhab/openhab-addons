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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SceneHandler extends BaseDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(SceneHandler.class);

    private Optional<ScheduledFuture<?>> sceneObserver = Optional.empty();
    private final TimeZoneProvider timeZoneProvider;

    public SceneHandler(Thing thing, Map<String, String> mapping, TimeZoneProvider timeZoneProvider) {
        super(thing, mapping);
        super.setChildHandler(this);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        // handle general initialize like setting bridge
        super.initialize();
        JSONObject values = gateway().model().getAllFor(config.id, PROPERTY_SCENES);
        handleUpdate(values);

        if (values.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED, "Scene not found");
        } else {
            updateStatus(ThingStatus.ONLINE);
            sceneObserver = Optional.of(scheduler.scheduleWithFixedDelay(this::checkScene, 5, 5, TimeUnit.MINUTES));
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
            JSONObject values = gateway().model().getAllFor(config.id, PROPERTY_SCENES);
            handleUpdate(values);
        } else if (CHANNEL_TRIGGER.equals(channelUID.getIdWithoutGroup())) {
            if (command instanceof DecimalType decimal) {
                int commandNumber = decimal.intValue();
                switch (commandNumber) {
                    case 0:
                        gateway().api().triggerScene(config.id, "trigger");
                        break;
                    case 1:
                        gateway().api().triggerScene(config.id, "undo");
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

    private void checkScene() {
        JSONObject values = gateway().model().getAllFor(config.id, PROPERTY_SCENES);
        if (values.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED, "Scene not found");
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
