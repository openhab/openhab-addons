/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
