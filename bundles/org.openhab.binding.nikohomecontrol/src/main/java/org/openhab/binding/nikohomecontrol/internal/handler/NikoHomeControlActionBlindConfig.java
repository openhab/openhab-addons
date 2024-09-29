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
package org.openhab.binding.nikohomecontrol.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link NikoHomeControlActionBlindConfig} is the config class for Niko Home Control Blind Actions.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlActionBlindConfig extends NikoHomeControlActionConfig {
    public boolean invert;
}
