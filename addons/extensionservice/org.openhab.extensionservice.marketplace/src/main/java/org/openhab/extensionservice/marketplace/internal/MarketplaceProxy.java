/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;
import org.openhab.extensionservice.marketplace.internal.MarketplaceProxy;
import org.openhab.extensionservice.marketplace.internal.MarketplaceXMLReader;
import org.openhab.extensionservice.marketplace.internal.model.Marketplace;
import org.openhab.extensionservice.marketplace.internal.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides local access to the market place content. Once started, it downloads the catalog and then makes
 * its content available from memory.
 *
 * Note that there is no progressive/lazy browsing implemented yet, but the service downloads the whole catalog.
 * Once the marketplace is filled with a lot of content, this will need to be addressed.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class MarketplaceProxy {

    private final Logger logger = LoggerFactory.getLogger(MarketplaceProxy.class);

    private static final String MP_URL = "https://marketplace.eclipse.org/taxonomy/term/4988%2C4396/api/p?client=org.eclipse.smarthome";
    private final URL url;
    private Node[] cachedNodes = null;
    private long refresh_interval = 3600;
    private long retry_delay = 60;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> refreshJob;

    /**
     * Creates a new instance, which immediately schedules a synchronization with the marketplace content.
     */
    public MarketplaceProxy() {
        try {
            url = new URL(MP_URL);
            this.executorService = Executors.newSingleThreadScheduledExecutor();
            this.refreshJob = this.executorService.scheduleWithFixedDelay(() -> refresh(), 0, refresh_interval,
                    TimeUnit.SECONDS);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Something is very wrong - cannot instantiate URL " + MP_URL);
        }
    }

    /**
     * returns the full list of marketplace nodes
     *
     * @return list of marketplace nodes
     */
    public List<Node> getNodes() {
        return cachedNodes != null ? Arrays.asList(cachedNodes) : Collections.emptyList();
    }

    /**
     * Refreshes the local content by synchronizing with the remote marketplace.
     */
    public synchronized void refresh() {
        XmlDocumentReader<Marketplace> reader = new MarketplaceXMLReader();
        try {
            Marketplace result = reader.readFromXML(url);
            cachedNodes = result.categories[0].nodes;
        } catch (Exception e) {
            if (cachedNodes == null) {
                logger.warn("Failed downloading Marketplace entries: {}", e.getMessage());
                logger.warn("Retrying again in a minute");
                this.executorService.schedule(() -> refresh(), retry_delay, TimeUnit.SECONDS);
            } else {
                logger.debug("Cannot access IoT Marketplace - will continue to use cached results: {}", e.getMessage());
            }
        }
    }

    public void dispose() {
        if (this.refreshJob != null && !this.refreshJob.isCancelled()) {
            this.refreshJob.cancel(true);
            this.refreshJob = null;
        }
        this.executorService.shutdown();
    }
}
