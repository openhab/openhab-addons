/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.dto;

import java.io.File;

import org.openhab.binding.meross.internal.api.MerossHttpConnector;

/**
 * The {@link HttpConnectorBuilder} class is a builder for MerossHttpConnector
 *
 * @author Giovanni Fabiani - Initial contribution
 */

public class HttpConnectorBuilder {
    private String apiBaseUrl = "";
    private String userEmail = "";
    private String userPassword = "";
    private File credentialFile;
    private File deviceFile;

    public static HttpConnectorBuilder newBuilder() {
        return new HttpConnectorBuilder();
    }

    public HttpConnectorBuilder setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
        return this;
    }

    public HttpConnectorBuilder setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    public HttpConnectorBuilder setUserPassword(String userPassword) {
        this.userPassword = userPassword;
        return this;
    }

    public HttpConnectorBuilder setCredentialFile(File credentialFile) {
        this.credentialFile = credentialFile;
        return this;
    }

    public HttpConnectorBuilder setDeviceFile(File deviceFile) {
        this.deviceFile = deviceFile;
        return this;
    }

    public MerossHttpConnector build() {
        return new MerossHttpConnector(apiBaseUrl, userEmail, userPassword, credentialFile, deviceFile);
    }
}
