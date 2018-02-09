/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.discovery.project;

import java.util.Collections;
import java.util.List;

/**
 * This class represents a location defined in the Lutron system. Areas are organized
 * hierarchically and can represent an entire house, a room in the house, or a specific
 * location within a room.
 *
 * @author Allan Tong - Initial contribution
 */
public class Area {
    private String name;
    private List<DeviceNode> deviceNodes;
    private List<Output> outputs;
    private List<Area> areas;

    public String getName() {
        return name;
    }

    public List<DeviceNode> getDeviceNodes() {
        return deviceNodes != null ? deviceNodes : Collections.<DeviceNode> emptyList();
    }

    public List<Output> getOutputs() {
        return outputs != null ? outputs : Collections.<Output> emptyList();
    }

    public List<Area> getAreas() {
        return areas != null ? areas : Collections.<Area> emptyList();
    }
}
