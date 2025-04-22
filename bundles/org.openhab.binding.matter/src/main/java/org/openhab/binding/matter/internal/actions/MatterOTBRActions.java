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
package org.openhab.binding.matter.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.controller.devices.converter.ThreadBorderRouterManagementConverter;
import org.openhab.binding.matter.internal.handler.NodeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.ActionOutputs;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MatterOTBRActions}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = MatterOTBRActions.class)
@ThingActionsScope(name = "matter-otbr")
public class MatterOTBRActions implements ThingActions {
    public final Logger logger = LoggerFactory.getLogger(getClass());

    protected @Nullable NodeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (NodeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "Thread: Update active operational data set", description = "Updates the active operational dataset for the node")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = "Set Result", type = "java.lang.String") }) String updateThreadActiveOperationalDataSet(
                    @ActionInput(name = "dataset", label = "Thread operational dataset", description = "The thread operational dataset to set") String dataset) {
        NodeHandler handler = this.handler;
        if (handler != null) {
            ThreadBorderRouterManagementConverter converter = handler
                    .findConverterByType(ThreadBorderRouterManagementConverter.class);
            if (converter != null) {
                try {
                    converter.setActiveDataset(dataset).get();
                    return "success";
                } catch (Exception e) {
                    logger.debug("Error setting active dataset", e);
                    return "error: " + e.getMessage();
                }
            } else {
                return "error: No converter found";
            }
        } else {
            return "error: No handler found";
        }
    }

    @RuleAction(label = "Thread: Get active operational data set", description = "Retrieves the active operational dataset from the node")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "dataset", label = "Thread Dataset", type = "java.lang.String") }) String getThreadActiveOperationalDataSet() {
        NodeHandler handler = this.handler;
        if (handler != null) {
            ThreadBorderRouterManagementConverter converter = handler
                    .findConverterByType(ThreadBorderRouterManagementConverter.class);
            if (converter != null) {
                try {
                    return converter.getActiveDataset().get();
                } catch (Exception e) {
                    logger.debug("Error getting active dataset", e);
                    return "error: " + e.getMessage();
                }
            } else {
                return "error: No converter found";
            }
        } else {
            return "error: No handler found";
        }
    }

    @RuleAction(label = "Thread: Update pending operational data set", description = "Updates the pending operational dataset for the node")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = "Set Result", type = "java.lang.String") }) String updateThreadPendingOperationalDataSet(
                    @ActionInput(name = "dataset", label = "Thread operational dataset", description = "The thread operational dataset to set") String dataset) {
        NodeHandler handler = this.handler;
        if (handler != null) {
            ThreadBorderRouterManagementConverter converter = handler
                    .findConverterByType(ThreadBorderRouterManagementConverter.class);
            if (converter != null) {
                try {
                    converter.setPendingDataset(dataset).get();
                    return "success";
                } catch (Exception e) {
                    logger.debug("Error setting pending dataset", e);
                    return "error: " + e.getMessage();
                }
            } else {
                return "error: No converter found";
            }
        } else {
            return "error: No handler found";
        }
    }

    @RuleAction(label = "Thread: Get pending operational data set", description = "Retrieves the pending operational dataset from the node")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "dataset", label = "Thread Dataset", type = "java.lang.String") }) String getThreadPendingOperationalDataSet() {
        NodeHandler handler = this.handler;
        if (handler != null) {
            ThreadBorderRouterManagementConverter converter = handler
                    .findConverterByType(ThreadBorderRouterManagementConverter.class);
            if (converter != null) {
                try {
                    return converter.getPendingDataset().get();
                } catch (Exception e) {
                    logger.debug("Error getting pending dataset", e);
                    return "error: " + e.getMessage();
                }
            } else {
                return "error: No converter found";
            }
        } else {
            return "error: No handler found";
        }
    }
}
