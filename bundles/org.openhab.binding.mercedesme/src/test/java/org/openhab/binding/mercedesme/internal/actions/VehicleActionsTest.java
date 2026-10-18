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
package org.openhab.binding.mercedesme.internal.actions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.handler.VehicleHandler;

/**
 * {@link VehicleActionsTest} checks the rule actions are handed over to the vehicle handler
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleActionsTest {

    @Test
    void forceUpdateIsHandedOverToVehicleHandler() {
        VehicleActions actions = new VehicleActions();
        VehicleHandler handler = mock(VehicleHandler.class);
        actions.setThingHandler(handler);

        actions.forceUpdate();

        verify(handler).forceUpdate();
    }

    @Test
    void forceUpdateWithoutThingHandlerIsIgnored() {
        VehicleActions actions = new VehicleActions();

        // no exception means the missing handler is ignored instead of breaking the rule
        actions.forceUpdate();
    }
}
