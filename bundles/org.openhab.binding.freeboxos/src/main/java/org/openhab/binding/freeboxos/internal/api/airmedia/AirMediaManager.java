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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ConfigurableRest;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaActionData.MediaAction;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaActionData.MediaType;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaConfig.AirMediaConfigResponse;

/**
 * The {@link AirMediaManager} is the Java class used to handle api requests
 * related to air media
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirMediaManager extends ConfigurableRest<AirMediaConfig, AirMediaConfigResponse> {
    private static final String RECEIVERS_SUB_PATH = "receivers";
    private static final String AIR_MEDIA_PATH = "airmedia";

    private final URI receiverUri;

    public AirMediaManager(FreeboxOsSession session) {
        super(AIR_MEDIA_PATH, CONFIG_SUB_PATH, session, AirMediaConfigResponse.class);
        receiverUri = getUriBuilder().path(RECEIVERS_SUB_PATH).build();
    }

    public List<AirMediaReceiver> getEquipments() throws FreeboxException {
        return getList(AirMediaReceiversResponse.class, receiverUri);
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
        post(String.format("%s/%s/", RECEIVERS_SUB_PATH, encodedReceiver), payload);
    }

    // Response classes
    private static class AirMediaReceiversResponse extends Response<List<AirMediaReceiver>> {
    }
}
