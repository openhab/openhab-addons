/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.miot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.basic.CommandParameterType;
import org.openhab.binding.miio.internal.basic.DeviceMapping;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDeviceAction;
import org.openhab.binding.miio.internal.basic.MiIoDeviceActionCondition;
import org.openhab.binding.miio.internal.basic.OptionsValueListDTO;
import org.openhab.binding.miio.internal.basic.StateDescriptionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Support creation of the miot db files
 * based on the the online miot spec files
 *
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiotParser {
    private final Logger logger = LoggerFactory.getLogger(MiotParser.class);

    private static final String BASEURL = "https://miot-spec.org/miot-spec-v2/";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final boolean SKIP_SIID_1 = true;

    private String model;
    private @Nullable String urn;
    private @Nullable JsonElement urnData;
    private @Nullable MiIoBasicDevice device;

    public MiotParser(String model) {
        this.model = model;
    }

    public static MiotParser parse(String model, HttpClient httpClient) throws MiotParseException {
        MiotParser miotParser = new MiotParser(model);
        try {
            String urn = miotParser.getURN(model, httpClient);
            if (urn == null) {
                throw new MiotParseException("Device not found in in miot specs : " + model);
            }
            JsonElement urnData = miotParser.getUrnData(urn, httpClient);
            miotParser.getDevice(urnData);
            return miotParser;
        } catch (Exception e) {
            throw new MiotParseException("Error parsing miot data: " + e.getMessage(), e);
        }
    }

    /**
     * Outputs the device json file touched up so the format matches the regular OH standard formatting
     *
     * @param device
     * @return
     */
    public static String toJson(MiIoBasicDevice device) {
        String usersJson = GSON.toJson(device);
        usersJson = usersJson.replace(".0,\n", ",\n");
        usersJson = usersJson.replace("\n", "\r\n").replace("  ", "\t");
        return usersJson;
    }

    public void writeDevice(String path, MiIoBasicDevice device) {
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(toJson(device));
            logger.info("Database file created:{}", path);
        } catch (FileNotFoundException e) {
            logger.info("Error writing file: {}", e.getMessage());
        }
    }

    public MiIoBasicDevice getDevice(JsonElement urnData) throws MiotParseException {
        Set<String> unknownUnits = new HashSet<>();
        Map<ActionDTO, ServiceDTO> deviceActions = new LinkedHashMap<>();
        StringBuilder channelConfigText = new StringBuilder("Suggested additional channelType \r\n");

        StringBuilder actionText = new StringBuilder("Manual actions for execution\r\n");

        MiIoBasicDevice device = new MiIoBasicDevice();
        DeviceMapping deviceMapping = new DeviceMapping();
        MiotDeviceDataDTO miotDevice = GSON.fromJson(urnData, MiotDeviceDataDTO.class);
        if (miotDevice == null) {
            throw new MiotParseException("Error parsing miot data: null");
        }
        List<MiIoBasicChannel> miIoBasicChannels = new ArrayList<>();
        deviceMapping.setPropertyMethod(MiIoCommand.GET_PROPERTIES.getCommand());
        deviceMapping.setMaxProperties(1);
        deviceMapping.setExperimental(true);
        deviceMapping.setId(Arrays.asList(new String[] { model }));
        Set<String> propCheck = new HashSet<>();

        for (ServiceDTO service : miotDevice.services) {
            String serviceId = service.type.substring(service.type.indexOf("service:")).split(":")[1];
            logger.info("SID: {}, description: {}, identifier: {}", service.siid, service.description, serviceId);

            if (service.properties != null) {
                for (PropertyDTO property : service.properties) {
                    String propertyId = property.type.substring(property.type.indexOf("property:")).split(":")[1];
                    logger.info("siid: {}, description: {}, piid: {}, description: {}, identifier: {}", service.siid,
                            service.description, property.piid, property.description, propertyId);
                    if (service.siid == 1 && SKIP_SIID_1) {
                        continue;
                    }
                    if (property.access.contains("read") || property.access.contains("write")) {
                        MiIoBasicChannel miIoBasicChannel = new MiIoBasicChannel();
                        miIoBasicChannel
                                .setFriendlyName((isPureAscii(service.description) && !service.description.isBlank()
                                        ? captializedName(service.description)
                                        : captializedName(serviceId))
                                        + " - "
                                        + (isPureAscii(property.description) && !property.description.isBlank()
                                                ? captializedName(property.description)
                                                : captializedName(propertyId)));
                        miIoBasicChannel.setSiid(service.siid);
                        miIoBasicChannel.setPiid(property.piid);
                        // avoid duplicates and make camel case and avoid invalid channel names
                        String chanId = propertyId.replace(" ", "").replace(".", "_").replace("-", "_");

                        int cnt = 0;
                        while (propCheck.contains(chanId + Integer.toString(cnt))) {
                            cnt++;
                        }
                        propCheck.add(chanId.concat(Integer.toString(cnt)));
                        if (cnt > 0) {
                            chanId = chanId.concat(Integer.toString(cnt));
                            propertyId = propertyId.concat(Integer.toString(cnt));
                            logger.warn("duplicate for property:{} - {} ({}", chanId, property.description, cnt);
                        }
                        if (property.unit != null && !property.unit.isBlank()) {
                            if (!property.unit.contains("none")) {
                                miIoBasicChannel.setUnit(property.unit);
                            }
                        }
                        miIoBasicChannel.setProperty(propertyId);
                        miIoBasicChannel.setChannel(chanId);
                        switch (property.format) {
                            case "bool":
                                miIoBasicChannel.setType("Switch");
                                break;
                            case "uint8":
                            case "uint16":
                            case "uint32":
                            case "int8":
                            case "int16":
                            case "int32":
                            case "int64":
                            case "float":
                                StateDescriptionDTO stateDescription = miIoBasicChannel.getStateDescription();
                                int decimals = -1;
                                String unit = "";
                                if (stateDescription == null) {
                                    stateDescription = new StateDescriptionDTO();
                                }
                                String type = MiIoQuantiyTypesConversion.getType(property.unit);
                                if (type != null) {
                                    miIoBasicChannel.setType("Number" + ":" + type);
                                    unit = " %unit%";
                                    decimals = property.format.contentEquals("float") ? 1 : 0;

                                } else {
                                    miIoBasicChannel.setType("Number");
                                    decimals = property.format.contentEquals("uint8") ? 0 : 1;
                                    if (property.unit != null) {
                                        unknownUnits.add(property.unit);
                                    }
                                }
                                if (property.valueRange != null && property.valueRange.size() == 3) {
                                    stateDescription
                                            .setMinimum(BigDecimal.valueOf(property.valueRange.get(0).doubleValue()));
                                    stateDescription
                                            .setMaximum(BigDecimal.valueOf(property.valueRange.get(1).doubleValue()));

                                    double step = property.valueRange.get(2).doubleValue();
                                    if (step != 0) {
                                        stateDescription.setStep(BigDecimal.valueOf(step));
                                        if (step >= 1) {
                                            decimals = 0;
                                        }
                                    }
                                }
                                if (decimals > -1) {
                                    stateDescription.setPattern("%." + Integer.toString(decimals) + "f" + unit);
                                }
                                miIoBasicChannel.setStateDescription(stateDescription);
                                break;
                            case "string":
                                miIoBasicChannel.setType("String");
                                break;
                            case "hex":
                                miIoBasicChannel.setType("String");
                                logger.info("no type mapping implemented for {}", property.format);
                                break;
                            default:
                                miIoBasicChannel.setType("String");
                                logger.info("no type mapping for {}", property.format);
                                break;
                        }
                        miIoBasicChannel.setRefresh(property.access.contains("read"));
                        // add option values
                        if (property.valueList != null && !property.valueList.isEmpty()) {
                            StateDescriptionDTO stateDescription = miIoBasicChannel.getStateDescription();
                            if (stateDescription == null) {
                                stateDescription = new StateDescriptionDTO();
                            }
                            stateDescription.setPattern(null);
                            List<OptionsValueListDTO> channeloptions = new LinkedList<>();
                            for (OptionsValueDescriptionsListDTO miotOption : property.valueList) {
                                // miIoBasicChannel.setValueList(property.valueList);
                                OptionsValueListDTO basicOption = new OptionsValueListDTO();
                                basicOption.setLabel(miotOption.getDescription());
                                basicOption.setValue(String.valueOf(miotOption.value));
                                channeloptions.add(basicOption);
                            }
                            stateDescription.setOptions(channeloptions);
                            miIoBasicChannel.setStateDescription(stateDescription);

                            // Add the mapping for the readme
                            StringBuilder mapping = new StringBuilder();
                            mapping.append("Value mapping [");

                            for (OptionsValueDescriptionsListDTO valueMap : property.valueList) {
                                mapping.append(String.format("\"%d\"=\"%s\",", valueMap.value, valueMap.description));
                            }
                            mapping.deleteCharAt(mapping.length() - 1);
                            mapping.append("]");
                            miIoBasicChannel.setReadmeComment(mapping.toString());
                        }
                        if (property.access.contains("write")) {
                            List<MiIoDeviceAction> miIoDeviceActions = new ArrayList<>();
                            MiIoDeviceAction action = new MiIoDeviceAction();
                            action.setCommand("set_properties");
                            switch (property.format) {
                                case "bool":
                                    action.setparameterType(CommandParameterType.ONOFFBOOL);
                                    break;
                                case "uint8":
                                case "int32":
                                case "float":
                                    action.setparameterType(CommandParameterType.NUMBER);
                                    break;
                                case "string":
                                    action.setparameterType(CommandParameterType.STRING);

                                    break;
                                default:
                                    action.setparameterType(CommandParameterType.STRING);
                                    break;
                            }
                            miIoDeviceActions.add(action);
                            miIoBasicChannel.setActions(miIoDeviceActions);
                        } else {
                            StateDescriptionDTO stateDescription = miIoBasicChannel.getStateDescription();
                            if (stateDescription == null) {
                                stateDescription = new StateDescriptionDTO();
                            }
                            stateDescription.setReadOnly(true);
                            miIoBasicChannel.setStateDescription(stateDescription);
                        }
                        miIoBasicChannels.add(miIoBasicChannel);
                    } else {
                        logger.info("No reading siid: {}, description: {}, piid: {},description: {}", service.siid,
                                service.description, property.piid, property.description);
                    }
                }
                if (service.actions != null) {
                    for (ActionDTO action : service.actions) {
                        deviceActions.put(action, service);
                        String actionId = action.type.substring(action.type.indexOf("action:")).split(":")[1];
                        actionText.append("`action{");
                        actionText.append(String.format("\"did\":\"%s-%s\",", serviceId, actionId));
                        actionText.append(String.format("\"siid\":%d,", service.siid));
                        actionText.append(String.format("\"aiid\":%d,", action.iid));
                        actionText.append(String.format("\"in\":%s", action.in));
                        actionText.append("}`\r\n");
                    }

                }
            } else {
                logger.info("SID: {}, description: {} has no identified properties", service.siid, service.description);
            }
        }
        if (!deviceActions.isEmpty()) {
            miIoBasicChannels.add(0, actionChannel(deviceActions));
        }
        deviceMapping.setChannels(miIoBasicChannels);
        device.setDevice(deviceMapping);
        logger.info(channelConfigText.toString());
        if (actionText.length() > 30) {
            logger.info("{}", actionText);
        } else {
            logger.info("No actions defined for device");
        }
        unknownUnits.remove("none");
        if (!unknownUnits.isEmpty()) {
            logger.info("New units identified (inform developer): {}", String.join(", ", unknownUnits));
        }

        this.device = device;
        return device;
    }

    private MiIoBasicChannel actionChannel(Map<ActionDTO, ServiceDTO> deviceActions) {
        MiIoBasicChannel miIoBasicChannel = new MiIoBasicChannel();
        if (!deviceActions.isEmpty()) {
            miIoBasicChannel.setProperty("");
            miIoBasicChannel.setChannel("actions");
            miIoBasicChannel.setFriendlyName("Actions");
            miIoBasicChannel.setType("String");
            miIoBasicChannel.setRefresh(false);
            StateDescriptionDTO stateDescription = new StateDescriptionDTO();
            List<OptionsValueListDTO> options = new LinkedList<>();
            List<MiIoDeviceAction> miIoDeviceActions = new LinkedList<>();
            deviceActions.forEach((action, service) -> {
                String actionId = action.type.substring(action.type.indexOf("action:")).split(":")[1];
                String serviceId = service.type.substring(service.type.indexOf("service:")).split(":")[1];
                String description = String.format("%s-%s", serviceId, actionId);
                OptionsValueListDTO option = new OptionsValueListDTO();
                option.label = captializedName(description);
                option.value = description;
                options.add(option);
                MiIoDeviceAction miIoDeviceAction = new MiIoDeviceAction();
                miIoDeviceAction.setCommand("action");
                miIoDeviceAction.setparameterType(CommandParameterType.EMPTY);
                miIoDeviceAction.setSiid(service.siid);
                miIoDeviceAction.setAiid(action.iid);
                if (!action.in.isEmpty()) {
                    miIoDeviceAction.setParameters(JsonParser.parseString(GSON.toJson(action.in)).getAsJsonArray());
                    miIoDeviceAction.setparameterType("fromparameter");
                }
                MiIoDeviceActionCondition miIoDeviceActionCondition = new MiIoDeviceActionCondition();
                String json = String.format("[{ \"matchValue\"=\"%s\"}]", description);
                miIoDeviceActionCondition.setName("matchValue");
                miIoDeviceActionCondition.setParameters(JsonParser.parseString(json).getAsJsonArray());
                miIoDeviceAction.setCondition(miIoDeviceActionCondition);
                miIoDeviceActions.add(miIoDeviceAction);
            });
            stateDescription.setOptions(options);
            miIoBasicChannel.setStateDescription(stateDescription);
            miIoBasicChannel.setActions(miIoDeviceActions);
        }
        return miIoBasicChannel;
    }

    private static String captializedName(String name) {
        if (name.isEmpty()) {
            return name;
        }
        String str = name.replace("-", " ").replace(".", " ");
        return Arrays.stream(str.split("\\s+")).map(t -> t.substring(0, 1).toUpperCase() + t.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static boolean isPureAscii(String v) {
        return StandardCharsets.US_ASCII.newEncoder().canEncode(v);
    }

    private JsonElement getUrnData(String urn, HttpClient httpClient)
            throws InterruptedException, TimeoutException, ExecutionException, JsonParseException {
        ContentResponse response;
        String urlStr = BASEURL + "instance?type=" + urn;
        logger.info("miot info: {}", urlStr);
        response = httpClient.newRequest(urlStr).timeout(15, TimeUnit.SECONDS).send();
        JsonElement json = JsonParser.parseString(response.getContentAsString());
        this.urnData = json;
        return json;
    }

    private @Nullable String getURN(String model, HttpClient httpClient) {
        ContentResponse response;
        try {
            response = httpClient.newRequest(BASEURL + "instances?status=released").timeout(15, TimeUnit.SECONDS)
                    .send();
            JsonElement json = JsonParser.parseString(response.getContentAsString());
            UrnsDTO data = GSON.fromJson(json, UrnsDTO.class);
            if (data == null) {
                return null;
            }
            for (ModelUrnsDTO device : data.getInstances()) {
                if (device.getModel().contentEquals(model)) {
                    this.urn = device.getType();
                    return device.getType();
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Failed downloading models: {}", e.getMessage());
        } catch (JsonParseException e) {
            logger.debug("Failed parsing downloading models: {}", e.getMessage());
        }
        return null;
    }

    public String getModel() {
        return model;
    }

    public @Nullable String getUrn() {
        return urn;
    }

    public @Nullable JsonElement getUrnData() {
        return urnData;
    }

    public @Nullable MiIoBasicDevice getDevice() {
        return device;
    }
}
