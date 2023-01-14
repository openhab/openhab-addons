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
package org.openhab.binding.lcn.internal.subhandler;

import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.core.library.types.StringType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRvarModeSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleRvarModeSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleRvarModeSubHandler(handler, info);
    }

    @Test
    public void testhandleCommand1Cooling() throws LcnException {
        l.handleCommandString(new StringType("COOLING"), 0);
        verify(handler).sendPck("REATC");
    }

    @Test
    public void testhandleCommand1Heating() throws LcnException {
        l.handleCommandString(new StringType("HEATING"), 0);
        verify(handler).sendPck("REATH");
    }

    @Test
    public void testhandleCommand2Cooling() throws LcnException {
        l.handleCommandString(new StringType("COOLING"), 1);
        verify(handler).sendPck("REBTC");
    }

    @Test
    public void testhandleCommand2Heating() throws LcnException {
        l.handleCommandString(new StringType("HEATING"), 1);
        verify(handler).sendPck("REBTH");
    }
}
