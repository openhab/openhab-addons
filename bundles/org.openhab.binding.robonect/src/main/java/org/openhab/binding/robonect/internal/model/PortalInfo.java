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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response to a Robonect portal status request.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class PortalInfo extends RobonectAnswer {
    private @Nullable Boolean enabled;
    private @Nullable Boolean connected;
    private @Nullable Address address;

    public @Nullable Boolean getEnabled() {
        return enabled;
    }

    public @Nullable Boolean getConnected() {
        return connected;
    }

    public @Nullable Address getAddress() {
        return address;
    }
}
