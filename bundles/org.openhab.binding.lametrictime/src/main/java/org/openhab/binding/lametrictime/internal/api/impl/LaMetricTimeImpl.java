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
package org.openhab.binding.lametrictime.internal.api.impl;

import static org.openhab.binding.lametrictime.internal.api.model.ApiValue.raw;

import java.util.Arrays;

import javax.ws.rs.client.ClientBuilder;

import org.openhab.binding.lametrictime.internal.api.Configuration;
import org.openhab.binding.lametrictime.internal.api.LaMetricTime;
import org.openhab.binding.lametrictime.internal.api.cloud.CloudConfiguration;
import org.openhab.binding.lametrictime.internal.api.cloud.LaMetricTimeCloud;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationActionException;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationActivationException;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationNotFoundException;
import org.openhab.binding.lametrictime.internal.api.local.LaMetricTimeLocal;
import org.openhab.binding.lametrictime.internal.api.local.LocalConfiguration;
import org.openhab.binding.lametrictime.internal.api.local.NotificationCreationException;
import org.openhab.binding.lametrictime.internal.api.local.UpdateException;
import org.openhab.binding.lametrictime.internal.api.local.model.Application;
import org.openhab.binding.lametrictime.internal.api.local.model.Audio;
import org.openhab.binding.lametrictime.internal.api.local.model.Bluetooth;
import org.openhab.binding.lametrictime.internal.api.local.model.Display;
import org.openhab.binding.lametrictime.internal.api.local.model.Frame;
import org.openhab.binding.lametrictime.internal.api.local.model.Notification;
import org.openhab.binding.lametrictime.internal.api.local.model.NotificationModel;
import org.openhab.binding.lametrictime.internal.api.local.model.UpdateAction;
import org.openhab.binding.lametrictime.internal.api.local.model.Widget;
import org.openhab.binding.lametrictime.internal.api.model.CoreAction;
import org.openhab.binding.lametrictime.internal.api.model.CoreApplication;
import org.openhab.binding.lametrictime.internal.api.model.CoreApps;
import org.openhab.binding.lametrictime.internal.api.model.Icon;
import org.openhab.binding.lametrictime.internal.api.model.Icons;
import org.openhab.binding.lametrictime.internal.api.model.enums.BrightnessMode;
import org.openhab.binding.lametrictime.internal.api.model.enums.Priority;
import org.openhab.binding.lametrictime.internal.api.model.enums.Sound;

/**
 * Implementation class for LaMetricTime interface.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class LaMetricTimeImpl implements LaMetricTime {
    private final LaMetricTimeLocal local;
    private final LaMetricTimeCloud cloud;

    private final Object muteLock = new Object();
    private Integer volumeSaveState;

    public LaMetricTimeImpl(Configuration config) {
        this(config.getLocalConfig(), config.getCloudConfig());
    }

    public LaMetricTimeImpl(Configuration config, ClientBuilder clientBuilder) {
        this(config.getLocalConfig(), config.getCloudConfig(), clientBuilder);
    }

    public LaMetricTimeImpl(LocalConfiguration localConfig, CloudConfiguration cloudConfig) {
        this.local = LaMetricTimeLocal.create(localConfig);
        this.cloud = LaMetricTimeCloud.create(cloudConfig);
    }

    public LaMetricTimeImpl(LocalConfiguration localConfig, CloudConfiguration cloudConfig,
            ClientBuilder clientBuilder) {
        this.local = LaMetricTimeLocal.create(localConfig, clientBuilder);
        this.cloud = LaMetricTimeCloud.create(cloudConfig, clientBuilder);
    }

    @Override
    public String getVersion() {
        return local.getApi().getApiVersion();
    }

    @Override
    public String notifyInfo(String message) throws NotificationCreationException {
        return notify(message, Priority.INFO, Icons.key("i1248"), Sound.NOTIFICATION, 1, 1);
    }

    @Override
    public String notifyWarning(String message) throws NotificationCreationException {
        return notify(message, Priority.WARNING, Icons.key("a2098"), Sound.NOTIFICATION2, 2, 2);
    }

    @Override
    public String notifyCritical(String message) throws NotificationCreationException {
        return notify(message, Priority.CRITICAL, Icons.key("a4787"), Sound.ALARM1, 0, 0);
    }

    @Override
    public String notify(String message, Priority priority, Icon icon, Sound sound, int messageRepeat, int soundRepeat)
            throws NotificationCreationException {
        // @formatter:off
        NotificationModel model = new NotificationModel()
                                      .withCycles(messageRepeat)
                                      .withFrames(Arrays.asList(new Frame().withText(message)
                                                                           .withIcon(raw(icon))));
        if (sound != null)
        {
            model.setSound(new org.openhab.binding.lametrictime.internal.api.local.model.Sound()
                               .withCategory(raw(sound.getCategory()))
                               .withId(raw(sound))
                               .withRepeat(soundRepeat));
        }
        // @formatter:on

        Notification notification = new Notification().withPriority(raw(priority)).withModel(model);
        return local.createNotification(notification);
    }

    @Override
    public Application getClock() {
        return getApplication(CoreApps.clock());
    }

    @Override
    public Application getCountdown() {
        return getApplication(CoreApps.countdown());
    }

    @Override
    public Application getRadio() {
        return getApplication(CoreApps.radio());
    }

    @Override
    public Application getStopwatch() {
        return getApplication(CoreApps.stopwatch());
    }

    @Override
    public Application getWeather() {
        return getApplication(CoreApps.weather());
    }

    @Override
    public Application getApplication(CoreApplication coreApp) {
        try {
            return getLocalApi().getApplication(coreApp.getPackageName());
        } catch (ApplicationNotFoundException e) {
            // core apps should never throw errors
            throw new RuntimeException("Failed to retrieve core application: " + coreApp.getPackageName(), e);
        }
    }

    @Override
    public Application getApplication(String name) throws ApplicationNotFoundException {
        return getLocalApi().getApplication(name);
    }

    @Override
    public void activateApplication(CoreApplication coreApp) {
        try {
            activateApplication(getApplication(coreApp));
        } catch (ApplicationActivationException e) {
            // core apps should never throw errors
            throw new RuntimeException("Failed to activate core application: " + coreApp.getPackageName(), e);
        }
    }

    @Override
    public void activateApplication(Application app) throws ApplicationActivationException {
        getLocalApi().activateApplication(app.getPackageName(), getFirstWidgetId(app));
    }

    @Override
    public void activateWidget(Widget widget) throws ApplicationActivationException {
        getLocalApi().activateApplication(widget.getPackageName(), widget.getId());
    }

    @Override
    public void doAction(CoreAction coreAction) {
        try {
            doAction(getApplication(coreAction.getApp()), coreAction.getAction());
        } catch (ApplicationActionException e) {
            // core apps should never throw errors
            throw new RuntimeException("Failed to execute weather forecast action", e);
        }
    }

    @Override
    public void doAction(Application app, UpdateAction action) throws ApplicationActionException {
        getLocalApi().doAction(app.getPackageName(), getFirstWidgetId(app), action);
    }

    @Override
    public void doAction(Widget widget, CoreAction coreAction) throws ApplicationActionException {
        doAction(widget, coreAction.getAction());
    }

    @Override
    public void doAction(Widget widget, UpdateAction action) throws ApplicationActionException {
        getLocalApi().doAction(widget.getPackageName(), widget.getId(), action);
    }

    protected String getFirstWidgetId(Application app) {
        return app.getWidgets().firstKey();
    }

    @Override
    public Display setBrightness(int brightness) throws UpdateException {
        return local
                .updateDisplay(new Display().withBrightness(brightness).withBrightnessMode(raw(BrightnessMode.MANUAL)));
    }

    @Override
    public Display setBrightnessMode(BrightnessMode mode) throws UpdateException {
        return local.updateDisplay(new Display().withBrightnessMode(raw(mode)));
    }

    @Override
    public Audio setVolume(int volume) throws UpdateException {
        return local.updateAudio(new Audio().withVolume(volume));
    }

    @Override
    public Audio mute() throws UpdateException {
        synchronized (muteLock) {
            Audio audio = local.getAudio();
            if (audio.getVolume() == 0) {
                return audio;
            }

            volumeSaveState = audio.getVolume();
            return setVolume(0);
        }
    }

    @Override
    public Audio unmute() throws UpdateException {
        synchronized (muteLock) {
            if (volumeSaveState == null) {
                Audio audio = local.getAudio();
                if (audio.getVolume() == 0) {
                    return setVolume(50);
                } else {
                    return audio;
                }
            }

            Audio audio = setVolume(volumeSaveState);
            volumeSaveState = null;
            return audio;
        }
    }

    @Override
    public Bluetooth setBluetoothActive(boolean active) throws UpdateException {
        return local.updateBluetooth(new Bluetooth().withActive(active));
    }

    @Override
    public Bluetooth setBluetoothName(String name) throws UpdateException {
        return local.updateBluetooth(new Bluetooth().withName(name));
    }

    @Override
    public LaMetricTimeLocal getLocalApi() {
        return local;
    }

    @Override
    public LaMetricTimeCloud getCloudApi() {
        return cloud;
    }
}
