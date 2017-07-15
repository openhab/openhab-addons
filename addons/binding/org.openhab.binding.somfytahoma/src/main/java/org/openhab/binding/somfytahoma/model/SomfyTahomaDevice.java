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
 * The {@link SomfyTahomaDevice} holds information about a device bound
 * to TahomaLink account.
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaDevice {

    private String uiClass;
    private String deviceURL;
    private String label;
    private String oid;

    public boolean isRollerShutter() {
        return uiClass.equals("RollerShutter");
    }

    public boolean isAwning() {
        return uiClass.equals("Awning");
    }

    public boolean isOnOff() {
        return uiClass.equals("OnOff");
    }

    public String getLabel() {
        return label;
    }

    public String getDeviceURL() {
        return deviceURL;
    }

    public String getOid() {
        return oid;
    }
}
