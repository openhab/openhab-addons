/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.login;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ModelInfo;

/**
 * The {@link ApiVersion} is the Java class used to map the api_version answer
 *
 * http://mafreebox.freebox.fr/api_version
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiVersion {
    private @Nullable String apiBaseUrl;
    private @Nullable String apiDomain;
    private @Nullable String apiVersion;
    private ModelInfo boxModel = ModelInfo.UNKNOWN;
    private @Nullable String boxModelName;
    private @Nullable String deviceName;
    private @Nullable String deviceType;
    private boolean httpsAvailable;
    private int httpsPort;
    private @Nullable String uid;

    /**
     * @return a string like eg : '/api/v8'
     */
    public String baseUrl() {
        return "%sv%s".formatted(getApiBaseUrl(), getApiVersion().split("\\.")[0]);
    }

    public String getApiBaseUrl() {
        return Objects.requireNonNull(apiBaseUrl);
    }

    public @Nullable String getApiDomain() {
        return apiDomain;
    }

    public String getApiVersion() {
        return Objects.requireNonNull(apiVersion);
    }

    public ModelInfo getBoxModel() {
        return boxModel;
    }

    public @Nullable String getBoxModelName() {
        return boxModelName;
    }

    public String getDeviceName() {
        return Objects.requireNonNull(deviceName);
    }

    public String getDeviceType() {
        return Objects.requireNonNull(deviceType);
    }

    public boolean isHttpsAvailable() {
        return httpsAvailable;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public String getUid() {
        return Objects.requireNonNull(uid);
    }
}
