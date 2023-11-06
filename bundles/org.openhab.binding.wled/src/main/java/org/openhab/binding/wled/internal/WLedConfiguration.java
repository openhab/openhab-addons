/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.wled.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WLedConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class WLedConfiguration {
    public String address = "";
    public int pollTime = 5;
    public int saturationThreshold;
    public boolean sortEffects = true;
    public boolean sortPalettes = true;
}
