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
package org.openhab.binding.lcn.internal.subhandler;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.connection.ModInfo;

/**
 * Handles serial number and firmware versions received from an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleMetaFirmwareSubHandler extends AbstractLcnModuleSubHandler {
    /** The pattern for the serial number and firmware PCK message */
    public static final Pattern PATTERN = Pattern.compile(LcnBindingConstants.ADDRESS_REGEX
            + "\\.SN(?<sn>[0-9|A-F]{10})(?<manu>[0-9|A-F]{2})FW(?<firmwareVersion>[0-9|A-F]{6})HW(?<hwType>\\d+)");

    public LcnModuleMetaFirmwareSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        // nothing
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        info.setFirmwareVersion(Integer.parseInt(matcher.group("firmwareVersion"), 16));
        handler.updateSerialNumberProperty(matcher.group("sn"));
        handler.updateFirmwareVersionProperty(matcher.group("firmwareVersion"));
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Set.of(PATTERN);
    }
}
