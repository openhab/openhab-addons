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
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.controller.devices.converter.ThreadBorderRouterManagementConverter;
import org.openhab.binding.matter.internal.handler.NodeHandler;
import org.openhab.binding.matter.internal.util.ThreadDataset;
import org.openhab.binding.matter.internal.util.ThreadDataset.ThreadTimestamp;
import org.openhab.binding.matter.internal.util.TlvCodec;
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

    @RuleAction(label = "Thread: Load external operational dataset", description = "Updates the local operational dataset configuration from a hex or JSON string for the node. Use the 'Push local operational dataset' action to push the dataset back to the device after loading.")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = "Set Result", type = "java.lang.String") }) String loadExternalOperationalDataset(
                    @ActionInput(name = "dataset", label = "Thread operational dataset", description = "The thread operational dataset to set (hex or JSON)") String dataset) {
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
                            return "error: Invalid JSON dataset";
                        }
                    } else {
                        tds = ThreadDataset.fromHex(dataset);
                    }
                    converter.updateThreadConfiguration(tds.toHex());
                    return "success";
                } catch (Exception e) {
                    logger.debug("Error setting  dataset", e);
                    return "error: " + e.getMessage();
                }
            } else {
                return "error: No converter found";
            }
        } else {
            return "error: No handler found";
        }
    }

    @RuleAction(label = "Thread: Load operational dataset from device", description = "Updates the local operational dataset configuration from the device.")
    public @Nullable @ActionOutputs({ @ActionOutput(name = "result", label = "Set Result", type = "java.lang.String"),
            @ActionOutput(name = "dataset", label = "Operational Dataset (Hex)", type = "java.lang.String") }) Map<String, Object> loadActiveOperationalDataset() {
        NodeHandler handler = this.handler;
        if (handler != null) {
            ThreadBorderRouterManagementConverter converter = handler
                    .findConverterByType(ThreadBorderRouterManagementConverter.class);
            if (converter != null) {
                try {
                    String dataset = Objects.requireNonNull(converter.getActiveDataset().get(),
                            "Could not get active dataset");
                    converter.updateThreadConfiguration(dataset);
                    return new TreeMap<String, Object>() {
                        {
                            put("result", "success");
                            put("dataset", dataset);
                        }
                    };
                } catch (Exception e) {
                    logger.debug("Error setting  dataset", e);
                    String message = Objects.requireNonNull(Optional.ofNullable(e.getMessage()).orElse(e.toString()));
                    return Map.of("error", message);
                }
            } else {
                return Map.of("error", "No converter found");
            }
        } else {
            return Map.of("error", "No handler found");
        }
    }

    @RuleAction(label = "Thread: Push local operational dataset", description = "Pushes the local operational dataset configuration to the device.")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = "Set Result", type = "java.lang.String") }) String pushOperationalDataSetHex(
                    @ActionInput(name = "delay", label = "Pending Delay", description = "The delay in milliseconds before the pending dataset is active. Required to be a minimum of 30 seconds (30000ms), use a longer value of 5 minutes or more if battery powered (sleepy) devices are present to ensure all device are notified of the new dataset.", defaultValue = "30000", required = true) @Nullable Long delay,
                    @ActionInput(name = "generatePendingTime", label = "Generate New Pending Timestamp", description = "Generate a new pending timestamp based on the current time.", defaultValue = "true", required = true) @Nullable Boolean generatePendingTime,
                    @ActionInput(name = "incrementActiveTime", label = "Increment Active Timestamp", description = "Increment the active timestamp by x seconds, required to commit the dataset if the existing active timestamp has the same value.", defaultValue = "1", required = true) @Nullable Integer incrementActiveTime) {
        NodeHandler handler = this.handler;
        if (handler == null) {
            return "error: No handler found";
        }
        ThreadBorderRouterManagementConverter converter = handler
                .findConverterByType(ThreadBorderRouterManagementConverter.class);
        if (converter == null) {
            return "error: No converter found";
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
            return "success: " + tds.toJson();
        } catch (Exception e) {
            logger.debug("Error setting pending dataset", e);
            return "error: " + e.getMessage();
        }
    }

    @RuleAction(label = "Thread: Operational Dataset Generator", description = "Generates a new operational dataset and optionally saves it locally.")
    public @Nullable @ActionOutputs({ @ActionOutput(name = "result", label = "Set Result", type = "java.lang.String"),
            @ActionOutput(name = "datasetJson", label = "Operational Dataset JSON", type = "java.lang.String"),
            @ActionOutput(name = "datasetHex", label = "Operational Dataset Hex", type = "java.lang.String") }) Map<String, Object> generateOperationalDataset(
                    @ActionInput(name = "save", label = "Save new operational dataset", description = "Both return the JSON dataset, and save the dataset to this Thing's configuration.  To push the dataset to the device, use the 'Push local operational dataset' action after committing.", defaultValue = "false", required = true) @Nullable Boolean save,
                    @ActionInput(name = "channel", label = "Thread network channel number", description = "The thread network channel number (11-26)", defaultValue = "22", required = true) @Nullable Integer channel,
                    @ActionInput(name = "activeTimestampSeconds", label = "Thread active timestamp seconds", description = "The thread active timestamp seconds", defaultValue = "1", required = true) @Nullable Long activeTimestampSeconds,
                    @ActionInput(name = "activeTimestampTicks", label = "Thread active timestamp ticks", description = "The thread active timestamp ticks", defaultValue = "0", required = true) @Nullable Integer activeTimestampTicks,
                    @ActionInput(name = "activeTimestampAuthoritative", label = "Thread active timestamp is authoritative", description = "The thread active timestamp is authoritative", defaultValue = "false", required = true) @Nullable Boolean activeTimestampAuthoritative,
                    @ActionInput(name = "panId", label = "Thread PAN ID", description = "The thread PAN ID to (1-65535)", defaultValue = "4460", required = true) @Nullable Integer panId,
                    @ActionInput(name = "extendedPanId", label = "Thread extended PAN ID", description = "The thread extended PAN ID in hex format(16 characters)", defaultValue = "1111111122222222", required = true) @Nullable String extendedPanId,
                    @ActionInput(name = "meshLocalPrefix", label = "Thread mesh-local prefix", description = "The thread mesh-local prefix", defaultValue = "fd11:22::/64", required = true) @Nullable String meshLocalPrefix,
                    @ActionInput(name = "networkName", label = "Thread network name", description = "The thread network name", defaultValue = "openHAB-Thread", required = true) @Nullable String networkName,
                    @ActionInput(name = "networkKey", label = "Thread network key", description = "The thread network key. Leave blank to auto generate a secure key", required = false) @Nullable String networkKey,
                    @ActionInput(name = "passPhrase", label = "Passphrase/Commissioner Credential", description = "The thread Passphrase/Commissioner Credential", defaultValue = "j01Nme", required = true) @Nullable String passPhrase,
                    @ActionInput(name = "rotationTime", label = "Security Policy Rotation Time", description = "Security Policy Rotation Time (hours). Defaults to 672.", defaultValue = "672") @Nullable Integer rotationTime,
                    @ActionInput(name = "obtainNetworkKey", label = "Security Policy: Obtain Network Key", description = "Security Policy: Obtain Network Key. Defaults to true.", defaultValue = "true") @Nullable Boolean obtainNetworkKey,
                    @ActionInput(name = "nativeCommissioning", label = "Security Policy: Native Commissioning", description = "Security Policy: Native Commissioning. Defaults to true.", defaultValue = "true") @Nullable Boolean nativeCommissioning,
                    @ActionInput(name = "routers", label = "Security Policy: Routers", description = "Security Policy: Routers Enabled. Defaults to true.", defaultValue = "true") @Nullable Boolean routers,
                    @ActionInput(name = "externalCommissioning", label = "Security Policy: External Commissioning", description = "Security Policy: External Commissioning. Defaults to true.", defaultValue = "true") @Nullable Boolean externalCommissioning,
                    @ActionInput(name = "commercialCommissioning", label = "Security Policy: Commercial Commissioning", description = "Security Policy: Commercial Commissioning. Defaults to false.", defaultValue = "false") @Nullable Boolean commercialCommissioning,
                    @ActionInput(name = "autonomousEnrollment", label = "Security Policy: Autonomous Enrollment", description = "Security Policy: Autonomous Enrollment. Defaults to true.", defaultValue = "true") @Nullable Boolean autonomousEnrollment,
                    @ActionInput(name = "networkKeyProvisioning", label = "Security Policy: Network Key Provisioning", description = "Security Policy: Network Key Provisioning. Defaults to true.", defaultValue = "true") @Nullable Boolean networkKeyProvisioning,
                    @ActionInput(name = "tobleLink", label = "Security Policy: TO BLE Link", description = "Security Policy: TO BLE Link. Defaults to true.", defaultValue = "true") @Nullable Boolean tobleLink,
                    @ActionInput(name = "nonCcmRouters", label = "Security Policy: Non-CCM Routers", description = "Security Policy: Non-CCM Routers. Defaults to false.", defaultValue = "false") @Nullable Boolean nonCcmRouters) {
        NodeHandler handler = this.handler;
        if (handler == null) {
            return Map.of("error", "No handler found");
        }
        ThreadBorderRouterManagementConverter converter = handler
                .findConverterByType(ThreadBorderRouterManagementConverter.class);
        if (converter == null) {
            return Map.of("error", "No converter found");
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
                    return Map.of("error", "Error generating master key: " + e.getMessage());
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
            return new TreeMap<String, Object>() {
                {
                    put("result", "success");
                    put("datasetJson", json);
                    put("datasetHex", hex);
                }
            };
        } catch (Exception e) {
            logger.debug("Error setting active dataset", e);
            return Map.of("error", "Error setting active dataset");
        }
    }
}
