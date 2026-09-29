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
package org.openhab.binding.homeconnectdirect.internal.configuration;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HomeConnectDirectApplianceConfiguration} class represents the appliance configuration options.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDirectApplianceConfiguration {
    public String haId = EMPTY;
    public String address = EMPTY;
    public int connectionRetryDelay = 1;
}
