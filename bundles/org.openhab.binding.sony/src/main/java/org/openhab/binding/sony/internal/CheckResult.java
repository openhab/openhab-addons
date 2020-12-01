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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This represents the result of an authorization check (extends {@link AccessResults} to provide more fine grained
 * response of OK conditions)
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class CheckResult extends AccessResult {
    /** Authorization was fine and uses HEADER style of authorization */
    public static final CheckResult OK_HEADER = new CheckResult("okHeader", "OK");

    /** Authorization was fine and uses COOKIE style of authorization */
    public static final CheckResult OK_COOKIE = new CheckResult("okCookie", "OK");

    /**
     * Constructs the check result from the code and message
     * 
     * @param code a non-null, non-empty code
     * @param msg a non-null, non-empty message
     */
    public CheckResult(final String code, final String msg) {
        super(code, msg);
    }

    /**
     * Constructst he check result from the access request
     * 
     * @param res a non-null access request
     */
    public CheckResult(final AccessResult res) {
        super(res.getCode(), res.getMsg());
    }
}
