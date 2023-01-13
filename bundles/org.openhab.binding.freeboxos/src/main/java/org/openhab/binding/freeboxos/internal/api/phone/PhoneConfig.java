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
package org.openhab.binding.freeboxos.internal.api.phone;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.PhoneNetworkStatus;
import org.openhab.binding.freeboxos.internal.rest.ActivableConfigIntf;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PhoneConfig implements ActivableConfigIntf {
    private PhoneNetworkStatus network = PhoneNetworkStatus.UNKNOWN;
    private boolean dectEcoMode;
    private @Nullable String dectPin;
    private int dectRingPattern;
    private boolean dectRegistration;
    private boolean dectNemoMode;
    private boolean dectEnabled;
    private boolean dectRingOnOff;

    @Override
    public boolean isEnabled() {
        return dectEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.dectEnabled = enabled;
    }

    public boolean isDectRingOnOff() {
        return dectRingOnOff;
    }

    public boolean isDectEcoMode() {
        return dectEcoMode;
    }

    public String getDectPin() {
        return Objects.requireNonNull(dectPin);
    }

    public int getDectRingPattern() {
        return dectRingPattern;
    }

    public boolean isDectRegistration() {
        return dectRegistration;
    }

    public boolean isDectNemoMode() {
        return dectNemoMode;
    }

    public boolean isDectEnabled() {
        return dectEnabled;
    }

    public void setDectRingOnOff(boolean status) {
        this.dectRingOnOff = status;
    }

    public PhoneNetworkStatus getNetwork() {
        return network;
    }
}
