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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import static org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcAction2} class represents the action Niko Home Control II communication object. It contains all fields
 * representing a Niko Home Control action and has methods to trigger the action in Niko Home Control and receive action
 * updates.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcAction2 extends NhcAction {

    private final Logger logger = LoggerFactory.getLogger(NhcAction2.class);

    private volatile boolean booleanState;
    private String deviceType;
    private String deviceTechnology;
    private String deviceModel;

    NhcAction2(String id, String name, String deviceType, String deviceTechnology, String deviceModel,
            @Nullable String location, ActionType type, NikoHomeControlCommunication nhcComm) {
        super(id, name, type, location, nhcComm);
        this.deviceType = deviceType;
        this.deviceTechnology = deviceTechnology;
        this.deviceModel = deviceModel;
    }

    /**
     * Get on/off state of action.
     * <p>
     * true for on, false for off
     *
     * @return action on/off state
     */
    boolean booleanState() {
        return booleanState;
    }

    @Override
    public int getState() {
        return booleanState ? state : 0;
    }

    /**
     * Sets on/off state of action.
     *
     * @param state - boolean false for on, true for off
     */
    public void setBooleanState(boolean state) {
        booleanState = state;
        if (getType().equals(ActionType.DIMMER)) {
            if (booleanState) {
                // only send stored brightness value if on
                updateState();
            } else {
                updateState(0);
            }
        } else {
            if (booleanState) {
                this.state = 100;
                updateState(100);
            } else {
                this.state = 0;
                updateState(0);
            }
        }
    }

    /**
     * Sets state of action. This version is used for Niko Home Control II.
     *
     * @param state - The allowed values depend on the action type.
     *            switch action: 0 or 100
     *            dimmer action: between 0 and 100
     *            rollershutter action: between 0 and 100
     */
    @Override
    public void setState(int state) {
        this.state = state;
        if (getType().equals(ActionType.DIMMER)) { // for dimmers, only send the update to the event
                                                   // handler if on
            if (booleanState) {
                updateState();
            }
        } else {
            updateState();
        }
    }

    /**
     * Sends action to Niko Home Control. This version is used for Niko Home Control II, that has extra status options.
     *
     * @param command - The allowed values depend on the action type.
     *            switch action: On or Off
     *            dimmer action: between 0 and 100, On or Off
     *            rollershutter action: between 0 and 100, Up, Down or Stop
     */
    @Override
    public void execute(String command) {
        logger.debug("execute action {} of type {} for {}", command, type, id);

        String cmd;
        if ("flag".equals(deviceModel)) {
            cmd = NHCON.equals(command) ? NHCTRUE : NHCFALSE;
        } else {
            cmd = command;
        }

        nhcComm.executeAction(id, cmd);
    }

    /**
     * @return type as returned from Niko Home Control
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * @return technology as returned from Niko Home Control
     */
    public String getDeviceTechnology() {
        return deviceTechnology;
    }

    /**
     * @return model as returned from Niko Home Control
     */
    public String getDeviceModel() {
        return deviceModel;
    }
}
