/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
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
package org.openhab.binding.lametrictime.api.local;

import java.util.List;
import java.util.SortedMap;

import javax.ws.rs.client.ClientBuilder;

import org.openhab.binding.lametrictime.api.local.impl.LaMetricTimeLocalImpl;
import org.openhab.binding.lametrictime.api.local.model.Api;
import org.openhab.binding.lametrictime.api.local.model.Application;
import org.openhab.binding.lametrictime.api.local.model.Audio;
import org.openhab.binding.lametrictime.api.local.model.Bluetooth;
import org.openhab.binding.lametrictime.api.local.model.Device;
import org.openhab.binding.lametrictime.api.local.model.Display;
import org.openhab.binding.lametrictime.api.local.model.Notification;
import org.openhab.binding.lametrictime.api.local.model.UpdateAction;
import org.openhab.binding.lametrictime.api.local.model.WidgetUpdates;
import org.openhab.binding.lametrictime.api.local.model.Wifi;

public interface LaMetricTimeLocal
{
    public Api getApi();

    public Device getDevice();

    public String createNotification(Notification notification) throws NotificationCreationException;

    public List<Notification> getNotifications();

    public Notification getCurrentNotification();

    public Notification getNotification(String id) throws NotificationNotFoundException;

    public void deleteNotification(String id) throws NotificationNotFoundException;

    public Display getDisplay();

    public Display updateDisplay(Display display) throws UpdateException;

    public Audio getAudio();

    public Audio updateAudio(Audio audio) throws UpdateException;

    public Bluetooth getBluetooth();

    public Bluetooth updateBluetooth(Bluetooth bluetooth) throws UpdateException;

    public Wifi getWifi();

    public void updateApplication(String packageName,
                                  String accessToken,
                                  WidgetUpdates widgetUpdates) throws UpdateException;

    public SortedMap<String, Application> getApplications();

    public Application getApplication(String packageName) throws ApplicationNotFoundException;

    public void activatePreviousApplication();

    public void activateNextApplication();

    public void activateApplication(String packageName,
                                    String widgetId) throws ApplicationActivationException;

    public void doAction(String packageName,
                         String widgetId,
                         UpdateAction action) throws ApplicationActionException;

    public static LaMetricTimeLocal create(LocalConfiguration config)
    {
        return new LaMetricTimeLocalImpl(config);
    }

    public static LaMetricTimeLocal create(LocalConfiguration config, ClientBuilder clientBuilder)
    {
        return new LaMetricTimeLocalImpl(config, clientBuilder);
    }
}
