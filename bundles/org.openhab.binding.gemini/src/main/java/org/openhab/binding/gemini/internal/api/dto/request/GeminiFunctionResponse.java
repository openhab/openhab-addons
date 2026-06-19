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
package org.openhab.binding.gemini.internal.api.dto.request;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The {@link GeminiFunctionResponse} record represents the execution result of a function call
 * sent back to the model as context.
 *
 * @author Florian Hotze - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NonNullByDefault
public record GeminiFunctionResponse(@Nullable String name, @Nullable Map<String, Object> response) {
}
