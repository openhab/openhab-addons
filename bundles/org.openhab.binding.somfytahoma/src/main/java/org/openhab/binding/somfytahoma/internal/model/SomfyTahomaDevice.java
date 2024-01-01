/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SomfyTahomaDevice} holds information about a device bound
 * to TahomaLink account.
 *
 * @author Ondrej Pecta - Initial contribution
 * @author Laurent Garnier - Add attributes data
 */
@NonNullByDefault
public class SomfyTahomaDevice {

    private String deviceURL = "";
    private String label = "";
    private String oid = "";
    private SomfyTahomaDeviceDefinition definition = new SomfyTahomaDeviceDefinition();
    private List<SomfyTahomaState> states = new ArrayList<>();
    private List<SomfyTahomaState> attributes = new ArrayList<>();
    private String placeOID = "";

    public String getLabel() {
        return label;
    }

    public String getDeviceURL() {
        return deviceURL;
    }

    public String getOid() {
        return oid;
    }

    public SomfyTahomaDeviceDefinition getDefinition() {
        return definition;
    }

    public List<SomfyTahomaState> getStates() {
        return states;
    }

    public List<SomfyTahomaState> getAttributes() {
        return attributes;
    }

    public String getPlaceOID() {
        return placeOID;
    }

    public void setPlaceOID(String placeOID) {
        this.placeOID = placeOID;
    }
}
