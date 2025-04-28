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
package org.openhab.binding.lgthinq.lgservices.model.devices.ac;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.*;
import static org.openhab.binding.lgthinq.lgservices.model.DeviceTypes.HEAT_PUMP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapabilityFactory;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDataType;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.MonitoringResultFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link AbstractACCapabilityFactory}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractACCapabilityFactory extends AbstractCapabilityFactory<ACCapability> {
    private final Logger logger = LoggerFactory.getLogger(AbstractACCapabilityFactory.class);

    @Override
    public final List<DeviceTypes> getSupportedDeviceTypes() {
        return List.of(DeviceTypes.AIR_CONDITIONER, HEAT_PUMP);
    }

    protected abstract Map<String, String> extractFeatureOptions(JsonNode optionsNode);

    @Override
    protected FeatureDefinition newFeatureDefinition(String featureName, JsonNode featuresNode,
            @Nullable String targetChannelId, @Nullable String refChannelId) {
        JsonNode featureNode = featuresNode.path(featureName);
        if (featureNode.isMissingNode()) {
            return FeatureDefinition.NULL_DEFINITION;
        }
        FeatureDefinition fd = new FeatureDefinition();
        fd.setName(featureName);
        fd.setDataType(FeatureDataType.fromValue(featureNode.path(getDataTypeFeatureNodeName()).asText()));
        JsonNode options = featureNode.path(getOptionsMapNodeName());
        if (options.isMissingNode()) {
            return FeatureDefinition.NULL_DEFINITION;
        }
        fd.setValuesMapping(extractFeatureOptions(options));
        return fd;
    }

    protected abstract String getDataTypeFeatureNodeName();

    private List<String> extractValueOptions(JsonNode optionsNode) throws LGThinqApiException {
        if (optionsNode.isMissingNode()) {
            throw new LGThinqApiException("Error extracting options supported by the device");
        } else {
            List<String> values = new ArrayList<>();
            optionsNode.fields().forEachRemaining(e -> {
                values.add(e.getValue().asText());
            });
            return values;
        }
    }

    private Map<String, String> extractOptions(JsonNode optionsNode, boolean invertKeyValue) {
        if (optionsNode.isMissingNode()) {
            logger.warn("Error extracting options supported by the device");
            return Collections.emptyMap();
        } else {
            Map<String, String> modes = new HashMap<String, String>();
            optionsNode.fields().forEachRemaining(e -> {
                if (invertKeyValue) {
                    modes.put(e.getValue().asText(), e.getKey());
                } else {
                    modes.put(e.getKey(), e.getValue().asText());
                }
            });
            return modes;
        }
    }

    @Override
    public ACCapability create(JsonNode rootNode) throws LGThinqException {
        ACCapability acCap = super.create(rootNode);

        JsonNode valuesNode = rootNode.path(getValuesNodeName());
        if (valuesNode.isMissingNode()) {
            throw new LGThinqApiException("Error extracting capabilities supported by the device");
        }
        // supported operation modes
        Map<String, String> allOpModes = extractOptions(
                valuesNode.path(getOpModeNodeName()).path(getOptionsMapNodeName()), true);
        Map<String, String> allFanSpeeds = extractOptions(
                valuesNode.path(getFanSpeedNodeName()).path(getOptionsMapNodeName()), true);

        List<String> supOpModeValues = extractValueOptions(
                valuesNode.path(getSupOpModeNodeName()).path(getOptionsMapNodeName()));
        List<String> supFanSpeedValues = extractValueOptions(
                valuesNode.path(getSupFanSpeedNodeName()).path(getOptionsMapNodeName()));
        supOpModeValues.remove("@NON");
        supOpModeValues.remove("@NON");
        // find correct operation IDs
        Map<String, String> opModes = new HashMap<>(supOpModeValues.size());
        supOpModeValues.forEach(v -> {
            // discovery ID of the operation
            String key = allOpModes.get(v);
            if (key != null) {
                opModes.put(key, v);
            }
        });
        acCap.setOpMod(opModes);

        Map<String, String> fanSpeeds = new HashMap<>(supFanSpeedValues.size());
        supFanSpeedValues.forEach(v -> {
            // discovery ID of the fan speed
            String key = allFanSpeeds.get(v);
            if (key != null) {
                fanSpeeds.put(key, v);
            }
        });
        acCap.setFanSpeed(fanSpeeds);

        // ===== get supported extra modes

        JsonNode supRacSubModeOps = valuesNode.path(getSupSubRacModeNodeName()).path(getOptionsMapNodeName());
        if (!supRacSubModeOps.isMissingNode()) {
            supRacSubModeOps.fields().forEachRemaining(f -> {
                if (CAP_AC_SUB_MODE_COOL_JET.equals(f.getValue().asText())) {
                    acCap.setJetModeAvailable(true);
                }
                if (CAP_AC_SUB_MODE_STEP_UP_DOWN.equals(f.getValue().asText())) {
                    acCap.setStepUpDownAvailable(true);
                }
                if (CAP_AC_SUB_MODE_STEP_LEFT_RIGHT.equals(f.getValue().asText())) {
                    acCap.setStepLeftRightAvailable(true);
                }
            });
        }

        // set Cool jetMode supportability
        if (acCap.isJetModeAvailable()) {
            JsonNode jetModeOps = valuesNode.path(getJetModeNodeName()).path(getOptionsMapNodeName());
            if (!jetModeOps.isMissingNode()) {
                jetModeOps.fields().forEachRemaining(j -> {
                    String value = j.getValue().asText();
                    if (CAP_AC_COOL_JET.containsKey(value)) {
                        acCap.setCoolJetModeCommandOn(j.getKey());
                    } else if (CAP_AC_COMMAND_OFF.equals(value)) {
                        acCap.setCoolJetModeCommandOff(j.getKey());
                    }
                });
            }
        }
        // ============== Collect Wind Direction (Up-Down, Left-Right) if supported ==================
        if (acCap.isStepUpDownAvailable()) {
            Map<String, String> stepUpDownValueMap = extractOptions(
                    valuesNode.path(getStepUpDownNodeName()).path(getOptionsMapNodeName()), false);
            // remove options who value doesn't start with @, that indicates for this feature that is not supported
            stepUpDownValueMap.values().removeIf(v -> !v.startsWith("@"));
            acCap.setStepUpDown(stepUpDownValueMap);
        }

        if (acCap.isStepLeftRightAvailable()) {
            Map<String, String> stepLeftRightValueMap = extractOptions(
                    valuesNode.path(getStepLeftRightNodeName()).path(getOptionsMapNodeName()), false);
            // remove options who value doesn't start with @, that indicates for this feature that is not supported
            stepLeftRightValueMap.values().removeIf(v -> !v.startsWith("@"));
            acCap.setStepLeftRight(stepLeftRightValueMap);
        }
        // =================================================== //

        // get Supported RAC Mode
        JsonNode supRACModeOps = valuesNode.path(getSupRacModeNodeName()).path(getOptionsMapNodeName());

        if (!supRACModeOps.isMissingNode()) {
            supRACModeOps.fields().forEachRemaining(r -> {
                String racOpValue = r.getValue().asText();
                switch (racOpValue) {
                    case CAP_AC_AUTODRY:
                        Map<String, String> dryStates = extractOptions(
                                valuesNode.path(getAutoDryStateNodeName()).path(getOptionsMapNodeName()), true);
                        if (!dryStates.isEmpty()) { // sanity check
                            acCap.setAutoDryModeAvailable(true);
                            dryStates.forEach((cmdKey, cmdValue) -> {
                                switch (cmdKey) {
                                    case CAP_AC_COMMAND_OFF:
                                        acCap.setAutoDryModeCommandOff(cmdValue);
                                        break;
                                    case CAP_AC_COMMAND_ON:
                                        acCap.setAutoDryModeCommandOn(cmdValue);
                                }
                            });
                        }
                        break;
                    case CAP_AC_AIRCLEAN:
                        Map<String, String> airCleanStates = extractOptions(
                                valuesNode.path(getAirCleanStateNodeName()).path(getOptionsMapNodeName()), true);
                        if (!airCleanStates.isEmpty()) {
                            acCap.setAirCleanAvailable(true);
                            airCleanStates.forEach((cmdKey, cmdValue) -> {
                                switch (cmdKey) {
                                    case CAP_AC_AIR_CLEAN_COMMAND_OFF:
                                        acCap.setAirCleanModeCommandOff(cmdValue);
                                        break;
                                    case CAP_AC_AIR_CLEAN_COMMAND_ON:
                                        acCap.setAirCleanModeCommandOn(cmdValue);
                                }
                            });
                        }
                        break;
                    case CAP_AC_ENERGYSAVING:
                        acCap.setEnergySavingAvailable(true);
                        // there's no definition for this values. Assuming the defaults
                        acCap.setEnergySavingModeCommandOff("0");
                        acCap.setEnergySavingModeCommandOn("1");
                        break;
                }
            });
        }
        if (HEAT_PUMP.equals(acCap.getDeviceType())) {
            JsonNode supHpAirSwitchNode = valuesNode.path(getHpAirWaterSwitchNodeName()).path(getOptionsMapNodeName());
            if (!supHpAirSwitchNode.isMissingNode()) {
                supHpAirSwitchNode.fields().forEachRemaining(r -> {
                    r.getValue().asText();
                });
            }
        }

        JsonNode infoNode = rootNode.get("Info");
        if (infoNode.isMissingNode()) {
            logger.warn("No info session defined in the cap data.");
        } else {
            // try to find monitoring result format
            MonitoringResultFormat format = MonitoringResultFormat.getFormatOf(infoNode.path("model").asText());
            if (!MonitoringResultFormat.UNKNOWN_FORMAT.equals(format)) {
                acCap.setMonitoringDataFormat(format);
            }
        }
        return acCap;
    }

    protected abstract String getOpModeNodeName();

    protected abstract String getFanSpeedNodeName();

    protected abstract String getSupOpModeNodeName();

    protected abstract String getSupFanSpeedNodeName();

    protected abstract String getJetModeNodeName();

    protected abstract String getStepUpDownNodeName();

    protected abstract String getStepLeftRightNodeName();

    protected abstract String getSupSubRacModeNodeName();

    protected abstract String getSupRacModeNodeName();

    protected abstract String getAutoDryStateNodeName();

    protected abstract String getAirCleanStateNodeName();

    protected abstract String getOptionsMapNodeName();

    @Override
    public ACCapability getCapabilityInstance() {
        return new ACCapability();
    }

    protected abstract String getValuesNodeName();

    // ===== For HP only ====
    protected abstract String getHpAirWaterSwitchNodeName();
}
