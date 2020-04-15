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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;
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
@NonNullByDefault
public class MarketplaceProxy {

    private static final String MP_URL = "https://marketplace.eclipse.org/taxonomy/term/4988%2C4396/api/p?client=org.eclipse.smarthome";
    private static final long REFRESH_INTERVAL = 3600;
    private static final long RETRY_DELAY = 60;

    private final Logger logger = LoggerFactory.getLogger(MarketplaceProxy.class);

    private Node[] cachedNodes = new Node[0];
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledFuture<?> refreshJob;
    private final URL url;

    /**
     * Creates a new instance, which immediately schedules a synchronization with the marketplace content.
     */
    public MarketplaceProxy() {
        try {
            url = new URL(MP_URL);
            refreshJob = executorService.scheduleWithFixedDelay(this::refresh, 0, REFRESH_INTERVAL, TimeUnit.SECONDS);
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
        return Collections.unmodifiableList(Arrays.asList(cachedNodes));
    }

    /**
     * Refreshes the local content by synchronizing with the remote marketplace.
     */
    public synchronized void refresh() {
        XmlDocumentReader<Marketplace> reader = new MarketplaceXMLReader();
        try {
            @Nullable
            Marketplace result = reader.readFromXML(url);
            if (result != null) {
                cachedNodes = result.categories[0].nodes;
            }
        } catch (Exception e) {
            if (cachedNodes.length == 0) {
                logger.warn("Failed downloading Marketplace entries: {}", e.getMessage());
                logger.warn("Retrying again in a minute");
                executorService.schedule(this::refresh, RETRY_DELAY, TimeUnit.SECONDS);
            } else {
                logger.debug("Cannot access IoT Marketplace - will continue to use cached results: {}", e.getMessage());
            }
        }
    }

    public void dispose() {
        if (!refreshJob.isCancelled()) {
            refreshJob.cancel(true);
        }
        executorService.shutdown();
    }
}
