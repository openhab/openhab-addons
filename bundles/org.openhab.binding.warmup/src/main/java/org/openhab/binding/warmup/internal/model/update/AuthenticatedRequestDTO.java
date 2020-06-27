/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.warmup.internal.model.update;

/**
 * @author James Melville - Initial contribution
 */
@SuppressWarnings("unused")
public class AuthenticatedRequestDTO {

    private AccountDTO account;
    private ModeDTO request;

    public AuthenticatedRequestDTO(String email, String token, ModeDTO request) {
        setAccount(new AccountDTO(email, token));
        setRequest(request);
    }

    public void setAccount(AccountDTO account) {
        this.account = account;
    }

    public void setRequest(ModeDTO request) {
        this.request = request;
    }
}
