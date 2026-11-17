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
 * A failure that the REST resource answers with an error body. Its message is the code; the text never holds a
 * secret, a server path or the message of another exception.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class KnxRestException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    static final int MAX_TEXT_LENGTH = 200;

    private final int status;
    private final String code;
    private final String text;

    public KnxRestException(KnxRestError error) {
        this(error, error.text());
    }

    public KnxRestException(KnxRestError error, String text) {
        super(error.code());
        this.status = error.status();
        this.code = error.code();
        this.text = text.length() > MAX_TEXT_LENGTH ? text.substring(0, MAX_TEXT_LENGTH) : text;
    }

    /** {@return a {@code bad-input} failure whose text names the parameter, header or body} */
    public static KnxRestException badInput(String name, String reason) {
        return new KnxRestException(KnxRestError.BAD_INPUT, name + ": " + reason);
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
