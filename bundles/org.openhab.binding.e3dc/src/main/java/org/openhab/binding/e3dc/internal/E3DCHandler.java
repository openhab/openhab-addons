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
package org.openhab.binding.e3dc.internal;

import static org.openhab.binding.e3dc.internal.E3DCBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Björn Brings - Initial contribution
 * @author Marco Loose - Minor changes
 */
@NonNullByDefault
public class E3DCHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(E3DCHandler.class);

    private final Object initDisposeLock = new Object();

    private @Nullable E3DCConfiguration config;
    private @Nullable ScheduledFuture<?> readDataJob;
    private @Nullable E3DCConnector e3dcconnect;
    private boolean isDisposed;

    private int count_PM = 1;
    private final int minCount_PM = 1;
    private final int maxCount_PM = 8;
    private int count_WB = 0;
    private final int minCount_WB = 0;
    private final int maxCount_WB = 8;
    private int count_FMS = 0;
    private final int minCount_FMS = 0;
    private final int maxCount_FMS = 2;
    private int count_FMS_REV = 0;
    private final int minCount_FMS_REV = 0;
    private final int maxCount_FMS_REV = 2;
    private int count_PVI = 1;
    private final int minCount_PVI = 0;
    private final int maxCount_PVI = 2;
    private int count_QPI = 1;
    private final int minCount_QPI = 0;
    private final int maxCount_QPI = 2;
    private int count_SE = 0;
    private final int minCount_SE = 0;
    private final int maxCount_SE = 2;

    public int getCount_PM() {
        return count_PM;
    }

    public int getForcedCount_PM() {
        return (config.getpowerMeterCount() > count_PM) ? config.getpowerMeterCount() : count_PM;
    }

    public int getForcedCount_PVI() {
        return (config.getTrackerCount() > count_PVI) ? config.getpowerMeterCount() : count_PVI;
    }

    public int getForcedCount_WB() {
        return (config.getWallboxCount() > count_WB) ? config.getWallboxCount() : count_WB;
    }

    public void setCount_PM(int count_PM) {
        this.count_PM = Math.min(Math.max(count_PM, minCount_PM), maxCount_PM);
        updateProperty(PROPERTY_PM_CONNECTED_DEVICES, Integer.toString(this.count_PM));

        E3DCRequests.setPmCount(getForcedCount_PM());
    }

    public int getCount_WB() {
        return count_WB;
    }

    public void setCount_WB(int count_WB) {
        this.count_WB = Math.min(Math.max(count_WB, minCount_WB), maxCount_WB);
        updateProperty(PROPERTY_WB_CONNECTED_DEVICES, Integer.toString(this.count_WB));

        E3DCRequests.setWbCount(getForcedCount_WB());
    }

    public int getCount_FMS() {
        return count_FMS;
    }

    public void setCount_FMS(int count_FMS) {
        this.count_FMS = Math.min(Math.max(count_FMS, minCount_FMS), maxCount_FMS);
        updateProperty(PROPERTY_FMS_CONNECTED_DEVICES, Integer.toString(this.count_FMS));
    }

    public int getCount_FMS_REV() {
        return count_FMS_REV;
    }

    public void setCount_FMS_REV(int count_FMS_REV) {
        this.count_FMS_REV = Math.min(Math.max(count_FMS_REV, minCount_FMS_REV), maxCount_FMS_REV);
        updateProperty(PROPERTY_FMS_REV_CONNECTED_DEVICES, Integer.toString(this.count_FMS_REV));
    }

    public int getCount_PVI() {
        return count_PVI;
    }

    public void setCount_PVI(int count_PVI) {
        this.count_PVI = Math.min(Math.max(count_PVI, minCount_PVI), maxCount_PVI);
        updateProperty(PROPERTY_PVI_USED_STRING_COUNT, Integer.toString(this.count_PVI));

        E3DCRequests.setPviCount(getForcedCount_PVI());
    }

    public int getCount_QPI() {
        return count_QPI;
    }

    public void setCount_QPI(int count_QPI) {
        this.count_QPI = Math.min(Math.max(count_QPI, minCount_QPI), maxCount_QPI);
        updateProperty(PROPERTY_QPI_INVERTER_COUNT, Integer.toString(this.count_QPI));
    }

    public int getCount_SE() {
        return count_SE;
    }

    public void setCount_SE(int count_SE) {
        this.count_SE = Math.min(Math.max(count_SE, minCount_SE), maxCount_SE);
        updateProperty(PROPERTY_SE_SE_COUNT, Integer.toString(this.count_SE));
    }

    public E3DCHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void updateProperty(String name, String value) {
        super.updateProperty(name, value);
    }

    private void getVersion() {
        // TODO remove method for release!
        String version = "";
        try {
            version = Versioning.INSTANCE.buildTime.toString();
        } catch (Exception e) {
            version = "---";
        }

        logger.info("Binding build: {}", version);
        this.updateProperty(PROPERTY_BUILD, version);
    }

    @Override
    @SuppressWarnings("null")
    public void handleCommand(ChannelUID channelUID, Command command) {
        boolean bValue;
        int iValue;

        this.logger.debug("handleCommand channel:{}  channelID:{}  command:{}", channelUID, channelUID.getId(),
                command);
        if (command instanceof RefreshType) {
            return;
        }
        if (e3dcconnect == null) {
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_GROUP_EMS + "#" + CHANNEL_StartManualCharge:
                // TODO
                logger.debug("Starting manual charge... is a TODO");
                break;

            case CHANNEL_GROUP_EMS + "#" + CHANNEL_StartEmergencyPowerTest:
                logger.debug("Not sure if we should allow Emergeny Power Tests... can we even?");
                break;
                
            case CHANNEL_GROUP_EMS + "#" + CHANNEL_WeatherRegulatedCharge:
                bValue = (command == OnOffType.ON);
                e3dcconnect.setWeatherRegulatedChargeEnable(bValue);
                break;

            case CHANNEL_GROUP_EMS + "#" + CHANNEL_PowerSave:
                bValue = (command == OnOffType.ON);
                e3dcconnect.setPowerSaveEnable(bValue);
                break;

            case CHANNEL_GROUP_EMS + "#" + CHANNEL_PowerLimitsUsed:
                bValue = (command == OnOffType.ON);
                e3dcconnect.setPowerLimitsUsed(bValue);
                break;

            case CHANNEL_GROUP_EMS + "#" + CHANNEL_MaxCharge:
                iValue = convertCommandToIntValue(command, 100, 3000);
                e3dcconnect.setMaxChargePower(iValue);
                break;

            case CHANNEL_GROUP_EMS + "#" + CHANNEL_MaxDischarge:
                iValue = convertCommandToIntValue(command, 100, 3000);
                e3dcconnect.setMaxDischargePower(iValue);
                break;

            case CHANNEL_GROUP_EMS + "#" + CHANNEL_DischargeStart:
                iValue = convertCommandToIntValue(command, 0, 500);
                e3dcconnect.setDischargeStartPower(iValue);
                break;

            case CHANNEL_GROUP_DEBUG + "#" + CHANNEL_DebugQuery:
                bValue = (command == OnOffType.ON);
                e3dcconnect.setDebugQuery(bValue);
                break;

            default:
                break;
        }
    }

    public int convertCommandToIntValue(Command command, int min, int max) {
        this.logger.debug("convertCommandToIntValue  command: {}", command);

        int value;
        double fValue;

        if (command instanceof DecimalType) {
            fValue = ((DecimalType) command).floatValue();
            logger.trace("convertCommandToIntValue DecimalType fValue: {}", Double.valueOf(fValue));
        } else if (command instanceof QuantityType) {
            fValue = ((QuantityType<?>) command).doubleValue();
            logger.trace("convertCommandToIntValue QuantityType fValue: {}", Double.valueOf(fValue));
        } else {
            throw new NumberFormatException("Command type '" + command + "' not supported");
        }

        value = (int) fValue;
        value = Math.min(max, value);
        value = Math.max(min, value);

        return value;
    }

    @Override
    public void initialize() {
        synchronized (initDisposeLock) {
            isDisposed = false;
        scheduler.execute(() -> {
            initSchedule();
        });
    }
    }

    @SuppressWarnings("null")
    private void initSchedule() {

        synchronized (initDisposeLock) {

            config = getConfigAs(E3DCConfiguration.class);
            getVersion();

            if (config.isConfigComplete()) {
                try {
            updateState(CHANNEL_GROUP_DEBUG + CHANNEL_DebugQuery, OnOffType.OFF);

                    e3dcconnect = new E3DCConnector(this, config);
            scheduleReadDataJob();

        } catch (Exception e) {
            String msg = String.format("Failed to connect to {}:{} - Please check config and restart thing.",
                    config.getIp(), config.getPort());

            logger.debug(msg, e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);

            if (e3dcconnect != null) {
                e3dcconnect.close();
                e3dcconnect = null;
            }
        }

            } else {
                StringJoiner joiner = new StringJoiner(" ");

                if (config.getIp().isBlank())
                    joiner.add("You must set an IP Address.");
                if (config.getWebusername().isBlank())
                    joiner.add("You must set the Webinterface username.");
                if (config.getWebpassword().isEmpty())
                    joiner.add("You must set the Webinterface password.");
                if (config.getRscppassword().length() < 6)
                    joiner.add("You must set the RSCP password (min. 6 characters).");

                var configErrorMessage = joiner.toString();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configErrorMessage);

            }
        }
    }

    private void scheduleReadDataJob() {
        synchronized (initDisposeLock) {
        int readDataInterval = config.getUpdateinterval();
        // Ensure that the request is finished
        if (readDataInterval < 5) {
            readDataInterval = 5;
        }

        logger.debug("Data request interval {} seconds", readDataInterval);
        cancelReadDataJob();
        readDataJob = this.scheduler.scheduleWithFixedDelay(this::refresh, 5L, readDataInterval, TimeUnit.SECONDS);
    }
    }

    @SuppressWarnings("null")
    private void refresh() {
        try {
            logger.trace("refresh...");

            if (isDisposed)
                cancelReadDataJob();

            if (e3dcconnect == null || e3dcconnect.isNotConnected())
                e3dcconnect = new E3DCConnector(this, config);

            if (e3dcconnect != null)
            e3dcconnect.requestE3DCData();

        } catch (Exception e) {
            String msg = String.format("Failed retriving data to. Closing Connection.");
            logger.debug(msg, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, msg);
            if (e3dcconnect != null) {
                e3dcconnect.close();
                e3dcconnect = null;
            }
        }
    }

    private void cancelReadDataJob() {
        final ScheduledFuture<?> scheduledFuture = readDataJob;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            logger.debug("Scheduled data requests cancelled");
        }
        this.readDataJob = null;
    }

    @Override
    @SuppressWarnings("null")
    public void dispose() {
        synchronized (initDisposeLock) {
        cancelReadDataJob();

        if (e3dcconnect != null) {
            e3dcconnect.close();
            e3dcconnect = null;
        }
            isDisposed = true;
        logger.debug("Thing {} disposed", getThing().getUID());
    }
    }

    @Override
    protected void updateState(String strChannelName, State dt) {
        super.updateState(strChannelName, dt);
    }

    @Override
    protected void updateStatus(ThingStatus ts, ThingStatusDetail statusDetail, @Nullable String reason) {
        super.updateStatus(ts, statusDetail, reason);
    }

    @Override
    protected void updateStatus(ThingStatus ts, ThingStatusDetail statusDetail) {
        super.updateStatus(ts, statusDetail);
    }

    @Override
    protected void updateStatus(ThingStatus ts) {
        super.updateStatus(ts);
    }

    public void addChannelsFromDevices() { // Dynamic channels
        logger.debug("Trying to build dynamic channels and groups...");

        final List<Channel> toBeAddedChannels = new ArrayList<>();
        final List<Channel> toBeRemovedChannels = new ArrayList<>();

        int removecount = 8;

        int pmCount = getForcedCount_PM();
        int pviCount = getForcedCount_PVI();
        int wbCount = getForcedCount_WB();

        for (int pmIndex = 1; pmIndex < removecount + 1; pmIndex++) {
            var oldChannelGroup = CHANNEL_GROUP_PM + ((pmIndex < 10) ? "0" : "") + Integer.toString(pmIndex);
            toBeRemovedChannels.addAll(removeChannelsOfGroup(oldChannelGroup));
        }

        for (int pviIndex = 1; pviIndex < removecount + 1; pviIndex++) {
            var oldChannelGroup = CHANNEL_GROUP_PVI + ((pviIndex < 10) ? "0" : "") + Integer.toString(pviIndex);
            toBeRemovedChannels.addAll(removeChannelsOfGroup(oldChannelGroup));
        }

        for (int wbIndex = 1; wbIndex < removecount + 1; wbIndex++) {
            var oldChannelGroup = CHANNEL_GROUP_WB + ((wbIndex < 10) ? "0" : "") + Integer.toString(wbIndex);
            toBeRemovedChannels.addAll(removeChannelsOfGroup(oldChannelGroup));
        }

        ThingBuilder builder = editThing().withoutChannels(toBeRemovedChannels);
        updateThing(builder.build());

        for (int pmIndex = 1; pmIndex < pmCount + 1; pmIndex++) {
            var newChannelGroup = CHANNEL_GROUP_PM + ((pmIndex < 10) ? "0" : "") + Integer.toString(pmIndex);
            toBeAddedChannels.addAll(createChannelsForGroup(newChannelGroup, CHANNEL_GROUP_TYPE_PM));
        }

        for (int pviIndex = 1; pviIndex < pviCount + 1; pviIndex++) {
            var newChannelGroup = CHANNEL_GROUP_PVI + ((pviIndex < 10) ? "0" : "") + Integer.toString(pviIndex);
            toBeAddedChannels.addAll(createChannelsForGroup(newChannelGroup, CHANNEL_GROUP_TYPE_PVI));
        }

        for (int wbIndex = 1; wbIndex < wbCount + 1; wbIndex++) {
            var newChannelGroup = CHANNEL_GROUP_WB + ((wbIndex < 10) ? "0" : "") + Integer.toString(wbIndex);
            toBeAddedChannels.addAll(createChannelsForGroup(newChannelGroup, CHANNEL_GROUP_TYPE_WB));
        }

        for (Channel channel : toBeAddedChannels) {
            builder.withChannel(channel);
        }

        updateThing(builder.build());
    }

    /**
     * Creates all {@link Channel}s for the given {@link ChannelGroupTypeUID}.
     *
     * @author Christoph Weitkamp - DarkSyk Binding Initial contribution
     * 
     * @param channelGroupId the channel group id
     * @param channelGroupTypeUID the {@link ChannelGroupTypeUID}
     * @return a list of all {@link Channel}s for the channel group
     */
    private List<Channel> createChannelsForGroup(String channelGroupId, ChannelGroupTypeUID channelGroupTypeUID) {
        logger.debug("Building channel group '{}' for thing '{}'.", channelGroupId, getThing().getUID());
        List<Channel> channels = new ArrayList<>();
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            for (ChannelBuilder channelBuilder : callback.createChannelBuilders(
                    new ChannelGroupUID(getThing().getUID(), channelGroupId), channelGroupTypeUID)) {
                Channel newChannel = channelBuilder.build(),
                        existingChannel = getThing().getChannel(newChannel.getUID().getId());
                if (existingChannel != null) {
                    logger.trace("Thing '{}' already has an existing channel '{}'. Omit adding new channel '{}'.",
                            getThing().getUID(), existingChannel.getUID(), newChannel.getUID());
                    continue;
                }
                channels.add(newChannel);
            }
        }
        return channels;
    }

    /**
     * Removes all {@link Channel}s of the given channel group.
     *
     * @author Christoph Weitkamp - DarkSyk Binding Initial contribution
     *
     * @param channelGroupId the channel group id
     * @return a list of all {@link Channel}s in the given channel group
     */
    private List<Channel> removeChannelsOfGroup(String channelGroupId) {
        logger.debug("Removing channel group '{}' from thing '{}'.", channelGroupId, getThing().getUID());
        return getThing().getChannelsOfGroup(channelGroupId);
    }
}
