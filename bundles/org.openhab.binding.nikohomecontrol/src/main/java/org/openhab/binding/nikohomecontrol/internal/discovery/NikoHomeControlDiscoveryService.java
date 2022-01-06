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
package org.openhab.binding.nikohomecontrol.internal.discovery;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;

import java.util.Date;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlBridgeHandler;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcEnergyMeter;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcThermostat;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If a Niko Home Control bridge is added or if the user scans manually for things this
 * {@link NikoHomeControlDiscoveryService} is used to return Niko Home Control Actions as things to the framework.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlDiscoveryService.class);

    private static final int TIMEOUT = 5;

    private ThingUID bridgeUID;
    private NikoHomeControlBridgeHandler handler;

    public NikoHomeControlDiscoveryService(NikoHomeControlBridgeHandler handler) {
        super(SUPPORTED_THING_TYPES_UIDS, TIMEOUT, false);
        logger.debug("discovery service {}", handler);
        bridgeUID = handler.getThing().getUID();
        this.handler = handler;
    }

    public void activate() {
        handler.setNhcDiscovery(this);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        handler.setNhcDiscovery(null);
    }

    /**
     * Discovers devices connected to a Niko Home Control controller
     */
    public void discoverDevices() {
        NikoHomeControlCommunication nhcComm = handler.getCommunication();

        if ((nhcComm == null) || !nhcComm.communicationActive()) {
            logger.warn("not connected");
            return;
        }
        logger.debug("getting devices on {}", handler.getThing().getUID().getId());

        Map<String, NhcAction> actions = nhcComm.getActions();

        actions.forEach((actionId, nhcAction) -> {
            String thingName = nhcAction.getName();
            String thingLocation = nhcAction.getLocation();

            switch (nhcAction.getType()) {
                case TRIGGER:
                    addActionDevice(new ThingUID(THING_TYPE_PUSHBUTTON, handler.getThing().getUID(), actionId),
                            actionId, thingName, thingLocation);
                    break;
                case RELAY:
                    addActionDevice(new ThingUID(THING_TYPE_ON_OFF_LIGHT, handler.getThing().getUID(), actionId),
                            actionId, thingName, thingLocation);
                    break;
                case DIMMER:
                    addActionDevice(new ThingUID(THING_TYPE_DIMMABLE_LIGHT, handler.getThing().getUID(), actionId),
                            actionId, thingName, thingLocation);
                    break;
                case ROLLERSHUTTER:
                    addActionDevice(new ThingUID(THING_TYPE_BLIND, handler.getThing().getUID(), actionId), actionId,
                            thingName, thingLocation);
                    break;
                default:
                    logger.debug("unrecognized action type {} for {} {}", nhcAction.getType(), actionId, thingName);
            }
        });

        Map<String, NhcThermostat> thermostats = nhcComm.getThermostats();

        thermostats.forEach((thermostatId, nhcThermostat) -> {
            String thingName = nhcThermostat.getName();
            String thingLocation = nhcThermostat.getLocation();
            addThermostatDevice(new ThingUID(THING_TYPE_THERMOSTAT, handler.getThing().getUID(), thermostatId),
                    thermostatId, thingName, thingLocation);
        });

        Map<String, NhcEnergyMeter> energyMeters = nhcComm.getEnergyMeters();

        energyMeters.forEach((energyMeterId, nhcEnergyMeter) -> {
            String thingName = nhcEnergyMeter.getName();
            addEnergyMeterDevice(new ThingUID(THING_TYPE_ENERGYMETER, handler.getThing().getUID(), energyMeterId),
                    energyMeterId, thingName);
        });
    }

    private void addActionDevice(ThingUID uid, String actionId, String thingName, @Nullable String thingLocation) {
        DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(thingName).withProperty(CONFIG_ACTION_ID, actionId);
        if (thingLocation != null) {
            discoveryResultBuilder.withProperty("Location", thingLocation);
        }
        thingDiscovered(discoveryResultBuilder.build());
    }

    private void addThermostatDevice(ThingUID uid, String thermostatId, String thingName,
            @Nullable String thingLocation) {
        DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(thingName).withProperty(CONFIG_THERMOSTAT_ID, thermostatId);
        if (thingLocation != null) {
            discoveryResultBuilder.withProperty("Location", thingLocation);
        }
        thingDiscovered(discoveryResultBuilder.build());
    }

    private void addEnergyMeterDevice(ThingUID uid, String energyMeterId, String thingName) {
        DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(thingName).withProperty(CONFIG_ENERGYMETER_ID, energyMeterId);
        thingDiscovered(discoveryResultBuilder.build());
    }

    @Override
    protected void startScan() {
        discoverDevices();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }
}
