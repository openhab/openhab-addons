/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.voice.openaitts.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Artur Fedjukevits - Initial contribution
 */
@NonNullByDefault
public class OpenAITTSConfiguration {

    public String apiKey = "";
    public String apiUrl = "https://api.openai.com/v1/audio/speech";
    public String model = "tts-1";
    public Double speed = 1.0;
    public String instructions = "";
}
