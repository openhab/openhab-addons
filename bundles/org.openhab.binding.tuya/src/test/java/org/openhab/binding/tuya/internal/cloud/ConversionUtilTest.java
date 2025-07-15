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
package org.openhab.binding.tuya.internal.cloud;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.util.ConversionUtil;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.test.java.JavaTest;

/**
 * The {@link ConversionUtilTest} is a test class for the {@link ConversionUtil} class
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class ConversionUtilTest extends JavaTest {

    @Test
    public void hexColorDecodeTestNew() {
        String hex = "00b403e803e8";
        HSBType hsb = ConversionUtil.hexColorDecode(hex);

        Assertions.assertEquals(new HSBType("180,100,100"), hsb);
    }

    @Test
    public void hexColorDecodeTestOld() {
        String hex = "00008000f0ff8b";
        HSBType hsb = ConversionUtil.hexColorDecode(hex);

        Assertions.assertEquals(new HSBType("240,100,50.196"), hsb);
    }

    @Test
    public void hexColorEncodeTestNew() {
        HSBType hsb = new HSBType("180,100,100");
        String hex = ConversionUtil.hexColorEncode(hsb, false);

        Assertions.assertEquals("00b403e803e8", hex);
    }

    @Test
    public void hexColorEncodeTestOld() {
        HSBType hsb = new HSBType("240,100,50");
        String hex = ConversionUtil.hexColorEncode(hsb, true);

        Assertions.assertEquals("00008000f0fe7f", hex);
    }
}
