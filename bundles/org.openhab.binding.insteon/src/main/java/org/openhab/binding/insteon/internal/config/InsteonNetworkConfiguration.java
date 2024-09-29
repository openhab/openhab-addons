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
package org.openhab.binding.insteon.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link InsteonNetworkConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Rob Nielsen - Initial contribution
 */
@NonNullByDefault
public class InsteonNetworkConfiguration {

    // required parameter
    private String port = "";

    private @Nullable Integer devicePollIntervalSeconds;

    private @Nullable String additionalDevices;

    private @Nullable String additionalFeatures;

    public String getPort() {
        return port;
    }

    public @Nullable Integer getDevicePollIntervalSeconds() {
        return devicePollIntervalSeconds;
    }

    public @Nullable String getAdditionalDevices() {
        return additionalDevices;
    }

    public @Nullable String getAdditionalFeatures() {
        return additionalFeatures;
    }
}
