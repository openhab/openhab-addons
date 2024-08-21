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
package org.openhab.voice.mimic.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Mimic Voice DTO.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class VoiceDto {

    public String key = "UNDEFINED";
    public String language = "UNDEFINED";
    public String name = "UNDEFINED";
    @Nullable
    public List<String> speakers = new ArrayList<>();
}
