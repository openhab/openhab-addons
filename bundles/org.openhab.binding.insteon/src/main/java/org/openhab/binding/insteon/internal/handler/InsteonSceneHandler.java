/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.handler;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonSceneConfiguration;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonScene;
import org.openhab.binding.insteon.internal.driver.Driver;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link InsteonSceneHandler} is the handler for an insteon scene thing.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonSceneHandler extends InsteonThingHandler {

    private @Nullable InsteonScene scene;

    public InsteonSceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public @Nullable InsteonDevice getDevice() {
        InsteonScene scene = this.scene;
        return scene == null ? null : scene.getDriver().getModemDevice();
    }

    public @Nullable InsteonScene getScene() {
        return scene;
    }

    private void setScene(@Nullable InsteonScene scene) {
        this.scene = scene;
    }

    @Override
    public void initialize() {
        InsteonSceneConfiguration config = getConfigAs(InsteonSceneConfiguration.class);

        scheduler.execute(() -> {
            if (getBridge() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge selected.");
                return;
            }

            int group = config.getGroup();
            if (!InsteonScene.isValidGroup(group)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid scene group, it must be between " + InsteonScene.GROUP_MIN + " and "
                                + InsteonScene.GROUP_MAX + ".");
                return;
            }

            if (getInsteonBinding().getScene(group) != null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Duplicate scene.");
                return;
            }

            InsteonScene scene = createScene(group);
            setScene(scene);
            updateChannels(scene);
            refresh();
        });
    }

    private InsteonScene createScene(int group) {
        Driver driver = getInsteonBinding().getDriver();
        InsteonScene scene = InsteonScene.makeScene(driver, group);
        scene.setHandler(this);
        scene.initialize();
        return scene;
    }

    private void updateChannels(InsteonScene scene) {
        getThing().getChannels().stream().map(channel -> channel.getConfiguration())
                .forEach(config -> config.put(PARAMETER_GROUP, scene.getGroup()));
    }

    @Override
    public void dispose() {
        setScene(null);

        super.dispose();
    }

    @Override
    public void bridgeThingUpdated() {
        Driver driver = getInsteonBinding().getDriver();
        InsteonScene scene = getScene();
        if (scene != null) {
            scene.setDriver(driver);
        }
    }

    @Override
    protected String getConfigInfo() {
        return getConfigAs(InsteonSceneConfiguration.class).toString();
    }

    @Override
    public void updateStatus() {
        InsteonScene scene = getScene();
        if (scene != null && !scene.hasModemDBEntry() && getInsteonBinding().isModemDBComplete()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Scene not found in modem database.");
            return;
        }

        super.updateStatus();
    }
}
