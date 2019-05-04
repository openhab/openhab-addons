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
package org.openhab.binding.freemobilesms.internal;

import fr.free.smsapi.Account;

/**
 * The {@link FreeMobileSmsConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Guilhem Bonnefille - Initial contribution
 */
public class FreeMobileSmsConfiguration implements Account {

    /**
     * User configuration parameter.
     */
    public String user;

    /**
     * Password configuration parameter.
     */
    public String password;

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
