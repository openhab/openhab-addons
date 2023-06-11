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
package org.openhab.binding.nanoleaf.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test Firmware check
 *
 * @author Stefan Höhn - Initial contribution
 */

@NonNullByDefault
public class OpenAPIUtilsTest {

    @Test
    public void testStateOn() {
        int[] versions = OpenAPIUtils.getFirmwareVersionNumbers("5.1.2");
        assertThat(versions[0], is(5));
        assertThat(versions[1], is(1));
        assertThat(versions[2], is(2));
        int[] versions2 = OpenAPIUtils.getFirmwareVersionNumbers("5.1.2-4");
        assertThat(versions2[0], is(5));
        assertThat(versions2[1], is(1));
        assertThat(versions2[2], is(2));
        assertThat(versions2[3], is(4));
    }
}
