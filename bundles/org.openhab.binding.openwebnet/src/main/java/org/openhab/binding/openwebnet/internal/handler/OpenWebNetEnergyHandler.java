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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.CHANNEL_POWER_TOTALIZER_DAY;
import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.CHANNEL_POWER_TOTALIZER_MONTH;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.openwebnet4j.OpenGateway;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.EnergyManagement;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereEnergyManagement;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetEnergyHandler} is responsible for handling commands/messages for an Energy Management OpenWebNet
 * device. It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 * @author Andrea Conte - Energy management
 */
@NonNullByDefault
public class OpenWebNetEnergyHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetEnergyHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.ENERGY_MANAGEMENT_SUPPORTED_THING_TYPES;
    private static final int POWER_SUBSCRIPTION_PERIOD = 10; // MINUTES
    private static int ENERGY_REFRESH_PERIOD = 30; // MINUTES

    private @Nullable ScheduledFuture<?> powerSchedule;
    private @Nullable ScheduledFuture<?> energySchedule;

    public OpenWebNetEnergyHandler(Thing thing) {
        super(thing);
    }

    public Boolean isFirstSchedulerLaunch = true;

    private Boolean channelExists(String channelID) {
        return thing.getChannel("openwebnet:" + channelID) != null;
    }

    @Override
    public void initialize() {
        super.initialize();

        Object refreshPeriodConfig = getConfig().get(OpenWebNetBindingConstants.CONFIG_PROPERTY_REFRESH_PERIOD);
        ENERGY_REFRESH_PERIOD = Integer.parseInt(refreshPeriodConfig.toString());

        // In order to get data from the probe we must send a command over the bus, this could be done only when the
        // bridge is online.
        // a) usual flow: binding is starting, the bridge isn't online (startup flow not yet completed) --> subscriber
        // can't be started here, it will be started inside the bridgeStatusChanged event.
        // b) thing's discovery: binding is up and running, the bridge is online --> subscriber must be started here
        // otherwise data will be read only after a reboot.

        OpenWebNetBridgeHandler h = bridgeHandler;
        if (h != null && h.isBusGateway()) {
            OpenGateway gw = h.gateway;
            if (gw != null && gw.isConnected()) {
                // bridge is online
                subscribeToActivePowerChanges();
                subscribeToEnergyTotalizer();
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        // subscribe the scheduler only after the bridge is online
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            subscribeToActivePowerChanges();
            subscribeToEnergyTotalizer();
        }
    }

    private void subscribeToActivePowerChanges() {
        powerSchedule = scheduler.scheduleWithFixedDelay(() -> {
            if (isFirstSchedulerLaunch) {
                logger.debug(
                        "subscribeToActivePowerChanges() For WHERE={} subscribing to active power changes notification for the next {}min",
                        deviceWhere, POWER_SUBSCRIPTION_PERIOD);
            } else {
                logger.debug(
                        "subscribeToActivePowerChanges() Refreshing subscription for the next {}min for WHERE={} to active power changes notification",
                        POWER_SUBSCRIPTION_PERIOD, deviceWhere);
            }
            Where w = deviceWhere;
            if (w == null) {
                logger.warn("subscribeToActivePowerChanges() WHERE=null. Skipping");
            } else {
                try {
                    send(EnergyManagement.setActivePowerNotificationsTime(w.value(), POWER_SUBSCRIPTION_PERIOD));
                    isFirstSchedulerLaunch = false;
                } catch (Exception e) {
                    if (isFirstSchedulerLaunch) {
                        logger.warn(
                                "subscribeToActivePowerChanges() For WHERE={} could not subscribe to active power changes notifications. Exception={}",
                                w, e.getMessage());
                    } else {
                        logger.warn(
                                "subscribeToActivePowerChanges() Unable to refresh subscription to active power changes notifications for WHERE={}. Exception={}",
                                w, e.getMessage());
                    }
                }
            }
        }, 0, POWER_SUBSCRIPTION_PERIOD - 1, TimeUnit.MINUTES);
    }

    private void subscribeToEnergyTotalizer() {

        if (!channelExists(CHANNEL_POWER_TOTALIZER_DAY) && !channelExists(CHANNEL_POWER_TOTALIZER_MONTH)) {
            logger.info("subscribeToEnergyTotalizer() No totalizers channels active. Skipping");
            return;
        }

        energySchedule = scheduler.scheduleWithFixedDelay(() -> {
            Where w = deviceWhere;
            if (w == null) {
                logger.warn("subscribeToEnergyTotalizer() WHERE=null. Skipping");
            } else {
                try {
                    if (channelExists(CHANNEL_POWER_TOTALIZER_DAY)) {
                        send(EnergyManagement.requestCurrentDayTotalizer(w.value()));
                    }
                    if (channelExists(CHANNEL_POWER_TOTALIZER_MONTH)) {
                        send(EnergyManagement.requestCurrentMonthTotalizer(w.value()));
                    }
                } catch (Exception e) {
                    logger.warn(
                            "subscribeToEnergyTotalizer() Could not subscribe to totalizers scheduler for WHERE={}. Exception={}",
                            w, e.getMessage());
                }
            }
        }, 0, ENERGY_REFRESH_PERIOD, TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        if (powerSchedule != null) {
            ScheduledFuture<?> ns = powerSchedule;
            ns.cancel(false);
            logger.debug("dispose() scheduler stopped.");
        }
        super.dispose();
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereEnergyManagement(wStr);
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        super.requestChannelState(channel);
        Where w = deviceWhere;
        if (w != null) {
            try {
                send(EnergyManagement.requestActivePower(w.value()));

                // refresh ONLY subscribed channels
                if (channelExists(CHANNEL_POWER_TOTALIZER_DAY)) {
                    send(EnergyManagement.requestCurrentDayTotalizer(w.value()));
                }
                if (channelExists(CHANNEL_POWER_TOTALIZER_MONTH)) {
                    send(EnergyManagement.requestCurrentMonthTotalizer(w.value()));
                }

            } catch (OWNException e) {
                logger.debug("Exception while requesting state for channel {}: {} ", channel, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        logger.debug("--- refreshDevice() : refreshing SINGLE... ({})", thing.getUID());
        requestChannelState(new ChannelUID(thing.getUID(), CHANNEL_POWER));

        // refresh ONLY subscribed channels
        if (channelExists(CHANNEL_POWER_TOTALIZER_DAY)) {
            requestChannelState(new ChannelUID(thing.getUID(), CHANNEL_POWER_TOTALIZER_DAY));
        }
        if (channelExists(CHANNEL_POWER_TOTALIZER_MONTH)) {
            requestChannelState(new ChannelUID(thing.getUID(), CHANNEL_POWER_TOTALIZER_MONTH));
        }
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        logger.warn("handleChannelCommand() Read only channel, unsupported command {}", command);
    }

    @Override
    protected String ownIdPrefix() {
        return Who.ENERGY_MANAGEMENT.value().toString();
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        super.handleMessage(msg);

        if (msg.isCommand()) {
            logger.warn("handleMessage() Ignoring unsupported command for thing {}. Frame={}", getThing().getUID(),
                    msg);
            return;
        } else {
            // fix: check for correct DIM (ActivePower / 113)
            if (msg.getDim().equals(EnergyManagement.DimEnergyMgmt.ACTIVE_POWER)) {
                updateActivePower(msg);
            } else if (msg.getDim().equals(EnergyManagement.DimEnergyMgmt.PARTIAL_TOTALIZER_CURRENT_DAY)) {
                updateCurrentDayTotalizer(msg);
            } else if (msg.getDim().equals(EnergyManagement.DimEnergyMgmt.PARTIAL_TOTALIZER_CURRENT_MONTH)) {
                updateCurrentMonthTotalizer(msg);
            } else {
                logger.debug("handleMessage() Ignoring message {} because it's not related to active power value.",
                        msg);
            }
        }
    }

    /**
     * Updates energy power state based on an EnergyManagement message received from the OWN network
     *
     * @param msg the EnergyManagement message received
     * @throws FrameException
     */
    private void updateActivePower(BaseOpenMessage msg) {
        Integer activePower;
        try {
            activePower = Integer.parseInt(msg.getDimValues()[0]);
            updateState(CHANNEL_POWER, new QuantityType<Power>(activePower, Units.WATT));
        } catch (FrameException e) {
            logger.warn("FrameException on frame {}: {}", msg, e.getMessage());
            updateState(CHANNEL_POWER, UnDefType.UNDEF);
        }
    }

    /**
     * Updates current month totalizer
     *
     * @param msg the EnergyManagement message received
     * @throws FrameException
     */
    private void updateCurrentDayTotalizer(BaseOpenMessage msg) {
        double currentDayEnergy;
        try {
            // TODO remove
            logger.debug("AC Wh: {}", Double.parseDouble(msg.getDimValues()[0]));
            logger.debug("AC KWh: {}", Double.parseDouble(msg.getDimValues()[0]) / 1000d);

            currentDayEnergy = Double.parseDouble(msg.getDimValues()[0]) / 1000d;
            updateState(CHANNEL_POWER_TOTALIZER_DAY, new QuantityType<Energy>(currentDayEnergy, Units.KILOWATT_HOUR));
        } catch (FrameException e) {
            logger.warn("FrameException on frame {}: {}", msg, e.getMessage());
            updateState(CHANNEL_POWER_TOTALIZER_DAY, UnDefType.UNDEF);
        }
    }

    /**
     * Updates current month totalizer
     *
     * @param msg the EnergyManagement message received
     * @throws FrameException
     */
    private void updateCurrentMonthTotalizer(BaseOpenMessage msg) {
        double currentMonthEnergy;
        try {
            currentMonthEnergy = Double.parseDouble(msg.getDimValues()[0]) / 1000d;
            updateState(CHANNEL_POWER_TOTALIZER_MONTH,
                    new QuantityType<Energy>(currentMonthEnergy, Units.KILOWATT_HOUR));
        } catch (FrameException e) {
            logger.warn("FrameException on frame {}: {}", msg, e.getMessage());
            updateState(CHANNEL_POWER_TOTALIZER_MONTH, UnDefType.UNDEF);
        }
    }
}
