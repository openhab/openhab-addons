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
package org.openhab.binding.e3dc.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration of one battery cell module.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBatteryModuleConfiguration {

    /** Zero based index of the module within the data of its battery. */
    public int moduleIndex;

    /** Zero based index of the battery that reports this module. */
    public int batteryIndex;
}
