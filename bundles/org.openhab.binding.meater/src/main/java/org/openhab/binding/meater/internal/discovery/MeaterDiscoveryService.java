/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.meater.internal.discovery;

import static org.openhab.binding.meater.internal.MeaterBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meater.internal.MeaterConfiguration;
import org.openhab.binding.meater.internal.handler.MeaterBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link MeaterDiscoveryService} searches for available
 * Meater probes discoverable through MEATER REST API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = MeaterDiscoveryService.class)
@NonNullByDefault
public class MeaterDiscoveryService extends AbstractThingHandlerDiscoveryService<MeaterBridgeHandler> {
    private static final int SEARCH_TIME = 2;

    public MeaterDiscoveryService() {
        super(MeaterBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
    }

    @Reference(unbind = "-")
    public void bindTranslationProvider(TranslationProvider translationProvider) {
        this.i18nProvider = translationProvider;
    }

    @Reference(unbind = "-")
    public void bindLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    @Override
    protected void startScan() {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        thingHandler.getMeaterThings().entrySet().stream().forEach(thing -> {
            thingDiscovered(
                    DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_MEATER_PROBE, bridgeUID, thing.getKey()))
                            .withLabel("@text/discovery.probe.label").withBridge(bridgeUID)
                            .withProperty(MeaterConfiguration.DEVICE_ID_LABEL, thing.getKey())
                            .withRepresentationProperty(MeaterConfiguration.DEVICE_ID_LABEL).build());
        });
    }
}
