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

import java.util.SortedMap;
import java.util.TreeMap;

import org.openhab.binding.lametrictime.api.local.model.BooleanParameter;
import org.openhab.binding.lametrictime.api.local.model.IntegerParameter;
import org.openhab.binding.lametrictime.api.local.model.Parameter;
import org.openhab.binding.lametrictime.api.local.model.UpdateAction;

public class CountdownApp extends CoreApplication
{
    private static final String NAME = "com.lametric.countdown";

    private static final String ACTION_CONFIGURE = "countdown.configure";
    private static final String ACTION_PAUSE = "countdown.pause";
    private static final String ACTION_RESET = "countdown.reset";
    private static final String ACTION_START = "countdown.start";

    private static final String PARAMETER_DURATION = "duration";
    private static final String PARAMETER_START_NOW = "start_now";

    public CountdownApp()
    {
        super(NAME);
    }

    public CoreAction configure(int duration, boolean startNow)
    {
        SortedMap<String, Parameter> parameters = new TreeMap<>();
        parameters.put(PARAMETER_DURATION, new IntegerParameter().withValue(duration));
        parameters.put(PARAMETER_START_NOW, new BooleanParameter().withValue(startNow));

        return new CoreAction(this,
                              new UpdateAction().withId(ACTION_CONFIGURE)
                                                .withParameters(parameters));
    }

    public CoreAction pause()
    {
        return new CoreAction(this, new UpdateAction().withId(ACTION_PAUSE));
    }

    public CoreAction reset()
    {
        return new CoreAction(this, new UpdateAction().withId(ACTION_RESET));
    }

    public CoreAction start()
    {
        return new CoreAction(this, new UpdateAction().withId(ACTION_START));
    }
}
