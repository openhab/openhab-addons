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
package org.openhab.binding.wiz.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wiz.internal.enums.WizMethodType;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents one WiZ Response
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizResponse {

    // The IP address we're coming from
    @Expose(deserialize = true)
    private String wizResponseIpAddress = "";

    // Increasing numeric value.
    // Bulb doesn't seem to care if it receives the same id multiple time
    // or commands with lower numbers after higher ones.
    @Expose
    private int id;
    // Not sure what env is - value always seems to be "pro"
    @Expose
    private @Nullable String env;

    // An error response
    @Expose
    private @Nullable ErrorResponseResult methodError;

    // The method being used - see the enum for details
    // We're setting this to "unknown"
    @Expose
    private WizMethodType method = WizMethodType.UnknownMethod;

    // The MAC address the response is coming from
    @Expose
    private String mac = "";

    // Whether or not a command succeeded (if the response is from a command)
    @Expose
    private boolean success = false;

    // The system configuration result, if present
    @Expose
    private @Nullable SystemConfigResult systemConfigResult;

    // The modeul configuration result, if present
    @Expose
    private @Nullable ModelConfigResult modelConfigResult;

    // The parameters or result of a command/response
    // A "result" is generally returned when solicited using a set/get method and a
    // "params" is retuned with an unsolicited sync/heartbeat. The result returned
    // from a get method is generally identical to the params returned in the
    // heartbeat.
    @Expose
    private @Nullable WizSyncState params;

    /**
     * Setters and Getters
     */

    public @Nullable SystemConfigResult getSystemConfigResults() {
        return this.systemConfigResult;
    }

    public void setSystemConfigResult(final SystemConfigResult configResult) {
        this.systemConfigResult = configResult;
    }

    public @Nullable ModelConfigResult getModelConfigResults() {
        return this.modelConfigResult;
    }

    public void setModelConfigResult(final ModelConfigResult configResult) {
        this.modelConfigResult = configResult;
    }

    public boolean getResultSuccess() {
        return this.success;
    }

    public void setResultSucess(final boolean success) {
        this.success = success;
    }

    public @Nullable WizSyncState getSyncState() {
        return this.params;
    }

    public void setSyncParams(final WizSyncState params) {
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

    public @Nullable WizMethodType getMethod() {
        return method;
    }

    public void setMethod(final WizMethodType method) {
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
