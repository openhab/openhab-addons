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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The message returned to the client by the voices.list method.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class ListVoicesResponse {

    /**
     * The list of voices.
     */
    private @Nullable List<Voice> voices;

    public @Nullable List<Voice> getVoices() {
        return voices;
    }

    public void setVoices(List<Voice> voices) {
        this.voices = voices;
    }
}
