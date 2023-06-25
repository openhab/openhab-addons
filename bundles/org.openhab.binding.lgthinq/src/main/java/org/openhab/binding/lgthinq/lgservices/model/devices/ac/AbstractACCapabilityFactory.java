/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;
import static org.openhab.binding.lgthinq.lgservices.model.DeviceTypes.HEAT_PUMP;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapabilityFactory;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDataType;
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

    private Map<String, String> extractInvertedOptions(JsonNode optionsNode) {
        if (optionsNode.isMissingNode()) {
            logger.warn("Error extracting options supported by the device");
            return Collections.EMPTY_MAP;
        } else {
            Map<String, String> modes = new HashMap<String, String>();
            optionsNode.fields().forEachRemaining(e -> {
                modes.put(e.getValue().asText(), e.getKey());
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
        Map<String, String> allOpModes = extractInvertedOptions(
                valuesNode.path(getOpModeNodeName()).path(getOptionsMapNodeName()));
        Map<String, String> allFanSpeeds = extractInvertedOptions(
                valuesNode.path(getFanSpeedNodeName()).path(getOptionsMapNodeName()));

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
        boolean isSupportDryMode = false, isSupportEnergyMode = false;

        JsonNode supRacSubModeOps = valuesNode.path(getSupSubRacModeNodeName()).path(getOptionsMapNodeName());
        if (!supRacSubModeOps.isMissingNode()) {
            supRacSubModeOps.fields().forEachRemaining(f -> {
                if ("@AC_MAIN_WIND_MODE_COOL_JET_W".equals(f.getValue().asText())) {
                    acCap.setJetModeAvailable(true);
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
        // get Supported RAC Mode
        JsonNode supRACModeOps = valuesNode.path(getSupRacModeNodeName()).path(getOptionsMapNodeName());

        if (!supRACModeOps.isMissingNode()) {
            supRACModeOps.fields().forEachRemaining(r -> {
                String racOpValue = r.getValue().asText();
                switch (racOpValue) {
                    case CAP_AC_AUTODRY:
                        Map<String, String> dryStates = extractInvertedOptions(
                                valuesNode.path(getAutoDryStateNodeName()).path(getOptionsMapNodeName()));
                        if (!dryStates.isEmpty()) { // sanity check
                            acCap.setAutoDryModeAvailable(true);
                            dryStates.forEach((cmdValue, cmdKey) -> {
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
                        Map<String, String> airCleanStates = extractInvertedOptions(
                                valuesNode.path(getAirCleanStateNodeName()).path(getOptionsMapNodeName()));
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
}
