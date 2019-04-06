/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wizlighting.internal.discovery;

/**
 * This is {@link TokenDTO} Object to parse Discovery response of access tokens.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class TokenDTO {
    public String access_token;
    public long expires_in;
    public String token_type;
    public String scope;
    public String refresh_token;
}
