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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link HomeDataModule} holds module informations returned by getHomeData endpoint
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class HomeDataModule extends NAThing implements NAModule {
    private @Nullable ZonedDateTime setupDate;
    private @Nullable String applianceType;
    private List<String> moduleBridged = List.of();

    public @Nullable String getApplianceType() {
        return applianceType;
    }

    public Optional<ZonedDateTime> getSetupDate() {
        return Optional.ofNullable(setupDate);
    }

    public List<String> getModuleBridged() {
        return moduleBridged;
    }

    @Override
    public boolean isIgnoredForThingUpdate() {
        return true;
    }
}
