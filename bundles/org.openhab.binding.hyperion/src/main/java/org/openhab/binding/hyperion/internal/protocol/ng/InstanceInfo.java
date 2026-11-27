/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hyperion.internal.protocol.ng;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link InstanceInfo} is a POJO representing Hyperion instance information in the Hyperion.ng server.
 *
 * @author Ole Morten Rønning - Initial contribution
 */
@NonNullByDefault
public class InstanceInfo {

    @SerializedName("friendly_name")
    private @Nullable String friendlyName;

    @SerializedName("instance")
    private int instance;

    @SerializedName("running")
    private boolean running;

    public @Nullable String getFriendlyName() {
        return friendlyName;
    }

    public int getInstance() {
        return instance;
    }

    public boolean isRunning() {
        return running;
    }
}
