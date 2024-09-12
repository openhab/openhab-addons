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
package org.openhab.binding.netatmo.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;

/**
 * The {@link HomeDataRoom} provides Room informations returned by getHomeData endpoint
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomeDataRoom extends NAObject implements NAModule {
    private List<String> moduleIds = List.of();

    @Override
    public ModuleType getType() {
        // In json api answer type for NARoom is used with free strings like kitchen, living...
        return ModuleType.ROOM;
    }

    public List<String> getModuleIds() {
        return moduleIds;
    }
}
