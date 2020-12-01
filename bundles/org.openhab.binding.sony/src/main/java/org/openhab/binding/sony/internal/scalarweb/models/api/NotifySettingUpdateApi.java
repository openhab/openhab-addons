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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the API in a setting update notification
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NotifySettingUpdateApi {

    /** The current value of the setting */
    private @Nullable String name;

    /** The target of the setting */
    private @Nullable String version;

    /**
     * Constructor used for deserialization only
     */
    public NotifySettingUpdateApi() {
    }

    /**
     * Get's the API name
     * 
     * @return the API name
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Get's the API version
     * 
     * @return the API version
     */
    public @Nullable String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "NotifySettingUpdateApi [name=" + name + ", version=" + version + "]";
    }
}
