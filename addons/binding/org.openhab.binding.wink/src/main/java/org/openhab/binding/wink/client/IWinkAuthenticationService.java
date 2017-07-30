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
 * This Interface abstracts away the token fetching method because getting an app key to get
 * tokens is a bit clunky. For now, the configuration requires that the service is provided with
 * an application id, a client secret and a refresh token in order to function. Ideally though,
 * this could be provided by a secure cloud service that dealt with the oauth bit and then the
 * user could just provide a non-expiring authentication token to that service.
 *
 * The service provides a method for retrieving and refreshing an access token.
 *
 * @author scrosby
 *
 */
public interface IWinkAuthenticationService {
    /**
     * Returns the current access token persisted in the service
     *
     * @return String access token
     */
    public String getAuthToken();

    /**
     * Returns a refreshed access token and hopefully persists the token for the next call
     * to the getAuthToken() method.
     *
     * @return String newly refreshed access token
     */
    public String refreshToken() throws AuthenticationException;
}
