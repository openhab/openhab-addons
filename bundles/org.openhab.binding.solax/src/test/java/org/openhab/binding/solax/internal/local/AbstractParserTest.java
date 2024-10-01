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
package org.openhab.binding.solax.internal.local;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solax.internal.connectivity.rawdata.local.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.InverterType;
import org.openhab.binding.solax.internal.model.local.LocalData;
import org.openhab.binding.solax.internal.model.local.RawDataParser;

/**
 * The {@link AbstractParserTest} Abstract class defining the common logic for testing local connections to the various
 * inverters and their parsers
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractParserTest {
    @Test
    public void testParser() {
        LocalConnectRawDataBean bean = LocalConnectRawDataBean.fromJson(getRawData());
        int type = bean.getType();
        InverterType inverterType = InverterType.fromIndex(type);
        assertEquals(getInverterType(), inverterType, "Inverter type not recognized properly");

        RawDataParser parser = inverterType.getParser();
        assertNotNull(parser);

        Set<String> supportedChannels = parser.getSupportedChannels();
        assertFalse(supportedChannels.isEmpty());

        LocalData data = parser.getData(bean);
        assertParserSpecific(data);
    }

    protected abstract InverterType getInverterType();

    protected abstract String getRawData();

    protected abstract void assertParserSpecific(LocalData data);
}
