/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.extensionservice.marketplace.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.extension.Extension;
import org.eclipse.smarthome.core.extension.ExtensionEventFactory;
import org.eclipse.smarthome.core.extension.ExtensionService;
import org.eclipse.smarthome.core.extension.ExtensionType;
import org.openhab.extensionservice.marketplace.MarketplaceExtension;
import org.openhab.extensionservice.marketplace.MarketplaceExtensionHandler;
import org.openhab.extensionservice.marketplace.MarketplaceHandlerException;
import org.openhab.extensionservice.marketplace.internal.model.Node;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an {@link ExtensionService}, which accesses the Eclipse IoT Marketplace and makes its content available as
 * extensions.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(configurationPid = "org.eclipse.smarthome.marketplace", immediate = true, property = {
        Constants.SERVICE_PID + "=org.eclipse.smarthome.marketplace", //
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=system", //
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=system:marketplace", //
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=Marketplace" //
})
public class MarketplaceExtensionService implements ExtensionService {
    /**
     * Enumeration of supported extension package types plus associated attributes.
     */
    private enum PackageType {
        BINDING("binding", MarketplaceExtension.EXT_TYPE_BINDING, "bindings", "Bindings"),
        RULE_TEMPLATE("rule_template", MarketplaceExtension.EXT_TYPE_RULE_TEMPLATE, "ruletemplates", "Rule Templates"),
        VOICE("voice", MarketplaceExtension.EXT_TYPE_VOICE, "voice", "Voice");

        /**
         * Constant used in marketplace nodes.
         */
        final String typeName;

        /**
         * MarketplaceExtension.EXT_TYPE_ symbolic name.
         */
        final String extType;

        /**
         * Key used in config file for setting visibility property.
         */
        final String configKey;

        /**
         * Label to display on Paper UI tab.
         */
        final String label;

        private PackageType(String typeName, String extType, String configKey, String label) {
            this.typeName = typeName;
            this.extType = extType;
            this.configKey = configKey;
            this.label = label;
        }
    }

    private static final String MARKETPLACE_HOST = "marketplace.eclipse.org";
    private static final Pattern EXTENSION_ID_PATTERN = Pattern.compile(".*?mpc_install=([^&]+?)(&.*)?");

    private final Logger logger = LoggerFactory.getLogger(MarketplaceExtensionService.class);

    // increased visibility for unit tests
    MarketplaceProxy proxy;
    private EventPublisher eventPublisher;
    private final Pattern labelPattern = Pattern.compile("<.*>"); // checks for the existence of any xml element
    private final Pattern descriptionPattern = Pattern.compile("<(javascript|div|font)"); // checks for the existence of
                                                                                          // some
    // invalid elements

    // configured package type inclusion settings, keyed by package typeName
    private Map<String, Boolean> packageTypeInclusions = new HashMap<>();

    private int maturityLevel = 1;
    private final Set<MarketplaceExtensionHandler> extensionHandlers = new HashSet<>();

    @Activate
    protected void activate(Map<String, Object> config) {
        this.proxy = new MarketplaceProxy();
        modified(config);
    }

    @Deactivate
    protected void deactivate() {
        this.proxy.dispose();
        this.proxy = null;
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        for (PackageType packageType : PackageType.values()) {
            Object inclusionCfg = config.get(packageType.configKey);
            if (inclusionCfg != null) {
                packageTypeInclusions.put(packageType.typeName,
                        inclusionCfg.toString().equals(Boolean.TRUE.toString()));
            }
        }
        Object cfgMaturityLevel = config.get("maturity");
        if (cfgMaturityLevel != null) {
            try {
                this.maturityLevel = Integer.valueOf(cfgMaturityLevel.toString());
            } catch (NumberFormatException e) {
                logger.warn("Ignoring invalid value '{}' for configuration parameter '{}'", cfgMaturityLevel.toString(),
                        "maturity");
            }
        }
    }

    @Reference
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC)
    protected void addExtensionHandler(MarketplaceExtensionHandler handler) {
        this.extensionHandlers.add(handler);
    }

    protected void removeExtensionHandler(MarketplaceExtensionHandler handler) {
        this.extensionHandlers.remove(handler);
    }

    @Override
    public List<Extension> getExtensions(Locale locale) {
        List<Node> nodes = proxy.getNodes();
        List<Extension> exts = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            if (node.id == null) {
                // workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=512493
                continue;
            }
            if (toMaturityLevel(node.status) < this.maturityLevel) {
                continue;
            }
            if (!packageTypeInclusions.getOrDefault(node.packagetypes, true)) {
                continue;
            }

            MarketplaceExtension ext = convertToExtension(node);
            if (ext != null) {
                if (setInstalledFlag(ext)) {
                    exts.add(ext);
                }
            }
        }
        return exts;
    }

    private boolean setInstalledFlag(MarketplaceExtension ext) {
        for (MarketplaceExtensionHandler handler : extensionHandlers) {
            if (handler.supports(ext)) {
                ext.setInstalled(handler.isInstalled(ext));
                return true;
            }
        }
        return false;
    }

    private MarketplaceExtension convertToExtension(Node node) {
        String extId = getExtensionId(node);

        String name = node.name;
        String desc = node.shortdescription;
        String version = StringUtils.isNotEmpty(node.version) ? node.version : "1.0";

        if (!validName(name) || !validDescription(desc)) {
            logger.debug("Ignoring node {} due to invalid content.", node.id);
            return null;
        }
        for (PackageType packageType : PackageType.values()) {
            if (packageType.typeName.equals(node.packagetypes)) {
                MarketplaceExtension ext = new MarketplaceExtension(extId, packageType.extType, name, version,
                        node.supporturl, false, desc, null, node.image, node.updateurl, node.packageformat);
                return ext;
            }
        }
        return null;
    }

    @Override
    public Extension getExtension(String id, Locale locale) {
        for (Extension extension : getExtensions(locale)) {
            if (extension.getId().equals(id)) {
                return extension;
            }
        }
        return null;
    }

    @Override
    public List<ExtensionType> getTypes(Locale locale) {
        ArrayList<ExtensionType> types = new ArrayList<>(2);
        List<Extension> exts = getExtensions(locale);
        for (PackageType packageType : PackageType.values()) {
            if (packageTypeInclusions.getOrDefault(packageType.typeName, true)) {
                for (Extension ext : exts) {
                    if (ext.getType().equals(packageType.extType)) {
                        types.add(new ExtensionType(packageType.extType, packageType.label));
                        break;
                    }
                }
            }
        }
        return Collections.unmodifiableList(types);
    }

    @Override
    public void install(String extensionId) {
        Extension ext = getExtension(extensionId, null);
        if (ext instanceof MarketplaceExtension) {
            MarketplaceExtension mpExt = (MarketplaceExtension) ext;
            for (MarketplaceExtensionHandler handler : extensionHandlers) {
                if (handler.supports(mpExt)) {
                    if (!handler.isInstalled(mpExt)) {
                        try {
                            handler.install(mpExt);
                            postInstalledEvent(extensionId);
                        } catch (MarketplaceHandlerException e) {
                            postFailureEvent(extensionId, e.getMessage());
                        }
                    } else {
                        postFailureEvent(extensionId, "Extension is already installed.");
                    }
                    return;
                }
            }
        }
        postFailureEvent(extensionId, "Extension not known.");
    }

    @Override
    public void uninstall(String extensionId) {
        Extension ext = getExtension(extensionId, null);
        if (ext instanceof MarketplaceExtension) {
            MarketplaceExtension mpExt = (MarketplaceExtension) ext;
            for (MarketplaceExtensionHandler handler : extensionHandlers) {
                if (handler.supports(mpExt)) {
                    if (handler.isInstalled(mpExt)) {
                        try {
                            handler.uninstall(mpExt);
                            postUninstalledEvent(extensionId);
                        } catch (MarketplaceHandlerException e) {
                            postFailureEvent(extensionId, e.getMessage());
                        }
                    } else {
                        postFailureEvent(extensionId, "Extension is not installed.");
                    }
                    return;
                }
            }
        }
        postFailureEvent(extensionId, "Extension not known.");
    }

    @Override
    public String getExtensionId(URI extensionURI) {
        if (extensionURI != null && extensionURI.getHost().equals(MARKETPLACE_HOST)) {
            return extractExensionId(extensionURI);
        }

        return null;
    }

    private void postInstalledEvent(String extensionId) {
        Event event = ExtensionEventFactory.createExtensionInstalledEvent(extensionId);
        eventPublisher.post(event);
    }

    private void postUninstalledEvent(String extensionId) {
        Event event = ExtensionEventFactory.createExtensionUninstalledEvent(extensionId);
        eventPublisher.post(event);
    }

    private void postFailureEvent(String extensionId, String msg) {
        Event event = ExtensionEventFactory.createExtensionFailureEvent(extensionId, msg);
        eventPublisher.post(event);
    }

    private String getExtensionId(Node node) {
        StringBuilder sb = new StringBuilder(MarketplaceExtension.EXT_PREFIX);
        boolean found = false;
        for (PackageType packageType : PackageType.values()) {
            if (packageType.typeName.equals(node.packagetypes)) {
                sb.append(packageType.extType).append("-");
                sb.append(node.id.replaceAll("[^a-zA-Z0-9_]", ""));
                return sb.toString();
            }
        }
        return null;
    }

    private int toMaturityLevel(String maturity) {
        switch (maturity) {
            case "Alpha":
                return 0;
            case "Beta":
                return 1;
            case "Production/Stable":
                return 2;
            case "Mature":
                return 3;
            default:
                logger.debug("Unknown maturity level value '{}' - using 'Alpha' instead.", maturity);
                return 0;
        }
    }

    private boolean validName(String name) {
        return !labelPattern.matcher(name).find();
    }

    private boolean validDescription(String desc) {
        return !descriptionPattern.matcher(desc).find();
    }

    private String extractExensionId(URI uri) {
        Matcher idMatcher = EXTENSION_ID_PATTERN.matcher(uri.getQuery());
        String id = null;
        if (idMatcher.matches() && idMatcher.groupCount() > 1) {
            id = idMatcher.group(1);
        }

        Optional<Node> extensionNode = getExtensionNode(id);

        return extensionNode.isPresent() ? getExtensionId(extensionNode.get()) : null;
    }

    private Optional<Node> getExtensionNode(String id) {
        return proxy.getNodes().stream().filter(node -> node != null && node.id.equals(id)).findFirst();
    }
}
