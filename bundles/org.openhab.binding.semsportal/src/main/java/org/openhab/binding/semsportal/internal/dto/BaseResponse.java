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
package org.openhab.binding.semsportal.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The base response contains the generic properties of each response from the portal. Depending on the request, the
 * data component contains different information. The subclasses of the BaseResponse contain the mapping of the data
 * with respect to their request context.
 *
 * @author Iwan Bron - Initial contribution
 */
@NonNullByDefault
public class BaseResponse {
    public static final String OK = "0";

    public static final String NO_SESSION = "100001";
    public static final String SESSION_EXPIRED = "100002";
    public static final String INVALID = "100005";
    public static final String EXCEPTION = "innerexception";

    private @Nullable String code;
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

    public boolean isError() {
        return EXCEPTION.equals(code);
    }

    public boolean isSessionInvalid() {
        return NO_SESSION.equals(code) || SESSION_EXPIRED.equals(code);
    }
}
