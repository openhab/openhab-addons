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
 * The {@link SomfyTahomaDevice} holds information about a device bound
 * to TahomaLink account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaDevice {

    private String uiClass = "";
    private String deviceURL = "";
    private String label = "";
    private String oid = "";
    private SomfyTahomaDeviceDefinition definition = new SomfyTahomaDeviceDefinition();
    private ArrayList<SomfyTahomaState> states = new ArrayList<>();

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
