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
package org.openhab.binding.bambulab.internal;

import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.AmsChannel.*;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class AmsDeviceConfiguration {
    public int number = -1;

    public void validateNumber() throws InitializationException {
        if (number < MIN_AMS || number > MAX_AMS) {
            var message = "AMS number has to be between %s and %s!".formatted(MIN_AMS, MAX_AMS);
            throw new InitializationException(CONFIGURATION_ERROR, message);
        }
    }
}
