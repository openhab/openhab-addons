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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.LcnAddr;
import org.openhab.binding.lcn.internal.common.LcnAddrGrp;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.core.thing.Thing;

/**
 * The {@link LcnGroupHandler} is responsible for handling commands, which are
 * addressed to an LCN group.
 *
 * The module in the field moduleAddress is used for state updates of the group as representative for all modules in
 * the group.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnGroupHandler extends LcnModuleHandler {
    private @Nullable LcnAddrGrp groupAddress;

    public LcnGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        LcnGroupConfiguration localConfig = getConfigAs(LcnGroupConfiguration.class);
        groupAddress = new LcnAddrGrp(localConfig.segmentId, localConfig.groupId);

        super.initialize();
    }

    @Override
    protected void requestFirmwareVersionAndSerialNumberIfNotSet() throws LcnException {
        // nothing, don't request the serial number of an LCN group representation module
    }

    @Override
    protected LcnAddr getCommandAddress() throws LcnException {
        LcnAddrGrp localAddress = groupAddress;
        if (localAddress == null) {
            throw new LcnException("LCN group address not set");
        }
        return localAddress;
    }
}
