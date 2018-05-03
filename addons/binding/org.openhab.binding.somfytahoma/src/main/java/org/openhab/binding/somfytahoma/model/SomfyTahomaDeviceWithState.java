/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.model;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaDeviceWithState} holds information about a device
 * with state bound to TahomaLink account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaDeviceWithState {

    private ArrayList<SomfyTahomaState> states;

    public boolean hasStates() {
        return states != null && states.size() > 0;
    }

    public ArrayList<SomfyTahomaState> getStates() {
        return states;
    }
}
