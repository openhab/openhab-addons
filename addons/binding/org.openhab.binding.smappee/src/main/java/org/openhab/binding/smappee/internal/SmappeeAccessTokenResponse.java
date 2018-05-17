/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.internal;

/**
 * Interface for OAuth access token response
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeAccessTokenResponse {

    public String access_token;
    public int expires_in;
    public String refresh_token;
}