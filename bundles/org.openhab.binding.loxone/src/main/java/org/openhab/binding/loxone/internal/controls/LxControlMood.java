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
package org.openhab.binding.loxone.internal.controls;

import java.io.IOException;

import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents a mood belonging to a {@link LxControlMood} object.
 * A mood is effectively a switch. When the switch is set to ON, mood is active and mixed into a set of active
 * moods.
 * A mood is deserialized using a default gson, not like {@link LxControl} which has a proprietary deserialization
 * method.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlMood extends LxControlSwitch {

    /**
     * An ID that uniquely identifies this mood (e.g. inside activeMoods)
     */
    @SerializedName("id")
    private Integer moodId;

    /**
     * Bitmask that tells if the mood is used for a specific purpose in the logic.
     * If it’s not used, it can be removed without affecting the logic on the Miniserver.
     * 0: not used
     * 1: this mood is activated by a movement event
     * 2: a T5 or other inputs activate/deactivate this mood
     */
    @SerializedName("used")
    private Integer isUsed;

    /**
     * Whether or not this mood can be controlled with a t5 input
     */
    @SerializedName("t5")
    private Boolean isT5Controlled;

    /**
     * If a mood is marked as static it cannot be deleted or modified in any way.
     * But it can be moved within and between favorite and additional lists.
     */
    @SerializedName("static")
    private Boolean isStatic;

    private LxControlLightControllerV2 controller;

    /**
     * This constructor will be called by the default JSON deserialization
     */
    LxControlMood() {
        super(null);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
    }

    public void initialize(LxControlConfig config, LxControlLightControllerV2 controller, LxUuid uuid) {
        this.uuid = uuid;
        this.controller = controller;
        super.initialize(config);
        // the 'all off' mood can't be operated as a switch, but needs to be present on the moods list for the
        // lighting controller
        // currently the API does not give a hint how to figure out the 'all off' mood
        // empirically this is the only mood that is not editable by the user and has a static flag set on
        // we will assume that the only static mood is 'all off' mood
        if (isStatic != null && isStatic) {
            removeAllChannels();
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
        if (controller != null) {
            controller.addMood(moodId);
        }
    }

    /**
     * Mix the mood out of active moods.
     *
     * @throws IOException when something went wrong with communication
     */
    @Override
    void off() throws IOException {
        if (controller != null) {
            controller.removeMood(moodId);
        }
    }

    /**
     * Return whether the mood is active of not.
     *
     * @return 1 if mood is active and 0 otherwise
     */
    @Override
    OnOffType getSwitchState() {
        if (controller != null && controller.isMoodOk(moodId)) {
            if (controller.isMoodActive(moodId)) {
                return OnOffType.ON;
            }
            return OnOffType.OFF;
        }
        return null;
    }
}
