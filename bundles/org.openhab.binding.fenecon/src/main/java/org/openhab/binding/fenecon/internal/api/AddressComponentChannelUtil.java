/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.fenecon.internal.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AddressComponentChannelUtil} is a small helper class for e.g. to split a list of {@link Address} in
 * {@link AddressComponent} and a list of {@link AddressChannel} for a group REST-API request.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class AddressComponentChannelUtil {

    public static List<String> createComponentRequests(List<Address> addresses) {
        return split(addresses).entrySet().stream()
                .map(entry -> createComponentRequest(entry.getKey(), entry.getValue())).toList();
    }

    protected static Map<AddressComponent, Set<AddressChannel>> split(List<Address> addresses) {
        return addresses.stream().collect(Collectors.toMap(Address::getComponent,
                value -> new TreeSet<AddressChannel>(List.of(value.getChannel())), (existing, newest) -> {
                    existing.addAll(newest);
                    return existing;
                }));
    }

    protected static String createComponentRequest(AddressComponent component, Set<AddressChannel> channels) {
        // Grouping REST-API requests - e.g. http://...:8084/rest/channel/_sum/(State|EssSoc)

        // For valid URIs the pipe delimiter must be encoded as %7C
        return component.component() + "/("
                + String.join("%7C", channels.stream().map(AddressChannel::channel).toList()) + ")";
    }
}
