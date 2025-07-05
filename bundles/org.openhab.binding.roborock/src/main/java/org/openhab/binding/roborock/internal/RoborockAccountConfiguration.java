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
package org.openhab.binding.roborock.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RoborockAccountConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class RoborockAccountConfiguration {

    /**
     * Account configuration parameters.
     */
    public String email = "";
    public String password = "";
    public String region = "US";
    public int refresh = 5; // in minutes
}
