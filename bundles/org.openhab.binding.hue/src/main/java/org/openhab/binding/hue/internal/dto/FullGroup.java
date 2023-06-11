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
package org.openhab.binding.hue.internal.dto;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

/**
 * Detailed group information.
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 * @author Laurent Garnier - field state added
 */
public class FullGroup extends Group {
    public static final Type GSON_TYPE = new TypeToken<Map<String, FullGroup>>() {
    }.getType();

    private State action;
    private List<String> lights;
    private State groupState; // Will not be set by hue API

    FullGroup() {
        super();
    }

    /**
     * Test constructor
     */
    public FullGroup(String id, String name, String type, State action, List<String> lights, State state) {
        super(id, name, type);
        this.action = action;
        this.lights = lights;
        this.groupState = state;
    }

    /**
     * Returns the last sent state update to the group.
     * This does not have to reflect the current state of the group.
     *
     * @return last state update
     */
    public State getAction() {
        return action;
    }

    /**
     * Returns a list of the lights in the group.
     *
     * @return lights in the group
     */
    public List<String> getLightIds() {
        return lights;
    }

    /**
     * Returns the current state of the group.
     *
     * @return current state
     */
    public State getState() {
        return groupState;
    }

    public void setState(State state) {
        this.groupState = state;
    }
}
