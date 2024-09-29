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
package org.openhab.binding.sensibo.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SensiboAccountConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SensiboAccountConfiguration {
    /**
     * API key from https://home.sensibo.com/me/api
     */
    @Nullable
    public String apiKey;
    public int refreshInterval = 120;

    @Override
    public String toString() {
        return "SensiboAccountConfiguration [apiKey=<not showing>, refreshInterval=" + refreshInterval + "]";
    }
}
