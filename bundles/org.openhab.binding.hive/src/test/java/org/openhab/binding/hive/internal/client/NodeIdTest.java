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
package org.openhab.binding.hive.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class NodeIdTest {
    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(NodeId.class)
                // Value field is not allowed to be null.
                .suppress(Warning.NULL_FIELDS)
                .usingGetClass()
                .verify();
    }
}
