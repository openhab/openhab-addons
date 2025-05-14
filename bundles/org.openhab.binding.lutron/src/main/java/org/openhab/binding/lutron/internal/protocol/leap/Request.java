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
package org.openhab.binding.lutron.internal.protocol.leap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.protocol.FanSpeedType;
import org.openhab.binding.lutron.internal.protocol.leap.dto.LeapRequest;

import com.google.gson.Gson;

/**
 * Contains static methods for constructing LEAP messages
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class Request {
    public static final String BUTTON_GROUP_URL = "/buttongroup";

    public static String goToLevel(int zone, int value) {
        String request = """
                {"CommuniqueType": "CreateRequest",\
                "Header": {"Url": "/zone/%d/commandprocessor"},\
                "Body": {\
                "Command": {\
                "CommandType": "GoToLevel",\
                "Parameter": [{"Type": "Level", "Value": %d}]}}}\
                """;
        return String.format(request, zone, value);
    }

    /** fadeTime must be in the format hh:mm:ss **/
    public static String goToDimmedLevel(int zone, int value, String fadeTime) {
        String request = """
                {"CommuniqueType": "CreateRequest",\
                "Header": {"Url": "/zone/%d/commandprocessor"},"Body": {"Command": {\
                "CommandType": "GoToDimmedLevel",\
                "DimmedLevelParameters": {"Level": %d, "FadeTime": "%s"}}}}\
                """;
        return String.format(request, zone, value, fadeTime);
    }

    /** fadeTime and delayTime must be in the format hh:mm:ss **/
    public static String goToDimmedLevel(int zone, int value, String fadeTime, String delayTime) {
        String request = """
                {"CommuniqueType": "CreateRequest",\
                "Header": {"Url": "/zone/%d/commandprocessor"},"Body": {"Command": {\
                "CommandType": "GoToDimmedLevel",\
                "DimmedLevelParameters": {"Level": %d, "FadeTime": "%s", "DelayTime": "%s"}}}}\
                """;
        return String.format(request, zone, value, fadeTime, delayTime);
    }

    public static String goToFanSpeed(int zone, FanSpeedType fanSpeed) {
        String request = """
                {"CommuniqueType": "CreateRequest",\
                "Header": {"Url": "/zone/%d/commandprocessor"},\
                "Body": {\
                "Command": {"CommandType": "GoToFanSpeed",\
                "FanSpeedParameters": {"FanSpeed": "%s"}}}}\
                """;
        return String.format(request, zone, fanSpeed.leapValue());
    }

    public static String buttonCommand(int button, CommandType command) {
        String request = """
                {"CommuniqueType": "CreateRequest",\
                "Header": {"Url": "/button/%d/commandprocessor"},\
                "Body": {"Command": {"CommandType": "%s"}}}\
                """;
        return String.format(request, button, command.toString());
    }

    public static String virtualButtonCommand(int virtualbutton, CommandType command) {
        String request = """
                {"CommuniqueType": "CreateRequest",\
                "Header": {"Url": "/virtualbutton/%d/commandprocessor"},\
                "Body": {"Command": {"CommandType": "%s"}}}\
                """;
        return String.format(request, virtualbutton, command.toString());
    }

    public static String zoneCommand(int zone, CommandType commandType) {
        String request = """
                {"CommuniqueType": "CreateRequest",\
                "Header": {"Url": "/zone/%d/commandprocessor"},\
                "Body": {\
                "Command": {\
                "CommandType": "%s"}}}\
                """;
        return String.format(request, zone, commandType.toString());
    }

    public static String request(CommuniqueType cType, String url) {
        LeapRequest leapRequest = new LeapRequest();
        leapRequest.communiqueType = cType.toString();
        leapRequest.header = new LeapRequest.Header();
        leapRequest.header.url = url;

        Gson gson = new Gson();
        return gson.toJson(leapRequest);
    }

    public static String ping() {
        return request(CommuniqueType.READREQUEST, "/server/1/status/ping");
    }

    public static String getDevices() {
        return getDevices("");
    }

    public static String getDevices(boolean thisDevice) {
        String url = String.format("where=IsThisDevice:%s", (thisDevice) ? "true" : "false");

        return getDevices(url);
    }

    public static String getDevices(String predicate) {
        String url = "/device";
        if (!predicate.isEmpty()) {
            url = String.format("%s?%s", url, predicate);
        }

        return request(CommuniqueType.READREQUEST, url);
    }

    public static String getVirtualButtons() {
        return request(CommuniqueType.READREQUEST, "/virtualbutton");
    }

    public static String getButtonGroups() {
        return request(CommuniqueType.READREQUEST, BUTTON_GROUP_URL);
    }

    public static String getProject() {
        return request(CommuniqueType.READREQUEST, "/project");
    }

    public static String getAreas() {
        return request(CommuniqueType.READREQUEST, "/area");
    }

    public static String getOccupancyGroups() {
        return request(CommuniqueType.READREQUEST, "/occupancygroup");
    }

    public static String getZoneStatus(int zone) {
        return request(CommuniqueType.READREQUEST, String.format("/zone/%d/status", zone));
    }

    public static String getZoneStatuses() {
        return request(CommuniqueType.READREQUEST,
                "/zone/status/expanded?where=Zone.ControlType:\"Dimmed\"|\"Switched\"|\"CCO\"|\"Shade\"");
    }

    public static String getOccupancyGroupStatus() {
        return request(CommuniqueType.READREQUEST, "/occupancygroup/status");
    }

    public static String subscribeButtonStatus(int button) {
        return request(CommuniqueType.SUBSCRIBEREQUEST, String.format("/button/%d/status/event", button));
    }

    public static String subscribeOccupancyGroupStatus() {
        return request(CommuniqueType.SUBSCRIBEREQUEST, "/occupancygroup/status");
    }

    public static String subscribeZoneStatus() {
        return request(CommuniqueType.SUBSCRIBEREQUEST, "/zone/status");
    }

    public static String subscribeAreaStatus() {
        return request(CommuniqueType.SUBSCRIBEREQUEST, "/area/status");
    }
}
