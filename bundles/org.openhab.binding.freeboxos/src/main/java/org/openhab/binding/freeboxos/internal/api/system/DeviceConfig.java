/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link DeviceConfig} is the Java class used to map minimal common
 * structure used by the system API in respose to get configuration requests
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class DeviceConfig {
    public static class DeviceConfigurationResponse extends Response<DeviceConfig> {
    }

    private class ModelInfo {
        private @NonNullByDefault({}) String prettyName;
    }

    private @Nullable ModelInfo modelInfo;
    private long uptimeVal;
    private @NonNullByDefault({}) String boardName;
    private @NonNullByDefault({}) String serial;
    private @NonNullByDefault({}) String firmwareVersion;

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getSerial() {
        return serial;
    }

    public long getUptimeVal() {
        return uptimeVal;
    }

    public String getBoardName() {
        return boardName;
    }

    public Optional<String> getPrettyName() {
        ModelInfo info = this.modelInfo;
        return info != null ? Optional.of(info.prettyName) : Optional.empty();
    }

    protected @Nullable List<Sensor> getSensors() {
        return null;
    }

    protected @Nullable List<Sensor> getFans() {
        return null;
    }

    public List<Sensor> getAllSensors() {
        ArrayList<Sensor> result = new ArrayList<>();

        List<Sensor> currentList = getFans();
        if (currentList != null) {
            result.addAll(currentList);
        }
        currentList = getSensors();
        if (currentList != null) {
            result.addAll(currentList);
        }
        return result;
    }
}
