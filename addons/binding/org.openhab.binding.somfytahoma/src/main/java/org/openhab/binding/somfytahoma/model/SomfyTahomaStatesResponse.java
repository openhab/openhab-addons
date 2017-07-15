/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.model;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaStatesResponse} holds information about
 * response to getting device's states command.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaStatesResponse {

    private ArrayList<SomfyTahomaDeviceWithState> devices;

    public ArrayList<SomfyTahomaDeviceWithState> getDevices() {
        return devices;
    }
}
