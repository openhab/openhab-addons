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
package org.openhab.binding.pirateweather.internal.config;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pirateweather.internal.handler.PirateWeatherAPIHandler;

/**
 * The {@link PirateWeatherAPIConfiguration} is the class used to match the {@link PirateWeatherAPIHandler}s
 * configuration.
 *
 * @author Scott Hanson - Pirate Weather convertion
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PirateWeatherAPIConfiguration {

    // supported languages (see https://pirateweather.net/dev/docs#forecast-request)
    public static final Set<String> SUPPORTED_LANGUAGES = Collections.unmodifiableSet(Stream.of("ar", "az", "be", "bg",
            "bn", "bs", "ca", "cs", "da", "de", "el", "en", "eo", "es", "et", "fi", "fr", "he", "hi", "hr", "hu", "id",
            "is", "it", "ja", "ka", "kn", "ko", "kw", "lv", "mr", "nb", "nl", "no", "pa", "pl", "pt", "ro", "ru", "sk",
            "sl", "sr", "sv", "ta", "te", "tet", "tr", "uk", "x-pig-latin", "zh", "zh-tw").collect(Collectors.toSet()));

    public @Nullable String apikey;
    public int refreshInterval = 60;
    public @Nullable String language;
}
