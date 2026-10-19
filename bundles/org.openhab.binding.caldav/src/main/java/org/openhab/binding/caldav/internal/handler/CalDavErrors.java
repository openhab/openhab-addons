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
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.logic.CalendarLimitException;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Classifies structured failures without exposing exception text, URLs or server responses.
 *
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Localized Thing status descriptions
 */
@NonNullByDefault
final class CalDavErrors {
    /**
     * Keeps plain Item error text separate from the reference localized by the Thing status service.
     */
    record Failure(ThingStatusDetail detail, String description, String statusDescription) {
    }

    private CalDavErrors() {
    }

    /**
     * Classifies an account failure with safe Item text and a translatable Thing status description.
     */
    static Failure account(Exception error) {
        return classify(error, false, false);
    }

    /**
     * Classifies a calendar failure, reporting a missing collection as GONE only after live synchronization.
     */
    static Failure calendar(Exception error, boolean synchronizedOnce) {
        return classify(error, true, synchronizedOnce);
    }

    private static Failure classify(Exception error, boolean calendar, boolean synchronizedOnce) {
        if (error instanceof CalDavHttpException http) {
            int status = http.statusCode();
            return switch (status) {
                case HttpStatus.UNAUTHORIZED_401 -> failure(ThingStatusDetail.CONFIGURATION_ERROR,
                        calendar ? "status.calendar.authentication" : "status.account.authentication",
                        (calendar ? "Authentication failed" : "CalDAV authentication failed") + " (HTTP 401)");
                case HttpStatus.FORBIDDEN_403 -> failure(ThingStatusDetail.CONFIGURATION_ERROR,
                        calendar ? "status.calendar.forbidden" : "status.account.forbidden",
                        (calendar ? "Calendar access forbidden" : "CalDAV access was forbidden") + " (HTTP 403)");
                case HttpStatus.NOT_FOUND_404 -> failure(
                        calendar && synchronizedOnce ? ThingStatusDetail.GONE : ThingStatusDetail.CONFIGURATION_ERROR,
                        calendar ? synchronizedOnce ? "status.calendar.gone" : "status.calendar.not-found"
                                : "status.account.not-found",
                        calendar ? synchronizedOnce ? "Calendar collection is no longer available (HTTP 404)"
                                : "Calendar collection was not found (HTTP 404)"
                                : "CalDAV endpoint was not found (HTTP 404)");
                default -> httpFailure(calendar ? "status.calendar.http-error" : "status.account.http-error",
                        calendar ? "Server returned HTTP " + status : "CalDAV request failed (HTTP " + status + ")",
                        status);
            };
        }
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (@Nullable
        Throwable cause = error; cause != null && visited.add(cause); cause = cause.getCause()) {
            if (cause instanceof UnknownHostException) {
                return communication("status.dns", "CalDAV server name could not be resolved");
            }
            if (cause instanceof SSLException || cause instanceof CertificateException) {
                return communication("status.tls", "TLS connection to the CalDAV server failed");
            }
            if (cause instanceof SocketTimeoutException || cause instanceof TimeoutException) {
                return communication("status.timeout", "CalDAV request timed out");
            }
            if (cause instanceof ConnectException || cause instanceof NoRouteToHostException) {
                return communication("status.connection", "Unable to connect to the CalDAV server");
            }
            if (cause instanceof CalDavHttpException http) {
                // Wrapped GET failures do not establish that the Calendar Collection itself has disappeared.
                return calendar && http.statusCode() == HttpStatus.NOT_FOUND_404
                        ? communication("status.calendar.resource-changed",
                                "Calendar resource changed during synchronization (HTTP 404)")
                        : httpFailure("status.account.http-error",
                                "CalDAV request failed (HTTP " + http.statusCode() + ")", http.statusCode());
            }
        }
        if (error instanceof CalendarLimitException) {
            return communication("status.calendar.limit", "Calendar data exceeds supported limits");
        }
        if (error instanceof IllegalArgumentException) {
            return failure(ThingStatusDetail.CONFIGURATION_ERROR,
                    calendar ? "status.calendar.configuration" : "status.account.configuration",
                    calendar ? "Invalid calendar configuration; check the collection URL and range settings"
                            : "Invalid CalDAV account configuration; check the URL, credentials and parameter values");
        }
        return communication(calendar ? "status.calendar.communication" : "status.account.communication",
                calendar ? "Calendar synchronization failed" : "CalDAV account communication failed");
    }

    private static Failure failure(ThingStatusDetail detail, String key, String description) {
        return new Failure(detail, description, "@text/" + key);
    }

    private static Failure httpFailure(String key, String description, int status) {
        return new Failure(ThingStatusDetail.COMMUNICATION_ERROR, description,
                "@text/" + key + " [\"" + status + "\"]");
    }

    private static Failure communication(String key, String description) {
        return failure(ThingStatusDetail.COMMUNICATION_ERROR, key, description);
    }
}
