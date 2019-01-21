/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.binding.onewire.internal.OwBindingConstants.THING_TYPE_IBUTTON;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.onewire.internal.device.DS2401;
import org.openhab.binding.onewire.test.AbstractDeviceTest;

/**
 * Tests cases for {@link DS2401}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DS2401Test extends AbstractDeviceTest {

    @Before
    public void setupMocks() {
        setupMocks(THING_TYPE_IBUTTON);
        deviceTestClazz = DS2401.class;
    }

    @Test
    public void presenceTestOn() {
        presenceTest(OnOffType.ON);
    }

    @Test
    public void presenceTestOff() {
        presenceTest(OnOffType.OFF);
    }
}
