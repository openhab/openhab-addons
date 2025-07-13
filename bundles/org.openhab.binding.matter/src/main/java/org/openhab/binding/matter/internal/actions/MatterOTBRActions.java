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

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.MatterBindingConstants;
import org.openhab.binding.matter.internal.controller.devices.converter.ThreadBorderRouterManagementConverter;
import org.openhab.binding.matter.internal.handler.NodeHandler;
import org.openhab.binding.matter.internal.util.ThreadDataset;
import org.openhab.binding.matter.internal.util.ThreadDataset.ThreadTimestamp;
import org.openhab.binding.matter.internal.util.TlvCodec;
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

/**
 * The {@link MatterOTBRActions} exposes Thread Border Router related actions
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = MatterOTBRActions.class)
@ThingActionsScope(name = "matter-otbr")
public class MatterOTBRActions implements ThingActions {
    public final Logger logger = LoggerFactory.getLogger(getClass());

    protected @Nullable NodeHandler handler;
    private final TranslationService translationService;

    @Activate
    public MatterOTBRActions(@Reference TranslationService translationService) {
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

    @RuleAction(label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_LOAD_EXTERNAL_DATASET, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_LOAD_EXTERNAL_DATASET)
    public @ActionOutputs({
            @ActionOutput(name = "result", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_LOAD_EXTERNAL_DATASET_RESULT, type = "java.lang.String") }) String loadExternalOperationalDataset(
                    @ActionInput(name = "dataset", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_LOAD_EXTERNAL_DATASET_DATASET, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_LOAD_EXTERNAL_DATASET_DATASET) String dataset) {
        NodeHandler handler = this.handler;
        if (handler != null) {
            ThreadBorderRouterManagementConverter converter = handler
                    .findConverterByType(ThreadBorderRouterManagementConverter.class);
            if (converter != null) {
                try {
                    ThreadDataset tds = null;
                    if (dataset.trim().startsWith("{")) {
                        tds = ThreadDataset.fromJson(dataset);
                        if (tds == null) {
                            return translationService
                                    .getTranslation(MatterBindingConstants.THING_ACTION_RESULT_INVALID_JSON);
                        }
                    } else {
                        tds = ThreadDataset.fromHex(dataset);
                    }
                    converter.updateThreadConfiguration(tds.toHex());
                    return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_SUCCESS);
                } catch (Exception e) {
                    logger.debug("Error setting  dataset", e);
                    return "error: " + e.getMessage();
                }
            } else {
                return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_CONVERTER);
            }
        } else {
            return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER);
        }
    }

    @RuleAction(label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_LOAD_ACTIVE_DATASET, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_LOAD_ACTIVE_DATASET)
    public @ActionOutputs({
            @ActionOutput(name = "result", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_LOAD_ACTIVE_DATASET_RESULT, type = "java.lang.String"),
            @ActionOutput(name = "dataset", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_LOAD_ACTIVE_DATASET_DATASET, type = "java.lang.String") }) Map<String, Object> loadActiveOperationalDataset() {
        NodeHandler handler = this.handler;
        if (handler != null) {
            ThreadBorderRouterManagementConverter converter = handler
                    .findConverterByType(ThreadBorderRouterManagementConverter.class);
            if (converter != null) {
                try {
                    String dataset = Objects.requireNonNull(converter.getActiveDataset().get(),
                            "Could not get active dataset");
                    converter.updateThreadConfiguration(dataset);
                    return Map.of("result",
                            translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_SUCCESS),
                            "dataset", dataset);
                } catch (Exception e) {
                    logger.debug("Error setting  dataset", e);
                    String message = Objects.requireNonNull(Optional.ofNullable(e.getMessage()).orElse(e.toString()));
                    return Map.of("error", message);
                }
            } else {
                return Map.of("error",
                        translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_CONVERTER));
            }
        } else {
            return Map.of("error",
                    translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER));
        }
    }

    @RuleAction(label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_PUSH_DATASET, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_PUSH_DATASET)
    public @ActionOutputs({
            @ActionOutput(name = "result", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_PUSH_DATASET_RESULT, type = "java.lang.String") }) String pushOperationalDataSetHex(
                    @ActionInput(name = "delay", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_PUSH_DATASET_DELAY, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_PUSH_DATASET_DELAY, defaultValue = "30000", required = true) @Nullable Long delay,
                    @ActionInput(name = "generatePendingTime", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_PUSH_DATASET_GENERATE_TIME, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_PUSH_DATASET_GENERATE_TIME, defaultValue = "true", required = true) @Nullable Boolean generatePendingTime,
                    @ActionInput(name = "incrementActiveTime", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_PUSH_DATASET_INCREMENT_TIME, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_PUSH_DATASET_INCREMENT_TIME, defaultValue = "1", required = true) @Nullable Integer incrementActiveTime) {
        NodeHandler handler = this.handler;
        if (handler == null) {
            return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER);
        }
        ThreadBorderRouterManagementConverter converter = handler
                .findConverterByType(ThreadBorderRouterManagementConverter.class);
        if (converter == null) {
            return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_CONVERTER);
        }
        ThreadDataset tds = converter.datasetFromConfiguration();
        if (delay == null) {
            delay = 30000L;
        }
        // default to generating a new pending timestamp
        if (generatePendingTime == null || generatePendingTime.booleanValue()) {
            tds.setPendingTimestamp(ThreadTimestamp.now(false));
        }
        ThreadTimestamp ts = Objects
                .requireNonNull(tds.getActiveTimestampObject().orElse(new ThreadTimestamp(1, 0, false)));

        ts.setSeconds(ts.getSeconds() + (incrementActiveTime == null ? 1 : incrementActiveTime.intValue()));
        tds.setActiveTimestamp(ts);
        tds.setDelayTimer(delay);
        logger.debug("New dataset: {}", tds.toJson());
        String dataset = tds.toHex();
        logger.debug("New dataset hex: {}", dataset);
        try {
            converter.setPendingDataset(dataset).get();
            return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_SUCCESS) + ": "
                    + tds.toJson();
        } catch (Exception e) {
            logger.debug("Error setting pending dataset", e);
            return "error: " + e.getMessage();
        }
    }

    @RuleAction(label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET)
    public @ActionOutputs({
            @ActionOutput(name = "result", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_RESULT, type = "java.lang.String"),
            @ActionOutput(name = "datasetJson", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_JSON, type = "java.lang.String"),
            @ActionOutput(name = "datasetHex", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_HEX, type = "java.lang.String") }) Map<String, Object> generateOperationalDataset(
                    @ActionInput(name = "save", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_SAVE, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_SAVE, defaultValue = "false", required = true) @Nullable Boolean save,
                    @ActionInput(name = "channel", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_CHANNEL, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_CHANNEL, defaultValue = "22", required = true) @Nullable Integer channel,
                    @ActionInput(name = "activeTimestampSeconds", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_TIMESTAMP_SECONDS, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_TIMESTAMP_SECONDS, defaultValue = "1", required = true) @Nullable Long activeTimestampSeconds,
                    @ActionInput(name = "activeTimestampTicks", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_TIMESTAMP_TICKS, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_TIMESTAMP_TICKS, defaultValue = "0", required = true) @Nullable Integer activeTimestampTicks,
                    @ActionInput(name = "activeTimestampAuthoritative", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_TIMESTAMP_AUTHORITATIVE, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_TIMESTAMP_AUTHORITATIVE, defaultValue = "false", required = true) @Nullable Boolean activeTimestampAuthoritative,
                    @ActionInput(name = "panId", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_PAN_ID, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_PAN_ID, defaultValue = "4460", required = true) @Nullable Integer panId,
                    @ActionInput(name = "extendedPanId", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_EXTENDED_PAN_ID, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_EXTENDED_PAN_ID, defaultValue = "1111111122222222", required = true) @Nullable String extendedPanId,
                    @ActionInput(name = "meshLocalPrefix", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_MESH_PREFIX, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_MESH_PREFIX, defaultValue = "fd11:22::/64", required = true) @Nullable String meshLocalPrefix,
                    @ActionInput(name = "networkName", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_NETWORK_NAME, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_NETWORK_NAME, defaultValue = "openHAB-Thread", required = true) @Nullable String networkName,
                    @ActionInput(name = "networkKey", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_NETWORK_KEY, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_NETWORK_KEY, required = false) @Nullable String networkKey,
                    @ActionInput(name = "passPhrase", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_PASSPHRASE, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_PASSPHRASE, defaultValue = "j01Nme", required = true) @Nullable String passPhrase,
                    @ActionInput(name = "rotationTime", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_ROTATION_TIME, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_ROTATION_TIME, defaultValue = "672") @Nullable Integer rotationTime,
                    @ActionInput(name = "obtainNetworkKey", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_OBTAIN_NETWORK_KEY, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_OBTAIN_NETWORK_KEY, defaultValue = "true") @Nullable Boolean obtainNetworkKey,
                    @ActionInput(name = "nativeCommissioning", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_NATIVE_COMMISSIONING, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_NATIVE_COMMISSIONING, defaultValue = "true") @Nullable Boolean nativeCommissioning,
                    @ActionInput(name = "routers", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_ROUTERS, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_ROUTERS, defaultValue = "true") @Nullable Boolean routers,
                    @ActionInput(name = "externalCommissioning", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_EXTERNAL_COMMISSIONING, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_EXTERNAL_COMMISSIONING, defaultValue = "true") @Nullable Boolean externalCommissioning,
                    @ActionInput(name = "commercialCommissioning", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_COMMERCIAL_COMMISSIONING, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_COMMERCIAL_COMMISSIONING, defaultValue = "false") @Nullable Boolean commercialCommissioning,
                    @ActionInput(name = "autonomousEnrollment", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_AUTONOMOUS_ENROLLMENT, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_AUTONOMOUS_ENROLLMENT, defaultValue = "true") @Nullable Boolean autonomousEnrollment,
                    @ActionInput(name = "networkKeyProvisioning", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_NETWORK_KEY_PROVISIONING, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_NETWORK_KEY_PROVISIONING, defaultValue = "true") @Nullable Boolean networkKeyProvisioning,
                    @ActionInput(name = "tobleLink", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_TOBLE_LINK, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_TOBLE_LINK, defaultValue = "true") @Nullable Boolean tobleLink,
                    @ActionInput(name = "nonCcmRouters", label = MatterBindingConstants.THING_ACTION_LABEL_OTBR_GENERATE_DATASET_NON_CCM_ROUTERS, description = MatterBindingConstants.THING_ACTION_DESC_OTBR_GENERATE_DATASET_NON_CCM_ROUTERS, defaultValue = "false") @Nullable Boolean nonCcmRouters) {
        NodeHandler handler = this.handler;
        if (handler == null) {
            return Map.of("error",
                    translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER));
        }
        ThreadBorderRouterManagementConverter converter = handler
                .findConverterByType(ThreadBorderRouterManagementConverter.class);
        if (converter == null) {
            return Map.of("error",
                    translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_CONVERTER));
        }
        try {
            ThreadTimestamp timestamp = new ThreadTimestamp(1, 0, false);
            if (activeTimestampSeconds != null) {
                timestamp.setSeconds(activeTimestampSeconds.longValue());
            }
            if (activeTimestampTicks != null) {
                timestamp.setTicks(activeTimestampTicks.intValue());
            }
            if (activeTimestampAuthoritative != null) {
                timestamp.setAuthoritative(activeTimestampAuthoritative.booleanValue());
            }
            long channelMask = 134215680;
            if (channel == null) {
                channel = 22;
            }
            if (panId == null) {
                panId = 4460;
            }
            if (extendedPanId == null) {
                extendedPanId = "1111111122222222";
            }
            if (meshLocalPrefix == null) {
                meshLocalPrefix = "fd11:22::/64";
            }
            if (networkName == null) {
                networkName = "openHAB-Thread";
            }
            if (passPhrase == null) {
                passPhrase = "j01Nme";
            }
            if (networkKey == null) {
                try {
                    networkKey = TlvCodec.bytesToHex(ThreadDataset.generateMasterKey());
                } catch (NoSuchAlgorithmException e) {
                    logger.debug("Error generating master key", e);
                    return Map.of("error",
                            translationService
                                    .getTranslation(MatterBindingConstants.THING_ACTION_RESULT_ERROR_GENERATING_KEY)
                                    + ": " + e.getMessage());
                }
            }
            if (save == null) {
                save = false;
            }

            String pskc = TlvCodec.bytesToHex(ThreadDataset.generatePskc(passPhrase, networkName, extendedPanId));

            logger.debug(
                    "All values: channel: {}, panId: {}, extendedPanId: {}, meshLocalPrefix: {}, networkName: {}, networkKey: {}, pskc: {}",
                    channel, panId, extendedPanId, meshLocalPrefix, networkName, networkKey, pskc);

            ThreadDataset dataset = new ThreadDataset(timestamp, null, null, channel, channelMask, panId, networkName,
                    networkKey, extendedPanId, pskc, meshLocalPrefix, null);

            int rotationTimeValue = (rotationTime == null) ? 672 : rotationTime.intValue();
            dataset.setSecurityPolicyRotation(rotationTimeValue);
            dataset.setObtainNetworkKey(obtainNetworkKey != null ? obtainNetworkKey.booleanValue() : true);
            dataset.setNativeCommissioning(nativeCommissioning != null ? nativeCommissioning.booleanValue() : true);
            dataset.setRoutersEnabled(routers != null ? routers.booleanValue() : true);
            dataset.setCommercialCommissioning(
                    commercialCommissioning != null ? commercialCommissioning.booleanValue() : false);
            dataset.setExternalCommissioning(
                    externalCommissioning != null ? externalCommissioning.booleanValue() : true);
            dataset.setAutonomousEnrollment(autonomousEnrollment != null ? autonomousEnrollment.booleanValue() : true);
            dataset.setNetworkKeyProvisioning(
                    networkKeyProvisioning != null ? networkKeyProvisioning.booleanValue() : true);
            dataset.setToBleLink(tobleLink != null ? tobleLink.booleanValue() : true);
            dataset.setNonCcmRouters(nonCcmRouters != null ? nonCcmRouters.booleanValue() : false);

            String json = dataset.toJson();
            String hex = dataset.toHex();
            logger.debug("Generated dataset: {}", json);
            logger.debug("Generated dataset hex: {}", hex);
            if (save.booleanValue()) {
                converter.updateThreadConfiguration(hex);
            }
            return Map.of("result",
                    translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_SUCCESS),
                    "datasetJson", json, "datasetHex", hex);
        } catch (Exception e) {
            logger.debug("Error setting active dataset", e);
            return Map.of("error", translationService
                    .getTranslation(MatterBindingConstants.THING_ACTION_RESULT_ERROR_SETTING_DATASET));
        }
    }
}
