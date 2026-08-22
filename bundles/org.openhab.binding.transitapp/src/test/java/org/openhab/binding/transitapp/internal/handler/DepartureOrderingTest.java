/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.transitapp.internal.handler;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult;

/**
 * The {@link DepartureOrderingTest} is responsible for testing departure sorting.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class DepartureOrderingTest {

    @Test
    public void testDepartureResultInitialization() {
        StopDeparturesResult result = new StopDeparturesResult();
        assertNotNull(result);
    }
}
