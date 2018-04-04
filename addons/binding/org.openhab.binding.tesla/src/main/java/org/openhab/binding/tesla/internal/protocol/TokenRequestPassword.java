/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
