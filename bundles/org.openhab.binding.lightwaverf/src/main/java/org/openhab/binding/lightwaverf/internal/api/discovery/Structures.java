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

package org.openhab.binding.lightwaverf.internal.api.discovery;

import java.util.ArrayList;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * The {@link lightwaverfBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */
public class Structures {

    @SerializedName("structures")
    @Expose
    private static ArrayList<Structures> structures = null;

    public ArrayList<Structures> getStructures() {
        return structures;
    }

    public void setStructures(ArrayList<Structures> structures) {
        Structures.structures = structures;
    }

}
