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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.scalarweb.gson.SupportedApiDeserializer;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebRequest;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.transports.SonyTransport;
import org.slf4j.Logger;

/**
 * This class represents all the supported APIs that a service provides and is used for serialization and
 * deserialization via {@link SupportedApiDeserializer}.
 * 
 * Note: if a service name is unknown/illegal (such as
 * <av:X_ScalarWebAPI_ServiceType>dial/dd.xml</av:X_ScalarWebAPI_ServiceType>), we need to catch
 * IllegalArgumentException (which is thrown in the 'as' methods) to prevent the illegal service from keeping the thing
 * from going online.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SupportedApi {
    /** The map of APIs to the api name (unmodifiable) */
    private final Map<String, SupportedApiInfo> apis;

    /** The map of notifications to notification name (unmodifiable) */
    private final Map<String, SupportedApiInfo> notifications;

    /** The set of protocols the service supports (unmodifiable) */
    private final Set<String> protocols;

    /** The service name */
    private final String service;

    /**
     * Constructs the supported API from the parameters
     * 
     * @param service a non-null, non-empty service name
     * @param apis a non-null, possibly empty list of supported apis
     * @param notifications a non-null, possibly empty list of notifications
     * @param protocols a non-null, possibly empty set of protocols
     */
    public SupportedApi(final String service, final List<SupportedApiInfo> apis,
            final List<SupportedApiInfo> notifications, final Set<String> protocols) {
        Validate.notEmpty(service, "service cannot be empty");
        Objects.requireNonNull(apis, "apis cannot be null");
        Objects.requireNonNull(notifications, "notifications cannot be null");
        Objects.requireNonNull(protocols, "protocols cannot be null");

        this.service = service;
        this.apis = Collections.unmodifiableMap(apis.stream().collect(Collectors.toMap(k -> k.getName(), v -> v)));
        this.notifications = Collections
                .unmodifiableMap(notifications.stream().collect(Collectors.toMap(k -> k.getName(), v -> v)));
        this.protocols = Collections.unmodifiableSet(protocols);
    }

    /**
     * Returns the service name
     * 
     * @return the non-null, non-empty service name
     */
    public String getService() {
        return service;
    }

    /**
     * Returns the set of protocols supported
     * 
     * @return a non-null, possibly empty unmodifiable set of protocols
     */
    public Set<String> getProtocols() {
        return protocols;
    }

    /**
     * Returns a collection of APIs supported
     * 
     * @return a non-null, possibly empty unmodifiable collection of apis
     */
    public Collection<SupportedApiInfo> getApis() {
        return apis.values();
    }

    /**
     * Returns a collection of notifications supported
     * 
     * @return a non-null, possibly empty unmodifiable collection of notifications
     */
    public Collection<SupportedApiInfo> getNotifications() {
        return notifications.values();
    }

    /**
     * Returns the API for a given method name
     * 
     * @param methodName a non-null, non empty method name
     * @return the supported API or null if none found
     */
    public @Nullable SupportedApiInfo getMethod(final String methodName) {
        Validate.notEmpty(methodName, "methodName cannot be null");
        return apis.get(methodName);
    }

    /**
     * Returns the set of protocols for a given method/version
     * 
     * @param methodName a non-null, non-empty method name
     * @param version a non-null, non-empty version
     * @return a non-null, possibly empty set of protocols
     */
    public Set<String> getProtocols(final String methodName, final String version) {
        Validate.notEmpty(methodName, "methodName cannot be null");
        Validate.notEmpty(version, "version cannot be null");

        final SupportedApiInfo info = apis.get(methodName);
        final SupportedApiVersionInfo vers = info == null ? null : info.getVersions(version);
        final Set<String> mthdProtocols = vers == null ? null : vers.getProtocols();
        return mthdProtocols != null && !mthdProtocols.isEmpty() ? mthdProtocols : protocols;
    }

    /**
     * A helper method to retrieve the supported API for a given service and transport
     * 
     * @param service a non-null service to use
     * @param serviceName a non-null, non-empty service name to query (may be different from service)
     * @param transport a non-null transport to use in retrieving the API
     * @param logger a non-null logger to log messasge
     * @return a non-null supported API
     */
    public static SupportedApi getSupportedApi(final ScalarWebService service, final String serviceName,
            final SonyTransport transport, final Logger logger) {
        Objects.requireNonNull(service, "service cannot be null");
        Validate.notEmpty(serviceName, "serviceName cannot be empty");
        Objects.requireNonNull(transport, "transport cannot be null");
        Objects.requireNonNull(logger, "logger cannot be null");

        final SupportedApi api = getSupportApi(service, serviceName, logger);
        return api == null ? getSupportApiAlternate(serviceName, transport, logger) : api;
    }

    /**
     * Helper method to get the supported API from teh service and service name
     * 
     * @param service a non-null service to use
     * @param serviceName a non-null, non-empty servicename to use
     * @param logger a non-null logger to log message to.
     * @return the supported API or null if not found
     */
    private static @Nullable SupportedApi getSupportApi(final ScalarWebService service, final String serviceName,
            final Logger logger) {
        Objects.requireNonNull(service, "service cannot be null");
        Validate.notEmpty(serviceName, "serviceName cannot be empty");
        Objects.requireNonNull(logger, "logger cannot be null");

        try {
            return service.execute(ScalarWebMethod.GETSUPPORTEDAPIINFO, new SupportedApiServices(serviceName))
                    .as(SupportedApi.class);
        } catch (final IOException | IllegalArgumentException e) {
            logger.trace("Exception getting supported api info: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Helper method to get the 'alternative' supported api from a servicename and transport. The 'alternative'
     * supported API is created from the getversions/getmethodtypes calls rather than the native getsupportedapi call.
     * 
     * @param serviceName a non-null, non-empty service name
     * @param transport a non-null transport
     * @param logger a non-null logger to log messages with
     * @return the supported api based on getversions/getmethodtypes calls
     */
    public static SupportedApi getSupportApiAlternate(final String serviceName, final SonyTransport transport,
            final Logger logger) {
        Validate.notEmpty(serviceName, "serviceName cannot be empty");
        Objects.requireNonNull(transport, "transport cannot be null");
        Objects.requireNonNull(logger, "logger cannot be null");

        final List<ScalarWebMethod> methods = new ArrayList<>();
        try {
            // Retrieve the versions for the service
            final List<String> versionResult = transport
                    .execute(new ScalarWebRequest(ScalarWebMethod.GETVERSIONS, ScalarWebMethod.V1_0))
                    .asArray(String.class);

            // For each version, retrieve the methods for the service
            for (final String apiVersion : versionResult) {
                final MethodTypes mtdResults = transport
                        .execute(new ScalarWebRequest(ScalarWebMethod.GETMETHODTYPES, ScalarWebMethod.V1_0, apiVersion))
                        .as(MethodTypes.class);
                methods.addAll(mtdResults.getMethods());
            }
        } catch (final IOException | IllegalArgumentException e) {
            logger.debug("Could not retrieve methods: {}", e.getMessage(), e);
        }

        final Map<String, Set<String>> mthdVersions = new HashMap<>();
        methods.stream().forEach(m -> {
            final String mthdName = m.getMethodName();
            Set<String> versions = mthdVersions.get(mthdName);
            if (versions == null) {
                versions = new HashSet<>();
                mthdVersions.put(mthdName, versions);
            }
            versions.add(m.getVersion());
        });

        final List<SupportedApiInfo> apis = mthdVersions.entrySet().stream()
                .map(e -> new SupportedApiInfo(e.getKey(),
                        e.getValue().stream().map(v -> new SupportedApiVersionInfo(v)).collect(Collectors.toList())))
                .collect(Collectors.toList());

        final Set<String> protocols = new HashSet<>();
        protocols.add(transport.getProtocolType());

        final List<SupportedApiInfo> notifications = new ArrayList<>();
        if (mthdVersions.containsKey(ScalarWebMethod.SWITCHNOTIFICATIONS)) {
            try {
                final Notifications ntfs = transport
                        .execute(new ScalarWebRequest(ScalarWebMethod.SWITCHNOTIFICATIONS, "1.0", new Notifications()))
                        .as(Notifications.class);
                Stream.concat(ntfs.getEnabled().stream(), ntfs.getDisabled().stream()).map(n -> {
                    final String version = n.getVersion();
                    final String name = n.getName();
                    return name == null || StringUtils.isEmpty(name) || version == null || StringUtils.isEmpty(version)
                            ? null
                            : new SupportedApiInfo(name, Arrays.asList(new SupportedApiVersionInfo(version)));
                }).filter(n -> n != null).forEachOrdered(notifications::add);
            } catch (final IOException | IllegalArgumentException e) {
                logger.debug("Exception getting notifications for service {}: {}", serviceName, e.getMessage());
            }
        } else {
            logger.debug("Service {} doesn't contain {} - ignoring notifications", serviceName,
                    ScalarWebMethod.SWITCHNOTIFICATIONS);
        }

        return new SupportedApi(serviceName, apis, notifications, protocols);
    }

    @Override
    public String toString() {
        return "SupportedApi [apis=" + apis + ", notifications=" + notifications + ", protocols=" + protocols
                + ", service=" + service + "]";
    }
}
