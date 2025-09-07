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
package org.openhab.binding.meross.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MerossDeviceConfiguration} class is an abstract base class with common configuration parameters.
 *
 * @author Mark Herwege - Initial contribution
 */

@NonNullByDefault
public abstract class MerossDeviceConfiguration {
    public String name = "";
}
