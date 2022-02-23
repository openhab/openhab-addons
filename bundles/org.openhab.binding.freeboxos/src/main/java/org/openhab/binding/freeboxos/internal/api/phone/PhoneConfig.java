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
package org.openhab.binding.freeboxos.internal.api.phone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.ActivableConfig;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PhoneConfig implements ActivableConfig {
    public class PhoneConfigResponse extends Response<PhoneConfig> {
    }

    protected @NonNullByDefault({}) String network;
    @SerializedName("dect_eco_mode")
    protected boolean dectEcoMode;
    @SerializedName("dect_pin")
    protected @NonNullByDefault({}) String dectPin;
    @SerializedName("dect_ring_pattern")
    protected int dectRingPattern;
    @SerializedName("dect_registration")
    protected boolean dectRegistration;
    @SerializedName("dect_nemo_mode")
    protected boolean dectNemoMode;
    @SerializedName("dect_enabled")
    protected boolean dectEnabled;
    @SerializedName("dect_ring_on_off")
    protected boolean dectRingOnOff;

    public boolean isDectRingOnOff() {
        return dectRingOnOff;
    }

    public void setDectRingOnOff(boolean dectRingOnOff) {
        this.dectRingOnOff = dectRingOnOff;
    }

    @Override
    public boolean isEnabled() {
        return dectEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.dectEnabled = enabled;
    }
}
