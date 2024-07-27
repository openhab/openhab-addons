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
package org.openhab.binding.pentair.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PentairBaseThingConfig } class contains configuration parameters for all Pentair child things.
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairBaseThingConfig {
    /** ID of thing on the Pentair RS485 bus. */
    public int id;
}
