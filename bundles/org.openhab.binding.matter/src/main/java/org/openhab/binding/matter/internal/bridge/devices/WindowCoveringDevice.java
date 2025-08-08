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
import java.util.LinkedHashMap;
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
public class WindowCoveringDevice extends GenericDevice {
    private ScheduledExecutorService operationalStateScheduler = Executors.newSingleThreadScheduledExecutor();
    private @Nullable ScheduledFuture<?> operationalStateTimer = null;
    private @Nullable Integer lastTargetPercent = null;

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
        attributeMap.put(
                WindowCoveringCluster.CLUSTER_PREFIX + "."
                        + WindowCoveringCluster.ATTRIBUTE_CURRENT_POSITION_LIFT_PERCENT100THS,
                itemStateToPercent(primaryItem.getState()) * 100);
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
                lastTargetPercent = percentType.intValue();
                int currentPercent = itemStateToPercent(primaryItem.getState());
                if (currentPercent >= 0) {
                    updateOperationalStatus(currentPercent);
                }
                // do logic to sen op state
                boolean open = percentType.intValue() == 0;
                Metadata primaryItemMetadata = this.primaryItemMetadata;
                String key = open ? "OPEN" : "CLOSED";
                if (primaryItem instanceof GroupItem groupItem) {
                    groupItem.send(percentType);
                } else if (primaryItem instanceof DimmerItem dimmerItem) {
                    dimmerItem.send(percentType);
                } else if (primaryItem instanceof RollershutterItem rollerShutterItem) {
                    if (percentType.intValue() == 100) {
                        rollerShutterItem.send(UpDownType.DOWN);
                    } else if (percentType.intValue() == 0) {
                        rollerShutterItem.send(UpDownType.UP);
                    } else {
                        rollerShutterItem.send(percentType);
                    }
                } else if (primaryItem instanceof SwitchItem switchItem) {
                    boolean invert = false;
                    if (primaryItemMetadata != null) {
                        Object invertObject = primaryItemMetadata.getConfiguration().getOrDefault("invert", false);
                        if (invertObject instanceof Boolean invertValue) {
                            invert = invertValue;
                        }
                    }
                    switchItem.send(OnOffType.from(invert ? open ? "ON" : "OFF" : open ? "OFF" : "ON"));
                } else if (primaryItem instanceof StringItem stringItem) {
                    Object value = key;
                    if (primaryItemMetadata != null) {
                        value = primaryItemMetadata.getConfiguration().getOrDefault(key, key);
                    }
                    stringItem.send(new StringType(value.toString()));
                }
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
                            lastTargetPercent = null;
                            // will send stop back
                            updateOperationalStatus(0);
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
        int localPercent = itemStateToPercent(state);
        if (localPercent >= 0) {
            try {
                setEndpointState(WindowCoveringCluster.CLUSTER_PREFIX,
                        WindowCoveringCluster.ATTRIBUTE_CURRENT_POSITION_LIFT_PERCENT100THS, localPercent * 100).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.debug("Could not set state", e);
                return;
            }
            cancelTimer();
            final Integer lp = localPercent;
            this.operationalStateTimer = operationalStateScheduler.schedule(() -> updateOperationalStatus(lp), 1000,
                    TimeUnit.MILLISECONDS);
        }
    }

    private int itemStateToPercent(State state) {
        int localPercent = 0;
        if (state instanceof PercentType percentType) {
            localPercent = percentType.intValue();
        } else if (state instanceof OpenClosedType openClosedType) {
            localPercent = openClosedType == OpenClosedType.OPEN ? 0 : 100;
        } else if (state instanceof OnOffType onOffType) {
            Metadata primaryItemMetadata = this.primaryItemMetadata;
            boolean invert = false;
            if (primaryItemMetadata != null) {
                logger.debug("primaryItemMetadata: {}", primaryItemMetadata);
                Object invertObject = primaryItemMetadata.getConfiguration().getOrDefault("invert", false);
                if (invertObject instanceof Boolean invertValue) {
                    logger.debug("invertObject: {}", invertObject);
                    invert = invertValue;
                }
            }
            localPercent = invert ? onOffType == OnOffType.ON ? 0 : 100 : onOffType == OnOffType.ON ? 100 : 0;
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

    private void updateOperationalStatus(Integer localPercent) {
        Integer lastTargetPercent = this.lastTargetPercent;
        WindowCoveringCluster.MovementStatus status = WindowCoveringCluster.MovementStatus.STOPPED;
        if (lastTargetPercent != null) {
            if (lastTargetPercent < localPercent) {
                status = WindowCoveringCluster.MovementStatus.CLOSING;
            } else if (lastTargetPercent > localPercent) {
                status = WindowCoveringCluster.MovementStatus.OPENING;
            } else {
                this.lastTargetPercent = null;
            }
        }
        AbstractMap<String, Object> t = new LinkedHashMap<String, Object>();
        t.put("global", status.getValue());
        t.put("lift", status.getValue());
        setEndpointState(WindowCoveringCluster.CLUSTER_PREFIX, WindowCoveringCluster.ATTRIBUTE_OPERATIONAL_STATUS, t);
    }

    private void cancelTimer() {
        ScheduledFuture<?> operationalStateTimer = this.operationalStateTimer;
        if (operationalStateTimer != null) {
            operationalStateTimer.cancel(true);
        }
        this.operationalStateTimer = null;
    }
}
