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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.net.NetUtil;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebConstants;
import org.openhab.binding.sony.internal.transports.SonyTransport;
import org.openhab.binding.sony.internal.transports.TransportOptionHeader;

/**
 * This class contains the logic to determine if an authorization call is needed (via {@link SonyAuth})
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyAuthChecker {
    /** The transport to use for check authorization */
    private final SonyTransport transport;

    /** The current access code */
    private final @Nullable String accessCode;

    /**
     * Constructs the checker from the transport and access code
     * 
     * @param transport a non-null transport
     * @param accessCode a possibly null, possibly empty access code
     */
    public SonyAuthChecker(final SonyTransport transport, final @Nullable String accessCode) {
        Objects.requireNonNull(transport, "transport cannot be null");

        this.transport = transport;
        this.accessCode = accessCode;
    }

    /**
     * Checks the result using the specified callback
     * 
     * @param callback a non-null callback
     * @return a non-null result
     */
    public CheckResult checkResult(final CheckResultCallback callback) {
        Objects.requireNonNull(callback, "callback cannot be null");

        final String localAccessCode = accessCode;

        // If we have an access code and it's not RQST...
        // try to set the access code header and check for a good result
        // This will work in a few different scenarios where a header is required for communications to be successful
        // If this works - return back that we had an OK using a HEADER (ie OK_HEADER)
        //
        // Note: we ignore RQST because we don't want to trigger the pairing screen on a device at this stage
        // and/or we are cookie based (probably websocket or authentication has been turned off on the device)
        if (localAccessCode != null
                && !StringUtils.equalsIgnoreCase(ScalarWebConstants.ACCESSCODE_RQST, localAccessCode)) {
            final TransportOptionHeader authHeader = new TransportOptionHeader(
                    NetUtil.createAccessCodeHeader(localAccessCode));
            try {
                transport.setOption(authHeader);
                if (AccessResult.OK.equals(callback.checkResult())) {
                    return CheckResult.OK_HEADER;
                }
            } finally {
                transport.removeOption(authHeader);
            }
        }

        // If we made it here - we are likely not header based but cookie based (or we are not even authenticated)
        // Attempt the check result without the auth header and return OK_COOKIE is good
        final AccessResult res = callback.checkResult();
        if (res == null) {
            return new CheckResult(CheckResult.OTHER, "Check result returned null");
        }

        if (AccessResult.OK.equals(res)) {
            return CheckResult.OK_COOKIE;
        }

        // We aren't either cookie or header based - return the results (likely needs pairing or the screen is off or
        // not on the main screen)
        return new CheckResult(res);
    }

    /**
     * Functional interface defining the check result callback
     */
    @NonNullByDefault
    public interface CheckResultCallback {
        /**
         * Called to check a result and return an {@link AccessResult}
         * 
         * @return a non-null access result
         */
        AccessResult checkResult();
    }
}
