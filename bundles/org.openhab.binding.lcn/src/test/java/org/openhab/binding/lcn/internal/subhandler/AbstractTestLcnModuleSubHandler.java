/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.connection.ModInfo;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class AbstractTestLcnModuleSubHandler {

    protected @Mock @NonNullByDefault({}) LcnModuleHandler handler;
    protected @Mock @NonNullByDefault({}) ModInfo info;
    private @NonNullByDefault({}) Collection<AbstractLcnModuleSubHandler> allHandlers;

    public void setUp() {
        when(handler.isMyAddress("000", "005")).thenReturn(true);

        allHandlers = new ArrayList<>();
        allHandlers.add(new LcnModuleBinarySensorSubHandler(handler, info));
        allHandlers.add(new LcnModuleCodeSubHandler(handler, info));
        allHandlers.add(new LcnModuleHostCommandSubHandler(handler, info));
        allHandlers.add(new LcnModuleKeyLockTableSubHandler(handler, info));
        allHandlers.add(new LcnModuleLedSubHandler(handler, info));
        allHandlers.add(new LcnModuleLogicSubHandler(handler, info));
        allHandlers.add(new LcnModuleMetaAckSubHandler(handler, info));
        allHandlers.add(new LcnModuleMetaFirmwareSubHandler(handler, info));
        allHandlers.add(new LcnModuleOperatingHoursCounterSubHandler(handler, info));
        allHandlers.add(new LcnModuleOutputSubHandler(handler, info));
        allHandlers.add(new LcnModuleRelaySubHandler(handler, info));
        allHandlers.add(new LcnModuleRollershutterOutputSubHandler(handler, info));
        allHandlers.add(new AbstractLcnModuleRollershutterRelaySubHandler(handler, info) {
        });
        allHandlers.add(new LcnModuleRvarLockSubHandler(handler, info));
        allHandlers.add(new LcnModuleRvarModeSubHandler(handler, info));
        allHandlers.add(new LcnModuleRvarSetpointSubHandler(handler, info));
        allHandlers.add(new LcnModuleS0CounterSubHandler(handler, info));
        allHandlers.add(new LcnModuleThresholdSubHandler(handler, info));
        allHandlers.add(new LcnModuleVariableSubHandler(handler, info));
    }

    protected void tryParseAllHandlers(String pck) {
        allHandlers.forEach(h -> h.tryParse(pck));
    }
}
