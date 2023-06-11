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
package org.openhab.binding.sleepiq.internal.handler;

import static org.openhab.binding.sleepiq.internal.SleepIQBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.openhab.binding.sleepiq.api.SleepIQ;
import org.openhab.binding.sleepiq.api.model.Bed;
import org.openhab.binding.sleepiq.api.model.BedSideStatus;
import org.openhab.binding.sleepiq.api.model.BedStatus;
import org.openhab.binding.sleepiq.internal.config.SleepIQBedConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SleepIQDualBedHandler} is responsible for handling channel state updates from the cloud service.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class SleepIQDualBedHandler extends BaseThingHandler implements BedStatusListener {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections.singleton(THING_TYPE_DUAL_BED);

    private final Logger logger = LoggerFactory.getLogger(SleepIQDualBedHandler.class);

    private volatile String bedId;

    public SleepIQDualBedHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Verifying SleepIQ cloud/bridge configuration");

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No cloud service bridge has been configured");
            return;
        }

        ThingHandler handler = bridge.getHandler();
        if (!(handler instanceof SleepIQCloudHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Incorrect bridge thing found");
            return;
        }

        logger.debug("Reading SleepIQ bed binding configuration");
        bedId = getConfigAs(SleepIQBedConfiguration.class).bedId;

        logger.debug("Registering SleepIQ bed status listener");
        SleepIQCloudHandler cloudHandler = (SleepIQCloudHandler) handler;
        cloudHandler.registerBedStatusListener(this);

        if (ThingStatus.ONLINE != bridge.getStatus()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else {
            updateProperties();
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateProperties();
        }
    }

    private void updateProperties() {
        logger.debug("Updating SleepIQ bed properties for bed {}", bedId);

        SleepIQCloudHandler cloudHandler = (SleepIQCloudHandler) getBridge().getHandler();
        Bed bed = cloudHandler.getBed(bedId);
        if (bed == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bed found with ID " + bedId);
            return;
        }

        updateProperties(cloudHandler.updateProperties(bed, editProperties()));
    }

    @Override
    public void dispose() {
        logger.debug("Disposing bed handler for bed {}", bedId);

        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof SleepIQCloudHandler) {
                ((SleepIQCloudHandler) bridgeHandler).unregisterBedStatusListener(this);
            }
        }

        bedId = null;
    }

    @Override
    public void onBedStateChanged(final SleepIQ cloud, final BedStatus status) {
        if (!status.getBedId().equals(bedId)) {
            return;
        }

        logger.debug("Updating left side status for bed {}", bedId);
        BedSideStatus left = status.getLeftSide();
        updateState(CHANNEL_LEFT_IN_BED, left.isInBed() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_LEFT_SLEEP_NUMBER, new DecimalType(left.getSleepNumber()));
        updateState(CHANNEL_LEFT_PRESSURE, new DecimalType(left.getPressure()));
        updateState(CHANNEL_LEFT_LAST_LINK, new StringType(left.getLastLink().toString()));
        updateState(CHANNEL_LEFT_ALERT_ID, new DecimalType(left.getAlertId()));
        updateState(CHANNEL_LEFT_ALERT_DETAILED_MESSAGE, new StringType(left.getAlertDetailedMessage()));

        logger.debug("Updating right side status for bed {}", bedId);
        BedSideStatus right = status.getRightSide();
        updateState(CHANNEL_RIGHT_IN_BED, right.isInBed() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_RIGHT_SLEEP_NUMBER, new DecimalType(right.getSleepNumber()));
        updateState(CHANNEL_RIGHT_PRESSURE, new DecimalType(right.getPressure()));
        updateState(CHANNEL_RIGHT_LAST_LINK, new StringType(right.getLastLink().toString()));
        updateState(CHANNEL_RIGHT_ALERT_ID, new DecimalType(right.getAlertId()));
        updateState(CHANNEL_RIGHT_ALERT_DETAILED_MESSAGE, new StringType(right.getAlertDetailedMessage()));
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // all channels are read-only

        if (command == RefreshType.REFRESH) {
            SleepIQCloudHandler cloudHandler = (SleepIQCloudHandler) getBridge().getHandler();
            cloudHandler.refreshBedStatus();
        }
    }
}
