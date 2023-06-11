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

public class NotificationModel
{
    private Integer cycles;
    private List<Frame> frames;
    private Sound sound;

    public Integer getCycles()
    {
        return cycles;
    }

    public void setCycles(Integer cycles)
    {
        this.cycles = cycles;
    }

    public NotificationModel withCycles(Integer cycles)
    {
        this.cycles = cycles;
        return this;
    }

    public List<Frame> getFrames()
    {
        return frames;
    }

    public void setFrames(List<Frame> frames)
    {
        this.frames = frames;
    }

    public NotificationModel withFrames(List<Frame> frames)
    {
        this.frames = frames;
        return this;
    }

    public Sound getSound()
    {
        return sound;
    }

    public void setSound(Sound sound)
    {
        this.sound = sound;
    }

    public NotificationModel withSound(Sound sound)
    {
        this.sound = sound;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("NotificationModel [cycles=");
        builder.append(cycles);
        builder.append(", frames=");
        builder.append(frames);
        builder.append(", sound=");
        builder.append(sound);
        builder.append("]");
        return builder.toString();
    }
}
