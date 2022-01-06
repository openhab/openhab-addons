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
package org.openhab.binding.sensebox.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SenseBoxConfiguration} is the base class for configuration
 * information held by devices and modules
 *
 * @author Hakan Tandogan - Initial contribution
 */
@NonNullByDefault
public class SenseBoxConfiguration {

    private @Nullable String senseBoxId;
    private long refreshInterval = 300;

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public @Nullable String getSenseBoxId() {
        return senseBoxId;
    }

    public void setSenseBoxId(String senseBoxId) {
        this.senseBoxId = senseBoxId;
    }
}
