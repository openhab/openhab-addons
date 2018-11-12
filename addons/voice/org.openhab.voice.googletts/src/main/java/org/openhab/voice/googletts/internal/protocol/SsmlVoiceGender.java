/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.googletts.internal.protocol;

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
