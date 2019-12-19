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
package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.smarthome.core.library.types.OnOffType;

/**
 * This POJO represents State Request Param
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class StateRequestParam implements Param {
    private boolean state; // true = ON, false = OFF

    public StateRequestParam(OnOffType command) {
        if (command == OnOffType.ON) {
            state = true;
        } else {
            state = false;
        }
    }

    public StateRequestParam(boolean state) {
        this.state = state;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
