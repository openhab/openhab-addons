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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxAirMediaReceiverRequest} is the Java class used to map the "AirMediaReceiverRequest"
 * structure used by the sending request to an AirMedia receiver API
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxAirMediaReceiverRequest {

    private enum MediaAction {
        START("start"),
        STOP("stop");

        private String action;

        private MediaAction(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }
    }

    private enum MediaType {
        VIDEO("video"),
        PHOTO("photo");

        private String mediaType;

        private MediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public String getMediaType() {
            return mediaType;
        }
    }

    private String action;
    private String mediaType;
    private String password;
    private Integer position;
    private String media;

    public void setStartAction() {
        this.action = MediaAction.START.getAction();
    }

    public void setStopAction() {
        this.action = MediaAction.STOP.getAction();
    }

    public void setVideoMediaType() {
        this.mediaType = MediaType.VIDEO.getMediaType();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setMedia(String media) {
        this.media = media;
    }
}
