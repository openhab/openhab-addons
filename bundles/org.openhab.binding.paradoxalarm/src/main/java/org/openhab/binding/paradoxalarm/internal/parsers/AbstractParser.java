/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
        return new Version(panelInfoBytes[9], panelInfoBytes[10], panelInfoBytes[11]);
    }

    @Override
    public Version parseHardwareVersion(byte[] panelInfoBytes) {
        return new Version(panelInfoBytes[16], panelInfoBytes[17]);
    }

    @Override
    public Version parseBootloaderVersion(byte[] panelInfoBytes) {
        return new Version(panelInfoBytes[18], panelInfoBytes[19], panelInfoBytes[20]);
    }
}
