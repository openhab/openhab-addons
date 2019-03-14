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

import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.openhab.binding.hdl.internal.handler.HdlPacket;
//import org.eclipse.smarthome.core.library.items.RollershutterItem;

/**
 * The MW02 class contains support channels for device Type MW02.
 * And how the information on the HDL bus is packet for this device.
 * This is a controller to control 2 curtains.
 *
 * @author stigla - Initial contribution
 */
public class MW02 extends Device {

    /** Device type for Curtain controller for controlling off 3. parts curtains **/
    private DeviceType deviceType = DeviceType.MW02;

    private UpDownType shutter1UpDownState = null;
    private UpDownType shutter2UpDownState = null;
    private StopMoveType shutter1StopMoveState = null;
    private StopMoveType shutter2StopMoveState = null;

    public MW02(DeviceConfiguration c) {
        super(c);
    }

    public void treatHDLPacketForDevice(HdlPacket p) {
        LOGGER.debug("Starting treating package of Commandtype: {}, Of source device: {}.", p.commandType,
                p.sourcedeviceType);

        switch (p.commandType) {
            case Response_Read_Status_of_Curtain_Switch:
                switch (p.data[0]) {
                    case (byte) 1: // Curtain 1
                        switch (p.data[1]) {
                            case (byte) 0: // Stop
                                setStopMoveShutter1Status(StopMoveType.STOP);
                                break;
                            case (byte) 1: // Open
                                setUpDownShutter1Status(UpDownType.DOWN);
                                setStopMoveShutter1Status(StopMoveType.MOVE);
                                break;
                            case (byte) 2: // Close
                                setUpDownShutter1Status(UpDownType.UP);
                                setStopMoveShutter1Status(StopMoveType.MOVE);
                                break;
                        }
                        break;
                    case (byte) 2: // Curtain 2
                        switch (p.data[1]) {
                            case (byte) 0: // Stop
                                setStopMoveShutter2Status(StopMoveType.STOP);
                                break;
                            case (byte) 1: // Open
                                setUpDownShutter2Status(UpDownType.DOWN);
                                setStopMoveShutter2Status(StopMoveType.MOVE);
                                break;
                            case (byte) 2: // Close
                                setUpDownShutter2Status(UpDownType.UP);
                                setStopMoveShutter2Status(StopMoveType.MOVE);
                                break;
                        }
                        break;
                    case (byte) 17:
                        // Percentage Curtain 1?
                        break;
                }
                break;
            case Response_Curtain_Switch_Control:
                switch (p.data[0]) {
                    case (byte) 1: // Curtain 1
                        switch (p.data[1]) {
                            case (byte) 0: // Stop
                                setStopMoveShutter1Status(StopMoveType.STOP);
                                break;
                            case (byte) 1: // Open
                                setUpDownShutter1Status(UpDownType.DOWN);
                                setStopMoveShutter1Status(StopMoveType.MOVE);
                                break;
                            case (byte) 2: // Close
                                setUpDownShutter1Status(UpDownType.UP);
                                setStopMoveShutter1Status(StopMoveType.MOVE);
                                break;
                        }
                        break;
                    case (byte) 2: // Curtain 2
                        switch (p.data[1]) {
                            case (byte) 0: // Stop
                                setStopMoveShutter2Status(StopMoveType.STOP);
                                break;
                            case (byte) 1: // Open
                                setUpDownShutter2Status(UpDownType.DOWN);
                                setStopMoveShutter2Status(StopMoveType.MOVE);
                                break;
                            case (byte) 2: // Close
                                setUpDownShutter2Status(UpDownType.UP);
                                setStopMoveShutter2Status(StopMoveType.MOVE);
                                break;
                        }
                        break;
                    case (byte) 17:
                        // Percentage Curtain 1?
                        break;
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
     * Sets the DeviceType for this Curtain controller.
     *
     * @param DeviceType as provided
     */
    void setType(DeviceType type) {
        this.deviceType = type;
    }

    /**
     * Sets the Shutter1 State.
     *
     * @param UpDownType Value of the Shutter1
     */
    public void setUpDownShutter1Status(UpDownType value) {
        if (this.shutter1UpDownState != value) {
            setUpdated(true);
        }
        this.shutter1UpDownState = value;
    }

    /**
     * get the UpDown value for Shutter 1
     */
    public UpDownType getUpDownShutter1Status() {
        return shutter1UpDownState;
    }

    /**
     * Sets the Shutter1 StopMove State.
     *
     * @param StopMove Value of the Shutter1
     */
    public void setStopMoveShutter1Status(StopMoveType value) {
        if (this.shutter1StopMoveState != value) {
            setUpdated(true);
        }
        this.shutter1StopMoveState = value;
    }

    /**
     * get the StopMove value for Shutter 1
     */
    public StopMoveType getStopMoveShutter1Status() {
        return shutter1StopMoveState;
    }

    /**
     * Sets the UpDown Shutter2 State.
     */
    public void setUpDownShutter2Status(UpDownType value) {
        if (this.shutter2UpDownState != value) {
            setUpdated(true);
        }
        this.shutter2UpDownState = value;
    }

    /**
     * get the UpDown value for Shutter 2
     */
    public UpDownType getUpDownShutter2Status() {
        return shutter2UpDownState;
    }

    /**
     * Sets the Shutter1 StopMove State.
     *
     * @param StopMove Value of the Shutter2
     */
    public void setStopMoveShutter2Status(StopMoveType value) {
        if (this.shutter2StopMoveState != value) {
            setUpdated(true);
        }
        this.shutter2StopMoveState = value;
    }

    /**
     * get the StopMove value for Shutter 2
     */
    public StopMoveType getStopMoveShutter2Status() {
        return shutter2StopMoveState;
    }
}
