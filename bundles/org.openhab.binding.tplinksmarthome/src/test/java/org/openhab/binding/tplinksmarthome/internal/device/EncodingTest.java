/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test class to test if text read from the device is correctly decoded to handle special characters.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class EncodingTest extends DeviceTestBase<SwitchDevice> {

    public EncodingTest() throws IOException {
        super(new SwitchDevice(), "encoding_test");
    }

    @Test
    public void testCorrectDecodingOfText() throws IOException {
        assertThat("Alias incorrectly decoded", deviceState.getSysinfo().getAlias(), is("MyßmärtPlug"));
    }
}
