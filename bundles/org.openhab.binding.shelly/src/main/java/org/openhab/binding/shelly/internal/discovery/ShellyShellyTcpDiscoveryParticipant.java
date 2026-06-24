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
package org.openhab.binding.shelly.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Supplements {@link ShellyMDNSDiscoveryParticipant} for Gen2+ devices that advertise via
 * {@code _shelly._tcp.local.} rather than (or in addition to) {@code _http._tcp.local.}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class)
public class ShellyShellyTcpDiscoveryParticipant extends ShellyMDNSDiscoveryParticipant {

    @Activate
    public ShellyShellyTcpDiscoveryParticipant(@Reference ConfigurationAdmin configurationAdmin,
            @Reference HttpClientFactory httpClientFactory, @Reference LocaleProvider localeProvider,
            @Reference ShellyTranslationProvider translationProvider, @Reference ShellyThingTable thingTable,
            @Reference NetworkAddressService networkAddressService, ComponentContext componentContext) {
        super(configurationAdmin, httpClientFactory, localeProvider, translationProvider, thingTable,
                networkAddressService, componentContext);
    }

    @Override
    public String getServiceType() {
        return "_shelly._tcp.local.";
    }
}
