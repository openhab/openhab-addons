/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

/**
 * Java bean to handle API response elements in regard of a RefreshToken request
 * 
 * @author Gaël L'hopital
 * 
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class RefreshTokenResponse extends AbstractMessage {
	protected String access_token;
	protected String refresh_token;
	protected Integer expires_in;
	protected Integer expire_in;
	protected List<String> scope;

	public String getAccessToken() {
		return this.access_token;
	}

	public String getRefreshToken() {
		return this.refresh_token;
	}

	public Integer getExpiresIn() {
		return this.expires_in;
	}

	public Integer getExpireIn() {
		return this.expire_in;
	}

	public List<String> getScope() {
		return this.scope;
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = createToStringBuilder();
		builder.appendSuper(super.toString());

		builder.append("accessToken", getAccessToken());
		builder.append("refreshToken", getRefreshToken());
		builder.append("expiresIn", getExpiresIn());
		builder.append("expireIn", getExpireIn());
		builder.append("scope", getScope());

		return builder.toString();
	}
}
