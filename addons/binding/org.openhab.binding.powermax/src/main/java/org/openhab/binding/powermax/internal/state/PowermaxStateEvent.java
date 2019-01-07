/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.state;

import java.util.EventObject;

/**
 * Event for state received from the Visonic alarm panel
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxStateEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private PowermaxState state;

    public PowermaxStateEvent(Object source, PowermaxState state) {
        super(source);
        this.state = state;
    }

    /**
     * @return the state object built from the received message
     */
    public PowermaxState getState() {
        return state;
    }

}
