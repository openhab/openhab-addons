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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.TriggerProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractTriggerProfile} class implements the behavior when being linked to an item.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractTriggerProfile implements TriggerProfile {

    private final Logger logger = LoggerFactory.getLogger(AbstractTriggerProfile.class);

    public static final String PARAM_EVENTS = "events";

    final ProfileCallback callback;
    final List<String> events;

    @SuppressWarnings("unchecked")
    AbstractTriggerProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;

        Object paramValue = context.getConfiguration().get(PARAM_EVENTS);
        logger.trace("Configuring profile '{}' with '{}' parameter: '{}'", getProfileTypeUID(), PARAM_EVENTS,
                paramValue);
        if (paramValue instanceof String) {
            String event = paramValue.toString();
            events = Collections.unmodifiableList(Arrays.asList(event.split(",")));
        } else if (paramValue instanceof Iterable) {
            List<String> values = new ArrayList<>();
            for (String event : (Iterable<String>) paramValue) {
                values.add(event);
            }
            events = Collections.unmodifiableList(values);
        } else {
            logger.error("Parameter '{}' is not a comma separated list of Strings", PARAM_EVENTS);
            events = List.of();
        }
    }
}
