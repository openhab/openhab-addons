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
package org.openhab.binding.restify.internal.handler;

import static java.lang.String.join;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class InitializationException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String i18nKey;
    private final String[] args;

    public InitializationException(String i18nKey, String... args) {
        super(translationKey(i18nKey, args));
        this.i18nKey = i18nKey;
        this.args = args;
    }

    private static String translationKey(String i18nKey, String[] args) {
        return "%s[%s]".formatted(i18nKey, join(", ", args));
    }

    public String translationKey() {
        return translationKey(i18nKey, args);
    }
}
