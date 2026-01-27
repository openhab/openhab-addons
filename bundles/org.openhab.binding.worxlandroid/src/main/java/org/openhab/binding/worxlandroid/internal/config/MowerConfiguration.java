/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.worxlandroid.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MowerConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Nils Billing - Initial contribution
 * @author Gaël L'hopital - Added serialNumber configuration element
 */
@NonNullByDefault
public class MowerConfiguration {
    public static final String SERIAL_NUMBER = "serialNumber";

    public String serialNumber = "";
    public int refreshStatusInterval = 600;
    public int pollingInterval = 3600;

    @Override
    public String toString() {
        return "MowerConfiguration [serialNumber='%s', pollingInterval='%d', refreshStatusInterval='%d']"
                .formatted(serialNumber, pollingInterval, refreshStatusInterval);
    }
}
