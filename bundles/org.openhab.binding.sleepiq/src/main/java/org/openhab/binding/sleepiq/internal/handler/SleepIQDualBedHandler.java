/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sleepiq.internal.api.dto.Bed;
import org.openhab.binding.sleepiq.internal.api.dto.BedSideStatus;
import org.openhab.binding.sleepiq.internal.api.dto.BedStatus;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationFeaturesResponse;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationStatusResponse;
import org.openhab.binding.sleepiq.internal.api.dto.SleepDataResponse;
import org.openhab.binding.sleepiq.internal.api.dto.Sleeper;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuator;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuatorSpeed;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationOutlet;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationOutletOperation;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationPreset;
import org.openhab.binding.sleepiq.internal.api.enums.Side;
import org.openhab.binding.sleepiq.internal.config.SleepIQBedConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SleepIQDualBedHandler} is responsible for handling channel state updates from the cloud service.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class SleepIQDualBedHandler extends BaseThingHandler implements BedStatusListener {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections.singleton(THING_TYPE_DUAL_BED);

    private static final long GET_SLEEP_DATA_DELAY_MINUTES = 5;

    private final Logger logger = LoggerFactory.getLogger(SleepIQDualBedHandler.class);

    private volatile String bedId = "";

    private @Nullable Sleeper sleeperLeft;
    private @Nullable Sleeper sleeperRight;

    private @Nullable BedStatus previousStatus;

    private @Nullable FoundationFeaturesResponse foundationFeatures;

    public SleepIQDualBedHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
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
        String localBedId = getConfigAs(SleepIQBedConfiguration.class).bedId;
        if (localBedId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bed id not found in configuration");
            return;
        }
        bedId = localBedId;
        // Assume the bed has a foundation until we determine otherwise
        setFoundationFeatures(new FoundationFeaturesResponse());

        logger.debug("BedHandler: Registering SleepIQ bed status listener for bedId={}", bedId);
        SleepIQCloudHandler cloudHandler = (SleepIQCloudHandler) handler;
        cloudHandler.registerBedStatusListener(this);

        if (ThingStatus.ONLINE != bridge.getStatus()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else {
            updateStatus(ThingStatus.ONLINE);
            scheduler.execute(() -> {
                updateProperties();
            });
        }
    }

    @Override
    public void dispose() {
        SleepIQCloudHandler cloudHandler = getCloudHandler();
        if (cloudHandler != null) {
            cloudHandler.unregisterBedStatusListener(this);
        }
        bedId = "";
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateProperties();
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command == RefreshType.REFRESH) {
            // Channels will be refreshed automatically by cloud handler
            return;
        }
        String channelId = channelUID.getId();
        String groupId = channelUID.getGroupId();

        switch (channelId) {
            case CHANNEL_LEFT_SLEEP_NUMBER:
            case CHANNEL_RIGHT_SLEEP_NUMBER:
                if (command instanceof DecimalType) {
                    Side side = Side.convertFromGroup(groupId);
                    logger.debug("BedHandler: Set sleepnumber to {} for bedId={}, side={}", command, bedId, side);
                    SleepIQCloudHandler cloudHandler = getCloudHandler();
                    if (cloudHandler != null) {
                        cloudHandler.setSleepNumber(bedId, side, ((DecimalType) command).intValue());
                    }
                }
                break;
            case CHANNEL_LEFT_PRIVACY_MODE:
            case CHANNEL_RIGHT_PRIVACY_MODE:
                if (command instanceof OnOffType) {
                    Side side = Side.convertFromGroup(groupId);
                    logger.debug("BedHandler: Set sleepnumber to {} for bedId={}, side={}", command, bedId, side);
                    SleepIQCloudHandler cloudHandler = getCloudHandler();
                    if (cloudHandler != null) {
                        cloudHandler.setPauseMode(bedId, command == OnOffType.ON ? true : false);
                    }
                }
                break;
            case CHANNEL_LEFT_FOUNDATION_PRESET:
            case CHANNEL_RIGHT_FOUNDATION_PRESET:
                logger.debug("Received command {} on channel {} to set preset", command, channelUID);
                if (isFoundationInstalled() && command instanceof DecimalType) {
                    try {
                        Side side = Side.convertFromGroup(groupId);
                        FoundationPreset preset = FoundationPreset.forValue(((DecimalType) command).intValue());
                        logger.debug("BedHandler: Set foundation preset to {} for bedId={}, side={}", command, bedId,
                                side);
                        SleepIQCloudHandler cloudHandler = getCloudHandler();
                        if (cloudHandler != null) {
                            cloudHandler.setFoundationPreset(bedId, side, preset, FoundationActuatorSpeed.SLOW);
                        }
                    } catch (IllegalArgumentException e) {
                        logger.info("BedHandler: Foundation preset invalid: {} must be 1-6", command);
                    }
                }
                break;
            case CHANNEL_LEFT_NIGHT_STAND_OUTLET:
            case CHANNEL_RIGHT_NIGHT_STAND_OUTLET:
            case CHANNEL_LEFT_UNDER_BED_LIGHT:
            case CHANNEL_RIGHT_UNDER_BED_LIGHT:
                logger.debug("Received command {} on channel {} to control outlet", command, channelUID);
                if (isFoundationInstalled() && command instanceof OnOffType) {
                    try {
                        logger.debug("BedHandler: Set foundation outlet channel {} to {} for bedId={}", channelId,
                                command, bedId);
                        FoundationOutlet outlet = FoundationOutlet.convertFromChannelId(channelId);
                        SleepIQCloudHandler cloudHandler = getCloudHandler();
                        if (cloudHandler != null) {
                            FoundationOutletOperation operation = command == OnOffType.ON ? FoundationOutletOperation.ON
                                    : FoundationOutletOperation.OFF;
                            cloudHandler.setFoundationOutlet(bedId, outlet, operation);
                        }
                    } catch (IllegalArgumentException e) {
                        logger.info("BedHandler: Can't convert channel {} to foundation outlet", channelId);
                    }
                }
                break;
            case CHANNEL_LEFT_POSITION_HEAD:
            case CHANNEL_RIGHT_POSITION_HEAD:
                logger.debug("Received command {} on channel {} to set position", command, channelUID);
                if (groupId != null && isFoundationInstalled() && command instanceof DecimalType) {
                    setFoundationPosition(groupId, channelId, command);
                }

            case CHANNEL_LEFT_POSITION_FOOT:
            case CHANNEL_RIGHT_POSITION_FOOT:
                logger.debug("Received command {} on channel {} to set position", command, channelUID);
                if (groupId != null && isFoundationInstalled() && isFoundationFootAdjustable()
                        && command instanceof DecimalType) {
                    setFoundationPosition(groupId, channelId, command);
                }
                break;
        }
    }

    @Override
    public void onSleeperChanged(final @Nullable Sleeper sleeper) {
        if (sleeper == null || !sleeper.getBedId().equals(bedId)) {
            return;
        }
        logger.debug("BedHandler: Updating sleeper information channels for bed={}, side={}", bedId, sleeper.getSide());

        if (sleeper.getSide().equals(Side.LEFT)) {
            sleeperLeft = sleeper;
            updateState(CHANNEL_LEFT_FIRST_NAME, new StringType(sleeper.getFirstName()));
            updateState(CHANNEL_LEFT_SLEEP_GOAL_MINUTES, new QuantityType<>(sleeper.getSleepGoal(), Units.MINUTE));
        } else {
            sleeperRight = sleeper;
            updateState(CHANNEL_RIGHT_FIRST_NAME, new StringType(sleeper.getFirstName()));
            updateState(CHANNEL_RIGHT_SLEEP_GOAL_MINUTES, new QuantityType<>(sleeper.getSleepGoal(), Units.MINUTE));
        }
    }

    @Override
    public void onBedStateChanged(final @Nullable BedStatus status) {
        if (status == null || !status.getBedId().equals(bedId)) {
            return;
        }
        logger.debug("BedHandler: Updating bed status channels for bed {}", bedId);
        BedStatus localPreviousStatus = previousStatus;

        BedSideStatus left = status.getLeftSide();
        updateState(CHANNEL_LEFT_IN_BED, left.isInBed() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_LEFT_SLEEP_NUMBER, new DecimalType(left.getSleepNumber()));
        updateState(CHANNEL_LEFT_PRESSURE, new DecimalType(left.getPressure()));
        updateState(CHANNEL_LEFT_LAST_LINK, new StringType(left.getLastLink().toString()));
        updateState(CHANNEL_LEFT_ALERT_ID, new DecimalType(left.getAlertId()));
        updateState(CHANNEL_LEFT_ALERT_DETAILED_MESSAGE, new StringType(left.getAlertDetailedMessage()));
        if (localPreviousStatus != null) {
            updateSleepDataChannels(localPreviousStatus.getLeftSide(), left, sleeperLeft);
        }

        BedSideStatus right = status.getRightSide();
        updateState(CHANNEL_RIGHT_IN_BED, right.isInBed() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_RIGHT_SLEEP_NUMBER, new DecimalType(right.getSleepNumber()));
        updateState(CHANNEL_RIGHT_PRESSURE, new DecimalType(right.getPressure()));
        updateState(CHANNEL_RIGHT_LAST_LINK, new StringType(right.getLastLink().toString()));
        updateState(CHANNEL_RIGHT_ALERT_ID, new DecimalType(right.getAlertId()));
        updateState(CHANNEL_RIGHT_ALERT_DETAILED_MESSAGE, new StringType(right.getAlertDetailedMessage()));
        if (localPreviousStatus != null) {
            updateSleepDataChannels(localPreviousStatus.getRightSide(), right, sleeperRight);
        }

        previousStatus = status;
    }

    @Override
    public void onFoundationStateChanged(String bedId, final @Nullable FoundationStatusResponse status) {
        if (status == null || !bedId.equals(this.bedId)) {
            return;
        }
        logger.debug("BedHandler: Updating foundation status channels for bed {}", bedId);
        updateState(CHANNEL_LEFT_POSITION_HEAD, new DecimalType(status.getLeftHeadPosition()));
        updateState(CHANNEL_LEFT_POSITION_FOOT, new DecimalType(status.getLeftFootPosition()));
        updateState(CHANNEL_RIGHT_POSITION_HEAD, new DecimalType(status.getRightHeadPosition()));
        updateState(CHANNEL_RIGHT_POSITION_FOOT, new DecimalType(status.getRightFootPosition()));
        updateState(CHANNEL_LEFT_FOUNDATION_PRESET, new DecimalType(status.getCurrentPositionPresetLeft().value()));
        updateState(CHANNEL_RIGHT_FOUNDATION_PRESET, new DecimalType(status.getCurrentPositionPresetRight().value()));
    }

    @Override
    public boolean isFoundationInstalled() {
        return foundationFeatures != null;
    }

    private void setFoundationFeatures(@Nullable FoundationFeaturesResponse features) {
        foundationFeatures = features;
    }

    private boolean isFoundationFootAdjustable() {
        FoundationFeaturesResponse localFoundationFeatures = foundationFeatures;
        return localFoundationFeatures != null ? localFoundationFeatures.hasFootControl() : false;
    }

    private void setFoundationPosition(String groupId, String channelId, Command command) {
        try {
            logger.debug("BedHandler: Set foundation position channel {} to {} for bedId={}", channelId, command,
                    bedId);
            Side side = Side.convertFromGroup(groupId);
            FoundationActuator actuator = FoundationActuator.convertFromChannelId(channelId);
            int position = ((DecimalType) command).intValue();
            SleepIQCloudHandler cloudHandler = getCloudHandler();
            if (cloudHandler != null) {
                cloudHandler.setFoundationPosition(bedId, side, actuator, position, FoundationActuatorSpeed.SLOW);
            }
        } catch (IllegalArgumentException e) {
            logger.info("BedHandler: Can't convert channel {} to foundation position", channelId);
        }
    }

    private void updateSleepDataChannels(BedSideStatus previousSideStatus, BedSideStatus currentSideStatus,
            @Nullable Sleeper sleeper) {
        if (sleeper == null) {
            logger.debug("BedHandler: Can't update sleep data channels because sleeper is null");
            return;
        }
        if (previousSideStatus.isInBed() && !currentSideStatus.isInBed()) {
            logger.debug("BedHandler: Bed status changed from IN BED to OUT OF BED for {}, side {}", bedId,
                    sleeper.getSide());
            scheduler.schedule(() -> {
                updateDailySleepDataChannels(sleeper);
                updateMonthlySleepDataChannels(sleeper);
            }, GET_SLEEP_DATA_DELAY_MINUTES, TimeUnit.MINUTES);
        }
    }

    public void updateDailySleepDataChannels(final @Nullable Sleeper sleeper) {
        SleepIQCloudHandler cloudHandler = getCloudHandler();
        if (cloudHandler == null || sleeper == null) {
            return;
        }
        SleepDataResponse sleepData = cloudHandler.getDailySleepData(sleeper.getSleeperId());
        if (sleepData == null) {
            logger.debug("BedHandler: Received no daily sleep data for bedId={}, sleeperId={}", sleeper.getBedId(),
                    sleeper.getSleeperId());
            return;
        }

        logger.debug("BedHandler: UPDATING DAILY SLEEP DATA CHANNELS for bedId={}, sleeperId={}", sleeper.getBedId(),
                sleeper.getSleeperId());
        if (sleepData.getSleepDataDays() == null || sleepData.getSleepDataDays().size() != 1) {
            if (sleeper.getSide().equals(Side.LEFT)) {
                updateState(CHANNEL_LEFT_TODAY_SLEEP_IQ, UnDefType.UNDEF);
                updateState(CHANNEL_LEFT_TODAY_AVG_HEART_RATE, UnDefType.UNDEF);
                updateState(CHANNEL_LEFT_TODAY_AVG_RESPIRATION_RATE, UnDefType.UNDEF);
                updateState(CHANNEL_LEFT_TODAY_MESSAGE, UnDefType.UNDEF);
                updateState(CHANNEL_LEFT_TODAY_SLEEP_DURATION_SECONDS, UnDefType.UNDEF);
                updateState(CHANNEL_LEFT_TODAY_SLEEP_IN_BED_SECONDS, UnDefType.UNDEF);
                updateState(CHANNEL_LEFT_TODAY_SLEEP_OUT_OF_BED_SECONDS, UnDefType.UNDEF);
                updateState(CHANNEL_LEFT_TODAY_SLEEP_RESTFUL_SECONDS, UnDefType.UNDEF);
                updateState(CHANNEL_LEFT_TODAY_SLEEP_RESTLESS_SECONDS, UnDefType.UNDEF);
            } else {
                updateState(CHANNEL_RIGHT_TODAY_SLEEP_IQ, UnDefType.UNDEF);
                updateState(CHANNEL_RIGHT_TODAY_AVG_HEART_RATE, UnDefType.UNDEF);
                updateState(CHANNEL_RIGHT_TODAY_AVG_RESPIRATION_RATE, UnDefType.UNDEF);
                updateState(CHANNEL_RIGHT_TODAY_MESSAGE, UnDefType.UNDEF);
                updateState(CHANNEL_RIGHT_TODAY_SLEEP_DURATION_SECONDS, UnDefType.UNDEF);
                updateState(CHANNEL_RIGHT_TODAY_SLEEP_IN_BED_SECONDS, UnDefType.UNDEF);
                updateState(CHANNEL_RIGHT_TODAY_SLEEP_OUT_OF_BED_SECONDS, UnDefType.UNDEF);
                updateState(CHANNEL_RIGHT_TODAY_SLEEP_RESTFUL_SECONDS, UnDefType.UNDEF);
                updateState(CHANNEL_RIGHT_TODAY_SLEEP_RESTLESS_SECONDS, UnDefType.UNDEF);
            }
            return;
        } else if (sleeper.getSide().equals(Side.LEFT)) {
            updateState(CHANNEL_LEFT_TODAY_SLEEP_IQ, new DecimalType(sleepData.getAverageSleepIQ()));
            updateState(CHANNEL_LEFT_TODAY_AVG_HEART_RATE, new DecimalType(sleepData.getAverageHeartRate()));
            updateState(CHANNEL_LEFT_TODAY_AVG_RESPIRATION_RATE,
                    new DecimalType(sleepData.getAverageRespirationRate()));
            updateState(CHANNEL_LEFT_TODAY_MESSAGE, new StringType(sleepData.getSleepDataDays().get(0).getMessage()));
            updateState(CHANNEL_LEFT_TODAY_SLEEP_DURATION_SECONDS,
                    new QuantityType<>(sleepData.getTotalSleepSessionTime(), Units.SECOND));
            updateState(CHANNEL_LEFT_TODAY_SLEEP_IN_BED_SECONDS,
                    new QuantityType<>(sleepData.getTotalInBedSeconds(), Units.SECOND));
            updateState(CHANNEL_LEFT_TODAY_SLEEP_OUT_OF_BED_SECONDS,
                    new QuantityType<>(sleepData.getTotalOutOfBedSeconds(), Units.SECOND));
            updateState(CHANNEL_LEFT_TODAY_SLEEP_RESTFUL_SECONDS,
                    new QuantityType<>(sleepData.getTotalRestfulSeconds(), Units.SECOND));
            updateState(CHANNEL_LEFT_TODAY_SLEEP_RESTLESS_SECONDS,
                    new QuantityType<>(sleepData.getTotalRestlessSeconds(), Units.SECOND));
        } else if (sleeper.getSide().equals(Side.RIGHT)) {
            updateState(CHANNEL_RIGHT_TODAY_SLEEP_IQ, new DecimalType(sleepData.getAverageSleepIQ()));
            updateState(CHANNEL_RIGHT_TODAY_AVG_HEART_RATE, new DecimalType(sleepData.getAverageHeartRate()));
            updateState(CHANNEL_RIGHT_TODAY_AVG_RESPIRATION_RATE,
                    new DecimalType(sleepData.getAverageRespirationRate()));
            updateState(CHANNEL_RIGHT_TODAY_MESSAGE, new StringType(sleepData.getSleepDataDays().get(0).getMessage()));
            updateState(CHANNEL_RIGHT_TODAY_SLEEP_DURATION_SECONDS,
                    new QuantityType<>(sleepData.getTotalSleepSessionTime(), Units.SECOND));
            updateState(CHANNEL_RIGHT_TODAY_SLEEP_IN_BED_SECONDS,
                    new QuantityType<>(sleepData.getTotalInBedSeconds(), Units.SECOND));
            updateState(CHANNEL_RIGHT_TODAY_SLEEP_OUT_OF_BED_SECONDS,
                    new QuantityType<>(sleepData.getTotalOutOfBedSeconds(), Units.SECOND));
            updateState(CHANNEL_RIGHT_TODAY_SLEEP_RESTFUL_SECONDS,
                    new QuantityType<>(sleepData.getTotalRestfulSeconds(), Units.SECOND));
            updateState(CHANNEL_RIGHT_TODAY_SLEEP_RESTLESS_SECONDS,
                    new QuantityType<>(sleepData.getTotalRestlessSeconds(), Units.SECOND));
        }
    }

    public void updateMonthlySleepDataChannels(final @Nullable Sleeper sleeper) {
        SleepIQCloudHandler cloudHandler = getCloudHandler();
        if (cloudHandler == null || sleeper == null) {
            return;
        }
        SleepDataResponse sleepData = cloudHandler.getMonthlySleepData(sleeper.getSleeperId());
        if (sleepData == null) {
            logger.debug("BedHandler: Received no monthly sleep data for bedId={}, sleeperId={}", sleeper.getBedId(),
                    sleeper.getSleeperId());
            return;
        }

        logger.debug("BedHandler: UPDATING MONTHLY SLEEP DATA CHANNELS for bedId={}, sleeperId={}", sleeper.getBedId(),
                sleeper.getSleeperId());
        if (sleeper.getSide().equals(Side.LEFT)) {
            updateState(CHANNEL_LEFT_MONTHLY_SLEEP_IQ, new DecimalType(sleepData.getAverageSleepIQ()));
            updateState(CHANNEL_LEFT_MONTHLY_AVG_HEART_RATE, new DecimalType(sleepData.getAverageHeartRate()));
            updateState(CHANNEL_LEFT_MONTHLY_AVG_RESPIRATION_RATE,
                    new DecimalType(sleepData.getAverageRespirationRate()));
        } else {
            updateState(CHANNEL_RIGHT_MONTHLY_SLEEP_IQ, new DecimalType(sleepData.getAverageSleepIQ()));
            updateState(CHANNEL_RIGHT_MONTHLY_AVG_HEART_RATE, new DecimalType(sleepData.getAverageHeartRate()));
            updateState(CHANNEL_RIGHT_MONTHLY_AVG_RESPIRATION_RATE,
                    new DecimalType(sleepData.getAverageRespirationRate()));
        }
    }

    private void updateProperties() {
        logger.debug("BedHandler: Updating bed properties for bedId={}", bedId);
        SleepIQCloudHandler cloudHandler = getCloudHandler();
        if (cloudHandler != null) {
            Bed bed = cloudHandler.getBed(bedId);
            if (bed == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "No bed found with ID " + bedId);
                return;
            }
            updateProperties(cloudHandler.updateProperties(bed, editProperties()));

            logger.debug("BedHandler: Checking if foundation is installed for bedId={}", bedId);
            if (isFoundationInstalled()) {
                FoundationFeaturesResponse foundationFeaturesResponse = cloudHandler.getFoundationFeatures(bedId);
                updateProperties(cloudHandler.updateFeatures(bedId, foundationFeaturesResponse, editProperties()));
                setFoundationFeatures(foundationFeaturesResponse);
            }
        }
    }

    private @Nullable SleepIQCloudHandler getCloudHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (SleepIQCloudHandler) bridge.getHandler();
        }
        return null;
    }
}
