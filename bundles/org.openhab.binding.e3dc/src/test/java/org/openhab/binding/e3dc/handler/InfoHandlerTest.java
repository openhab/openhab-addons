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
package org.openhab.binding.e3dc.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.e3dc.internal.handler.E3DCInfoHandler;
import org.openhab.binding.e3dc.mock.ThingMock;

/**
 * The {@link InfoHandlerTest} Test Handler creation and Infrastructure
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class InfoHandlerTest {

    @Test
    public void testInfoHandlerCreation() {
        ThingMock m = new ThingMock();
        E3DCInfoHandler h = new E3DCInfoHandler(m);
        h.initialize();
    }
}
