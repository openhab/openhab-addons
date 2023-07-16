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
package org.openhab.binding.connectedcar.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity.OAuthToken;

/**
 * {@link BrandAuthenticator} defines the interface for brand specific authentication support/flow
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public interface BrandAuthenticator {
    public String getLoginUrl(IdentityOAuthFlow oauth) throws ApiException;

    public ApiIdentity login(String loginUrl, IdentityOAuthFlow oauth) throws ApiException;

    public String updateAuthorizationUrl(String url) throws ApiException;

    public IdentityOAuthFlow updateSigninParameters(IdentityOAuthFlow oauth) throws ApiException;

    public ApiIdentity grantAccess(IdentityOAuthFlow oauth) throws ApiException;

    public OAuthToken refreshToken(ApiIdentity token) throws ApiException;
}
