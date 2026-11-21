/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.projectstore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.handler.DeviceThingHandler;
import org.openhab.binding.knx.internal.project.KnxConfiguredAddress;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;

/**
 * The group addresses the device Things of a bridge use, to compare with the ETS project.
 *
 * @param addresses per device Thing in UID order, its addresses in channel order
 * @param thingsWithoutHandler device Things that have no handler, so their addresses are not known
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record ConfiguredAddresses(List<KnxConfiguredAddress> addresses, int thingsWithoutHandler) {

    public ConfiguredAddresses {
        addresses = List.copyOf(addresses);
    }

    public static ConfiguredAddresses of(Bridge bridge) {
        List<Thing> things = new ArrayList<>(bridge.getThings());
        things.sort(Comparator.comparing(thing -> thing.getUID().getAsString()));
        List<KnxConfiguredAddress> addresses = new ArrayList<>();
        int withoutHandler = 0;
        for (Thing thing : things) {
            if (thing.getHandler() instanceof DeviceThingHandler handler) {
                addresses.addAll(handler.configuredAddresses());
            } else if (KNXBindingConstants.THING_TYPE_DEVICE.equals(thing.getThingTypeUID())) {
                withoutHandler++;
            }
        }
        return new ConfiguredAddresses(addresses, withoutHandler);
    }
}
