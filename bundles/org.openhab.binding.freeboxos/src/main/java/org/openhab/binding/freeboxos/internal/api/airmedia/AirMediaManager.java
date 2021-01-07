/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.airmedia;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.ListResponse;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaActionData.MediaAction;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaActionData.MediaType;
import org.openhab.binding.freeboxos.internal.handler.ApiHandler;

/**
 * The {@link AirMediaManager} is the Java class used to handle api requests
 * related to air media
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirMediaManager extends RestManager {

    public AirMediaManager(ApiHandler apiHandler) {
        super(apiHandler);
    }

    public List<AirMediaReceiver> getReceivers() throws FreeboxException {
        return apiHandler.getList("airmedia/receivers/", AirMediaReceiversResponse.class, true);
    }

    public AirMediaConfig getConfig() throws FreeboxException {
        return apiHandler.get("airmedia/config/", AirMediaConfigResponse.class, true);
    }

    public AirMediaConfig setConfig(AirMediaConfig config) throws FreeboxException {
        return apiHandler.put("airmedia/config/", config, AirMediaConfigResponse.class);
    }

    public void sendToReceiver(String receiver, String password, MediaAction action, MediaType type)
            throws FreeboxException {
        sendToReceiver(receiver, new AirMediaActionData(password, action, type));
    }

    public void sendToReceiver(String receiver, String password, MediaAction action, MediaType type, String url)
            throws FreeboxException {
        sendToReceiver(receiver, new AirMediaActionData(password, action, type, url));
    }

    private void sendToReceiver(String receiver, AirMediaActionData payload) throws FreeboxException {
        String encodedReceiver = URLEncoder.encode(receiver, StandardCharsets.UTF_8);
        apiHandler.post(String.format("airmedia/receivers/%s/", encodedReceiver), payload);
    }

    // Response classes and validity evaluations
    private static class AirMediaConfigResponse extends Response<AirMediaConfig> {
    }

    private static class AirMediaReceiversResponse extends ListResponse<AirMediaReceiver> {
    }
}
