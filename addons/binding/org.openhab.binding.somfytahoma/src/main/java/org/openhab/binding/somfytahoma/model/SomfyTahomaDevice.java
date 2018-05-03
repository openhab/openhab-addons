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
 * The {@link SomfyTahomaDevice} holds information about a device bound
 * to TahomaLink account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaDevice {

    private String uiClass;
    private String deviceURL;
    private String label;
    private String oid;
    private SomfyTahomaDeviceDefinition definition;
    private ArrayList<SomfyTahomaState> states;

    public String getLabel() {
        return label;
    }

    public String getDeviceURL() {
        return deviceURL;
    }

    public String getOid() {
        return oid;
    }

    public String getUiClass() {
        return uiClass;
    }

    public SomfyTahomaDeviceDefinition getDefinition() {
        return definition;
    }

    public ArrayList<SomfyTahomaState> getStates() {
        return states;
    }
}
