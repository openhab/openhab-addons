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
package org.openhab.binding.mycroft.internal.api.dto;

import org.openhab.binding.mycroft.internal.api.MessageType;

/**
 * This message is sent in response to a VolumeGet message
 * with the current volume in Mycroft
 * NOT FUNCTIONAL
 * (see https://community.mycroft.ai/t/openhab-plugin-development-audio-volume-message-types-missing/10576)
 *
 * @author Gwendal Roulleau - Initial contribution
 */
public class MessageVolumeGetResponse extends BaseMessage {

    public Data data = new Data();

    public MessageVolumeGetResponse() {
        this.type = MessageType.mycroft_volume_get_response;
    }

    public static class Data {
        public float percent = 0;
        public Boolean muted = false;
    }
}
