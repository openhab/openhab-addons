/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hapero.internal.handler;

import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hapero.internal.HaperoBindingConstants;
import org.openhab.binding.hapero.internal.device.Device;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link HaperoFurnaceHandler} handles the Furnace Thing
 * It parses the A,B,D and E Blocks of the Data Stream for Channel data
 * and the SI Block for Property values
 *
 * @author Daniel Walter - Initial contribution
 */
@NonNullByDefault
public class HaperoFurnaceHandler extends HaperoThingHandler {

    /* Data indices in the data stream block for the channel data */
    // Block A Indices
    private static final int CHANNEL_COMBUSTIONTEMPERATURE_INDEX = 0;
    private static final int CHANNEL_PELLETCHANNELTEMPERATURE_INDEX = 1;
    private static final int CHANNEL_BOILERTEMPERATURE_INDEX = 2;
    private static final int CHANNEL_OUTSIDETEMPERATURE_INDEX = 4;
    // Block B Indices
    private static final int CHANNEL_FURNACESTATUS_INDEX = 0;
    private static final int CHANNEL_BURNERSTATUS_INDEX = 1;
    private static final int CHANNEL_MATERIALSTATUS_INDEX = 2;
    private static final int CHANNEL_AIRSTATUS_INDEX = 3;
    private static final int CHANNEL_GRATESTATUS_INDEX = 4;
    private static final int CHANNEL_ERRORSTATUS_INDEX = 5;
    private static final int CHANNEL_MULTIFUNCTIONMOTORMODE_INDEX = 6;
    private static final int CHANNEL_MULTIFUNCTIONMOTORSTATUS_INDEX = 7;
    // Block D Indices
    private static final int CHANNEL_FURNACEPOWER_INDEX = 0;
    private static final int CHANNEL_BOILERTEMPERATURESET_INDEX = 1;
    // Block E Indices
    private static final int CHANNEL_FURNACEAIRFLOW_INDEX = 0;
    private static final int CHANNEL_FURNACEAIRFLOWSET_INDEX = 1;
    private static final int CHANNEL_FURNACEAIRPOWER_INDEX = 2;
    private static final int CHANNEL_FURNACEAIRDRIVE_INDEX = 3;
    private static final int CHANNEL_FURNACEAIRO2_INDEX = 4;
    // Block SI Indices
    private static final int PROPERTY_SERIAL_INDEX = 0;
    private static final int PROPERTY_DSIPLAYSERIAL_INDEX = 1;
    private static final int PROPERTY_PROGRAMVERSION_INDEX = 2;
    private static final int PROPERTY_OSVERSION_INDEX = 4;

    /* Some temperature values are scaled down by 10 */
    private static final int VALUE_SCALE_10 = 10;

    /**
     * Constructor
     *
     * @param thing
     */
    public HaperoFurnaceHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable State getStateForChannel(Channel channel, Device device) {
        State state = null;
        Device newDevice = null;
        Float value;
        String stringValue;
        Integer intValue;
        String channelId = channel.getUID().getIdWithoutGroup();
        HaperoBridgeHandler bridgeHandler = null;
        Bridge bridge = getBridge();

        if (bridge != null) {
            bridgeHandler = (HaperoBridgeHandler) bridge.getHandler();
        }

        if (bridgeHandler != null) {
            switch (channelId) {
                case HaperoBindingConstants.CHANNEL_COMBUSTIONTEMPERATURE:
                    newDevice = bridgeHandler.getDevice("A");

                    if (newDevice != null) {
                        value = newDevice.getFloat(CHANNEL_COMBUSTIONTEMPERATURE_INDEX);
                        if (value != null) {
                            state = new QuantityType<Temperature>(value, SIUnits.CELSIUS);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_PELLETCHANNELTEMPERATURE:
                    newDevice = bridgeHandler.getDevice("A");

                    if (newDevice != null) {
                        value = newDevice.getFloat(CHANNEL_PELLETCHANNELTEMPERATURE_INDEX);
                        if (value != null) {
                            state = new QuantityType<Temperature>(value, SIUnits.CELSIUS);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_BOILERTEMPERATURE:
                    newDevice = bridgeHandler.getDevice("A");

                    if (newDevice != null) {
                        value = newDevice.getFloat(CHANNEL_BOILERTEMPERATURE_INDEX);
                        if (value != null) {
                            value /= VALUE_SCALE_10;
                            state = new QuantityType<Temperature>(value, SIUnits.CELSIUS);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_OUTSIDETEMPERATURE:
                    newDevice = bridgeHandler.getDevice("A");

                    if (newDevice != null) {
                        value = newDevice.getFloat(CHANNEL_OUTSIDETEMPERATURE_INDEX);
                        if (value != null) {
                            value /= VALUE_SCALE_10;
                            state = new QuantityType<Temperature>(value, SIUnits.CELSIUS);
                        }
                    }
                    break;

                case HaperoBindingConstants.CHANNEL_FURNACESTATUS:
                    newDevice = bridgeHandler.getDevice("B");

                    if (newDevice != null) {
                        stringValue = newDevice.getString(CHANNEL_FURNACESTATUS_INDEX);
                        if (stringValue != null) {
                            state = new StringType(stringValue);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_BURNERSTATUS:
                    newDevice = bridgeHandler.getDevice("B");

                    if (newDevice != null) {
                        stringValue = newDevice.getString(CHANNEL_BURNERSTATUS_INDEX);
                        if (stringValue != null) {
                            state = new StringType(stringValue);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_MATERIALSTATUS:
                    newDevice = bridgeHandler.getDevice("B");

                    if (newDevice != null) {
                        stringValue = newDevice.getString(CHANNEL_MATERIALSTATUS_INDEX);
                        if (stringValue != null) {
                            state = new StringType(stringValue);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_AIRSTATUS:
                    newDevice = bridgeHandler.getDevice("B");

                    if (newDevice != null) {
                        stringValue = newDevice.getString(CHANNEL_AIRSTATUS_INDEX);
                        if (stringValue != null) {
                            state = new StringType(stringValue);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_GRATESTATUS:
                    newDevice = bridgeHandler.getDevice("B");

                    if (newDevice != null) {
                        stringValue = newDevice.getString(CHANNEL_GRATESTATUS_INDEX);
                        if (stringValue != null) {
                            state = new StringType(stringValue);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_ERRORSTATUS:
                    newDevice = bridgeHandler.getDevice("B");

                    if (newDevice != null) {
                        stringValue = newDevice.getString(CHANNEL_ERRORSTATUS_INDEX);
                        if (stringValue != null) {
                            state = new StringType(stringValue);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_MULTIFUNCTIONMOTORMODE:
                    newDevice = bridgeHandler.getDevice("B");

                    if (newDevice != null) {
                        stringValue = newDevice.getString(CHANNEL_MULTIFUNCTIONMOTORMODE_INDEX);
                        if (stringValue != null) {
                            state = new StringType(stringValue);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_MULTIFUNCTIONMOTORSTATUS:
                    newDevice = bridgeHandler.getDevice("B");

                    if (newDevice != null) {
                        stringValue = newDevice.getString(CHANNEL_MULTIFUNCTIONMOTORSTATUS_INDEX);
                        if (stringValue != null) {
                            state = new StringType(stringValue);
                        }
                    }
                    break;

                case HaperoBindingConstants.CHANNEL_FURNACEPOWER:
                    newDevice = bridgeHandler.getDevice("D");

                    if (newDevice != null) {
                        value = newDevice.getFloat(CHANNEL_FURNACEPOWER_INDEX);
                        if (value != null) {
                            value /= VALUE_SCALE_10;
                            state = new QuantityType<Dimensionless>(value, Units.PERCENT);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_BOILERTEMPERATURESET:
                    newDevice = bridgeHandler.getDevice("D");

                    if (newDevice != null) {
                        value = newDevice.getFloat(CHANNEL_BOILERTEMPERATURESET_INDEX);
                        if (value != null) {
                            value /= VALUE_SCALE_10;
                            state = new QuantityType<Temperature>(value, SIUnits.CELSIUS);
                        }
                    }
                    break;

                case HaperoBindingConstants.CHANNEL_FURNACEAIRFLOW:
                    newDevice = bridgeHandler.getDevice("E");

                    if (newDevice != null) {
                        intValue = newDevice.getInteger(CHANNEL_FURNACEAIRFLOW_INDEX);
                        if (intValue != null) {
                            state = new DecimalType(intValue);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_FURNACEAIRFLOWSET:
                    newDevice = bridgeHandler.getDevice("E");

                    if (newDevice != null) {
                        intValue = newDevice.getInteger(CHANNEL_FURNACEAIRFLOWSET_INDEX);
                        if (intValue != null) {
                            state = new DecimalType(intValue);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_FURNACEAIRPOWER:
                    newDevice = bridgeHandler.getDevice("E");

                    if (newDevice != null) {
                        value = newDevice.getFloat(CHANNEL_FURNACEAIRPOWER_INDEX);
                        if (value != null) {
                            value /= VALUE_SCALE_10;
                            state = new QuantityType<Dimensionless>(value, Units.PERCENT);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_FURNACEAIRDRIVE:
                    newDevice = bridgeHandler.getDevice("E");

                    if (newDevice != null) {
                        intValue = newDevice.getInteger(CHANNEL_FURNACEAIRDRIVE_INDEX);
                        if (intValue != null) {
                            state = new DecimalType(intValue);
                        }
                    }
                    break;
                case HaperoBindingConstants.CHANNEL_FURNACEAIRO2:
                    newDevice = bridgeHandler.getDevice("E");

                    if (newDevice != null) {
                        value = newDevice.getFloat(CHANNEL_FURNACEAIRO2_INDEX);
                        if (value != null) {
                            value /= VALUE_SCALE_10;
                            state = new QuantityType<Dimensionless>(value, Units.PERCENT);
                        }
                    }
                    break;

                default:
                    logger.warn("Unknown channel requested for furnace: {}", channelId);
                    return null;
            }
        }

        if (state == null) {
            logger.warn("Could not update furnace channel {}.", channelId);
        }

        return state;
    }

    @Override
    public void updateThingProperties() {
        Device device = null;
        HaperoBridgeHandler bridgeHandler = null;
        Bridge bridge = getBridge();
        String data;
        Map<String, String> properties = editProperties();

        if (bridge != null) {
            bridgeHandler = (HaperoBridgeHandler) bridge.getHandler();
        }

        if (bridgeHandler != null) {
            device = bridgeHandler.getDevice("SI");

            if (device != null) {
                data = device.getString(PROPERTY_SERIAL_INDEX);
                if (data != null) {
                    properties.put(Thing.PROPERTY_SERIAL_NUMBER, data);
                }

                data = device.getString(PROPERTY_DSIPLAYSERIAL_INDEX);
                if (data != null) {
                    properties.put("displaySerialNumber", data);
                }

                data = device.getString(PROPERTY_PROGRAMVERSION_INDEX);
                if (data != null) {
                    properties.put("programVersion", data);
                }

                data = device.getString(PROPERTY_OSVERSION_INDEX);
                if (data != null) {
                    properties.put("osVersion", data);
                }

                updateProperties(properties);
            }
        }

        super.updateThingProperties();
    }
}
