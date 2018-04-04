/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.io.IOException;

import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;

/**
 * This class represents a mood belonging to a {@link LxControlMood} object.
 * A mood is effectively a switch. When the switch is set to ON, mood is active and mixed into a set of active
 * moods.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlMood extends LxControlSwitch {
    private Integer moodId;
    private Boolean isStatic;
    private LxControlLightControllerV2 controller;

    /**
     * Create a control representing a single mood of a light controller V2.
     *
     * @param client
     *            communication client used to send commands to the Miniserver
     * @param uuid
     *            controller's UUID
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            room to which mood belongs
     * @param category
     *            category to which mood belongs
     * @param moodId
     *            mood ID withing the controller (received from the Miniserver)
     * @param isStatic
     *            true if this mood is static and can't be deleted or modified in any way
     * @param controller
     *            light controller that this mood belongs to
     */
    LxControlMood(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category,
            Integer moodId, Boolean isStatic, LxControlLightControllerV2 controller) {
        super(client, uuid, json, room, category);
        this.moodId = moodId;
        this.isStatic = isStatic;
        this.controller = controller;
    }

    /**
     * Get an ID of this mood. ID indentifies the mood within a light controller.
     * It is equal to the mood ID received from the Miniserver.
     *
     * @return
     *         mood ID
     */
    public Integer getId() {
        return moodId;
    }

    /**
     * Returns if mood is statically-defined 'all off' mood.
     * Setting 'all off' mood on the Miniserver switches all outputs off, but it can't be mixed with other moods.
     * Attempt to mix it results in no change in the active moods list.
     *
     * @return
     *         true if this is static all outputs off mood
     */
    public boolean isAllOffMood() {
        // currently the API does not give a hint how to figure out the 'all off' mood
        // empirically this is the only mood that is not editable by the user and has a static flag set on
        // we will assume that the only static mood is 'all off' mood
        return isStatic == null ? false : isStatic;
    }

    /**
     * Get controller's UUID that the mood belongs to.
     *
     * @return UUID of the lighting controller
     */
    public LxUuid getControllerUuid() {
        return LxControlMood.this.getUuid();
    }

    /**
     * Mix the mood into active moods.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    @Override
    public void on() throws IOException {
        controller.addMood(moodId);
    }

    /**
     * Mix the mood out of active moods.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    @Override
    public void off() throws IOException {
        controller.removeMood(moodId);
    }

    /**
     * Return whether the mood is active of not.
     *
     * @return
     *         1 if mood is active and 0 otherwise
     */
    @Override
    public Double getState() {
        if (controller.isMoodActive(moodId)) {
            return 1.0;
        }
        return 0.0;
    }
}
