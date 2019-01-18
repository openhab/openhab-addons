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
package org.openhab.binding.gpstracker.internal.provider;

import org.openhab.binding.gpstracker.internal.handler.TrackerHandler;

/**
 * Functional interface for checking tracker registration.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public interface TrackerRegistry {

    /**
     * Returns a handler for a given id
     * 
     * @param trackerId the id of the tracker
     * @return the handler or null if it does not exist
     */
    TrackerHandler getTrackerHandler(String trackerId);
}
