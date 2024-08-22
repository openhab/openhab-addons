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
package org.openhab.binding.visualcrossing.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

import static org.openhab.core.thing.ChannelUID.CHANNEL_GROUP_SEPARATOR;

/**
 * The {@link VisualCrossingBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class VisualCrossingBindingConstants {

    public static final String BINDING_ID = "visualcrossing";
    public static final Set<String> SUPPORTED_LANGUAGES = Set.of("ar", "bg", "cs", "da", "de", "el", "en", "es", "fa",
            "fi", "fr", "he", "hu,", "it", "ja", "ko", "nl", "pl", "pt", "ru", "sk", "sr", "sv", "tr", "uk", "vi",
            "zh");
    public static final ThingTypeUID WEATHER_THING_TYPE = new ThingTypeUID(BINDING_ID, "weather");

    public static final class Channels {
        public static final class BasicChannelGroup {
            private static final String CHANNEL_ID = "basic"+ CHANNEL_GROUP_SEPARATOR;

            public static final String COST = CHANNEL_ID + "cost";
            public static final String DESCRIPTION = CHANNEL_ID + "description";
        }

        public static final class CurrentConditions {
            private static final String CHANNEL_ID = "current-conditions"+ CHANNEL_GROUP_SEPARATOR;

            public static final String DATE_TIME = CHANNEL_ID + "date-time";
            public static final String TIME_STAMP = CHANNEL_ID + "time-stamp";
            public static final String TEMPERATURE = CHANNEL_ID + "temperature";
            public static final String FEELS_LIKE = CHANNEL_ID + "feels-like";
            public static final String HUMIDITY = CHANNEL_ID + "humidity";
            public static final String DEW = CHANNEL_ID + "dew";
            public static final String PRECIP = CHANNEL_ID + "precip";
            public static final String PRECIP_PROB = CHANNEL_ID + "precip-prob";
            public static final String PRECIP_TYPE = CHANNEL_ID + "precip-type";
            public static final String SNOW = CHANNEL_ID + "snow";
            public static final String SNOW_DEPTH = CHANNEL_ID + "snow-depth";
            public static final String WIND_GUST = CHANNEL_ID + "wind-gust";
            public static final String WIND_SPEED = CHANNEL_ID + "wind-speed";
            public static final String WIND_DIR = CHANNEL_ID + "wind-dir";
            public static final String PRESSURE = CHANNEL_ID + "pressure";
            public static final String VISIBILITY = CHANNEL_ID + "visibility";
            public static final String CLOUD_COVER = CHANNEL_ID + "cloud-cover";
            public static final String SOLAR_RADIATION = CHANNEL_ID + "solar-radiation";
            public static final String SOLAR_ENERGY = CHANNEL_ID + "solar-energy";
            public static final String UV_INDEX = CHANNEL_ID + "uv-index";
            public static final String CONDITIONS = CHANNEL_ID + "conditions";
            public static final String ICON = CHANNEL_ID + "icon";
            public static final String STATIONS = CHANNEL_ID + "stations";
            public static final String SOURCE = CHANNEL_ID + "source";
            public static final String SUNRISE = CHANNEL_ID + "sunrise";
            public static final String SUNRISE_EPOCH = CHANNEL_ID + "sunrise-epoch";
            public static final String SUNSET = CHANNEL_ID + "sunset";
            public static final String SUNSET_EPOCH = CHANNEL_ID + "sunset-epoch";
            public static final String MOON_PHASE = CHANNEL_ID + "moon-phase";
        }

        public static final class ChannelDay {
            public static final int NR_OF_DAYS = 15;
            public static final int NR_OF_HOURS = 24;
            private final String channelId;

            public ChannelDay(int idx) {
                var prefix = "day" + (idx < 10 ? "0" : "");
                this.channelId = prefix + idx;
            }

            public String dateTime() {
                return "%s#date-time".formatted(channelId);
            }

            public String timeStamp() {
                return "%s#time-stamp".formatted(channelId);
            }

            public String temperature() {
                return "%s#temperature".formatted(channelId);
            }

            public String temperatureMin() {
                return "%s#temperature-min".formatted(channelId);
            }

            public String temperatureMax() {
                return "%s#temperature-max".formatted(channelId);
            }

            public String feelsLike() {
                return "%s#feels-like".formatted(channelId);
            }

            public String feelsLikeMin() {
                return "%s#feels-like-min".formatted(channelId);
            }

            public String feelsLikeMax() {
                return "%s#feels-like-max".formatted(channelId);
            }

            public String dew() {
                return "%s#dew".formatted(channelId);
            }

            public String humidity() {
                return "%s#humidity".formatted(channelId);
            }

            public String precip() {
                return "%s#precip".formatted(channelId);
            }

            public String precipProb() {
                return "%s#precip-prob".formatted(channelId);
            }

            public String precipType() {
                return "%s#precip-type".formatted(channelId);
            }

            public String precipCover() {
                return "%s#precip-cover".formatted(channelId);
            }

            public String snow() {
                return "%s#snow".formatted(channelId);
            }

            public String snowDepth() {
                return "%s#snow-depth".formatted(channelId);
            }

            public String windGust() {
                return "%s#wind-gust".formatted(channelId);
            }

            public String windSpeed() {
                return "%s#wind-speed".formatted(channelId);
            }

            public String windDir() {
                return "%s#wind-dir".formatted(channelId);
            }

            public String pressure() {
                return "%s#pressure".formatted(channelId);
            }

            public String cloudCover() {
                return "%s#cloud-cover".formatted(channelId);
            }

            public String visibility() {
                return "%s#visibility".formatted(channelId);
            }

            public String solarRadiation() {
                return "%s#solar-radiation".formatted(channelId);
            }

            public String solarEnergy() {
                return "%s#solar-energy".formatted(channelId);
            }

            public String uvIndex() {
                return "%s#uv-index".formatted(channelId);
            }

            public String sunrise() {
                return "%s#sunrise".formatted(channelId);
            }

            public String sunriseEpoch() {
                return "%s#sunrise-epoch".formatted(channelId);
            }

            public String sunset() {
                return "%s#sunset".formatted(channelId);
            }

            public String sunsetEpoch() {
                return "%s#sunset-epoch".formatted(channelId);
            }

            public String moonPhase() {
                return "%s#moon-phase".formatted(channelId);
            }

            public String conditions() {
                return "%s#conditions".formatted(channelId);
            }

            public String description() {
                return "%s#description".formatted(channelId);
            }

            public String icon() {
                return "%s#icon".formatted(channelId);
            }

            public String stations() {
                return "%s#stations".formatted(channelId);
            }

            public String source() {
                return "%s#source".formatted(channelId);
            }

            public String severeRisk() {
                return "%s#severe-risk".formatted(channelId);
            }

            public Hour hour(int idx) {
                return new Hour(idx);
            }

            public class Hour {
                private final String hourIdx;

                public Hour(int idx) {
                    var prefix = "hour" + (idx < 10 ? "0" : "");
                    this.hourIdx = prefix + idx;
                }

                public String hourDateTime() {
                    return "%s#%s-date-time".formatted(channelId, hourIdx);
                }

                public String hourTimeStamp() {
                    return "%s#%s-time-stamp".formatted(channelId, hourIdx);
                }

                public String hourTemperature() {
                    return "%s#%s-temperature".formatted(channelId, hourIdx);
                }

                public String hourFeelsLike() {
                    return "%s#%s-feels-like".formatted(channelId, hourIdx);
                }

                public String hourHumidity() {
                    return "%s#%s-humidity".formatted(channelId, hourIdx);
                }

                public String hourDew() {
                    return "%s#%s-dew".formatted(channelId, hourIdx);
                }

                public String hourPrecip() {
                    return "%s#%s-precip".formatted(channelId, hourIdx);
                }

                public String hourPrecipProb() {
                    return "%s#%s-precip-prob".formatted(channelId, hourIdx);
                }

                public String hourPrecipType() {
                    return "%s#%s-precip-type".formatted(channelId, hourIdx);
                }

                public String hourSnow() {
                    return "%s#%s-snow".formatted(channelId, hourIdx);
                }

                public String hourSnowDepth() {
                    return "%s#%s-snow-depth".formatted(channelId, hourIdx);
                }

                public String hourWindGust() {
                    return "%s#%s-wind-gust".formatted(channelId, hourIdx);
                }

                public String hourWindSpeed() {
                    return "%s#%s-wind-speed".formatted(channelId, hourIdx);
                }

                public String hourWindDir() {
                    return "%s#%s-wind-dir".formatted(channelId, hourIdx);
                }

                public String hourPressure() {
                    return "%s#%s-pressure".formatted(channelId, hourIdx);
                }

                public String hourVisibility() {
                    return "%s#%s-visibility".formatted(channelId, hourIdx);
                }

                public String hourCloudCover() {
                    return "%s#%s-cloud-cover".formatted(channelId, hourIdx);
                }

                public String hourSolarRadiation() {
                    return "%s#%s-solar-radiation".formatted(channelId, hourIdx);
                }

                public String hourSolarEnergy() {
                    return "%s#%s-solar-energy".formatted(channelId, hourIdx);
                }

                public String hourUvIndex() {
                    return "%s#%s-uv-index".formatted(channelId, hourIdx);
                }

                public String hourSevereRisk() {
                    return "%s#%s-severe-risk".formatted(channelId, hourIdx);
                }

                public String hourConditions() {
                    return "%s#%s-conditions".formatted(channelId, hourIdx);
                }

                public String hourIcon() {
                    return "%s#%s-icon".formatted(channelId, hourIdx);
                }

                public String hourStations() {
                    return "%s#%s-stations".formatted(channelId, hourIdx);
                }

                public String hourSource() {
                    return "%s#%s-source".formatted(channelId, hourIdx);
                }
            }
        }
    }
}
