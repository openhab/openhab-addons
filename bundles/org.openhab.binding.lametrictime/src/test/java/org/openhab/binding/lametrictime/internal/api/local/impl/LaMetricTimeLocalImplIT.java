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
package org.openhab.binding.lametrictime.internal.api.local.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lametrictime.internal.api.dto.CoreApps;
import org.openhab.binding.lametrictime.internal.api.dto.enums.BrightnessMode;
import org.openhab.binding.lametrictime.internal.api.dto.enums.Priority;
import org.openhab.binding.lametrictime.internal.api.dto.enums.SoundCategory;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationActionException;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationActivationException;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationNotFoundException;
import org.openhab.binding.lametrictime.internal.api.local.LocalConfiguration;
import org.openhab.binding.lametrictime.internal.api.local.NotificationCreationException;
import org.openhab.binding.lametrictime.internal.api.local.NotificationNotFoundException;
import org.openhab.binding.lametrictime.internal.api.local.UpdateException;
import org.openhab.binding.lametrictime.internal.api.local.dto.Audio;
import org.openhab.binding.lametrictime.internal.api.local.dto.Bluetooth;
import org.openhab.binding.lametrictime.internal.api.local.dto.Display;
import org.openhab.binding.lametrictime.internal.api.local.dto.Frame;
import org.openhab.binding.lametrictime.internal.api.local.dto.GoalData;
import org.openhab.binding.lametrictime.internal.api.local.dto.Notification;
import org.openhab.binding.lametrictime.internal.api.local.dto.NotificationModel;
import org.openhab.binding.lametrictime.internal.api.local.dto.Sound;
import org.openhab.binding.lametrictime.internal.api.test.TestUtil;

/**
 * <p>
 * This test is excluded from the normal battery of tests because it is not a
 * unit test, but rather a live test against an actual device. The purpose of
 * this test is to make sure that after a firmware upgrade, the device still
 * responds in a backwards compatible way.
 * </p>
 * <br>
 * <p>
 * To run this test, first create a file called 'device.properties' in the
 * matching package as this class under 'src/test/resources' with two
 * properties: 'host' and 'apiKey'. After putting the configuration in place,
 * either execute the test via your IDE or run 'mvn -DskipITs=false
 * integration-test'.
 * </p>
 *
 * @author Gregory Moyer - Initial contribution
 */
@Disabled
@NonNullByDefault
public class LaMetricTimeLocalImplIT {
    private static final String PROP_HOST = "host";
    private static final String PROP_API_KEY = "apiKey";

    @Nullable
    private static LaMetricTimeLocalImpl local;

    @BeforeAll
    public static void setup() throws IOException {
        File file = TestUtil.getTestDataPath(LaMetricTimeLocalImplIT.class, "device.properties").toFile();
        if (!file.exists()) {
            throw new IllegalStateException("Device configuration properties missing at " + file.getAbsolutePath());
        }

        try (InputStream in = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(in);

            if (!properties.containsKey(PROP_HOST)) {
                throw new IllegalStateException("Device configuration property " + PROP_HOST + " was not found");
            }

            if (!properties.containsKey(PROP_API_KEY)) {
                throw new IllegalStateException("Device configuration property " + PROP_API_KEY + " was not found");
            }

            LocalConfiguration config = new LocalConfiguration().withHost(properties.getProperty(PROP_HOST))
                    .withApiKey(properties.getProperty(PROP_API_KEY)).withLogging(true);
            local = new LaMetricTimeLocalImpl(config);
        }
    }

    @Test
    public void testGetApi() {
        local.getApi();
    }

    @Test
    public void testGetDevice() {
        local.getDevice();
    }

    @Test
    public void testCreateAndGetNotification() throws NotificationCreationException, NotificationNotFoundException {
        String id = local.createNotification(buildSimpleNotification(1));
        local.getCurrentNotification();
        local.getNotification(id);
    }

    @Test
    public void testCreateGoalNotification() throws NotificationCreationException, NotificationNotFoundException {
        local.createNotification(buildGoalNotification(1));
    }

    @Test
    public void testCreateChartNotification() throws NotificationCreationException, NotificationNotFoundException {
        local.createNotification(buildChartNotification(1));
    }

    @Test
    public void testGetNotifications() {
        local.getNotifications();
    }

    @Test
    public void testGetInvalidNotification() {
        assertThrows(NotificationNotFoundException.class, () -> local.getNotification("invalid"));
    }

    @Test
    public void testCreateAndDeleteNotification() throws NotificationCreationException, NotificationNotFoundException {
        String id = local.createNotification(buildSimpleNotification(0));
        local.deleteNotification(id);
    }

    @Test
    public void testGetDisplay() {
        local.getDisplay();
    }

    @Test
    public void testUpdateDisplay() throws UpdateException {
        local.updateDisplay(new Display().withBrightnessMode(BrightnessMode.AUTO.toRaw()));
    }

    @Test
    public void testGetAudio() {
        local.getAudio();
    }

    @Test
    public void testUpdateAudio() throws UpdateException {
        local.updateAudio(new Audio().withVolume(25));
    }

    @Test
    public void testGetBluetooth() {
        local.getBluetooth();
    }

    @Test
    public void testUpdateBluetooth() throws UpdateException {
        local.updateBluetooth(new Bluetooth().withActive(false));
    }

    @Test
    public void testGetWifi() {
        local.getWifi();
    }

    @Test
    public void testGetApplications() {
        local.getApplications();
    }

    @Test
    public void testGetClockApplication() throws ApplicationNotFoundException {
        local.getApplication(CoreApps.clock().getPackageName());
    }

    @Test
    public void testGetCountdownApplication() throws ApplicationNotFoundException {
        local.getApplication(CoreApps.countdown().getPackageName());
    }

    @Test
    public void testGetRadioApplication() throws ApplicationNotFoundException {
        local.getApplication(CoreApps.radio().getPackageName());
    }

    @Test
    public void testGetStopwatchApplication() throws ApplicationNotFoundException {
        local.getApplication(CoreApps.stopwatch().getPackageName());
    }

    @Test
    public void testGetWeatherApplication() throws ApplicationNotFoundException {
        local.getApplication(CoreApps.weather().getPackageName());
    }

    @Test
    public void testGetInvalidApplication() {
        assertThrows(ApplicationNotFoundException.class, () -> local.getApplication("invalid"));
    }

    @Test
    public void testActivatePreviousApplication() {
        local.activatePreviousApplication();
    }

    @Test
    public void testActivateNextApplication() {
        local.activateNextApplication();
    }

    @Test
    public void testActivateApplication() throws ApplicationActivationException, ApplicationNotFoundException {
        // delete any notifications on the device or else the activate fails
        local.getNotifications().stream().forEach(n -> {
            try {
                local.deleteNotification(n.getId());
            } catch (NotificationNotFoundException e) {
                // ignore
            }
        });

        local.activateApplication(CoreApps.clock().getPackageName(),
                local.getApplication(CoreApps.clock().getPackageName()).getWidgets().firstKey());
    }

    @Test
    public void testDoAction() throws ApplicationActionException, ApplicationNotFoundException {
        local.doAction(CoreApps.weather().getPackageName(),
                local.getApplication(CoreApps.weather().getPackageName()).getWidgets().firstKey(),
                CoreApps.weather().forecast().getAction());
    }

    private Notification buildSimpleNotification(int cycles) {
        return new Notification().withPriority(Priority.CRITICAL.toRaw()).withModel(new NotificationModel()
                .withCycles(cycles)
                .withSound(new Sound().withCategory(SoundCategory.NOTIFICATIONS.toRaw())
                        .withId(org.openhab.binding.lametrictime.internal.api.dto.enums.Sound.CAT.toRaw()))
                .withFrames(Arrays.asList(new Frame().withText("CAT!").withIcon(
                        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAICAYAAADED76LAAAAUklEQVQYlWNUVFBgYGBgYBC98uE/AxJ4rSPAyMDAwMCETRJZjAnGgOlAZote+fCfCV0nOmA0+yKAYTwygJuAzQoGBgYGRkUFBQZ0dyDzGQl5EwCTESNpFb6zEwAAAABJRU5ErkJggg=="))));
    }

    private Notification buildGoalNotification(int cycles) {
        return new Notification().withPriority(Priority.CRITICAL.toRaw())
                .withModel(new NotificationModel().withCycles(cycles).withFrames(Arrays.asList(new Frame()
                        .withGoalData(new GoalData().withStart(0).withCurrent(50).withEnd(100).withUnit("%")).withIcon(
                                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAICAYAAADED76LAAAAUklEQVQYlWNUVFBgYGBgYBC98uE/AxJ4rSPAyMDAwMCETRJZjAnGgOlAZote+fCfCV0nOmA0+yKAYTwygJuAzQoGBgYGRkUFBQZ0dyDzGQl5EwCTESNpFb6zEwAAAABJRU5ErkJggg=="))));
    }

    private Notification buildChartNotification(int cycles) {
        return new Notification().withPriority(Priority.CRITICAL.toRaw()).withModel(new NotificationModel()
                .withCycles(cycles)
                .withFrames(Arrays.asList(new Frame().withChartData(Arrays.asList(1, 2, 3, 4, 5, 6, 7)).withIcon(
                        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAICAYAAADED76LAAAAUklEQVQYlWNUVFBgYGBgYBC98uE/AxJ4rSPAyMDAwMCETRJZjAnGgOlAZote+fCfCV0nOmA0+yKAYTwygJuAzQoGBgYGRkUFBQZ0dyDzGQl5EwCTESNpFb6zEwAAAABJRU5ErkJggg=="))));
    }
}
