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
package org.openhab.binding.growatt.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link GrowattPlant} is a DTO containing plant data fields received from the Growatt cloud server.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattPlant {

    private @Nullable String plantId;
    private @Nullable String plantName;

    public String getId() {
        String plantId = this.plantId;
        return plantId != null ? plantId : "";
    }

    public String getName() {
        String plantName = this.plantName;
        return plantName != null ? plantName : "";
    }
}
