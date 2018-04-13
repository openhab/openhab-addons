/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.internal.discovery;

import static org.openhab.binding.velbus.VelbusBindingConstants.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.velbus.handler.VelbusBridgeHandler;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.VelbusModule;
import org.openhab.binding.velbus.internal.VelbusModuleAddress;
import org.openhab.binding.velbus.internal.VelbusPacketListener;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusScanPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If the user scans manually for things this
 * {@link VelbusThingDiscoveryService}
 * is used to return Velbus Modules as things to the framework.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusThingDiscoveryService extends AbstractDiscoveryService implements VelbusPacketListener {
    private static final int SEARCH_TIME = 60;

    private final Logger logger = LoggerFactory.getLogger(VelbusThingDiscoveryService.class);

    private HashMap<Byte, VelbusModule> velbusModules = new HashMap<Byte, VelbusModule>();

    private VelbusBridgeHandler velbusBridgeHandler;

    public VelbusThingDiscoveryService(VelbusBridgeHandler velbusBridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
        this.velbusBridgeHandler = velbusBridgeHandler;
    }

    public void activate() {
        this.velbusBridgeHandler.setDefaultPacketListener(this);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        velbusBridgeHandler.setDefaultPacketListener(null);
    }

    @Override
    protected void startScan() {
        for (int i = 0x00; i <= 0xFF; i++) {
            VelbusScanPacket packet = new VelbusScanPacket((byte) i);
            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        }
    }

    @Override
    public void onPacketReceived(byte[] packet) {
        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte address = packet[2];
            byte length = packet[3];
            byte command = packet[4];

            if (command == COMMAND_MODULE_TYPE && packet.length >= 10) {
                handleModuleTypeCommand(packet, address);
            } else if (command == COMMAND_MODULE_NAME_PART1 & packet.length >= 4 + length) {
                handleChannelNameCommand(packet, address, length, 1);
            } else if (command == COMMAND_MODULE_NAME_PART2 && packet.length >= 4 + length) {
                handleChannelNameCommand(packet, address, length, 2);
            } else if (command == COMMAND_MODULE_NAME_PART3 && packet.length >= 4 + length) {
                handleChannelNameCommand(packet, address, length, 3);
            } else if (command == COMMAND_SUBTYPE && packet.length >= 4 + length) {
                handleModuleSubtypeCommand(packet, address);
            } else {
                logger.debug("Unknown command '{}' to address '{}'.", String.format("%02X", command),
                        String.format("%02X", address));
            }
        }
    }

    private void handleModuleTypeCommand(byte[] packet, byte address) {
        byte moduleType = packet[5];
        byte highByteOfSerialNumber = packet[6];
        byte lowByteOfSerialNumber = packet[7];
        byte memoryMapVersion = packet[8];
        byte buildYear = packet[9];
        byte buildWeek = packet[10];

        VelbusModule velbusModule = null;
        switch (moduleType) {
            case MODULE_TYPE_VMB1BLS:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB1BLS, 1);
                break;
            case MODULE_TYPE_VMB1DM:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB1DM, 1);
                break;
            case MODULE_TYPE_VMB1LED:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB1LED, 1);
                break;
            case MODULE_TYPE_VMB1RY:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB1RY, 1);
                break;
            case MODULE_TYPE_VMB1RYNO:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB1RYNO, 5);
                break;
            case MODULE_TYPE_VMB1RYNOS:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB1RYNOS, 5);
                break;
            case MODULE_TYPE_VMB2BLE:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB2BLE, 2);
                break;
            case MODULE_TYPE_VMB2PBN:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB2PBN, 8);
                break;
            case MODULE_TYPE_VMB4DC:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB4DC, 4);
                break;
            case MODULE_TYPE_VMB4RY:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB4RY, 4);
                break;
            case MODULE_TYPE_VMB4RYLD:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB4RYLD, 5);
                break;
            case MODULE_TYPE_VMB4RYNO:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB4RYNO, 5);
                break;
            case MODULE_TYPE_VMB6IN:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB6IN, 6);
                break;
            case MODULE_TYPE_VMB6PBN:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB6PBN, 8);
                break;
            case MODULE_TYPE_VMB7IN:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB7IN, 8);
                break;
            case MODULE_TYPE_VMB8IR:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB8IR, 8);
                break;
            case MODULE_TYPE_VMB8PB:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB8PB, 8);
                break;
            case MODULE_TYPE_VMB8PBU:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB8PBU, 8);
                break;
            case MODULE_TYPE_VMBDME:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBDME, 1);
                break;
            case MODULE_TYPE_VMBDMI:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBDMI, 1);
                break;
            case MODULE_TYPE_VMBDMIR:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBDMIR, 1);
                break;
            case MODULE_TYPE_VMBGP1:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP1, 9);
                break;
            case MODULE_TYPE_VMBGP2:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP2, 9);
                break;
            case MODULE_TYPE_VMBGP4:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP4, 9);
                break;
            case MODULE_TYPE_VMBGP4PIR:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP4PIR, 9);
                break;
            case MODULE_TYPE_VMBGPO:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGPO, 33);
                break;
            case MODULE_TYPE_VMBGPOD:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGPOD, 33);
                break;
            case MODULE_TYPE_VMBPIRC:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBPIRC, 7);
                break;
            case MODULE_TYPE_VMBPIRM:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBPIRM, 7);
                break;
            case MODULE_TYPE_VMBPIRO:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBPIRO, 9);
                break;
        }

        if (velbusModule != null) {
            registerVelbusModule(address, velbusModule);
        }
    }

    private void handleModuleSubtypeCommand(byte[] packet, byte address) {
        VelbusModule velbusModule = velbusModules.get(address);

        if (velbusModule != null) {
            byte[] subAddresses = new byte[4];
            System.arraycopy(packet, 8, subAddresses, 0, 4);

            velbusModule.getModuleAddress().setSubAddresses(subAddresses);

            for (int i = 0; i < subAddresses.length; i++) {
                if (subAddresses[i] != (byte) 0xFF) {
                    velbusModules.put(subAddresses[i], velbusModule);
                }
            }

            notifyDiscoveredVelbusModule(velbusModule);
        }
    }

    protected void registerVelbusModule(byte address, VelbusModule velbusModule) {
        velbusModules.put(address, velbusModule);
        notifyDiscoveredVelbusModule(velbusModule);

        velbusModule.sendChannelNameRequests(velbusBridgeHandler);
    }

    private void handleChannelNameCommand(byte[] packet, byte address, byte length, int namePartNumber) {
        VelbusModule velbusModule = velbusModules.get(address);

        if (velbusModule != null) {
            byte channel = packet[5];
            byte[] namePart = Arrays.copyOfRange(packet, 6, 6 + length - 2);

            VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address, channel);
            velbusModule.setChannelName(velbusChannelIdentifier, namePartNumber, namePart);
            notifyDiscoveredVelbusModule(velbusModule);
        }
    }

    private void notifyDiscoveredVelbusModule(VelbusModule velbusModule) {
        ThingUID bridgeUID = velbusBridgeHandler.getThing().getUID();

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(velbusModule.getThingUID(bridgeUID))
                .withThingType(velbusModule.getThingTypeUID()).withProperties(velbusModule.getProperties())
                .withRepresentationProperty(MODULE_ADDRESS).withBridge(bridgeUID).withLabel(velbusModule.getLabel())
                .build();

        thingDiscovered(discoveryResult);
    }
}
