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
package org.openhab.binding.alarmdecoder.internal.config;

import static org.openhab.binding.alarmdecoder.internal.AlarmDecoderBindingConstants.DEFAULT_MAPPING;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link KeypadConfig} class contains fields mapping thing configuration parameters for KeypadHandler.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class KeypadConfig {
    public String addressMask = "0";
    public boolean sendCommands = false;
    public boolean sendStar = false;
    public String commandMapping = DEFAULT_MAPPING;
}
