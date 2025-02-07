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
package org.openhab.binding.amazonechocontrol.internal.dto;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

/**
 * The {@link PlaySearchPhraseTO} encapsulates the payload to validate a search phrase
 *
 * @author Jan N. Klug - Initial contribution
 */
public class PlaySearchPhraseTO {
    @SuppressWarnings("unchecked")
    public static final TypeToken<BehaviorOperationValidationResultTO<PlaySearchPhraseTO>> VALIDATION_RESULT_TO_TYPE_TOKEN = (TypeToken<BehaviorOperationValidationResultTO<PlaySearchPhraseTO>>) TypeToken
            .getParameterized(BehaviorOperationValidationResultTO.class, PlaySearchPhraseTO.class);

    public String deviceType = "ALEXA_CURRENT_DEVICE_TYPE";
    public String deviceSerialNumber = "ALEXA_CURRENT_DSN";
    public String locale = "ALEXA_CURRENT_LOCALE";
    public String customerId;
    public String searchPhrase;
    public String sanitizedSearchPhrase;
    public String musicProviderId = "ALEXA_CURRENT_DSN";

    @Override
    public @NonNull String toString() {
        return "PlaySearchPhraseTO{deviceType='" + deviceType + "', deviceSerialNumber='" + deviceSerialNumber
                + "', locale='" + locale + "', customerId='" + customerId + "', searchPhrase='" + searchPhrase
                + "', sanitizedSearchPhrase='" + sanitizedSearchPhrase + "', musicProviderId='" + musicProviderId
                + "'}";
    }
}
