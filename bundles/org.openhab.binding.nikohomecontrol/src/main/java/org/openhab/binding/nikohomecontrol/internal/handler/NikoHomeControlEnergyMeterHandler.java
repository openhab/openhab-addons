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
package org.openhab.binding.nikohomecontrol.internal.handler;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.CHANNEL_POWER;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcEnergyMeter;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcEnergyMeterEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcEnergyMeter2;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlEnergyMeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlEnergyMeterHandler extends BaseThingHandler implements NhcEnergyMeterEvent {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlEnergyMeterHandler.class);

    private volatile @Nullable NhcEnergyMeter nhcEnergyMeter;

    private String energyMeterId = "";

    public NikoHomeControlEnergyMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        NhcEnergyMeter nhcEnergyMeter = this.nhcEnergyMeter;
        if (nhcEnergyMeter == null) {
            logger.debug("energy meter with ID {} not initialized", energyMeterId);
            return;
        }

        if (REFRESH.equals(command)) {
            energyMeterEvent(nhcEnergyMeter.getPower());
        }
    }

    @Override
    public void initialize() {
        NikoHomeControlEnergyMeterConfig config = getConfig().as(NikoHomeControlEnergyMeterConfig.class);

        energyMeterId = config.energyMeterId;

        NikoHomeControlCommunication nhcComm = getCommunication();
        if (nhcComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "@text/offline.bridge-unitialized");
            return;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }

        // We need to do this in a separate thread because we may have to wait for the
        // communication to become active
        scheduler.submit(() -> {
            if (!nhcComm.communicationActive()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error");
                return;
            }

            NhcEnergyMeter nhcEnergyMeter = nhcComm.getEnergyMeters().get(energyMeterId);
            if (nhcEnergyMeter == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.configuration-error.energyMeterId");
                return;
            }

            nhcEnergyMeter.setEventHandler(this);

            updateProperties();

            // Subscribing to power readings starts an intensive data flow, therefore only do it when there is an item
            // linked to the channel
            if (isLinked(CHANNEL_POWER)) {
                nhcComm.startEnergyMeter(energyMeterId);
            }

            this.nhcEnergyMeter = nhcEnergyMeter;

            logger.debug("energy meter intialized {}", energyMeterId);

            Bridge bridge = getBridge();
            if ((bridge != null) && (bridge.getStatus() == ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        });
    }

    @Override
    public void dispose() {
        NikoHomeControlCommunication nhcComm = getCommunication();

        if (nhcComm != null) {
            nhcComm.stopEnergyMeter(energyMeterId);
        }
    }

    private void updateProperties() {
        Map<String, String> properties = new HashMap<>();

        if (nhcEnergyMeter instanceof NhcEnergyMeter2) {
            NhcEnergyMeter2 energyMeter = (NhcEnergyMeter2) nhcEnergyMeter;
            properties.put("model", energyMeter.getModel());
            properties.put("technology", energyMeter.getTechnology());
        }

        thing.setProperties(properties);
    }

    @Override
    public void energyMeterEvent(@Nullable Integer power) {
        if (power == null) {
            updateState(CHANNEL_POWER, UnDefType.UNDEF);
        } else {
            updateState(CHANNEL_POWER, new QuantityType<>(power, Units.WATT));
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void energyMeterInitialized() {
        Bridge bridge = getBridge();
        if ((bridge != null) && (bridge.getStatus() == ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void energyMeterRemoved() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.configuration-error.energyMeterRemoved");
    }

    @Override
    // Subscribing to power readings starts an intensive data flow, therefore only do it when there is an item linked to
    // the channel
    public void channelLinked(ChannelUID channelUID) {
        NikoHomeControlCommunication nhcComm = getCommunication();
        if (nhcComm != null) {
            // This can be expensive, therefore do it in a job.
            scheduler.submit(() -> {
                if (!nhcComm.communicationActive()) {
                    restartCommunication(nhcComm);
                }

                if (nhcComm.communicationActive()) {
                    nhcComm.startEnergyMeter(energyMeterId);
                    updateStatus(ThingStatus.ONLINE);
                }
            });
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        NikoHomeControlCommunication nhcComm = getCommunication();
        if (nhcComm != null) {
            // This can be expensive, therefore do it in a job.
            scheduler.submit(() -> {
                if (!nhcComm.communicationActive()) {
                    restartCommunication(nhcComm);
                }

                if (nhcComm.communicationActive()) {
                    nhcComm.stopEnergyMeter(energyMeterId);
                    // as this is momentary power production/consumption, we set it UNDEF as we do not get readings
                    // anymore
                    updateState(CHANNEL_POWER, UnDefType.UNDEF);
                    updateStatus(ThingStatus.ONLINE);
                }
            });
        }
    }

    private void restartCommunication(NikoHomeControlCommunication nhcComm) {
        // We lost connection but the connection object is there, so was correctly started.
        // Try to restart communication.
        nhcComm.restartCommunication();
        // If still not active, take thing offline and return.
        if (!nhcComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error");
            return;
        }
        // Also put the bridge back online
        NikoHomeControlBridgeHandler nhcBridgeHandler = getBridgeHandler();
        if (nhcBridgeHandler != null) {
            nhcBridgeHandler.bridgeOnline();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "@text/offline.bridge-unitialized");
        }
    }

    private @Nullable NikoHomeControlCommunication getCommunication() {
        NikoHomeControlBridgeHandler nhcBridgeHandler = getBridgeHandler();
        return nhcBridgeHandler != null ? nhcBridgeHandler.getCommunication() : null;
    }

    private @Nullable NikoHomeControlBridgeHandler getBridgeHandler() {
        Bridge nhcBridge = getBridge();
        return nhcBridge != null ? (NikoHomeControlBridgeHandler) nhcBridge.getHandler() : null;
    }
}
