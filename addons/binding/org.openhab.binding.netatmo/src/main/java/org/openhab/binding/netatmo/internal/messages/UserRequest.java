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
 * A user request returns information about a user such as preferred language,
 * Preferred units, and list of devices.
 * 
 * @author Gaël L'hopital
 */
public class UserRequest extends AbstractTokenedRequest {

	public UserRequest(OAuthCredentials credentials) {
		super("api/getuser", credentials);
	}

}
