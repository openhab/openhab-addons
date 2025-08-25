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
package org.openhab.binding.myenergi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MyenergiDeviceConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class MyenergiDeviceConfiguration {

    public long refreshInterval = 30L; // by default, we refresh the device measurements every 30 secs.
}
