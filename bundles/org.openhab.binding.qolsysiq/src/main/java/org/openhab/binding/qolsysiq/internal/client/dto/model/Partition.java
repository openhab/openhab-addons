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
package org.openhab.binding.qolsysiq.internal.client.dto.model;

import java.util.List;

/**
 * A logical alarm partition that can be armed, report state and contain zones
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Partition {
    public Integer partitionId;
    public String name;
    public PartitionStatus status;
    public Boolean secureArm;
    public List<Zone> zoneList;
}
