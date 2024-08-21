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
package org.openhab.binding.ojelectronics.internal.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ojelectronics.internal.ThermostatHandler;
import org.openhab.binding.ojelectronics.internal.models.groups.GroupContentModel;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refreshes values of {@link ThermostatHandler}
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
public class RefreshGroupContentService {

    private final List<GroupContentModel> groupContentList;
    private final Logger logger = LoggerFactory.getLogger(RefreshGroupContentService.class);
    private List<Thing> things;

    /**
     * Creates a new instance of {@link RefreshGroupContentService}
     *
     * @param groupContents {@link GroupContentModel}
     * @param things Things
     */
    public RefreshGroupContentService(List<GroupContentModel> groupContents, List<Thing> things) {
        this.groupContentList = groupContents;
        this.things = things;
        if (this.things.isEmpty()) {
            logger.warn("Bridge contains no thermostats.");
        }
    }

    /**
     * Handles the changes to all things.
     */
    public void handle() {
        new RefreshThermostatsService(groupContentList.stream().flatMap(entry -> entry.thermostats.stream())
                .collect(Collectors.toCollection(ArrayList::new)), things).handle();
    }
}
