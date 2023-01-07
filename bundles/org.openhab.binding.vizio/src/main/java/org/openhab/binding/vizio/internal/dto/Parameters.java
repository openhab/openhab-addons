/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.vizio.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Parameters} class contains data from the Vizio TV JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
public class Parameters {
    @SerializedName("FLAT")
    private String flat;
    @SerializedName("HELPTEXT")
    private String helptext;
    @SerializedName("HASHONLY")
    private String hashonly;

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getHelptext() {
        return helptext;
    }

    public void setHelptext(String helptext) {
        this.helptext = helptext;
    }

    public String getHashonly() {
        return hashonly;
    }

    public void setHashonly(String hashonly) {
        this.hashonly = hashonly;
    }
}
