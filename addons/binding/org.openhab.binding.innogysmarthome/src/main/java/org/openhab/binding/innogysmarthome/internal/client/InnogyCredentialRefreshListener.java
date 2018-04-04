/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;

/**
 * The {@link InnogyCredentialRefreshListener} is injected in the google oauth library and called, when the access
 * tokens timed out. It is used to update the {@link InnogyConfig}, to keep the valid tokens up to date.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class InnogyCredentialRefreshListener implements CredentialRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(InnogyCredentialRefreshListener.class);
    private InnogyConfig config;

    public InnogyCredentialRefreshListener(InnogyConfig config) {
        this.config = config;
    }

    @Override
    public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
        config.setAccessToken(credential.getAccessToken());
        logger.debug("innogy access token saved (onTokenResponse): {}", credential.getAccessToken());
    }

    @Override
    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {
        config.setAccessToken(credential.getAccessToken());
        logger.debug("innogy access token saved (onTokenErrorResponse): {}", credential.getAccessToken());
    }

}
