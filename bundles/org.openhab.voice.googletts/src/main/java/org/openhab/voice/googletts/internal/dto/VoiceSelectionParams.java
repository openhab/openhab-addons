/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * Description of which voice to use for a synthesis request.
 *
 * @author Wouter Born - Initial contribution
 */
public class VoiceSelectionParams {

    /**
     * The language (and optionally also the region) of the voice expressed as a BCP-47 language tag, e.g. "en-US".
     * Required. This should not include a script tag (e.g. use "cmn-cn" rather than "cmn-Hant-cn"), because the script
     * will be inferred from the input provided in the SynthesisInput. The TTS service will use this parameter to help
     * choose an appropriate voice. Note that the TTS service may choose a voice with a slightly different language code
     * than the one selected; it may substitute a different region (e.g. using en-US rather than en-CA if there isn't a
     * Canadian voice available), or even a different language, e.g. using "nb" (Norwegian Bokmal) instead of "no"
     * (Norwegian)".
     */
    private String languageCode;

    /**
     * The name of the voice. Optional; if not set, the service will choose a voice based on the other parameters such
     * as languageCode and gender.
     */
    private String name;

    /**
     * The preferred gender of the voice. Optional; if not set, the service will choose a voice based on the other
     * parameters such as languageCode and name. Note that this is only a preference, not requirement; if a voice of the
     * appropriate gender is not available, the synthesizer should substitute a voice with a different gender rather
     * than failing the request.
     */
    private SsmlVoiceGender ssmlGender;

    public VoiceSelectionParams() {
    }

    public VoiceSelectionParams(String languageCode, String name, SsmlVoiceGender ssmlGender) {
        this.languageCode = languageCode;
        this.name = name;
        this.ssmlGender = ssmlGender;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getName() {
        return name;
    }

    public SsmlVoiceGender getSsmlGender() {
        return ssmlGender;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSsmlGender(SsmlVoiceGender ssmlGender) {
        this.ssmlGender = ssmlGender;
    }
}
