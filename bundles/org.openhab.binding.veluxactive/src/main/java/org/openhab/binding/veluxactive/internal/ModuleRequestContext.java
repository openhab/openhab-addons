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
package org.openhab.binding.veluxactive.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.veluxactive.internal.handler.VeluxActiveModuleHandler;

/**
 * Captures a module's API address and handler lifecycle for asynchronous polling and commands.
 *
 * The account groups contexts by home to share status requests and delivers results to their handlers.
 * Before executing a command or publishing a result, the captured context is checked with
 * {@link VeluxActiveModuleHandler#isRequestContextCurrent(ModuleRequestContext)} so work from an obsolete lifecycle or
 * home association
 * can be
 * discarded. Child contexts also capture their gateway's context, allowing the same check to reject work when the
 * gateway has been reinitialized, disposed or is no longer online.
 *
 * An unresolved home is represented by an empty ID so the module can participate in topology polling.
 * Such contexts must not be used for status requests or commands to the API.
 *
 * @param handler handler receiving this module's state updates or communication failures
 * @param homeId resolved home ID at capture time, or empty while unresolved
 * @param moduleId configured module ID used to address the device
 * @param generation handler lifecycle counter at capture time, invalidated by initialization, disposal and Bridge
 *            status changes
 * @param parent captured gateway context for a child module, or null for a gateway
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public record ModuleRequestContext(VeluxActiveModuleHandler handler, String homeId, String moduleId, long generation,
        @Nullable ModuleRequestContext parent) {
}
