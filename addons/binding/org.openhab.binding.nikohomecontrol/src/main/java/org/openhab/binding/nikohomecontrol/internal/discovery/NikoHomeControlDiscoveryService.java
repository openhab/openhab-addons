/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.discovery;

import static org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants.*;

import java.util.Date;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants;
import org.openhab.binding.nikohomecontrol.handler.NikoHomeControlBridgeHandler;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If a Niko Home Control bridge is added or if the user scans manually for things this
 * {@link NikoHomeControlDiscoveryService}
 * is used to return Niko Home Control Actions as things to the framework.
 *
 * @author Mark Herwege
 */
public class NikoHomeControlDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(NikoHomeControlDiscoveryService.class);

    private static final int TIMEOUT = 5;

    private ThingUID bridgeUID;
    private NikoHomeControlBridgeHandler handler;

    public NikoHomeControlDiscoveryService(NikoHomeControlBridgeHandler handler) {
        super(NikoHomeControlBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, false);
        logger.debug("Niko Home Control: discovery service {}", handler);
        this.bridgeUID = handler.getThing().getUID();
        this.handler = handler;
    }

    public void activate() {
        this.handler.setNhcDiscovery(this);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        this.handler.setNhcDiscovery(null);
        this.handler = null;
    }

    /**
     * Discovers devices connected to a Niko Home Control controller
     */
    public void discoverDevices() {
        NikoHomeControlCommunication nhcComm = this.handler.getCommunication();

        if ((nhcComm == null) || !nhcComm.communicationActive()) {
            logger.warn("Niko Home Control: not connected.");
            return;
        }
        logger.debug("Niko Home Control: getting devices on {}", this.handler.getThing().getUID().getId());

        Map<Integer, NhcAction> actions = nhcComm.getActions();

        for (Map.Entry<Integer, NhcAction> action : actions.entrySet()) {

            int actionId = action.getKey();
            NhcAction nhcAction = action.getValue();
            String thingName = nhcAction.getName();
            String thingLocation = nhcAction.getLocation();

            switch (nhcAction.getType()) {
                case 0: // handles all-off
                case 1: // switch
                    addDevice(new ThingUID(THING_TYPE_ON_OFF_LIGHT, this.handler.getThing().getUID(),
                            Integer.toString(actionId)), actionId, thingName, thingLocation);

                    break;
                case 2: // dimmer
                    addDevice(new ThingUID(THING_TYPE_DIMMABLE_LIGHT, this.handler.getThing().getUID(),
                            Integer.toString(actionId)), actionId, thingName, thingLocation);
                    break;
                case 4: // rollershutter
                case 5:
                    addDevice(new ThingUID(THING_TYPE_BLIND, this.handler.getThing().getUID(),
                            Integer.toString(actionId)), actionId, thingName, thingLocation);
                    break;
                default:
                    logger.debug("Niko Home Control: unrecognized action type {} for {} {}", nhcAction.getType(),
                            actionId, thingName);
            }
        }
    }

    private void addDevice(ThingUID uid, int actionId, String thingName, String thingLocation) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(thingName)
                .withProperty("Location", thingLocation).withProperty(CONFIG_ACTION_ID, actionId).build();
        thingDiscovered(discoveryResult);
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
