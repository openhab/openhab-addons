/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal;

import java.math.BigDecimal;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.DimmerOutputCommand;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A profile to control multiple dimmer outputs simultaneously with ramp.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class DimmerOutputProfile implements StateProfile {
    private final Logger logger = LoggerFactory.getLogger(DimmerOutputProfile.class);
    /** The Profile's UID */
    static final ProfileTypeUID UID = new ProfileTypeUID(LcnBindingConstants.BINDING_ID, "output");
    private final ProfileCallback callback;
    private int rampMs;
    private boolean controlAllOutputs;
    private boolean controlOutputs12;

    public DimmerOutputProfile(ProfileCallback callback, ProfileContext profileContext) {
        this.callback = callback;

        Optional<Object> ramp = getConfig(profileContext, "ramp");
        Optional<Object> allOutputs = getConfig(profileContext, "controlAllOutputs");
        Optional<Object> outputs12 = getConfig(profileContext, "controlOutputs12");

        ramp.ifPresent(b -> {
            if (b instanceof BigDecimal) {
                rampMs = (int) (((BigDecimal) b).doubleValue() * 1000);
            } else {
                logger.warn("Could not parse 'ramp', unexpected type, should be float: {}", ramp);
            }
        });

        allOutputs.ifPresent(b -> {
            if (b instanceof Boolean) {
                controlAllOutputs = true;
            } else {
                logger.warn("Could not parse 'controlAllOutputs', unexpected type, should be true/false: {}", b);
            }
        });

        outputs12.ifPresent(b -> {
            if (b instanceof Boolean) {
                controlOutputs12 = true;
            } else {
                logger.warn("Could not parse 'controlOutputs12', unexpected type, should be true/false: {}", b);
            }
        });
    }

    private Optional<Object> getConfig(ProfileContext profileContext, String key) {
        return Optional.ofNullable(profileContext.getConfiguration().get(key));
    }

    @Override
    public void onCommandFromItem(Command command) {
        if (rampMs != 0 && rampMs != LcnDefs.FIXED_RAMP_MS && controlOutputs12) {
            logger.warn("Unsupported 'ramp' setting. Will be forced to 250ms: {}", rampMs);
        }
        BigDecimal value;
        if (command instanceof DecimalType) {
            value = ((DecimalType) command).toBigDecimal();
        } else if (command instanceof OnOffType) {
            value = ((OnOffType) command) == OnOffType.ON ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        } else {
            logger.warn("Unsupported type: {}", command.toFullString());
            return;
        }
        callback.handleCommand(new DimmerOutputCommand(value, controlAllOutputs, controlOutputs12, rampMs));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        callback.sendUpdate(state);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return UID;
    }

    @Override
    public void onCommandFromHandler(Command command) {
        // nothing
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // nothing
    }
}
