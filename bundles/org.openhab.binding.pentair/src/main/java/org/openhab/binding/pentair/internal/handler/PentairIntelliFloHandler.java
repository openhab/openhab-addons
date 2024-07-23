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
package org.openhab.binding.pentair.internal.handler;

import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.actions.PentairIntelliFloActions;
import org.openhab.binding.pentair.internal.handler.helpers.PentairPumpStatus;
import org.openhab.binding.pentair.internal.parser.PentairBasePacket;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairIntelliFloHandler} is responsible for implementation of the Intelliflo Pump. This will
 * parse status packets to set the stat for various channels.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairIntelliFloHandler extends PentairBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PentairIntelliFloHandler.class);
    private PentairPumpStatus pumpStatus = new PentairPumpStatus();

    // runmode is used to send watchdog to pump when running
    private boolean runMode = false;

    private static @Nullable ScheduledFuture<?> pollingJob;

    private PentairIntelliFloActions actions = new PentairIntelliFloActions();

    public PentairIntelliFloHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void finishOnline() {
        super.finishOnline();
        actions.initialize(Objects.requireNonNull(getBridgeHandler()).getBaseActions(), getPentairID());

        startPollingJob();
    }

    @Override
    public void goOffline(ThingStatusDetail detail) {
        super.goOffline(detail);

        // PentairIntelliFloHandler.pollingJob will be cancelled when called and there are no pumps associated
        // with the bridge
    }

    public PentairIntelliFloActions getActions() {
        return actions;
    }

    public void setRunMode(boolean runMode) {
        this.runMode = runMode;
    }

    private void startPollingJob() {
        if (pollingJob == null) {
            PentairIntelliFloHandler.pollingJob = scheduler
                    .scheduleWithFixedDelay(PentairIntelliFloHandler::pumpWatchDog, 10, 30, TimeUnit.SECONDS);
        }
    }

    private static void stopPollingJob() {
        ScheduledFuture<?> pollingJob = PentairIntelliFloHandler.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        PentairIntelliFloHandler.pollingJob = null;
    }

    /**
     * Job to send pump query status packages to all Intelliflo Pump things in order to see the status.
     * Note: From the internet is seems some FW versions of EasyTouch controllers send this automatically and this the
     * pump status packets can just be snooped, however my controller version does not do this. No harm in sending.
     *
     */
    private static void pumpWatchDog() {
        boolean pumpsStillOnline = false;
        Bridge bridge = PentairBaseBridgeHandler.getSingleBridge();
        if (bridge == null) {
            PentairIntelliFloHandler.stopPollingJob();
            return;
        }

        Collection<Thing> things = bridge.getThings();

        for (Thing t : things) {
            if (!t.getThingTypeUID().equals(INTELLIFLO_THING_TYPE)) {
                continue;
            }

            if (t.getStatus() != ThingStatus.ONLINE) {
                continue;
            }

            pumpsStillOnline = true;

            PentairIntelliFloHandler handler = (PentairIntelliFloHandler) t.getHandler();
            if (handler == null) {
                continue;
            }

            if (handler.runMode) {
                handler.getActions().coreSetOnOROff(true);
            } else {
                handler.getActions().getStatus();
            }
        }

        if (!pumpsStillOnline) {
            PentairIntelliFloHandler.stopPollingJob();
        }
    }

    // checkOtherMaster - check to make sure the system does not have a controller OR that the controller is in
    // servicemode
    private boolean checkOtherMaster() {
        PentairBaseBridgeHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.bridge-missing");
            return true;
        }

        PentairControllerHandler handler = bridgeHandler.findController();

        return (handler != null && !handler.getServiceMode());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType onOffCommand) {
            boolean state = onOffCommand == OnOffType.ON;

            switch (channelUID.getId()) {
                case CHANNEL_INTELLIFLO_RUN:
                case CHANNEL_INTELLIFLO_RPM:
                    if (!state) {
                        updateState(INTELLIFLO_RUNPROGRAM, OnOffType.OFF);
                    }

                    actions.setOnOrOff(state);

                    break;
                case INTELLIFLO_RUNPROGRAM:
                    if (checkOtherMaster()) {
                        logger.debug("Unable to send command to pump as there is another master in the system");
                        return;
                    }

                    if (command instanceof DecimalType programNumber) {
                        if (programNumber.intValue() == 0) {
                            actions.setOnOrOff(false);
                        } else {
                            actions.setRunProgram(programNumber.intValue());
                        }
                    }
            }
        } else if (command instanceof DecimalType decimalCommand) {
            int num = decimalCommand.intValue();

            switch (channelUID.getId()) {
                case CHANNEL_INTELLIFLO_RPM:
                    updateState(INTELLIFLO_RUNPROGRAM, OnOffType.OFF);
                    actions.setRPM(num);
                    break;
            }
        } else if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_INTELLIFLO_RUN:
                    updateChannel(channelUID, pumpStatus.run);
                    break;
                case CHANNEL_INTELLIFLO_POWER:
                    updateChannel(channelUID, pumpStatus.power, Units.WATT);
                    break;
                case CHANNEL_INTELLIFLO_RPM:
                    updateChannel(channelUID, pumpStatus.rpm);
                    break;
                case INTELLIFLO_GPM:
                    updateChannel(channelUID, pumpStatus.gpm, ImperialUnits.GALLON_PER_MINUTE);
                    break;
                case INTELLIFLO_STATUS1:
                    updateChannel(channelUID, pumpStatus.status1);
                    break;
                case INTELLIFLO_STATUS2:
                    updateChannel(channelUID, pumpStatus.status2);
                    break;
            }
        }
    }

    @Override
    public void processPacketFrom(PentairBasePacket packet) {
        if (waitStatusForOnline) {
            finishOnline();
        }

        PentairStandardPacket p = (PentairStandardPacket) packet;

        switch (p.getByte(PentairStandardPacket.ACTION)) {
            case 0x01: // Pump command - A5 00 10 60 01 02 00 20
                logger.debug("[{}] Pump command (ack)", p.getSource());
                break;
            case 0x04: // Pump control panel on/off
                boolean remotemode;

                remotemode = p.getByte(0 + PentairStandardPacket.STARTOFDATA) == (byte) 0xFF;
                logger.debug("[{}] Pump control panel (ack): {}", p.getSource(), remotemode);
                break;
            case 0x05: // Set pump mode ack
                logger.debug("[{}] Set pump mode (ack): {}", p.getSource(),
                        p.getByte(0 + PentairStandardPacket.STARTOFDATA));
                break;
            case 0x06: // Set run mode ack
                logger.debug("[{}] Set run mode (ack): {}", p.getSource(),
                        p.getByte(0 + PentairStandardPacket.STARTOFDATA));
                break;
            case 0x07: // Pump status (after a request)
                if (p.getPacketLengthHeader() != 15) {
                    logger.debug("[{}]: Expected length of 15 onm pump status: {}", p.getSource(), p);
                    return;
                }

                pumpStatus.parsePacket(p);
                logger.debug("[{}] Pump status: {}", p.getSource(), pumpStatus);
                this.refreshAllChannels();
                break;
            default:
                logger.debug("[{}] Unhandled Intelliflo command {}: {}", p.getSource(), p.getAction(), p.toString());
                break;
        }
    }
}
