/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openhab.binding.netatmo.internal.OAuthCredentials;

/**
 * Gets a renewed refresh token from the Netatmo API to use in future
 * measurement requests.
 * 
 * @author Andreas Brenk
 * @since 1.4.0
 * @see <a
 *      href="http://dev.netatmo.com/doc/authentication/refreshtoken">refreshtoken</a>
 */
public class RefreshTokenRequest extends AbstractRequest {

	private static final String CONTENT = "grant_type=refresh_token&refresh_token=%s&client_id=%s&client_secret=%s&scope=read_station";

	private final String clientId;
	private final String clientSecret;
	private final String refreshToken;

	public RefreshTokenRequest(OAuthCredentials credentials) {
		super("oauth2/token");
		this.clientId = credentials.clientId;
		this.clientSecret = credentials.clientSecret;
		this.refreshToken = credentials.refreshToken;
	}

	public String getContent() {
		return String.format(CONTENT, this.refreshToken, this.clientId,
				this.clientSecret);
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = createToStringBuilder();
		builder.appendSuper(super.toString());
		builder.append("clientId", this.clientId);
		builder.append("clientSecret", this.clientSecret);
		builder.append("refreshToken", this.refreshToken);

		return builder.toString();
	}
}
