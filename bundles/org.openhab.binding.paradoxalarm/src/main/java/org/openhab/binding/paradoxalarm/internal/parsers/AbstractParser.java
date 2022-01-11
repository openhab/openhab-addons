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
package org.openhab.binding.paradoxalarm.internal.parsers;

import org.openhab.binding.paradoxalarm.internal.model.Version;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractParser} Contains parsing methods irelevant from panel type
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public abstract class AbstractParser implements IParadoxParser {

    private final Logger logger = LoggerFactory.getLogger(AbstractParser.class);

    @Override
    public Version parseApplicationVersion(byte[] panelInfoBytes) {
        return new Version(translateAsNibbles(panelInfoBytes[9]), translateAsNibbles(panelInfoBytes[10]),
                translateAsNibbles(panelInfoBytes[11]));
    }

    @Override
    public Version parseHardwareVersion(byte[] panelInfoBytes) {
        return new Version(translateAsNibbles(panelInfoBytes[16]), translateAsNibbles(panelInfoBytes[17]));
    }

    @Override
    public Version parseBootloaderVersion(byte[] panelInfoBytes) {
        return new Version(translateAsNibbles(panelInfoBytes[18]), translateAsNibbles(panelInfoBytes[19]),
                translateAsNibbles(panelInfoBytes[20]));
    }

    private byte translateAsNibbles(byte byteValue) {
        return (byte) ((ParadoxUtil.getHighNibble(byteValue) * 10 + ParadoxUtil.getLowNibble(byteValue)) & 0xFF);
    }
}
