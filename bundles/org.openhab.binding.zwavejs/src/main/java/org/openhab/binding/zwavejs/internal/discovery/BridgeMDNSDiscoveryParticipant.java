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
package org.openhab.binding.zwavejs.internal.discovery;

import static org.openhab.binding.zwavejs.internal.BindingConstants.*;

import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BridgeMDNSDiscoveryParticipant} is responsible for discovering new and removed Zwave JS servers. It uses
 * the
 * central {@link org.openhab.core.config.discovery.mdns.internal.MDNSDiscoveryService}.
 *
 * @author Leo Siepel - Initial contribution
 */
@Component(configurationPid = "discovery.zwavejs")
@NonNullByDefault
public class BridgeMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_zwave-js-server._tcp.local.";
    private static final String MDNS_PROPERTY_HOME_ID = "homeId";

    private final TranslationProvider translationProvider;

    @Activate
    public BridgeMDNSDiscoveryParticipant(@Reference TranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_GATEWAY);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID uid = getThingUID(service);

        if (uid != null) {
            String host = service.getHostAddresses()[0];
            String homeId = service.getPropertyString(MDNS_PROPERTY_HOME_ID);
            String thingTypeId = uid.getAsString().split(ThingUID.SEPARATOR)[1];
            String label = translationProvider.getText(FrameworkUtil.getBundle(getClass()),
                    "discovery.%s.label".formatted(thingTypeId), null, null, host);

            DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(uid) //
                    .withLabel(label) //
                    .withProperty(CONFIG_HOSTNAME, host) //
                    .withProperty(CONFIG_PORT, service.getPort()) //
                    .withProperty(PROPERTY_HOME_ID, homeId) //
                    .withRepresentationProperty(MDNS_PROPERTY_HOME_ID);

            return builder.build();
        }

        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String homeId = service.getPropertyString(MDNS_PROPERTY_HOME_ID);

        if (homeId != null) {
            return new ThingUID(THING_TYPE_GATEWAY, homeId);
        }
        return null;
    }
}
