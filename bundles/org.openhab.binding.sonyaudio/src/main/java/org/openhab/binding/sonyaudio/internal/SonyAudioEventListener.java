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
package org.openhab.binding.sonyaudio.internal;

import java.util.EventListener;

import org.openhab.binding.sonyaudio.internal.protocol.SonyAudioConnection;

/**
 * The {@link SonyAudioEventListener} event listener interface
 * handlers.
 *
 * @author David - Initial contribution
 */
public interface SonyAudioEventListener extends EventListener {
    void updateConnectionState(boolean connected);

    void updateInput(int zone, SonyAudioConnection.SonyAudioInput input);

    void updateSeekStation(String seek);

    void updateCurrentRadioStation(int radioStation);

    void updateVolume(int zone, SonyAudioConnection.SonyAudioVolume volume);

    void updatePowerStatus(int zone, boolean power);
}
