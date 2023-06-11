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
package org.openhab.binding.fsinternetradio.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.fsinternetradio.internal.radio.FrontierSiliconRadioConstants;

/**
 * Radio service mock.
 *
 * @author Markus Rathgeb - Initial contribution
 * @author Velin Yordanov - Small adjustments
 */
public class RadioServiceDummy extends HttpServlet {
    private static Map<Integer, String> requestParameters = new ConcurrentHashMap<>();

    private static final long serialVersionUID = 1L;

    private static final String MOCK_RADIO_PIN = "1234";

    private static final String REQUEST_SET_POWER = "/" + FrontierSiliconRadioConstants.REQUEST_SET_POWER;
    private static final String REQUEST_GET_POWER = "/" + FrontierSiliconRadioConstants.REQUEST_GET_POWER;
    private static final String REQUEST_GET_MODE = "/" + FrontierSiliconRadioConstants.REQUEST_GET_MODE;
    private static final String REQUEST_SET_MODE = "/" + FrontierSiliconRadioConstants.REQUEST_SET_MODE;
    private static final String REQUEST_SET_VOLUME = "/" + FrontierSiliconRadioConstants.REQUEST_SET_VOLUME;
    private static final String REQUEST_GET_VOLUME = "/" + FrontierSiliconRadioConstants.REQUEST_GET_VOLUME;
    private static final String REQUEST_SET_MUTE = "/" + FrontierSiliconRadioConstants.REQUEST_SET_MUTE;
    private static final String REQUEST_GET_MUTE = "/" + FrontierSiliconRadioConstants.REQUEST_GET_MUTE;
    private static final String REQUEST_SET_PRESET_ACTION = "/"
            + FrontierSiliconRadioConstants.REQUEST_SET_PRESET_ACTION;
    private static final String REQUEST_GET_PLAY_INFO_TEXT = "/"
            + FrontierSiliconRadioConstants.REQUEST_GET_PLAY_INFO_TEXT;
    private static final String REQUEST_GET_PLAY_INFO_NAME = "/"
            + FrontierSiliconRadioConstants.REQUEST_GET_PLAY_INFO_NAME;
    private static final String VALUE = "value";

    /*
     * For the purposes of the tests it is assumed that the current station and the additional information
     * are always the same (random_station and additional_info)
     */
    private final String playInfoNameValue = "random_station";
    private final String playInfoNameTag = makeC8_arrayTag(playInfoNameValue);

    private final String playInfoTextValue = "additional_info";
    private final String playInfoTextTag = makeC8_arrayTag(playInfoTextValue);

    private final int httpStatus;

    private String tagToReturn = "";
    private String responseToReturn = "";

    private boolean isInvalidResponseExpected;
    private boolean isInvalidValueExpected;
    private boolean isOKAnswerExpected = true;

    private String powerValue;
    private String powerTag = "";

    private String muteValue;
    private String muteTag = "";

    private String absoluteVolumeValue;
    private String absoluteVolumeTag = "";

    private String modeValue;
    private String modeTag = "";

    private String radioStation = "";

    public RadioServiceDummy() {
        this.httpStatus = HttpStatus.OK_200;
    }

    public String getRadioStation() {
        return radioStation;
    }

    public void setRadioStation(final String radioStation) {
        this.radioStation = radioStation;
    }

    public void setInvalidResponseExpected(boolean isInvalidResponseExpected) {
        this.isInvalidResponseExpected = isInvalidResponseExpected;
    }

    public void setOKAnswerExpected(boolean isOKAnswerExpected) {
        this.isOKAnswerExpected = isOKAnswerExpected;
    }

    public boolean containsRequestParameter(int value, String parameter) {
        String url = requestParameters.get(value);
        if (url == null) {
            return false;
        }

        return url.contains(parameter);
    }

    public void clearRequestParameters() {
        requestParameters.clear();
    }

    public boolean areRequestParametersEmpty() {
        return requestParameters.isEmpty();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String queryString = request.getQueryString();
        Collection<String> requestParameterNames = Collections.list(request.getParameterNames());
        if (queryString != null && requestParameterNames.contains(VALUE)) {
            StringBuffer fullUrl = request.getRequestURL().append("?").append(queryString);
            int value = Integer.parseInt(Objects.requireNonNullElse(request.getParameter(VALUE), ""));
            requestParameters.put(value, fullUrl.toString());
        }

        String pin = request.getParameter("pin");
        if (!MOCK_RADIO_PIN.equals(pin)) {
            response.setStatus(HttpStatus.FORBIDDEN_403);
        } else if (!isOKAnswerExpected) {
            response.setStatus(HttpStatus.NOT_FOUND_404);
        } else {
            response.setStatus(HttpStatus.OK_200);
            response.setContentType("text/xml");
            String commandString = request.getPathInfo();

            switch (commandString) {
                case (REQUEST_SET_POWER):
                    if (isInvalidValueExpected) {
                        powerValue = null;
                    } else {
                        powerValue = request.getParameter(VALUE);
                    }

                case (REQUEST_GET_POWER):
                    powerTag = makeU8Tag(powerValue);
                    tagToReturn = powerTag;
                    break;

                case (REQUEST_SET_MUTE):
                    if (isInvalidValueExpected) {
                        muteValue = null;
                    } else {
                        muteValue = request.getParameter(VALUE);
                    }

                case (REQUEST_GET_MUTE):
                    muteTag = makeU8Tag(muteValue);
                    tagToReturn = muteTag;
                    break;

                case (REQUEST_SET_MODE):
                    if (isInvalidValueExpected) {
                        modeValue = null;
                    } else {
                        modeValue = request.getParameter(VALUE);
                    }

                case (REQUEST_GET_MODE):
                    modeTag = makeU32Tag(modeValue);
                    tagToReturn = modeTag;
                    break;

                case (REQUEST_SET_VOLUME):
                    if (isInvalidValueExpected) {
                        absoluteVolumeValue = null;
                    } else {
                        absoluteVolumeValue = request.getParameter(VALUE);
                    }

                case (REQUEST_GET_VOLUME):
                    absoluteVolumeTag = makeU8Tag(absoluteVolumeValue);
                    tagToReturn = absoluteVolumeTag;
                    break;

                case (REQUEST_SET_PRESET_ACTION):
                    final String station = request.getParameter(VALUE);
                    setRadioStation(station);
                    break;

                case (REQUEST_GET_PLAY_INFO_NAME):
                    tagToReturn = playInfoNameTag;
                    break;

                case (REQUEST_GET_PLAY_INFO_TEXT):
                    tagToReturn = playInfoTextTag;
                    break;

                default:
                    tagToReturn = "";
                    break;
            }

            if (isInvalidResponseExpected) {
                responseToReturn = makeInvalidXMLResponse();
            } else {
                responseToReturn = makeValidXMLResponse();
            }
            PrintWriter out = response.getWriter();
            out.print(responseToReturn);
        }
    }

    protected String makeU8Tag(final String value) {
        return String.format("<value><u8>%s</u8></value>", value);
    }

    protected String makeU32Tag(final String value) {
        return String.format("<value><u32>%s</u32></value>", value);
    }

    protected String makeC8_arrayTag(final String value) {
        return String.format("<value><c8_array>%s</c8_array></value>", value);
    }

    private String makeValidXMLResponse() throws IOException {
        return new String(getClass().getResourceAsStream("/validXml.xml").readAllBytes(), StandardCharsets.UTF_8);
    }

    private String makeInvalidXMLResponse() throws IOException {
        return new String(getClass().getResourceAsStream("/invalidXml.xml").readAllBytes(), StandardCharsets.UTF_8);
    }

    public void setInvalidResponse(boolean value) {
        isInvalidResponseExpected = value;
    }
}
