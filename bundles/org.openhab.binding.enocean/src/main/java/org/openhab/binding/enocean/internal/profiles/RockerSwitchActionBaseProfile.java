/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanProfileRockerSwitchActionConfig;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.TriggerProfile;
import org.openhab.core.types.State;

/**
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public abstract class RockerSwitchActionBaseProfile implements TriggerProfile {

    protected final ProfileCallback callback;
    protected ProfileContext context;

    protected @Nullable State previousState;

    protected static final String ANY_DIR = "*";

    public RockerSwitchActionBaseProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.context = context;
    }

    protected boolean isEventValid(String event) {
        String[] directions = event.split("\\|");
        if (directions.length != 2) {
            return false;
        }

        EnOceanProfileRockerSwitchActionConfig config = context.getConfiguration()
                .as(EnOceanProfileRockerSwitchActionConfig.class);
        if (!(config.channelAFilter.equals(ANY_DIR) || config.channelAFilter.equals(directions[0]))) {
            return false;
        } else if (!(config.channelBFilter.equals(ANY_DIR) || config.channelBFilter.equals(directions[1]))) {
            return false;
        }

        return true;
    }
}
