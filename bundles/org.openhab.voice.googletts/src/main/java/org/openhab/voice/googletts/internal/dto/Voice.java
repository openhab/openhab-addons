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
package org.openhab.voice.googletts.internal.dto;

import java.util.List;

/**
 * Description of a voice supported by the TTS service.
 *
 * @author Wouter Born - Initial contribution
 */
public class Voice {

    /**
     * The languages that this voice supports, expressed as BCP-47 language tags (e.g. "en-US", "es-419", "cmn-tw").
     */
    private List<String> languageCodes;

    /**
     * The name of this voice. Each distinct voice has a unique name.
     */
    private String name;

    /**
     * The natural sample rate (in hertz) for this voice.
     */
    private Long naturalSampleRateHertz;

    /**
     * The gender of this voice.
     */
    private SsmlVoiceGender ssmlGender;

    public List<String> getLanguageCodes() {
        return languageCodes;
    }

    public void setLanguageCodes(List<String> languageCodes) {
        this.languageCodes = languageCodes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getNaturalSampleRateHertz() {
        return naturalSampleRateHertz;
    }

    public void setNaturalSampleRateHertz(Long naturalSampleRateHertz) {
        this.naturalSampleRateHertz = naturalSampleRateHertz;
    }

    public SsmlVoiceGender getSsmlGender() {
        return ssmlGender;
    }

    public void setSsmlGender(SsmlVoiceGender ssmlGender) {
        this.ssmlGender = ssmlGender;
    }
}
