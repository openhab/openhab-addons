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
 * The MPT04_48 class contains support channels for device Type MPT04.
 * And how the information on the HDL bus is packet for this device.
 * This is a button panel with 4 buttons.
 *
 * @author stigla - Initial contribution
 */
public class MPT0448 extends Device {

    /** Device type for Digital touch switch 4 buttons **/
    private DeviceType deviceType = DeviceType.MPT04_48;

    private OnOffType button1 = null;
    private OnOffType button2 = null;
    private OnOffType button3 = null;
    private OnOffType button4 = null;

    public MPT0448(DeviceConfiguration c) {
        super(c);
    }

    public void treatHDLPacketForDevice(HdlPacket p) {
        switch (p.commandType) {
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
     * Sets the DeviceType for this touch panel.
     *
     *
     */
    void setType(DeviceType type) {
        this.deviceType = type;
    }

    /**
     * Sets the ButtonValue value for touch panel.
     *
     * @param OnOff Value of the Button1
     */
    public void setbutton1Value(OnOffType value) {
        if (this.button1 != value) {
            setUpdated(true);
        }
        this.button1 = value;
    }

    /**
     * the button1 Value as <code>OnOffType</code>
     */
    public OnOffType getbutton1Value() {
        return button1;
    }

    /**
     * Sets the ButtonValue value for touch panel.
     *
     * @param OnOff Value of the Button2
     */
    public void setbutton2Value(OnOffType value) {
        if (this.button2 != value) {
            setUpdated(true);
        }
        this.button2 = value;
    }

    /**
     * the button2 Value as <code>OnOffType</code>
     */
    public OnOffType getbutton2Value() {
        return button2;
    }

    /**
     * Sets the ButtonValue value for touch panel.
     *
     * @param OnOff Value of the Button3
     */
    public void setbutton3Value(OnOffType value) {
        if (this.button3 != value) {
            setUpdated(true);
        }
        this.button3 = value;
    }

    /**
     * the button2 Value as <code>OnOffType</code>
     */
    public OnOffType getbutton3Value() {
        return button3;
    }

    /**
     * Sets the ButtonValue value for touch panel.
     *
     * @param OnOff Value of the Button4
     */
    public void setbutton4Value(OnOffType value) {
        if (this.button4 != value) {
            setUpdated(true);
        }
        this.button4 = value;
    }

    /**
     * the button2 Value as <code>OnOffType</code>
     */
    public OnOffType getbutton4Value() {
        return button4;
    }

}
