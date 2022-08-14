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
package org.openhab.binding.lametrictime.api.model.enums;

import org.openhab.binding.lametrictime.api.model.ApiValue;

public enum SoundCategory implements ApiValue
{
    NOTIFICATIONS,
    ALARMS;

    @Override
    public String toRaw()
    {
        return name().toLowerCase();
    }

    public static SoundCategory toEnum(String raw)
    {
        if (raw == null)
        {
            return null;
        }

        try
        {
            return valueOf(raw.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            // not a valid raw string
            return null;
        }
    }
}
