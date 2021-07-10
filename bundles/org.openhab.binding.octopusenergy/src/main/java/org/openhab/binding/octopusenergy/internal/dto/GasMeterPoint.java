/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.dto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants;
import org.openhab.binding.octopusenergy.internal.exception.RecordNotFoundException;

/**
 * The {@link GasMeterPoint} is a DTO class representing a gas meter point.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class GasMeterPoint {

    // {
    // "mprn":"3029115504",
    // "consumption_standard":23882,
    // "meters":[
    // ],
    // "agreements":[
    // ]
    // }

    /**
     * The Meter Point Reference Number, a unique identifier of each meter point.
     */
    public String mprn = OctopusEnergyBindingConstants.UNDEFINED_STRING;

    public @Nullable Long consumptionStandard;

    public ArrayList<Meter> meters = new ArrayList<>();
    public ArrayList<Agreement> agreements = new ArrayList<>();

    public Map<String, String> getThingProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("mprn", mprn);
        Long cs = consumptionStandard;
        if (cs != null) {
            properties.put("consumptionStandard", cs.toString());
        }
        return properties;
    }

    public Agreement getAgreementAsOf(ZonedDateTime startTime) throws RecordNotFoundException {
        for (Agreement agr : agreements) {
            if ((startTime.isEqual(agr.validFrom) || startTime.isAfter(agr.validFrom))
                    && (startTime.isBefore(agr.validTo))) {
                return agr;
            }
        }
        throw new RecordNotFoundException("No agreement found for time - " + startTime.toString());
    }
}
