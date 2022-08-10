/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.ActionType;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc1.NhcAction1;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcAction2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcAction} class represents the action Niko Home Control communication object. It contains all fields
 * representing a Niko Home Control action and has methods to trigger the action in Niko Home Control and receive action
 * updates. Specific implementation are {@link NhcAction1} and {@link NhcAction2}.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public abstract class NhcAction {

    private final Logger logger = LoggerFactory.getLogger(NhcAction.class);

    protected NikoHomeControlCommunication nhcComm;

    protected String id;
    protected String name;
    protected ActionType type;
    protected @Nullable String location;
    protected volatile int state;
    protected volatile int closeTime = 0;
    protected volatile int openTime = 0;

    @Nullable
    private NhcActionEvent eventHandler;

    protected NhcAction(String id, String name, ActionType type, @Nullable String location,
            NikoHomeControlCommunication nhcComm) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.nhcComm = nhcComm;
    }

    /**
     * This method should be called when an object implementing the {@NhcActionEvent} interface is initialized.
     * It keeps a record of the event handler in that object so it can be updated when the action receives an update
     * from the Niko Home Control IP-interface.
     *
     * @param eventHandler
     */
    public void setEventHandler(NhcActionEvent eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * This method should be called when an object implementing the {@NhcActionEvent} interface is disposed.
     * It resets the reference, so no updates go to the handler anymore.
     *
     */
    public void unsetEventHandler() {
        this.eventHandler = null;
    }

    /**
     * Get id of action.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Get name of action.
     *
     * @return action name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of action.
     *
     * @param name action name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get type of action identified.
     * <p>
     * ActionType can be RELAY (for simple light or socket switch), DIMMER, ROLLERSHUTTER, TRIGGER or GENERIC.
     *
     * @return {@link ActionType}
     */
    public ActionType getType() {
        return type;
    }

    /**
     * Get location name of action.
     *
     * @return location name
     */
    public @Nullable String getLocation() {
        return location;
    }

    /**
     * Set location name of action.
     *
     * @param location action location name
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    /**
     * Get state of action.
     * <p>
     * State is a value between 0 and 100 for a dimmer or rollershutter.
     * Rollershutter state is 0 for fully closed and 100 for fully open.
     * State is 0 or 100 for a switch.
     *
     * @return action state
     */
    public int getState() {
        return state;
    }

    /**
     * Set openTime and closeTime for rollershutter action.
     * <p>
     * Time is in seconds to fully open or close a rollershutter.
     *
     * @param openTime
     * @param closeTime
     */
    public void setShutterTimes(int openTime, int closeTime) {
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    /**
     * Get openTime of action.
     * <p>
     * openTime is the time in seconds to fully open a rollershutter.
     *
     * @return action openTime
     */
    public int getOpenTime() {
        return openTime;
    }

    /**
     * Get closeTime of action.
     * <p>
     * closeTime is the time in seconds to fully close a rollershutter.
     *
     * @return action closeTime
     */
    public int getCloseTime() {
        return closeTime;
    }

    protected void updateState() {
        updateState(state);
    }

    protected void updateState(int state) {
        NhcActionEvent eventHandler = this.eventHandler;
        if (eventHandler != null) {
            logger.debug("update channel state for {} with {}", id, state);
            eventHandler.actionEvent(state);
        }
    }

    /**
     * Method called when action is removed from the Niko Home Control Controller.
     */
    public void actionRemoved() {
        logger.debug("action removed {}, {}", id, name);
        NhcActionEvent eventHandler = this.eventHandler;
        if (eventHandler != null) {
            eventHandler.actionRemoved();
            unsetEventHandler();
        }
    }

    /**
     * Sets state of action. This method is implemented in {@link NhcAction1} and {@link NhcAction2}.
     *
     * @param state - The allowed values depend on the action type.
     *            switch action: 0 or 100
     *            dimmer action: between 0 and 100
     *            rollershutter action: between 0 and 100
     */
    public abstract void setState(int state);

    /**
     * Sends action to Niko Home Control. This method is implemented in {@link NhcAction1} and {@link NhcAction2}.
     *
     * @param command - The allowed values depend on the action type.
     *            switch action: On or Off
     *            dimmer action: between 0 and 100, On or Off
     *            rollershutter action: between 0 and 100, Up, Down or Stop
     */
    public abstract void execute(String command);
}
