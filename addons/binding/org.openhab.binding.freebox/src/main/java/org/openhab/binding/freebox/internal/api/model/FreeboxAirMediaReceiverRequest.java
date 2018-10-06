/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    private static final String START_ACTION = "start";
    private static final String STOP_ACTION = "stop";
    private static final String VIDEO = "video";

    private String action;
    private String mediaType;
    private String password;
    private Integer position;
    private String media;

    public void setStartAction() {
        this.action = START_ACTION;
    }

    public void setStopAction() {
        this.action = STOP_ACTION;
    }

    public void setVideoMediaType() {
        this.mediaType = VIDEO;
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
