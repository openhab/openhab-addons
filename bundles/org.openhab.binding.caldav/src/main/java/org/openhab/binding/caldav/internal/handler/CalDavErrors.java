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
package org.openhab.binding.caldav.internal.handler;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.logic.CalendarLimitException;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Classifies structured failures without exposing exception text, URLs or server responses.
 *
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
final class CalDavErrors {
    record Failure(ThingStatusDetail detail, String description) {
    }

    private CalDavErrors() {
    }

    static Failure account(Exception error) {
        return classify(error, false, false);
    }

    static Failure calendar(Exception error, boolean synchronizedOnce) {
        return classify(error, true, synchronizedOnce);
    }

    private static Failure classify(Exception error, boolean calendar, boolean synchronizedOnce) {
        if (error instanceof CalDavHttpException http) {
            int status = http.statusCode();
            return switch (status) {
                case 401 -> new Failure(ThingStatusDetail.CONFIGURATION_ERROR,
                        (calendar ? "Authentication failed" : "CalDAV authentication failed") + " (HTTP 401)");
                case 403 -> new Failure(ThingStatusDetail.CONFIGURATION_ERROR,
                        (calendar ? "Calendar access forbidden" : "CalDAV access was forbidden") + " (HTTP 403)");
                case 404 -> new Failure(
                        calendar && synchronizedOnce ? ThingStatusDetail.GONE : ThingStatusDetail.CONFIGURATION_ERROR,
                        calendar ? synchronizedOnce ? "Calendar collection is no longer available (HTTP 404)"
                                : "Calendar collection was not found (HTTP 404)"
                                : "CalDAV endpoint was not found (HTTP 404)");
                default -> communication(
                        calendar ? "Server returned HTTP " + status : "CalDAV request failed (HTTP " + status + ")");
            };
        }
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (@Nullable
        Throwable cause = error; cause != null && visited.add(cause); cause = cause.getCause()) {
            if (cause instanceof UnknownHostException) {
                return communication("CalDAV server name could not be resolved");
            }
            if (cause instanceof SSLException || cause instanceof CertificateException) {
                return communication("TLS connection to the CalDAV server failed");
            }
            if (cause instanceof SocketTimeoutException || cause instanceof TimeoutException) {
                return communication("CalDAV request timed out");
            }
            if (cause instanceof ConnectException || cause instanceof NoRouteToHostException) {
                return communication("Unable to connect to the CalDAV server");
            }
            if (cause instanceof CalDavHttpException http) {
                // Wrapped GET failures do not establish that the Calendar Collection itself has disappeared.
                return communication(calendar && http.statusCode() == 404
                        ? "Calendar resource changed during synchronization (HTTP 404)"
                        : "CalDAV request failed (HTTP " + http.statusCode() + ")");
            }
        }
        if (error instanceof CalendarLimitException) {
            return communication("Calendar data exceeds supported limits");
        }
        if (error instanceof IllegalArgumentException) {
            return new Failure(ThingStatusDetail.CONFIGURATION_ERROR,
                    calendar ? "Invalid calendar configuration; check the collection URL and range settings"
                            : "Invalid CalDAV account configuration; check the URL, credentials and parameter values");
        }
        return communication(calendar ? "Calendar synchronization failed" : "CalDAV account communication failed");
    }

    private static Failure communication(String description) {
        return new Failure(ThingStatusDetail.COMMUNICATION_ERROR, description);
    }
}
