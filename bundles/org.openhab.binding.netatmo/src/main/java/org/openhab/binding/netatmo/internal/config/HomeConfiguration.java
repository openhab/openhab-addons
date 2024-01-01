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
package org.openhab.binding.netatmo.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;

/**
 * The {@link HomeConfiguration} is responsible for holding configuration information for any
 * Netatmo Home - security or energy, or both
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class HomeConfiguration extends NAThingConfiguration {
    public String securityId = "";
    public String energyId = "";

    @Override
    public String getId() {
        return getIdForArea(energyId.isBlank() ? FeatureArea.SECURITY : FeatureArea.ENERGY);
    }

    public String getIdForArea(FeatureArea feature) {
        return FeatureArea.ENERGY.equals(feature) ? energyId.isBlank() ? id : energyId
                : FeatureArea.SECURITY.equals(feature) ? securityId.isBlank() ? id : securityId : id;
    }
}
