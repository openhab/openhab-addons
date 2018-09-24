/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.internal.model;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaEvent} holds information about Tahoma
 * event.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaEvent {
    private String name;
    private String deviceURL;
    private ArrayList<SomfyTahomaState> deviceStates;

    public String getName() {
        return name;
    }

    public String getDeviceUrl() {
        return deviceURL;
    }

    public ArrayList<SomfyTahomaState> getDeviceStates() {
        return deviceStates;
    }
}
