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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.hdl.HdlBindingConstants.emumFHMode;
import org.openhab.binding.hdl.internal.handler.HdlPacket;

/**
 * The MPL8_48_FH class contains support channels for device Type MPL8.
 * And how the information on the HDL bus is packet for this device.
 * This is a button panel which contains temperature measurement.
 *
 * @author stigla - Initial contribution
 */
public class MPL848FH extends Device {
    private double temperatureValue;
    private OnOffType uvSwitch1 = null; // Status On/OFf
    private OnOffType uvSwitch2 = null; // Normal Mode
    private OnOffType uvSwitch3 = null; // Day Mode
    private OnOffType uvSwitch4 = null; // Night Mode
    private OnOffType uvSwitch5 = null; // Away Mode
    private OnOffType uvSwitch6 = null; // Timer Mode

    // Floor Heating
    private String floorHeatingTemperaturType;
    private double floorHeatingCurrentTemperatur;
    private OnOffType floorHeatingStatus = null;
    private emumFHMode floorHeatingMode;
    private double floorHeatingSetNormalTemperatur;
    private double floorHeatingSetDayTemperatur;
    private double floorHeatingSetNightTemperatur;
    private double floorHeatingSetAwayTemperatur;
    private String floorHeatingTimer;

    // AC
    private String acFanSpeed;
    private String acMode;
    private double acCoolingTemp;
    private double acHeatTemp;
    private double acAutoTemp;
    private double acDryTemp;
    private double acCurrentTemp;

    /** Device type for this Button Panel (DLP) with AC, Music, Clock, Floor Heating **/
    private DeviceType deviceType = DeviceType.MPL8_48_FH;

    public MPL848FH(DeviceConfiguration c) {
        super(c);
    }

    public void treatHDLPacketForDevice(HdlPacket p) {
        switch (p.commandType) {
            case Broadcast_Temperature:
                String InToHex = String.format("%02X", p.data[5]) + String.format("%02X", p.data[4])
                        + String.format("%02X", p.data[3]) + String.format("%02X", p.data[2]);

                Long i = Long.valueOf(InToHex, 16);
                Float tempfloat = Float.intBitsToFloat(i.intValue());
                setTemperatureValue(tempfloat);
                break;
            case Response_Panel_Control:
                switch (p.data[0]) {
                    case (byte) 4:
                        setACCoolingTemperatur(p.data[1]);
                        break;
                    case (byte) 5:
                        if (p.data[1] == 0) {
                            setACFanSpeed("Auto");
                        } else if (p.data[1] == 1) {
                            setACFanSpeed("High");
                        } else if (p.data[1] == 2) {
                            setACFanSpeed("Medium");
                        } else if (p.data[1] == 3) {
                            setACFanSpeed("Low");
                        }
                        break;
                    case (byte) 6:
                        if (p.data[1] == 0) {
                            setACMode("Cooling");
                        } else if (p.data[1] == 1) {
                            setACMode("Heating,");
                        } else if (p.data[1] == 2) {
                            setACMode("Fan");
                        } else if (p.data[1] == 3) {
                            setACMode("Auto");
                        } else if (p.data[1] == 4) {
                            setACMode("Dehumidfy");
                        }
                        break;
                    case (byte) 7:
                        setACHeatTemperatur(p.data[1]);
                        break;
                    case (byte) 8:
                        setACAutoTemperatur(p.data[1]);
                        break;

                    case (byte) 19:
                        setACDryTemperatur(p.data[1]);
                        break;
                    case (byte) 20:
                        if (p.data[1] == 1) {
                            setFloorHeatingStatus(OnOffType.ON);
                        } else {
                            setFloorHeatingStatus(OnOffType.OFF);
                        }
                        break;
                    case (byte) 21:
                        if (p.data[1] == 1) {
                            setFloorHeatingMode(emumFHMode.Normal);
                        } else if (p.data[1] == 2) {
                            setFloorHeatingMode(emumFHMode.Day);
                        } else if (p.data[1] == 3) {
                            setFloorHeatingMode(emumFHMode.Night);
                        } else if (p.data[1] == 4) {
                            setFloorHeatingMode(emumFHMode.Away);
                        } else if (p.data[1] == 5) {
                            setFloorHeatingMode(emumFHMode.Timer);
                        }
                        break;
                    case (byte) 25:
                        setFloorHeatingSetNormalTemperatur(p.data[1]);
                        break;
                    case (byte) 26:
                        setFloorHeatingSetDayTemperatur(p.data[1]);
                        break;
                    case (byte) 27:
                        setFloorHeatingSetNightTemperatur(p.data[1]);
                        break;
                    case (byte) 28:
                        setFloorHeatingSetAwayTemperatur(p.data[1]);
                        break;
                    case (byte) 29:
                        // Navigation in panel.
                        break;
                    default:
                        LOGGER.debug("For type: {}, Unhandled Byte in Response Panel Control: {}.", p.sourcedeviceType,
                                p.data[0]);
                        break;
                }

                break;
            case Response_Read_Floor_Heating_Status_DLP:
                if (p.data[0] == 1) {
                    setFloorHeatingTemperaturType("F");
                } else {
                    setFloorHeatingTemperaturType("C");
                }

                if (p.data[2] == 1) {
                    setFloorHeatingStatus(OnOffType.ON);
                } else {
                    setFloorHeatingStatus(OnOffType.OFF);
                }

                if (p.data[3] == 1) {
                    setFloorHeatingMode(emumFHMode.Normal);
                } else if (p.data[3] == 2) {
                    setFloorHeatingMode(emumFHMode.Day);
                } else if (p.data[3] == 3) {
                    setFloorHeatingMode(emumFHMode.Night);
                } else if (p.data[3] == 4) {
                    setFloorHeatingMode(emumFHMode.Away);
                } else if (p.data[3] == 5) {
                    setFloorHeatingMode(emumFHMode.Timer);
                }

                if (p.data[8] == 1) {
                    setFloorHeatingTimer("Night");
                } else {
                    setFloorHeatingTimer("Day");
                }

                // This has to be done last so Current Temperature can be set correctly, since current temperature needs
                // to know what FloorHeatingMode DLP is in.
                setFloorHeatingSetNormalTemperatur(p.data[4]);
                setFloorHeatingSetDayTemperatur(p.data[5]);
                setFloorHeatingSetNightTemperatur(p.data[6]);
                setFloorHeatingSetAwayTemperatur(p.data[7]);

                break;
            case Response_Control_Floor_Heating_Status_DLP:
                if (p.data[1] == 1) {
                    setFloorHeatingTemperaturType("F");
                } else {
                    setFloorHeatingTemperaturType("C");
                }
                if (p.data[2] == 1) {
                    setFloorHeatingStatus(OnOffType.ON);
                } else {
                    setFloorHeatingStatus(OnOffType.OFF);
                }

                if (p.data[3] == 1) {
                    setFloorHeatingMode(emumFHMode.Normal);
                } else if (p.data[3] == 2) {
                    setFloorHeatingMode(emumFHMode.Day);
                } else if (p.data[3] == 3) {
                    setFloorHeatingMode(emumFHMode.Night);
                } else if (p.data[3] == 4) {
                    setFloorHeatingMode(emumFHMode.Away);
                } else if (p.data[3] == 5) {
                    setFloorHeatingMode(emumFHMode.Timer);
                }

                setFloorHeatingSetNormalTemperatur(p.data[4]);
                setFloorHeatingSetDayTemperatur(p.data[5]);
                setFloorHeatingSetNightTemperatur(p.data[6]);
                setFloorHeatingSetAwayTemperatur(p.data[7]);

                break;
            case Response_UV_Switch_Control:
                switch (p.data[0]) {
                    case (byte) 1:
                        if (p.data[1] == 1) {
                            setUVSwitch1(OnOffType.ON);
                        } else {
                            setUVSwitch1(OnOffType.OFF);
                        }
                        break;
                    case (byte) 2:
                        if (p.data[1] == 2) {
                            setUVSwitch2(OnOffType.ON);
                        } else {
                            setUVSwitch2(OnOffType.OFF);
                        }
                        break;
                    case (byte) 3:
                        if (p.data[1] == 3) {
                            setUVSwitch3(OnOffType.ON);
                        } else {
                            setUVSwitch3(OnOffType.OFF);
                        }
                        break;
                    case (byte) 4:
                        if (p.data[1] == 4) {
                            setUVSwitch4(OnOffType.ON);
                        } else {
                            setUVSwitch4(OnOffType.OFF);
                        }
                        break;
                    case (byte) 5:
                        if (p.data[1] == 5) {
                            setUVSwitch5(OnOffType.ON);
                        } else {
                            setUVSwitch5(OnOffType.OFF);
                        }
                        break;
                    case (byte) 6:
                        if (p.data[1] == 6) {
                            setUVSwitch6(OnOffType.ON);
                        } else {
                            setUVSwitch6(OnOffType.OFF);
                        }
                        break;
                    default:
                        LOGGER.debug("For type: {}, Unhandled UV Switch Number: {}.", p.sourcedeviceType, p.data[0]);
                        break;
                }
            default:
                LOGGER.debug("For type: {}, Unhandled CommandType: {}.", p.sourcedeviceType, p.commandType);
                break;
        }
    }

    @Override
    public DeviceType getType() {
        return deviceType;
    }

    /**
     * Sets the DeviceType for this thermostat.
     *
     * @param DeviceType as provided by the C message
     */
    void setType(DeviceType type) {
        this.deviceType = type;
    }

    /**
     * Sets the actual temperature for this DLP panel.
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

    public void setUVSwitch1(OnOffType UVSwitch1) {
        if (this.uvSwitch1 != UVSwitch1) {
            setUpdated(true);
        }
        this.uvSwitch1 = UVSwitch1;
    }

    public OnOffType getUVSwitch1() {
        return uvSwitch1;
    }

    public void setUVSwitch2(OnOffType UVSwitch2) {
        if (this.uvSwitch2 != UVSwitch2) {
            setUpdated(true);
        }
        this.uvSwitch2 = UVSwitch2;
    }

    public OnOffType getUVSwitch2() {
        return uvSwitch2;
    }

    public void setUVSwitch3(OnOffType UVSwitch3) {
        if (this.uvSwitch3 != UVSwitch3) {
            setUpdated(true);
        }
        this.uvSwitch3 = UVSwitch3;
    }

    public OnOffType getUVSwitch3() {
        return uvSwitch3;
    }

    public void setUVSwitch4(OnOffType UVSwitch4) {
        if (this.uvSwitch4 != UVSwitch4) {
            setUpdated(true);
        }
        this.uvSwitch4 = UVSwitch4;
    }

    public OnOffType getUVSwitch4() {
        return uvSwitch4;
    }

    public void setUVSwitch5(OnOffType UVSwitch5) {
        if (this.uvSwitch5 != UVSwitch5) {
            setUpdated(true);
        }
        this.uvSwitch5 = UVSwitch5;
    }

    public OnOffType getUVSwitch5() {
        return uvSwitch5;
    }

    public void setUVSwitch6(OnOffType UVSwitch6) {
        if (this.uvSwitch6 != UVSwitch6) {
            setUpdated(true);
        }
        this.uvSwitch6 = UVSwitch6;
    }

    public OnOffType getUVSwitch6() {
        return uvSwitch6;
    }

    public void setFloorHeatingTemperaturType(String FloorHeatingTemperaturType) {
        if (this.floorHeatingTemperaturType != FloorHeatingTemperaturType) {
            setUpdated(true);
        }
        this.floorHeatingTemperaturType = FloorHeatingTemperaturType;
    }

    public String getFloorHeatingTemperaturType() {
        return floorHeatingTemperaturType;
    }

    public void setFloorHeatingCurrentTemperatur(double FloorHeatingCurrentTemperatur) {
        if (this.floorHeatingCurrentTemperatur != FloorHeatingCurrentTemperatur) {
            setUpdated(true);
        }
        this.floorHeatingCurrentTemperatur = FloorHeatingCurrentTemperatur;
    }

    public DecimalType getFloorHeatingCurrentTemperatur() {
        BigDecimal floorHeatingCurrentTemperaturValue = BigDecimal.valueOf(this.floorHeatingCurrentTemperatur);
        return new DecimalType(floorHeatingCurrentTemperaturValue);
    }

    public void setFloorHeatingStatus(OnOffType FloorHeatingStatus) {
        if (this.floorHeatingStatus != FloorHeatingStatus) {
            setUpdated(true);
        }
        this.floorHeatingStatus = FloorHeatingStatus;
    }

    public OnOffType getFloorHeatingStatus() {
        return floorHeatingStatus;
    }

    public void setFloorHeatingMode(emumFHMode FloorHeatingMode) {
        if (this.floorHeatingMode != FloorHeatingMode) {
            setUpdated(true);
        }
        this.floorHeatingMode = FloorHeatingMode;
    }

    public emumFHMode getFloorHeatingMode() {
        return floorHeatingMode;
    }

    public void setFloorHeatingSetNormalTemperatur(double FloorHeatingSetNormalTemperatur) {
        if (this.floorHeatingSetNormalTemperatur != FloorHeatingSetNormalTemperatur) {
            setUpdated(true);
        }
        this.floorHeatingSetNormalTemperatur = FloorHeatingSetNormalTemperatur;

        if (this.floorHeatingMode == emumFHMode.Normal) {
            setFloorHeatingCurrentTemperatur(FloorHeatingSetNormalTemperatur);
        }

    }

    public DecimalType getFloorHeatingSetNormalTemperatur() {
        BigDecimal floorHeatingSetNormalTemperaturValue = BigDecimal.valueOf(this.floorHeatingSetNormalTemperatur);
        return new DecimalType(floorHeatingSetNormalTemperaturValue);
    }

    public void setFloorHeatingSetDayTemperatur(double FloorHeatingSetDayTemperatur) {
        if (this.floorHeatingSetDayTemperatur != FloorHeatingSetDayTemperatur) {
            setUpdated(true);
        }
        this.floorHeatingSetDayTemperatur = FloorHeatingSetDayTemperatur;

        if (this.floorHeatingMode == emumFHMode.Day) {
            setFloorHeatingCurrentTemperatur(FloorHeatingSetDayTemperatur);
        }

        if (this.floorHeatingMode == emumFHMode.Timer && this.floorHeatingTimer == "Day") {
            setFloorHeatingCurrentTemperatur(FloorHeatingSetDayTemperatur);
        }
    }

    public DecimalType getFloorHeatingSetDayTemperatur() {
        BigDecimal floorHeatingSetDayTemperaturValue = BigDecimal.valueOf(this.floorHeatingSetDayTemperatur);
        return new DecimalType(floorHeatingSetDayTemperaturValue);

    }

    public void setFloorHeatingSetNightTemperatur(double FloorHeatingSetNightTemperatur) {
        if (this.floorHeatingSetNightTemperatur != FloorHeatingSetNightTemperatur) {
            setUpdated(true);
        }
        this.floorHeatingSetNightTemperatur = FloorHeatingSetNightTemperatur;

        if (this.floorHeatingMode == emumFHMode.Night) {
            setFloorHeatingCurrentTemperatur(FloorHeatingSetNightTemperatur);
        }

        if (this.floorHeatingMode == emumFHMode.Timer && this.floorHeatingTimer == "Night") {
            setFloorHeatingCurrentTemperatur(FloorHeatingSetNightTemperatur);
        }

    }

    public DecimalType getFloorHeatingSetNightTemperatur() {
        BigDecimal floorHeatingSetNightTemperaturValue = BigDecimal.valueOf(this.floorHeatingSetNightTemperatur);
        return new DecimalType(floorHeatingSetNightTemperaturValue);
    }

    public void setFloorHeatingSetAwayTemperatur(double FloorHeatingSetAwayTemperatur) {
        if (this.floorHeatingSetAwayTemperatur != FloorHeatingSetAwayTemperatur) {
            setUpdated(true);
        }
        this.floorHeatingSetAwayTemperatur = FloorHeatingSetAwayTemperatur;

        if (this.floorHeatingMode == emumFHMode.Away) {
            setFloorHeatingCurrentTemperatur(FloorHeatingSetAwayTemperatur);
        }
    }

    public DecimalType getFloorHeatingSetAwayTemperatur() {
        BigDecimal floorHeatingSetAwayTemperaturValue = BigDecimal.valueOf(this.floorHeatingSetAwayTemperatur);
        return new DecimalType(floorHeatingSetAwayTemperaturValue);
    }

    public void setFloorHeatingTimer(String FloorHeatingTimer) {
        if (this.floorHeatingTimer != FloorHeatingTimer) {
            setUpdated(true);
        }
        this.floorHeatingTimer = FloorHeatingTimer;
    }

    public String getFloorHeatingTimer() {
        return floorHeatingTimer;
    }

    // AC

    public void setACFanSpeed(String ACFanSpeed) {
        if (this.acFanSpeed != ACFanSpeed) {
            setUpdated(true);
        }
        this.acFanSpeed = ACFanSpeed;
    }

    public String getACFanSpeed() {
        return acFanSpeed;
    }

    public void setACMode(String ACMode) {
        if (this.acMode != ACMode) {
            setUpdated(true);
        }
        this.acMode = ACMode;
    }

    public String getACMode() {
        return acMode;
    }

    public void setACCoolingTemperatur(double ACCoolingTemperatur) {
        if (this.acCoolingTemp != ACCoolingTemperatur) {
            setUpdated(true);
        }
        this.acCoolingTemp = ACCoolingTemperatur;

        if (this.getACMode() == "Cooling") {
            setACCurrentTemperatur(ACCoolingTemperatur);
        }
    }

    public DecimalType getACCoolingTemperatur() {
        BigDecimal acCoolingTempValue = BigDecimal.valueOf(this.acCoolingTemp);
        return new DecimalType(acCoolingTempValue);
    }

    public void setACHeatTemperatur(double ACHeatTemperatur) {
        if (this.acHeatTemp != ACHeatTemperatur) {
            setUpdated(true);
        }
        this.acHeatTemp = ACHeatTemperatur;

        if (this.getACMode() == "Heating") {
            setACCurrentTemperatur(ACHeatTemperatur);
        }
    }

    public DecimalType getACHeatTemperatur() {
        BigDecimal acHeatTempValue = BigDecimal.valueOf(this.acHeatTemp);
        return new DecimalType(acHeatTempValue);
    }

    public void setACAutoTemperatur(double ACAutoTemperatur) {
        if (this.acAutoTemp != ACAutoTemperatur) {
            setUpdated(true);
        }
        this.acAutoTemp = ACAutoTemperatur;

        if (this.getACMode() == "Auto") {
            setACCurrentTemperatur(ACAutoTemperatur);
        }
    }

    public DecimalType getACAutoTemperatur() {
        BigDecimal acAutoTempValue = BigDecimal.valueOf(this.acAutoTemp);
        return new DecimalType(acAutoTempValue);
    }

    public void setACDryTemperatur(double ACDryTemperatur) {
        if (this.acDryTemp != ACDryTemperatur) {
            setUpdated(true);
        }
        this.acDryTemp = ACDryTemperatur;

        if (this.getACMode() == "Dehumidfy") {
            setACCurrentTemperatur(ACDryTemperatur);
        }
    }

    public DecimalType getACDryTemperatur() {
        BigDecimal acDryTempValue = BigDecimal.valueOf(this.acDryTemp);
        return new DecimalType(acDryTempValue);
    }

    public void setACCurrentTemperatur(double ACCurrentTemperatur) {
        if (this.acCurrentTemp != ACCurrentTemperatur) {
            setUpdated(true);
        }
        this.acCurrentTemp = ACCurrentTemperatur;
    }

    public DecimalType getACCurrentTemperatur() {
        BigDecimal acCurrentTempValue = BigDecimal.valueOf(this.acCurrentTemp);
        return new DecimalType(acCurrentTempValue);
    }

}
