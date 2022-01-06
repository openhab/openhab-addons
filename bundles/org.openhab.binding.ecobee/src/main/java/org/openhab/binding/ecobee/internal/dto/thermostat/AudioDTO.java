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
package org.openhab.binding.ecobee.internal.dto.thermostat;

import java.util.List;

/**
 * The {@link AudioDTO} contains all the audio properties of the thermostat
 * (only applicable to ecobee4).
 *
 * All read-only except for voiceEngines.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class AudioDTO {

    /*
     * The volume level for audio playback. This includes volume of the voice assistant. A value between 0 and 100.
     */
    public Integer playbackVolume;

    /*
     * Turn microphone (privacy mode) on and off.
     */
    public Boolean microphoneEnabled;

    /*
     * The volume level for alerts on the thermostat. A value between 0 and 10, with 0 meaning 'off' - the zero
     * value may not be honored by all ecobee versions.
     */
    public Integer soundAlertVolume;

    /*
     * The volume level for key presses on the thermostat. A value between 0 and 10, with 0 meaning 'off' - the
     * zero value may not be honored by all ecobee versions.
     */
    public Integer soundTickVolume;

    /*
     * The list of voice engines compatible with the selected thermostat.
     */
    public List<VoiceEngineDTO> voiceEngines;
}
