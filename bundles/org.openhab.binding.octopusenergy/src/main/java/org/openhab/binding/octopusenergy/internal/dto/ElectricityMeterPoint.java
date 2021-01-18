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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants;
import org.openhab.binding.octopusenergy.internal.exception.RecordNotFoundException;
import org.openhab.binding.octopusenergy.internal.util.PriceOptimiser;

/**
 * The {@link ElectricityMeterPoint} is a DTO class representing an electricity meter point.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class ElectricityMeterPoint {

    // {
    // "mpan":"1012493048237",
    // "profile_class":1,
    // "consumption_standard":4514,
    // "meters":[
    // ],
    // "agreements":[
    // ],
    // "is_export":false
    // }

    /**
     * The Meter Point Administration Number, a unique identifier of each meter point.
     */
    public String mpan = OctopusEnergyBindingConstants.UNDEFINED_STRING;

    public @Nullable Integer profileClass;
    public @Nullable Long consumptionStandard;

    public List<Meter> meters = new ArrayList<>();
    public List<Agreement> agreements = new ArrayList<>();

    public @Nullable Boolean isExport;

    public List<Consumption> consumptionList = new ArrayList<>();

    /**
     * a list of price slots, ordered from earliest to latest
     */
    public List<Price> priceList = new ArrayList<>();

    public PriceOptimiser optimizer = new PriceOptimiser();

    public Map<String, String> getThingProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("mpan", mpan);
        Integer pc = profileClass;
        if (pc != null) {
            properties.put("profileClass", pc.toString());
        }
        Long cs = consumptionStandard;
        if (cs != null) {
            properties.put("consumptionStandard", cs.toString());
        }
        Boolean ie = isExport;
        if (ie != null) {
            properties.put("isExport", ie.toString());
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

    public String getProductAsOf(ZonedDateTime startTime) throws RecordNotFoundException {
        return getAgreementAsOf(startTime).getProduct();
    }

    public Consumption getMostRecentConsumption() throws RecordNotFoundException {
        if (consumptionList.size() > 0) {
            return Collections.max(consumptionList, Consumption.INTERVAL_START_ORDER_ASC);
        }
        throw new RecordNotFoundException();
    }

    public BigDecimal getMaxPrice(boolean includeVat) throws RecordNotFoundException {
        if (priceList.size() > 0) {
            Price p = Collections.max(priceList, Price.PRICE_ORDER_ASC);
            return includeVat ? p.valueIncVat : p.valueExcVat;
        }
        throw new RecordNotFoundException();
    }

    public BigDecimal getMinPrice(boolean includeVat) throws RecordNotFoundException {
        if (priceList.size() > 0) {
            Price p = Collections.min(priceList, Price.PRICE_ORDER_ASC);
            return includeVat ? p.valueIncVat : p.valueExcVat;
        }
        throw new RecordNotFoundException();
    }
}
