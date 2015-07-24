/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import org.openhab.binding.netatmo.internal.OAuthCredentials;

/**
 * Base class for all Netatmo API requests that needs to be authenticated by an
 * access token
 * 
 * @author Gaël L'hopital
 */
public abstract class AbstractTokenedRequest extends AbstractRequest {

	private OAuthCredentials credentials;

	/**
	 * Creates a request
	 * 
	 * @param resourceUrl
	 *            url api path of the request
	 * @param credentials
	 *            credentials to use to emit the request
	 */
	public AbstractTokenedRequest(String resourceUrl,
			OAuthCredentials credentials) {
		super(resourceUrl);

		this.credentials = credentials;

	}

	@Override
	protected StringBuilder getUrlBuilder() {
		StringBuilder urlBuilder = super.getUrlBuilder();
		urlBuilder.append("?access_token=");
		urlBuilder.append(credentials.accessToken);
		return urlBuilder;
	}

}
