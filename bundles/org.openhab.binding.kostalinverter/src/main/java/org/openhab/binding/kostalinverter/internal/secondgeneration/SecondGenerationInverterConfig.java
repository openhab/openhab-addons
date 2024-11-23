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
package org.openhab.binding.kostalinverter.internal.secondgeneration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SecondGenerationInverterConfig} class defines constants, which are
 * used in the second generation part of the binding.
 *
 * @author Christian Schneider - Initial contribution
 * @author Örjan Backsell - Added parameters for configuration options Piko1020, Piko New Generation
 *
 */

@NonNullByDefault
public class SecondGenerationInverterConfig {
    public String url = "";
    public String username = "";
    public String password = "";
    public int refreshInterval = 60;
    public String dxsIdConf = "";
    public String valueConf = "";
    public boolean hasBattery = false;
}
