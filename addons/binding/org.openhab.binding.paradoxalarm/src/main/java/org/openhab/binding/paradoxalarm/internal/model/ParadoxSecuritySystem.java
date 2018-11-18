/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link ParadoxSecuritySystem} Composition class which contains all Paradox entities.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxSecuritySystem {
    private List<Partition> partitions = new ArrayList<Partition>(8);
    private List<Zone> zones = new ArrayList<Zone>(192);

    public List<Partition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Partition> partitions) {
        this.partitions = partitions;
    }

    public List<Zone> getZones() {
        return zones;
    }

    public void setZones(List<Zone> zones) {
        this.zones = zones;
    }
}
