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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.hdl.internal.handler.HdlPacket;

/**
 * The MR1216233 class contains support channels for device Type MR1216.
 * And how the information on the HDL bus is packet for this device.
 * This is a relay block with 12 relays.
 *
 * @author stigla - Initial contribution
 */
public class MR1216 extends Device {

    /** Device type for this Relay 12x16A **/
    private DeviceType deviceType = DeviceType.MR1216_233;

    private OnOffType relayCh01 = null;
    private OnOffType relayCh02 = null;
    private OnOffType relayCh03 = null;
    private OnOffType relayCh04 = null;
    private OnOffType relayCh05 = null;
    private OnOffType relayCh06 = null;
    private OnOffType relayCh07 = null;
    private OnOffType relayCh08 = null;
    private OnOffType relayCh09 = null;
    private OnOffType relayCh10 = null;
    private OnOffType relayCh11 = null;
    private OnOffType relayCh12 = null;

    public MR1216(DeviceConfiguration c) {
        super(c);
    }

    public void treatHDLPacketForDevice(HdlPacket p) {
        switch (p.commandType) {
            case Response_Read_Status_of_Channels:
                if (p.data[1] == 0) {
                    setRelayCh01(OnOffType.OFF);
                } else {
                    setRelayCh01(OnOffType.ON);
                }
                if (p.data[2] == 0) {
                    setRelayCh02(OnOffType.OFF);
                } else {
                    setRelayCh02(OnOffType.ON);
                }
                if (p.data[3] == 0) {
                    setRelayCh03(OnOffType.OFF);
                } else {
                    setRelayCh03(OnOffType.ON);
                }
                if (p.data[4] == 0) {
                    setRelayCh04(OnOffType.OFF);
                } else {
                    setRelayCh04(OnOffType.ON);
                }
                if (p.data[5] == 0) {
                    setRelayCh05(OnOffType.OFF);
                } else {
                    setRelayCh05(OnOffType.ON);
                }
                if (p.data[6] == 0) {
                    setRelayCh06(OnOffType.OFF);
                } else {
                    setRelayCh06(OnOffType.ON);
                }
                if (p.data[7] == 0) {
                    setRelayCh07(OnOffType.OFF);
                } else {
                    setRelayCh07(OnOffType.ON);
                }
                if (p.data[8] == 0) {
                    setRelayCh08(OnOffType.OFF);
                } else {
                    setRelayCh08(OnOffType.ON);
                }
                if (p.data[9] == 0) {
                    setRelayCh09(OnOffType.OFF);
                } else {
                    setRelayCh09(OnOffType.ON);
                }
                if (p.data[10] == 0) {
                    setRelayCh10(OnOffType.OFF);
                } else {
                    setRelayCh10(OnOffType.ON);
                }
                if (p.data[11] == 0) {
                    setRelayCh11(OnOffType.OFF);
                } else {
                    setRelayCh11(OnOffType.ON);
                }
                if (p.data[12] == 0) {
                    setRelayCh12(OnOffType.OFF);
                } else {
                    setRelayCh12(OnOffType.ON);
                }
                break;
            case Response_Read_Current_Level_of_Channels:
                if (p.data[1] == 0) {
                    setRelayCh01(OnOffType.OFF);
                } else {
                    setRelayCh01(OnOffType.ON);
                }
                if (p.data[2] == 0) {
                    setRelayCh02(OnOffType.OFF);
                } else {
                    setRelayCh02(OnOffType.ON);
                }
                if (p.data[3] == 0) {
                    setRelayCh03(OnOffType.OFF);
                } else {
                    setRelayCh03(OnOffType.ON);
                }
                if (p.data[4] == 0) {
                    setRelayCh04(OnOffType.OFF);
                } else {
                    setRelayCh04(OnOffType.ON);
                }
                if (p.data[5] == 0) {
                    setRelayCh05(OnOffType.OFF);
                } else {
                    setRelayCh05(OnOffType.ON);
                }
                if (p.data[6] == 0) {
                    setRelayCh06(OnOffType.OFF);
                } else {
                    setRelayCh06(OnOffType.ON);
                }
                if (p.data[7] == 0) {
                    setRelayCh07(OnOffType.OFF);
                } else {
                    setRelayCh07(OnOffType.ON);
                }
                if (p.data[8] == 0) {
                    setRelayCh08(OnOffType.OFF);
                } else {
                    setRelayCh08(OnOffType.ON);
                }
                if (p.data[9] == 0) {
                    setRelayCh09(OnOffType.OFF);
                } else {
                    setRelayCh09(OnOffType.ON);
                }
                if (p.data[10] == 0) {
                    setRelayCh10(OnOffType.OFF);
                } else {
                    setRelayCh10(OnOffType.ON);
                }
                if (p.data[11] == 0) {
                    setRelayCh11(OnOffType.OFF);
                } else {
                    setRelayCh11(OnOffType.ON);
                }
                if (p.data[12] == 0) {
                    setRelayCh12(OnOffType.OFF);
                } else {
                    setRelayCh12(OnOffType.ON);
                }
                break;
            case Response_Single_Channel_Control:
                if ((p.data[4] & 0x01) == 1) {
                    setRelayCh01(OnOffType.ON);
                } else {
                    setRelayCh01(OnOffType.OFF);
                }
                if ((p.data[4] & 0x02) == 2) {
                    setRelayCh02(OnOffType.ON);
                } else {
                    setRelayCh02(OnOffType.OFF);
                }
                if ((p.data[4] & 0x04) == 4) {
                    setRelayCh03(OnOffType.ON);
                } else {
                    setRelayCh03(OnOffType.OFF);
                }
                if ((p.data[4] & 0x08) == 8) {
                    setRelayCh04(OnOffType.ON);
                } else {
                    setRelayCh04(OnOffType.OFF);
                }
                if ((p.data[4] & 0x10) == 16) {
                    setRelayCh05(OnOffType.ON);
                } else {
                    setRelayCh05(OnOffType.OFF);
                }
                if ((p.data[4] & 0x20) == 32) {
                    setRelayCh06(OnOffType.ON);
                } else {
                    setRelayCh06(OnOffType.OFF);
                }
                if ((p.data[4] & 0x40) == 64) {
                    setRelayCh07(OnOffType.ON);
                } else {
                    setRelayCh07(OnOffType.OFF);
                }
                if ((p.data[4] & 0x80) == 128) {
                    setRelayCh08(OnOffType.ON);
                } else {
                    setRelayCh08(OnOffType.OFF);
                }
                if ((p.data[5] & 0x01) == 1) {
                    setRelayCh09(OnOffType.ON);
                } else {
                    setRelayCh09(OnOffType.OFF);
                }
                if ((p.data[5] & 0x02) == 2) {
                    setRelayCh10(OnOffType.ON);
                } else {
                    setRelayCh10(OnOffType.OFF);
                }
                if ((p.data[5] & 0x04) == 4) {
                    setRelayCh11(OnOffType.ON);
                } else {
                    setRelayCh11(OnOffType.OFF);
                }
                if ((p.data[5] & 0x08) == 8) {
                    setRelayCh12(OnOffType.ON);
                } else {
                    setRelayCh12(OnOffType.OFF);
                }
                break;
            case Broadcast_Status_of_Scene:
                LOGGER.debug("For type: {}, CommandType: {} Needs a lot of work.", p.sourcedeviceType, p.commandType);
                break;
            default:
                LOGGER.debug("For type: {}, Unhandled CommandType: {}.", p.sourcedeviceType, p.commandType);
                break;
        }
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

    public void setRelayCh03(OnOffType RelayCh03) {
        if (this.relayCh03 != RelayCh03) {
            setUpdated(true);
        }
        this.relayCh03 = RelayCh03;
    }

    public OnOffType getRelayCh03State() {
        return relayCh03;
    }

    public void setRelayCh04(OnOffType RelayCh04) {
        if (this.relayCh04 != RelayCh04) {
            setUpdated(true);
        }
        this.relayCh04 = RelayCh04;
    }

    public OnOffType getRelayCh04State() {
        return relayCh04;
    }

    public void setRelayCh05(OnOffType RelayCh05) {
        if (this.relayCh05 != RelayCh05) {
            setUpdated(true);
        }
        this.relayCh05 = RelayCh05;
    }

    public OnOffType getRelayCh05State() {
        return relayCh05;
    }

    public void setRelayCh06(OnOffType RelayCh06) {
        if (this.relayCh06 != RelayCh06) {
            setUpdated(true);
        }
        this.relayCh06 = RelayCh06;
    }

    public OnOffType getRelayCh06State() {
        return relayCh06;
    }

    public void setRelayCh07(OnOffType RelayCh07) {
        if (this.relayCh07 != RelayCh07) {
            setUpdated(true);
        }
        this.relayCh07 = RelayCh07;
    }

    public OnOffType getRelayCh07State() {
        return relayCh07;
    }

    public void setRelayCh08(OnOffType RelayCh08) {
        if (this.relayCh08 != RelayCh08) {
            setUpdated(true);
        }
        this.relayCh08 = RelayCh08;
    }

    public OnOffType getRelayCh08State() {
        return relayCh08;
    }

    public void setRelayCh09(OnOffType RelayCh09) {
        if (this.relayCh09 != RelayCh09) {
            setUpdated(true);
        }
        this.relayCh09 = RelayCh09;
    }

    public OnOffType getRelayCh09State() {
        return relayCh09;
    }

    public void setRelayCh10(OnOffType RelayCh10) {
        if (this.relayCh10 != RelayCh10) {
            setUpdated(true);
        }
        this.relayCh10 = RelayCh10;
    }

    public OnOffType getRelayCh10State() {
        return relayCh10;
    }

    public void setRelayCh11(OnOffType RelayCh11) {
        if (this.relayCh11 != RelayCh11) {
            setUpdated(true);
        }
        this.relayCh11 = RelayCh11;
    }

    public OnOffType getRelayCh11State() {
        return relayCh11;
    }

    public void setRelayCh12(OnOffType RelayCh12) {
        if (this.relayCh12 != RelayCh12) {
            setUpdated(true);
        }
        this.relayCh12 = RelayCh12;
    }

    public OnOffType getRelayCh12State() {
        return relayCh12;
    }

    @Override
    public DeviceType getType() {
        return deviceType;
    }

    /**
     * Sets the DeviceType for this Relay .
     *
     * @param DeviceType as provided by the HDL Packet message
     */
    void setType(DeviceType type) {
        this.deviceType = type;
    }

}
