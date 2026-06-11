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
 * The {@link CityConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CityConfiguration {
    public String codeInsee = "";

    public ConfigurationLevel check() {
        if (codeInsee.isBlank()) {
            return ConfigurationLevel.EMPTY_INSEE;
        }
        return ConfigurationLevel.COMPLETED;
    }
}
