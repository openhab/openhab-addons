/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.openhab.binding.plugwiseha.internal.api.model.object.ActuatorFunctionality;
import org.openhab.binding.plugwiseha.internal.api.model.object.ActuatorFunctionalityRelay;
import org.openhab.binding.plugwiseha.internal.api.model.object.ActuatorFunctionalityThermostat;
import org.openhab.binding.plugwiseha.internal.api.model.object.Appliance;
import org.openhab.binding.plugwiseha.internal.api.model.object.Appliances;
import org.openhab.binding.plugwiseha.internal.api.model.object.DomainObjects;
import org.openhab.binding.plugwiseha.internal.api.model.object.GatewayInfo;
import org.openhab.binding.plugwiseha.internal.api.model.object.Location;
import org.openhab.binding.plugwiseha.internal.api.model.object.Locations;
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

    private final static int MAX_AGE_MINUTES_REFRESH = 10;
    private final static int MAX_AGE_MINUTES_FULL_REFRESH = 30;
    private final static DateTimeFormatter FORMAT = DateTimeFormatter.RFC_1123_DATE_TIME; // default Date format that
                                                                                          // will be used in conversion

    private final Logger logger = LoggerFactory.getLogger(PlugwiseHAController.class);

    private final HttpClient httpClient;
    private final PlugwiseHAXStream xStream;
    private final Transformer domainObjectsTransformer;

    private final String host;
    private final int port;
    private final String username;
    private final String smileId;

    private @Nullable ZonedDateTime gatewayUpdateDateTime;
    private @Nullable ZonedDateTime gatewayFullUpdateDateTime;
    private @Nullable DomainObjects domainObjects;

    public PlugwiseHAController(HttpClient httpClient, String host, int port, String username, String smileId)
            throws PlugwiseHAException {
        this.httpClient = httpClient;
        this.host = host;
        this.port = port;
        this.username = username;
        this.smileId = smileId;

        this.xStream = new PlugwiseHAXStream();
        this.domainObjectsTransformer = PlugwiseHAController
                .setXSLT(new StreamSource(getClass().getClassLoader().getResourceAsStream("domain_objects.xslt")));
    }

    // Public methods

    public void start(Runnable callback) throws PlugwiseHAException {
        refresh();
        callback.run();
    }

    public void stop() {
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
        GatewayInfo gatewayInfo = this.domainObjects.getGatewayInfo();

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

    public Appliances getAppliances() throws PlugwiseHAException {
        return getAppliances(false);
    }

    public Appliances getAppliances(Boolean forceRefresh) throws PlugwiseHAException {
        Appliances appliances = this.domainObjects.getAppliances();

        if (!forceRefresh && appliances != null) {
            this.logger.debug("Found {} Plugwise Home Automation appliance(s)", appliances.size());
            return appliances;
        } else {
            PlugwiseHAControllerRequest<DomainObjects> request;

            request = newRequest(DomainObjects.class, this.domainObjectsTransformer);

            request.setPath("/core/domain_objects");
            request.addPathParameter("class", "Appliance");

            DomainObjects domainObjects = executeRequest(request);
            this.gatewayUpdateDateTime = ZonedDateTime.parse(request.getServerDateTime(), PlugwiseHAController.FORMAT);

            return mergeDomainObjects(domainObjects).getAppliances();
        }
    }

    public Appliance getAppliance(String id) {
        return this.domainObjects.getAppliances().get(id);
    }

    public Locations getLocations() throws PlugwiseHAException {
        return getLocations(false);
    }

    public Locations getLocations(Boolean forceRefresh) throws PlugwiseHAException {
        Locations locations = this.domainObjects.getLocations();

        if (!forceRefresh && locations != null) {
            this.logger.debug("Found {} Plugwise Home Automation location(s)", locations.size());
            return locations;
        } else {
            PlugwiseHAControllerRequest<DomainObjects> request;

            request = newRequest(DomainObjects.class, this.domainObjectsTransformer);

            request.setPath("/core/domain_objects");
            request.addPathParameter("class", "Location");

            DomainObjects domainObjects = executeRequest(request);
            this.gatewayUpdateDateTime = ZonedDateTime.parse(request.getServerDateTime(), PlugwiseHAController.FORMAT);

            return mergeDomainObjects(domainObjects).getLocations();
        }
    }

    public Location getLocation(String id) {
        return this.domainObjects.getLocations().get(id);
    }

    public @Nullable DomainObjects getDomainObjects() throws PlugwiseHAException {
        PlugwiseHAControllerRequest<DomainObjects> request;

        request = newRequest(DomainObjects.class, this.domainObjectsTransformer);

        request.setPath("/core/domain_objects");
        request.addPathParameter("@locale", "en-US");

        DomainObjects domainObjects = executeRequest(request);
        this.gatewayUpdateDateTime = ZonedDateTime.parse(request.getServerDateTime(), PlugwiseHAController.FORMAT);
        this.gatewayFullUpdateDateTime = this.gatewayUpdateDateTime;

        return mergeDomainObjects(domainObjects);
    }

    public @Nullable DomainObjects getUpdatedDomainObjects() throws PlugwiseHAException {
        if (this.gatewayUpdateDateTime == null || this.gatewayFullUpdateDateTime == null
                || this.gatewayUpdateDateTime.isBefore(ZonedDateTime.now().minusMinutes(MAX_AGE_MINUTES_REFRESH))
                || this.gatewayFullUpdateDateTime
                        .isBefore(ZonedDateTime.now().minusMinutes(MAX_AGE_MINUTES_FULL_REFRESH))) {
            return getDomainObjects();
        } else {
            return getUpdatedDomainObjects(this.gatewayUpdateDateTime);
        }
    }

    public @Nullable DomainObjects getUpdatedDomainObjects(@Nullable ZonedDateTime since) throws PlugwiseHAException {
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

    public void switchRelay(Appliance appliance, String state) throws PlugwiseHAException {
        List<String> allowStates = Arrays.asList("on", "off");
        if (allowStates.contains(state.toLowerCase())) {
            if (state.toLowerCase().equals("on")) {
                switchRelayOn(appliance);
            } else {
                switchRelayOff(appliance);
            }
        }
    }

    public void switchRelayOn(Appliance appliance) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);
        // Boolean relayLockState = appliance.getRelayLockState().orElse(null);

        request.setPath("/core/appliances");
        request.addPathParameter("id", String.format("%s/relay", appliance.getId()));
        request.setBodyParameter(new ActuatorFunctionalityRelay("on"));
        // request.setBodyParameter(new ActuatorFunctionalityRelay("on", relayLockState));

        executeRequest(request);
    }

    public void switchRelayOff(Appliance appliance) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);
        // Boolean relayLockState = appliance.getRelayLockState().orElse(null);

        request.setPath("/core/appliances");
        request.addPathParameter("id", String.format("%s/relay", appliance.getId()));
        request.setBodyParameter(new ActuatorFunctionalityRelay("off"));
        // request.setBodyParameter(new ActuatorFunctionalityRelay("off", relayLockState));

        executeRequest(request);
    }

    public void switchRelayLock(Appliance appliance, String state) throws PlugwiseHAException {
        List<String> allowStates = Arrays.asList("on", "off");
        if (allowStates.contains(state.toLowerCase())) {
            if (state.toLowerCase().equals("on")) {
                switchRelayLockOn(appliance);
            } else {
                switchRelayLockOff(appliance);
            }
        }
    }

    public void switchRelayLockOff(Appliance appliance) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);

        request.setPath("/core/appliances");
        request.addPathParameter("id", String.format("%s/relay", appliance.getId()));
        request.setBodyParameter(new ActuatorFunctionalityRelay(null, false));

        executeRequest(request);
    }

    public void switchRelayLockOn(Appliance appliance) throws PlugwiseHAException {
        PlugwiseHAControllerRequest<Void> request = newRequest(Void.class);

        request.setPath("/core/appliances");
        request.addPathParameter("id", String.format("%s/relay", appliance.getId()));
        request.setBodyParameter(new ActuatorFunctionalityRelay(null, true));

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

    private <T> T executeRequest(PlugwiseHAControllerRequest<T> request) throws PlugwiseHAException {
        T result;
        result = request.execute();
        return result;
    }

    private @Nullable DomainObjects mergeDomainObjects(@Nullable DomainObjects domainObjects) {
        if (this.domainObjects != null) {
            Appliances appliances = domainObjects.getAppliances();
            Locations locations = domainObjects.getLocations();

            if (appliances != null) {
                this.domainObjects.mergeAppliances(appliances);
            }

            if (locations != null) {
                this.domainObjects.mergeLocations(locations);
            }
        } else {
            this.domainObjects = domainObjects;
        }

        return this.domainObjects;
    }
}