/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.eltako.internal.discovery;

import static org.openhab.binding.eltako.internal.misc.EltakoBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.eltako.internal.handler.EltakoFam14BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EltakoDeviceDiscoveryService} is used to discover Eltako devices
 *
 * @author Martin Wenske - Initial contribution
 */

public class EltakoDeviceDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(EltakoDeviceDiscoveryService.class);

    /*
     * Some local variables
     */
    private EltakoFam14BridgeHandler bridgeHandler;
    private Boolean DeviceDiscoveryThreadIsNotCanceled;
    private Boolean DeviceDiscoveryThreadDone;

    public EltakoDeviceDiscoveryService(EltakoFam14BridgeHandler bridgeHandler) {
        super(null, 40, false);
        this.bridgeHandler = bridgeHandler;
        DeviceDiscoveryThreadIsNotCanceled = false;
        DeviceDiscoveryThreadDone = true;
    }

    /**
     * Device Discovery Service has been added to a bridge.
     */
    public void activate() {
        super.activate(null);
        logger.debug("Aktivate Device Discovery Service");
    }

    /**
     * Device Discovery Service has been removed from a bridge = Stop any active scan.
     */
    @Override
    public void deactivate() {
        super.deactivate();
        // Log event to console
        logger.debug("Deaktivate Device Discovery Service");
        // Stopping scan
        DeviceDiscoveryThreadIsNotCanceled = false;
        // Wait for scan to be stopped
        while (!DeviceDiscoveryThreadDone) {
            ;
        }
    }

    /**
     * Scan for new devices. This method is called by the framework within a new thread.
     */
    @Override
    protected void startScan() {
        int[] message = new int[14];
        int[] data = new int[] { 0, 0, 0, 0 };
        int[] id = new int[] { 0, 0, 0, 0 };

        // Log event to console
        logger.debug("Starting Eltako discovery scan");
        // Signal scan is running
        DeviceDiscoveryThreadDone = false;
        // Set Discovery Thread exit condition
        DeviceDiscoveryThreadIsNotCanceled = true;

        for (int i = 0; i < 256; i++) {
            // Check if Thread should end
            if (!DeviceDiscoveryThreadIsNotCanceled) {
                break;
            }
            if (this.bridgeHandler == null) {
                // Log event to console
                logger.debug("Bridge instance not available => end scan");
                break;
            }
            if (i == 0) {
                // Force FAM14 into config mode
                this.bridgeHandler.constuctMessage(message, 5, 0xff, data, id, 0xFF);
                // Log event to console
                logger.debug("DiscoveryService: Force FAM14 into config mode");
            } else if (i == 255) {
                // Force FAM14 into telegram mode
                this.bridgeHandler.constuctMessage(message, 5, 0xff, data, id, 0x00);
                // Log event to console
                logger.debug("DiscoveryService: Force FAM14 into telegram mode");
            } else {
                // Search for ID
                this.bridgeHandler.constuctMessage(message, 5, 0xf0, data, id, i);
                // Log event to console
                logger.debug("DiscoveryService: Search for device with ID {}", i);
            }
            // Send telegram using bridge
            this.bridgeHandler.serialWrite(message, 14);

            // Wait some time to ease the CPU
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("Sleep does not work in DeviceDiscoveryThread: {}", e);
            }
        }
        // Signal scan has been stopped
        DeviceDiscoveryThreadDone = true;
        // Log event to console
        logger.debug("DeviceDiscoveryThread ended");
    }

    /**
     * Device scan should be stopped
     */
    @Override
    public void stopScan() {
        super.stopScan();
        // Log event to console
        logger.debug("Stopping Eltako discovery scan");
        // Stopping scan
        DeviceDiscoveryThreadIsNotCanceled = false;
        // Wait for scan to be stopped
        while (!DeviceDiscoveryThreadDone) {
            ;
        }
    }

    /**
     * Called by framework in order to get supported thing types
     */
    @Override
    public Set<@NonNull ThingTypeUID> getSupportedThingTypes() {
        logger.debug("Get supported thing types");
        return SUPPORTED_DEVICE_THING_TYPES_UIDS;
    }

    /**
     * Called by Bridge when a new telegram has been received
     */
    public void telegramReceived(int[] packet) {
        // Check if the scan is active
        if (DeviceDiscoveryThreadDone == true) {
            return;
        }
        // ###########################################################
        // Prepare data to be written to log
        StringBuffer strbuf = new StringBuffer();
        // Create string out of byte data
        for (int i = 0; i < 14; i++) {
            strbuf.append(String.format("%02X ", packet[i]));
        }
        // Log event to console
        logger.trace("DeviceDiscovery: Telegram Received: {}", strbuf);
        // ###########################################################
        // Add new devices depending on the ID count replied by the device
        for (int i = 0; i < packet[5]; i++) {
            // Create new device
            createdevice(packet[9], packet[4] + i, packet[10]);
        }
    }

    /**
     * Add a discovered device to list of found devices
     */
    public void createdevice(int modelId, int deviceId, int hardwareVersion) {
        ThingTypeUID thingTypeUID;
        // Create instance of new thing depending on modelId received
        switch (modelId) {
            case 4:
                thingTypeUID = new ThingTypeUID(BINDING_ID, "FUD14");
                break;
            case 6:
                thingTypeUID = new ThingTypeUID(BINDING_ID, "FSB14");
                break;
            default:
                return;
        }
        logger.debug("Created Device: modelId = {} deviceId = {}", modelId, deviceId);
        // Get new Thing UID
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeHandler.getThing().getUID(),
                String.format("%08d", deviceId));
        // Create result (thing)
        DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(thingUID)
                .withBridge(bridgeHandler.getThing().getUID());
        // Add property to device
        discoveryResultBuilder.withProperty(GENERIC_DEVICE_ID, String.format("%08d", deviceId));
        discoveryResultBuilder.withProperty(GENERIC_HARDWARE_VERSION,
                String.format("V%d.%d", hardwareVersion >> 4, hardwareVersion & 0x0F));
        // Add thing to discovery result list
        thingDiscovered(discoveryResultBuilder.build());
    }
}
