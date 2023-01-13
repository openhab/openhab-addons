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
package org.openhab.binding.freeboxos.internal.api.player;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ModelInfo;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.StbType;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Player {
    private @Nullable String mac;
    private StbType stbType = StbType.UNKNOWN;
    private int id;
    private @Nullable ZonedDateTime lastTimeReachable;
    private boolean apiAvailable;
    private @Nullable String deviceName;
    private ModelInfo deviceModel = ModelInfo.UNKNOWN;
    private boolean reachable;
    private @Nullable String uid;
    private @Nullable String apiVersion;
    private List<String> lanGids = List.of();

    public String getMac() {
        return Objects.requireNonNull(mac).toLowerCase();
    }

    public StbType getStbType() {
        return stbType;
    }

    public int getId() {
        return id;
    }

    public ZonedDateTime getLastTimeReachable() {
        return Objects.requireNonNull(lastTimeReachable);
    }

    public boolean isApiAvailable() {
        return apiAvailable;
    }

    public String getDeviceName() {
        return Objects.requireNonNull(deviceName);
    }

    public ModelInfo getDeviceModel() {
        return deviceModel;
    }

    public boolean isReachable() {
        return reachable;
    }

    public String getUid() {
        return Objects.requireNonNull(uid);
    }

    public String getApiVersion() {
        return apiAvailable ? Objects.requireNonNull(apiVersion) : "";
    }

    public List<String> getLanGids() {
        return lanGids;
    }

    /**
     * @return a string like eg : '17/api/v8'
     */
    public String baseUrl() {
        return "%d/api/v%s/".formatted(id, getApiVersion().split("\\.")[0]);
    }

}
