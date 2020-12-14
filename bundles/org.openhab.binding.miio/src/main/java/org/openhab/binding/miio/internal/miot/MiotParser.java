/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.WordUtils;
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
import org.openhab.binding.miio.internal.basic.OptionsValueListDTO;
import org.openhab.binding.miio.internal.basic.StateDescriptionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Support creation of the miot db files
 *
 * Run to make a miot database file
 *
 * Run in IDE with 'run as java application'
 * or run in command line as:
 * mvn exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.ReadmeHelper" -Dexec.classpathScope="test"
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiotParser {
    private final Logger logger = LoggerFactory.getLogger(MiotParser.class);

    private static final String BASEURL = "http://miot-spec.org/miot-spec-v2/";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser PARSER = new JsonParser();

    private static final HttpClient httpClient = new HttpClient();

    private String model;
    private @Nullable String urn;
    private @Nullable JsonElement urnData;
    private @Nullable MiIoBasicDevice device;

    public MiotParser(String model) {
        this.model = model;
    }

    public static MiotParser parse(String model) throws MiotParseException {
        MiotParser miotParser = new MiotParser(model);
        try {
            httpClient.setFollowRedirects(false);
            httpClient.start();
            String urn = miotParser.getURN(model);
            if (urn == null) {
                throw new MiotParseException("Device not found in in miot specs : " + model);
            }
            JsonElement urnData = miotParser.getUrnData(urn);
            miotParser.getDevice(urnData);
            httpClient.stop();
            return miotParser;
        } catch (Exception e) {
            throw new MiotParseException("Error parsing miot data: " + e.getMessage(), e);
        }
    }

    public void writeEmuDevice(String path, MiIoBasicDevice device) {
        JsonObject usersJson = new JsonObject();
        JsonArray properties = new JsonArray();

        for (MiIoBasicChannel ch : device.getDevice().getChannels()) {
            JsonObject prop = new JsonObject();
            prop.addProperty("property", ch.getProperty());
            switch (ch.getType()) {
                case "Dimmer":
                case "Number":
                    prop.addProperty("fakeresponse", (int) (Math.random() * 100));
                    break;
                case "Switch":
                    prop.addProperty("fakeresponse", "true");
                    break;
                default:
                    prop.addProperty("fakeresponse", "normal");
                    break;

            }
            prop.addProperty("datatype", ch.getType());
            properties.add(prop);
        }

        usersJson.add("properties", properties);
        try (

                PrintWriter out = new PrintWriter(path)) {
            out.println(usersJson);
            logger.info("Database file created:{}", path);
        } catch (FileNotFoundException e) {
            logger.info("Error writing file: {}", e.getMessage());
        }
    }

    public void writeDevice(String path, MiIoBasicDevice device) {
        String usersJson = GSON.toJson(device);
        try (PrintWriter out = new PrintWriter(path)) {
            usersJson = usersJson.replace("\n", "\r\n").replace("  ", "\t");
            out.println(usersJson);
            logger.info("Database file created:{}", path);
        } catch (FileNotFoundException e) {
            logger.info("Error writing file: {}", e.getMessage());
        }
    }

    public MiIoBasicDevice getDevice(JsonElement urnData) {
        Set<String> unknownUnits = new HashSet<>();
        StringBuilder channelConfigText = new StringBuilder("Suggested additional channelType \r\n");

        StringBuilder actionText = new StringBuilder("Manual actions for execution\r\n");

        MiIoBasicDevice device = new MiIoBasicDevice();
        DeviceMapping deviceMapping = new DeviceMapping();
        MiotDeviceDataDTO miotDevice = GSON.fromJson(urnData, MiotDeviceDataDTO.class);
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
                    if (property.access.contains("read")) {
                        MiIoBasicChannel miIoBasicChannel = new MiIoBasicChannel();
                        // miIoBasicChannel.setProperty(prop);
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
                        // avoid duplicates and make camel case and avoid wrong names
                        String chanId = propertyId.replace(" ", "");

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
                            // TODO: can I do something with this info?
                            if (!property.unit.contains("none")) {
                                miIoBasicChannel.setUnit(property.unit);
                            }
                        }
                        miIoBasicChannel.setProperty(propertyId);
                        miIoBasicChannel.setChannel(chanId);

                        // miIoBasicChannel.setChannelType("miot_" + property.format);

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
                                if (stateDescription == null) {
                                    stateDescription = new StateDescriptionDTO();
                                }
                                String type = MiIoQuantiyTypesConversion.getType(property.unit);
                                if (type != null) {
                                    miIoBasicChannel.setType("Number" + ":" + type);
                                    stateDescription.setPattern(
                                            "%." + (property.format.contentEquals("uint8") ? "0" : "1") + "f %unit%");
                                } else {
                                    miIoBasicChannel.setType("Number");

                                    stateDescription.setPattern(
                                            "%." + (property.format.contentEquals("uint8") ? "0" : "1") + "f");

                                    if (property.unit != null) {
                                        unknownUnits.add(property.unit);
                                    }
                                }
                                if (property.valueRange != null && property.valueRange.size() == 3) {
                                    stateDescription
                                            .setMinimum(BigDecimal.valueOf(property.valueRange.get(0).doubleValue()));
                                    stateDescription
                                            .setMaximum(BigDecimal.valueOf(property.valueRange.get(1).doubleValue()));
                                    stateDescription
                                            .setStep(BigDecimal.valueOf(property.valueRange.get(2).doubleValue()));
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
                        miIoBasicChannel.setRefresh(true);
                        // add option values
                        if (property.valueList != null && property.valueList.size() > 0) {
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
                            // Use custom channelType to support the properties mapping
                            // miIoBasicChannel.setChannelType(captializedName(model).replace(" ", "") + "_" + chanId);

                            // Add the mapping to the readme
                            StringBuilder mapping = new StringBuilder();
                            mapping.append("Value mapping [");

                            for (OptionsValueDescriptionsListDTO valueMap : property.valueList) {
                                mapping.append(String.format("%d=\"%s\",", valueMap.value, valueMap.description));
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
                        channelConfigText = printChannelDefinitions(channelConfigText, miIoBasicChannel, model,
                                property);
                    } else {
                        logger.info("No reading siid: {}, description: {}, piid: {},description: {}", service.siid,
                                service.description, property.piid, property.description);
                    }

                }
                if (service.actions != null) {
                    for (ActionDTO action : service.actions) {
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
            // TODO: Process actions
        }
        deviceMapping.setChannels(miIoBasicChannels);
        device.setDevice(deviceMapping);
        if (actionText.length() > 35) {
            deviceMapping.setReadmeComment(
                    "Identified " + actionText.toString().replace("Manual", "manual").replace("\r\n", "<br />")
                            + "Please test and feedback if they are working to they can be linked to a channel.");
        }
        logger.info(channelConfigText.toString());
        if (actionText.length() > 30) {
            logger.info(actionText.toString());
        } else {
            logger.info("No actions defined for device");
        }
        unknownUnits.remove("none");
        if (unknownUnits.size() > 0) {
            logger.info("New units identified (inform developer): {}", String.join(", ", unknownUnits));
        }

        this.device = device;
        return device;
    }

    private StringBuilder printChannelDefinitions(StringBuilder sb, MiIoBasicChannel miIoBasicChannel, String model,
            PropertyDTO property) {
        /*
         * <channel-type id="vacuumaction">
         * <item-type>Number</item-type>
         * <label>Vacuum Action</label>
         * <state>
         * <options>
         * <option value="0">Stop</option>
         * <option value="1">Vacuum</option>
         * <option value="2">Pause</option>
         * </options>
         * </state>
         * </channel-type>
         */
        if (property.valueList != null && property.valueList.size() > 0
                || property.valueRange != null && property.valueRange.size() > 0) {
            sb.append(String.format("<channel-type id=\"%s_%s\">\r\n", captializedName(model).replace(" ", ""),
                    miIoBasicChannel.getChannel()));
            sb.append(String.format("<item-type>%s</item-type>\r\n", miIoBasicChannel.getType()));
            sb.append(String.format("<label>%s</label>\r\n", miIoBasicChannel.getFriendlyName()));

            sb.append("<state");
            if (!property.access.contains("write")) {
                sb.append(" readOnly=\"true\"");
            }
            if (property.valueRange != null) {
                if (property.valueRange.size() < 3) {
                    logger.warn("unknown range - CHECK! ");
                }
                sb.append(String.format(" min=\"%s\"", property.valueRange.get(0)));
                sb.append(String.format(" max=\"%s\"", property.valueRange.get(1)));
                sb.append(String.format(" step=\"%s\"", property.valueRange.get(2)));
            }
            sb.append(">\r\n");
            if (property.valueList != null && property.valueList.size() > 0) {
                sb.append("<options>\r\n");
                for (OptionsValueDescriptionsListDTO valueMap : property.valueList) {
                    sb.append(String.format("<option value=\"%d\">%s</option>\r\n", valueMap.value,
                            valueMap.description));
                }
                sb.append("</options>\r\n");
            }
            sb.append("</state>\r\n");
            sb.append("</channel-type>\r\n");
        }
        return sb;
    }

    private static String captializedName(String name) {
        return WordUtils.capitalizeFully(name.replace("-", " ").replace(".", " "));
    }

    public static boolean isPureAscii(String v) {
        return StandardCharsets.US_ASCII.newEncoder().canEncode(v);
    }

    private JsonElement getUrnData(String urn)
            throws InterruptedException, TimeoutException, ExecutionException, JsonParseException {
        ContentResponse response;
        String urlStr = BASEURL + "instance?type=" + urn;
        logger.info("miot info: {}", urlStr);
        response = httpClient.newRequest(urlStr).timeout(15, TimeUnit.SECONDS).send();
        JsonElement json = PARSER.parse(response.getContentAsString());
        this.urnData = json;
        return json;
    }

    private @Nullable String getURN(String model) {
        ContentResponse response;
        try {
            response = httpClient.newRequest(BASEURL + "instances?status=released").timeout(15, TimeUnit.SECONDS)
                    .send();
            JsonElement json = PARSER.parse(response.getContentAsString());
            urns data = GSON.fromJson(json, urns.class);
            for (ModelUrns devices : data.getInstances()) {
                if (devices.model.contentEquals(model)) {
                    this.urn = devices.type;
                    return devices.type;
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Failed downloading models: {}", e.getMessage());
        } catch (JsonParseException e) {
            logger.debug("Failed parsing downloading models: {}", e.getMessage());
        }

        return null;
    }

    public class urns {
        @SerializedName("instances")
        @Expose
        private List<ModelUrns> instances = Collections.emptyList();

        public List<ModelUrns> getInstances() {
            return instances;
        }
    }

    public class ModelUrns {
        @SerializedName("model")
        @Expose
        private String model = "";
        @SerializedName("version")
        @Expose
        private Integer version = 0;
        @SerializedName("type")
        @Expose
        private String type = "";
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
