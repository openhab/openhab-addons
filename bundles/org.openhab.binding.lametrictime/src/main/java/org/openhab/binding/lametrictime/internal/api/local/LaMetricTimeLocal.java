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
package org.openhab.binding.lametrictime.internal.api.local;

import java.util.List;
import java.util.SortedMap;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.api.local.dto.Api;
import org.openhab.binding.lametrictime.internal.api.local.dto.Application;
import org.openhab.binding.lametrictime.internal.api.local.dto.Audio;
import org.openhab.binding.lametrictime.internal.api.local.dto.Bluetooth;
import org.openhab.binding.lametrictime.internal.api.local.dto.Device;
import org.openhab.binding.lametrictime.internal.api.local.dto.Display;
import org.openhab.binding.lametrictime.internal.api.local.dto.Notification;
import org.openhab.binding.lametrictime.internal.api.local.dto.UpdateAction;
import org.openhab.binding.lametrictime.internal.api.local.dto.WidgetUpdates;
import org.openhab.binding.lametrictime.internal.api.local.dto.Wifi;
import org.openhab.binding.lametrictime.internal.api.local.impl.LaMetricTimeLocalImpl;

/**
 * Interface for local device access.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public interface LaMetricTimeLocal {
    Api getApi();

    Device getDevice();

    String createNotification(Notification notification) throws NotificationCreationException;

    List<Notification> getNotifications();

    @Nullable
    Notification getCurrentNotification();

    Notification getNotification(String id) throws NotificationNotFoundException;

    void deleteNotification(String id) throws NotificationNotFoundException;

    Display getDisplay();

    Display updateDisplay(Display display) throws UpdateException;

    Audio getAudio();

    Audio updateAudio(Audio audio) throws UpdateException;

    Bluetooth getBluetooth();

    Bluetooth updateBluetooth(Bluetooth bluetooth) throws UpdateException;

    Wifi getWifi();

    void updateApplication(String packageName, String accessToken, WidgetUpdates widgetUpdates) throws UpdateException;

    SortedMap<String, Application> getApplications();

    @Nullable
    Application getApplication(String packageName) throws ApplicationNotFoundException;

    void activatePreviousApplication();

    void activateNextApplication();

    void activateApplication(String packageName, String widgetId) throws ApplicationActivationException;

    void doAction(String packageName, String widgetId, @Nullable UpdateAction action) throws ApplicationActionException;

    static LaMetricTimeLocal create(LocalConfiguration config) {
        return new LaMetricTimeLocalImpl(config);
    }

    static LaMetricTimeLocal create(LocalConfiguration config, ClientBuilder clientBuilder) {
        return new LaMetricTimeLocalImpl(config, clientBuilder);
    }
}
