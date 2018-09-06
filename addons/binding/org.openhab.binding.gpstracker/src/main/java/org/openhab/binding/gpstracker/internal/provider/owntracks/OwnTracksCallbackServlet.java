/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.provider.owntracks;


import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.provider.AbstractCallbackServlet;
import org.openhab.binding.gpstracker.internal.provider.TrackerRegistry;

/**
 * Callback servlet for OwnTracks trackers
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class OwnTracksCallbackServlet extends AbstractCallbackServlet {
    /**
     * Servlet path
     */
    private static final String CALLBACK_PATH = "/gpstracker/owntracks";

    /**
     * Provider name
     */
    private static final String PROVIDER = "OwnTracks";

    /**
     * Constructor called at binding startup.
     *
     * @param discoveryService Discovery service for new trackers.
     * @param trackerRegistryImpl Tracker registry implementation
     */
    public OwnTracksCallbackServlet(TrackerDiscoveryService discoveryService, TrackerRegistry trackerRegistryImpl) {
        super(discoveryService, trackerRegistryImpl);
    }

    @Override
    public String getPath() {
        return CALLBACK_PATH;
    }

    @Override
    protected String getProvider() {
        return PROVIDER;
    }
}
