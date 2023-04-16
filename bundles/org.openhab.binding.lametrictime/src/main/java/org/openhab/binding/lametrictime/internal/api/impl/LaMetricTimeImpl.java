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

import static org.openhab.binding.lametrictime.internal.api.dto.ApiValue.raw;

import java.util.Arrays;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.api.Configuration;
import org.openhab.binding.lametrictime.internal.api.LaMetricTime;
import org.openhab.binding.lametrictime.internal.api.cloud.CloudConfiguration;
import org.openhab.binding.lametrictime.internal.api.cloud.LaMetricTimeCloud;
import org.openhab.binding.lametrictime.internal.api.dto.CoreAction;
import org.openhab.binding.lametrictime.internal.api.dto.CoreApplication;
import org.openhab.binding.lametrictime.internal.api.dto.CoreApps;
import org.openhab.binding.lametrictime.internal.api.dto.Icon;
import org.openhab.binding.lametrictime.internal.api.dto.Icons;
import org.openhab.binding.lametrictime.internal.api.dto.enums.BrightnessMode;
import org.openhab.binding.lametrictime.internal.api.dto.enums.Priority;
import org.openhab.binding.lametrictime.internal.api.dto.enums.Sound;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationActionException;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationActivationException;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationNotFoundException;
import org.openhab.binding.lametrictime.internal.api.local.LaMetricTimeLocal;
import org.openhab.binding.lametrictime.internal.api.local.LocalConfiguration;
import org.openhab.binding.lametrictime.internal.api.local.NotificationCreationException;
import org.openhab.binding.lametrictime.internal.api.local.UpdateException;
import org.openhab.binding.lametrictime.internal.api.local.dto.Application;
import org.openhab.binding.lametrictime.internal.api.local.dto.Audio;
import org.openhab.binding.lametrictime.internal.api.local.dto.Bluetooth;
import org.openhab.binding.lametrictime.internal.api.local.dto.Display;
import org.openhab.binding.lametrictime.internal.api.local.dto.Frame;
import org.openhab.binding.lametrictime.internal.api.local.dto.Notification;
import org.openhab.binding.lametrictime.internal.api.local.dto.NotificationModel;
import org.openhab.binding.lametrictime.internal.api.local.dto.UpdateAction;
import org.openhab.binding.lametrictime.internal.api.local.dto.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation class for LaMetricTime interface.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class LaMetricTimeImpl implements LaMetricTime {

    private final Logger logger = LoggerFactory.getLogger(LaMetricTimeImpl.class);

    private final LaMetricTimeLocal local;
    private final LaMetricTimeCloud cloud;

    private final Object muteLock = new Object();
    @Nullable
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
    public String notifyInfo(@Nullable String message) throws NotificationCreationException {
        return notify(message, Priority.INFO, Icons.key("i1248"), Sound.NOTIFICATION, 1, 1);
    }

    @Override
    public String notifyWarning(@Nullable String message) throws NotificationCreationException {
        return notify(message, Priority.WARNING, Icons.key("a2098"), Sound.NOTIFICATION2, 2, 2);
    }

    @Override
    public String notifyCritical(@Nullable String message) throws NotificationCreationException {
        return notify(message, Priority.CRITICAL, Icons.key("a4787"), Sound.ALARM1, 0, 0);
    }

    @Override
    public String notify(@Nullable String message, @Nullable Priority priority, @Nullable Icon icon,
            @Nullable Sound sound, int messageRepeat, int soundRepeat) throws NotificationCreationException {
        // @formatter:off
        NotificationModel model = new NotificationModel()
                                      .withCycles(messageRepeat)
                                      .withFrames(Arrays.asList(new Frame().withText(message)
                                                                           .withIcon(raw(icon))));
        if (sound != null)
        {
            model.setSound(new org.openhab.binding.lametrictime.internal.api.local.dto.Sound()
                               .withCategory(raw(sound.getCategory()))
                               .withId(raw(sound))
                               .withRepeat(soundRepeat));
        }
        // @formatter:on

        Notification notification = new Notification().withPriority(raw(priority)).withModel(model);
        return local.createNotification(notification);
    }

    @Override
    public @Nullable Application getClock() {
        return getApplication(CoreApps.clock());
    }

    @Override
    public @Nullable Application getCountdown() {
        return getApplication(CoreApps.countdown());
    }

    @Override
    public @Nullable Application getRadio() {
        return getApplication(CoreApps.radio());
    }

    @Override
    public @Nullable Application getStopwatch() {
        return getApplication(CoreApps.stopwatch());
    }

    @Override
    public @Nullable Application getWeather() {
        return getApplication(CoreApps.weather());
    }

    @Override
    public @Nullable Application getApplication(@Nullable CoreApplication coreApp) {
        try {
            return getLocalApi().getApplication(coreApp.getPackageName());
        } catch (ApplicationNotFoundException e) {
            // core apps should never throw errors
            logger.error("Failed to retrieve core application: {}", coreApp.getPackageName(), e);
            return null;
        }
    }

    @Override
    public @Nullable Application getApplication(@Nullable String name) throws ApplicationNotFoundException {
        if (name != null) {
            return getLocalApi().getApplication(name);
        } else {
            return null;
        }
    }

    @Override
    public void activateApplication(@Nullable CoreApplication coreApp) {
        try {
            activateApplication(getApplication(coreApp));
        } catch (ApplicationActivationException e) {
            // core apps should never throw errors
            logger.error("Failed to activate core application: {}", coreApp.getPackageName(), e);
        }
    }

    @Override
    public void activateApplication(@Nullable Application app) throws ApplicationActivationException {
        getLocalApi().activateApplication(app.getPackageName(), getFirstWidgetId(app));
    }

    @Override
    public void activateWidget(@Nullable Widget widget) throws ApplicationActivationException {
        getLocalApi().activateApplication(widget.getPackageName(), widget.getId());
    }

    @Override
    public void doAction(@Nullable CoreAction coreAction) {
        try {
            doAction(getApplication(coreAction.getApp()), coreAction.getAction());
        } catch (ApplicationActionException e) {
            // core apps should never throw errors
            logger.error("Failed to execute weather forecast action", e);
        }
    }

    @Override
    public void doAction(@Nullable Application app, @Nullable UpdateAction action) throws ApplicationActionException {
        getLocalApi().doAction(app.getPackageName(), getFirstWidgetId(app), action);
    }

    @Override
    public void doAction(@Nullable Widget widget, @Nullable CoreAction coreAction) throws ApplicationActionException {
        doAction(widget, coreAction.getAction());
    }

    @Override
    public void doAction(@Nullable Widget widget, @Nullable UpdateAction action) throws ApplicationActionException {
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
    public Display setBrightnessMode(@Nullable BrightnessMode mode) throws UpdateException {
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
    public Bluetooth setBluetoothName(@Nullable String name) throws UpdateException {
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
