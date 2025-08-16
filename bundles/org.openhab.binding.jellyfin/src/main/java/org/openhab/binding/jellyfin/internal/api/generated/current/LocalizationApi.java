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
package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.CountryInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.CultureDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.LocalizationOption;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ParentalRating;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LocalizationApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public LocalizationApi() {
        this(Configuration.getDefaultApiClient());
    }

    public LocalizationApi(ApiClient apiClient) {
        memberVarHttpClient = apiClient.getHttpClient();
        memberVarObjectMapper = apiClient.getObjectMapper();
        memberVarBaseUri = apiClient.getBaseUri();
        memberVarInterceptor = apiClient.getRequestInterceptor();
        memberVarReadTimeout = apiClient.getReadTimeout();
        memberVarResponseInterceptor = apiClient.getResponseInterceptor();
        memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
    }

    protected ApiException getApiException(String operationId, HttpResponse<InputStream> response) throws IOException {
        String body = response.body() == null ? null : new String(response.body().readAllBytes());
        String message = formatExceptionMessage(operationId, response.statusCode(), body);
        return new ApiException(response.statusCode(), message, response.headers(), body);
    }

    private String formatExceptionMessage(String operationId, int statusCode, String body) {
        if (body == null || body.isEmpty()) {
            body = "[no body]";
        }
        return operationId + " call failed with: " + statusCode + " - " + body;
    }

    /**
     * Gets known countries.
     * 
     * @return List&lt;CountryInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<CountryInfo> getCountries() throws ApiException {
        ApiResponse<List<CountryInfo>> localVarResponse = getCountriesWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets known countries.
     * 
     * @return ApiResponse&lt;List&lt;CountryInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<CountryInfo>> getCountriesWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getCountriesRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getCountries", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<CountryInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<CountryInfo>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<CountryInfo>>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getCountriesRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Localization/Countries";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets known cultures.
     * 
     * @return List&lt;CultureDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<CultureDto> getCultures() throws ApiException {
        ApiResponse<List<CultureDto>> localVarResponse = getCulturesWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets known cultures.
     * 
     * @return ApiResponse&lt;List&lt;CultureDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<CultureDto>> getCulturesWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getCulturesRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getCultures", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<CultureDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<CultureDto>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<CultureDto>>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getCulturesRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Localization/Cultures";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets localization options.
     * 
     * @return List&lt;LocalizationOption&gt;
     * @throws ApiException if fails to make API call
     */
    public List<LocalizationOption> getLocalizationOptions() throws ApiException {
        ApiResponse<List<LocalizationOption>> localVarResponse = getLocalizationOptionsWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets localization options.
     * 
     * @return ApiResponse&lt;List&lt;LocalizationOption&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<LocalizationOption>> getLocalizationOptionsWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLocalizationOptionsRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLocalizationOptions", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<LocalizationOption>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<LocalizationOption>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<LocalizationOption>>() {
                                        }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getLocalizationOptionsRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Localization/Options";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets known parental ratings.
     * 
     * @return List&lt;ParentalRating&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ParentalRating> getParentalRatings() throws ApiException {
        ApiResponse<List<ParentalRating>> localVarResponse = getParentalRatingsWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets known parental ratings.
     * 
     * @return ApiResponse&lt;List&lt;ParentalRating&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ParentalRating>> getParentalRatingsWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getParentalRatingsRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getParentalRatings", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<ParentalRating>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<ParentalRating>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<ParentalRating>>() {
                                        }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getParentalRatingsRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Localization/ParentalRatings";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
