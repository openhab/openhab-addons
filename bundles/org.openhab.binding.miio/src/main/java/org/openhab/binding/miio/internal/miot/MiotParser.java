/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.Reader;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpStatus;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

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
    private static final String VALUE_TOKEN = "$value$";
    private static final Pattern LEADING_SPACES = Pattern.compile("(?m)^((?: {2})+)");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s\\p{Z}\\p{Cc}]+");
    private static final Pattern TRAILING_ZERO = Pattern.compile("(\\d)\\.0(,?\n)");

    private String model;
    private @Nullable String urn;
    private @Nullable JsonElement urnData;
    private @Nullable MiIoBasicDevice device;

    public MiotParser(String model) {
        this.model = model;
    }

    /**
     * Creates the device from the newest spec of the model: the highest released version, or if the model has no
     * released spec the highest preview version, or else the highest debug version.
     */
    public static MiotParser parse(String model, HttpClient httpClient) throws MiotParseException {
        return parse(model, null, httpClient);
    }

    /**
     * Creates the device from the spec of the model.
     *
     * @param version the spec version to use (any status), or null for the newest spec
     */
    public static MiotParser parse(String model, @Nullable Integer version, HttpClient httpClient)
            throws MiotParseException {
        MiotParser miotParser = new MiotParser(model);
        try {
            String urn = miotParser.getURN(model, version, httpClient);
            if (urn == null) {
                throw new MiotParseException("Device not found in in miot specs : " + model);
            }
            JsonElement urnData = miotParser.getUrnData(urn, httpClient);
            miotParser.getDevice(urnData);
            return miotParser;
        } catch (InterruptedException | InterruptedIOException e) {
            Thread.currentThread().interrupt();
            throw new MiotParseException("Interrupted while downloading miot data", e);
        } catch (JsonIOException e) {
            // Gson wraps the exception of an interrupted read of the streamed list of specs
            if (e.getCause() instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            throw new MiotParseException("Error parsing miot data: " + e.getMessage(), e);
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
        usersJson = TRAILING_ZERO.matcher(usersJson).replaceAll("$1$2");
        // only the indentation is converted, double spaces inside texts must stay as they are
        usersJson = LEADING_SPACES.matcher(usersJson).replaceAll(m -> "\t".repeat(m.group(1).length() / 2));
        return usersJson.replace("\n", "\r\n");
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
                        miIoBasicChannel.setFriendlyName(displayName(service.description, serviceId) + " - "
                                + displayName(property.description, propertyId));
                        miIoBasicChannel.setSiid(service.siid);
                        miIoBasicChannel.setPiid(property.piid);
                        String chanId = channelId(propertyId);
                        int cnt = reserveChannelId(propCheck, chanId);
                        if (cnt > 0) {
                            chanId = chanId.concat(Integer.toString(cnt));
                            propertyId = propertyId.concat(Integer.toString(cnt));
                            logger.warn("duplicate for property:{} - {} ({}", chanId, property.description, cnt);
                        }
                        miIoBasicChannel.setProperty(propertyId);
                        miIoBasicChannel.setChannel(chanId);
                        applyPropertyFormat(miIoBasicChannel, property, unknownUnits);
                        miIoBasicChannel.setRefresh(property.access.contains("read"));
                        if (property.access.contains("write")) {
                            List<MiIoDeviceAction> miIoDeviceActions = new ArrayList<>();
                            MiIoDeviceAction action = new MiIoDeviceAction();
                            action.setCommand("set_properties");
                            switch (property.format != null ? property.format : "") {
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
            } else {
                logger.info("SID: {}, description: {} has no identified properties", service.siid, service.description);
            }
            if (service.actions != null) {
                for (ActionDTO action : service.actions) {
                    String actionId = actionId(action);
                    MiIoBasicChannel inputChannel = actionInputChannel(service, serviceId, action, actionId, propCheck,
                            unknownUnits);
                    if (inputChannel != null) {
                        miIoBasicChannels.add(inputChannel);
                    } else {
                        deviceActions.put(action, service);
                    }
                    actionText.append("`action{");
                    actionText.append(String.format("\"did\":\"%s-%s\",", serviceId, actionId));
                    actionText.append(String.format("\"siid\":%d,", service.siid));
                    actionText.append(String.format("\"aiid\":%d,", action.iid));
                    actionText.append(String.format("\"in\":%s", inputs(action)));
                    actionText.append("}`\r\n");
                }
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

    /**
     * Sets the unit, channel type, state description and value mapping of a channel from the format of a MIoT
     * property.
     */
    private void applyPropertyFormat(MiIoBasicChannel miIoBasicChannel, PropertyDTO property,
            Set<String> unknownUnits) {
        if (property.unit != null && !property.unit.isBlank()) {
            if (!property.unit.contains("none")) {
                miIoBasicChannel.setUnit(property.unit);
            }
        }

        String format = property.format != null ? property.format : "";
        switch (format) {
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
                    decimals = format.contentEquals("float") ? 1 : 0;

                } else {
                    miIoBasicChannel.setType("Number");
                    decimals = format.contentEquals("uint8") ? 0 : 1;
                    if (property.unit != null) {
                        unknownUnits.add(property.unit);
                    }
                }
                if (property.valueRange != null && property.valueRange.size() == 3) {
                    stateDescription.setMinimum(property.valueRange.get(0));
                    stateDescription.setMaximum(property.valueRange.get(1));

                    BigDecimal step = property.valueRange.get(2);
                    if (step.signum() != 0) {
                        stateDescription.setStep(step);
                        if (step.compareTo(BigDecimal.ONE) >= 0) {
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
                logger.info("no type mapping implemented for {}", format);
                break;
            default:
                miIoBasicChannel.setType("String");
                logger.info("no type mapping for {}", property.format);
                break;
        }

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
                basicOption.setLabel(cleanText(miotOption.getDescription()));
                basicOption.setValue(String.valueOf(miotOption.value));
                channeloptions.add(basicOption);
            }
            stateDescription.setOptions(channeloptions);
            miIoBasicChannel.setStateDescription(stateDescription);

            // Add the mapping for the readme
            StringBuilder mapping = new StringBuilder();
            mapping.append("Value mapping [");

            for (OptionsValueDescriptionsListDTO valueMap : property.valueList) {
                mapping.append(String.format("\"%d\"=\"%s\",", valueMap.value, cleanText(valueMap.getDescription())));
            }
            mapping.deleteCharAt(mapping.length() - 1);
            mapping.append("]");
            miIoBasicChannel.setReadmeComment(mapping.toString());
        }
    }

    /**
     * Creates a channel for an action with exactly one input. The command value of the channel is sent as that input,
     * with the {@code $value$} token in the parameters of the action. An action with several inputs can not be filled
     * from one channel value, so it is not handled here and stays on the actions channel.
     *
     * @return the channel, or null if the action input can not be provided by one channel
     */
    private @Nullable MiIoBasicChannel actionInputChannel(ServiceDTO service, String serviceId, ActionDTO action,
            String actionId, Set<String> propCheck, Set<String> unknownUnits) {
        List<Object> inputs = inputs(action);
        if (inputs.size() != 1 || !(inputs.get(0) instanceof Number)) {
            return null;
        }
        int piid = ((Number) inputs.get(0)).intValue();
        List<PropertyDTO> properties = service.properties;
        PropertyDTO input = null;
        if (properties != null) {
            input = properties.stream().filter(property -> property.piid != null && property.piid == piid).findFirst()
                    .orElse(null);
        }
        if (input == null) {
            logger.info("Input piid {} of action {} not found in the properties of service {}", piid, actionId,
                    service.siid);
            return null;
        }
        String chanId = channelId(serviceId + "_" + actionId);
        int cnt = reserveChannelId(propCheck, chanId);
        if (cnt > 0) {
            chanId = chanId.concat(Integer.toString(cnt));
            logger.warn("duplicate for action:{} ({})", chanId, cnt);
        }
        MiIoBasicChannel miIoBasicChannel = new MiIoBasicChannel();
        miIoBasicChannel.setFriendlyName(
                displayName(service.description, serviceId) + " - " + displayName(action.description, actionId));
        miIoBasicChannel.setChannel(chanId);
        miIoBasicChannel.setProperty("");
        miIoBasicChannel.setRefresh(false);
        applyPropertyFormat(miIoBasicChannel, input, unknownUnits);

        MiIoDeviceAction miIoDeviceAction = new MiIoDeviceAction();
        miIoDeviceAction.setCommand("action");
        miIoDeviceAction.setSiid(service.siid);
        miIoDeviceAction.setAiid(action.iid);
        String channelType = miIoBasicChannel.getType();
        if (channelType.startsWith("Number")) {
            miIoDeviceAction.setparameterType(CommandParameterType.NUMBER);
        } else if ("Switch".equals(channelType)) {
            miIoDeviceAction.setparameterType(CommandParameterType.ONOFFBOOL);
        } else {
            miIoDeviceAction.setparameterType(CommandParameterType.STRING);
        }
        JsonObject inputParameter = new JsonObject();
        inputParameter.addProperty("piid", piid);
        inputParameter.addProperty("value", VALUE_TOKEN);
        JsonArray parameters = new JsonArray();
        parameters.add(inputParameter);
        miIoDeviceAction.setParameters(parameters);
        List<MiIoDeviceAction> miIoDeviceActions = new ArrayList<>();
        miIoDeviceActions.add(miIoDeviceAction);
        miIoBasicChannel.setActions(miIoDeviceActions);
        return miIoBasicChannel;
    }

    private static String actionId(ActionDTO action) {
        return action.type.substring(action.type.indexOf("action:")).split(":")[1];
    }

    /**
     * @return the inputs of the action, an action without inputs in the spec has none
     */
    private static List<Object> inputs(ActionDTO action) {
        List<Object> inputs = action.in;
        return inputs != null ? inputs : List.of();
    }

    /**
     * Removes the characters that are not valid in a channel name.
     */
    private static String channelId(String id) {
        return id.replace(" ", "").replace(".", "_").replace("-", "_");
    }

    /**
     * Registers the channel id, to avoid duplicate channel names.
     *
     * @return the number to append to the channel id to make it unique, 0 if it is unique already
     */
    private static int reserveChannelId(Set<String> propCheck, String chanId) {
        int cnt = 0;
        while (propCheck.contains(chanId + Integer.toString(cnt))) {
            cnt++;
        }
        propCheck.add(chanId.concat(Integer.toString(cnt)));
        return cnt;
    }

    private static String displayName(@Nullable String description, String id) {
        String text = description == null ? "" : cleanText(description);
        return !text.isEmpty() && isPureAscii(text) ? captializedName(text) : captializedName(id);
    }

    /**
     * Spec descriptions are display texts that end up in labels, the readme and translation files. They can have
     * leading, trailing or repeated spaces, so every run of whitespace or control characters (also line breaks and
     * non-breaking spaces) becomes one space.
     */
    private static String cleanText(String text) {
        return WHITESPACE.matcher(text).replaceAll(" ").strip();
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
            Set<String> actionCheck = new HashSet<>();
            deviceActions.forEach((action, service) -> {
                String actionId = actionId(action);
                String serviceId = service.type.substring(service.type.indexOf("service:")).split(":")[1];
                String description = String.format("%s-%s", serviceId, actionId);
                String label = captializedName(description);
                // services of the same type (e.g. main and side brush) share action names, while the action value
                // must be unique as every action matching the selected value is executed
                int cnt = 0;
                String candidate = description;
                while (!actionCheck.add(candidate)) {
                    candidate = description + ++cnt;
                }
                if (cnt > 0) {
                    description = candidate;
                    label = label + " " + cnt;
                    logger.warn("duplicate for action:{} ({})", description, cnt);
                }
                OptionsValueListDTO option = new OptionsValueListDTO();
                option.label = label;
                option.value = description;
                options.add(option);
                MiIoDeviceAction miIoDeviceAction = new MiIoDeviceAction();
                miIoDeviceAction.setCommand("action");
                miIoDeviceAction.setparameterType(CommandParameterType.EMPTY);
                miIoDeviceAction.setSiid(service.siid);
                miIoDeviceAction.setAiid(action.iid);
                List<Object> inputs = inputs(action);
                if (!inputs.isEmpty()) {
                    miIoDeviceAction.setParameters(JsonParser.parseString(GSON.toJson(inputs)).getAsJsonArray());
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
            throws InterruptedException, TimeoutException, ExecutionException, JsonParseException, MiotParseException {
        ContentResponse response;
        String urlStr = BASEURL + "instance?type=" + urn;
        logger.info("miot info: {}", urlStr);
        response = httpClient.newRequest(urlStr).timeout(15, TimeUnit.SECONDS).send();
        if (response.getStatus() != HttpStatus.OK_200) {
            throw new MiotParseException("Http status " + response.getStatus() + " for " + urlStr);
        }
        JsonElement json = JsonParser.parseString(response.getContentAsString());
        this.urnData = json;
        return json;
    }

    /**
     * @return the urn of the spec, or null if the list of specs has no (such) spec for the model
     */
    private @Nullable String getURN(String model, @Nullable Integer version, HttpClient httpClient)
            throws InterruptedException, TimeoutException, ExecutionException, IOException, MiotParseException {
        ModelUrnsDTO instance = findInstance(model, version, httpClient);
        if (instance == null) {
            return null;
        }
        logger.info("miot spec for {}: version {}, status {}, urn {}", model, instance.getVersion(),
                instance.getStatus(), instance.getType());
        this.urn = instance.getType();
        return instance.getType();
    }

    /**
     * Searches the list of all specs (released, preview and debug) for the model. The list has several megabytes, more
     * than the client buffers for a response, so it is read as a stream.
     */
    private @Nullable ModelUrnsDTO findInstance(String model, @Nullable Integer version, HttpClient httpClient)
            throws InterruptedException, TimeoutException, ExecutionException, IOException, MiotParseException {
        String urlStr = BASEURL + "instances?status=all";
        InputStreamResponseListener listener = new InputStreamResponseListener();
        Request request = httpClient.newRequest(urlStr).timeout(60, TimeUnit.SECONDS);
        request.send(listener);
        try (InputStream stream = listener.getInputStream();
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Response response = listener.get(15, TimeUnit.SECONDS);
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new MiotParseException("Http status " + response.getStatus() + " for " + urlStr);
            }
            return selectInstance(reader, model, version);
        } catch (Exception e) {
            // closing the stream does not end the request, it would keep its connection until the request timeout
            request.abort(e);
            throw e;
        }
    }

    /**
     * Selects the spec of the model from the json list of specs. Entries without version or urn are ignored.
     *
     * @param version the version to select, or null for the newest spec: a released spec is preferred over a preview
     *            and a preview over a debug spec, of those the highest version is selected
     * @return the spec, or null if the list has no (such) spec for the model
     */
    static @Nullable ModelUrnsDTO selectInstance(Reader json, String model, @Nullable Integer version)
            throws IOException {
        ModelUrnsDTO selected = null;
        int selectedRank = -1;
        int selectedVersion = -1;
        JsonReader reader = new JsonReader(json);
        reader.beginObject();
        while (reader.hasNext()) {
            if (!"instances".equals(reader.nextName())) {
                reader.skipValue();
                continue;
            }
            reader.beginArray();
            while (reader.hasNext()) {
                ModelUrnsDTO instance = GSON.fromJson(reader, ModelUrnsDTO.class);
                if (instance == null || !model.equals(instance.getModel())) {
                    continue;
                }
                Integer instanceVersion = instance.getVersion();
                if (instanceVersion == null || instance.getType() == null) {
                    continue;
                }
                if (version != null) {
                    if (version.equals(instanceVersion)) {
                        selected = instance;
                    }
                    continue;
                }
                int rank = statusRank(instance.getStatus());
                if (rank > selectedRank || (rank == selectedRank && instanceVersion > selectedVersion)) {
                    selected = instance;
                    selectedRank = rank;
                    selectedVersion = instanceVersion;
                }
            }
            reader.endArray();
        }
        reader.endObject();
        return selected;
    }

    private static int statusRank(@Nullable String status) {
        if (status == null) {
            return 0;
        }
        return switch (status) {
            case "released" -> 3;
            case "preview" -> 2;
            case "debug" -> 1;
            default -> 0;
        };
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
