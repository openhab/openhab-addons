/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.fmiweather.internal.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class representing response from the FMI weather service
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class FMIResponse {

    private Map<Location, Map<String, Data>> dataByLocationByParameter;

    /**
     * Builder class for FMIResponse
     *
     * @author Sami Salonen
     *         author Sami Salonen - Initial contribution/
     *
     */
    public static class Builder {

        private Map<Location, Map<String, List<Long>>> timestampsByLocationByParameter;
        private Map<Location, Map<String, List<@Nullable BigDecimal>>> valuesByLocationByParameter;

        public Builder() {
            timestampsByLocationByParameter = new HashMap<>();
            valuesByLocationByParameter = new HashMap<>();
        }

        public Builder appendLocationData(Location location, @Nullable Integer capacityHintForValues, String parameter,
                long epochSecond, @Nullable BigDecimal val) {
            timestampsByLocationByParameter.computeIfAbsent(location, k -> new HashMap<>()).computeIfAbsent(parameter,
                    k -> capacityHintForValues == null ? new ArrayList<>() : new ArrayList<>(capacityHintForValues))
                    .add(epochSecond);
            valuesByLocationByParameter.computeIfAbsent(location, k -> new HashMap<>()).computeIfAbsent(parameter,
                    k -> capacityHintForValues == null ? new ArrayList<>() : new ArrayList<>(capacityHintForValues))
                    .add(val);
            return this;
        }

        public FMIResponse build() {
            Map<Location, Map<String, Data>> out = new HashMap<>(timestampsByLocationByParameter.size());
            timestampsByLocationByParameter.entrySet().forEach(entry -> {
                collectParametersForLocation(out, entry);
            });
            return new FMIResponse(out);
        }

        private void collectParametersForLocation(Map<Location, Map<String, Data>> out,
                Entry<Location, Map<String, List<Long>>> locationEntry) {
            Location location = locationEntry.getKey();
            Map<String, List<Long>> timestampsByParameter = locationEntry.getValue();
            out.put(location, new HashMap<String, Data>(timestampsByParameter.size()));
            timestampsByParameter.entrySet().stream().forEach(parameterEntry -> {
                collectValuesForParameter(out, location, parameterEntry);
            });
        }

        private void collectValuesForParameter(Map<Location, Map<String, Data>> out, Location location,
                Entry<String, List<Long>> parameterEntry) {
            String parameter = parameterEntry.getKey();
            long[] timestamps = parameterEntry.getValue().stream().mapToLong(Long::longValue).toArray();
            BigDecimal[] values = valuesByLocationByParameter.get(location).get(parameter)
                    .toArray(new @Nullable BigDecimal[0]);
            Data dataValues = new Data(timestamps, values);
            out.get(location).put(parameter, dataValues);
        }
    }

    public FMIResponse(Map<Location, Map<String, Data>> dataByLocationByParameter) {
        this.dataByLocationByParameter = dataByLocationByParameter;
    }

    public Optional<Data> getData(Location location, String parameter) {
        return Optional.ofNullable(dataByLocationByParameter.get(location)).map(paramData -> paramData.get(parameter));
    }

    public Set<Location> getLocations() {
        return dataByLocationByParameter.keySet();
    }

    public Optional<Set<String>> getParameters(Location location) {
        return Optional.ofNullable(dataByLocationByParameter.get(location)).map(paramData -> paramData.keySet());
    }

    @Override
    public String toString() {
        return new StringBuilder("FMIResponse(").append(dataByLocationByParameter.toString()).append(")").toString();
    }
}
