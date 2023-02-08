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
package org.openhab.binding.plugwiseha.internal.api.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.model.dto.ActuatorFunctionality;
import org.openhab.binding.plugwiseha.internal.api.model.dto.ActuatorFunctionalityOffsetTemperature;
import org.openhab.binding.plugwiseha.internal.api.model.dto.ActuatorFunctionalityRelay;
import org.openhab.binding.plugwiseha.internal.api.model.dto.ActuatorFunctionalityThermostat;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Appliance;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Appliances;
import org.openhab.binding.plugwiseha.internal.api.model.dto.DomainObjects;
import org.openhab.binding.plugwiseha.internal.api.model.dto.GatewayInfo;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Location;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Locations;
import org.openhab.binding.plugwiseha.internal.api.model.dto.LocationsArray;
import org.openhab.binding.plugwiseha.internal.api.xml.PlugwiseHAXStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlugwiseHAController} class provides the interface to the Plugwise
 * Home Automation API and stores/caches the object model for use by the various
 * ThingHandlers of this binding.
 * 
 * @author B. van Wetten - Initial contribution
 */
@NonNullByDefault
public class PlugwiseHAController {

    // Private member variables/constants

    private static final int MAX_AGE_MINUTES_FULL_REFRESH = 15;
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.RFC_1123_DATE_TIME; // default Date format that
                                                                                          // will be used in conversion

    private final Logger logger = LoggerFactory.getLogger(PlugwiseHAController.class);

    private final HttpClient httpClient;
    private final PlugwiseHAXStream xStream;
    private final Transformer domainObjectsTransformer;

    private final String host;
    private final int port;
    private final String username;
    private final String smileId;
    private final int maxAgeSecondsRefresh;

    private @Nullable ZonedDateTime gatewayUpdateDateTime;
    private @Nullable ZonedDateTime gatewayFullUpdateDateTime;
    private @Nullable DomainObjects domainObjects;

    public PlugwiseHAController(HttpClient httpClient, String host, int port, String username, String smileId,
            int maxAgeSecondsRefresh) throws PlugwiseHAException {
        this.httpClient = httpClient;
        this.host = host;
        this.port = port;
        this.username = username;
        this.smileId = smileId;
        this.maxAgeSecondsRefresh = maxAgeSecondsRefresh;

        this.xStream = new PlugwiseHAXStream();

        ClassLoader localClassLoader = getClass().getClassLoader();
        if (localClassLoader != null) {
            this.domainObjectsTransformer = PlugwiseHAController
                    .setXSLT(new StreamSource(localClassLoader.getResourceAsStream("domain_objects.xslt")));
        } else {
            throw new PlugwiseHAException("PlugwiseHAController.domainObjectsTransformer could not be initialized");
        }
    }

    // Public methods

    public void start(Runnable callback) throws PlugwiseHAException {
        refresh();
        callback.run();
    }

    public void refresh() throws PlugwiseHAException {
        synchronized (this) {
            this.getUpdatedDomainObjects();
        }
    }

    // Public API methods

    public GatewayInfo getGatewayInfo() throws PlugwiseHAException {
        return getGatewayInfo(false);
    }

    public GatewayInfo getGatewayInfo(Boolean forceRefresh) throws PlugwiseHAException {
        GatewayInfo gatewayInfo = null;
        DomainObjects localDomainObjects = this.domainObjects;
        if (localDomainObjects != null) {
            gatewayInfo = localDomainObjects.getGatewayInfo();
        }

        if (!forceRefresh && gatewayInfo != null) {
            this.logger.debug("Found Plugwise Home Automation gateway");
            return gatewayInfo;
        } else {
            PlugwiseHAControllerRequest<DomainObjects> request;

            request = newRequest(DomainObjects.class, this.domainObjectsTransformer);

            request.setPath("/core/domain_objects");
            request.addPathParameter("class", "Gateway");

            DomainObjects domainObjects = executeRequest(request);
            this.gatewayUpdateDateTime = ZonedDateTime.parse(request.getServerDateTime(), PlugwiseHAController.FORMAT);

            return mergeDomainObjects(domainObjects).getGatewayInfo();
        }
    }

    public Appliances getAppliances(Boolean forceRefresh) throws PlugwiseHAException {
        Appliances appliances = null;
        DomainObjects localDomainObjects = this.domainObjects;
        if (localDomainObjects != null) {
            appliances = localDomainObjects.getAppliances();
        }

        if (!forceRefresh && appliances != null) {
            return appliances;
        } else {
            PlugwiseHAControllerRequest<DomainObjects> request;

            request = newRequest(DomainObjects.class, this.domainObjectsTransformer);

            request.setPath("/core/domain_objects");
            request.addPathParameter("class", "Appliance");

            DomainObjects domainObjects = executeRequest(request);
            this.gatewayUpdateDateTime = ZonedDateTime.parse(request.getServerDateTime(), PlugwiseHAController.FORMAT);
            int size = 0;
            if (!(domainObjects.getAppliances() == null)) {
                size = domainObjects.getAppliances().size();
            }
            this.logger.debug("Found {} Plugwise Home Automation appliance(s)", size);

            return mergeDomainObjects(domainObjects).getAppliances();
        }
    }

    public @Nullable Appliance getAppliance(String id) throws PlugwiseHAException {
        Appliances appliances = this.getAppliances(false);
        if (!appliances.containsKey(id)) {
            appliances = this.getAppliances(true);
        }

        if (!appliances.containsKey(id)) {
            this.logger.debug("Plugwise Home Automation Appliance with id {} is not known", id);
            return null;
        } else {
            return appliances.get(id);
        }
    }

    public Locations getLocations(Boolean forceRefresh) throws PlugwiseHAException {
        Locations locations = null;
        DomainObjects localDomainObjects = this.domainObjects;
        if (localDomainObjects != null) {
            locations = localDomainObjects.getLocations();
        }

        if (!forceRefresh && locations != null) {
            return locations;
        } else {
            PlugwiseHAControllerRequest<DomainObjects> request;

            request = newRequest(DomainObjects.class, this.domainObjectsTransformer);

            request.setPath("/core/domain_objects");
            request.addPathParameter("class", "Location");

            DomainObjects domainObjects = executeRequest(request);
            this.gatewayUpdateDateTime = ZonedDateTime.parse(request.getServerDateTime(), PlugwiseHAController.FORMAT);
            int size = 0;
            if (!(domainObjects.getLocations() == null)) {
                size = domainObjects.getLocations().size();
            }
            this.logger.debug("Found {} Plugwise Home Automation Zone(s)", size);
            return mergeDomainObjects(domainObjects).getLocations();
        }
    }

    public @Nullable Location getLocation(String id) throws PlugwiseHAException {
        Locations locations = this.getLocations(false);
        if (!locations.containsKey(id)) {
            locations = this.getLocations(true);
        }

        if (!locations.containsKey(id)) {
            this.logger.debug("Plugwise Home Automation Zone with {} is not known", id);
            return null;
        } else {
            return locations.get(id);
        }
    }

    public @Nullable DomainObjects getDomainObjects() throws PlugwiseHAException {
        PlugwiseHAControllerRequest<DomainObjects> request;

        request = newRequest(DomainObjects.class, this.domainObjectsTransformer);

        request.setPath("/core/domain_objects");
        request.addPathParameter("@locale", "en-US");
        DomainObjects domainObjects = executeRequest(request);

        ZonedDateTime serverTime = ZonedDateTime.parse(request.getServerDateTime(), PlugwiseHAController.FORMAT);
        this.gatewayUpdateDateTime = serverTime;
        this.gatewayFullUpdateDateTime = serverTime;

        return mergeDomainObjects(domainObjects);
    }

    public @Nullable DomainObjects getUpdatedDomainObjects() throws PlugwiseHAException {
        ZonedDateTime localGatewayUpdateDateTime = this.gatewayUpdateDateTime;
        ZonedDateTime localGatewayFullUpdateDateTime = this.gatewayFullUpdateDateTime;

        if (localGatewayUpdateDateTime == null || localGatewayFullUpdateDateTime == null) {
            return getDomainObjects();
        } else if (localGatewayUpdateDateTime.isBefore(ZonedDateTime.now().minusSeconds(maxAgeSecondsRefresh))) {
            return getUpdatedDomainObjects(localGatewayUpdateDateTime);
        } else if (localGatewayFullUpdateDateTime
                .isBefore(ZonedDateTime.now().minusMinutes(MAX_AGE_MINUTES_FULL_REFRESH))) {
            return getDomainObjects();
        } else {
            return null;
        }
    }

    public @Nullable DomainObjects getUpdatedDomainObjects(ZonedDateTime since) throws PlugwiseHAException {
        return getUpdatedDomainObjects(since.toEpochSecond());
    }

    public @Nullable DomainObjects getUpdatedDomainObjects(Long since) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<DomainObjects> request;

        request = newRequest(DomainObjects.class, this.domainObjectsTransformer);

        request.setPath("/core/domain_objects");
        request.addPathFilter("modified_date", "ge", since);
        request.addPathFilter("deleted_date", "ge", "0");
        request.addPathParameter("@memberModifiedDate", since);
        request.addPathParameter("@locale", "en-US");

        DomainObjects domainObjects = executeRequest(request);
        this.gatewayUpdateDateTime = ZonedDateTime.parse(request.getServerDateTime(), PlugwiseHAController.FORMAT);

        return mergeDomainObjects(domainObjects);
    }

    public void setLocationThermostat(Location location, Double temperature) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);
        Optional<ActuatorFunctionality> thermostat = location.getActuatorFunctionalities().getFunctionalityThermostat();

        if (thermostat.isPresent()) {
            request.setPath("/core/locations");

            request.addPathParameter("id", String.format("%s/thermostat", location.getId()));
            request.addPathParameter("id", String.format("%s", thermostat.get().getId()));
            request.setBodyParameter(new ActuatorFunctionalityThermostat(temperature));

            executeRequest(request);
        }
    }

    public void setThermostat(Appliance appliance, Double temperature) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);
        Optional<ActuatorFunctionality> thermostat = appliance.getActuatorFunctionalities()
                .getFunctionalityThermostat();

        if (thermostat.isPresent()) {
            request.setPath("/core/appliances");

            request.addPathParameter("id", String.format("%s/thermostat", appliance.getId()));
            request.addPathParameter("id", String.format("%s", thermostat.get().getId()));
            request.setBodyParameter(new ActuatorFunctionalityThermostat(temperature));

            executeRequest(request);
        }
    }

    public void setOffsetTemperature(Appliance appliance, Double temperature) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);
        Optional<ActuatorFunctionality> offsetTemperatureFunctionality = appliance.getActuatorFunctionalities()
                .getFunctionalityOffsetTemperature();

        if (offsetTemperatureFunctionality.isPresent()) {
            request.setPath("/core/appliances");

            request.addPathParameter("id", String.format("%s/offset", appliance.getId()));
            request.addPathParameter("id", String.format("%s", offsetTemperatureFunctionality.get().getId()));
            request.setBodyParameter(new ActuatorFunctionalityOffsetTemperature(temperature));

            executeRequest(request);
        }
    }

    public void setPreHeating(Location location, Boolean state) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);
        Optional<ActuatorFunctionality> thermostat = location.getActuatorFunctionalities().getFunctionalityThermostat();

        request.setPath("/core/locations");
        request.addPathParameter("id", String.format("%s/thermostat", location.getId()));
        request.addPathParameter("id", String.format("%s", thermostat.get().getId()));
        request.setBodyParameter(new ActuatorFunctionalityThermostat(state, null, null));

        executeRequest(request);
    }

    public void setAllowCooling(Location location, Boolean state) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);
        Optional<ActuatorFunctionality> thermostat = location.getActuatorFunctionalities().getFunctionalityThermostat();

        request.setPath("/core/locations");
        request.addPathParameter("id", String.format("%s/thermostat", location.getId()));
        request.addPathParameter("id", String.format("%s", thermostat.get().getId()));
        request.setBodyParameter(new ActuatorFunctionalityThermostat(null, state, null));

        executeRequest(request);
    }

    public void setRegulationControl(Location location, String state) throws PlugwiseHAException {
        List<String> allowStates = Arrays.asList("active", "passive", "off");
        if (!allowStates.contains(state.toLowerCase())) {
            this.logger.warn("Trying to set the regulation control to an invalid state");
            return;
        }

        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);
        Optional<ActuatorFunctionality> thermostat = location.getActuatorFunctionalities().getFunctionalityThermostat();

        request.setPath("/core/locations");
        request.addPathParameter("id", String.format("%s/thermostat", location.getId()));
        request.addPathParameter("id", String.format("%s", thermostat.get().getId()));
        request.setBodyParameter(new ActuatorFunctionalityThermostat(null, null, state));

        executeRequest(request);
    }

    public void setRelay(Appliance appliance, Boolean state) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);

        request.setPath("/core/appliances");
        request.addPathParameter("id", String.format("%s/relay", appliance.getId()));
        request.setBodyParameter(new ActuatorFunctionalityRelay(state ? "on" : "off"));

        executeRequest(request);
    }

    public void setRelayLock(Appliance appliance, Boolean state) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);

        request.setPath("/core/appliances");
        request.addPathParameter("id", String.format("%s/relay", appliance.getId()));
        request.setBodyParameter(new ActuatorFunctionalityRelay(null, state));

        executeRequest(request);
    }

    public void setPresetScene(Location location, String state) throws PlugwiseHAException {
        List<String> allowStates = Arrays.asList("home", "asleep", "away", "vacation", "no_frost");
        if (!allowStates.contains(state.toLowerCase())) {
            this.logger.warn("Trying to set the preset scene to an invalid state");
            return;
        }

        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);

        request.setPath("/core/locations");
        request.addPathParameter("id", String.format("%s", location.getId()));

        Location locationWithChangesOnly = new Location();
        locationWithChangesOnly.setPreset(state);
        locationWithChangesOnly.setId(location.getId());

        LocationsArray locations = new LocationsArray();
        locations.items = new Location[] { locationWithChangesOnly };

        request.setBodyParameter(locations);

        executeRequest(request);
    }

    public ZonedDateTime ping() throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request;

        request = newRequest(Void.class, null);

        request.setPath("/cache/gateways");
        request.addPathParameter("ping");

        executeRequest(request);

        return ZonedDateTime.parse(request.getServerDateTime(), PlugwiseHAController.FORMAT);
    }

    // Protected and private methods

    private static Transformer setXSLT(StreamSource xsltSource) throws PlugwiseHAException {
        try {
            return TransformerFactory.newInstance().newTransformer(xsltSource);
        } catch (TransformerConfigurationException e) {
            throw new PlugwiseHAException("Could not create XML transformer", e);
        }
    }

    private <T> PlugwiseHAControllerRequest<T> newRequest(Class<T> responseType, @Nullable Transformer transformer) {
        return new PlugwiseHAControllerRequest<T>(responseType, this.xStream, transformer, this.httpClient, this.host,
                this.port, this.username, this.smileId);
    }

    private <T> PlugwiseHAControllerRequest<T> newRequest(Class<T> responseType) {
        return new PlugwiseHAControllerRequest<T>(responseType, this.xStream, null, this.httpClient, this.host,
                this.port, this.username, this.smileId);
    }

    @SuppressWarnings("null")
    private <T> T executeRequest(PlugwiseHAControllerRequest<T> request) throws PlugwiseHAException {
        T result;
        result = request.execute();
        return result;
    }

    private DomainObjects mergeDomainObjects(@Nullable DomainObjects updatedDomainObjects) {
        DomainObjects localDomainObjects = this.domainObjects;
        if (localDomainObjects == null && updatedDomainObjects != null) {
            this.domainObjects = updatedDomainObjects;
            return updatedDomainObjects;
        } else if (localDomainObjects != null && updatedDomainObjects == null) {
            return localDomainObjects;
        } else if (localDomainObjects != null && updatedDomainObjects != null) {
            Appliances appliances = updatedDomainObjects.getAppliances();
            Locations locations = updatedDomainObjects.getLocations();

            if (appliances != null) {
                localDomainObjects.mergeAppliances(appliances);
            }

            if (locations != null) {
                localDomainObjects.mergeLocations(locations);
            }
            this.domainObjects = localDomainObjects;
            return localDomainObjects;
        } else {
            return new DomainObjects();
        }
    }
}
