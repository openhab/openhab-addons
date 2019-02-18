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
package org.openhab.binding.somfytahoma.internal.model;

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
