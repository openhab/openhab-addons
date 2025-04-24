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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AddressComponentChannelSplitter} is a small helper class to split a list of {@link Address} in
 * {@link AddressComponent} and a list of {@link AddressChannel}.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class AddressComponentChannelSplitter {

    public static Map<AddressComponent, List<AddressChannel>> split(List<Address> addresses) {

        return addresses.stream().collect(Collectors.toMap(Address::getComponent,
                value -> new ArrayList<AddressChannel>(List.of(value.getChannel())), (existing, newest) -> {
                    existing.addAll(newest);
                    return existing;
                }));
    }
}
