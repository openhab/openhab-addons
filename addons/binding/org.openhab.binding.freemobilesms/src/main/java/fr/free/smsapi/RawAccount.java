/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package fr.free.smsapi;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An {@link Account} implementation.
 *
 * @author Guilhem Bonnefille - Initial contribution
 */
@NonNullByDefault
public class RawAccount implements Account {

    protected final String user;
    protected final String password;

    public RawAccount(String user, String password) {
        this.user = user;
        this.password = password;
    }


    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
