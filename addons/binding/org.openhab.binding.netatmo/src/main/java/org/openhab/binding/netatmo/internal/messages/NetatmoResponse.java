/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.openhab.binding.netatmo.internal.NetatmoException;

/**
 * Java Bean to represent a the generic part of a Netatmo API request response.
 * Specific part is held by the Body field
 * 
 * @author Gaël L'hopital
 * 
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class NetatmoResponse<T> extends AbstractMessage {

	/**
	 * Sample error response:
	 * 
	 * <pre>
	 * {"error": {"code": 9, "message": "Device not found" }}
	 * </pre>
	 * 
	 * @author Andreas Brenk
	 * @since 1.4.0
	 */
	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	public static class ErrorPart extends AbstractMessage {

		protected int code;
		protected String message;

		/**
		 * <ul>
		 * <li>1 : No access token given to the API
		 * <li>2 : The access token is not valid
		 * <li>3 : The access token has expired
		 * <li>4 : Internal error
		 * <li>5 : The application has been deactivated
		 * <li>9 : The device has not been found
		 * <li>10 : A mandatory API parameter is missing
		 * <li>11 : An unexpected error occured
		 * <li>13 : Operation not allowed
		 * <li>15 : Installation of the device has not been finalized
		 * <li>21 : Invalid argument
		 * <li>25 : Invalid date given
		 * <li>26 : Maximum usage of the API has been reached by application
		 * </ul>
		 * 
		 * @see #isAccessTokenExpired()
		 */
		public int getCode() {
			return this.code;
		}

		public String getMessage() {
			return this.message;
		}

		public boolean isAccessTokenExpired() {
			return this.code == 3;
		}

		public boolean isAccessTokenInvalid() {
			return this.code == 2;
		}

		@Override
		public String toString() {
			final ToStringBuilder builder = createToStringBuilder();
			builder.appendSuper(super.toString());

			builder.append("code", getCode());
			builder.append("message", getMessage());

			return builder.toString();
		}
	}

	protected ErrorPart error;
	protected String status;
	protected T body;
	protected Double time_exec;
	protected Double time_server;

	public ErrorPart getError() {
		return this.error;
	}

	public boolean isError() {
		return this.error != null;
	}

	public T getBody() {
		return this.body;
	}

	public void evaluate() throws NetatmoException {
		if (isError()) {
			throw new NetatmoException(this);
		}
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = createToStringBuilder();
		builder.appendSuper(super.toString());

		if (isError()) {
			builder.append("error", this.error);
		}
		;

		return builder.toString();
	}
}
