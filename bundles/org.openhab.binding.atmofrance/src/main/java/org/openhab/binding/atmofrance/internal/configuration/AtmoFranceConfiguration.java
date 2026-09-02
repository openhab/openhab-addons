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
package org.openhab.binding.atmofrance.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AtmoFranceConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AtmoFranceConfiguration {
    public String username = "";
    public String password = "";

    public ConfigurationLevel check() {
        if (username.isBlank()) {
            return ConfigurationLevel.EMPTY_USERNAME;
        } else if (password.isBlank()) {
            return ConfigurationLevel.EMPTY_PASSWORD;
        }
        return ConfigurationLevel.COMPLETED;
    }
}
