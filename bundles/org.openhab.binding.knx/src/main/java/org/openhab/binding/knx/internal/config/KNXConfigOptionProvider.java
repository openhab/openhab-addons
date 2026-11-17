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
package org.openhab.binding.knx.internal.config;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.i18n.KNXTranslationProvider;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.binding.knx.internal.security.KeyringFacts.InterfaceFacts;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.calimero.secure.Keyring.Interface.Type;

/**
 * Suggests the interfaces of the keyring a KNX IP bridge has loaded as values for its address parameters. The
 * parameters stay free text, so any address can still be typed.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@Component(service = ConfigOptionProvider.class)
public class KNXConfigOptionProvider implements ConfigOptionProvider {
    static final String TUNNEL_SOURCE_ADDRESS = "tunnelSourceAddress";
    static final String LOCAL_SOURCE_ADDRESS = "localSourceAddr";

    private final KNXHandlerFactory handlerFactory;

    @Activate
    public KNXConfigOptionProvider(final @Reference KNXHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        boolean tunnel = TUNNEL_SOURCE_ADDRESS.equals(param);
        if (!tunnel && !LOCAL_SOURCE_ADDRESS.equals(param)) {
            return null;
        }
        // MainUI asks with thing:<thingUID> for a thing that exists
        if (!"thing".equals(uri.getScheme())) {
            return null;
        }
        ThingUID thingUID;
        try {
            thingUID = new ThingUID(uri.getSchemeSpecificPart());
        } catch (IllegalArgumentException e) {
            return null;
        }
        KeyringFacts facts = null;
        for (KNXBridgeBaseThingHandler handler : handlerFactory.getBridges()) {
            if (handler instanceof IPBridgeThingHandler && thingUID.equals(handler.getThing().getUID())) {
                facts = handler.getKeyringFacts();
            }
        }
        if (facts == null) {
            return null;
        }
        List<ParameterOption> options = facts.interfaces().stream()
                .filter(iface -> (iface.type() == Type.Tunneling) == tunnel).map(KNXConfigOptionProvider::option)
                .toList();
        return options.isEmpty() ? null : options;
    }

    private static ParameterOption option(InterfaceFacts iface) {
        int linked = iface.linkedSecuredGroups().size();
        String address = iface.address().toString();
        var host = iface.host();
        String label = host == null ? KNXTranslationProvider.I18N.get("option.knx-interface-no-host", address, linked)
                : KNXTranslationProvider.I18N.get("option.knx-interface", address, host.toString(), linked);
        return new ParameterOption(address, label);
    }
}
