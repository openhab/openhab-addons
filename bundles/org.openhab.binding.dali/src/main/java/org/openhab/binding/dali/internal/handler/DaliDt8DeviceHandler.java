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
package org.openhab.binding.dali.internal.handler;

import static org.openhab.binding.dali.internal.DaliBindingConstants.CHANNEL_COLOR;
import static org.openhab.binding.dali.internal.DaliBindingConstants.CHANNEL_COLOR_TEMPERATURE;
import static org.openhab.binding.dali.internal.DaliBindingConstants.THING_TYPE_DEVICE_DT8;
import static org.openhab.binding.dali.internal.DaliBindingConstants.THING_TYPE_GROUP_DT8;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dali.internal.protocol.DaliAddress;
import org.openhab.binding.dali.internal.protocol.DaliResponse;
import org.openhab.binding.dali.internal.protocol.DaliResponse.NumericMask;
import org.openhab.binding.dali.internal.protocol.DaliStandardCommand;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DaliDeviceHandler} handles commands for things of type Device and Group.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliDt8DeviceHandler extends DaliDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(DaliDt8DeviceHandler.class);

    public DaliDt8DeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            final DaliserverBridgeHandler daliHandler = getBridgeHandler();
            if (CHANNEL_COLOR_TEMPERATURE.equals(channelUID.getId())) {
                DaliAddress address;
                if (THING_TYPE_DEVICE_DT8.equals(this.thing.getThingTypeUID())) {
                    address = DaliAddress.createShortAddress(targetId);
                } else if (THING_TYPE_GROUP_DT8.equals(this.thing.getThingTypeUID())) {
                    address = DaliAddress.createGroupAddress(targetId);
                } else {
                    throw new DaliException("unknown device type");
                }
                if (command instanceof DecimalType) {
                    // Color temperature in DALI is represented in mirek ("reciprocal megakelvin")
                    // It is one million times the reciprocal of the color temperature (in Kelvin)
                    final int mirek = (int) (1E6f
                            / (Math.min(Math.max(((DecimalType) command).intValue(), 1000), 20000)));
                    final byte mirekLsb = (byte) (mirek & 0xff);
                    final byte mirekMsb = (byte) ((mirek >> 8) & 0xff);
                    // Write mirek value to the DTR0+DTR1 registers
                    daliHandler.sendCommand(DaliStandardCommand.createSetDTR0Command(mirekLsb));
                    daliHandler.sendCommand(DaliStandardCommand.createSetDTR1Command(mirekMsb));
                    // Indicate that the follwing command is a DT8 (WW/CW and single-channel RGB) command
                    daliHandler.sendCommand(DaliStandardCommand.createSetDeviceTypeCommand(8));
                    // Set the color temperature to the value in DTR0+DTR1
                    daliHandler.sendCommand(DaliStandardCommand.createSetColorTemperatureCommand(address));
                    // Finish the command sequence
                    daliHandler.sendCommand(DaliStandardCommand.createSetDeviceTypeCommand(8));
                    daliHandler.sendCommand(DaliStandardCommand.createActivateCommand(address));

                }

                DaliAddress readAddress = address;
                if (readDeviceTargetId != null) {
                    readAddress = DaliAddress.createShortAddress(readDeviceTargetId);
                }
                // Write argument 0xc2 (query temporary color temperature) to DTR0 and set DT8
                daliHandler.sendCommand(DaliStandardCommand.createSetDTR0Command(0xc2));
                daliHandler.sendCommand(DaliStandardCommand.createSetDeviceTypeCommand(8));
                // Mirek MSB is returned as result
                CompletableFuture<@Nullable NumericMask> responseMsb = daliHandler.sendCommandWithResponse(
                        DaliStandardCommand.createQueryColorValueCommand(readAddress), DaliResponse.NumericMask.class);
                daliHandler.sendCommand(DaliStandardCommand.createSetDeviceTypeCommand(8));
                // Mirek LSB is written to DTR0
                CompletableFuture<@Nullable NumericMask> responseLsb = daliHandler.sendCommandWithResponse(
                        DaliStandardCommand.createQueryContentDTR0Command(readAddress), DaliResponse.NumericMask.class);

                CompletableFuture.allOf(responseMsb, responseLsb).thenAccept(x -> {
                    @Nullable
                    NumericMask msb = responseMsb.join(), lsb = responseLsb.join();
                    if (msb != null && !msb.mask && lsb != null && !lsb.mask) {
                        final int msbValue = msb.value != null ? msb.value : 0;
                        final int lsbValue = lsb.value != null ? lsb.value : 0;
                        final int mirek = ((msbValue & 0xff) << 8) | (lsbValue & 0xff);
                        final int kelvin = (int) (1E6f / mirek);
                        updateState(channelUID, new DecimalType(kelvin));
                    }
                }).exceptionally(e -> {
                    logger.warn("Error querying device status: {}", e.getMessage());
                    return null;
                });

            } else if (CHANNEL_COLOR.equals(channelUID.getId())) {
                DaliAddress address;
                if (THING_TYPE_DEVICE_DT8.equals(this.thing.getThingTypeUID())) {
                    address = DaliAddress.createShortAddress(targetId);
                } else if (THING_TYPE_GROUP_DT8.equals(this.thing.getThingTypeUID())) {
                    address = DaliAddress.createGroupAddress(targetId);
                } else {
                    throw new DaliException("unknown device type");
                }
                if (command instanceof HSBType) {
                    PercentType[] rgb = ((HSBType) command).toRGB();
                    final int r = (int) (254 * (rgb[0].floatValue() / 100));
                    final int g = (int) (254 * (rgb[1].floatValue() / 100));
                    final int b = (int) (254 * (rgb[2].floatValue() / 100));
                    logger.trace("RGB: {} {} {}", r, g, b);
                    // Write RGB values to the DTR0+DTR1+DTR2 registers
                    daliHandler.sendCommand(DaliStandardCommand.createSetDTR0Command(r));
                    daliHandler.sendCommand(DaliStandardCommand.createSetDTR1Command(g));
                    daliHandler.sendCommand(DaliStandardCommand.createSetDTR2Command(b));
                    // Indicate that the following command is a DT8 (WW/CW and single-channel RGB) command
                    daliHandler.sendCommand(DaliStandardCommand.createSetDeviceTypeCommand(8));
                    // Set the color to the values in DTR0+DTR1+DTR2
                    daliHandler.sendCommand(DaliStandardCommand.createSetRgbDimlevelCommand(address));
                    // Finish the command sequence
                    daliHandler.sendCommand(DaliStandardCommand.createSetDeviceTypeCommand(8));
                    daliHandler.sendCommand(DaliStandardCommand.createActivateCommand(address));
                }

                DaliAddress readAddress = address;
                if (readDeviceTargetId != null) {
                    readAddress = DaliAddress.createShortAddress(readDeviceTargetId);
                }
                // Write argument 0xE9 (query red dimlevel) to DTR0 and set DT8
                daliHandler.sendCommand(DaliStandardCommand.createSetDTR0Command(0xe9));
                daliHandler.sendCommand(DaliStandardCommand.createSetDeviceTypeCommand(8));
                // Red component is returned as result
                CompletableFuture<@Nullable NumericMask> responseRed = daliHandler.sendCommandWithResponse(
                        DaliStandardCommand.createQueryColorValueCommand(readAddress), DaliResponse.NumericMask.class);
                // Write argument 0xEA (query green dimlevel) to DTR0 and set DT8
                daliHandler.sendCommand(DaliStandardCommand.createSetDTR0Command(0xea));
                daliHandler.sendCommand(DaliStandardCommand.createSetDeviceTypeCommand(8));
                // Green component is returned as result
                CompletableFuture<@Nullable NumericMask> responseGreen = daliHandler.sendCommandWithResponse(
                        DaliStandardCommand.createQueryColorValueCommand(readAddress), DaliResponse.NumericMask.class);
                // Write argument 0xEB (query blue dimlevel) to DTR0 and set DT8
                daliHandler.sendCommand(DaliStandardCommand.createSetDTR0Command(0xeb));
                daliHandler.sendCommand(DaliStandardCommand.createSetDeviceTypeCommand(8));
                // Blue component is returned as result
                CompletableFuture<@Nullable NumericMask> responseBlue = daliHandler.sendCommandWithResponse(
                        DaliStandardCommand.createQueryColorValueCommand(readAddress), DaliResponse.NumericMask.class);

                CompletableFuture.allOf(responseRed, responseGreen, responseBlue).thenAccept(x -> {
                    @Nullable
                    NumericMask r = responseRed.join(), g = responseGreen.join(), b = responseBlue.join();
                    if (r != null && !r.mask && g != null && !g.mask && b != null && !b.mask) {
                        final int rValue = r.value != null ? r.value : 0;
                        final int gValue = g.value != null ? g.value : 0;
                        final int bValue = b.value != null ? b.value : 0;
                        updateState(channelUID, HSBType.fromRGB(rValue, gValue, bValue));
                    }
                }).exceptionally(e -> {
                    logger.warn("Error querying device status: {}", e.getMessage());
                    return null;
                });

            } else {
                super.handleCommand(channelUID, command);
            }
        } catch (DaliException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
