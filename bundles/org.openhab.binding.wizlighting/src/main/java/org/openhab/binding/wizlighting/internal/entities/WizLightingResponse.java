/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import com.google.gson.annotations.Expose;

/**
 * This POJO represents one WiZ Lighting Response
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizLightingResponse {

    // The IP address we're coming from
    @Expose(serialize = false, deserialize = true)
    private String wizResponseIpAddress = "bulbIPAddres";

    // Increasing numeric value.
    // Bulb doesn't seem to care if it receives the same id multiple time
    // or commands with lower numbers after higher ones.
    @Expose(serialize = true, deserialize = true)
    private int id;
    // Not sure what env is - value always seems to be "pro"
    @Expose(serialize = true, deserialize = true)
    private @Nullable String env;

    // An error response
    @Expose(serialize = true, deserialize = true)
    private @Nullable ErrorResponseResult methodError;

    // The method being used - see the enum for details
    // We're setting this to "unknown"
    @Expose(serialize = true, deserialize = true)
    private WizLightingMethodType method = WizLightingMethodType.unknownMethod;

    // The MAC address the response is coming from
    @Expose(serialize = true, deserialize = true)
    private String mac = "bulbMacAddress";

    // Whether or not a command succeeded (if the response is from a command)
    @Expose(serialize = true, deserialize = true)
    private boolean success = false;

    // The system configuration result, if present
    @Expose(serialize = true, deserialize = true)
    private @Nullable SystemConfigResult configResult;

    // The parameters or result of a command/response
    // A "result" is generally returned when solicited using a set/get method and a
    // "params" is retuned with an unsolicited sync/heartbeat. The result returned
    // from a get method is generally identical to the params returned in the
    // heartbeat.
    @Expose(serialize = true, deserialize = true)
    private @Nullable WizLightingSyncState params;

    /**
     * Setters and Getters
     */

    public @Nullable SystemConfigResult getSystemConfigResults() {
        return this.configResult;
    }

    public void setSystemConfigResult(final SystemConfigResult configResult) {
        this.configResult = configResult;
    }

    public boolean getResultSuccess() {
        return this.success;
    }

    public void setResultSucess(final boolean success) {
        this.success = success;
    }

    public @Nullable WizLightingSyncState getSyncState() {
        return this.params;
    }

    public void setSyncParams(final WizLightingSyncState params) {
        this.params = params;
    }

    public String getWizResponseMacAddress() {
        return this.mac;
    }

    public void setWizResponseMacAddress(final String wizResponseMacAddress) {
        this.mac = wizResponseMacAddress;
    }

    public String getWizResponseIpAddress() {
        return this.wizResponseIpAddress;
    }

    public void setWizResponseIpAddress(final String wizResponseIpAddress) {
        this.wizResponseIpAddress = wizResponseIpAddress;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public @Nullable WizLightingMethodType getMethod() {
        return method;
    }

    public void setMethod(final WizLightingMethodType method) {
        this.method = method;
    }

    public @Nullable String getEnv() {
        return env;
    }

    public void setEnv(final String env) {
        this.env = env;
    }

    public @Nullable ErrorResponseResult getError() {
        return methodError;
    }

    public void setError(ErrorResponseResult error) {
        this.methodError = error;
    }
}
