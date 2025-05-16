/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tado.internal.handler;

import static org.openhab.binding.tado.internal.TadoBindingConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.discovery.TadoDiscoveryService;
import org.openhab.binding.tado.internal.servlet.TadoAuthenticationServlet;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.DeviceCodeResponseDTO;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TadoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dennis Frommknecht - Initial contribution
 * @author Andrew Fiddian-Green - OAuth RFC18628 authentication
 */
@NonNullByDefault
@Component(configurationPid = "binding.tado", service = ThingHandlerFactory.class)
public class TadoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_HOME, THING_TYPE_ZONE,
            THING_TYPE_MOBILE_DEVICE);

    // tado specific RFC-8628 oAuth authentication parameters
    private static final String OAUTH_DEVICE_URL = "https://login.tado.com/oauth2/device_authorize";
    private static final String OAUTH_TOKEN_URL = "https://login.tado.com/oauth2/token";
    private static final String OAUTH_CLIENT_ID = "1bb50063-6b0c-4d11-bd99-387f4a91cc46";
    private static final String OAUTH_SCOPE = "offline_access";

    private final Logger logger = LoggerFactory.getLogger(TadoHandlerFactory.class);
    private final Set<TadoHomeHandler> oAuthClientServiceSubscribers = new HashSet<>();
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final Map<String, OAuthClientService> oAuthClientServices = new ConcurrentHashMap<>();

    private final TadoStateDescriptionProvider stateDescriptionProvider;
    private final HttpService httpService;
    private final OAuthFactory oAuthFactory;
    private final TadoAuthenticationServlet httpServlet;

    @Activate
    public TadoHandlerFactory(@Reference TadoStateDescriptionProvider stateDescriptionProvider,
            @Reference HttpService httpService, @Reference OAuthFactory oAuthFactory) {
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.httpService = httpService;
        this.oAuthFactory = oAuthFactory;
        this.httpServlet = new TadoAuthenticationServlet(this);
    }

    @Deactivate
    public void deactivate() {
        oAuthClientServices.keySet().forEach(id -> oAuthFactory.ungetOAuthService(id));
        oAuthClientServices.clear();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_HOME)) {
            TadoHomeHandler tadoHomeHandler = new TadoHomeHandler((Bridge) thing, this);
            registerTadoDiscoveryService(tadoHomeHandler);
            return tadoHomeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_ZONE)) {
            return new TadoZoneHandler(thing, stateDescriptionProvider);
        } else if (thingTypeUID.equals(THING_TYPE_MOBILE_DEVICE)) {
            return new TadoMobileDeviceHandler(thing);
        }

        return null;
    }

    private synchronized void registerTadoDiscoveryService(TadoHomeHandler tadoHomeHandler) {
        TadoDiscoveryService discoveryService = new TadoDiscoveryService(tadoHomeHandler);
        ServiceRegistration<?> serviceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<>());
        discoveryService.activate();
        this.discoveryServiceRegs.put(tadoHomeHandler.getThing().getUID(), serviceRegistration);
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof TadoHomeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                TadoDiscoveryService service = (TadoDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (service != null) {
                    service.deactivate();
                }
            }
        }
    }

    /**
     * A {@link TadoHomeHandler} calls this to subscribe to an {@link OAuthClientService}.
     * Note: that TadoHomeHandlers which do not require a OAuthClientService do not call this.
     * Retrieves the pre-existing {@link OAuthClientService} if present, or creates a new one.
     * If necessary also registers the {@link TadoAuthenticationServlet}.
     *
     * @param tadoHomeHandler the subscribing thing handler
     * @param user the optional user name (may be null)
     * @return an {@link OAuthClientService}
     */
    public OAuthClientService subscribeOAuthClientService(TadoHomeHandler tadoHomeHandler, @Nullable String user) {
        if (oAuthClientServiceSubscribers.isEmpty()) {
            try {
                httpService.registerServlet(TadoAuthenticationServlet.PATH, httpServlet, null, null);
            } catch (ServletException | NamespaceException e) {
                logger.debug("subscribeOAuthClientService() error {}", e.getMessage(), e);
            }
        }

        oAuthClientServiceSubscribers.add(tadoHomeHandler);

        OAuthClientService oAuthClientService = oAuthClientServices.get(getServiceId(user));
        if (oAuthClientService == null) {
            oAuthClientService = oAuthFactory.getOAuthClientService(getServiceId(user));
            if (oAuthClientService != null) {
                oAuthClientServices.put(getServiceId(user), oAuthClientService);
            }
        }

        if (oAuthClientService == null) {
            oAuthClientService = oAuthFactory.createOAuthClientService(getServiceId(user), OAUTH_TOKEN_URL,
                    OAUTH_DEVICE_URL, OAUTH_CLIENT_ID, null, OAUTH_SCOPE, false);
            oAuthClientServices.put(getServiceId(user), oAuthClientService);
        }

        return oAuthClientService;
    }

    /**
     * A {@link TadoHomeHandler} calls this to unsubscribe from the {@link OAuthClientService}.
     * If it is the last one then it clears the {@link OAuthClientService} and unregisters the
     * {@link TadoAuthenticationServlet}
     *
     * @param tadoHomeHandler
     */
    public void unsubscribeOAuthClientService(TadoHomeHandler tadoHomeHandler) {
        if (oAuthClientServiceSubscribers.remove(tadoHomeHandler) && oAuthClientServiceSubscribers.isEmpty()) {
            httpService.unregister(TadoAuthenticationServlet.PATH);
        }
    }

    /**
     * Returns a nullable {@link AccessTokenResponse} if the OAuthClientService exists.
     *
     * @param user the optional user name (may be null)
     * @return a nullable {@link AccessTokenResponse}.
     * @throws OAuthException
     * @throws IOException
     * @throws OAuthResponseException
     */
    public @Nullable AccessTokenResponse getAccessTokenResponse(@Nullable String user)
            throws OAuthException, IOException, OAuthResponseException {
        OAuthClientService oAuthClientService = oAuthClientServices.get(getServiceId(user));
        if (oAuthClientService == null) {
            throw new OAuthException("Missing OAuthClientService");
        }
        try {
            return oAuthClientService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException e) {
            logger.debug("getAccessTokenResponse() error {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns a non null DeviceCodeResponse from the OAuthClientService if it exists.
     *
     * @param user the optional user name (may be null)
     * @return a {@link DeviceCodeResponseDTO}
     * @throws OAuthException if it cannot return a non null result
     */
    public DeviceCodeResponseDTO getDeviceCodeResponse(@Nullable String user) throws OAuthException {
        OAuthClientService oAuthClientService = oAuthClientServices.get(getServiceId(user));
        if (oAuthClientService == null) {
            throw new OAuthException("Missing OAuthClientService");
        }
        DeviceCodeResponseDTO result = oAuthClientService.getDeviceCodeResponse();
        if (result == null) {
            throw new OAuthException("Expecting non null DeviceCodeResponse");
        }
        return result;
    }

    /**
     * Check if there is an OAuthClientService registered
     *
     * @param user the optional user name (may be null)
     */
    public boolean hasOAuthClientService(@Nullable String user) {
        return oAuthClientServices.containsKey(getServiceId(user));
    }

    /**
     * Build a unique OAuth service id using the (optional) user name if present and not blank
     *
     * @param user the optional user name (may be null)
     */
    private String getServiceId(@Nullable String user) {
        return THING_TYPE_HOME.toString() + (user != null && !user.isBlank() ? ":" + user : "");
    }
}
