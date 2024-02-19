/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cloudrain.internal.api.model.AuthParams;
import org.openhab.binding.cloudrain.internal.api.model.Controller;
import org.openhab.binding.cloudrain.internal.api.model.Irrigation;
import org.openhab.binding.cloudrain.internal.api.model.Zone;

/**
 * A simple proxy implementation for the {@link CloudrainAPI} which delegates calls to either the real Cloudrain API or
 * a mockup implementation for testing purposes based on the {@link CloudrainConfig) parameter 'testMode'.
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class CloudrainAPIProxy implements CloudrainAPI {

    private boolean isTestMode = false;

    private CloudrainAPI realAPI;
    private CloudrainAPI testAPI;

    /**
     * Creates an instance of this CloudrainAPIProxy with a reference to the real API and a test implementation.
     *
     * @param realAPI an instance of the CloudrainAPI representing a real API implementation
     * @param testAPI an instance of the CloudrainAPI representing a test implementation
     */
    public CloudrainAPIProxy(CloudrainAPI realAPI, CloudrainAPI testAPI) {
        this.realAPI = realAPI;
        this.testAPI = testAPI;
    }

    @Override
    public void initialize(CloudrainAPIConfig config) throws CloudrainAPIException {
        this.isTestMode = config.getTestMode();
        getTarget().initialize(config);
    }

    @Override
    public void authenticate(AuthParams authParams) throws CloudrainAPIException {
        getTarget().authenticate(authParams);
    }

    @Override
    public List<Controller> getControllers() throws CloudrainAPIException {
        return getTarget().getControllers();
    }

    @Override
    public @Nullable Zone getZone(String id) throws CloudrainAPIException {
        return getTarget().getZone(id);
    }

    @Override
    public List<Zone> getZones() throws CloudrainAPIException {
        return getTarget().getZones();
    }

    @Override
    public List<Irrigation> getIrrigations() throws CloudrainAPIException {
        return getTarget().getIrrigations();
    }

    @Override
    public @Nullable Irrigation getIrrigation(String zoneId) throws CloudrainAPIException {
        return getTarget().getIrrigation(zoneId);
    }

    @Override
    public void startIrrigation(String zoneId, int duration) throws CloudrainAPIException {
        getTarget().startIrrigation(zoneId, duration);
    }

    @Override
    public void adjustIrrigation(String zoneId, int duration) throws CloudrainAPIException {
        getTarget().adjustIrrigation(zoneId, duration);
    }

    @Override
    public void stopIrrigation(String zoneId) throws CloudrainAPIException {
        getTarget().stopIrrigation(zoneId);
    }

    private CloudrainAPI getTarget() {
        return isTestMode ? testAPI : realAPI;
    }
}
