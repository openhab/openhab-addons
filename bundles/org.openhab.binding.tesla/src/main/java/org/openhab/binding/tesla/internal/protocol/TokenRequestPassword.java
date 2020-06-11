/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal.protocol;

import java.security.GeneralSecurityException;

/**
 * The {@link TokenRequestPassword} is a datastructure to capture
 * authentication/credentials required to log into the
 * Tesla Remote Service
 *
 * @author Karel Goderis - Initial contribution
 * @author Nicolai Grødum - Adding token based auth
 */
@SuppressWarnings("unused")
public class TokenRequestPassword extends TokenRequest {

    private String grant_type = "password";
    private String email;
    private String password;

    public TokenRequestPassword(String email, String password) throws GeneralSecurityException {
        super();

        this.email = email;
        this.password = password;
    }

}
