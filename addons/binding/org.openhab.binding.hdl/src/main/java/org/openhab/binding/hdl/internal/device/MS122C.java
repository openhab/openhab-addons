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
import java.util.Date;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.openhab.binding.hdl.internal.handler.HdlPacket;

/**
 * The MS122C class contains support channels for device Type MS12.
 * And how the information on the HDL bus is packet for this device.
 * This is a sensor with 12 functions.
 *
 * @author stigla - Initial contribution
 */
public class MS122C extends Device {
    private double temperatureValue;
    private double brightnessValue;
    private StopMoveType motionSensorValue = null;
    private StopMoveType sonicValue = null;
    private OpenClosedType dryContact1Value = null;
    private OpenClosedType dryContact2Value = null;
    private OnOffType relayCh01 = null;
    private OnOffType relayCh02 = null;

    /** Date setpoint until the temperature setpoint is valid */
    private Date dateSetpoint;

    /** Device type for this Sensor with 12 functions **/
    private DeviceType deviceType = DeviceType.MS12_2C;

    public MS122C(DeviceConfiguration c) {
        super(c);
    }

    @Override
    public DeviceType getType() {
        return deviceType;
    }

    /**
     * Sets the DeviceType for this thermostat.
     */
    void setType(DeviceType type) {
        this.deviceType = type;
    }

    public void setDateSetpoint(Date date) {
        this.dateSetpoint = date;
    }

    public Date getDateSetpoint() {
        return dateSetpoint;
    }

    public void treatHDLPacketForDevice(HdlPacket p) {
        switch (p.commandType) {
            case Broadcast_Sensors_Status_Automatically:
                setTemperatureValue(p.data[0] - 20.0);
                setBrightnessValue(ushort(p.data[2], p.data[1]));

                if (p.data[3] == 1) {
                    setMotionSensorValue(StopMoveType.MOVE);
                } else {
                    setMotionSensorValue(StopMoveType.STOP);
                }
                if (p.data[4] == 1) {
                    setSonicValue(StopMoveType.MOVE);
                } else {
                    setSonicValue(StopMoveType.STOP);
                }
                if (p.data[5] == 1) {
                    setDryContact1Value(OpenClosedType.OPEN);
                } else {
                    setDryContact1Value(OpenClosedType.CLOSED);
                }
                if (p.data[6] == 1) {
                    setDryContact2Value(OpenClosedType.OPEN);
                } else {
                    setDryContact2Value(OpenClosedType.CLOSED);
                }
                break;
            case Response_Read_Sensors_Status:
                if (p.data[0] == -8) {
                    setTemperatureValue(p.data[1] - 20.0);
                    setBrightnessValue(ushort(p.data[2], p.data[3]));
                    if (p.data[4] == 1) {
                        setMotionSensorValue(StopMoveType.MOVE);
                    } else {
                        setMotionSensorValue(StopMoveType.STOP);
                    }
                    if (p.data[5] == 1) {
                        setSonicValue(StopMoveType.MOVE);
                    } else {
                        setSonicValue(StopMoveType.STOP);
                    }
                    if (p.data[6] == 1) {
                        setDryContact1Value(OpenClosedType.CLOSED);
                    } else {
                        setDryContact1Value(OpenClosedType.OPEN);
                    }
                    if (p.data[7] == 1) {
                        setDryContact2Value(OpenClosedType.CLOSED);
                    } else {
                        setDryContact2Value(OpenClosedType.OPEN);
                    }
                }
                break;
            case Broadcast_Temperature:
                String InToHex = String.format("%02X", p.data[5]) + String.format("%02X", p.data[4])
                        + String.format("%02X", p.data[3]) + String.format("%02X", p.data[2]);

                Long i = Long.valueOf(InToHex, 16);
                Float tempfloat = Float.intBitsToFloat(i.intValue());
                setTemperatureValue(tempfloat);
                break;
            case Response_Single_Channel_Control:
                if ((p.data[0]) == 1) {
                    if ((p.data[2]) == 100) {
                        setRelayCh01(OnOffType.ON);
                    } else {
                        setRelayCh01(OnOffType.OFF);
                    }
                }
                if ((p.data[0]) == 2) {
                    if ((p.data[2]) == 100) {
                        setRelayCh02(OnOffType.ON);
                    } else {
                        setRelayCh02(OnOffType.OFF);
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
                break;
            default:
                LOGGER.debug("For Device Type: {}, Unhandled CommandType: {}.", getType(), p.commandType);
                break;
        }
        return;
    }

    public void setRelayCh01(OnOffType RelayCh01) {
        if (this.relayCh01 != RelayCh01) {
            setUpdated(true);
        }
        this.relayCh01 = RelayCh01;
    }

    public OnOffType getRelayCh01State() {
        return relayCh01;
    }

    public void setRelayCh02(OnOffType RelayCh02) {
        if (this.relayCh02 != RelayCh02) {
            setUpdated(true);
        }
        this.relayCh02 = RelayCh02;
    }

    public OnOffType getRelayCh02State() {
        return relayCh02;
    }

    /**
     * Sets the DryContact1Value sensor for 12in1 Sensor.
     *
     * @param OpenClosedType Value of the DryContact1Value
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
     * Sets the Sonic sensor for 12in1 Sensor.
     *
     * @param OnOff Value of the Sonic sensor
     */
    public void setSonicValue(StopMoveType value) {
        if (this.sonicValue != value) {
            setUpdated(true);
        }
        this.sonicValue = value;
    }

    /**
     * Sets the DryContact2Value sensor for 12in1 Sensor.
     *
     * @param OpenClosedType Value of the DryContact1Value
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
     * the Sonic sensor Value as <code>OnOffType</code>
     */
    public StopMoveType getSonicValue() {
        return sonicValue;
    }

    /**
     * Sets the Motion sensor for 12in1 Sensor.
     *
     * @param OnOff Value of the Motion sensor
     */
    public void setMotionSensorValue(StopMoveType value) {
        if (this.motionSensorValue != value) {
            setUpdated(true);
        }
        this.motionSensorValue = value;
    }

    /**
     * the Motion sensor Value as <code>OnOffType</code>
     */
    public StopMoveType getMotionSensorValue() {
        return motionSensorValue;
    }

    /**
     * Sets the Brightness Value for this Sensor.
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
     * the Brightness Value as <code>DecimalType</code>
     */

    public DecimalType getBrightnessValue() {
        BigDecimal brightnessValue = BigDecimal.valueOf(this.brightnessValue).setScale(1, RoundingMode.HALF_UP);
        return new DecimalType(brightnessValue);
    }

    /**
     * Sets the actual temperature for this 12in1 sensor.
     *
     * @param value the actual temperature value as provided
     */
    public void setTemperatureValue(double value) {
        if (this.temperatureValue != value) {
            setUpdated(true);
        }
        this.temperatureValue = value;
    }

    /**
     * the Temperature as <code>DecimalType</code>
     */
    public DecimalType getTemperatureValue() {
        BigDecimal temperatureValue = BigDecimal.valueOf(this.temperatureValue);// .setScale(1, RoundingMode.HALF_UP);
        return new DecimalType(temperatureValue);
    }

}
