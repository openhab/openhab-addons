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
package org.openhab.binding.freeboxos.internal.api.rest;

import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.MediaReceiverManager.Receiver;

/**
 * The {@link MediaReceiverManager} is the Java class used to handle api requests related to air media receivers
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MediaReceiverManager extends ListableRest<Receiver, MediaReceiverManager.ReceiverResponse> {
    private static final String SUB_PATH = "receivers";

    public static record Receiver(boolean passwordProtected, //
            Map<MediaType, Boolean> capabilities, //
            String name // This name is the UPnP name of the host
    ) {
    }

    protected static class ReceiverResponse extends Response<Receiver> {
    }

    public enum Action {
        START,
        STOP,
        UNKNOWN
    }

    public enum MediaType {
        VIDEO,
        PHOTO,
        AUDIO,
        SCREEN,
        UNKNOWN
    }

    private static record Request(String password, Action action, MediaType mediaType, @Nullable String media,
            int position) {
    }

    public MediaReceiverManager(FreeboxOsSession session, UriBuilder uriBuilder) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, ReceiverResponse.class, uriBuilder.path(SUB_PATH));
    }

    public @Nullable Receiver getReceiver(String receiverName) throws FreeboxException {
        return getDevices().stream().filter(rcv -> receiverName.equals(rcv.name())).findFirst().orElse(null);
    }

    public void sendToReceiver(String receiver, String password, Action action, MediaType type)
            throws FreeboxException {
        sendToReceiver(receiver, new Request(password, action, type, null, 0));
    }

    public void sendToReceiver(String receiver, String password, Action action, MediaType type, String url)
            throws FreeboxException {
        sendToReceiver(receiver, new Request(password, action, type, url, 0));
    }

    private void sendToReceiver(String receiver, Request payload) throws FreeboxException {
        post(payload, GenericResponse.class, receiver);
    }
}
