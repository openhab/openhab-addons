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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PhoneStatus} is the Java class used to map the
 * structure used by the phone API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PhoneStatus {
    public static class PhoneStatusResponse extends Response<List<PhoneStatus>> {
    }

    public enum PhoneType {
        UNKNOWN,
        @SerializedName("fxs")
        LAND_LINE,
        @SerializedName("dect")
        DECT;
    }

    private int id;
    private boolean isRinging;
    private boolean onHook;
    private PhoneType type = PhoneType.UNKNOWN;

    public boolean isRinging() {
        return isRinging;
    }

    public boolean isOnHook() {
        return onHook;
    }

    public PhoneType getType() {
        return type;
    }

    public long getId() {
        return id;
    }
}
