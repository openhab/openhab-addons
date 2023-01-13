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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.PhoneType;

/**
 * The {@link PhoneStatus} is the Java class used to map the structure used by the phone API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PhoneStatus {
    private int id;
    private boolean isRinging;
    private boolean onHook;
    private boolean hardwareDefect;
    private PhoneType type = PhoneType.UNKNOWN;
    private @Nullable String vendor;
    private int gainRx;
    private int gainTx;

    public int getId() {
        return id;
    }

    public boolean isRinging() {
        return isRinging;
    }

    public boolean isOnHook() {
        return onHook;
    }

    public PhoneType getType() {
        return type;
    }

    public boolean isHardwareDefect() {
        return hardwareDefect;
    }

    public @Nullable String getVendor() {
        return vendor;
    }

    public int getGainRx() {
        return gainRx;
    }

    public int getGainTx() {
        return gainTx;
    }

    public void setGainRx(int gainRx) {
        this.gainRx = gainRx;
    }

    public void setGainTx(int gainTx) {
        this.gainTx = gainTx;
    }

}
