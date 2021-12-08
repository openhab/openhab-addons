/*
 * Copyright 2022 Mark Hilbush
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
package org.openhab.binding.sleepiq.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Side} represents the possible sides of the bed (i.e. left and right).
 */
@NonNullByDefault
public enum Side {
    LEFT(0),
    RIGHT(1);

    private final int side;

    Side(final int side) {
        this.side = side;
    }

    public int value() {
        return side;
    }

    public static Side forValue(int value) {
        for (Side s : Side.values()) {
            if (s.side == value) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid side: " + value);
    }

    public static Side convertFromGroup(@Nullable String channelGroup) {
        return "left".equalsIgnoreCase(channelGroup) ? Side.LEFT : Side.RIGHT;
    }

    @Override
    public String toString() {
        return side == 0 ? LEFT.name() : RIGHT.name();
    }
}
