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
package org.openhab.binding.loxone.internal.controls;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.core.LxCategory;
import org.openhab.binding.loxone.internal.core.LxContainer;
import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;
import org.openhab.binding.loxone.internal.core.LxUuid;

/**
 * This class represents a mood belonging to a {@link LxControlMood} object.
 * A mood is effectively a switch. When the switch is set to ON, mood is active and mixed into a set of active
 * moods.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlMood extends LxControlSwitch {
    private final Integer moodId;
    private final LxControlLightControllerV2 controller;

    /**
     * Create a control representing a single mood of a light controller V2.
     *
     * @param handlerApi thing handler object representing the Miniserver
     * @param uuid       controller's UUID
     * @param json       JSON describing the control as received from the Miniserver
     * @param room       room to which mood belongs
     * @param category   category to which mood belongs
     * @param moodId     mood ID within the controller (received from the Miniserver)
     * @param isStatic   true if this mood is static and can't be deleted or modified in any way
     * @param controller light controller that this mood belongs to
     */
    LxControlMood(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category,
            Integer moodId, Boolean isStatic, LxControlLightControllerV2 controller) {
        super(handlerApi, uuid, json, room, category);
        this.moodId = moodId;
        this.controller = controller;
        // the 'all off' mood can't be operated as a switch, but needs to be present on the moods list for the
        // lighting controller
        // currently the API does not give a hint how to figure out the 'all off' mood
        // empirically this is the only mood that is not editable by the user and has a static flag set on
        // we will assume that the only static mood is 'all off' mood
        if (isStatic != null && isStatic) {
            channels.clear();
        }
    }

    @Override
    String getLabel() {
        return "Mood / " + super.getLabel();
    }

    /**
     * Get an ID of this mood. ID identifies the mood within a light controller.
     * It is equal to the mood ID received from the Miniserver.
     *
     * @return mood ID
     */
    Integer getId() {
        return moodId;
    }

    /**
     * Mix the mood into active moods.
     *
     * @throws IOException when something went wrong with communication
     */
    @Override
    void on() throws IOException {
        controller.addMood(moodId);
    }

    /**
     * Mix the mood out of active moods.
     *
     * @throws IOException when something went wrong with communication
     */
    @Override
    void off() throws IOException {
        controller.removeMood(moodId);
    }

    /**
     * Return whether the mood is active of not.
     *
     * @return 1 if mood is active and 0 otherwise
     */
    @Override
    OnOffType getState() {
        if (controller.isMoodOk(moodId)) {
            if (controller.isMoodActive(moodId)) {
                return OnOffType.ON;
            }
            return OnOffType.OFF;
        }
        return null;
    }
}
