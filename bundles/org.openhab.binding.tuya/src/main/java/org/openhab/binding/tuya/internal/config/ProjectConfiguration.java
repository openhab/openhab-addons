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
package org.openhab.binding.tuya.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ProjectConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ProjectConfiguration {
    public String username = "";
    public String password = "";
    public String accessId = "";
    public String accessSecret = "";
    public Integer countryCode = 0;
    public String schema = "";
    public String dataCenter = "";

    public boolean isValid() {
        return !username.isEmpty() && !password.isEmpty() && !accessId.isEmpty() && !accessSecret.isEmpty()
                && countryCode != 0 && !schema.isEmpty() && !dataCenter.isEmpty();
    }
}
