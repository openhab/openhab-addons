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
package org.openhab.binding.atagone.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thing configuration parameters for the ATAG ONE thermostat.
 * Field names must match the parameter {@code name} attributes in {@code config.xml}.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class AtagOneConfiguration {

    public String hostname = "";
    public int port = 10000;
    public int refreshInterval = 30;
    public String clientId = "";
}
