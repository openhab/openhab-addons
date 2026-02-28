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
package org.openhab.binding.evcc.internal.discovery.mapper;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.discovery.Utils;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link BatteryDiscoveryMapper} is responsible for mapping the discovered batteries to discovery results.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@Component(service = EvccDiscoveryMapper.class)
@NonNullByDefault
public class BatteryDiscoveryMapper implements EvccDiscoveryMapper {

    @Override
    public Collection<DiscoveryResult> discover(JsonObject state, EvccBridgeHandler bridgeHandler) {
        AtomicInteger counter = new AtomicInteger(0);
        return Optional.ofNullable(state.getAsJsonObject(JSON_KEY_BATTERY))
                .map(batteryNode -> batteryNode.getAsJsonArray(JSON_KEY_DEVICES)).stream()
                .flatMap(deviceArray -> StreamSupport.stream(deviceArray.spliterator(), false))
                .filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject).map(battery -> {
                    int index = counter.getAndIncrement();
                    String title = battery.has(JSON_KEY_TITLE)
                            ? battery.get(JSON_KEY_TITLE).getAsString().toLowerCase(Locale.ROOT)
                            : JSON_KEY_BATTERY + index;

                    ThingUID uid = new ThingUID(EvccBindingConstants.THING_TYPE_BATTERY,
                            bridgeHandler.getThing().getUID(), Utils.sanitizeName(title));
                    return DiscoveryResultBuilder.create(uid).withLabel(title)
                            .withBridge(bridgeHandler.getThing().getUID()).withProperty(PROPERTY_INDEX, index)
                            .withProperty(PROPERTY_TITLE, title).withRepresentationProperty(PROPERTY_TITLE).build();

                }).toList();
    }
}
