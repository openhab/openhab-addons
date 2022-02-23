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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link SystemConf} is the Java class used to map the "SystemConfig"
 * structure used by the system API
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SystemConf extends DeviceConfig {
    // Response classes
    public static class SystemConfigurationResponse extends Response<SystemConf> {
    }

    private @NonNullByDefault({}) String mac;
    private @Nullable List<Sensor> fans;
    private @Nullable List<Sensor> sensors;

    public String getMac() {
        return mac.toLowerCase();
    }

    @Override
    protected @Nullable List<Sensor> getSensors() {
        return sensors;
    }

    @Override
    protected @Nullable List<Sensor> getFans() {
        return fans;
    }
}
