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
package org.openhab.binding.smarther.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Signals that an "invalid property value" messaging issue with API gateway has occurred.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherIllegalPropertyValueException extends IllegalArgumentException {

    private static final long serialVersionUID = -2549779559688846805L;

    private static final String MSG_FORMAT = "Invalid value '%s' received for enum '%s'";

    /**
     * Constructs a {@code SmartherIllegalPropertyValueException} with the specified detail message.
     *
     * @param message
     *            the error message returned from the API gateway
     */
    public SmartherIllegalPropertyValueException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code SmartherIllegalPropertyValueException} with the specified property name and invalid value
     * returned by the API gateway.
     *
     * @param propertyName
     *            the property name that caused the issue
     * @param invalidValue
     *            the invalid value returned by the API gateway for {@code PropertyName}
     */
    public SmartherIllegalPropertyValueException(String propertyName, String invalidValue) {
        super(String.format(MSG_FORMAT, invalidValue, propertyName));
    }

}
