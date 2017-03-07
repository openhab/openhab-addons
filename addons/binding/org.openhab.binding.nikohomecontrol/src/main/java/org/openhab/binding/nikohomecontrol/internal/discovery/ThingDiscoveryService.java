/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.discovery;

import static org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants;
import org.openhab.binding.nikohomecontrol.handler.NikoHomeControlBridgeHandler;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If a Niko Home Control bridge is added or if the user scans manually for things this {@link ThingDiscoveryService}
 * is used to return Niko Home Control Actions as things to the framework.
 *
 * @author Mark Herwege
 */
public class ThingDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(ThingDiscoveryService.class);

    private static final int TIMEOUT = 5;

    private ServiceRegistration<?> reg = null;
    private ThingUID bridgeUID;
    private NikoHomeControlBridgeHandler handler;

    private NikoHomeControlCommunication nhcComm;

    public ThingDiscoveryService(ThingUID bridgeUID, NikoHomeControlBridgeHandler handler) {
        super(NikoHomeControlBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, false);
        logger.debug("Niko Home Control: discovery service {}", handler);
        this.bridgeUID = bridgeUID;
        this.handler = handler;
    }

    public void start(BundleContext bundleContext) {
        if (reg != null) {
            return;
        }
        reg = bundleContext.registerService(DiscoveryService.class.getName(), this, new Hashtable<String, Object>());
    }

    public void stop() {
        if (reg != null) {
            reg.unregister();
        }
        handler = null;
        reg = null;
    }

    /**
     * Discovers devices connected to a Niko Home Control controller
     */
    public void discoverDevices() {

        nhcComm = handler.getCommunication();
        if (nhcComm == null) {
            logger.warn("Niko Home Control: not connected.");
            return;
        }
        logger.debug("Niko Home Control: getting devices on {}", handler.getThing().getUID().getId());

        for (int actionID : nhcComm.getActions()) {

            String thingName = nhcComm.getActionName(actionID);
            String thingLocation = nhcComm.getLocationName(nhcComm.getActionLocation(actionID));

            switch (nhcComm.getActionType(actionID)) {
                case 0: // handles all-off
                case 1: // switch
                    addDevice(new ThingUID(THING_TYPE_ON_OFF_LIGHT, handler.getThing().getUID(),
                            Integer.toString(actionID)), thingName, thingLocation);

                    break;
                case 2: // dimmer
                    addDevice(new ThingUID(THING_TYPE_DIMMABLE_LIGHT, handler.getThing().getUID(),
                            Integer.toString(actionID)), thingName, thingLocation);
                    break;
                case 5: // rollershutter
                    addDevice(new ThingUID(THING_TYPE_BLIND, handler.getThing().getUID(), Integer.toString(actionID)),
                            thingName, thingLocation);
                    break;
                default:
                    logger.warn("Niko Home Control: unrecognized action type {} for {} {}",
                            nhcComm.getActionType(actionID), actionID, thingName);
            }

        }

    }

    protected void addDevice(ThingUID uid, String thingName, String thingLocation) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(thingName)
                .withProperty("Location", thingLocation).build();
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
