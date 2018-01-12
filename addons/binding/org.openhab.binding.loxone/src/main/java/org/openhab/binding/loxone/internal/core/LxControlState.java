/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.util.ArrayList;
import java.util.List;

/**
 * A state of a Loxone control ({@link LxControl})
 * <p>
 * Each control object may have a number of states defined, that describe the overall condition of the control.
 * List of states is read from LoxApp3.json configuration file.
 * <p>
 * Each state is identified by its own UUID and a name of the state. Names are proprietary to a particular type of the
 * control and as such are defined in {@link LxControl} child classes implementation (e.g. {@link LxControlSwitch}
 * Objects of this class are used to bind state updates received from the Miniserver to a control object.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlState {
    private LxUuid uuid;
    private String name;
    private Double value;
    private String textValue;
    private LxControl control;
    private List<LxControlStateListener> listeners = new ArrayList<>();

    /**
     * Create a control state object.
     *
     * @param uuid
     *            UUID of the state
     * @param name
     *            name of the state
     * @param control
     *            control to which this state belongs
     */
    LxControlState(LxUuid uuid, String name, LxControl control) {
        this.uuid = uuid;
        this.name = name;
        this.control = control;
        uuid.setUpdate(true);
    }

    /**
     * Sets current value of the control's state
     *
     * @param value
     *            current state's value to set
     * @param textValue
     *            current state's text value to set
     */
    void setValue(Double value, String textValue) {
        boolean changed = false;

        uuid.setUpdate(true);

        if (value != null && !value.equals(this.value)) {
            this.value = value;
            changed = true;
        }

        if (textValue != null && !textValue.equals(this.textValue)) {
            this.textValue = textValue;
            changed = true;
        }

        if (changed) {
            for (LxControlStateListener listener : listeners) {
                listener.onStateChange(this);
            }
        }
    }

    /**
     * Sets current text value of the control's state
     *
     * @param value
     *            new text message value
     */
    void setValue(String value) {
        uuid.setUpdate(true);
    }

    /**
     * Gets current value of the control's state
     *
     * @return
     *         current state's value
     */
    Double getValue() {
        return value;
    }

    /**
     * Gets current value of the control's state
     *
     * @return
     *         current state's value
     */
    String getTextValue() {
        return textValue;
    }

    /**
     * Gets control to which state belongs
     *
     * @return
     *         state's control object
     */
    LxControl getControl() {
        return control;
    }

    /**
     * Gets state's name.
     * <p>
     * State's name is proprietary per control type.
     *
     * @return
     *         state's name
     */
    String getName() {
        return name;
    }

    /**
     * Sets state's name
     *
     * @param name
     *            state's name
     */
    void setName(String name) {
        this.name = name;
        uuid.setUpdate(true);
    }

    /**
     * Gets UUID of the state
     *
     * @return
     *         state's UUID
     */
    LxUuid getUuid() {
        return uuid;
    }

    /**
     * Adds a listener to state changes
     *
     * @param listener
     *            an object implementing state change listener interface
     */
    void addListener(LxControlStateListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener of state changes
     *
     * @param listener
     *            listener object to remove
     */
    void removeListener(LxControlStateListener listener) {
        listeners.remove(listener);
    }

}
