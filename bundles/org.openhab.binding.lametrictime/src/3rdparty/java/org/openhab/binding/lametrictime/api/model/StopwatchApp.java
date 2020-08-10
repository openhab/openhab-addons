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

import org.openhab.binding.lametrictime.api.local.model.UpdateAction;

public class StopwatchApp extends CoreApplication
{
    private static final String NAME = "com.lametric.stopwatch";

    private static final String ACTION_PAUSE = "stopwatch.pause";
    private static final String ACTION_RESET = "stopwatch.reset";
    private static final String ACTION_START = "stopwatch.start";

    public StopwatchApp()
    {
        super(NAME);
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
