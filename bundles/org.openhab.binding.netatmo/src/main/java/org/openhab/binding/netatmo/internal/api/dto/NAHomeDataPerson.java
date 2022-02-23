/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;

/**
 * The {@link NAHomeDataPerson} provides Person informations returned by getHomeData endpoint
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAHomeDataPerson extends NAThing implements NetatmoModule {
    private @Nullable String url;

    @Override
    public ModuleType getType() {
        return ModuleType.NAPerson;
    }

    public boolean isKnown() {
        return description != null;
    }

    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }
}
