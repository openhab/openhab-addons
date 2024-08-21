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
package org.openhab.binding.revogi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RevogiSmartStripControlConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Andi Bräu - Initial contribution
 */

@NonNullByDefault
public class RevogiSmartStripControlConfiguration {

    public String serialNumber = "Serial Number";

    public int pollInterval = 60;

    public String ipAddress = "127.0.0.1";
}
