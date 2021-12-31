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
package org.openhab.binding.sonnen.internal;

import static org.openhab.binding.sonnen.internal.SonnenBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonnen.communication.SonnenJSONCommunication;
import org.openhab.binding.sonnen.communication.SonnenJsonDataDTO;
import org.openhab.binding.sonnen.utilities.Helper;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonnenHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class SonnenHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SonnenHandler.class);

    private SonnenConfiguration config = new SonnenConfiguration();

    private @Nullable ScheduledFuture<?> refreshJob;

    boolean resultOk = false;

    private SonnenJSONCommunication serviceCommunication;

    private boolean automaticRefreshing = false;

    private Map<String, Boolean> linkedChannels = new HashMap<String, Boolean>();

    public SonnenHandler(Thing thing) {
        super(thing);
        serviceCommunication = new SonnenJSONCommunication();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing sonnen handler for thing {}", getThing().getUID());
        config = getConfigAs(SonnenConfiguration.class);
        boolean validConfig = true;
        String errors = "";
        String statusDescr = null;
        if (config.refreshInterval < 0 && config.refreshInterval > 999) {
            errors += " Parameter 'refresh Rate' greater then 0 and less then 1000.";
            statusDescr = "Parameter 'refresh Rate' greater then 0 and less then 1000.";
            validConfig = false;
        }
        if (config.hostIP == null) {
            errors += " Parameter 'hostIP' must be configured.";
            statusDescr = "IP Address must be configured!";
            validConfig = false;
        }
        errors = errors.trim();
        Helper message = new Helper();
        message.setStatusDescription(statusDescr);
        if (validConfig) {
            serviceCommunication.setConfig(config);
            if (serviceCommunication.refreshBatteryConnection(message, this.getThing().getUID().toString())) {
                updateStatus(ThingStatus.ONLINE);
                updateLinkedChannels();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message.getStatusDesription());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message.getStatusDesription());
        }
    }

    /**
     * Calls the service to update the battery data
     *
     * @param postdata
     */
    private boolean updatebatteryData(@Nullable String postdata) {
        Helper message = new Helper();
        if (serviceCommunication.refreshBatteryConnection(message, this.getThing().getUID().toString())) {
            updateStatus(ThingStatus.ONLINE);
            return true;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    message.getStatusDesription());
            return false;
        }
    }

    private void updateLinkedChannels() {
        verifyLinkedChannel(CHANNELBATTERYCHARGING);
        verifyLinkedChannel(CHANNELBATTERYDISCHARGING);
        verifyLinkedChannel(CHANNELGRIDFEEDIN);
        verifyLinkedChannel(CHANNELCONSUMPTION);
        verifyLinkedChannel(CHANNELSOLARPRODUCTION);
        verifyLinkedChannel(CHANNELBATTERYLEVEL);
        verifyLinkedChannel(CHANNELFLOWCONSUMPTIONBATTERY);
        verifyLinkedChannel(CHANNELFLOWCONSUMPTIONGRID);
        verifyLinkedChannel(CHANNELFLOWCONSUMPTIONPRODUCTION);
        verifyLinkedChannel(CHANNELFLOWGRIDBATTERY);
        verifyLinkedChannel(CHANNELFLOWPRODUCTIONBATTERY);
        verifyLinkedChannel(CHANNELFLOWPRODUCTIONGRID);

        if (!linkedChannels.isEmpty()) {
            updatebatteryData(null);
            for (Channel channel : getThing().getChannels()) {
                updateChannel(channel.getUID().getId());
            }
            startAutomaticRefresh();
            automaticRefreshing = true;
        }
    }

    private void verifyLinkedChannel(String channelID) {
        if (isLinked(channelID) && !linkedChannels.containsKey(channelID)) {
            linkedChannels.put(channelID, true);
        }
    }

    @Override
    public void dispose() {
        stopScheduler();
    }

    private void stopScheduler() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
    }

    /**
     * Start the job refreshing the oven status
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> job = refreshJob;
        if (job == null || job.isCancelled()) {
            int period = config.refreshInterval;
            refreshJob = scheduler.scheduleWithFixedDelay(this::run, 0, period, TimeUnit.SECONDS);
        }
    }

    private void run() {
        updatebatteryData(null);
        for (Channel channel : getThing().getChannels()) {
            updateChannel(channel.getUID().getId());
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (!automaticRefreshing) {
            logger.debug("Start automatic refreshing");
            startAutomaticRefresh();
            automaticRefreshing = true;
        }
        verifyLinkedChannel(channelUID.getId());
        updateChannel(channelUID.getId());
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        linkedChannels.remove(channelUID.getId());
        if (linkedChannels.isEmpty()) {
            automaticRefreshing = false;
            stopScheduler();
            logger.debug("Stop automatic refreshing");
        }
    }

    private void updateChannel(String channelId) {
        if (isLinked(channelId)) {
            State state = null;
            SonnenJsonDataDTO data = serviceCommunication.getBatteryData();
            if (data != null) {
                switch (channelId) {
                    case CHANNELBATTERYDISCHARGING:
                        update(OnOffType.from(data.isBatteryDischarging()), channelId);
                        break;
                    case CHANNELBATTERYCHARGING:
                        update(OnOffType.from(data.isBatteryCharging()), channelId);
                        break;
                    case CHANNELCONSUMPTION:
                        state = new QuantityType<Power>(data.getConsumptionHouse(), Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNELGRIDFEEDIN:
                        state = new QuantityType<Power>(data.getGridFeedIn(), Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNELSOLARPRODUCTION:
                        state = new QuantityType<Power>(data.getSolarProduction(), Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNELBATTERYLEVEL:
                        state = new QuantityType<Dimensionless>(data.getBatteryChargingLevel(), Units.PERCENT);
                        update(state, channelId);
                        break;
                    case CHANNELFLOWCONSUMPTIONBATTERY:
                        update(OnOffType.from(data.isFlowConsumptionBattery()), channelId);
                        break;
                    case CHANNELFLOWCONSUMPTIONGRID:
                        update(OnOffType.from(data.isFlowConsumptionGrid()), channelId);
                        break;
                    case CHANNELFLOWCONSUMPTIONPRODUCTION:
                        update(OnOffType.from(data.isFlowConsumptionProduction()), channelId);
                        break;
                    case CHANNELFLOWGRIDBATTERY:
                        update(OnOffType.from(data.isFlowGridBattery()), channelId);
                        break;
                    case CHANNELFLOWPRODUCTIONBATTERY:
                        update(OnOffType.from(data.isFlowProductionBattery()), channelId);
                        break;
                    case CHANNELFLOWPRODUCTIONGRID:
                        update(OnOffType.from(data.isFlowProductionGrid()), channelId);
                        break;
                }
            }
        }
    }

    /**
     * Updates the State of the given channel
     *
     * @param state
     * @param channelId
     */
    private void update(@Nullable State state, String channelId) {
        logger.debug("Update channel {} with state {}", channelId, (state == null) ? "null" : state.toString());

        if (state != null) {
            updateState(channelId, state);

        } else {
            updateState(channelId, UnDefType.NULL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
