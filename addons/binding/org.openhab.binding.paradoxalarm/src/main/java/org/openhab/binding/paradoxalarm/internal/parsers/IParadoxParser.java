package org.openhab.binding.paradoxalarm.internal.parsers;

import org.openhab.binding.paradoxalarm.internal.model.PartitionState;
import org.openhab.binding.paradoxalarm.internal.model.ZoneState;
import org.openhab.binding.paradoxalarm.internal.model.ZoneStateFlags;

public interface IParadoxParser {
    public PartitionState calculatePartitionState(byte[] partitionFlags);

    public ZoneState calculateZoneState(int id, ZoneStateFlags zoneStateFlags);
}
