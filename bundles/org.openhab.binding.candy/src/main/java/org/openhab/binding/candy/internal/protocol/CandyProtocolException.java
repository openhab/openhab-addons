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
package org.openhab.binding.candy.internal.protocol;

import java.io.Serial;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.I18nUtil;

/**
 * An expected protocol failure whose message is translated when the exception is created.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class CandyProtocolException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final String TRANSPORT = "@text/communication-error-transport";
    public static final String TIMEOUT = "@text/communication-error-timeout";
    public static final String HTTP_STATUS = "@text/communication-error-http";
    public static final String CLIENT_UNAVAILABLE = "@text/communication-error-client";
    public static final String INVALID_RESPONSE = "@text/communication-error-response";

    public CandyProtocolException(String message) {
        super(translate(message));
    }

    public CandyProtocolException(String message, @Nullable Throwable cause) {
        super(translate(message), cause);
    }

    private static String translate(String message) {
        if (!I18nUtil.isConstant(message)) {
            return message;
        }
        try {
            return ResourceBundle
                    .getBundle("OH-INF.i18n.candy", Locale.getDefault(), CandyProtocolException.class.getClassLoader())
                    .getString(I18nUtil.stripConstant(message));
        } catch (MissingResourceException e) {
            return message;
        }
    }
}
