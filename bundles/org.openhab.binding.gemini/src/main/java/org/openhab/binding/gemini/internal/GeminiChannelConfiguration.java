/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.gemini.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link GeminiChannelConfiguration} class contains the configuration for a Gemini chat channel.
 * Values can be null/empty, in which case they fall back to the defaults from the thing-type.xml.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class GeminiChannelConfiguration {
    public @Nullable String model;
    public @Nullable Double temperature;
    public @Nullable Double topP;
    public @Nullable Integer maxOutputTokens;
    public @Nullable String systemMessage;
}
