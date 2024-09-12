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
package org.openhab.binding.miele.internal.api.dto;

import org.openhab.binding.miele.internal.FullyQualifiedApplianceIdentifier;
import org.openhab.binding.miele.internal.MieleBindingConstants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HomeDevice} class represents the HomeDevice node in the response JSON.
 *
 * @author Jacob Laursen - Initial contribution
 **/
public class HomeDevice {

    private static final String MIELE_APPLIANCE_CLASS = "com.miele.xgw3000.gateway.hdm.deviceclasses.MieleAppliance";

    public String Name;
    public String Status;
    public String ParentUID;
    public String ProtocolAdapterName;
    public String Vendor;
    public String UID;
    public String Type;
    public JsonArray DeviceClasses;
    public String Version;
    public String TimestampAdded;
    public JsonObject Error;
    public JsonObject Properties;

    public HomeDevice() {
    }

    public FullyQualifiedApplianceIdentifier getApplianceIdentifier() {
        return new FullyQualifiedApplianceIdentifier(this.UID);
    }

    public String getSerialNumber() {
        return Properties.get("serial.number").getAsString();
    }

    public String getFirmwareVersion() {
        return Properties.get("firmware.version").getAsString();
    }

    public String getRemoteUid() {
        JsonElement remoteUid = Properties.get("remote.uid");
        if (remoteUid == null) {
            // remote.uid and serial.number seems to be the same. If remote.uid
            // is missing for some reason, it makes sense to provide fallback
            // to serial number.
            return getSerialNumber();
        }
        return remoteUid.getAsString();
    }

    public String getConnectionType() {
        JsonElement connectionType = Properties.get("connection.type");
        if (connectionType == null) {
            return null;
        }
        return connectionType.getAsString();
    }

    public String getConnectionBaudRate() {
        JsonElement baudRate = Properties.get("connection.baud.rate");
        if (baudRate == null) {
            return null;
        }
        return baudRate.getAsString();
    }

    public String getApplianceModel() {
        JsonElement model = Properties.get("miele.model");
        if (model == null) {
            return "";
        }
        return model.getAsString();
    }

    public String getDeviceClass() {
        for (JsonElement dc : DeviceClasses) {
            String dcStr = dc.getAsString();
            if (dcStr.contains(MieleBindingConstants.MIELE_CLASS) && !dcStr.equals(MIELE_APPLIANCE_CLASS)) {
                return dcStr.substring(MieleBindingConstants.MIELE_CLASS.length());
            }
        }
        return null;
    }
}
