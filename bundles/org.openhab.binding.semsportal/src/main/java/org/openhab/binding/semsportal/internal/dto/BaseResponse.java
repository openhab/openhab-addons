/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.semsportal.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Base response of the portal: all responses carry a code and a message. Subclasses add the data.
 *
 * @author Iwan Bron - Initial contribution
 * @author Julio Vilmar Gesser - Migrated to the SEMS+ API
 */
@NonNullByDefault
public class BaseResponse {
    public static final String OK = "00000";
    // the session token expired, or a newer login of the same client type replaced it
    public static final String SESSION_EXPIRED = "C0602";
    public static final String NO_SESSION = "100002";
    // credentials rejected
    public static final String INVALID = "100005";
    // no access to the requested power station, also given when the station does not exist
    public static final String NO_ACCESS = "100025";

    private @Nullable String code;
    @SerializedName(value = "description", alternate = "msg")
    private @Nullable String msg;

    public @Nullable String getCode() {
        return code;
    }

    public @Nullable String getMsg() {
        return msg;
    }

    public boolean isOk() {
        return OK.equals(code);
    }

    public boolean isNoAccess() {
        return NO_ACCESS.equals(code);
    }

    public boolean isSessionInvalid() {
        return SESSION_EXPIRED.equals(code) || NO_SESSION.equals(code);
    }
}
