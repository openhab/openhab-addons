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

package org.openhab.binding.ferroamp.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SsoParameters} is responsible for all parameters regarded to SSO
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class SsoParameters {

    public static List<String> getChannelParametersSso() {
        final List<String> channelParametersSs0 = Arrays.asList("id", "measured-voltage-pv-string",
                "measured-current-pv-string", "total-solar-energy", "relay-status", "temperature", "fault-code",
                "dc-link-voltage", "timestamp");
        return channelParametersSs0;
    }
}