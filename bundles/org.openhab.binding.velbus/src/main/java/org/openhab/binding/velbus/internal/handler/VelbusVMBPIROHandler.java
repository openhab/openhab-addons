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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.measure.quantity.Illuminance;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.packets.VelbusLightValueRequestPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusVMBPIROHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusVMBPIROHandler extends VelbusTemperatureSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMBPIRO));

    private ChannelUID illuminanceChannel;

    public VelbusVMBPIROHandler(Thing thing) {
        super(thing, 0, new ChannelUID(thing.getUID(), "input", "CH9"));

        this.illuminanceChannel = new ChannelUID(thing.getUID(), "input", "LIGHT");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (command instanceof RefreshType) {
            if (channelUID.equals(illuminanceChannel)) {
                VelbusLightValueRequestPacket packet = new VelbusLightValueRequestPacket(
                        getModuleAddress().getAddress());

                byte[] packetBytes = packet.getBytes();
                velbusBridgeHandler.sendPacket(packetBytes);
            }
        }
    }

    @Override
    public boolean onPacketReceived(byte[] packet) {
        if (!super.onPacketReceived(packet)) {
            return false;
        }

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte command = packet[4];

            if (command == COMMAND_MODULE_STATUS && packet.length >= 6) {
                byte highByteCurrentLightValue = packet[6];
                byte lowByteCurrentLightValue = packet[7];

                double lightValue = (((highByteCurrentLightValue & 0xff) << 8) + (lowByteCurrentLightValue & 0xff));
                QuantityType<Illuminance> lightValueState = new QuantityType<>(lightValue, Units.LUX);
                updateState(illuminanceChannel, lightValueState);
            }
        }

        return true;
    }
}
