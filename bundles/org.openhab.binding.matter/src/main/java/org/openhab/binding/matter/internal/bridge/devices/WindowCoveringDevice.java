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

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.WindowCoveringCluster;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.State;

/**
 * The {@link WindowCoveringDevice} is a device that represents a Window Covering.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class WindowCoveringDevice extends BaseDevice {
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private @Nullable ScheduledFuture<?> movementFinishedTimer = null;
    private @Nullable Integer lastTargetPercent;
    private int lastCurrentPercent;
    private boolean invert = false;

    public WindowCoveringDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem item) {
        super(metadataRegistry, client, item);
    }

    @Override
    public String deviceType() {
        return "WindowCovering";
    }

    @Override
    protected MatterDeviceOptions activate() {
        primaryItem.addStateChangeListener(this);
        MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
        Map<String, Object> attributeMap = primaryMetadata.getAttributeOptions();
        Metadata primaryItemMetadata = this.primaryItemMetadata;
        if (primaryItemMetadata != null) {
            Object invertObject = primaryItemMetadata.getConfiguration().get("invert");
            if (invertObject instanceof Boolean invertValue) {
                invert = invertValue;
            }
        }
        lastCurrentPercent = itemStateToPercent(primaryItem.getState());
        int percent100ths = lastCurrentPercent * 100;
        attributeMap.put(WindowCoveringCluster.CLUSTER_PREFIX + "."
                + WindowCoveringCluster.ATTRIBUTE_CURRENT_POSITION_LIFT_PERCENT100THS, percent100ths);
        attributeMap.put(WindowCoveringCluster.CLUSTER_PREFIX + "."
                + WindowCoveringCluster.ATTRIBUTE_TARGET_POSITION_LIFT_PERCENT100THS, percent100ths);
        return new MatterDeviceOptions(attributeMap, primaryMetadata.label);
    }

    @Override
    public void dispose() {
        primaryItem.removeStateChangeListener(this);
        cancelTimer();
    }

    @Override
    public void handleMatterEvent(String clusterName, String attributeName, Object data) {
        switch (attributeName) {
            case WindowCoveringCluster.ATTRIBUTE_TARGET_POSITION_LIFT_PERCENT100THS:
                PercentType percentType = new PercentType((int) ((Double) data / 100));
                int targetPercent = percentType.intValue();
                Metadata primaryItemMetadata = this.primaryItemMetadata;
                if (primaryItem instanceof GroupItem groupItem) {
                    groupItem.send(percentType);
                } else if (primaryItem instanceof DimmerItem dimmerItem) {
                    dimmerItem.send(percentType);
                } else if (primaryItem instanceof RollershutterItem rollerShutterItem) {
                    if (targetPercent == 100) {
                        rollerShutterItem.send(UpDownType.DOWN);
                    } else if (targetPercent == 0) {
                        rollerShutterItem.send(UpDownType.UP);
                    } else {
                        rollerShutterItem.send(percentType);
                    }
                } else if (primaryItem instanceof SwitchItem switchItem) {
                    // anything > 0 is considered partially open and ON , otherwise completely open and OFF (unless
                    // invert is true)
                    boolean isOn = targetPercent > 0;
                    if (invert) {
                        isOn = !isOn;
                    }
                    switchItem.send(isOn ? OnOffType.ON : OnOffType.OFF);
                } else if (primaryItem instanceof StringItem stringItem) {
                    Object value = targetPercent == 0 ? "OPEN" : "CLOSED";
                    if (primaryItemMetadata != null) {
                        value = primaryItemMetadata.getConfiguration().getOrDefault(value, value);
                    }
                    stringItem.send(new StringType(value.toString()));
                }
                lastTargetPercent = targetPercent;
                break;
            case WindowCoveringCluster.ATTRIBUTE_OPERATIONAL_STATUS:
                if (data instanceof AbstractMap treeMap) {
                    @SuppressWarnings("unchecked")
                    AbstractMap<String, Object> map = (AbstractMap<String, Object>) treeMap;
                    if (map.get("global") instanceof Integer value) {
                        if (WindowCoveringCluster.MovementStatus.STOPPED.getValue().equals(value)
                                && primaryItem instanceof RollershutterItem rollerShutterItem) {
                            rollerShutterItem.send(StopMoveType.STOP);
                            cancelTimer();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void updateState(Item item, State state) {
        int currentPercent = itemStateToPercent(state);
        try {
            // check if this is matter initiated or openHAB initiated, if openHAB we will fake the target position so
            // operation direction is correct
            if (lastTargetPercent == null && currentPercent != lastCurrentPercent) {
                // either 0/OPEN or 100/CLOSED, if the value was not updated, we ignore it (probably should not happen)
                int targetPercent = currentPercent < lastCurrentPercent ? 0 : 100;
                setEndpointState(WindowCoveringCluster.CLUSTER_PREFIX,
                        WindowCoveringCluster.ATTRIBUTE_TARGET_POSITION_LIFT_PERCENT100THS, targetPercent * 100).get();
                lastTargetPercent = targetPercent;
            }
            setEndpointState(WindowCoveringCluster.CLUSTER_PREFIX,
                    WindowCoveringCluster.ATTRIBUTE_CURRENT_POSITION_LIFT_PERCENT100THS, currentPercent * 100).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("Could not set state", e);
            return;
        }
        lastCurrentPercent = currentPercent;
        // Once we have stopped moving we reset the matter target state, currently there is not a
        // uniform way to detect this, so we wait 5 seconds of no updates. Setting this too early/late will not have
        // negative effects, but it looks better in UIs if we wait a bit.
        cancelTimer();
        this.movementFinishedTimer = scheduler.schedule(() -> finishMovement(currentPercent), 5000,
                TimeUnit.MILLISECONDS);
    }

    private int itemStateToPercent(State state) {
        int localPercent = 0;
        if (state instanceof PercentType percentType) {
            localPercent = percentType.intValue();
        } else if (state instanceof OpenClosedType openClosedType) {
            localPercent = openClosedType == OpenClosedType.OPEN ? 0 : 100;
        } else if (state instanceof OnOffType onOffType) {
            boolean isOn = onOffType == OnOffType.ON;
            if (invert) {
                isOn = !isOn;
            }
            localPercent = isOn ? 100 : 0;
        } else if (state instanceof StringType stringType) {
            Metadata primaryItemMetadata = this.primaryItemMetadata;
            if (primaryItemMetadata != null) {
                Object openValue = primaryItemMetadata.getConfiguration().get("OPEN");
                Object closeValue = primaryItemMetadata.getConfiguration().get("CLOSED");
                if (openValue instanceof String && closeValue instanceof String) {
                    if (stringType.equals(openValue)) {
                        localPercent = 0;
                    } else if (stringType.equals(closeValue)) {
                        localPercent = 100;
                    }
                }
            }
        }
        return localPercent;
    }

    private void finishMovement(final Integer currentPercent) {
        try {
            setEndpointState(WindowCoveringCluster.CLUSTER_PREFIX,
                    WindowCoveringCluster.ATTRIBUTE_TARGET_POSITION_LIFT_PERCENT100THS, currentPercent * 100).get();
            lastTargetPercent = null;
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("Could not set target state", e);
        }
    }

    private void cancelTimer() {
        ScheduledFuture<?> targetStateTimer = this.movementFinishedTimer;
        if (targetStateTimer != null) {
            targetStateTimer.cancel(true);
        }
        this.movementFinishedTimer = null;
    }
}
