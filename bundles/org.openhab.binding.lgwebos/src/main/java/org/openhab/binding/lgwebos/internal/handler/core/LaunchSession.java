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
/* This file is based on:
 *
 * LaunchSession
 * Connect SDK
 *
 * Copyright (c) 2014 LG Electronics.
 * Created by Jeffrey Glenn on 07 Mar 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openhab.binding.lgwebos.internal.handler.core;

import org.openhab.binding.lgwebos.internal.handler.LGWebOSTVSocket;

/**
 * {@link LaunchSession} is a value object to describe a session with an application running on WebOSTV.
 *
 * Any time anything is launched onto a first screen device, there will be important session information that needs to
 * be tracked. {@link LaunchSession} tracks this data, and must be retained to perform certain actions within the
 * session.
 *
 * @author Jeffrey Glenn - Connect SDK initial contribution
 * @author Sebastian Prehn - Adoption for openHAB
 */
public class LaunchSession {

    private String appId;
    private String appName;
    private String sessionId;

    private transient LGWebOSTVSocket socket;
    private transient LaunchSessionType sessionType;

    /**
     * LaunchSession type is used to help DeviceService's know how to close a LaunchSession.
     *
     */
    public enum LaunchSessionType {
        /** Unknown LaunchSession type, may be unable to close this launch session */
        Unknown,
        /** LaunchSession represents a launched app */
        App,
        /** LaunchSession represents an external input picker that was launched */
        ExternalInputPicker,
        /** LaunchSession represents a media app */
        Media,
        /** LaunchSession represents a web app */
        WebApp
    }

    public LaunchSession() {
    }

    /**
     * Instantiates a LaunchSession object for a given app ID.
     *
     * @param appId System-specific, unique ID of the app
     * @return the launch session
     */
    public static LaunchSession launchSessionForAppId(String appId) {
        LaunchSession launchSession = new LaunchSession();
        launchSession.appId = appId;
        return launchSession;
    }

    /** @return System-specific, unique ID of the app (ex. youtube.leanback.v4, 0000134, hulu) */
    public String getAppId() {
        return appId;
    }

    /**
     * Sets the system-specific, unique ID of the app (ex. youtube.leanback.v4, 0000134, hulu)
     *
     * @param appId Id of the app
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /** @return User-friendly name of the app (ex. YouTube, Browser, Hulu) */
    public String getAppName() {
        return appName;
    }

    /**
     * Sets the user-friendly name of the app (ex. YouTube, Browser, Hulu)
     *
     * @param appName Name of the app
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /** @return Unique ID for the session (only provided by certain protocols) */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session id (only provided by certain protocols)
     *
     * @param sessionId Id of the current session
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /** @return WebOSTVSocket responsible for launching the session. */
    public LGWebOSTVSocket getService() {
        return socket;
    }

    /**
     * DeviceService responsible for launching the session.
     *
     * @param service Sets the DeviceService
     */
    public void setService(LGWebOSTVSocket service) {
        this.socket = service;
    }

    /**
     * @return When closing a LaunchSession, the DeviceService relies on the sessionType to determine the method of
     *         closing the session.
     */
    public LaunchSessionType getSessionType() {
        return sessionType;
    }

    /**
     * Sets the LaunchSessionType of this LaunchSession.
     *
     * @param sessionType The type of LaunchSession
     */
    public void setSessionType(LaunchSessionType sessionType) {
        this.sessionType = sessionType;
    }

    /**
     * Close the app/media associated with the session.
     *
     * @param listener the response listener
     */
    public void close(ResponseListener<CommandConfirmation> listener) {
        socket.closeLaunchSession(this, listener);
    }
}
