/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.enigma2.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * The {@link Enigma2RemoteKeyTest} class is responsible for testing {@link Enigma2RemoteKey}.
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public class Enigma2RemoteKeyTest {
    @Test
    public void testGetValue() {
        assertThat(Enigma2RemoteKey.ARROW_LEFT.getValue(), is(412));
    }
}
