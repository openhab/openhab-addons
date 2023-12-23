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
package org.openhab.binding.intellicenter2.internal.model;

import static java.util.stream.Collectors.toList;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.CIRCUIT;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.OBJTYP;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.intellicenter2.internal.protocol.ICRequest;
import org.openhab.binding.intellicenter2.internal.protocol.ICResponse;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class GetConfiguration {

    private static final String GET_CONFIGURATION = "GetConfiguration";
    public static final ICRequest REQUEST = ICRequest.getQuery(GET_CONFIGURATION);

    private final ICResponse response;

    public GetConfiguration(ICResponse response) {
        this.response = Objects.requireNonNull(response);
    }

    public List<Circuit> getFeatureCircuits() {
        return response.getAnswer().stream()
                .filter(r -> CIRCUIT.toString().equals(r.getOptionalValueAsString(OBJTYP).orElse("")))
                .filter(r -> r.getObjectName().startsWith("FTR")).map(Circuit::new).collect(toList());
    }

    @Override
    public String toString() {
        return "GetConfiguration{" + "response=" + response + '}';
    }
}
