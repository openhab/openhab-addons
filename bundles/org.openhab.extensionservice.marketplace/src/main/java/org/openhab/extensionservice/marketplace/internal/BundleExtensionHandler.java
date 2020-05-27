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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.openhab.extensionservice.marketplace.MarketplaceExtension;
import org.openhab.extensionservice.marketplace.MarketplaceExtensionHandler;
import org.openhab.extensionservice.marketplace.MarketplaceHandlerException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MarketplaceExtensionHandler} implementation, which handles extensions as jar files (specifically, OSGi
 * bundles) and installs
 * them through the standard OSGi bundle installation mechanism.
 * The information, which installed bundle corresponds to which extension is written to a file in the bundle's data
 * store. It is therefore wiped together with the bundles upon an OSGi "clean".
 * We might want to move this class into a separate bundle in future, when we add support for further extension types.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(immediate = true)
public class BundleExtensionHandler implements MarketplaceExtensionHandler {
    // this is for backwards compatibility; if we don't see BUNDLE_FILE but
    // we do see this, we migrate its contents to BUNDLE_FILE
    private static final String BINDING_FILE = "installedBindingsMap.csv";

    private static final String BUNDLE_FILE = "installedBundlesMap.csv";

    // extension types supported by this handler
    private static final List<String> SUPPORTED_EXT_TYPES = Arrays.asList(MarketplaceExtension.EXT_TYPE_BINDING,
            MarketplaceExtension.EXT_TYPE_VOICE);

    private final Logger logger = LoggerFactory.getLogger(BundleExtensionHandler.class);

    private Map<String, Long> installedBundles;

    private BundleContext bundleContext;

    @Activate
    protected void activate(BundleContext bundleContext, Map<String, Object> config) {
        this.bundleContext = bundleContext;
        installedBundles = loadInstalledBundlesMap();
    }

    @Deactivate
    protected void deactivate() {
        this.installedBundles = null;
        this.bundleContext = null;
    }

    @Override
    public boolean supports(MarketplaceExtension ext) {
        // we support only certain extension types, and only as pure OSGi bundles
        return SUPPORTED_EXT_TYPES.contains(ext.getType())
                && ext.getPackageFormat().equals(MarketplaceExtension.EXT_FORMAT_BUNDLE);
    }

    @Override
    public boolean isInstalled(MarketplaceExtension ext) {
        return installedBundles.containsKey(ext.getId());
    }

    @Override
    public void install(MarketplaceExtension ext) throws MarketplaceHandlerException {
        String url = ext.getDownloadUrl();
        try {
            Bundle bundle = bundleContext.installBundle(url);
            try {
                bundle.start();
            } catch (BundleException e) {
                logger.warn("Installed bundle, but failed to start it: {}", e.getMessage());
            }
            installedBundles.put(ext.getId(), bundle.getBundleId());
            persistInstalledBundlesMap(installedBundles);
        } catch (BundleException e) {
            logger.debug("Failed to install bundle from marketplace.", e);
            throw new MarketplaceHandlerException("Bundle cannot be installed: " + e.getMessage());
        }
    }

    @Override
    public void uninstall(MarketplaceExtension ext) throws MarketplaceHandlerException {
        Long id = installedBundles.get(ext.getId());
        if (id != null) {
            Bundle bundle = bundleContext.getBundle(id);
            if (bundle != null) {
                try {
                    bundle.stop();
                    bundle.uninstall();
                    installedBundles.remove(ext.getId());
                    persistInstalledBundlesMap(installedBundles);
                } catch (BundleException e) {
                    throw new MarketplaceHandlerException("Failed deinstalling bundle: " + e.getMessage());
                }
            } else {
                // we do not have such a bundle, so let's remove it from our internal map
                installedBundles.remove(ext.getId());
                persistInstalledBundlesMap(installedBundles);
                throw new MarketplaceHandlerException("Id not known.");
            }
        } else {
            throw new MarketplaceHandlerException("Id not known.");
        }
    }

    private Map<String, Long> loadInstalledBundlesMap() {
        File dataFile = bundleContext.getDataFile(BUNDLE_FILE);
        if (dataFile != null && dataFile.exists()) {
            return loadInstalledBundlesFile(dataFile);
        }
        File obsoleteFile = bundleContext.getDataFile(BINDING_FILE);
        if (obsoleteFile != null & obsoleteFile.exists()) {
            return loadInstalledBundlesFile(obsoleteFile);
        }
        return new HashMap<>();
    }

    private Map<String, Long> loadInstalledBundlesFile(File dataFile) {
        try (FileReader reader = new FileReader(dataFile)) {
            LineIterator lineIterator = IOUtils.lineIterator(reader);
            Map<String, Long> map = new HashMap<>();
            while (lineIterator.hasNext()) {
                String line = lineIterator.nextLine();
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    try {
                        map.put(parts[0], Long.valueOf(parts[1]));
                    } catch (NumberFormatException e) {
                        logger.debug("Cannot parse '{}' as a number in file {} - ignoring it.", parts[1],
                                dataFile.getName());
                    }
                } else {
                    logger.debug("Invalid line in file {} - ignoring it:\n{}", dataFile.getName(), line);
                }
            }
            return map;
        } catch (IOException e) {
            logger.debug("File '{}' for installed bundles does not exist.", dataFile.getName());
            // ignore and just return an empty map
        }
        return new HashMap<>();
    }

    private synchronized void persistInstalledBundlesMap(Map<String, Long> map) {
        File dataFile = bundleContext.getDataFile(BUNDLE_FILE);
        if (dataFile != null) {
            try (FileWriter writer = new FileWriter(dataFile)) {
                for (Entry<String, Long> entry : map.entrySet()) {
                    writer.write(entry.getKey() + ";" + entry.getValue() + System.lineSeparator());
                }
            } catch (IOException e) {
                logger.warn("Failed writing file '{}': {}", dataFile.getName(), e.getMessage());
            }
        } else {
            logger.debug("System does not support bundle data files -> not persisting installed bundle info");
        }
    }
}
