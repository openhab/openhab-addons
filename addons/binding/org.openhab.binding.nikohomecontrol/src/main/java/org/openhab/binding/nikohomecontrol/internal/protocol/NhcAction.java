/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.protocol;

import org.openhab.binding.nikohomecontrol.handler.NikoHomeControlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcAction} class represents the action Niko Home Control communication object. It contains all fields
 * representing a Niko Home Control action and has methods to trigger the action in Niko Home Control and receive action
 * updates.
 *
 * @author Mark Herwege
 */
public final class NhcAction {

    private final Logger logger = LoggerFactory.getLogger(NhcAction.class);

    private NikoHomeControlCommunication nhcComm;

    private int id;
    private String name;
    private Integer type;
    private String location;
    private Integer state;

    private NikoHomeControlHandler thingHandler;

    NhcAction(int id, String name, Integer type, String location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
    }

    /**
     * This method should be called if the ThingHandler for the thing corresponding to this action is initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * the action receives an update from the Niko Home Control IP-interface.
     *
     * @param handler
     */
    public void setThingHandler(NikoHomeControlHandler handler) {
        this.thingHandler = handler;
    }

    /**
     * This method sets a pointer to the nhcComm object of class {@link NikoHomeControlCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Niko Home Control IP-interface when..
     *
     * @param nhcComm
     */
    public void setNhcComm(NikoHomeControlCommunication nhcComm) {
        this.nhcComm = nhcComm;
    }

    /**
     * Get name of action.
     *
     * @return action name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get type of action identified.
     * <p>
     * Action type is 0 or 1 for a switch, 2 for a dimmer, 3 or 4 for a rollershutter.
     *
     * @return action type
     */
    public Integer getType() {
        return this.type;
    }

    /**
     * Get location name of action.
     *
     * @return location name
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Get state of action.
     * <p>
     * State is a value between 0 and 100 for a dimmer or rollershutter.
     * State is 0 or 100 for a switch.
     *
     * @return action state
     */
    public Integer getState() {
        return this.state;
    }

    /**
     * Sets state of action.
     * <p>
     * State is a value between 0 and 100 for a dimmer or rollershutter.
     * State is 0 or 100 for a switch.
     * If a thing handler is registered for the action, send a state update through the handler.
     * This method should only be called from inside this package.
     *
     * @param state
     */
    void setState(int state) {
        this.state = state;
        if (thingHandler != null) {
            logger.debug("Niko Home Control: update channel state for {} with {}", id, state);
            thingHandler.handleStateUpdate(this.type, state);
        }
    }

    /**
     * Sends action to Niko Home Control.
     *
     * @param percent - The allowed values depend on the action type.
     *            switch action: 0 or 100
     *            dimmer action: between 0 and 100, 254 for on, 255 for off
     *            rollershutter action: between 0 (closed) and 100 (open), 255 to open, 254 to close, 253 to stop
     */
    public void execute(int percent) {
        logger.debug("Niko Home Control: execute action {} of type {} for {}", percent, this.type, this.id);

        NhcMessageCmd nhcCmd = new NhcMessageCmd("executeactions", this.id, percent);

        // rollershutters have extra fields in the command
        if ((this.type == 4) || (this.type == 5)) {
            switch (percent) {
                case 255: // open
                    nhcCmd.setEndValue(100);
                    break;
                case 254: // close
                    nhcCmd.setStartValue(100);
                    break;
                case 253: // stop
                    nhcCmd.setStartValue(getState());
            }
        }

        nhcComm.sendMessage(nhcCmd);
    }
}
