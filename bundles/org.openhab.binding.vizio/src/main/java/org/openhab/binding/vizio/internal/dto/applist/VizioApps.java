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
package org.openhab.binding.vizio.internal.dto.applist;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VizioApps} class contains a list of VizioApp objects
 *
 * @author Michael Lobstein - Initial contribution
 */
public class VizioApps {
    @SerializedName("Apps")
    private List<VizioApp> apps = new ArrayList<VizioApp>();

    public List<VizioApp> getApps() {
        return apps;
    }

    public void setApps(List<VizioApp> apps) {
        this.apps = apps;
    }
}
