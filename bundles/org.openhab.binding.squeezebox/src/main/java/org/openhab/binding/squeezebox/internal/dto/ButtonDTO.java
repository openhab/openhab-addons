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
package org.openhab.binding.squeezebox.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ButtonDTO} represents a custom button that overrides existing
 * button functionality. For example, "like song" replaces the repeat button.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ButtonDTO {

    /**
     * Indicates whether button is standard or custom
     */
    public Boolean custom;

    /**
     * Indicates if standard button is enabled or disabled
     */
    public Boolean enabled;

    /**
     * Concatenation of elements of command array
     */
    public String command;

    /**
     * Currently not used
     */
    @SerializedName("icon")
    public String icon;

    /**
     * Currently not used
     */
    @SerializedName("jiveStyle")
    public String jiveStyle;

    /**
     * Currently not used
     */
    @SerializedName("tooltip")
    public String toolTip;

    public boolean isCustom() {
        return custom == null ? Boolean.FALSE : custom;
    }

    public boolean isEnabled() {
        return enabled == null ? Boolean.FALSE : enabled;
    }
}
