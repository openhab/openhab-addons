/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.panasonictv.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for PanasonicTvHandler.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
public class PanasonicTvConfiguration {
    public String remoteControllerUdn = "";
    public String mediaRendererUdn = "";
    public int refreshInterval = 1000;

    @Override
    public String toString() {
        return "PanasonicTvConfiguration{" + "remoteControllerUdn='" + remoteControllerUdn + '\''
                + ", mediaRendererUdn='" + mediaRendererUdn + '\'' + ", refreshInterval=" + refreshInterval + '}';
    }
}
