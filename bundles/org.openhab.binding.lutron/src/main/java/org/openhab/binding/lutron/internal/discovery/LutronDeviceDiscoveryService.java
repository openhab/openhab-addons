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
package org.openhab.binding.lutron.internal.discovery;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.lutron.internal.LutronHandlerFactory;
import org.openhab.binding.lutron.internal.discovery.project.Area;
import org.openhab.binding.lutron.internal.discovery.project.Component;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;
import org.openhab.binding.lutron.internal.discovery.project.Device;
import org.openhab.binding.lutron.internal.discovery.project.DeviceGroup;
import org.openhab.binding.lutron.internal.discovery.project.DeviceNode;
import org.openhab.binding.lutron.internal.discovery.project.DeviceType;
import org.openhab.binding.lutron.internal.discovery.project.GreenMode;
import org.openhab.binding.lutron.internal.discovery.project.Output;
import org.openhab.binding.lutron.internal.discovery.project.OutputType;
import org.openhab.binding.lutron.internal.discovery.project.Project;
import org.openhab.binding.lutron.internal.discovery.project.Timeclock;
import org.openhab.binding.lutron.internal.handler.IPBridgeHandler;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfig;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigGrafikEye;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigIntlSeetouch;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigPalladiom;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigPico;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigSeetouch;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigTabletopSeetouch;
import org.openhab.binding.lutron.internal.xml.DbXmlInfoReader;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LutronDeviceDiscoveryService} finds all devices paired with Lutron bridges by retrieving the
 * configuration XML from them via HTTP.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Added support for more output devices and keypads, VCRX, repeater virtual buttons,
 *         Timeclock, and Green Mode. Added option to read XML from file. Switched to jetty HTTP client for better
 *         exception handling. Added keypad model discovery.
 */
@NonNullByDefault
public class LutronDeviceDiscoveryService extends AbstractDiscoveryService {

    private static final int DECLARATION_MAX_LEN = 80;
    private static final long HTTP_REQUEST_TIMEOUT = 60; // seconds
    private static final int DISCOVERY_SERVICE_TIMEOUT = 90; // seconds

    private static final String XML_DECLARATION_START = "<?xml";
    private static final Pattern XML_DECLARATION_PATTERN = Pattern.compile(XML_DECLARATION_START,
            Pattern.LITERAL | Pattern.CASE_INSENSITIVE);

    private final Logger logger = LoggerFactory.getLogger(LutronDeviceDiscoveryService.class);

    private final IPBridgeHandler bridgeHandler;
    private DbXmlInfoReader dbXmlInfoReader = new DbXmlInfoReader();

    private final HttpClient httpClient;

    private @Nullable Future<?> scanTask;

    public LutronDeviceDiscoveryService(IPBridgeHandler bridgeHandler, HttpClient httpClient)
            throws IllegalArgumentException {
        super(LutronHandlerFactory.DISCOVERABLE_DEVICE_TYPES_UIDS, DISCOVERY_SERVICE_TIMEOUT);

        this.bridgeHandler = bridgeHandler;
        this.httpClient = httpClient;
    }

    @Override
    protected synchronized void startScan() {
        Future<?> scanTask = this.scanTask;
        if (scanTask == null || scanTask.isDone()) {
            this.scanTask = scheduler.submit(this::asyncDiscoveryTask);
        }
    }

    private synchronized void asyncDiscoveryTask() {
        try {
            readDeviceDatabase();
        } catch (RuntimeException e) {
            logger.warn("Runtime exception scanning for devices: {}", e.getMessage(), e);

            if (scanListener != null) {
                scanListener.onErrorOccurred(null); // null so it won't log a stack trace
            }
        }
    }

    private void readDeviceDatabase() {
        Project project = null;

        if (bridgeHandler.getIPBridgeConfig() == null) {
            logger.debug("Unable to get bridge config. Exiting.");
            return;
        }
        String discFileName = bridgeHandler.getIPBridgeConfig().discoveryFile;
        String address = "http://" + bridgeHandler.getIPBridgeConfig().ipAddress + "/DbXmlInfo.xml";

        if (discFileName == null || discFileName.isEmpty()) {
            // Read XML from bridge via HTTP
            logger.trace("Sending http request for {}", address);
            InputStreamResponseListener listener = new InputStreamResponseListener();
            Response response = null;

            // Use response stream instead of doing it the simple synchronous way because the response can be very large
            httpClient.newRequest(address).method(HttpMethod.GET).timeout(HTTP_REQUEST_TIMEOUT, TimeUnit.SECONDS)
                    .header(HttpHeader.ACCEPT, "text/html").header(HttpHeader.ACCEPT_CHARSET, "utf-8").send(listener);

            try {
                response = listener.get(HTTP_REQUEST_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.info("Exception getting HTTP response: {}", e.getMessage());
            }

            if (response != null && response.getStatus() == HttpStatus.OK_200) {
                logger.trace("Received good http response.");

                try (InputStream responseStream = listener.getInputStream();
                        InputStreamReader xmlStreamReader = new InputStreamReader(responseStream,
                                StandardCharsets.UTF_8);
                        BufferedReader xmlBufReader = new BufferedReader(xmlStreamReader)) {
                    flushPrePrologLines(xmlBufReader);

                    project = dbXmlInfoReader.readFromXML(xmlBufReader);
                    if (project == null) {
                        logger.info("Failed to parse XML project file from {}", address);
                    }
                } catch (IOException e) {
                    logger.info("IOException while processing XML project file: {}", e.getMessage());
                }
            } else {
                if (response != null) {
                    logger.info("Received HTTP error response: {} {}", response.getStatus(), response.getReason());
                } else {
                    logger.info("No response for HTTP request.");
                }
            }
        } else {
            // Read XML from file
            File xmlFile = new File(discFileName);

            try (BufferedReader xmlReader = Files.newBufferedReader(xmlFile.toPath(), StandardCharsets.UTF_8)) {
                flushPrePrologLines(xmlReader);

                project = dbXmlInfoReader.readFromXML(xmlReader);
                if (project == null) {
                    logger.info("Could not process XML project file {}", discFileName);
                }
            } catch (IOException | SecurityException e) {
                logger.info("Exception reading XML project file {} : {}", discFileName, e.getMessage());
            }
        }

        if (project != null) {
            Stack<String> locationContext = new Stack<>();

            for (Area area : project.getAreas()) {
                processArea(area, locationContext);
            }
            for (Timeclock timeclock : project.getTimeclocks()) {
                processTimeclocks(timeclock, locationContext);
            }
            for (GreenMode greenMode : project.getGreenModes()) {
                processGreenModes(greenMode, locationContext);
            }
        }
    }

    /**
     * Flushes any lines or characters before the start of the XML declaration in the supplied BufferedReader.
     *
     * @param xmlReader BufferedReader source of the XML document
     * @throws IOException
     */
    private void flushPrePrologLines(BufferedReader xmlReader) throws IOException {
        String inLine = null;
        xmlReader.mark(DECLARATION_MAX_LEN);
        boolean foundXmlDec = false;

        while (!foundXmlDec && (inLine = xmlReader.readLine()) != null) {
            Matcher matcher = XML_DECLARATION_PATTERN.matcher(inLine);
            if (matcher.find()) {
                foundXmlDec = true;
                xmlReader.reset();
                if (matcher.start() > 0) {
                    logger.trace("Discarding {} characters.", matcher.start());
                    xmlReader.skip(matcher.start());
                }
            } else {
                logger.trace("Discarding line: {}", inLine);
                xmlReader.mark(DECLARATION_MAX_LEN);
            }
        }
    }

    private void processArea(Area area, Stack<String> context) {
        context.push(area.getName());

        for (DeviceNode deviceNode : area.getDeviceNodes()) {
            if (deviceNode instanceof DeviceGroup group) {
                processDeviceGroup(area, group, context);
            } else if (deviceNode instanceof Device device) {
                processDevice(area, device, context);
            }
        }

        for (Output output : area.getOutputs()) {
            processOutput(output, context);
        }

        for (Area subarea : area.getAreas()) {
            processArea(subarea, context);
        }

        context.pop();
    }

    private void processDeviceGroup(Area area, DeviceGroup deviceGroup, Stack<String> context) {
        context.push(deviceGroup.getName());

        for (Device device : deviceGroup.getDevices()) {
            processDevice(area, device, context);
        }

        context.pop();
    }

    private void processDevice(Area area, Device device, Stack<String> context) {
        List<Integer> buttons;
        KeypadConfig kpConfig;
        String kpModel;

        DeviceType type = device.getDeviceType();

        if (type != null) {
            String label = generateLabel(context, device.getName());

            switch (type) {
                case MOTION_SENSOR:
                    notifyDiscovery(THING_TYPE_OCCUPANCYSENSOR, device.getIntegrationId(), label);
                    notifyDiscovery(THING_TYPE_OGROUP, area.getIntegrationId(), area.getName());
                    break;

                case SEETOUCH_KEYPAD:
                case HYBRID_SEETOUCH_KEYPAD:
                    kpConfig = new KeypadConfigSeetouch();
                    discoverKeypad(device, label, THING_TYPE_KEYPAD, "seeTouch Keypad", kpConfig);
                    break;

                case INTERNATIONAL_SEETOUCH_KEYPAD:
                    kpConfig = new KeypadConfigIntlSeetouch();
                    discoverKeypad(device, label, THING_TYPE_INTLKEYPAD, "International seeTouch Keypad", kpConfig);
                    break;

                case SEETOUCH_TABLETOP_KEYPAD:
                    kpConfig = new KeypadConfigTabletopSeetouch();
                    discoverKeypad(device, label, THING_TYPE_TTKEYPAD, "Tabletop seeTouch Keypad", kpConfig);
                    break;

                case PALLADIOM_KEYPAD:
                    kpConfig = new KeypadConfigPalladiom();
                    discoverKeypad(device, label, THING_TYPE_PALLADIOMKEYPAD, "Palladiom Keypad", kpConfig);
                    break;

                case PICO_KEYPAD:
                    kpConfig = new KeypadConfigPico();
                    discoverKeypad(device, label, THING_TYPE_PICO, "Pico Keypad", kpConfig);
                    break;

                case VISOR_CONTROL_RECEIVER:
                    notifyDiscovery(THING_TYPE_VCRX, device.getIntegrationId(), label);
                    break;

                case WCI:
                    notifyDiscovery(THING_TYPE_WCI, device.getIntegrationId(), label);
                    break;

                case MAIN_REPEATER:
                    notifyDiscovery(THING_TYPE_VIRTUALKEYPAD, device.getIntegrationId(), label);
                    break;

                case QS_IO_INTERFACE:
                    notifyDiscovery(THING_TYPE_QSIO, device.getIntegrationId(), label);
                    break;

                case GRAFIK_EYE_QS:
                    buttons = getComponentIdList(device.getComponents(), ComponentType.BUTTON);
                    // remove button IDs >= 300 which the handler does not recognize
                    List<Integer> buttonsCopy = new ArrayList<>(buttons);
                    for (Integer c : buttonsCopy) {
                        if (c >= 300) {
                            buttons.remove(Integer.valueOf(c));
                        }
                    }
                    kpConfig = new KeypadConfigGrafikEye();
                    kpModel = kpConfig.determineModelFromComponentIds(buttons);
                    if (kpModel == null) {
                        logger.info("Unable to determine model of GrafikEye Keypad {} with button IDs: {}",
                                device.getIntegrationId(), buttons);
                        notifyDiscovery(THING_TYPE_GRAFIKEYEKEYPAD, device.getIntegrationId(), label);
                    } else {
                        logger.debug("Found GrafikEye keypad {} model: {}", device.getIntegrationId(), kpModel);
                        notifyDiscovery(THING_TYPE_GRAFIKEYEKEYPAD, device.getIntegrationId(), label, "model", kpModel);
                    }
                    break;
            }
        } else {
            logger.warn("Unrecognized device type {}", device.getType());
        }
    }

    private void discoverKeypad(Device device, String label, ThingTypeUID ttUid, String description,
            KeypadConfig kpConfig) {
        List<Integer> buttons = getComponentIdList(device.getComponents(), ComponentType.BUTTON);
        String kpModel = kpConfig.determineModelFromComponentIds(buttons);
        if (kpModel == null) {
            logger.info("Unable to determine model of {} {} with button IDs: {}", description,
                    device.getIntegrationId(), buttons);
            notifyDiscovery(ttUid, device.getIntegrationId(), label);
        } else {
            logger.debug("Found {} {} model: {}", description, device.getIntegrationId(), kpModel);
            notifyDiscovery(ttUid, device.getIntegrationId(), label, "model", kpModel);
        }
    }

    private List<Integer> getComponentIdList(List<Component> clist, ComponentType ctype) {
        List<Integer> returnList = new LinkedList<>();
        for (Component c : clist) {
            if (c.getComponentType() == ctype) {
                returnList.add(c.getComponentNumber());
            }
        }
        return returnList;
    }

    private void processOutput(Output output, Stack<String> context) {
        OutputType type = output.getOutputType();

        if (type != null) {
            String label = generateLabel(context, output.getName());

            switch (type) {
                case INC:
                case MLV:
                case ELV:
                case DALI:
                case ECO_SYSTEM_FLUORESCENT:
                case FLUORESCENT_DB:
                case ZERO_TO_TEN:
                case AUTO_DETECT:
                    notifyDiscovery(THING_TYPE_DIMMER, output.getIntegrationId(), label);
                    break;

                case CEILING_FAN_TYPE:
                    notifyDiscovery(THING_TYPE_FAN, output.getIntegrationId(), label);
                    break;

                case NON_DIM:
                case NON_DIM_INC:
                case NON_DIM_ELV:
                case RELAY_LIGHTING:
                    notifyDiscovery(THING_TYPE_SWITCH, output.getIntegrationId(), label);
                    break;

                case CCO_PULSED:
                    notifyDiscovery(THING_TYPE_CCO, output.getIntegrationId(), label, CCO_TYPE, CCO_TYPE_PULSED);
                    break;

                case CCO_MAINTAINED:
                    notifyDiscovery(THING_TYPE_CCO, output.getIntegrationId(), label, CCO_TYPE, CCO_TYPE_MAINTAINED);
                    break;

                case SYSTEM_SHADE:
                case MOTOR:
                    notifyDiscovery(THING_TYPE_SHADE, output.getIntegrationId(), label);
                    break;

                case SHEER_BLIND:
                    notifyDiscovery(THING_TYPE_BLIND, output.getIntegrationId(), label, BLIND_TYPE_PARAMETER,
                            BLIND_TYPE_SHEER);
                    break;

                case VENETIAN_BLIND:
                    notifyDiscovery(THING_TYPE_BLIND, output.getIntegrationId(), label, BLIND_TYPE_PARAMETER,
                            BLIND_TYPE_VENETIAN);
                    break;
            }
        } else {
            logger.warn("Unrecognized output type {}", output.getType());
        }
    }

    private void processTimeclocks(Timeclock timeclock, Stack<String> context) {
        String label = generateLabel(context, timeclock.getName());
        notifyDiscovery(THING_TYPE_TIMECLOCK, timeclock.getIntegrationId(), label);
    }

    private void processGreenModes(GreenMode greenmode, Stack<String> context) {
        String label = generateLabel(context, greenmode.getName());
        notifyDiscovery(THING_TYPE_GREENMODE, greenmode.getIntegrationId(), label);
    }

    private void notifyDiscovery(ThingTypeUID thingTypeUID, @Nullable Integer integrationId, String label,
            @Nullable String propName, @Nullable Object propValue) {
        if (integrationId == null) {
            logger.info("Discovered {} with no integration ID", label);

            return;
        }

        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(thingTypeUID, bridgeUID, integrationId.toString());

        Map<String, Object> properties = new HashMap<>();

        properties.put(INTEGRATION_ID, integrationId);

        if (propName != null && propValue != null) {
            properties.put(propName, propValue);
        }

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(label)
                .withProperties(properties).withRepresentationProperty(INTEGRATION_ID).build();

        thingDiscovered(result);

        logger.debug("Discovered {}", uid);
    }

    private void notifyDiscovery(ThingTypeUID thingTypeUID, Integer integrationId, String label) {
        notifyDiscovery(thingTypeUID, integrationId, label, null, null);
    }

    private String generateLabel(Stack<String> context, String deviceName) {
        return String.join(" ", context) + " " + deviceName;
    }
}
