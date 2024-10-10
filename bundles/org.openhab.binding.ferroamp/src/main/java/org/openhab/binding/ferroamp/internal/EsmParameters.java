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
 * The {@link EsmParameters} is responsible for all parameters regarded to ESM
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class EsmParameters {
    public String jsonPostEsm;

    public EsmParameters(String jsonPostEsm) {
        this.jsonPostEsm = jsonPostEsm;
    }

    public static List<String> getChannelParametersEsm() {
        final List<String> channelParametersEsm = Arrays.asList("esm-unique-identifier", "esm-soh", "esm-soc",
                "esm-total-rated-capacity-all-batteries", "esm-rated-power-battery", "esm-status", "esm-timestamp");
        return channelParametersEsm;
    }
}
