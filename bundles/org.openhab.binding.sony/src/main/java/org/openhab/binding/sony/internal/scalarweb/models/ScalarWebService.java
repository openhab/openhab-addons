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
package org.openhab.binding.sony.internal.scalarweb.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.sony.internal.scalarweb.models.api.MethodTypes;
import org.openhab.binding.sony.internal.scalarweb.models.api.ServiceProtocol;
import org.openhab.binding.sony.internal.scalarweb.models.api.SupportedApi;
import org.openhab.binding.sony.internal.scalarweb.models.api.SupportedApiInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.SupportedApiVersionInfo;
import org.openhab.binding.sony.internal.transports.SonyTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.openhab.binding.sony.internal.transports.TransportOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the different web services available
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebService implements AutoCloseable {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ScalarWebService.class);

    // The various well know service names (must be unique as they are channel groups)
    public static final String ACCESSCONTROL = "accessControl";
    public static final String APPCONTROL = "appControl";
    public static final String AUDIO = "audio";
    public static final String AVCONTENT = "avContent";
    public static final String BROWSER = "browser";
    public static final String CEC = "cec";
    public static final String CONTENTSHARE = "contentshare";
    public static final String ENCRYPTION = "encryption";
    public static final String GUIDE = "guide";
    public static final String ILLUMINATION = "illumination";
    public static final String SYSTEM = "system";
    public static final String VIDEO = "video";
    public static final String VIDEOSCREEN = "videoScreen";

    // These are (undocumented) services that I haven't figured out yet
    public static final String NOTIFICATION = "notification";
    public static final String RECORDING = "recording";

    // The various know services to their labels
    private static final Map<String, String> SERVICELABELS = Collections.unmodifiableMap(new HashMap<String, String>() {
        private static final long serialVersionUID = 5934100497468165317L;
        {
            put(ACCESSCONTROL, labelFor(ACCESSCONTROL));
            put(APPCONTROL, labelFor(APPCONTROL));
            put(AUDIO, labelFor(AUDIO));
            put(AVCONTENT, labelFor(AVCONTENT));
            put(BROWSER, labelFor(BROWSER));
            put(CEC, labelFor(CEC));
            put(CONTENTSHARE, labelFor(CONTENTSHARE));
            put(ENCRYPTION, labelFor(ENCRYPTION));
            put(GUIDE, labelFor(GUIDE));
            put(ILLUMINATION, labelFor(ILLUMINATION));
            put(SYSTEM, labelFor(SYSTEM));
            put(VIDEO, labelFor(VIDEO));
            put(VIDEOSCREEN, labelFor(VIDEOSCREEN));
        }
    });

    /** The service name */
    private final String serviceName;

    /** The service version */
    private final String version;

    /** Transport Factory */
    private final SonyTransportFactory transportFactory;

    /** Transport used for communication */
    private final SonyTransport transport;

    /** The API supported by this service */
    private final SupportedApi supportedApi;

    /**
     * Instantiates a new scalar web service.
     *
     * @param transportFactory the non-null transport factory to use
     * @param serviceProtocol the non-null service protocol to use
     * @param version the non-null, non-empty service version
     * @param supportedApi the non-null supported api
     */
    public ScalarWebService(final SonyTransportFactory transportFactory, final ServiceProtocol serviceProtocol,
            final String version, final SupportedApi supportedApi) {
        Objects.requireNonNull(transportFactory, "transportFactory cannot be null");
        Objects.requireNonNull(serviceProtocol, "serviceProtocol cannot be null");
        Validate.notEmpty(version, "version cannot be empty");
        Objects.requireNonNull(supportedApi, "supportedApi cannot be null");

        this.transportFactory = transportFactory;
        this.serviceName = serviceProtocol.getServiceName();
        this.version = version;
        this.supportedApi = supportedApi;

        final SonyTransport transport = transportFactory.getSonyTransport(serviceProtocol);
        if (transport == null) {
            throw new IllegalArgumentException("No transport found for " + serviceProtocol);
        }
        this.transport = transport;
    }

    /**
     * Retrieves the methods for this service
     * 
     * @return a non-null, possibly empty list of {@link ScalarWebMethod}
     */
    public List<ScalarWebMethod> getMethods() {
        final Set<String> versions = new HashSet<>();
        versions.add(ScalarWebMethod.V1_0);

        final List<ScalarWebMethod> methods = new ArrayList<>();
        try {
            // Retrieve the api versions for the service
            versions.addAll(execute(new ScalarWebRequest(ScalarWebMethod.GETVERSIONS, version)).asArray(String.class));
        } catch (final IOException e) {
            if (StringUtils.contains(e.getMessage(), String.valueOf(HttpStatus.NOT_FOUND_404))) {
                logger.debug("Could not retrieve method versions - missing method {}: {}", ScalarWebMethod.GETVERSIONS,
                        e.getMessage());
            } else {
                logger.debug("Could not retrieve methods versions: {}", e.getMessage(), e);
            }
        }

        // For each version, retrieve the methods for the service
        for (final String apiVersion : versions) {
            try {
                final MethodTypes mtdResults = execute(
                        new ScalarWebRequest(ScalarWebMethod.GETMETHODTYPES, version, apiVersion))
                                .as(MethodTypes.class);
                methods.addAll(mtdResults.getMethods());
            } catch (final IOException e) {
                logger.debug("Could not retrieve {} vers {}: {}", ScalarWebMethod.GETMETHODTYPES, apiVersion,
                        e.getMessage());
            }
        }

        // Merge in any methods reported that weren't returned by getmethodtypes/version
        supportedApi.getApis().forEach(api -> {
            api.getVersions().forEach(v -> {
                if (!methods.stream().anyMatch(m -> StringUtils.equalsIgnoreCase(m.getMethodName(), api.getName())
                        && StringUtils.equalsIgnoreCase(v.getVersion(), m.getVersion()))) {
                    methods.add(new ScalarWebMethod(api.getName(), new ArrayList<>(), new ArrayList<>(), v.getVersion(),
                            ScalarWebMethod.UNKNOWN_VARIATION));
                }
            });
        });
        return methods;
    }

    /**
     * Gets the list of notifications for the service
     * 
     * @return a non-null, possibly empty list of {@link ScalarWebMethod}
     */
    public List<ScalarWebMethod> getNotifications() {
        final List<ScalarWebMethod> notifications = new ArrayList<>();
        // add in any supported api that has no match above (shouldn't really be any but we are being complete)
        // don't use unknown variant since that will prevent change detection in github implementation
        supportedApi.getNotifications().forEach(api -> {
            api.getVersions().forEach(v -> {
                notifications.add(
                        new ScalarWebMethod(api.getName(), new ArrayList<>(), new ArrayList<>(), v.getVersion(), 0));
            });
        });
        return notifications;
    }

    /**
     * Gets the latest version for method name
     *
     * @param methodName the non-null, non-empty method name
     * @return the latest version or null if not found
     */
    public @Nullable String getVersion(final String methodName) {
        Validate.notEmpty(methodName, "methodName cannto be empty");
        final SupportedApiInfo api = supportedApi.getMethod(methodName);
        final SupportedApiVersionInfo vers = api == null ? null : api.getLatestVersion();
        return vers == null ? null : vers.getVersion();
    }

    /**
     * Gets all the versions for a given method
     *
     * @param methodName the non-null, non-empty method name
     * @return the non-null, possibly empty list of versions
     */
    public List<String> getVersions(final String methodName) {
        Validate.notEmpty(methodName, "methodName cannto be empty");
        final SupportedApiInfo api = supportedApi.getMethod(methodName);
        return api == null ? new ArrayList<>()
                : api.getVersions().stream().map(v -> v.getVersion()).collect(Collectors.toList());
    }

    /**
     * Determines if the method name exists in the service
     *
     * @param methodName the non-null, non-empty method name
     * @return true if it exists, false otherwise
     */
    public boolean hasMethod(final String methodName) {
        Validate.notEmpty(methodName, "methodName cannto be empty");
        return supportedApi.getMethod(methodName) != null;
    }

    /**
     * Gets the service name
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the service version
     *
     * @return the service version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the transport related to this service
     *
     * @return the non-null sony transport
     */
    public SonyTransport getTransport() {
        return transport;
    }

    /**
     * Executes the latest method version using the specified parameters
     *
     * @param methodName the method name
     * @param parms the parameters to use
     * @return the scalar web result
     */
    public ScalarWebResult execute(final String methodName, final Object... parms) {
        Validate.notEmpty(methodName, "methodName cannot be empty");
        return executeSpecific(methodName, null, parms);
    }

    /**
     * Executes a specific method/version using the specified parameters
     *
     * @param methodName the method name
     * @param version the possibly null, possibly empty version (null/empty to use latest version)
     * @param parms the parameters to use
     * @return the scalar web result
     */
    public ScalarWebResult executeSpecific(final String methodName, final @Nullable String version,
            final Object... parms) {
        Validate.notEmpty(methodName, "methodName cannot be empty");

        if (version == null || StringUtils.isEmpty(version)) {
            final String mtdVersion = getVersion(methodName);
            if (mtdVersion == null) {
                logger.debug("Method {} doesn't exist in the service {}", methodName, serviceName);
                return ScalarWebResult.createNotImplemented(methodName);
            }
            return execute(new ScalarWebRequest(methodName, mtdVersion, parms));
        } else {
            return execute(new ScalarWebRequest(methodName, version, parms));
        }
    }

    /**
     * Execute the specified request with the specified options
     *
     * @param request the non-null request to execute
     * @param options the possibly not specified options to use the execution with
     * @return the scalar web result
     */
    public ScalarWebResult execute(final ScalarWebRequest request, final TransportOption... options) {
        Objects.requireNonNull(request, "request cannot be null");

        final Set<String> protocols = supportedApi.getProtocols(request.getMethod(), request.getVersion());
        if (protocols.contains(transport.getProtocolType())) {
            return transport.execute(request, options);
        } else {
            final ServiceProtocol serviceProtocol = new ServiceProtocol(serviceName, protocols);
            try (final SonyTransport mthdTransport = transportFactory.getSonyTransport(serviceProtocol)) {
                if (mthdTransport == null) {
                    logger.debug("No transport for {} with protocols: {}", request, protocols);
                    return new ScalarWebResult(HttpStatus.INTERNAL_SERVER_ERROR_500,
                            "No transport for " + request + " with protocols: " + protocols);
                } else {
                    logger.debug("Execution of {} is using a different protocol {} than the service {}", request,
                            mthdTransport.getProtocolType(), transport.getProtocolType());
                    return mthdTransport.execute(request, options);
                }
            }
        }
    }

    /**
     * Returns the label for a given service name
     * 
     * @param serviceName a non-null, non-empty service name
     * @return the label for the service
     */
    private static String labelFor(final String serviceName) {
        Validate.notEmpty(serviceName, "serviceName cannot be empty");
        switch (serviceName) {
            case ACCESSCONTROL:
                return "Access Control";
            case APPCONTROL:
                return "Application Control";
            case AUDIO:
                return "Audio";
            case AVCONTENT:
                return "A/V Content";
            case BROWSER:
                return "Browser";
            case CEC:
                return "CEC";
            case CONTENTSHARE:
                return "Content Share";
            case ENCRYPTION:
                return "Encryption";
            case GUIDE:
                return "Guide";
            case ILLUMINATION:
                return "Illumination";
            case SYSTEM:
                return "System";
            case VIDEO:
                return "Video";
            case VIDEOSCREEN:
                return "Video Screen";
            default:
                return serviceName;
        }
    }

    /**
     * Returns a map of service names to their label
     * 
     * @return a non-null, non-empty map of service names to service labels
     */
    public static Map<String, String> getServiceLabels() {
        return SERVICELABELS;
    }

    @Override
    public void close() {
        transport.close();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(100);
        final String newLine = java.lang.System.lineSeparator();

        sb.append("Service: ");
        sb.append(serviceName);
        sb.append(newLine);

        for (final ScalarWebMethod mthd : getMethods().stream()
                .sorted(Comparator.comparing(ScalarWebMethod::getMethodName)).collect(Collectors.toList())) {
            sb.append("   ");
            sb.append(mthd);
            sb.append(newLine);
        }

        return sb.toString();
    }
}
