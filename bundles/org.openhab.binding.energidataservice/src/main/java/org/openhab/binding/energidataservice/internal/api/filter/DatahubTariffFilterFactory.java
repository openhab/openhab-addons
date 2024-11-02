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
package org.openhab.binding.energidataservice.internal.api.filter;

import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.energidataservice.internal.api.DatahubTariffFilter;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameter;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameterType;
import org.openhab.binding.energidataservice.internal.api.filter.dto.DatahubFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Factory for creating a {@link DatahubTariffFilter} for a specific Grid Company GLN.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DatahubTariffFilterFactory {

    private static final String RESOURCE_NAME = "/filters/grid_tariffs.yaml";

    private static final String NOTE_NET_TARIFF_C = "Nettarif C";
    private static final String NOTE_SYSTEM_TARIFF = "Systemtarif";
    private static final String NOTE_ELECTRICITY_TAX = "Elafgift";
    private static final String NOTE_REDUCED_ELECTRICITY_TAX = "Reduceret elafgift";
    private static final String NOTE_TRANSMISSION_NET_TARIFF = "Transmissions nettarif";

    private final Logger logger = LoggerFactory.getLogger(DatahubTariffFilterFactory.class);

    private final Map<String, DatahubFilter> filterMap = getMap();

    private Map<String, DatahubFilter> getMap() {
        HashMap<String, DatahubFilter> filterMap = new HashMap<>();
        Collection<DatahubFilter> filters = parseResource();
        for (DatahubFilter filter : filters) {
            filterMap.put(filter.gln(), filter);
        }
        return filterMap;
    }

    private Collection<DatahubFilter> parseResource() {
        try (InputStream inputStream = DatahubTariffFilterFactory.class.getResourceAsStream(RESOURCE_NAME)) {
            if (inputStream == null) {
                throw new IllegalStateException("Grid tariffs resource not found");
            }

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.registerModule(new JavaTimeModule());

            return mapper.readValue(inputStream,
                    mapper.getTypeFactory().constructCollectionType(List.class, DatahubFilter.class));
        } catch (IOException e) {
            throw new IllegalStateException("Grid tariffs resource could not be read and parsed", e);
        }
    }

    public DatahubTariffFilter getGridTariffByGLN(String globalLocationNumber) {
        DatahubFilter datahubFilter = filterMap.get(globalLocationNumber);
        if (datahubFilter != null) {
            logger.trace("Found filter in YAML resource: {}", datahubFilter);
            return new DatahubTariffFilter(datahubFilter.chargeTypeCodes(), datahubFilter.notes(),
                    datahubFilter.start());
        }
        return new DatahubTariffFilter(Set.of(), Set.of(NOTE_NET_TARIFF_C),
                DateQueryParameter.of(DateQueryParameterType.START_OF_YEAR));
    }

    public static DatahubTariffFilter getSystemTariff() {
        return new DatahubTariffFilter(Set.of(), Set.of(NOTE_SYSTEM_TARIFF),
                DateQueryParameter.of(ENERGINET_CUTOFF_DATE));
    }

    public static DatahubTariffFilter getTransmissionGridTariff() {
        return new DatahubTariffFilter(Set.of(), Set.of(NOTE_TRANSMISSION_NET_TARIFF),
                DateQueryParameter.of(ENERGINET_CUTOFF_DATE));
    }

    public static DatahubTariffFilter getElectricityTax() {
        return new DatahubTariffFilter(Set.of(), Set.of(NOTE_ELECTRICITY_TAX),
                DateQueryParameter.of(ENERGINET_CUTOFF_DATE));
    }

    public static DatahubTariffFilter getReducedElectricityTax() {
        return new DatahubTariffFilter(Set.of(), Set.of(NOTE_REDUCED_ELECTRICITY_TAX),
                DateQueryParameter.of(LocalDate.of(2021, 2, 1)));
    }
}
