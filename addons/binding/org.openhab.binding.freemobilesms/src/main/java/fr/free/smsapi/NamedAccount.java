/**
 * Copyright (c) 2014,2019 by the respective copyright holders.
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

public class NamedAccount extends RawAccount {

    protected final String name;

    public NamedAccount(String name, String user, String password) {
        super(user, password);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
