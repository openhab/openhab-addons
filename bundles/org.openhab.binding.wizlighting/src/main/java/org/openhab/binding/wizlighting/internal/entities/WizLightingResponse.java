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
package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This POJO represents one WiZ Lighting Response
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizLightingResponse {

    // Increasing numeric value.
    // Bulb doesn't seem to care if it receives the same id multiple time
    // or commands with lower numbers after higher ones.
    private int id;
    // Not sure what env is - value always seems to be "pro"
    private @Nullable String env;
    // The method being used - see the enum for details
    private @Nullable String method;

    // The parameters or result of a command/response
    private @Nullable SyncResponseParam params;
    private @Nullable CommandResponseResult result;

    private @Nullable String wizResponseIpAddress;

    public WizLightingResponse() {
    }

    public @Nullable String getWizResponseMacAddress() {
        SyncResponseParam params = this.getParams();
        CommandResponseResult result = this.getResult();
        if (params != null) {
            return params.mac;
        } else if (result != null) {
            return result.mac;
        } else {
            return null;
        }
    }

    public void setWizResponseMacAddress(final String wizResponseMacAddress) {
        SyncResponseParam params = this.getParams();
        CommandResponseResult result = this.getResult();
        if (params != null) {
            params.mac = wizResponseMacAddress;
        } else if (result != null) {
            result.mac = wizResponseMacAddress;
        }
    }

    public @Nullable String getWizResponseIpAddress() {
        return this.wizResponseIpAddress;
    }

    public void setWizResponseIpAddress(final String wizResponseIpAddress) {
        this.wizResponseIpAddress = wizResponseIpAddress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public @Nullable String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public @Nullable String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public @Nullable SyncResponseParam getParams() {
        return params;
    }

    public void setParams(SyncResponseParam params) {
        this.params = params;
    }

    public @Nullable CommandResponseResult getResult() {
        return result;
    }

    public void setResult(CommandResponseResult result) {
        this.result = result;
    }
}
