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

import org.openhab.binding.paradoxalarm.internal.model.PartitionState;
import org.openhab.binding.paradoxalarm.internal.model.Version;
import org.openhab.binding.paradoxalarm.internal.model.ZoneState;
import org.openhab.binding.paradoxalarm.internal.model.ZoneStateFlags;

/**
 * The {@link IParadoxParser} Interface for Paradox Parsers implementations
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public interface IParadoxParser {
    PartitionState calculatePartitionState(byte[] partitionFlags);

    ZoneState calculateZoneState(int id, ZoneStateFlags zoneStateFlags);

    Version parseApplicationVersion(byte[] panelInfoBytes);

    Version parseHardwareVersion(byte[] panelInfoBytes);

    Version parseBootloaderVersion(byte[] panelInfoBytes);
}
