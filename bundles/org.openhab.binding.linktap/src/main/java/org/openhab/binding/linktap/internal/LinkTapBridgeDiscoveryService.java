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
package org.openhab.binding.linktap.internal;

import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.*;
import static org.openhab.binding.linktap.internal.LinkTapBridgeHandler.MDNS_LOOKUP;
import static org.openhab.binding.linktap.internal.Utils.cleanPrintableChars;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linktap.protocol.frames.TLGatewayFrame;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * The {@link LinkTapBridgeDiscoveryService} is an implementation of a discovery service for VeSync devices. The
 * meta-data is
 * read by the bridge, and the discovery data updated via a callback implemented by the DeviceMetaDataUpdatedHandler.
 *
 * @author David Godyear - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "discovery.linktap")
public class LinkTapBridgeDiscoveryService implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_http._tcp.local.";
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_GATEWAY);
    private static final String RAW_MODEL = "model";
    private static final String RAW_ID = "ID";
    private static final String RAW_MAC = "MAC";
    private static final String RAW_IP = "IP";
    private static final String RAW_ADMIN_URL = "admin_url";
    private static final String RAW_VENDOR = "vendor";
    private static final String RAW_VERSION = "version";
    private static final String[] KEYS = new String[] { RAW_MODEL, RAW_ID, RAW_MAC, RAW_IP, RAW_ADMIN_URL, RAW_VENDOR,
            RAW_VERSION };

    private static final String TEXT_CHARSET = StandardCharsets.UTF_8.name();

    protected final ThingRegistry thingRegistry;
    private final Logger logger = LoggerFactory.getLogger(LinkTapBridgeDiscoveryService.class);
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;

    @Activate
    public LinkTapBridgeDiscoveryService(final @Reference ThingRegistry thingRegistry,
            @Reference TranslationProvider translationProvider, @Reference LocaleProvider localeProvider) {
        this.thingRegistry = thingRegistry;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
    }

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        final String itemId = String.format("%04X", new Random().nextInt(Short.MAX_VALUE));
        String qualifiedName = service.getQualifiedName();
        String name = service.getName();

        if (logger.isEnabledForLevel(Level.TRACE)) {
            logger.trace("[{}] Device found: {}", itemId, cleanPrintableChars(qualifiedName));
        }

        if (!name.startsWith("LinkTapGw_")) {
            logger.trace("[{}] Not a LinkTap Gateway - wrong name", itemId);
            return null;
        }
        if (80 != service.getPort()) {
            logger.trace("[{}] Not a LinkTap Gateway - incorrect port", itemId);
            return null;
        }

        if (!"tcp".equals(service.getProtocol())) {
            logger.trace("[{}] Not a LinkTap Gateway - incorrect protocol", itemId);
            return null;
        }

        if (!"http".equals(service.getApplication())) {
            logger.trace("[{}] Not a LinkTap Gateway - incorrect application", itemId);
            return null;
        }

        ThingUID uid = getThingUID(service);
        if (uid == null) {
            return null;
        }

        Properties rawDataProps = extractProps(service);
        if (rawDataProps.isEmpty()) {
            return null;
        }

        final Map<String, Object> bridgeProperties = new HashMap<>(4);
        final String gatewayId = getGwId(service.getName());
        bridgeProperties.put(BRIDGE_PROP_GW_ID, gatewayId);
        final String macId = (String) rawDataProps.get(RAW_MAC);
        if (macId != null) {
            bridgeProperties.put(BRIDGE_PROP_MAC_ADDR, macId);
        }
        final String version = (String) rawDataProps.get(RAW_VERSION);
        if (version != null) {
            bridgeProperties.put(BRIDGE_PROP_GW_VER, version);
        }
        final String hostname = getHostName(service);
        if (hostname.isEmpty()) {
            return null;
        }
        bridgeProperties.put(BRIDGE_CONFIG_HOSTNAME, qualifiedName);

        if (gatewayId.isEmpty()) {
            return null;
        }
        logger.debug("[{}] Discovered Gateway Id {}", itemId, gatewayId);

        final String ipV4Addr = (String) rawDataProps.get(RAW_IP);

        MDNS_LOOKUP.clearItem(qualifiedName);
        MDNS_LOOKUP.registerItem(qualifiedName, ipV4Addr, () -> {
            logger.debug("[{}] Registered mdns qualified name to IPv4 {} -> {}", itemId, qualifiedName, ipV4Addr);
            List<Thing> things = thingRegistry.getAll().stream()
                    .filter(thing -> THING_TYPE_GATEWAY.equals(thing.getThingTypeUID())).toList();
            for (final Thing thing : things) {
                final ThingHandler handler = thing.getHandler();
                if (handler instanceof LinkTapBridgeHandler bridgeHandler) {
                    bridgeHandler.attemptReconnectIfNeeded();
                    logger.trace("[{}] Bridge handler {} notified", itemId, handler.getThing().getLabel());
                }
            }
        });

        return DiscoveryResultBuilder.create((new ThingUID(THING_TYPE_GATEWAY, gatewayId)))
                .withProperties(bridgeProperties).withLabel("LinkTap Gateway (" + gatewayId + ")")
                .withRepresentationProperty(BRIDGE_PROP_GW_ID).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        final Map<String, Object> bridgeProperties = new HashMap<>(4);
        final String gatewayId = getGwId(service.getName());
        bridgeProperties.put(BRIDGE_PROP_GW_ID, gatewayId);
        if (bridgeProperties.get(BRIDGE_PROP_GW_ID) == null) {
            return null;
        }
        return (new ThingUID(THING_TYPE_GATEWAY,
                gatewayId + "_" + String.format("0x%08X", new Random().nextInt(Integer.MAX_VALUE))));
    }

    public Properties extractProps(ServiceInfo serviceInfo) {
        final Properties result = new Properties();
        String data = "";
        try {
            data = new String(serviceInfo.getTextBytes(), TEXT_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            logger.warn("{}", getLocalizedText("warning.discovery-charset-missing"));
        }
        final int[] keyIndexes = new int[7];

        for (int i = 0; i < KEYS.length; ++i) {
            keyIndexes[i] = data.indexOf(KEYS[i] + "=");
        }
        Arrays.sort(keyIndexes);
        if (keyIndexes[0] == -1) {
            return result;
        }

        String wCopy = data;
        for (int si = keyIndexes.length - 1; si > -1; --si) {
            final String foundField = wCopy.substring(keyIndexes[si]).trim();
            wCopy = wCopy.substring(0, keyIndexes[si]);
            final Optional<String> potentialKey = Arrays.stream(KEYS).filter(foundField::startsWith).findFirst();
            if (potentialKey.isPresent()) {
                final String key = potentialKey.get();
                result.put(key, foundField.substring(key.length() + 1));
            }
        }
        return result;
    }

    @Override
    public long getRemovalGracePeriodSeconds(ServiceInfo serviceInfo) {
        return MDNSDiscoveryParticipant.super.getRemovalGracePeriodSeconds(serviceInfo);
    }

    private String getGwId(final String serviceName) {
        String[] segments = serviceName.split("_");
        if (segments.length > 1) {
            return segments[1];
        }
        return TLGatewayFrame.EMPTY_STRING;
    }

    private String getHostName(final ServiceInfo serviceInfo) {
        final Inet4Address[] addrs = serviceInfo.getInet4Addresses();
        if (addrs.length == 0) {
            logger.trace("No IPv4 given in mdns data");
            return TLGatewayFrame.EMPTY_STRING;
        }
        String candidateDnsName = addrs[0].getHostName();
        if (candidateDnsName.isEmpty()) {
            logger.trace("No DNS given by IPv4 address from mdns data");
            candidateDnsName = addrs[0].toString();
        }
        if (candidateDnsName.startsWith("/")) {
            candidateDnsName = candidateDnsName.substring(1);
        }
        return candidateDnsName;
    }
}
