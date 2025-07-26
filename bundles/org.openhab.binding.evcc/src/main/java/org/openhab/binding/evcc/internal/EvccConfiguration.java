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
package org.openhab.binding.evcc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EvccConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Florian Hotze - Initial contribution
 * @author Marcel Goerentz - Rework the binding
 */
@NonNullByDefault
public class EvccConfiguration {
    public String host = "";
    public int pollInterval = 30;
    public int port = 7070;
    public String scheme = "http";
}
