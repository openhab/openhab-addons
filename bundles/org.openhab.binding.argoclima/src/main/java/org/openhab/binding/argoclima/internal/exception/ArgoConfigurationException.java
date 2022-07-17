/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.argoclima.internal.exception;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The class {@code ArgoConfigurationException} is thrown in case of any configuration-related issue (ex. invalid value
 * format)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoConfigurationException extends Exception {

    private static final long serialVersionUID = 174501670495658964L;
    public final String rawValue;

    private static String getMessageEx(String message, String paramValue) {
        if (paramValue.isEmpty()) {
            return message;
        }
        return String.format("%s. Value: [%s]", message, paramValue);
    }

    public ArgoConfigurationException(String message) {
        super(message);
        this.rawValue = "";
    }

    public ArgoConfigurationException(String message, String paramValue) {
        super(getMessageEx(message, paramValue));
        this.rawValue = paramValue;
    }

    public ArgoConfigurationException(String message, String paramValue, Throwable e) {
        super(getMessageEx(message, paramValue), e);
        this.rawValue = paramValue;
    }

    @Override
    public @Nullable String getMessage() {
        var msg = super.getMessage();
        if (this.getCause() != null) {
            msg += ". Caused by: " + Objects.requireNonNull(this.getCause()).getMessage();
        }
        return msg;
    }
}
