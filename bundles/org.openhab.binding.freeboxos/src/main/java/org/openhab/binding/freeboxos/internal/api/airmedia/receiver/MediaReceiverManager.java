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
package org.openhab.binding.freeboxos.internal.api.airmedia.receiver;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.RECEIVERS_SUB_PATH;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.MediaAction;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.MediaType;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.rest.ListableRest;

/**
 * The {@link MediaReceiverManager} is the Java class used to handle api requests related to air media
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MediaReceiverManager extends ListableRest<AirMediaReceiver, MediaReceiverManager.ReceiverResponse> {
    public static class ReceiverResponse extends Response<AirMediaReceiver> {
    }

    public MediaReceiverManager(FreeboxOsSession session, UriBuilder uriBuilder) throws FreeboxException {
        super(session, Permission.NONE, ReceiverResponse.class, uriBuilder.path(RECEIVERS_SUB_PATH));
    }

    public void sendToReceiver(String receiver, String password, MediaAction action, MediaType type)
            throws FreeboxException {
        sendToReceiver(receiver, new AirMediaReceiverRequest(password, action, type));
    }

    public void sendToReceiver(String receiver, String password, MediaAction action, MediaType type, String url)
            throws FreeboxException {
        sendToReceiver(receiver, new AirMediaReceiverRequest(password, action, type, url));
    }

    private void sendToReceiver(String receiver, AirMediaReceiverRequest payload) throws FreeboxException {
        post(payload, receiver);
    }
}
