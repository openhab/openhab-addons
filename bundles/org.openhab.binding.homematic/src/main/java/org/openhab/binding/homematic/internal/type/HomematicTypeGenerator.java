/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public interface HomematicTypeGenerator {

    /**
     * Initializes the type generator.
     */
    void initialize();

    /**
     * Generates the ThingType and ChannelTypes for the given device.
     */
    void generate(HmDevice device);

    /**
     * Generates the ChannelType for the given datapoint.
     */
    ChannelType createChannelType(HmDatapoint dp, ChannelTypeUID channelTypeUID);

    /**
     * Validates all devices for multiple firmware versions. Different firmware versions for the same device may have
     * different datapoints which may cause warnings in the logfile.
     */
    void validateFirmwares();

    /**
     * Returns true, if the given datapoint can be ignored for metadata generation.
     */
    boolean isIgnoredDatapoint(HmDatapoint dp);
}
