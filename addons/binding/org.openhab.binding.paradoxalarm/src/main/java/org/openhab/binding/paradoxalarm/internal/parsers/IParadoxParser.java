/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.parsers;

import org.openhab.binding.paradoxalarm.internal.model.PartitionState;
import org.openhab.binding.paradoxalarm.internal.model.Version;
import org.openhab.binding.paradoxalarm.internal.model.ZoneState;
import org.openhab.binding.paradoxalarm.internal.model.ZoneStateFlags;

/**
 * The {@link IParadoxParser} Interface for Paradox Parsers implementations
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public interface IParadoxParser {
    public PartitionState calculatePartitionState(byte[] partitionFlags);

    public ZoneState calculateZoneState(int id, ZoneStateFlags zoneStateFlags);

    public Version parseApplicationVersion(byte[] panelInfoBytes);

    public Version parseHardwareVersion(byte[] panelInfoBytes);

    public Version parseBootloaderVersion(byte[] panelInfoBytes);
}
