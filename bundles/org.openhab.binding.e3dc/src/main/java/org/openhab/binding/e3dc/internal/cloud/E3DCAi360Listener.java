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
package org.openhab.binding.e3dc.internal.cloud;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Receives the AI 360 settings from the bridge, which polls them from the cloud.
 *
 * <p>
 * These values do not arrive with the RSCP poll cycle, so a thing that publishes them talks to the cloud
 * through this listener rather than through {@link org.openhab.binding.e3dc.internal.E3DCDataListener}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface E3DCAi360Listener {

    /** Called once per cloud poll with the current settings. */
    void onAi360(E3DCAi360State state);
}
