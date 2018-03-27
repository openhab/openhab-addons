/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla.internal.protocol;

/**
 * The {@link TokenResponse} is a datastructure to capture
 * authentication response from Tesla Remote Service
 *
 * @author Nicolai Grødum
 */
public class TokenResponse {

    public String access_token;
    public String token_type;
    public Long expires_in;
    public Long created_at;
    public String refresh_token;

    public TokenResponse() {
    }
}
