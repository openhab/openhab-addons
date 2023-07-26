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
package org.openhab.binding.lametrictime.internal.api;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.api.cloud.CloudConfiguration;
import org.openhab.binding.lametrictime.internal.api.cloud.LaMetricTimeCloud;
import org.openhab.binding.lametrictime.internal.api.dto.CoreAction;
import org.openhab.binding.lametrictime.internal.api.dto.CoreApplication;
import org.openhab.binding.lametrictime.internal.api.dto.CoreApps;
import org.openhab.binding.lametrictime.internal.api.dto.Icon;
import org.openhab.binding.lametrictime.internal.api.dto.enums.BrightnessMode;
import org.openhab.binding.lametrictime.internal.api.dto.enums.Priority;
import org.openhab.binding.lametrictime.internal.api.dto.enums.Sound;
import org.openhab.binding.lametrictime.internal.api.impl.LaMetricTimeImpl;
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
import org.openhab.binding.lametrictime.internal.api.local.dto.UpdateAction;
import org.openhab.binding.lametrictime.internal.api.local.dto.Widget;

/**
 * Interface for LaMetric Time devices.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public interface LaMetricTime {
    /**
     * Get the version identifier reported by the device.
     *
     * @return the version
     */
    String getVersion();

    /**
     * Send a low priority message to the device.
     *
     * @param message
     *            the text to display
     * @return the identifier of the newly created notification
     * @throws NotificationCreationException
     *             if there is a communication error or malformed data
     */
    String notifyInfo(String message) throws NotificationCreationException;

    /**
     * Send a medium priority message to the device.
     *
     * @param message
     *            the text to display
     * @return the identifier of the newly created notification
     * @throws NotificationCreationException
     *             if there is a communication error or malformed data
     */
    String notifyWarning(String message) throws NotificationCreationException;

    /**
     * Send an urgent message to the device. The notification will not be
     * automatically removed. The user will be required to dismiss this
     * notification or it must be deleted through he API.
     *
     * @param message
     *            the text to display
     * @return the identifier of the newly created notification
     * @throws NotificationCreationException
     *             if there is a communication error or malformed data
     */
    String notifyCritical(String message) throws NotificationCreationException;

    /**
     * Send a notification to the device.
     *
     * Priority is important. It defines the urgency of this notification as
     * related to others in the queue and the current state of the device.
     * <ol>
     * <li>{@link Priority#INFO}: lowest priority; not shown when the
     * screensaver is active; waits for its turn in the queue
     * <li>{@link Priority#WARNING}: middle priority; not shown when the
     * screensaver is active; preempts {@link Priority#INFO}
     * <li>{@link Priority#CRITICAL}: highest priority; shown even when the
     * screensaver is active; preempts all other notifications (to be used
     * sparingly)
     * </ol>
     *
     * @param message
     *            the text to display
     * @param priority
     *            the urgency of this notification; defaults to
     *            {@link Priority#INFO}
     * @param icon
     *            the icon to display next to the message; can be
     *            <code>null</code>
     * @param sound
     *            the sound to play when the notification is displayed; can be
     *            <code>null</code>
     * @param messageRepeat
     *            the number of times the message should be displayed before
     *            being removed (use <code>0</code> to leave the notification on
     *            the device until it is dismissed by the user or deleted
     *            through the API)
     * @param soundRepeat
     *            the number of times to repeat the sound (use <code>0</code> to
     *            keep the sound looping until the notification is dismissed by
     *            the user or deleted through the API)
     * @return the identifier of the newly created notification
     * @throws NotificationCreationException
     *             if there is a communication error or malformed data
     */
    String notify(String message, Priority priority, Icon icon, Sound sound, int messageRepeat, int soundRepeat)
            throws NotificationCreationException;

    /**
     * Get the built-in clock application. This applications displays the time
     * and date. It also provides an alarm feature.
     *
     * @return the clock app
     */
    @Nullable
    Application getClock();

    /**
     * Get the built-in countdown timer application. This application counts
     * time down to zero when it sets off a beeper until it is reset. The
     * countdown can also be paused.
     *
     * @return the countdown app
     */
    @Nullable
    Application getCountdown();

    /**
     * Get the built-in radio application. The radio can play streams from the
     * Internet. The streams are set up in a list and can be navigated using
     * 'next' and 'previous' actions. The music can be started and stopped.
     *
     * @return the radio app
     */
    @Nullable
    Application getRadio();

    /**
     * Get the built-in stopwatch application. The stopwatch counts time
     * forwards and can be started, paused, and reset.
     *
     * @return the stopwatch app
     */
    @Nullable
    Application getStopwatch();

    /**
     * Get the built-in weather application. This application displays the
     * current weather conditions. It can also display the forecast for today
     * and tomorrow.
     *
     * @return the weather app
     */
    @Nullable
    Application getWeather();

    /**
     * Get any of the built-in applications.
     *
     * @param coreApp
     *            the app to retrieve
     * @return the requested app
     */
    @Nullable
    Application getApplication(CoreApplication coreApp);

    /**
     * Get any application installed on the device.
     *
     * @param name
     *            the name of the app to retrieve
     * @return the requested app
     * @throws ApplicationNotFoundException
     *             if the requested app is not found on the device
     */
    @Nullable
    Application getApplication(@Nullable String name) throws ApplicationNotFoundException;

    /**
     * Display the given built-in application on the device.
     *
     * @param coreApp
     *            the app to activate
     */
    void activateApplication(CoreApplication coreApp);

    /**
     * Display the first instance (widget) of the given application on the
     * device.
     *
     * @param app
     *            the app to activate
     * @throws ApplicationActivationException
     *             if the app fails to activate
     */
    void activateApplication(Application app) throws ApplicationActivationException;

    /**
     * Display the given widget on the device. A widget is simply an instance of
     * an application. Some applications can be installed more than once (e.g.
     * the {@link CoreApps#weather() weather} app) and each instance is assigned
     * a widget.
     *
     * @param widget
     *            the application instance (widget) to activate
     * @throws ApplicationActivationException
     *             if the app fails to activate
     */
    void activateWidget(Widget widget) throws ApplicationActivationException;

    /**
     * Perform the given action on the first instance (widget) of the
     * corresponding built-in application. The widget will activate
     * automatically before carrying out the action.
     *
     * @param coreAction
     *            the action to perform
     */
    void doAction(CoreAction coreAction);

    /**
     * Perform the given action on the first instance (widget) of the given
     * application. The widget will activate automatically before carrying out
     * the action.
     *
     * @param app
     *            the app which understands the requested action
     * @param action
     *            the action to perform
     * @throws ApplicationActionException
     *             if the action cannot be performed
     */
    void doAction(Application app, UpdateAction action) throws ApplicationActionException;

    /**
     * Perform the given core action on the given widget. A widget is simply an
     * instance of an application. Some applications can be installed more than
     * once (e.g. the {@link CoreApps#weather() weather} app) and each instance
     * is assigned a widget. The widget will activate automatically before
     * carrying out the action.
     *
     * @param widget
     *            the widget which understands the requested core action
     * @param action
     *            the action to perform
     * @throws ApplicationActionException
     *             if the action cannot be performed
     */
    void doAction(@Nullable Widget widget, CoreAction action) throws ApplicationActionException;

    /**
     * Perform the given action on the given widget. A widget is simply an
     * instance of an application. Some applications can be installed more than
     * once (e.g. the {@link CoreApps#weather() weather} app) and each instance
     * is assigned a widget. The widget will activate automatically before
     * carrying out the action.
     *
     * @param widget
     *            the widget which understands the requested action
     * @param action
     *            the action to perform
     * @throws ApplicationActionException
     *             if the action cannot be performed
     */
    void doAction(Widget widget, UpdateAction action) throws ApplicationActionException;

    /**
     * Set the display brightness. The {@link #setBrightnessMode(BrightnessMode)
     * brightness mode} will also be set to {@link BrightnessMode#MANUAL}.
     *
     * @param brightness
     *            the brightness value to set (must be between 0 and 100,
     *            inclusive)
     * @return the updated state of the display
     * @throws UpdateException
     *             if the update failed
     */
    Display setBrightness(int brightness) throws UpdateException;

    /**
     * Set the brightness mode on the display. {@link BrightnessMode#MANUAL}
     * will immediately respect the current brightness value while
     * {@link BrightnessMode#AUTO} will ignore the brightness value and set the
     * brightness based on ambient light intensity.
     *
     * @param mode
     *            the mode to set
     * @return the updated state of the display
     * @throws UpdateException
     *             if the update failed
     */
    Display setBrightnessMode(BrightnessMode mode) throws UpdateException;

    /**
     * Set the speaker volume on the device.
     *
     * @param volume
     *            the volume to set (must be between 0 and 100, inclusive)
     * @return the update audio state
     * @throws UpdateException
     *             if the update failed
     */
    Audio setVolume(int volume) throws UpdateException;

    /**
     * Mute the device's speakers. The current volume will be stored so that
     * {@link #unmute()} will restored it. If the volume is currently at zero,
     * no action will be taken.
     *
     * @return the update audio state
     * @throws UpdateException
     *             if the update failed
     */
    Audio mute() throws UpdateException;

    /**
     * Restore the volume prior to {@link #mute()}. If the volume has not been
     * muted previously and the volume is currently zero, it will be set to 50%.
     *
     * @return the update audio state
     * @throws UpdateException
     *             if the update failed
     */
    Audio unmute() throws UpdateException;

    /**
     * Set the active state of the Bluetooth radio on the device.
     *
     * @param active
     *            <code>true</code> to activate Bluetooth; <code>false</code> to
     *            deactive it
     * @return the updated state of Bluetooth on the device
     * @throws UpdateException
     *             if the update failed
     */
    Bluetooth setBluetoothActive(boolean active) throws UpdateException;

    /**
     * Set the device name as seen via Bluetooth connectivity.
     *
     * @param name
     *            the name to display on other devices
     * @return the updated state of Bluetooth on the device
     * @throws UpdateException
     *             if the update failed
     */
    Bluetooth setBluetoothName(String name) throws UpdateException;

    /**
     * Get the local API for more advanced interactions as well device inquiry.
     *
     * @return the local API
     */
    LaMetricTimeLocal getLocalApi();

    /**
     * Get the cloud API for interacting with LaMetric's services.
     *
     * @return the cloud API
     */
    LaMetricTimeCloud getCloudApi();

    /**
     * Create an instance of this API. For greater control over the
     * configuration, see {@link #create(Configuration, ClientBuilder)},
     * {@link #create(LocalConfiguration, CloudConfiguration)}, and
     * {@link #create(LocalConfiguration, CloudConfiguration, ClientBuilder)}.
     *
     * @param config
     *            the configuration parameters that the new instance will use
     * @return the API instance
     */
    static LaMetricTime create(Configuration config) {
        return new LaMetricTimeImpl(config);
    }

    /**
     * Create an instance of this API. For greater control over the
     * configuration, see
     * {@link #create(LocalConfiguration, CloudConfiguration, ClientBuilder)}.
     *
     * @param config
     *            the configuration parameters that the new instance will use
     * @param clientBuilder
     *            the builder that will be used to create clients for
     *            communicating with the device and cloud services
     * @return the API instance
     */
    static LaMetricTime create(Configuration config, ClientBuilder clientBuilder) {
        return new LaMetricTimeImpl(config, clientBuilder);
    }

    /**
     * Create an instance of this API specifying detailed configuration for both
     * the local and cloud APIs. See also
     * {@link #create(LocalConfiguration, CloudConfiguration, ClientBuilder)}.
     *
     * @param localConfig
     *            the local API configuration for the new instance
     * @param cloudConfig
     *            the cloud API configuration for the new instance
     * @return the API instance
     */
    static LaMetricTime create(LocalConfiguration localConfig, CloudConfiguration cloudConfig) {
        return new LaMetricTimeImpl(localConfig, cloudConfig);
    }

    /**
     * Create an instance of this API specifying detailed configuration for both
     * the local and cloud APIs as well as the generic client.
     *
     * @param localConfig
     *            the local API configuration for the new instance
     * @param cloudConfig
     *            the cloud API configuration for the new instance
     * @param clientBuilder
     *            the builder that will be used to create clients for
     *            communicating with the device and cloud services
     * @return the API instance
     */
    static LaMetricTime create(LocalConfiguration localConfig, CloudConfiguration cloudConfig,
            ClientBuilder clientBuilder) {
        return new LaMetricTimeImpl(localConfig, cloudConfig, clientBuilder);
    }
}
