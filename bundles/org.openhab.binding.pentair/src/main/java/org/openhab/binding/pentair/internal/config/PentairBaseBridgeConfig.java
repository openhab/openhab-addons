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

import static org.openhab.binding.pentair.internal.PentairBindingConstants.DEFAULT_PENTAIR_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PentairBaseBridgeConfig } class contains the base parameters in all bridges
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairBaseBridgeConfig {
    /** ID to use when sending commands on the Pentair RS485 bus. */
    public int id = DEFAULT_PENTAIR_ID;
    /** enable automatic discovery */
    public boolean discovery = true;
}
