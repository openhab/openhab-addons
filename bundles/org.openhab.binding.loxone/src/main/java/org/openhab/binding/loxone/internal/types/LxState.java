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
package org.openhab.binding.loxone.internal.types;

import org.openhab.binding.loxone.internal.controls.LxControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A state of a Loxone control ({@link LxControl})
 * <p>
 * Each control object may have a number of states defined, that describe the overall condition of the control.
 * List of states is read from LoxApp3.json configuration file.
 * <p>
 * Each state is identified by its own UUID and a name of the state. Names are proprietary to a particular type of the
 * control and as such are defined in {@link LxControl} child classes implementation.
 * Objects of this class are used to bind state updates received from the Miniserver to a control object.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxState {
    private final LxUuid uuid;
    private final String name;
    private final LxControl control;
    private final Logger logger = LoggerFactory.getLogger(LxState.class);
    private Object stateValue;

    /**
     * Create a control state object.
     *
     * @param uuid UUID of the state
     * @param name name of the state
     * @param control control to which this state belongs
     */
    public LxState(LxUuid uuid, String name, LxControl control) {
        this.uuid = uuid;
        this.name = name;
        this.control = control;
    }

    /**
     * Gets UUID of the state
     *
     * @return state's UUID
     */
    public LxUuid getUuid() {
        return uuid;
    }

    /**
     * Sets current value of the control's state
     *
     * @param value current state's value to set
     */
    public void setStateValue(Object value) {
        logger.debug("State set ({},{}) control ({},{}) value={}", uuid, name, control.getUuid(), control.getName(),
                value);
        if (value != null && !value.equals(this.stateValue)) {
            this.stateValue = value;
            control.onStateChange(this);
        }
    }

    /**
     * Gets current value of the control's state
     *
     * @return current state's value
     */
    public Object getStateValue() {
        return stateValue;
    }

    /**
     * Gets state's name.
     * <p>
     * State's name is proprietary per control type.
     *
     * @return state's name
     */
    public String getName() {
        return name;
    }
}
