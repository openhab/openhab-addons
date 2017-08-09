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
 * This is a singleton instance of the authentication service to get tokens from the wink rest api
 *
 * @author Shawn Crosby
 *
 */
public class WinkAuthenticationService {
    private static IWinkAuthenticationService instance;

    /**
     * Returns a singleton instance of the current authentication service
     *
     * @return
     */
    public static IWinkAuthenticationService getInstance() {
        if (instance == null) {
            synchronized (WinkAuthenticationService.class) {
                if (instance == null) {
                    instance = new DummyService();
                }
            }
        }
        return instance;
    }

    /**
     * Creates a new singleton authentication service
     *
     * @param service
     */
    public static void setInstance(IWinkAuthenticationService service) {
        instance = service;
    }

    private static class DummyService implements IWinkAuthenticationService {

        @Override
        public String getAuthToken() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String refreshToken() throws AuthenticationException {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
