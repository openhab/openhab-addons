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
 * A devicelist request returns the list of devices owned by the user, and their
 * modules.
 * 
 * @author Andreas Brenk - Initial OH1 version
 * @author Gaël L'hopital - Port to OH2
 * @see <a href="http://dev.netatmo.com/doc/restapi/devicelist">devicelist</a>
 */
public class DeviceListRequest extends AbstractTokenedRequest {

	private final String deviceId;

	/**
	 * Creates a request for the list of devices owned by the user, and their
	 * modules.
	 * 
	 * @param accessToken
	 *            mandatory, must not be <code>null</code>
	 */
	public DeviceListRequest(OAuthCredentials credentials) {
		this(credentials, null);
	}

	/**
	 * Creates a request for a specific device owned by the user, and their
	 * modules.
	 * 
	 * @param accessToken
	 *            mandatory, must not be <code>null</code>
	 * @param deviceId
	 *            Id of the requested device
	 */
	public DeviceListRequest(OAuthCredentials credentials, final String deviceId) {
		super("api/devicelist", credentials);

		this.deviceId = deviceId;
	}

	@Override
	protected StringBuilder getUrlBuilder() {
		StringBuilder urlBuilder = super.getUrlBuilder();
		if (deviceId != null) {
			urlBuilder.append("&device_id=");
			urlBuilder.append(this.deviceId);
		}
		return urlBuilder;
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = createToStringBuilder();
		builder.appendSuper(super.toString());
		builder.append("deviceId", this.deviceId);

		return builder.toString();
	}

}
