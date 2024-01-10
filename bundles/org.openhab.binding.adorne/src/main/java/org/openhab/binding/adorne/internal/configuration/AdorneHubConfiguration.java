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
package org.openhab.binding.adorne.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AdorneHubConfiguration} class represents the hub configuration options.
 *
 * @author Mark Theiding - Initial contribution
 */
@NonNullByDefault
public class AdorneHubConfiguration {
    public String host = "LCM1.local";
    public Integer port = 2112;
}
