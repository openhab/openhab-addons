/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.velbus.internal.discovery;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.VelbusFirstGenerationDeviceModuleAddress;
import org.openhab.binding.velbus.internal.VelbusModule;
import org.openhab.binding.velbus.internal.VelbusModuleAddress;
import org.openhab.binding.velbus.internal.VelbusPacketListener;
import org.openhab.binding.velbus.internal.handler.VelbusBridgeHandler;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusScanPacket;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If the user scans manually for things this
 * {@link VelbusThingDiscoveryService}
 * is used to return Velbus Modules as things to the framework.
 *
 * @author Cedric Boon - Initial contribution
 * @author Daniel Rosengarten - Review sub-addresses management, add VMB1RYS, VMBDALI
 */
@Component(scope = ServiceScope.PROTOTYPE, service = VelbusThingDiscoveryService.class)
@NonNullByDefault
public class VelbusThingDiscoveryService extends AbstractThingHandlerDiscoveryService<VelbusBridgeHandler>
        implements VelbusPacketListener {
    private static final int SEARCH_TIME = 60;

    private final Logger logger = LoggerFactory.getLogger(VelbusThingDiscoveryService.class);

    private Map<Byte, VelbusModule> velbusModules = new HashMap<>();

    public VelbusThingDiscoveryService() {
        super(VelbusBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now());
        thingHandler.clearDefaultPacketListener();
    }

    @Override
    protected void startScan() {
        for (int i = 0x00; i <= 0xFF; i++) {
            VelbusScanPacket packet = new VelbusScanPacket((byte) i);
            byte[] packetBytes = packet.getBytes();

            thingHandler.sendPacket(packetBytes);
        }
    }

    @Override
    public boolean onPacketReceived(byte[] packet) {
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
                handleModuleSubtypeCommand(packet, address, 1);
            } else if (command == COMMAND_SUBTYPE_2 && packet.length >= 4 + length) {
                handleModuleSubtypeCommand(packet, address, 2);
            } else if (command == COMMAND_SUBTYPE_3 && packet.length >= 4 + length) {
                handleModuleSubtypeCommand(packet, address, 3);
            } else {
                logger.debug("Unknown command '{}' to address '{}'.", String.format("%02X", command),
                        String.format("%02X", address));
            }
        }
        return true;
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
            case MODULE_TYPE_VMB1BL:
                velbusModule = new VelbusModule(new VelbusFirstGenerationDeviceModuleAddress(address), moduleType,
                        highByteOfSerialNumber, lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek,
                        THING_TYPE_VMB1BL, 1);
                break;
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
            case MODULE_TYPE_VMB1RYS:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB1RYS, 5);
                break;
            case MODULE_TYPE_VMB1TS:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB1TS, 1);
                break;
            case MODULE_TYPE_VMB2BL:
                velbusModule = new VelbusModule(new VelbusFirstGenerationDeviceModuleAddress(address), moduleType,
                        highByteOfSerialNumber, lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek,
                        THING_TYPE_VMB2BL, 2);
                break;
            case MODULE_TYPE_VMB2BLE:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB2BLE, 2);
                break;
            case MODULE_TYPE_VMB2BLE_10:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB2BLE_10, 2);
                break;
            case MODULE_TYPE_VMB2PBN:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB2PBN, 8);
                break;
            case MODULE_TYPE_VMB4AN:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB4AN, 4);
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
            case MODULE_TYPE_VMB4RYLD_10:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB4RYLD_10, 5);
                break;
            case MODULE_TYPE_VMB4RYNO:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB4RYNO, 5);
                break;
            case MODULE_TYPE_VMB4RYNO_10:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB4RYNO_10, 5);
                break;
            case MODULE_TYPE_VMB6IN:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB6IN, 6);
                break;
            case MODULE_TYPE_VMB6PBN:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB6PBN, 8);
                break;
            case MODULE_TYPE_VMB6PB_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB6PB_20, 8);
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
            case MODULE_TYPE_VMBEL1:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBEL1, 9);
                break;
            case MODULE_TYPE_VMBEL1_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBEL1_20, 9);
                break;
            case MODULE_TYPE_VMBEL2:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBEL2, 9);
                break;
            case MODULE_TYPE_VMBEL2_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBEL2_20, 9);
                break;
            case MODULE_TYPE_VMBEL4:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBEL4, 9);
                break;
            case MODULE_TYPE_VMBEL4_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBEL4_20, 9);
                break;
            case MODULE_TYPE_VMBELO:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBELO, 33);
                break;
            case MODULE_TYPE_VMBELO_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBELO_20, 33);
                break;
            case MODULE_TYPE_VMBELPIR:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBELPIR, 9);
                break;
            case MODULE_TYPE_VMBEL4PIR_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBEL4PIR_20, 9);
                break;
            case MODULE_TYPE_VMBGP1:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP1, 9);
                break;
            case MODULE_TYPE_VMBGP1_2:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP1_2, 9);
                break;
            case MODULE_TYPE_VMBGP1_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP1_20, 9);
                break;
            case MODULE_TYPE_VMBGP2:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP2, 9);
                break;
            case MODULE_TYPE_VMBGP2_2:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP2_2, 9);
                break;
            case MODULE_TYPE_VMBGP2_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP2_20, 9);
                break;
            case MODULE_TYPE_VMBGP4:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP4, 9);
                break;
            case MODULE_TYPE_VMBGP4_2:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP4_2, 9);
                break;
            case MODULE_TYPE_VMBGP4_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP4_20, 9);
                break;
            case MODULE_TYPE_VMBGP4PIR:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP4PIR, 9);
                break;
            case MODULE_TYPE_VMBGP4PIR_2:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP4PIR_2, 9);
                break;
            case MODULE_TYPE_VMBGP4PIR_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 1), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGP4PIR_20, 9);
                break;
            case MODULE_TYPE_VMBGPO:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGPO, 33);
                break;
            case MODULE_TYPE_VMBGPOD:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGPOD, 33);
                break;
            case MODULE_TYPE_VMBGPOD_2:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGPOD_2, 33);
                break;
            case MODULE_TYPE_VMBGPO_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 4), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBGPO_20, 33);
                break;
            case MODULE_TYPE_VMBMETEO:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBMETEO, 13);
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
            case MODULE_TYPE_VMBRFR8S:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBRFR8S, 8);
                break;
            case MODULE_TYPE_VMBVP1:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBVP1, 8);
                break;
            case MODULE_TYPE_VMBKP:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBKP, 8);
                break;
            case MODULE_TYPE_VMBIN:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBIN, 8);
                break;
            case MODULE_TYPE_VMB4PB:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB4PB, 8);
                break;
            case MODULE_TYPE_VMBDALI:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 9), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBDALI, 81);
                break;
            case MODULE_TYPE_VMBDALI_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 9), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBDALI_20, 81);
                break;
            case MODULE_TYPE_VMB4LEDPWM_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB4LEDPWM_20, 4);
                break;
            case MODULE_TYPE_VMB8DC_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB8DC_20, 8);
                break;
            case MODULE_TYPE_VMBPIRO_10:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBPIRO_10, 9);
                break;
            case MODULE_TYPE_VMBPIR_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMBPIR_20, 7);
                break;
            case MODULE_TYPE_VMB8IN_20:
                velbusModule = new VelbusModule(new VelbusModuleAddress(address, 0), moduleType, highByteOfSerialNumber,
                        lowByteOfSerialNumber, memoryMapVersion, buildYear, buildWeek, THING_TYPE_VMB8IN_20, 8);
                break;
        }

        if (velbusModule != null) {
            registerVelbusModule(address, velbusModule);
        }
    }

    private void handleModuleSubtypeCommand(byte[] packet, byte address, int subTypeNumber) {
        if (velbusModules.containsKey(address)) {
            VelbusModule velbusModule = velbusModules.get(address);

            byte[] subAddresses = new byte[4];
            System.arraycopy(packet, 8, subAddresses, 0, 4);

            velbusModule.getModuleAddress().setSubAddresses(subAddresses, subTypeNumber);

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
        velbusModule.sendChannelNameRequests(thingHandler);
    }

    private void handleChannelNameCommand(byte[] packet, byte address, byte length, int namePartNumber) {
        if (velbusModules.containsKey(address)) {
            VelbusModule velbusModule = velbusModules.get(address);

            byte channel = packet[5];
            byte[] namePart = Arrays.copyOfRange(packet, 6, 6 + length - 2);

            VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address, channel);
            velbusModule.setChannelName(velbusChannelIdentifier, namePartNumber, namePart);
            notifyDiscoveredVelbusModule(velbusModule);
        }
    }

    private void notifyDiscoveredVelbusModule(VelbusModule velbusModule) {
        ThingUID bridgeUID = thingHandler.getThing().getUID();

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(velbusModule.getThingUID(bridgeUID))
                .withThingType(velbusModule.getThingTypeUID()).withProperties(velbusModule.getProperties())
                .withRepresentationProperty(ADDRESS).withBridge(bridgeUID).withLabel(velbusModule.getLabel()).build();

        thingDiscovered(discoveryResult);
    }

    @Override
    public void initialize() {
        thingHandler.setDefaultPacketListener(this);
        super.initialize();
    }
}
