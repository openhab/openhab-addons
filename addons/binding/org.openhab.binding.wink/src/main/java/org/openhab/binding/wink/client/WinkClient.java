/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.client;

/**
 * This is a singleton instance of a wink client for communicating with the wink rest api.
 *
 * @author Shawn Crosby
 *
 */
public class WinkClient {
    private static IWinkClient instance;

    /**
     * Get a singleton instance of the wink client
     *
     * @return
     */
    public static synchronized IWinkClient getInstance() {
        if (instance == null) {
            instance = new CloudRestfulWinkClient();
        }

        return instance;
    }

    /**
     * Allows for setting an instance of a new client. Mostly for unit tests
     *
     * @param testClient
     */
    public static synchronized void setInstance(IWinkClient testClient) {
        instance = testClient;
    }
}
