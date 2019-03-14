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

import java.util.Date;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.hdl.internal.handler.HdlPacket;

/**
 * The ML01 class contains support channels for device Type ML01.
 * and how the information on the HDL bus is packet for this device.
 * This is a logic module.
 *
 * @author stigla - Initial contribution
 */
public class ML01 extends Device {

    private Date dateTime = null;
    private OnOffType uvSwitch200 = null;
    private OnOffType uvSwitch201 = null;
    private OnOffType uvSwitch202 = null;
    private OnOffType uvSwitch203 = null;
    private OnOffType uvSwitch204 = null;
    private OnOffType uvSwitch205 = null;
    private OnOffType uvSwitch206 = null;
    private OnOffType uvSwitch207 = null;
    private OnOffType uvSwitch208 = null;
    private OnOffType uvSwitch209 = null;
    private OnOffType uvSwitch210 = null;
    private OnOffType uvSwitch211 = null;
    private OnOffType uvSwitch212 = null;
    private OnOffType uvSwitch213 = null;
    private OnOffType uvSwitch214 = null;
    private OnOffType uvSwitch215 = null;
    private OnOffType uvSwitch216 = null;
    private OnOffType uvSwitch217 = null;
    private OnOffType uvSwitch218 = null;
    private OnOffType uvSwitch219 = null;
    private OnOffType uvSwitch220 = null;
    private OnOffType uvSwitch221 = null;
    private OnOffType uvSwitch222 = null;
    private OnOffType uvSwitch223 = null;
    private OnOffType uvSwitch224 = null;
    private OnOffType uvSwitch225 = null;
    private OnOffType uvSwitch226 = null;
    private OnOffType uvSwitch227 = null;
    private OnOffType uvSwitch228 = null;
    private OnOffType uvSwitch229 = null;
    private OnOffType uvSwitch230 = null;
    private OnOffType uvSwitch231 = null;
    private OnOffType uvSwitch232 = null;
    private OnOffType uvSwitch233 = null;
    private OnOffType uvSwitch234 = null;
    private OnOffType uvSwitch235 = null;
    private OnOffType uvSwitch236 = null;
    private OnOffType uvSwitch237 = null;
    private OnOffType uvSwitch238 = null;
    private OnOffType uvSwitch239 = null;
    private OnOffType uvSwitch240 = null;

    /** Device type for this Logic Panel **/
    private DeviceType deviceType = DeviceType.ML01;

    public ML01(DeviceConfiguration c) {
        super(c);
    }

    @SuppressWarnings("deprecation")
    public void treatHDLPacketForDevice(HdlPacket p) {
        switch (p.commandType) {
            case Broadcast_System_Date_and_Time_Every_Minute:
                Date aDate = new Date(p.data[0] + 2100, p.data[1], p.data[2], p.data[3], p.data[4], p.data[5]);
                setDateSetpoint(aDate);
                setUpdated(true);
                // LOGGER.debug("Time is: {}", aDate);
                break;
            case Response_UV_Switch_Control:
                switch (p.data[0]) {
                    case (byte) 201:
                        if (p.data[1] == 1) {
                            setUVSwitch201(OnOffType.ON);
                        } else {
                            setUVSwitch201(OnOffType.OFF);
                        }
                        break;
                    case (byte) 202:
                        if (p.data[1] == 1) {
                            setUVSwitch202(OnOffType.ON);
                        } else {
                            setUVSwitch202(OnOffType.OFF);
                        }
                        break;
                    case (byte) 203:
                        if (p.data[1] == 1) {
                            setUVSwitch203(OnOffType.ON);
                        } else {
                            setUVSwitch203(OnOffType.OFF);
                        }
                        break;
                    case (byte) 204:
                        if (p.data[1] == 1) {
                            setUVSwitch204(OnOffType.ON);
                        } else {
                            setUVSwitch204(OnOffType.OFF);
                        }
                        break;
                    case (byte) 205:
                        if (p.data[1] == 1) {
                            setUVSwitch205(OnOffType.ON);
                        } else {
                            setUVSwitch205(OnOffType.OFF);
                        }
                        break;
                    case (byte) 206:
                        if (p.data[1] == 1) {
                            setUVSwitch206(OnOffType.ON);
                        } else {
                            setUVSwitch206(OnOffType.OFF);
                        }
                        break;
                    case (byte) 207:
                        if (p.data[1] == 1) {
                            setUVSwitch207(OnOffType.ON);
                        } else {
                            setUVSwitch207(OnOffType.OFF);
                        }
                        break;
                    case (byte) 208:
                        if (p.data[1] == 1) {
                            setUVSwitch208(OnOffType.ON);
                        } else {
                            setUVSwitch208(OnOffType.OFF);
                        }
                        break;
                    case (byte) 209:
                        if (p.data[1] == 1) {
                            setUVSwitch209(OnOffType.ON);
                        } else {
                            setUVSwitch209(OnOffType.OFF);
                        }
                        break;
                    case (byte) 210:
                        if (p.data[1] == 1) {
                            setUVSwitch210(OnOffType.ON);
                        } else {
                            setUVSwitch210(OnOffType.OFF);
                        }
                        break;
                    case (byte) 211:
                        if (p.data[1] == 1) {
                            setUVSwitch211(OnOffType.ON);
                        } else {
                            setUVSwitch211(OnOffType.OFF);
                        }
                        break;
                    case (byte) 212:
                        if (p.data[1] == 1) {
                            setUVSwitch212(OnOffType.ON);
                        } else {
                            setUVSwitch212(OnOffType.OFF);
                        }
                        break;
                    case (byte) 213:
                        if (p.data[1] == 1) {
                            setUVSwitch213(OnOffType.ON);
                        } else {
                            setUVSwitch213(OnOffType.OFF);
                        }
                        break;
                    case (byte) 214:
                        if (p.data[1] == 1) {
                            setUVSwitch214(OnOffType.ON);
                        } else {
                            setUVSwitch214(OnOffType.OFF);
                        }
                        break;
                    case (byte) 215:
                        if (p.data[1] == 1) {
                            setUVSwitch215(OnOffType.ON);
                        } else {
                            setUVSwitch215(OnOffType.OFF);
                        }
                        break;
                    case (byte) 216:
                        if (p.data[1] == 1) {
                            setUVSwitch216(OnOffType.ON);
                        } else {
                            setUVSwitch216(OnOffType.OFF);
                        }
                        break;
                    case (byte) 217:
                        if (p.data[1] == 1) {
                            setUVSwitch217(OnOffType.ON);
                        } else {
                            setUVSwitch217(OnOffType.OFF);
                        }
                        break;
                    case (byte) 218:
                        if (p.data[1] == 1) {
                            setUVSwitch218(OnOffType.ON);
                        } else {
                            setUVSwitch218(OnOffType.OFF);
                        }
                        break;
                    case (byte) 219:
                        if (p.data[1] == 1) {
                            setUVSwitch219(OnOffType.ON);
                        } else {
                            setUVSwitch219(OnOffType.OFF);
                        }
                        break;
                    case (byte) 220:
                        if (p.data[1] == 1) {
                            setUVSwitch220(OnOffType.ON);
                        } else {
                            setUVSwitch220(OnOffType.OFF);
                        }
                        break;
                    case (byte) 221:
                        if (p.data[1] == 1) {
                            setUVSwitch221(OnOffType.ON);
                        } else {
                            setUVSwitch221(OnOffType.OFF);
                        }
                        break;
                    case (byte) 222:
                        if (p.data[1] == 1) {
                            setUVSwitch222(OnOffType.ON);
                        } else {
                            setUVSwitch222(OnOffType.OFF);
                        }
                        break;
                    case (byte) 223:
                        if (p.data[1] == 1) {
                            setUVSwitch223(OnOffType.ON);
                        } else {
                            setUVSwitch223(OnOffType.OFF);
                        }
                        break;
                    case (byte) 224:
                        if (p.data[1] == 1) {
                            setUVSwitch224(OnOffType.ON);
                        } else {
                            setUVSwitch224(OnOffType.OFF);
                        }
                        break;
                    case (byte) 225:
                        if (p.data[1] == 1) {
                            setUVSwitch225(OnOffType.ON);
                        } else {
                            setUVSwitch225(OnOffType.OFF);
                        }
                        break;
                    case (byte) 226:
                        if (p.data[1] == 1) {
                            setUVSwitch226(OnOffType.ON);
                        } else {
                            setUVSwitch226(OnOffType.OFF);
                        }
                        break;
                    case (byte) 227:
                        if (p.data[1] == 1) {
                            setUVSwitch227(OnOffType.ON);
                        } else {
                            setUVSwitch227(OnOffType.OFF);
                        }
                        break;
                    case (byte) 228:
                        if (p.data[1] == 1) {
                            setUVSwitch228(OnOffType.ON);
                        } else {
                            setUVSwitch228(OnOffType.OFF);
                        }
                        break;
                    case (byte) 229:
                        if (p.data[1] == 1) {
                            setUVSwitch229(OnOffType.ON);
                        } else {
                            setUVSwitch229(OnOffType.OFF);
                        }
                        break;
                    case (byte) 230:
                        if (p.data[1] == 1) {
                            setUVSwitch230(OnOffType.ON);
                        } else {
                            setUVSwitch230(OnOffType.OFF);
                        }
                        break;
                    case (byte) 231:
                        if (p.data[1] == 1) {
                            setUVSwitch231(OnOffType.ON);
                        } else {
                            setUVSwitch231(OnOffType.OFF);
                        }
                        break;
                    case (byte) 232:
                        if (p.data[1] == 1) {
                            setUVSwitch232(OnOffType.ON);
                        } else {
                            setUVSwitch232(OnOffType.OFF);
                        }
                        break;
                    case (byte) 233:
                        if (p.data[1] == 1) {
                            setUVSwitch233(OnOffType.ON);
                        } else {
                            setUVSwitch233(OnOffType.OFF);
                        }
                        break;
                    case (byte) 234:
                        if (p.data[1] == 1) {
                            setUVSwitch234(OnOffType.ON);
                        } else {
                            setUVSwitch234(OnOffType.OFF);
                        }
                        break;
                    case (byte) 235:
                        if (p.data[1] == 1) {
                            setUVSwitch235(OnOffType.ON);
                        } else {
                            setUVSwitch235(OnOffType.OFF);
                        }
                        break;
                    case (byte) 236:
                        if (p.data[1] == 1) {
                            setUVSwitch236(OnOffType.ON);
                        } else {
                            setUVSwitch236(OnOffType.OFF);
                        }
                        break;
                    case (byte) 237:
                        if (p.data[1] == 1) {
                            setUVSwitch237(OnOffType.ON);
                        } else {
                            setUVSwitch237(OnOffType.OFF);
                        }
                        break;
                    case (byte) 238:
                        if (p.data[1] == 1) {
                            setUVSwitch238(OnOffType.ON);
                        } else {
                            setUVSwitch238(OnOffType.OFF);
                        }
                        break;
                    case (byte) 239:
                        if (p.data[1] == 1) {
                            setUVSwitch239(OnOffType.ON);
                        } else {
                            setUVSwitch239(OnOffType.OFF);
                        }
                        break;
                    case (byte) 240:
                        if (p.data[1] == 1) {
                            setUVSwitch240(OnOffType.ON);
                        } else {
                            setUVSwitch240(OnOffType.OFF);
                        }
                        break;

                    default:
                        LOGGER.debug("For type: {}, Unhandled UV Switch Number: {}.", p.sourcedeviceType, p.data[0]);
                        break;
                }
                break;
            default:
                LOGGER.debug("For type: {}, Unhandled CommandType: {}.", p.sourcedeviceType, p.commandType);
                break;
        }

    }

    public void setUVSwitch200(OnOffType uvSwitch200) {
        if (this.uvSwitch200 != uvSwitch200) {
            setUpdated(true);
        }
        this.uvSwitch200 = uvSwitch200;
    }

    public OnOffType getUVSwitch200() {
        return uvSwitch200;
    }

    public void setUVSwitch201(OnOffType uvSwitch201) {
        if (this.uvSwitch201 != uvSwitch201) {
            setUpdated(true);
        }
        this.uvSwitch201 = uvSwitch201;
    }

    public OnOffType getUVSwitch201() {
        return uvSwitch201;
    }

    public void setUVSwitch202(OnOffType uvSwitch202) {
        if (this.uvSwitch202 != uvSwitch202) {
            setUpdated(true);
        }
        this.uvSwitch202 = uvSwitch202;
    }

    public OnOffType getUVSwitch202() {
        return uvSwitch202;
    }

    public void setUVSwitch203(OnOffType uvSwitch203) {
        if (this.uvSwitch203 != uvSwitch203) {
            setUpdated(true);
        }
        this.uvSwitch203 = uvSwitch203;
    }

    public OnOffType getUVSwitch203() {
        return uvSwitch203;
    }

    public void setUVSwitch204(OnOffType uvSwitch204) {
        if (this.uvSwitch204 != uvSwitch204) {
            setUpdated(true);
        }
        this.uvSwitch204 = uvSwitch204;
    }

    public OnOffType getUVSwitch204() {
        return uvSwitch204;
    }

    public void setUVSwitch205(OnOffType uvSwitch205) {
        if (this.uvSwitch205 != uvSwitch205) {
            setUpdated(true);
        }
        this.uvSwitch205 = uvSwitch205;
    }

    public OnOffType getUVSwitch205() {
        return uvSwitch205;
    }

    public void setUVSwitch206(OnOffType uvSwitch206) {
        if (this.uvSwitch206 != uvSwitch206) {
            setUpdated(true);
        }
        this.uvSwitch206 = uvSwitch206;
    }

    public OnOffType getUVSwitch206() {
        return uvSwitch206;
    }

    public void setUVSwitch207(OnOffType uvSwitch207) {
        if (this.uvSwitch207 != uvSwitch207) {
            setUpdated(true);
        }
        this.uvSwitch207 = uvSwitch207;
    }

    public OnOffType getUVSwitch207() {
        return uvSwitch207;
    }

    public void setUVSwitch208(OnOffType uvSwitch208) {
        if (this.uvSwitch208 != uvSwitch208) {
            setUpdated(true);
        }
        this.uvSwitch208 = uvSwitch208;
    }

    public OnOffType getUVSwitch208() {
        return uvSwitch208;
    }

    public void setUVSwitch209(OnOffType uvSwitch209) {
        if (this.uvSwitch209 != uvSwitch209) {
            setUpdated(true);
        }
        this.uvSwitch209 = uvSwitch209;
    }

    public OnOffType getUVSwitch209() {
        return uvSwitch209;
    }

    public void setUVSwitch210(OnOffType uvSwitch210) {
        if (this.uvSwitch210 != uvSwitch210) {
            setUpdated(true);
        }
        this.uvSwitch210 = uvSwitch210;
    }

    public OnOffType getUVSwitch210() {
        return uvSwitch210;
    }

    public void setUVSwitch211(OnOffType uvSwitch211) {
        if (this.uvSwitch211 != uvSwitch211) {
            setUpdated(true);
        }
        this.uvSwitch211 = uvSwitch211;
    }

    public OnOffType getUVSwitch211() {
        return uvSwitch211;
    }

    public void setUVSwitch212(OnOffType uvSwitch212) {
        if (this.uvSwitch212 != uvSwitch212) {
            setUpdated(true);
        }
        this.uvSwitch212 = uvSwitch212;
    }

    public OnOffType getUVSwitch212() {
        return uvSwitch212;
    }

    public void setUVSwitch213(OnOffType uvSwitch213) {
        if (this.uvSwitch213 != uvSwitch213) {
            setUpdated(true);
        }
        this.uvSwitch213 = uvSwitch213;
    }

    public OnOffType getUVSwitch213() {
        return uvSwitch213;
    }

    public void setUVSwitch214(OnOffType uvSwitch214) {
        if (this.uvSwitch214 != uvSwitch214) {
            setUpdated(true);
        }
        this.uvSwitch214 = uvSwitch214;
    }

    public OnOffType getUVSwitch214() {
        return uvSwitch214;
    }

    public void setUVSwitch215(OnOffType uvSwitch215) {
        if (this.uvSwitch215 != uvSwitch215) {
            setUpdated(true);
        }
        this.uvSwitch215 = uvSwitch215;
    }

    public OnOffType getUVSwitch215() {
        return uvSwitch215;
    }

    public void setUVSwitch216(OnOffType uvSwitch216) {
        if (this.uvSwitch216 != uvSwitch216) {
            setUpdated(true);
        }
        this.uvSwitch216 = uvSwitch216;
    }

    public OnOffType getUVSwitch216() {
        return uvSwitch216;
    }

    public void setUVSwitch217(OnOffType uvSwitch217) {
        if (this.uvSwitch217 != uvSwitch217) {
            setUpdated(true);
        }
        this.uvSwitch217 = uvSwitch217;
    }

    public OnOffType getUVSwitch217() {
        return uvSwitch217;
    }

    public void setUVSwitch218(OnOffType uvSwitch218) {
        if (this.uvSwitch218 != uvSwitch218) {
            setUpdated(true);
        }
        this.uvSwitch218 = uvSwitch218;
    }

    public OnOffType getUVSwitch218() {
        return uvSwitch218;
    }

    public void setUVSwitch219(OnOffType uvSwitch219) {
        if (this.uvSwitch219 != uvSwitch219) {
            setUpdated(true);
        }
        this.uvSwitch219 = uvSwitch219;
    }

    public OnOffType getUVSwitch219() {
        return uvSwitch219;
    }

    public void setUVSwitch220(OnOffType uvSwitch220) {
        if (this.uvSwitch220 != uvSwitch220) {
            setUpdated(true);
        }
        this.uvSwitch220 = uvSwitch220;
    }

    public OnOffType getUVSwitch220() {
        return uvSwitch220;
    }

    public void setUVSwitch221(OnOffType uvSwitch221) {
        if (this.uvSwitch221 != uvSwitch221) {
            setUpdated(true);
        }
        this.uvSwitch221 = uvSwitch221;
    }

    public OnOffType getUVSwitch221() {
        return uvSwitch221;
    }

    public void setUVSwitch222(OnOffType uvSwitch222) {
        if (this.uvSwitch222 != uvSwitch222) {
            setUpdated(true);
        }
        this.uvSwitch222 = uvSwitch222;
    }

    public OnOffType getUVSwitch222() {
        return uvSwitch222;
    }

    public void setUVSwitch223(OnOffType uvSwitch223) {
        if (this.uvSwitch223 != uvSwitch223) {
            setUpdated(true);
        }
        this.uvSwitch223 = uvSwitch223;
    }

    public OnOffType getUVSwitch223() {
        return uvSwitch223;
    }

    public void setUVSwitch224(OnOffType uvSwitch224) {
        if (this.uvSwitch224 != uvSwitch224) {
            setUpdated(true);
        }
        this.uvSwitch224 = uvSwitch224;
    }

    public OnOffType getUVSwitch224() {
        return uvSwitch224;
    }

    public void setUVSwitch225(OnOffType uvSwitch225) {
        if (this.uvSwitch225 != uvSwitch225) {
            setUpdated(true);
        }
        this.uvSwitch225 = uvSwitch225;
    }

    public OnOffType getUVSwitch225() {
        return uvSwitch225;
    }

    public void setUVSwitch226(OnOffType uvSwitch226) {
        if (this.uvSwitch226 != uvSwitch226) {
            setUpdated(true);
        }
        this.uvSwitch226 = uvSwitch226;
    }

    public OnOffType getUVSwitch226() {
        return uvSwitch226;
    }

    public void setUVSwitch227(OnOffType uvSwitch227) {
        if (this.uvSwitch227 != uvSwitch227) {
            setUpdated(true);
        }
        this.uvSwitch227 = uvSwitch227;
    }

    public OnOffType getUVSwitch227() {
        return uvSwitch227;
    }

    public void setUVSwitch228(OnOffType uvSwitch228) {
        if (this.uvSwitch228 != uvSwitch228) {
            setUpdated(true);
        }
        this.uvSwitch228 = uvSwitch228;
    }

    public OnOffType getUVSwitch228() {
        return uvSwitch228;
    }

    public void setUVSwitch229(OnOffType uvSwitch229) {
        if (this.uvSwitch229 != uvSwitch229) {
            setUpdated(true);
        }
        this.uvSwitch229 = uvSwitch229;
    }

    public OnOffType getUVSwitch229() {
        return uvSwitch229;
    }

    public void setUVSwitch230(OnOffType uvSwitch230) {
        if (this.uvSwitch230 != uvSwitch230) {
            setUpdated(true);
        }
        this.uvSwitch230 = uvSwitch230;
    }

    public OnOffType getUVSwitch230() {
        return uvSwitch230;
    }

    public void setUVSwitch231(OnOffType uvSwitch231) {
        if (this.uvSwitch231 != uvSwitch231) {
            setUpdated(true);
        }
        this.uvSwitch231 = uvSwitch231;
    }

    public OnOffType getUVSwitch231() {
        return uvSwitch231;
    }

    public void setUVSwitch232(OnOffType uvSwitch232) {
        if (this.uvSwitch232 != uvSwitch232) {
            setUpdated(true);
        }
        this.uvSwitch232 = uvSwitch232;
    }

    public OnOffType getUVSwitch232() {
        return uvSwitch232;
    }

    public void setUVSwitch233(OnOffType uvSwitch233) {
        if (this.uvSwitch233 != uvSwitch233) {
            setUpdated(true);
        }
        this.uvSwitch233 = uvSwitch233;
    }

    public OnOffType getUVSwitch233() {
        return uvSwitch233;
    }

    public void setUVSwitch234(OnOffType uvSwitch234) {
        if (this.uvSwitch234 != uvSwitch234) {
            setUpdated(true);
        }
        this.uvSwitch234 = uvSwitch234;
    }

    public OnOffType getUVSwitch234() {
        return uvSwitch234;
    }

    public void setUVSwitch235(OnOffType uvSwitch235) {
        if (this.uvSwitch235 != uvSwitch235) {
            setUpdated(true);
        }
        this.uvSwitch235 = uvSwitch235;
    }

    public OnOffType getUVSwitch235() {
        return uvSwitch235;
    }

    public void setUVSwitch236(OnOffType uvSwitch236) {
        if (this.uvSwitch236 != uvSwitch236) {
            setUpdated(true);
        }
        this.uvSwitch236 = uvSwitch236;
    }

    public OnOffType getUVSwitch236() {
        return uvSwitch236;
    }

    public void setUVSwitch237(OnOffType uvSwitch237) {
        if (this.uvSwitch237 != uvSwitch237) {
            setUpdated(true);
        }
        this.uvSwitch237 = uvSwitch237;
    }

    public OnOffType getUVSwitch237() {
        return uvSwitch237;
    }

    public void setUVSwitch238(OnOffType uvSwitch238) {
        if (this.uvSwitch238 != uvSwitch238) {
            setUpdated(true);
        }
        this.uvSwitch238 = uvSwitch238;
    }

    public OnOffType getUVSwitch238() {
        return uvSwitch238;
    }

    public void setUVSwitch239(OnOffType uvSwitch239) {
        if (this.uvSwitch239 != uvSwitch239) {
            setUpdated(true);
        }
        this.uvSwitch239 = uvSwitch239;
    }

    public OnOffType getUVSwitch239() {
        return uvSwitch239;
    }

    public void setUVSwitch240(OnOffType uvSwitch240) {
        if (this.uvSwitch240 != uvSwitch240) {
            setUpdated(true);
        }
        this.uvSwitch240 = uvSwitch240;
    }

    public OnOffType getUVSwitch240() {
        return uvSwitch240;
    }

    public void setDateSetpoint(Date date) {
        this.dateTime = date;
    }

    public Date getDateSetpoint() {
        return dateTime;
    }

    @Override
    public DeviceType getType() {
        return deviceType;
    }

    /**
     * Sets the DeviceType for this Logic Module.
     *
     * @param DeviceType as provided by the hdlPacket
     */
    void setType(DeviceType type) {
        this.deviceType = type;
    }

}
