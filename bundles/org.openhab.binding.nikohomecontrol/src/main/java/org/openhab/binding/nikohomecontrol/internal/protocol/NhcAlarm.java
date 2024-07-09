/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcAlarm} class represents the alarm control Niko Home Control communication object. It contains all
 * fields representing a Niko Home Control alarm control device and has methods to arm/disarm in Niko Home Control
 * and receive alarms. A specific implementation is {@link
 * org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcAlarm2}.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public abstract class NhcAlarm {
    private final Logger logger = LoggerFactory.getLogger(NhcAlarm.class);

    protected NikoHomeControlCommunication nhcComm;

    protected final String id;
    protected String name;
    protected @Nullable String location;

    protected volatile String state = "";

    @Nullable
    private NhcAlarmEvent eventHandler;

    protected NhcAlarm(String id, String name, @Nullable String location, NikoHomeControlCommunication nhcComm) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.nhcComm = nhcComm;
    }

    /**
     * This method should be called when an object implementing the {@NhcAlarmEvent} interface is initialized.
     * It keeps a record of the event handler in that object so it can be updated when the alarm control device
     * receives an update from the Niko Home Control IP-interface.
     *
     * @param eventHandler
     */
    public void setEventHandler(NhcAlarmEvent eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * This method should be called when an object implementing the {@NhcAlarmEvent} interface is disposed.
     * It resets the reference, so no updates go to the handler anymore.
     */
    public void unsetEventHandler() {
        this.eventHandler = null;
    }

    /**
     * Get the id of the alarm control device.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Get name of the alarm control device.
     *
     * @return alarm control name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of the alarm control device.
     *
     * @param name alarm control name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get location name of alarm control device.
     *
     * @return location name
     */
    public @Nullable String getLocation() {
        return location;
    }

    /**
     * Set location of the alarm control device.
     *
     * @param location alarm control location
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    /**
     * Get state of the alarm control device.
     *
     * @return action state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets state of alarm.
     *
     * @param state: Off, PreArmed, DetectorProblem, Armed, PreAlarm or Alarm
     */
    public void setState(String state) {
        this.state = state;
        updateState();
    }

    /**
     * Send update of alarm state through event handler to subscribers.
     */
    public void updateState() {
        NhcAlarmEvent eventHandler = this.eventHandler;
        if (eventHandler != null) {
            logger.debug("update channel state for {} with {}", id, state);
            eventHandler.alarmEvent(state);
        }
    }

    /**
     * Send alarm trigger through event handler to subscribers.
     */
    public void triggerAlarm() {
        NhcAlarmEvent eventHandler = this.eventHandler;
        if (eventHandler != null) {
            logger.debug("trigger alarm for {}", id);
            eventHandler.alarmTriggerEvent();
        }
    }

    /**
     * Method called when alarm control device is removed from the Niko Home Control Controller.
     */
    public void alarmDeviceRemoved() {
        logger.debug("alarm device removed {}, {}", id, name);
        NhcAlarmEvent eventHandler = this.eventHandler;
        if (eventHandler != null) {
            eventHandler.deviceRemoved();
            unsetEventHandler();
        }
    }

    public void executeArm() {
        logger.debug("arm the alarm with id {}", id);
        nhcComm.executeArm(id);
    }

    public void executeDisarm() {
        logger.debug("disarm the alarm with id {}", id);
        nhcComm.executeDisarm(id);
    }
}
