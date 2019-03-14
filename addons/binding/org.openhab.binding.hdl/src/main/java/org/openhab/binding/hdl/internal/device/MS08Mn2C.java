/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hdl.internal.device;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.openhab.binding.hdl.internal.handler.HdlPacket;

/**
 * The MS08Mn2C class contains support channels for device Type MS08.
 * And how the information on the HDL bus is packet for this device.
 * This is a sensor with 8 functions.
 *
 * @author stigla - Initial contribution
 */
public class MS08Mn2C extends Device {
    private double temperatureValue;
    private double brightnessValue;
    private StopMoveType motionSensorValue = null;
    private OpenClosedType dryContact1Value = null;
    private OpenClosedType dryContact2Value = null;

    /** Device type for this sensor with 8 functions **/
    private DeviceType deviceType = DeviceType.MS08Mn_2C;

    public MS08Mn2C(DeviceConfiguration c) {
        super(c);
    }

    public void treatHDLPacketForDevice(HdlPacket p) {
        switch (p.commandType) {
            case Broadcast_Sensors_Status_Automatically:
                setTemperatureValue(p.data[0] - 20);
                setBrightnessValue(ushort(p.data[2], p.data[1]));
                if (p.data[3] == 1) {
                    setMotionSensorValue(StopMoveType.MOVE);
                } else {
                    setMotionSensorValue(StopMoveType.STOP);
                }
                if (p.data[4] == 1) {
                    setMotionSensorValue(StopMoveType.MOVE);
                } else {
                    setMotionSensorValue(StopMoveType.STOP);
                }
                if (p.data[5] == 1) {
                    setDryContact1Value(OpenClosedType.CLOSED);
                } else {
                    setDryContact1Value(OpenClosedType.OPEN);
                }
                if (p.data[6] == 1) {
                    setDryContact2Value(OpenClosedType.CLOSED);
                } else {
                    setDryContact2Value(OpenClosedType.OPEN);
                }
                break;
            case Response_Read_Sensors_Status:
                if (p.data[0] == -8) {
                    setTemperatureValue(p.data[1] - 20);
                    setBrightnessValue(ushort(p.data[2], p.data[3]));
                    if (p.data[4] == 1) {
                        setMotionSensorValue(StopMoveType.MOVE);
                    } else {
                        setMotionSensorValue(StopMoveType.STOP);
                    }
                    if (p.data[5] == 1) {
                        setDryContact1Value(OpenClosedType.CLOSED);
                    } else {
                        setDryContact1Value(OpenClosedType.OPEN);
                    }
                    if (p.data[6] == 1) {
                        setDryContact2Value(OpenClosedType.CLOSED);
                    } else {
                        setDryContact2Value(OpenClosedType.OPEN);
                    }
                }
                break;
            case Response_Auto_broadcast_Dry_Contact_Status:
                if (p.data[1] == 1) {
                    if (p.data[2] == 1) {
                        setDryContact1Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact1Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 2) {
                    if (p.data[2] == 1) {
                        setDryContact2Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact2Value(OpenClosedType.CLOSED);
                    }
                }
                break;
            case Response_Read_Dry_Contact_Status:
                if (p.data[1] == 1) {
                    if (p.data[2] == 1) {
                        setDryContact1Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact1Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 2) {
                    if (p.data[2] == 1) {
                        setDryContact2Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact2Value(OpenClosedType.CLOSED);
                    }
                }
                break;
            case Auto_broadcast_Dry_Contact_Status:
                if (p.data[1] == 1) {
                    if (p.data[2] == 1) {
                        setDryContact1Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact1Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 2) {
                    if (p.data[2] == 1) {
                        setDryContact2Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact2Value(OpenClosedType.CLOSED);
                    }
                }
            default:
                LOGGER.debug("For Device Type: {}, Unhandled CommandType: {}.", getType(), p.commandType);
                break;
        }
    }

    @Override
    public DeviceType getType() {
        return deviceType;
    }

    /**
     * Sets the DeviceType for this sensor.
     *
     * @param DeviceType as provided by the C message
     */
    void setType(DeviceType type) {
        this.deviceType = type;
    }

    /**
     * Sets the DryContact1Value sensor for 8in1 Sensor.
     *
     * @param OnOff Value of the DryContact1Value sensor
     */
    public void setDryContact1Value(OpenClosedType value) {
        if (this.dryContact1Value != value) {
            setUpdated(true);
        }
        this.dryContact1Value = value;
    }

    /**
     * the DryContact1Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact1Value() {
        return dryContact1Value;
    }

    /**
     * Sets the DryContact2Value sensor for 8in1 Sensor.
     *
     * @param OnOff Value of the DryContact2Value sensor
     */
    public void setDryContact2Value(OpenClosedType value) {
        if (this.dryContact2Value != value) {
            setUpdated(true);
        }
        this.dryContact2Value = value;
    }

    /**
     * the DryContact2Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact2Value() {
        return dryContact2Value;
    }

    /**
     * Sets the Motion sensor for 8in1 Sensor.
     *
     * @param StopMoveType Value of the Motion sensor
     */
    public void setMotionSensorValue(StopMoveType value) {
        if (this.motionSensorValue != value) {
            setUpdated(true);
        }
        this.motionSensorValue = value;
    }

    /**
     * the Motion sensor Value as <code>StopMoveType</code>
     */
    public StopMoveType getMotionSensorValue() {
        return motionSensorValue;
    }

    /**
     * Sets the Brightness Value for this 8in1 Sensor.
     *
     * @param value the Brightness value as provided
     */
    public void setBrightnessValue(double value) {
        if (this.brightnessValue != value) {
            setUpdated(true);
        }
        this.brightnessValue = value;
    }

    /**
     * the BrightnessHighValue as <code>DecimalType</code>
     */
    public DecimalType getBrightnessValue() {
        BigDecimal brightnessValue = BigDecimal.valueOf(this.brightnessValue).setScale(1, RoundingMode.HALF_UP);
        return new DecimalType(brightnessValue);
    }

    /**
     * Sets the temperature for this 8in1 sensor.
     *
     * @param value the actual temperature raw value as provided by the L message
     */
    public void setTemperatureValue(double value) {
        if (this.temperatureValue != value) {
            setUpdated(true);
        }
        this.temperatureValue = value;
    }

    /**
     * Returns the measured temperature of this sensor.
     * 0�C is displayed if no actual is measured.
     *
     * @return
     *         the actual temperature as <code>DecimalType</code>
     */
    public DecimalType getTemperatureValue() {
        BigDecimal temperatureValue = BigDecimal.valueOf(this.temperatureValue);// .setScale(1, RoundingMode.HALF_UP);
        return new DecimalType(temperatureValue);
    }
}
