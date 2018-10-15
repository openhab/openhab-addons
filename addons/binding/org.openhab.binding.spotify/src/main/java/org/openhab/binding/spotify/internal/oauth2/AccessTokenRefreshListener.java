/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.oauth2;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Handler to act up on changes of the access token.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public interface AccessTokenRefreshListener {

    /**
     * Called when the access token has changed.
     *
     * @param accessToken the new access token
     */
    void onTokenResponse(AccessTokenResponse accessToken);
}
