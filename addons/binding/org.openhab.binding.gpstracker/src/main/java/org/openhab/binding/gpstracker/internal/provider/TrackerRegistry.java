/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
