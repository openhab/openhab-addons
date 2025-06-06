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
package org.openhab.binding.dmx.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TunableWhiteThingHandlerConfiguration} is a helper class for the base thing handler configuration
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TunableWhiteThingHandlerConfiguration {
    public String dmxid = "";
    public int fadetime = 0;
    public int dimtime = 0;
    public String turnonvalue = "255,255";
    public String turnoffvalue = "0,0";
    public boolean dynamicturnonvalue = false;
}
