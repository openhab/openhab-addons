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
package org.openhab.binding.samsungtv.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception for Samsung IP Control requests
 *
 * @author Nick Waterton - Initial contribution
 */
@NonNullByDefault
public class IpControlException extends Exception {

    private static final long serialVersionUID = 4215387291630497183L;

    private final boolean tokenRejected;

    public IpControlException(String message) {
        this(message, false, null);
    }

    public IpControlException(String message, boolean tokenRejected) {
        this(message, tokenRejected, null);
    }

    public IpControlException(String message, @Nullable Throwable cause) {
        this(message, false, cause);
    }

    private IpControlException(String message, boolean tokenRejected, @Nullable Throwable cause) {
        super(message, cause);
        this.tokenRejected = tokenRejected;
    }

    /**
     * @return true if the TV rejected the access token, so a new one is needed
     */
    public boolean isTokenRejected() {
        return tokenRejected;
    }
}
