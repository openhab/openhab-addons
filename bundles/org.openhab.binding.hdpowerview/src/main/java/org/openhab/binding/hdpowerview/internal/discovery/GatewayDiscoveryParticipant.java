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
package org.openhab.binding.hdpowerview.internal.discovery;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;

import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers HD PowerView Generation 3 Gateways by means of mDNS.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
@Component
public class GatewayDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String LABEL_KEY = "discovery.gateway.label";

    private final Logger logger = LoggerFactory.getLogger(GatewayDiscoveryParticipant.class);

    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    @Activate
    public GatewayDiscoveryParticipant(final @Reference TranslationProvider i18nProvider,
            final @Reference LocaleProvider localeProvider) {
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        for (String host : service.getHostAddresses()) {
            if (VALID_IP_V4_ADDRESS.matcher(host).matches()) {
                ThingUID thingUID = new ThingUID(THING_TYPE_GATEWAY, host.replace('.', '_'));
                DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(HDPowerViewHubConfiguration.HOST, host)
                        .withRepresentationProperty(HDPowerViewHubConfiguration.HOST)
                        .withLabel(i18nProvider.getText(FrameworkUtil.getBundle(getClass()), LABEL_KEY, LABEL_KEY,
                                localeProvider.getLocale(), host))
                        .build();
                logger.debug("mDNS discovered Generation 3 Gateway on host '{}'", host);
                return hub;
            }
        }
        return null;
    }

    @Override
    public String getServiceType() {
        return "_powerview-g3._tcp.local.";
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_GATEWAY);
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        for (String host : service.getHostAddresses()) {
            if (VALID_IP_V4_ADDRESS.matcher(host).matches()) {
                return new ThingUID(THING_TYPE_GATEWAY, host.replace('.', '_'));
            }
        }
        return null;
    }
}
