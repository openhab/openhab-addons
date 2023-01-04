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
package org.openhab.binding.mycroft.internal.api.dto;

import org.openhab.binding.mycroft.internal.api.MessageType;

/**
 * This message asks Mycroft to give information about
 * the title played on its underlying player.
 * Work in progress
 * 
 * @author Gwendal Roulleau - Initial contribution
 */
public class MessageAudioTrackInfo extends BaseMessage {

    public MessageAudioTrackInfo() {
        this.type = MessageType.mycroft_audio_service_track_info;
    }
}
