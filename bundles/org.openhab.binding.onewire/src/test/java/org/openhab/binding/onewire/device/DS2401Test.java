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
package org.openhab.binding.onewire.device;

import static org.openhab.binding.onewire.internal.OwBindingConstants.THING_TYPE_BASIC;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.device.DS2401;
import org.openhab.core.library.types.OnOffType;

/**
 * Tests cases for {@link DS2401}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2401Test extends DeviceTestParent<DS2401> {

    @BeforeEach
    public void setupMocks() {
        setupMocks(THING_TYPE_BASIC, DS2401.class);
    }

    @Test
    public void presenceTestOn() throws OwException {
        presenceTest(OnOffType.ON);
    }

    @Test
    public void presenceTestOff() throws OwException {
        presenceTest(OnOffType.OFF);
    }
}
