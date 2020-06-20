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
package org.openhab.binding.lcn.internal.subhandler;

import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.connection.ModInfo;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class AbstractTestLcnModuleSubHandler {
    @Mock
    protected @NonNullByDefault({}) LcnModuleHandler handler;
    @Mock
    protected @NonNullByDefault({}) ModInfo info;

    public AbstractTestLcnModuleSubHandler() {
        setUp();
    }

    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(handler.isMyAddress("000", "005")).thenReturn(true);
    }
}
