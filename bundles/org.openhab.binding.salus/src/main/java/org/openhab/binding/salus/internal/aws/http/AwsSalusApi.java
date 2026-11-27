/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.salus.internal.aws.http;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;
import static java.util.Objects.requireNonNull;
import static org.openhab.binding.salus.internal.aws.http.AwsSigner.*;

import java.net.URLEncoder;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.salus.internal.rest.AbstractSalusApi;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.binding.salus.internal.rest.RestClient;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * The SalusApi class is responsible for interacting with a REST API to perform various operations related to the Salus
 * system. It handles authentication, token management, and provides methods to retrieve and manipulate device
 * information and properties.
 *
 * @author Martin Grześlowski - Initial contribution
 * @author Martin Grześlowski - AWS device shadow commands
 */
@NonNullByDefault
public class AwsSalusApi extends AbstractSalusApi<Authentication> {
    private final AuthenticationHelper authenticationHelper;
    private final String companyCode;
    private final String awsService;
    private final String region;
    @Nullable
    CogitoCredentials cogitoCredentials;

    AwsSalusApi(String username, byte[] password, String baseUrl, RestClient restClient, GsonMapper mapper, Clock clock,
            AuthenticationHelper authenticationHelper, String companyCode, String awsService, String region) {
        super(username, password, baseUrl, restClient, mapper, clock);
        this.authenticationHelper = authenticationHelper;
        this.companyCode = companyCode;
        this.awsService = awsService;
        this.region = region;
    }

    public AwsSalusApi(HttpClientFactory httpClientFactory, String username, byte[] password, String baseUrl,
            RestClient restClient, GsonMapper gsonMapper, String userPoolId, String identityPoolId, String clientId,
            String region, String companyCode, String awsService) {
        this(username, password, baseUrl, restClient, gsonMapper, Clock.systemDefaultZone(),
                new AuthenticationHelper(httpClientFactory, userPoolId, clientId, region, identityPoolId), companyCode,
                awsService, region);
    }

    @Override
    protected void login() throws AuthSalusApiException {
        logger.debug("Login with username '{}'", username);
        var result = authenticationHelper.performSrpAuthentication(username, new String(password, UTF_8));
        var localAuth = authentication = new Authentication(result.getAccessToken(), result.getExpiresIn(),
                result.getTokenType(), result.getRefreshToken(), result.getIdToken());
        var localExpireTime = authTokenExpireTime = ZonedDateTime.now(clock).withZoneSameInstant(UTC)
                .plusSeconds(localAuth.expiresIn() - TOKEN_EXPIRE_TIME_ADJUSTMENT_SECONDS);

        var id = authenticationHelper.getId(result);

        var cogito = authenticationHelper.getCredentialsForIdentity(result, id.getIdentityId());
        cogitoCredentials = new CogitoCredentials(//
                cogito.getCredentials().getAccessKeyId(), //
                cogito.getCredentials().getSecretKey(), //
                cogito.getCredentials().getSessionToken());

        var cogitoExpirationTime = cogito.getCredentials().getExpiration();
        if (cogitoExpirationTime.isBefore(localExpireTime.toInstant())) {
            authTokenExpireTime = ZonedDateTime.ofInstant(cogitoExpirationTime, UTC);
        }
    }

    @Override
    protected void cleanAuth() {
        super.cleanAuth();
        cogitoCredentials = null;
    }

    @Override
    public SortedSet<Device> findDevices() throws AuthSalusApiException, SalusApiException {
        var result = new TreeSet<Device>();
        var gateways = findGateways();
        for (var gatewayId : gateways) {
            var response = get(url("/api/v1/occupants/slider_details?id=%s&type=gateway".formatted(gatewayId)),
                    authHeaders());
            if (response == null) {
                continue;
            }
            result.addAll(mapper.parseAwsDevices(response));
        }
        return result;
    }

    private List<String> findGateways() throws SalusApiException, AuthSalusApiException {
        var response = get(url("/api/v1/occupants/slider_list"), authHeaders());
        if (response == null) {
            return List.of();
        }
        return mapper.parseAwsGatewayIds(response);
    }

    private RestClient.Header[] authHeaders() throws AuthSalusApiException {
        refreshAccessToken();
        return new RestClient.Header[] {
                new RestClient.Header("x-access-token", requireNonNull(authentication).accessToken()),
                new RestClient.Header("x-auth-token", requireNonNull(authentication).idToken()),
                new RestClient.Header("x-company-code", companyCode) };
    }

    @Override
    public SortedSet<DeviceProperty<?>> findDeviceProperties(String dsn)
            throws SalusApiException, AuthSalusApiException {
        var path = shadowPath(dsn);
        var response = restClient.get("https://" + shadowHost() + path, shadowHeaders("GET", path, null));
        if (response == null) {
            return new TreeSet<>();
        }

        return new TreeSet<>(mapper.parseAwsDeviceProperties(response));
    }

    private String shadowHost() {
        return "%s.iot.%s.amazonaws.com".formatted(awsService, region);
    }

    private String shadowPath(String dsn) throws SalusApiException {
        if (!dsn.matches("[a-zA-Z0-9:_-]{1,128}")) {
            throw new SalusApiException("Invalid AWS Thing name");
        }
        return "/things/%s/shadow".formatted(dsn);
    }

    private synchronized RestClient.Header[] shadowHeaders(String method, String path, @Nullable String body)
            throws AuthSalusApiException, SalusApiException {
        refreshAccessToken();
        var time = ZonedDateTime.now(clock).withZoneSameInstant(UTC);
        var headers = sign(method, shadowHost(), path, time, requireNonNull(cogitoCredentials), region,
                "iotdevicegateway", body);
        return headers.entrySet().stream().map(header -> new RestClient.Header(header.getKey(), header.getValue()))
                .toArray(RestClient.Header[]::new);
    }

    @Override
    public Object setValueForProperty(String dsn, String propertyName, Object value)
            throws SalusApiException, AuthSalusApiException {
        var path = "/topics/" + URLEncoder.encode("$aws" + shadowPath(dsn) + "/update", UTF_8) + "?qos=1";
        var body = mapper.awsShadowUpdate(propertyName, value);
        // Salus consumes the published update command; UpdateThingShadow alone only changes cloud state.
        restClient.post("https://" + shadowHost() + path, new RestClient.Content(body),
                shadowHeaders("POST", path, body));
        // Publish returns an empty body. Reported-state polling confirms device application.
        return value;
    }
}
