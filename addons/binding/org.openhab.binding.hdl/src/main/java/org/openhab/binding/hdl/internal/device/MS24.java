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

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.openhab.binding.hdl.internal.handler.HdlPacket;

/**
 * The MS24 class contains support channels for device Type MS24.
 * And how the information on the HDL bus is packet for this device.
 * This is a controller with 24 dry contacts
 *
 * @author stigla - Initial contribution
 */
public class MS24 extends Device {

    /** Device type for Sensor Input Module **/
    private DeviceType deviceType = DeviceType.MS24;

    private OpenClosedType dryContact1 = null;
    private OpenClosedType dryContact2 = null;
    private OpenClosedType dryContact3 = null;
    private OpenClosedType dryContact4 = null;
    private OpenClosedType dryContact5 = null;
    private OpenClosedType dryContact6 = null;
    private OpenClosedType dryContact7 = null;
    private OpenClosedType dryContact8 = null;
    private OpenClosedType dryContact9 = null;
    private OpenClosedType dryContact10 = null;
    private OpenClosedType dryContact11 = null;
    private OpenClosedType dryContact12 = null;
    private OpenClosedType dryContact13 = null;
    private OpenClosedType dryContact14 = null;
    private OpenClosedType dryContact15 = null;
    private OpenClosedType dryContact16 = null;
    private OpenClosedType dryContact17 = null;
    private OpenClosedType dryContact18 = null;
    private OpenClosedType dryContact19 = null;
    private OpenClosedType dryContact20 = null;
    private OpenClosedType dryContact21 = null;
    private OpenClosedType dryContact22 = null;
    private OpenClosedType dryContact23 = null;
    private OpenClosedType dryContact24 = null;

    public MS24(DeviceConfiguration c) {
        super(c);
    }

    public void treatHDLPacketForDevice(HdlPacket p) {
        switch (p.commandType) {
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
                if (p.data[1] == 3) {
                    if (p.data[2] == 1) {
                        setDryContact3Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact3Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 4) {
                    if (p.data[2] == 1) {
                        setDryContact4Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact4Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 5) {
                    if (p.data[2] == 1) {
                        setDryContact5Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact5Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 6) {
                    if (p.data[2] == 1) {
                        setDryContact6Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact6Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 7) {
                    if (p.data[2] == 1) {
                        setDryContact7Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact7Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 8) {
                    if (p.data[2] == 1) {
                        setDryContact8Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact8Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 9) {
                    if (p.data[2] == 1) {
                        setDryContact9Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact9Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 10) {
                    if (p.data[2] == 1) {
                        setDryContact10Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact10Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 11) {
                    if (p.data[2] == 1) {
                        setDryContact11Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact11Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 12) {
                    if (p.data[2] == 1) {
                        setDryContact12Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact12Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 13) {
                    if (p.data[2] == 1) {
                        setDryContact13Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact13Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 14) {
                    if (p.data[2] == 1) {
                        setDryContact14Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact14Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 15) {
                    if (p.data[2] == 1) {
                        setDryContact15Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact15Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 16) {
                    if (p.data[2] == 1) {
                        setDryContact16Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact16Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 17) {
                    if (p.data[2] == 1) {
                        setDryContact17Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact17Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 18) {
                    if (p.data[2] == 1) {
                        setDryContact18Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact18Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 19) {
                    if (p.data[2] == 1) {
                        setDryContact19Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact19Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 20) {
                    if (p.data[2] == 1) {
                        setDryContact20Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact20Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 21) {
                    if (p.data[2] == 1) {
                        setDryContact21Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact21Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 22) {
                    if (p.data[2] == 1) {
                        setDryContact22Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact22Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 23) {
                    if (p.data[2] == 1) {
                        setDryContact23Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact23Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 24) {
                    if (p.data[2] == 1) {
                        setDryContact24Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact24Value(OpenClosedType.CLOSED);
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
                if (p.data[1] == 3) {
                    if (p.data[2] == 1) {
                        setDryContact3Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact3Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 4) {
                    if (p.data[2] == 1) {
                        setDryContact4Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact4Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 5) {
                    if (p.data[2] == 1) {
                        setDryContact5Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact5Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 6) {
                    if (p.data[2] == 1) {
                        setDryContact6Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact6Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 7) {
                    if (p.data[2] == 1) {
                        setDryContact7Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact7Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 8) {
                    if (p.data[2] == 1) {
                        setDryContact8Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact8Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 9) {
                    if (p.data[2] == 1) {
                        setDryContact9Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact9Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 10) {
                    if (p.data[2] == 1) {
                        setDryContact10Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact10Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 11) {
                    if (p.data[2] == 1) {
                        setDryContact11Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact11Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 12) {
                    if (p.data[2] == 1) {
                        setDryContact12Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact12Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 13) {
                    if (p.data[2] == 1) {
                        setDryContact13Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact13Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 14) {
                    if (p.data[2] == 1) {
                        setDryContact14Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact14Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 15) {
                    if (p.data[2] == 1) {
                        setDryContact15Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact15Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 16) {
                    if (p.data[2] == 1) {
                        setDryContact16Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact16Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 17) {
                    if (p.data[2] == 1) {
                        setDryContact17Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact17Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 18) {
                    if (p.data[2] == 1) {
                        setDryContact18Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact18Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 19) {
                    if (p.data[2] == 1) {
                        setDryContact19Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact19Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 20) {
                    if (p.data[2] == 1) {
                        setDryContact20Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact20Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 21) {
                    if (p.data[2] == 1) {
                        setDryContact21Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact21Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 22) {
                    if (p.data[2] == 1) {
                        setDryContact22Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact22Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 23) {
                    if (p.data[2] == 1) {
                        setDryContact23Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact23Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 24) {
                    if (p.data[2] == 1) {
                        setDryContact24Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact24Value(OpenClosedType.CLOSED);
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
                if (p.data[1] == 3) {
                    if (p.data[2] == 1) {
                        setDryContact3Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact3Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 4) {
                    if (p.data[2] == 1) {
                        setDryContact4Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact4Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 5) {
                    if (p.data[2] == 1) {
                        setDryContact5Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact5Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 6) {
                    if (p.data[2] == 1) {
                        setDryContact6Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact6Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 7) {
                    if (p.data[2] == 1) {
                        setDryContact7Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact7Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 8) {
                    if (p.data[2] == 1) {
                        setDryContact8Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact8Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 9) {
                    if (p.data[2] == 1) {
                        setDryContact9Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact9Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 10) {
                    if (p.data[2] == 1) {
                        setDryContact10Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact10Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 11) {
                    if (p.data[2] == 1) {
                        setDryContact11Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact11Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 12) {
                    if (p.data[2] == 1) {
                        setDryContact12Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact12Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 13) {
                    if (p.data[2] == 1) {
                        setDryContact13Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact13Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 14) {
                    if (p.data[2] == 1) {
                        setDryContact14Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact14Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 15) {
                    if (p.data[2] == 1) {
                        setDryContact15Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact15Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 16) {
                    if (p.data[2] == 1) {
                        setDryContact16Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact16Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 17) {
                    if (p.data[2] == 1) {
                        setDryContact17Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact17Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 18) {
                    if (p.data[2] == 1) {
                        setDryContact18Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact18Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 19) {
                    if (p.data[2] == 1) {
                        setDryContact19Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact19Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 20) {
                    if (p.data[2] == 1) {
                        setDryContact20Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact20Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 21) {
                    if (p.data[2] == 1) {
                        setDryContact21Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact21Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 22) {
                    if (p.data[2] == 1) {
                        setDryContact22Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact22Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 23) {
                    if (p.data[2] == 1) {
                        setDryContact23Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact23Value(OpenClosedType.CLOSED);
                    }
                }
                if (p.data[1] == 24) {
                    if (p.data[2] == 1) {
                        setDryContact24Value(OpenClosedType.OPEN);
                    } else {
                        setDryContact24Value(OpenClosedType.CLOSED);
                    }
                }
                break;
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
     * Sets the DeviceType for this 24 ports dry contact inputs.
     *
     * @param DeviceType as provided
     */
    void setType(DeviceType type) {
        this.deviceType = type;
    }

    /**
     * Sets the DryContact1Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact1Value(OpenClosedType value) {
        if (this.dryContact1 != value) {
            setUpdated(true);
        }
        this.dryContact1 = value;
    }

    /**
     * the DryContact1Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact1Value() {
        return dryContact1;
    }

    /**
     * Sets the DryContact1Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact2Value(OpenClosedType value) {
        if (this.dryContact2 != value) {
            setUpdated(true);
        }
        this.dryContact2 = value;
    }

    /**
     * the DryContact1Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact2Value() {
        return dryContact2;
    }

    /**
     * Sets the DryContact1Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact3Value(OpenClosedType value) {
        if (this.dryContact3 != value) {
            setUpdated(true);
        }
        this.dryContact3 = value;
    }

    /**
     * the DryContact1Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact3Value() {
        return dryContact3;
    }

    /**
     * Sets the DryContact1Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact4Value(OpenClosedType value) {
        if (this.dryContact4 != value) {
            setUpdated(true);
        }
        this.dryContact4 = value;
    }

    /**
     * the DryContact1Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact4Value() {
        return dryContact4;
    }

    /**
     * Sets the DryContact5Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact5Value(OpenClosedType value) {
        if (this.dryContact5 != value) {
            setUpdated(true);
        }
        this.dryContact5 = value;
    }

    /**
     * the DryContact5Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact5Value() {
        return dryContact5;
    }

    /**
     * Sets the DryContact6Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact6Value(OpenClosedType value) {
        if (this.dryContact6 != value) {
            setUpdated(true);
        }
        this.dryContact6 = value;
    }

    /**
     * the DryContact6Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact6Value() {
        return dryContact6;
    }

    /**
     * Sets the DryContact7Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact7Value(OpenClosedType value) {
        if (this.dryContact7 != value) {
            setUpdated(true);
        }
        this.dryContact7 = value;
    }

    /**
     * the DryContact7Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact7Value() {
        return dryContact7;
    }

    /**
     * Sets the DryContact8Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact8Value(OpenClosedType value) {
        if (this.dryContact8 != value) {
            setUpdated(true);
        }
        this.dryContact8 = value;
    }

    /**
     * the DryContact1Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact8Value() {
        return dryContact8;
    }

    /**
     * Sets the DryContact9Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact9Value(OpenClosedType value) {
        if (this.dryContact9 != value) {
            setUpdated(true);
        }
        this.dryContact9 = value;
    }

    /**
     * the DryContact9Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact9Value() {
        return dryContact9;
    }

    /**
     * Sets the DryContact10Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact10Value(OpenClosedType value) {
        if (this.dryContact10 != value) {
            setUpdated(true);
        }
        this.dryContact10 = value;
    }

    /**
     * the DryContact10Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact10Value() {
        return dryContact10;
    }

    /**
     * Sets the DryContact11Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact11Value(OpenClosedType value) {
        if (this.dryContact11 != value) {
            setUpdated(true);
        }
        this.dryContact11 = value;
    }

    /**
     * the DryContact11Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact11Value() {
        return dryContact11;
    }

    /**
     * Sets the DryContact12Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact1Value
     */
    public void setDryContact12Value(OpenClosedType value) {
        if (this.dryContact12 != value) {
            setUpdated(true);
        }
        this.dryContact12 = value;
    }

    /**
     * the DryContact12Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact12Value() {
        return dryContact12;
    }

    /**
     * Sets the DryContact13Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact13Value
     */
    public void setDryContact13Value(OpenClosedType value) {
        if (this.dryContact13 != value) {
            setUpdated(true);
        }
        this.dryContact13 = value;
    }

    /**
     * the DryContact13Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact13Value() {
        return dryContact13;
    }

    /**
     * Sets the DryContact14Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact14Value
     */
    public void setDryContact14Value(OpenClosedType value) {
        if (this.dryContact14 != value) {
            setUpdated(true);
        }
        this.dryContact14 = value;
    }

    /**
     * the DryContact14Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact14Value() {
        return dryContact14;
    }

    /**
     * Sets the DryContact15Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact15Value
     */
    public void setDryContact15Value(OpenClosedType value) {
        if (this.dryContact15 != value) {
            setUpdated(true);
        }
        this.dryContact15 = value;
    }

    /**
     * the DryContact15Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact15Value() {
        return dryContact15;
    }

    /**
     * Sets the DryContact16Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact16Value
     */
    public void setDryContact16Value(OpenClosedType value) {
        if (this.dryContact16 != value) {
            setUpdated(true);
        }
        this.dryContact16 = value;
    }

    /**
     * the DryContact1Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact16Value() {
        return dryContact16;
    }

    /**
     * Sets the DryContact17Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact17Value
     */
    public void setDryContact17Value(OpenClosedType value) {
        if (this.dryContact17 != value) {
            setUpdated(true);
        }
        this.dryContact17 = value;
    }

    /**
     * the DryContact17Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact17Value() {
        return dryContact17;
    }

    /**
     * Sets the DryContact18Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact18Value
     */
    public void setDryContact18Value(OpenClosedType value) {
        if (this.dryContact18 != value) {
            setUpdated(true);
        }
        this.dryContact18 = value;
    }

    /**
     * the DryContact18Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact18Value() {
        return dryContact18;
    }

    /**
     * Sets the DryContact19Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact19Value
     */
    public void setDryContact19Value(OpenClosedType value) {
        if (this.dryContact19 != value) {
            setUpdated(true);
        }
        this.dryContact19 = value;
    }

    /**
     * the DryContact19Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact19Value() {
        return dryContact19;
    }

    /**
     * Sets the DryContact20Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact20Value
     */
    public void setDryContact20Value(OpenClosedType value) {
        if (this.dryContact20 != value) {
            setUpdated(true);
        }
        this.dryContact20 = value;
    }

    /**
     * the DryContact20Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact20Value() {
        return dryContact20;
    }

    /**
     * Sets the DryContact21Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact21Value
     */
    public void setDryContact21Value(OpenClosedType value) {
        if (this.dryContact21 != value) {
            setUpdated(true);
        }
        this.dryContact21 = value;
    }

    /**
     * the DryContact1Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact21Value() {
        return dryContact21;
    }

    /**
     * Sets the DryContact22Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact22Value
     */
    public void setDryContact22Value(OpenClosedType value) {
        if (this.dryContact22 != value) {
            setUpdated(true);
        }
        this.dryContact22 = value;
    }

    /**
     * the DryContact22Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact22Value() {
        return dryContact22;
    }

    /**
     * Sets the DryContact23Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact23Value
     */
    public void setDryContact23Value(OpenClosedType value) {
        if (this.dryContact23 != value) {
            setUpdated(true);
        }
        this.dryContact23 = value;
    }

    /**
     * the DryContact1Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact23Value() {
        return dryContact23;
    }

    /**
     * Sets the DryContact24Value value for module 24 ports dry contact inputs.
     *
     * @param OpenClosedType Value of the DryContact24Value
     */
    public void setDryContact24Value(OpenClosedType value) {
        if (this.dryContact24 != value) {
            setUpdated(true);
        }
        this.dryContact24 = value;
    }

    /**
     * the DryContact24Value as <code>OpenClosedType</code>
     */
    public OpenClosedType getDryContact24Value() {
        return dryContact24;
    }

}
