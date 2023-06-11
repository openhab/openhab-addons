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
package org.openhab.binding.homematic.internal.converter;

import org.junit.jupiter.api.BeforeEach;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * @author Michael Reitler - Initial contribution
 */
public class BaseConverterTest {

    protected final HmDatapoint floatDp = new HmDatapoint("floatDp", "", HmValueType.FLOAT, null, false,
            HmParamsetType.VALUES);
    protected final HmDatapoint integerDp = new HmDatapoint("integerDp", "", HmValueType.INTEGER, null, false,
            HmParamsetType.VALUES);
    protected final HmDatapoint floatQuantityDp = new HmDatapoint("floatQuantityDp", "", HmValueType.FLOAT, null, false,
            HmParamsetType.VALUES);
    protected final HmDatapoint integerQuantityDp = new HmDatapoint("floatIntegerDp", "", HmValueType.INTEGER, null,
            false, HmParamsetType.VALUES);

    @BeforeEach
    public void setup() {
        HmChannel stubChannel = new HmChannel("stubChannel", 0);
        stubChannel.setDevice(new HmDevice("LEQ123456", HmInterface.RF, "HM-STUB-DEVICE", "", "", ""));
        floatDp.setChannel(stubChannel);
    }
}
