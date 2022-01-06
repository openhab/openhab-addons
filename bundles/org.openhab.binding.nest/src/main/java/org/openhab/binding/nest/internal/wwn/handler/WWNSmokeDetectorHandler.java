/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.wwn.handler;

import static org.openhab.binding.nest.internal.wwn.WWNBindingConstants.*;
import static org.openhab.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.core.types.RefreshType.REFRESH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.wwn.dto.WWNSmokeDetector;
import org.openhab.binding.nest.internal.wwn.dto.WWNSmokeDetector.BatteryHealth;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The smoke detector handler, it handles the data from WWN for the smoke detector.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Handle channel refresh command
 */
@NonNullByDefault
public class WWNSmokeDetectorHandler extends WWNBaseHandler<WWNSmokeDetector> {
    private final Logger logger = LoggerFactory.getLogger(WWNSmokeDetectorHandler.class);

    public WWNSmokeDetectorHandler(Thing thing) {
        super(thing, WWNSmokeDetector.class);
    }

    @Override
    protected State getChannelState(ChannelUID channelUID, WWNSmokeDetector smokeDetector) {
        switch (channelUID.getId()) {
            case CHANNEL_CO_ALARM_STATE:
                return getAsStringTypeOrNull(smokeDetector.getCoAlarmState());
            case CHANNEL_LAST_CONNECTION:
                return getAsDateTimeTypeOrNull(smokeDetector.getLastConnection());
            case CHANNEL_LAST_MANUAL_TEST_TIME:
                return getAsDateTimeTypeOrNull(smokeDetector.getLastManualTestTime());
            case CHANNEL_LOW_BATTERY:
                return getAsOnOffTypeOrNull(smokeDetector.getBatteryHealth() == null ? null
                        : smokeDetector.getBatteryHealth() == BatteryHealth.REPLACE);
            case CHANNEL_MANUAL_TEST_ACTIVE:
                return getAsOnOffTypeOrNull(smokeDetector.isManualTestActive());
            case CHANNEL_SMOKE_ALARM_STATE:
                return getAsStringTypeOrNull(smokeDetector.getSmokeAlarmState());
            case CHANNEL_UI_COLOR_STATE:
                return getAsStringTypeOrNull(smokeDetector.getUiColorState());
            default:
                logger.error("Unsupported channelId '{}'", channelUID.getId());
                return UnDefType.UNDEF;
        }
    }

    /**
     * Handles any incoming command requests.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH.equals(command)) {
            WWNSmokeDetector lastUpdate = getLastUpdate();
            if (lastUpdate != null) {
                updateState(channelUID, getChannelState(channelUID, lastUpdate));
            }
        }
    }

    @Override
    protected void update(@Nullable WWNSmokeDetector oldSmokeDetector, WWNSmokeDetector smokeDetector) {
        logger.debug("Updating {}", getThing().getUID());

        updateLinkedChannels(oldSmokeDetector, smokeDetector);
        updateProperty(PROPERTY_FIRMWARE_VERSION, smokeDetector.getSoftwareVersion());

        ThingStatus newStatus = smokeDetector.isOnline() == null ? ThingStatus.UNKNOWN
                : smokeDetector.isOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE;
        if (newStatus != thing.getStatus()) {
            updateStatus(newStatus);
        }
    }
}
