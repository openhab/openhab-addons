/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

    private static final long serialVersionUID = -4053305903339688036L;

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
     * @param trackerRegistry Tracker registry
     */
    public OwnTracksCallbackServlet(TrackerDiscoveryService discoveryService, TrackerRegistry trackerRegistry) {
        super(discoveryService, trackerRegistry);
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
