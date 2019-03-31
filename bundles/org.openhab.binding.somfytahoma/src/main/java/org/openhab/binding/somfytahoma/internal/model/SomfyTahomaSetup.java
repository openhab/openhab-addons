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

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaSetup} holds information about devices bound
 * to TahomaLink account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaSetup {

    private ArrayList<SomfyTahomaDevice> devices = new ArrayList<>();

    private ArrayList<SomfyTahomaGateway> gateways = new ArrayList<>();

    public ArrayList<SomfyTahomaDevice> getDevices() {
        return devices;
    }

    public ArrayList<SomfyTahomaGateway> getGateways() {
        return gateways;
    }
}
