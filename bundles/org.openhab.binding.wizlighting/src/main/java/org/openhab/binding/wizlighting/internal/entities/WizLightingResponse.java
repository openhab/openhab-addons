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
import org.openhab.binding.wizlighting.internal.enums.WizLightingMethodType;

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
    private @Nullable WizLightingMethodType method;

    // The parameters or result of a command/response
    // A "result" is generally returned when solicited using a set/get method and a
    // "params" is retuned with an unsolicited sync/heartbeat. The result returned
    // from a get method is generally identical to the params returned in the
    // heartbeat.
    private @Nullable WizResponseParam params;
    private @Nullable WizResponseParam result;

    private @Nullable String wizResponseIpAddress;

    public WizLightingResponse() {
    }

    public @Nullable String getWizResponseMacAddress() {
        WizResponseParam params = this.getParams();
        WizResponseParam result = this.getResult();
        if (params != null) {
            return params.mac;
        } else if (result != null) {
            return result.mac;
        } else {
            return null;
        }
    }

    public void setWizResponseMacAddress(final String wizResponseMacAddress) {
        WizResponseParam params = this.getParams();
        WizResponseParam result = this.getResult();
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

    public @Nullable WizLightingMethodType getMethod() {
        return method;
    }

    public void setMethod(WizLightingMethodType method) {
        this.method = method;
    }

    public @Nullable String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public @Nullable WizResponseParam getParams() {
        if (params != null) {
            return params;
        } else if (result != null) {
            return result;
        } else {
            return null;
        }
    }

    public void setParams(WizResponseParam params) {
        this.params = params;
    }

    public @Nullable WizResponseParam getResult() {
        if (result != null) {
            return result;
        } else if (params != null) {
            return params;
        } else {
            return null;
        }
    }

    public void setResult(WizResponseParam result) {
        this.result = result;
    }
}
