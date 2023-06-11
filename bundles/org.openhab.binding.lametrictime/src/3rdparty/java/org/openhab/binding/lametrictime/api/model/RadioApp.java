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

public class RadioApp extends CoreApplication
{
    private static final String NAME = "com.lametric.radio";

    private static final String ACTION_NEXT = "radio.next";
    private static final String ACTION_PLAY = "radio.play";
    private static final String ACTION_PREV = "radio.prev";
    private static final String ACTION_STOP = "radio.stop";

    public RadioApp()
    {
        super(NAME);
    }

    public CoreAction next()
    {
        return new CoreAction(this, new UpdateAction().withId(ACTION_NEXT));
    }

    public CoreAction play()
    {
        return new CoreAction(this, new UpdateAction().withId(ACTION_PLAY));
    }

    public CoreAction previous()
    {
        return new CoreAction(this, new UpdateAction().withId(ACTION_PREV));
    }

    public CoreAction stop()
    {
        return new CoreAction(this, new UpdateAction().withId(ACTION_STOP));
    }
}
