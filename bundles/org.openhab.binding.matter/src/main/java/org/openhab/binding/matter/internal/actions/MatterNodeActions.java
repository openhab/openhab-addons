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

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.PairingCodes;
import org.openhab.binding.matter.internal.controller.MatterControllerClient;
import org.openhab.binding.matter.internal.handler.NodeHandler;
import org.openhab.binding.matter.internal.util.MatterVendorIDs;
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
 * The {@link MatterNodeActions}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = MatterNodeActions.class)
@ThingActionsScope(name = "matter")
public class MatterNodeActions implements ThingActions {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private @Nullable NodeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (NodeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "Generate a new pairing code for a Matter device", description = "Generates a new manual and QR pairing code to be used to pair the Matter device with an external Matter controller")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "manualPairingCode", label = "Manual pairing code", type = "java.lang.String"),
            @ActionOutput(name = "qrPairingCode", label = "QR pairing code", type = "qrCode") }) Map<String, Object> generateNewPairingCode() {
        NodeHandler handler = this.handler;
        if (handler != null) {
            MatterControllerClient client = handler.getClient();
            if (client != null) {
                try {
                    PairingCodes code = client.enhancedCommissioningWindow(handler.getNodeId()).get();
                    return Map.of("manualPairingCode", code.manualPairingCode, "qrPairingCode", code.qrPairingCode);
                } catch (InterruptedException | ExecutionException e) {
                    logger.debug("Failed to generate new pairing code for device {}", handler.getNodeId(), e);
                }
            }
        }
        return null;
    }

    @RuleAction(label = "Decommission Matter node from fabric", description = "This will remove the device from the Matter fabric.  If the device is online and reachable this will attempt to remove the credentials from the device first before removing it from the network.  Once a device is removed, this Thing will go offline and can be removed.")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = "Result from decommissioning process", type = "java.lang.String") }) String decommissionNode() {
        NodeHandler handler = this.handler;
        if (handler != null) {
            MatterControllerClient client = handler.getClient();
            if (client != null) {
                try {
                    client.removeNode(handler.getNodeId()).get();
                    return "success";
                } catch (InterruptedException | ExecutionException e) {
                    logger.debug("Failed to decommission device {}", handler.getNodeId(), e);
                    return e.getLocalizedMessage();
                }
            }
        }
        return null;
    }

    @RuleAction(label = "List connected Matter fabrics", description = "This will list all the Matter fabrics this node belongs to")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = "Connected Fabrics", type = "java.lang.String") }) String getFabrics() {
        NodeHandler handler = this.handler;
        if (handler != null) {
            MatterControllerClient client = handler.getClient();
            if (client != null) {
                try {
                    var fabrics = client.getFabrics(handler.getNodeId()).get();
                    return fabrics.stream().map(fabric -> String.format("#%d %s (%s)", fabric.fabricIndex, fabric.label,
                            MatterVendorIDs.VENDOR_IDS.get(fabric.vendorId))).collect(Collectors.joining(", "));
                } catch (InterruptedException | ExecutionException e) {
                    logger.debug("Failed to retrieve fabrics {}", handler.getNodeId(), e);
                    return e.getLocalizedMessage();
                }
            }
        }
        return null;
    }

    @RuleAction(label = "Remove connected Matter fabric", description = "This removes a connected Matter fabric from a device.  Use the 'List connected Matter fabrics' action to retrieve the fabric index number")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = "Remove Result", type = "java.lang.String") }) String removeFabric(
                    @ActionInput(name = "indexNumber", label = "The index number of the fabric", description = "The index number of the connected Matter fabric") Integer indexNumber) {
        NodeHandler handler = this.handler;
        if (handler != null) {
            MatterControllerClient client = handler.getClient();
            if (client != null) {
                try {
                    client.removeFabric(handler.getNodeId(), indexNumber).get();
                    return "success";
                } catch (InterruptedException | ExecutionException e) {
                    logger.debug("Failed to remove fabric {} {} ", handler.getNodeId(), indexNumber, e);
                    return e.getLocalizedMessage();
                }
            }
        }
        return null;
    }
}
