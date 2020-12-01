/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * This class represents the request to get the service protocols and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ServiceProtocols {
    /** The service names */
    private final Set<ServiceProtocol> serviceProtocols = new HashSet<>();

    /**
     * Instantiates a new gets the service protocols.
     *
     * @param results the non-null results
     */
    public ServiceProtocols(final ScalarWebResult results) {
        Objects.requireNonNull(results, "results cannot be null");

        final JsonArray rsts = results.getResults();
        if (rsts == null) {
            throw new JsonParseException("No results to deserialize");
        }

        for (final JsonElement elm : rsts) {
            if (elm.isJsonArray()) {
                final JsonArray ja = elm.getAsJsonArray();
                if (ja.size() > 0) {
                    final String serviceName = ja.get(0).getAsString();
                    if (serviceName == null || StringUtils.isEmpty(serviceName)) {
                        continue;
                    }
                    final Set<String> protocols = new HashSet<>();
                    if (ja.size() > 1 && ja.get(1).isJsonArray()) {
                        for (final JsonElement je : ja.get(1).getAsJsonArray()) {
                            protocols.add(je.getAsString());
                        }
                    }
                    serviceProtocols.add(new ServiceProtocol(serviceName, protocols));
                }
            }
        }
    }

    /**
     * Gets the service names
     *
     * @return the non-null, possibly empty unmodifiable set of service names
     */
    public Set<ServiceProtocol> getServiceProtocols() {
        return Collections.unmodifiableSet(serviceProtocols);
    }
}
