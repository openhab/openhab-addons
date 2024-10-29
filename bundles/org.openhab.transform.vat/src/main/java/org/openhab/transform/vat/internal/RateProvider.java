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
package org.openhab.transform.vat.internal;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.transform.vat.internal.model.VATCountry;
import org.openhab.transform.vat.internal.model.VATPeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * The {@link RateProvider} class provides VAT rates for different
 * countries in different periods of time.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class RateProvider {
    private static final String RESOURCE_NAME = "/vat_rates.yaml";

    private final Logger logger = LoggerFactory.getLogger(RateProvider.class);
    private final Map<String, List<VATPeriod>> rateMap = getMap();

    public @Nullable BigDecimal getPercentage(String country) {
        return getPercentage(country, Instant.now());
    }

    public @Nullable BigDecimal getPercentage(String country, Instant time) {
        List<VATPeriod> vatPeriods = rateMap.get(country);
        if (vatPeriods == null) {
            return null;
        }
        for (VATPeriod vatPeriod : vatPeriods) {
            if (!time.isBefore(vatPeriod.start()) && time.isBefore(vatPeriod.end())) {
                return vatPeriod.percentage();
            }
        }

        logger.warn("No VAT rate for country {} valid at {}. This is a bug, please report", country, time);

        return null;
    }

    private Map<String, List<VATPeriod>> getMap() {
        HashMap<String, List<VATPeriod>> rateMap = new HashMap<>();
        Collection<VATCountry> rates = parseResource();
        for (VATCountry rate : rates) {
            rateMap.put(rate.country(), rate.vatPeriod());
        }
        return rateMap;
    }

    private Collection<VATCountry> parseResource() {
        try (InputStream inputStream = RateProvider.class.getResourceAsStream(RESOURCE_NAME)) {
            if (inputStream == null) {
                throw new IllegalStateException("VAT resource not found");
            }

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.registerModule(new JavaTimeModule());

            return mapper.readValue(inputStream,
                    mapper.getTypeFactory().constructCollectionType(List.class, VATCountry.class));
        } catch (IOException e) {
            throw new IllegalStateException("VAT resource could not be read and parsed", e);
        }
    }
}
