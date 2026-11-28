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
package org.openhab.binding.knx.internal.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Every failure the REST resource answers itself: the HTTP status, the code of the error body and its fixed text.
 * The texts are English constants of the API, like the console's, and are not translated.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public enum KnxRestError {
    BAD_INPUT(400, "bad-input", "A parameter, header or body cannot be understood."),
    NOT_A_PROJECT(400, "not-a-project", "The file is not an ETS project."),
    MALFORMED(400, "malformed", "The ETS project file is damaged or not complete."),
    PASSWORD_REQUIRED(400, "password-required", "The ETS project is password protected. Give its password."),
    WRONG_PASSWORD(400, "wrong-password", "The password of the ETS project is wrong."),
    UNSUPPORTED_PROTECTION(400, "unsupported-protection", "This kind of ETS project protection is not supported."),
    LIMIT_EXCEEDED(413, "limit-exceeded", "The ETS project is larger than the reader allows."),
    NOT_A_KEYRING(400, "not-a-keyring", "The file is not a keyring that can be read."),
    UNKNOWN_BRIDGE(404, "unknown-bridge", "There is no such KNX bridge."),
    NO_PROJECT(404, "no-project", "The bridge has no stored ETS project."),
    UNKNOWN_DEVICE(404, "unknown-device", "The project has no such device."),
    UNKNOWN_GROUP_ADDRESS(404, "unknown-group-address", "The project has no such group address."),
    PROJECT_UNUSABLE(409, "project-unusable", "The stored ETS project cannot be used. Import it again."),
    BUSY(409, "busy", "Another upload is running. Try again when it has finished."),
    MONITOR_OFF(409, "monitor-off", "The group monitor of this bridge is off: its buffer size is 0."),
    SEND_NOT_ALLOWED(403, "send-not-allowed",
            "Sending from the KNX page is not allowed for this bridge. Its setting \"Allow sending from the KNX page\" is off."),
    BAD_GROUP_ADDRESS(400, "bad-group-address", "This is not a three-level group address a telegram can go to."),
    UNKNOWN_DATA_TYPE(400, "unknown-data-type", "The add-on does not know this data point type."),
    BAD_VALUE(400, "bad-value", "The data point type cannot take this value."),
    BRIDGE_OFFLINE(409, "bridge-offline", "The bridge is not online."),
    TOO_MANY_TELEGRAMS(429, "too-many-telegrams",
            "At most 5 telegrams in 10 seconds can be sent from the KNX page. Try again in a moment."),
    SEND_FAILED(502, "send-failed", "The telegram could not be sent."),
    TOO_LARGE(413, "too-large", "The upload is larger than allowed."),
    TOO_MANY_STREAMS(503, "too-many-streams", "Too many telegram streams are open."),
    STORE_FAILED(500, "store-failed", "The result could not be stored. The earlier state is kept."),
    READ_FAILED(500, "read-failed", "The upload could not be read."),
    INTERNAL(500, "internal", "Something went wrong inside the KNX add-on.");

    private final int status;
    private final String code;
    private final String text;

    KnxRestError(int status, String code, String text) {
        this.status = status;
        this.code = code;
        this.text = text;
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String text() {
        return text;
    }
}
