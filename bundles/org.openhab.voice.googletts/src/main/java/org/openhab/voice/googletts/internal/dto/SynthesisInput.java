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
 * Contains text input to be synthesized. Either text or ssml must be supplied. Supplying both or neither returns
 * google.rpc.Code.INVALID_ARGUMENT. The input size is limited to 5000 characters.
 *
 * @author Wouter Born - Initial contribution
 */
public class SynthesisInput {

    /**
     * The SSML document to be synthesized. The SSML document must be valid and well-formed. Otherwise the RPC will fail
     * and return google.rpc.Code.INVALID_ARGUMENT.
     */
    private String ssml;

    /**
     * The raw text to be synthesized.
     */
    private String text;

    public SynthesisInput() {
    }

    public SynthesisInput(String text) {
        if (text.startsWith("<speak>")) {
            ssml = text;
        } else {
            this.text = text;
        }
    }

    public String getSsml() {
        return ssml;
    }

    public String getText() {
        return text;
    }

    public void setSsml(String ssml) {
        this.ssml = ssml;
    }

    public void setText(String text) {
        this.text = text;
    }
}
