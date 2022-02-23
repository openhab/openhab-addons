/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaActionData.MediaAction;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaActionData.MediaType;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaReceiver.AirMediaReceiverResponse;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaReceiver.AirMediaReceiversResponse;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.ListableRest;

/**
 * The {@link MediaReceiverManager} is the Java class used to handle api requests
 * related to air media
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MediaReceiverManager
        extends ListableRest<AirMediaReceiver, AirMediaReceiverResponse, AirMediaReceiversResponse> {
    private static final String RECEIVERS_SUB_PATH = "receivers";

    public MediaReceiverManager(FreeboxOsSession session, UriBuilder uriBuilder) {
        super(session, AirMediaReceiverResponse.class, AirMediaReceiversResponse.class, uriBuilder, RECEIVERS_SUB_PATH);
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
        post(payload, receiver);
    }
}
