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
package org.openhab.binding.onewire.internal.owserver;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onewire.internal.SensorId;

/**
 * The {@link OwserverDeviceParameter} device parameter definition for owserver bridge handler
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class OwserverDeviceParameter {
    private String prefix = "";
    private String path = "";

    /**
     * device parameter for owserver bridge handler
     *
     * @param prefix path prefix (e.g. "uncached/")
     * @param path path without sensor id (e.g. "/humidity")
     */
    public OwserverDeviceParameter(String prefix, String path) {
        if (prefix.endsWith("/")) {
            this.prefix = prefix.substring(0, prefix.length() - 1);
        } else {
            this.prefix = prefix;
        }
        if (path.startsWith("/")) {
            this.path = path;
        } else {
            this.path = "/" + path;
        }
    }

    /**
     * device parameter for owserver bridge handler
     *
     * @param path path without sensor id (e.g. "/humidity")
     */
    public OwserverDeviceParameter(String path) {
        this("", path);
    }

    /**
     * get the full owfs path for a given sensor id
     *
     * @param sensorId
     */
    public String getPath(SensorId sensorId) {
        return prefix + sensorId.getFullPath() + path;
    }

    @Override
    public String toString() {
        return prefix + "/sensorId" + path;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof OwserverDeviceParameter)) {
            return false;
        }

        return ((OwserverDeviceParameter) o).toString().equals(toString());
    }
}
