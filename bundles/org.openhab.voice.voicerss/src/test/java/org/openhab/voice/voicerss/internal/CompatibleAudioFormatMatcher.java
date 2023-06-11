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
package org.openhab.voice.voicerss.internal;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.openhab.core.audio.AudioFormat;

/**
 * Hamcrest {@link Matcher} to assert a compatible {@link AudioFormat}.
 *
 * @author Andreas Brenk - Initial contribution
 */
public class CompatibleAudioFormatMatcher extends TypeSafeMatcher<AudioFormat> {

    private final AudioFormat audioFormat;

    public CompatibleAudioFormatMatcher(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    @Override
    protected boolean matchesSafely(AudioFormat actual) {
        return audioFormat.isCompatible(actual);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an audio format compatible to ").appendValue(audioFormat);
    }

    /**
     * Creates a matcher that matches when the examined object is
     * compatible to the specified <code>audioFormat</code>.
     *
     * @param audioFormat the audio format which must be compatible
     */
    public static Matcher<AudioFormat> compatibleAudioFormat(AudioFormat audioFormat) {
        return new CompatibleAudioFormatMatcher(audioFormat);
    }
}
