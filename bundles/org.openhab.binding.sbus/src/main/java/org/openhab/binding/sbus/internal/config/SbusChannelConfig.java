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
package org.openhab.binding.sbus.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SbusChannelConfig} class contains fields mapping channel configuration parameters.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusChannelConfig {
    /**
     * The physical channel number on the SBUS device
     */
    public int channelNumber;
}
