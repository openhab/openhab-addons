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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.MatterBindingConstants;
import org.openhab.binding.matter.internal.client.dto.PairingCodes;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OperationalCredentialsCluster;
import org.openhab.binding.matter.internal.controller.MatterControllerClient;
import org.openhab.binding.matter.internal.handler.NodeHandler;
import org.openhab.binding.matter.internal.util.MatterVendorIDs;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.ActionOutputs;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link MatterNodeActions} exposes Matter related actions for the Matter Node Thing.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = MatterNodeActions.class)
@ThingActionsScope(name = "matter")
public class MatterNodeActions implements ThingActions {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    protected @Nullable NodeHandler handler;
    private final TranslationService translationService;

    @Activate
    public MatterNodeActions(@Reference TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof NodeHandler nodeHandler) {
            this.handler = nodeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = MatterBindingConstants.THING_ACTION_LABEL_NODE_GENERATE_NEW_PAIRING_CODE, description = MatterBindingConstants.THING_ACTION_DESC_NODE_GENERATE_NEW_PAIRING_CODE)
    public @ActionOutputs({
            @ActionOutput(name = "manualPairingCode", label = MatterBindingConstants.THING_ACTION_LABEL_NODE_GENERATE_NEW_PAIRING_CODE_MANUAL_PAIRING_CODE, type = "java.lang.String"),
            @ActionOutput(name = "qrPairingCode", label = MatterBindingConstants.THING_ACTION_LABEL_NODE_GENERATE_NEW_PAIRING_CODE_QR_PAIRING_CODE, type = "qrCode") }) Map<String, Object> generateNewPairingCode() {
        NodeHandler handler = this.handler;
        if (handler != null) {
            MatterControllerClient client = handler.getClient();
            if (client != null) {
                try {
                    PairingCodes code = client.enhancedCommissioningWindow(handler.getNodeId()).get();
                    return Map.of("manualPairingCode", code.manualPairingCode, "qrPairingCode", code.qrPairingCode);
                } catch (InterruptedException | ExecutionException | JsonParseException e) {
                    logger.debug("Failed to generate new pairing code for device {}", handler.getNodeId(), e);
                }
            }
        }
        return Map.of("manualPairingCode",
                translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER),
                "qrPairingCode",
                translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER));
    }

    @RuleAction(label = MatterBindingConstants.THING_ACTION_LABEL_NODE_DECOMMISSION, description = MatterBindingConstants.THING_ACTION_DESC_NODE_DECOMMISSION)
    public @ActionOutputs({
            @ActionOutput(name = "result", label = MatterBindingConstants.THING_ACTION_LABEL_NODE_DECOMMISSION_RESULT, type = "java.lang.String") }) String decommissionNode() {
        NodeHandler handler = this.handler;
        if (handler != null) {
            MatterControllerClient client = handler.getClient();
            if (client != null) {
                try {
                    client.removeNode(handler.getNodeId()).get();
                    return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_SUCCESS);
                } catch (InterruptedException | ExecutionException e) {
                    logger.debug("Failed to decommission device {}", handler.getNodeId(), e);
                    return Objects.requireNonNull(Optional.ofNullable(e.getLocalizedMessage()).orElse(e.toString()));
                }
            }
        }
        return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER);
    }

    @RuleAction(label = MatterBindingConstants.THING_ACTION_LABEL_NODE_GET_FABRICS, description = MatterBindingConstants.THING_ACTION_DESC_NODE_GET_FABRICS)
    public @ActionOutputs({
            @ActionOutput(name = "result", label = MatterBindingConstants.THING_ACTION_LABEL_NODE_GET_FABRICS_RESULT, type = "java.lang.String") }) String getFabrics() {
        NodeHandler handler = this.handler;
        if (handler != null) {
            MatterControllerClient client = handler.getClient();
            if (client != null) {
                try {
                    List<OperationalCredentialsCluster.FabricDescriptorStruct> fabrics = client
                            .getFabrics(handler.getNodeId()).get();
                    String result = fabrics.stream().map(fabric -> String.format("#%d %s (%s)", fabric.fabricIndex,
                            fabric.label, MatterVendorIDs.VENDOR_IDS.get(fabric.vendorId)))
                            .collect(Collectors.joining(", "));
                    return result.isEmpty()
                            ? translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_FABRICS)
                            : result;
                } catch (InterruptedException | ExecutionException | JsonParseException e) {
                    logger.debug("Failed to retrieve fabrics {}", handler.getNodeId(), e);
                    return Objects.requireNonNull(Optional.ofNullable(e.getLocalizedMessage()).orElse(e.toString()));
                }
            }
        }
        return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER);
    }

    @RuleAction(label = MatterBindingConstants.THING_ACTION_LABEL_NODE_REMOVE_FABRIC, description = MatterBindingConstants.THING_ACTION_DESC_NODE_REMOVE_FABRIC)
    public @ActionOutputs({
            @ActionOutput(name = "result", label = MatterBindingConstants.THING_ACTION_LABEL_NODE_REMOVE_FABRIC_RESULT, type = "java.lang.String") }) String removeFabric(
                    @ActionInput(name = "indexNumber", label = MatterBindingConstants.THING_ACTION_LABEL_NODE_REMOVE_FABRIC_INDEX, description = MatterBindingConstants.THING_ACTION_DESC_NODE_REMOVE_FABRIC_INDEX) Integer indexNumber) {
        NodeHandler handler = this.handler;
        if (handler != null) {
            MatterControllerClient client = handler.getClient();
            if (client != null) {
                try {
                    client.removeFabric(handler.getNodeId(), indexNumber).get();
                    return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_SUCCESS);
                } catch (InterruptedException | ExecutionException e) {
                    logger.debug("Failed to remove fabric {} {} ", handler.getNodeId(), indexNumber, e);
                    return Objects.requireNonNull(Optional.ofNullable(e.getLocalizedMessage()).orElse(e.toString()));
                }
            }
        }
        return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER);
    }
}
