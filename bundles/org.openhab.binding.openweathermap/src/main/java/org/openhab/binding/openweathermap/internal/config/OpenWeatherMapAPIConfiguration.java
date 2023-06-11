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
package org.openhab.binding.openweathermap.internal.config;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.handler.OpenWeatherMapAPIHandler;

/**
 * The {@link OpenWeatherMapAPIConfiguration} is the class used to match the {@link OpenWeatherMapAPIHandler}s
 * configuration.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapAPIConfiguration {
    // supported languages (see https://openweathermap.org/current#multi)
    public static final Set<String> SUPPORTED_LANGUAGES = Set.of("af", "al", "ar", "az", "bg", "ca", "cz", "da", "de",
            "el", "en", "es", "sp", "eu", "fa", "fi", "fr", "gl", "he", "hi", "hr", "hu", "id", "it", "ja", "kr", "la",
            "lt", "mk", "nl", "no", "pl", "pt", "pt_br", "ro", "ru", "se", "sv", "sk", "sl", "sr", "th", "tr", "uk",
            "ua", "vi", "zh_cn", "zh_tw", "zu");

    public @Nullable String apikey;
    public int refreshInterval;
    public @Nullable String language;
    public String apiVersion = "2.5";
}
