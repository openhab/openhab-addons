/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.util;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility for validating e-mail addresses.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class EmailValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$");

    private EmailValidator() {
        throw new UnsupportedOperationException();
    }

    public static boolean isValid(String emailAddress) {
        return EMAIL_PATTERN.matcher(emailAddress).matches();
    }
}
