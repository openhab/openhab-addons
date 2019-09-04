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
package org.openhab.binding.innogysmarthome.internal.client;

import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InnogyCredentialRefreshListener} is injected in the google oauth library and called, when the access
 * tokens timed out. It is used to update the {@link InnogyConfig}, to keep the valid tokens up to date.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class InnogyCredentialRefreshListener implements AccessTokenRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(InnogyCredentialRefreshListener.class);
    private InnogyConfig config;

    public InnogyCredentialRefreshListener(InnogyConfig config) {
        this.config = config;
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        config.setAccessToken(tokenResponse.getAccessToken());
        logger.debug("innogy access token saved (onTokenResponse): {}", tokenResponse.getAccessToken());
    }
}
