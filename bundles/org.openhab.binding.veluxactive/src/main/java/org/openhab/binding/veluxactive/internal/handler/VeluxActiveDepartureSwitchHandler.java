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
package org.openhab.binding.veluxactive.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.veluxactive.internal.ModuleRequestContext;
import org.openhab.binding.veluxactive.internal.api.dto.HomeStatus;
import org.openhab.binding.veluxactive.internal.api.dto.Module;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;

/**
 * Reports the physical NXD departure switch's battery and connectivity diagnostics.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class VeluxActiveDepartureSwitchHandler extends VeluxActiveBatteryModuleHandler {

    public VeluxActiveDepartureSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    synchronized void update(ModuleRequestContext moduleRequestContext, HomeStatus status) {
        if (!isRequestContextCurrent(moduleRequestContext)) {
            return;
        }
        Module module = status.module(moduleRequestContext.moduleId());
        if (module != null && !checkGateway(moduleRequestContext, module)) {
            return;
        }
        if (status.error(moduleRequestContext.moduleId()) != null
                || module != null && Boolean.FALSE.equals(module.reachable)) {
            failed(moduleRequestContext, "@text/offline.comm-error.departure-switch.unreachable");
        } else if (module == null) {
            missingStatus("@text/status.departure-switch.missing");
        } else {
            updateDiagnostics(module);
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
