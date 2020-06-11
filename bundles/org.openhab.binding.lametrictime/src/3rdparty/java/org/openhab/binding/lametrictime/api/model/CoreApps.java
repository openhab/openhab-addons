/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.lametrictime.api.model;

public class CoreApps
{
    private static final ClockApp CLOCK = new ClockApp();
    private static final CountdownApp COUNTDOWN = new CountdownApp();
    private static final RadioApp RADIO = new RadioApp();
    private static final StopwatchApp STOPWATCH = new StopwatchApp();
    private static final WeatherApp WEATHER = new WeatherApp();

    public static ClockApp clock()
    {
        return CLOCK;
    }

    public static CountdownApp countdown()
    {
        return COUNTDOWN;
    }

    public static RadioApp radio()
    {
        return RADIO;
    }

    public static StopwatchApp stopwatch()
    {
        return STOPWATCH;
    }

    public static WeatherApp weather()
    {
        return WEATHER;
    }

    // @formatter:off
    private CoreApps() {}
    // @formatter:on
}
