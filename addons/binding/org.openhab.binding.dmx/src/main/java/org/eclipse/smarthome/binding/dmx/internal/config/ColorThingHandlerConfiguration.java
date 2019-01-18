/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.dmx.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ColorThingHandlerConfiguration} is a helper class for the base thing handler configuration
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class ColorThingHandlerConfiguration {
    public String dmxid = "";
    public int fadetime = 0;
    public int dimtime = 0;
    public String turnonvalue = "255,255,255";
    public String turnoffvalue = "0,0,0";
    public boolean dynamicturnonvalue = false;
}
