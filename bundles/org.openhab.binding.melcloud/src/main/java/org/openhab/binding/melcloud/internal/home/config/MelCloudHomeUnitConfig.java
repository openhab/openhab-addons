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
package org.openhab.binding.melcloud.internal.home.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Config class shared by the {@code ata-unit} and {@code atw-unit} Thing types: both only need the
 * unit's {@code unitId} to look themselves up in the bridge's shared {@code /context} poll.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeUnitConfig {

    public String unitId = "";

    @Override
    public String toString() {
        return "[unitId=" + unitId + "]";
    }
}
