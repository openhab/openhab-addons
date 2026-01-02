/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.blink.internal.service;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkCommand;

import com.google.gson.Gson;

/**
 * The {@link NetworkService} class handles all communication with camera related blink apis.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class NetworkService extends BaseBlinkApiService {
    public NetworkService(HttpClient httpClient, Gson gson) {
        super(httpClient, gson);
    }

    /**
     * Arms or disarms the network
     *
     * @param account blink account
     * @param networkId network id
     * @param enable arm/disarm
     * @return command id
     */
    public Long arm(@Nullable BlinkAccount account, @Nullable String networkId, boolean enable) throws IOException {
        if (account == null || account.account == null || networkId == null)
            throw new IllegalArgumentException("Cannot call network arm api without account or network");
        String action = (enable) ? "/state/arm" : "/state/disarm";
        String uri = "/api/v1/accounts/" + account.account.account_id + "/networks/" + networkId + action;
        BlinkCommand cmd = apiRequest(account.account.tier, uri, HttpMethod.POST, account.auth.token, null,
                BlinkCommand.class);
        return cmd.id;
    }
}
