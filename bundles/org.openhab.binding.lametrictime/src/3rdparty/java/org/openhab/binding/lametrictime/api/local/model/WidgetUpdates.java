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
package org.openhab.binding.lametrictime.api.local.model;

import java.util.List;

public class WidgetUpdates
{
    private List<Frame> frames;

    public List<Frame> getFrames()
    {
        return frames;
    }

    public void setFrames(List<Frame> frames)
    {
        this.frames = frames;
    }

    public WidgetUpdates withFrames(List<Frame> frames)
    {
        this.frames = frames;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("WidgetUpdates [frames=");
        builder.append(frames);
        builder.append("]");
        return builder.toString();
    }
}
