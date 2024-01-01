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
package org.openhab.binding.mielecloud.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public final class MieleCloudBindingTestConstants {
    private MieleCloudBindingTestConstants() {
        throw new IllegalStateException("MieleCloudTestConstants must not be instantiated");
    }

    public static final String BRIDGE_ID = "genesis";

    public static final String SERVICE_HANDLE = MieleCloudBindingConstants.THING_TYPE_BRIDGE.getAsString() + ":"
            + BRIDGE_ID;
}
