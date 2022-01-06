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
package org.openhab.voice.googletts.internal.dto;

/**
 * Gender of the voice as described in SSML voice element.
 *
 * @author Wouter Born - Initial contribution
 */
public enum SsmlVoiceGender {

    /**
     * An unspecified gender. In VoiceSelectionParams, this means that the client doesn't care which gender the selected
     * voice will have. In the Voice field of ListVoicesResponse, this may mean that the voice doesn't fit any of the
     * other categories in this enum, or that the gender of the voice isn't known.
     */
    SSML_VOICE_GENDER_UNSPECIFIED,

    /**
     * A male voice.
     */
    MALE,

    /**
     * A female voice.
     */
    FEMALE,

    /**
     * A gender-neutral voice.
     */
    NEUTRAL

}
