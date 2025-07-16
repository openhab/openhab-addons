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
package org.openhab.binding.evcc.internal.discovery.mapper;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonObject;

/**
 * The {@link SiteDiscoveryMapper} is responsible for mapping the discovered site to a discovery result
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class SiteDiscoveryMapper implements EvccDiscoveryMapper {

    @Override
    public Collection<DiscoveryResult> discover(JsonObject root, EvccBridgeHandler bridgeHandler) {
        List<DiscoveryResult> results = new ArrayList<>();
        if (!root.has("siteTitle")) {
            return results;
        }
        String siteTitle = root.get("siteTitle").getAsString();
        ThingUID uid = new ThingUID(EvccBindingConstants.THING_TYPE_SITE, bridgeHandler.getThing().getUID(), "site");
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel("evcc Site - " + siteTitle)
                .withBridge(bridgeHandler.getThing().getUID()).withProperty(PROPERTY_TYPE, "site")
                .withProperty(PROPERTY_SITE_TITLE, siteTitle).withRepresentationProperty(PROPERTY_SITE_TITLE).build();
        results.add(result);
        return results;
    }
}
