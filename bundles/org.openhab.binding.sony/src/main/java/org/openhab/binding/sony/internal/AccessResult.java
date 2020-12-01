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
package org.openhab.binding.sony.internal;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.net.HttpResponse;

/**
 * This enum represents what type of action is needed when we first connect to the device
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class AccessResult {
    /** OK - device either needs no pairing or we have already paird */
    public static final AccessResult OK = new AccessResult("ok", "OK");
    public static final AccessResult NEEDSPAIRING = new AccessResult("needspairing", "Device needs pairing");
    public static final AccessResult SERVICEMISSING = new AccessResult("servicemissing", "Service is missing");
    /** Device needs pairing but the display is off */
    public static final AccessResult DISPLAYOFF = new AccessResult("displayoff",
            "Unable to request an access code - Display is turned off (must be on to see code)");
    /** Need to be in the home menu */
    public static final AccessResult HOMEMENU = new AccessResult("homemenu",
            "Unable to request an access code - HOME menu not displayed on device. Please display the home menu and try again.");
    /** Need to be in the home menu */
    public static final AccessResult PENDING = new AccessResult("pending",
            "Access Code requested. Please update the Access Code with what is shown on the device screen.");
    public static final AccessResult NOTACCEPTED = new AccessResult("notaccepted",
            "Access code was not accepted - please either request a new one or verify number matches what's shown on the device.");
    /** Some other error */
    public static final String OTHER = "other";

    /** The actual code */
    private final String code;

    /** The actual message */
    private final String msg;

    /**
     * Creates the result from the code/msg
     *
     * @param code the non-null, non-empty code
     * @param msg the non-null, non-empty msg
     */
    public AccessResult(final String code, final String msg) {
        Validate.notEmpty(code, "code cannot be empty");
        Validate.notEmpty(msg, "msg cannot be empty");

        this.code = code;
        this.msg = msg;
    }

    /**
     * Constructs the result from the response
     * 
     * @param resp the non-null response
     */
    public AccessResult(final HttpResponse resp) {
        Objects.requireNonNull(resp, "resp cannot be null");
        this.code = AccessResult.OTHER;

        final String content = resp.getContent();
        this.msg = resp.getHttpCode() + " - " + (StringUtils.defaultIfEmpty(content, resp.getHttpReason()));
    }

    /**
     * Returns the related code
     *
     * @return a non-null, non-empty code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Returns the related message
     *
     * @return a non-null, non-empty message
     */
    public String getMsg() {
        return this.msg;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof AccessResult)) {
            return false;
        }

        final AccessResult other = (AccessResult) obj;
        return StringUtils.equals(code, other.code);
    }
}
