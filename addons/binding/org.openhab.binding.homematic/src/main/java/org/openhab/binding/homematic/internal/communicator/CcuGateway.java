/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.client.UnknownParameterSetException;
import org.openhab.binding.homematic.internal.communicator.client.UnknownRpcFailureException;
import org.openhab.binding.homematic.internal.communicator.parser.CcuLoadDeviceNamesParser;
import org.openhab.binding.homematic.internal.communicator.parser.CcuParamsetDescriptionParser;
import org.openhab.binding.homematic.internal.communicator.parser.CcuValueParser;
import org.openhab.binding.homematic.internal.communicator.parser.CcuVariablesAndScriptsParser;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmResult;
import org.openhab.binding.homematic.internal.model.TclScript;
import org.openhab.binding.homematic.internal.model.TclScriptDataList;
import org.openhab.binding.homematic.internal.model.TclScriptList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * HomematicGateway implementation for a CCU.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class CcuGateway extends AbstractHomematicGateway {
    private final Logger logger = LoggerFactory.getLogger(CcuGateway.class);

    private Map<String, String> tclregaScripts;
    private HttpClient httpClient;
    private XStream xStream = new XStream(new StaxDriver());

    protected CcuGateway(String id, HomematicConfig config, HomematicGatewayAdapter gatewayAdapter) {
        super(id, config, gatewayAdapter);

        xStream.setClassLoader(CcuGateway.class.getClassLoader());
        xStream.autodetectAnnotations(true);
        xStream.alias("scripts", TclScriptList.class);
        xStream.alias("list", TclScriptDataList.class);
        xStream.alias("result", HmResult.class);
    }

    @Override
    protected void startClients() throws IOException {
        super.startClients();

        tclregaScripts = loadTclRegaScripts();

        httpClient = new HttpClient();
        httpClient.setConnectTimeout(config.getTimeout() * 1000L);
        try {
            httpClient.start();
        } catch (Exception ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }

    @Override
    protected void stopClients() {
        super.stopClients();
        tclregaScripts = null;
        if (httpClient != null) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                // ignore
            }
            httpClient = null;
        }
    }

    @Override
    protected void loadVariables(HmChannel channel) throws IOException {
        TclScriptDataList resultList = sendScriptByName("getAllVariables", TclScriptDataList.class);
        new CcuVariablesAndScriptsParser(channel).parse(resultList);
    }

    @Override
    protected void loadScripts(HmChannel channel) throws IOException {
        TclScriptDataList resultList = sendScriptByName("getAllPrograms", TclScriptDataList.class);
        new CcuVariablesAndScriptsParser(channel).parse(resultList);
    }

    @Override
    protected void loadDeviceNames(Collection<HmDevice> devices) throws IOException {
        TclScriptDataList resultList = sendScriptByName("getAllDeviceNames", TclScriptDataList.class);
        new CcuLoadDeviceNamesParser(devices).parse(resultList);
    }

    @Override
    protected void setChannelDatapointValues(HmChannel channel, HmParamsetType paramsetType) throws IOException {
        try {
            super.setChannelDatapointValues(channel, paramsetType);
        } catch (UnknownRpcFailureException ex) {
            logger.debug(
                    "RpcMessage unknown RPC failure (-1 Failure), fetching values with TclRega script for device {}, channel: {}, paramset: {}",
                    channel.getDevice().getAddress(), channel.getNumber(), paramsetType);

            Collection<String> dpNames = new ArrayList<String>();
            for (HmDatapoint dp : channel.getDatapoints().values()) {
                if (!dp.isVirtual() && dp.isReadable() && dp.getParamsetType() == HmParamsetType.VALUES) {
                    dpNames.add(dp.getName());
                }
            }
            if (dpNames.size() > 0) {
                HmDevice device = channel.getDevice();
                String channelName = String.format("%s.%s:%s.", device.getHmInterface().getName(), device.getAddress(),
                        channel.getNumber());
                String datapointNames = StringUtils.join(dpNames.toArray(), "\\t");
                TclScriptDataList resultList = sendScriptByName("getAllChannelValues", TclScriptDataList.class,
                        new String[] { "channel_name", "datapoint_names" },
                        new String[] { channelName, datapointNames });
                new CcuValueParser(channel).parse(resultList);
                channel.setInitialized(true);
            }
        }
    }

    @Override
    protected void addChannelDatapoints(HmChannel channel, HmParamsetType paramsetType) throws IOException {
        try {
            getRpcClient(channel.getDevice().getHmInterface()).addChannelDatapoints(channel, paramsetType);
        } catch (UnknownParameterSetException ex) {
            logger.debug(
                    "RpcMessage RPC failure (-3 Unknown paramset), fetching metadata with TclRega script for device: {}, channel: {}, paramset: {}",
                    channel.getDevice().getAddress(), channel.getNumber(), paramsetType);

            TclScriptDataList resultList = sendScriptByName("getParamsetDescription", TclScriptDataList.class,
                    new String[] { "device_address", "channel_number" },
                    new String[] { channel.getDevice().getAddress(), channel.getNumber().toString() });
            new CcuParamsetDescriptionParser(channel, paramsetType).parse(resultList);
        }
    }

    @Override
    protected void setVariable(HmDatapoint dp, Object value) throws IOException {
        String strValue = StringUtils.replace(ObjectUtils.toString(value), "\"", "\\\"");
        if (dp.isStringType()) {
            strValue = "\"" + strValue + "\"";
        }
        HmResult result = sendScriptByName("setVariable", HmResult.class,
                new String[] { "variable_name", "variable_state" }, new String[] { dp.getInfo(), strValue });
        if (!result.isValid()) {
            throw new IOException("Unable to set CCU variable " + dp.getInfo());
        }
    }

    @Override
    protected void executeScript(HmDatapoint dp) throws IOException {
        HmResult result = sendScriptByName("executeProgram", HmResult.class, new String[] { "program_name" },
                new String[] { dp.getInfo() });
        if (!result.isValid()) {
            throw new IOException("Unable to start CCU program: " + dp.getInfo());
        }
    }

    /**
     * Sends a TclRega script to the CCU.
     */
    private <T> T sendScriptByName(String scriptName, Class<T> clazz) throws IOException {
        return sendScriptByName(scriptName, clazz, new String[] {}, null);
    }

    /**
     * Sends a TclRega script with the specified variables to the CCU.
     */
    private <T> T sendScriptByName(String scriptName, Class<T> clazz, String[] variableNames, String[] values)
            throws IOException {
        String script = tclregaScripts.get(scriptName);
        for (int i = 0; i < variableNames.length; i++) {
            script = StringUtils.replace(script, "{" + variableNames[i] + "}", values[i]);
        }
        return sendScript(script, clazz);
    }

    /**
     * Main method for sending a TclRega script and parsing the XML result.
     */
    @SuppressWarnings("unchecked")
    private synchronized <T> T sendScript(String script, Class<T> clazz) throws IOException {
        try {
            script = StringUtils.trim(script);
            if (StringUtils.isEmpty(script)) {
                throw new RuntimeException("Homematic TclRegaScript is empty!");
            }
            if (logger.isTraceEnabled()) {
                logger.trace("TclRegaScript: {}", script);
            }

            StringContentProvider content = new StringContentProvider(script, config.getEncoding());
            ContentResponse response = httpClient.POST(config.getTclRegaUrl()).content(content)
                    .timeout(config.getTimeout(), TimeUnit.SECONDS)
                    .header(HttpHeader.CONTENT_TYPE, "text/plain;charset=" + config.getEncoding()).send();

            String result = new String(response.getContent(), config.getEncoding());
            result = StringUtils.substringBeforeLast(result, "<xml><exec>");
            if (logger.isTraceEnabled()) {
                logger.trace("Result TclRegaScript: {}", result);
            }

            return (T) xStream.fromXML(result);
        } catch (Exception ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }

    /**
     * Load predefined scripts from an XML file.
     */
    private Map<String, String> loadTclRegaScripts() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("homematic/tclrega-scripts.xml");
        TclScriptList scriptList = (TclScriptList) xStream.fromXML(stream);

        Map<String, String> result = new HashMap<String, String>();
        if (scriptList.getScripts() != null) {
            for (TclScript script : scriptList.getScripts()) {
                result.put(script.name, StringUtils.trimToNull(script.data));
            }
        }
        return result;
    }

}
