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
package org.openhab.binding.haassohnpelletoven.validation;

import java.util.regex.Pattern;

/**
 * The {@link PinValidator} is responsible for validating a given PIN.
 *
 * @author Christian Feininger - Initial contribution
 */
public class PinValidator {

    Pattern pattern = Pattern.compile("(\\d{4})");

    /***
     * Verifies if a given String is a valid PIN.
     *
     * @param pin to be verified.
     * @return true if valid, false otherwise.
     */
    public boolean isValid(String pin) {
        return pattern.matcher(pin).matches();
    }
}
