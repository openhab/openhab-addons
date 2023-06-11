/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonPlaySearchPhraseOperationPayload} encapsulate the GSON for validation requests and results
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonPlaySearchPhraseOperationPayload {
    public @Nullable String deviceType = "ALEXA_CURRENT_DEVICE_TYPE";
    public @Nullable String deviceSerialNumber = "ALEXA_CURRENT_DSN";
    public @Nullable String locale = "ALEXA_CURRENT_LOCALE";
    public @Nullable String customerId;
    public @Nullable String searchPhrase;
    public @Nullable String sanitizedSearchPhrase;
    public @Nullable String musicProviderId = "ALEXA_CURRENT_DSN";
}
