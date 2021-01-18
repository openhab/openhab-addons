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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.octopusenergy.internal.exception.RecordNotFoundException;

/**
 * The {@link Accounts} class is a DTO to hold an API response to a 'accounts' call.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class Accounts {

    // {
    // "number":"A-E653DBA",
    // "properties":[
    // ]
    // }

    /**
     * The account number of the Octopus Energy account.
     */
    public String number = "";

    /**
     * A list of account properties (i.e. houses)
     */
    public ArrayList<AccountProperty> properties = new ArrayList<>();

    /**
     * @return a list of electricity meter points across all properties in this account.
     */
    public List<ElectricityMeterPoint> getElectricityMeterPoints() {
        ArrayList<ElectricityMeterPoint> combined = new ArrayList<>();
        for (AccountProperty ap : properties) {
            combined.addAll(ap.electricityMeterPoints);
        }
        return combined;
    }

    /**
     * @return a list of gas meter points across all properties in this account.
     */
    public List<GasMeterPoint> getGasMeterPoints() {
        ArrayList<GasMeterPoint> combined = new ArrayList<>();
        for (AccountProperty ap : properties) {
            combined.addAll(ap.gasMeterPoints);
        }
        return combined;
    }

    /**
     * finds the meter point with the given identifier.
     *
     * @param identifier
     * @return
     *         the meter point or null if no meter point with the given identifier is found.
     */
    public ElectricityMeterPoint getElectricityMeterPoint(String identifier) throws RecordNotFoundException {
        for (AccountProperty ap : properties) {
            for (ElectricityMeterPoint mp : ap.electricityMeterPoints) {
                if (mp.mpan.equals(identifier)) {
                    return mp;
                }
            }
        }
        throw new RecordNotFoundException("No Meter Point wuth MPAN: " + identifier + " exists");
    }

    /**
     * finds the meter point with the given identifier.
     *
     * @param identifier
     * @return
     *         the meter point or null if no meter point with the given identifier is found.
     */
    public GasMeterPoint getGasMeterPoint(String identifier) throws RecordNotFoundException {
        for (AccountProperty ap : properties) {
            for (GasMeterPoint mp : ap.gasMeterPoints) {
                if (mp.mprn.equals(identifier)) {
                    return mp;
                }
            }
        }
        throw new RecordNotFoundException("No Meter Point wuth MPRN: " + identifier + " exists");
    }
}
