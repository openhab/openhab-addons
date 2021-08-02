/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;

/**
 * {@link BrandAuthenticator} defines the interface for brand specific authentication support/flow
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface BrandAuthenticator {
    public String getLoginUrl() throws ApiException;

    public ApiToken login(String loginUrl, TokenOAuthFlow oauth) throws ApiException;

    public ApiToken grantAccess(TokenOAuthFlow oauth) throws ApiException;

    public ApiToken refreshToken(CombinedConfig config, ApiToken token) throws ApiException;

    public String updateAuthorizationUrl(String url) throws ApiException;
}
