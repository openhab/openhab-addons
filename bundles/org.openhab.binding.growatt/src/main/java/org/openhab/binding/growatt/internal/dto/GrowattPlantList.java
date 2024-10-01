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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link GrowattPlantList} is a DTO containing plant list and user data fields received from the Growatt cloud
 * server.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattPlantList {

    private @Nullable List<GrowattPlant> data;
    private @Nullable GrowattUser user;
    private @Nullable Boolean success;

    public List<GrowattPlant> getPlants() {
        List<GrowattPlant> data = this.data;
        return data != null ? data : List.of();
    }

    public Boolean getSuccess() {
        Boolean success = this.success;
        return success != null ? success : false;
    }

    public @Nullable GrowattUser getUserId() {
        return user;
    }
}
