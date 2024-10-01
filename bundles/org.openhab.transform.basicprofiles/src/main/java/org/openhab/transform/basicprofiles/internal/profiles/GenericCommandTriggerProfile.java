/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.GENERIC_COMMAND_UID;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GenericCommandTriggerProfile} class implements the behavior when being linked to an item.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GenericCommandTriggerProfile extends AbstractTriggerProfile {

    private final Logger logger = LoggerFactory.getLogger(GenericCommandTriggerProfile.class);

    private static final List<Class<? extends Command>> SUPPORTED_COMMANDS = List.of(IncreaseDecreaseType.class,
            NextPreviousType.class, OnOffType.class, PlayPauseType.class, RewindFastforwardType.class,
            StopMoveType.class, UpDownType.class);

    public static final String PARAM_COMMAND = "command";

    private @Nullable Command command;

    public GenericCommandTriggerProfile(ProfileCallback callback, ProfileContext context) {
        super(callback, context);

        Object paramValue = context.getConfiguration().get(PARAM_COMMAND);
        logger.trace("Configuring profile '{}' with '{}' parameter: '{}'", getProfileTypeUID(), PARAM_COMMAND,
                paramValue);
        if (paramValue instanceof String value) {
            command = TypeParser.parseCommand(SUPPORTED_COMMANDS, value);
            if (command == null) {
                logger.debug("Value '{}' for parameter '{}' is a not supported command. Using StringType instead.",
                        value, PARAM_COMMAND);
                command = StringType.valueOf(value);
            }
        } else {
            logger.error("Parameter '{}' is not of type String: {}", PARAM_COMMAND, paramValue);
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return GENERIC_COMMAND_UID;
    }

    @Override
    public void onTriggerFromHandler(String payload) {
        Command c = command;
        if (c != null && events.contains(payload)) {
            callback.sendCommand(c);
        }
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // do nothing
    }
}
