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
package org.openhab.binding.surepetcare.internal.handler;

import org.junit.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareUtils;

/**
 * The {@link SurePetcareUtilsTest} class implements unit test case for {@link SurePetcareUtils}
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareUtilsTest extends SurePetcareUtils {

    @Test
    public void testGetDeviceId() {
        SurePetcareUtils.getDeviceId();
    }

}
