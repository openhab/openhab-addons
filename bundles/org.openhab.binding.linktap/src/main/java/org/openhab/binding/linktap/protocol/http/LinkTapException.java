/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.linktap.protocol.http;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LinkTapException} is a class for general exceptions that support
 * i18key functionality.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class LinkTapException extends I18Exception {

    @Serial
    private static final long serialVersionUID = -7739358310944502365L;

    protected String i18Key = "";

    public LinkTapException() {
        super();
    }

    public LinkTapException(final String message) {
        super(message);
    }

    public LinkTapException(final Throwable cause) {
        super(cause);
    }

    public LinkTapException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getI18Key() {
        return getI18Key("exception.unexpected-exception");
    }
}
