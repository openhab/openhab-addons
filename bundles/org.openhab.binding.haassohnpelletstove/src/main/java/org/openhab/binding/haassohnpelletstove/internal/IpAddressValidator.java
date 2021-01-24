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
package org.openhab.binding.haassohnpelletstove.internal;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link IpAddressValidator} is responsible for validating a given IP-Address.
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class IpAddressValidator {

    private static final String ZEROTO255 = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";

    private static final String IP_REGEXP = ZEROTO255 + "\\." + ZEROTO255 + "\\." + ZEROTO255 + "\\." + ZEROTO255;

    private static final Pattern IP_PATTERN = Pattern.compile(IP_REGEXP);

    /**
     * Verifies a given String as a valid IP-Address
     *
     * @param address to be verified
     * @return true if valid IP-Address, false otherwise
     */
    public boolean isValid(@Nullable String address) {
        return IP_PATTERN.matcher(address).matches();
    }
}
