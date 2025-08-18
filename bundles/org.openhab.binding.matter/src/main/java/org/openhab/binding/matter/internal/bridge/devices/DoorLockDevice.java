/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.bridge.devices;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * The {@link DoorLockDevice} is a device that represents a Door Lock.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class DoorLockDevice extends BaseDevice {

    public DoorLockDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem item) {
        super(metadataRegistry, client, item);
    }

    @Override
    public String deviceType() {
        return "DoorLock";
    }

    @Override
    public void handleMatterEvent(String clusterName, String attributeName, Object data) {
        switch (attributeName) {
            case DoorLockCluster.ATTRIBUTE_LOCK_STATE: {
                int lockInt = ((Double) data).intValue();
                boolean locked = DoorLockCluster.LockStateEnum.LOCKED.getValue() == lockInt;
                if (primaryItem instanceof GroupItem groupItem) {
                    groupItem.send(OnOffType.from(locked));
                } else {
                    ((SwitchItem) primaryItem).send(OnOffType.from(locked));
                }
            }
            default:
                break;
        }
    }

    @Override
    protected MatterDeviceOptions activate() {
        primaryItem.addStateChangeListener(this);
        MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
        Map<String, Object> attributeMap = primaryMetadata.getAttributeOptions();
        attributeMap.put(DoorLockCluster.CLUSTER_PREFIX + "." + DoorLockCluster.ATTRIBUTE_LOCK_STATE,
                Optional.ofNullable(primaryItem.getStateAs(OnOffType.class))
                        .orElseGet(() -> OnOffType.OFF) == OnOffType.ON ? DoorLockCluster.LockStateEnum.LOCKED.value
                                : DoorLockCluster.LockStateEnum.UNLOCKED.value);
        return new MatterDeviceOptions(attributeMap, primaryMetadata.label);
    }

    @Override
    public void dispose() {
        primaryItem.removeStateChangeListener(this);
    }

    @Override
    public void updateState(Item item, State state) {
        if (state instanceof OnOffType onOffType) {
            setEndpointState(DoorLockCluster.CLUSTER_PREFIX, DoorLockCluster.ATTRIBUTE_LOCK_STATE,
                    onOffType == OnOffType.ON ? DoorLockCluster.LockStateEnum.LOCKED.value
                            : DoorLockCluster.LockStateEnum.UNLOCKED.value);
        }
    }
}
