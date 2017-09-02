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
 * The {@link SomfyTahomaSetup} holds information about devices bound
 * to TahomaLink account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaSetup {

    private ArrayList<SomfyTahomaDevice> devices;

    private ArrayList<SomfyTahomaGateway> gateways;

    public ArrayList<SomfyTahomaDevice> getDevices() {
        return devices;
    }

    public ArrayList<SomfyTahomaGateway> getGateways() {
        return gateways;
    }
}
