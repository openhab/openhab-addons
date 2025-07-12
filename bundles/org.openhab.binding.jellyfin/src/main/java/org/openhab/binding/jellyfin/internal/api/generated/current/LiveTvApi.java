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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ChannelMappingOptionsDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ChannelType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.GetProgramsDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.GuideInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ListingsProviderInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.LiveTvInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.NameIdPair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RecordingStatus;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SeriesTimerInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SeriesTimerInfoDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SetChannelMappingDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SortOrder;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TimerInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TimerInfoDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TunerChannelMapping;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TunerHostInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LiveTvApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public LiveTvApi() {
        this(Configuration.getDefaultApiClient());
    }

    public LiveTvApi(ApiClient apiClient) {
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
     * Adds a listings provider.
     * 
     * @param pw Password. (optional)
     * @param validateListings Validate listings. (optional, default to false)
     * @param validateLogin Validate login. (optional, default to false)
     * @param listingsProviderInfo New listings info. (optional)
     * @return ListingsProviderInfo
     * @throws ApiException if fails to make API call
     */
    public ListingsProviderInfo addListingProvider(@org.eclipse.jdt.annotation.NonNull String pw,
            @org.eclipse.jdt.annotation.NonNull Boolean validateListings,
            @org.eclipse.jdt.annotation.NonNull Boolean validateLogin,
            @org.eclipse.jdt.annotation.NonNull ListingsProviderInfo listingsProviderInfo) throws ApiException {
        ApiResponse<ListingsProviderInfo> localVarResponse = addListingProviderWithHttpInfo(pw, validateListings,
                validateLogin, listingsProviderInfo);
        return localVarResponse.getData();
    }

    /**
     * Adds a listings provider.
     * 
     * @param pw Password. (optional)
     * @param validateListings Validate listings. (optional, default to false)
     * @param validateLogin Validate login. (optional, default to false)
     * @param listingsProviderInfo New listings info. (optional)
     * @return ApiResponse&lt;ListingsProviderInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ListingsProviderInfo> addListingProviderWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String pw, @org.eclipse.jdt.annotation.NonNull Boolean validateListings,
            @org.eclipse.jdt.annotation.NonNull Boolean validateLogin,
            @org.eclipse.jdt.annotation.NonNull ListingsProviderInfo listingsProviderInfo) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = addListingProviderRequestBuilder(pw, validateListings,
                validateLogin, listingsProviderInfo);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("addListingProvider", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<ListingsProviderInfo>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<ListingsProviderInfo>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<ListingsProviderInfo>() {
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

    private HttpRequest.Builder addListingProviderRequestBuilder(@org.eclipse.jdt.annotation.NonNull String pw,
            @org.eclipse.jdt.annotation.NonNull Boolean validateListings,
            @org.eclipse.jdt.annotation.NonNull Boolean validateLogin,
            @org.eclipse.jdt.annotation.NonNull ListingsProviderInfo listingsProviderInfo) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/ListingProviders";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "pw";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("pw", pw));
        localVarQueryParameterBaseName = "validateListings";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("validateListings", validateListings));
        localVarQueryParameterBaseName = "validateLogin";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("validateLogin", validateLogin));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(listingsProviderInfo);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Adds a tuner host.
     * 
     * @param tunerHostInfo New tuner host. (optional)
     * @return TunerHostInfo
     * @throws ApiException if fails to make API call
     */
    public TunerHostInfo addTunerHost(@org.eclipse.jdt.annotation.NonNull TunerHostInfo tunerHostInfo)
            throws ApiException {
        ApiResponse<TunerHostInfo> localVarResponse = addTunerHostWithHttpInfo(tunerHostInfo);
        return localVarResponse.getData();
    }

    /**
     * Adds a tuner host.
     * 
     * @param tunerHostInfo New tuner host. (optional)
     * @return ApiResponse&lt;TunerHostInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<TunerHostInfo> addTunerHostWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull TunerHostInfo tunerHostInfo) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = addTunerHostRequestBuilder(tunerHostInfo);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("addTunerHost", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<TunerHostInfo>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<TunerHostInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<TunerHostInfo>() {
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

    private HttpRequest.Builder addTunerHostRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull TunerHostInfo tunerHostInfo) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/TunerHosts";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(tunerHostInfo);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Cancels a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @throws ApiException if fails to make API call
     */
    public void cancelSeriesTimer(@org.eclipse.jdt.annotation.Nullable String timerId) throws ApiException {
        cancelSeriesTimerWithHttpInfo(timerId);
    }

    /**
     * Cancels a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> cancelSeriesTimerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String timerId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = cancelSeriesTimerRequestBuilder(timerId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("cancelSeriesTimer", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder cancelSeriesTimerRequestBuilder(@org.eclipse.jdt.annotation.Nullable String timerId)
            throws ApiException {
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling cancelSeriesTimer");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/SeriesTimers/{timerId}".replace("{timerId}",
                ApiClient.urlEncode(timerId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Cancels a live tv timer.
     * 
     * @param timerId Timer id. (required)
     * @throws ApiException if fails to make API call
     */
    public void cancelTimer(@org.eclipse.jdt.annotation.Nullable String timerId) throws ApiException {
        cancelTimerWithHttpInfo(timerId);
    }

    /**
     * Cancels a live tv timer.
     * 
     * @param timerId Timer id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> cancelTimerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String timerId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = cancelTimerRequestBuilder(timerId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("cancelTimer", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder cancelTimerRequestBuilder(@org.eclipse.jdt.annotation.Nullable String timerId)
            throws ApiException {
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling cancelTimer");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Timers/{timerId}".replace("{timerId}", ApiClient.urlEncode(timerId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Creates a live tv series timer.
     * 
     * @param seriesTimerInfoDto New series timer info. (optional)
     * @throws ApiException if fails to make API call
     */
    public void createSeriesTimer(@org.eclipse.jdt.annotation.NonNull SeriesTimerInfoDto seriesTimerInfoDto)
            throws ApiException {
        createSeriesTimerWithHttpInfo(seriesTimerInfoDto);
    }

    /**
     * Creates a live tv series timer.
     * 
     * @param seriesTimerInfoDto New series timer info. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> createSeriesTimerWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull SeriesTimerInfoDto seriesTimerInfoDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = createSeriesTimerRequestBuilder(seriesTimerInfoDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("createSeriesTimer", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder createSeriesTimerRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull SeriesTimerInfoDto seriesTimerInfoDto) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/SeriesTimers";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(seriesTimerInfoDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Creates a live tv timer.
     * 
     * @param timerInfoDto New timer info. (optional)
     * @throws ApiException if fails to make API call
     */
    public void createTimer(@org.eclipse.jdt.annotation.NonNull TimerInfoDto timerInfoDto) throws ApiException {
        createTimerWithHttpInfo(timerInfoDto);
    }

    /**
     * Creates a live tv timer.
     * 
     * @param timerInfoDto New timer info. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> createTimerWithHttpInfo(@org.eclipse.jdt.annotation.NonNull TimerInfoDto timerInfoDto)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = createTimerRequestBuilder(timerInfoDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("createTimer", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder createTimerRequestBuilder(@org.eclipse.jdt.annotation.NonNull TimerInfoDto timerInfoDto)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Timers";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(timerInfoDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Delete listing provider.
     * 
     * @param id Listing provider id. (optional)
     * @throws ApiException if fails to make API call
     */
    public void deleteListingProvider(@org.eclipse.jdt.annotation.NonNull String id) throws ApiException {
        deleteListingProviderWithHttpInfo(id);
    }

    /**
     * Delete listing provider.
     * 
     * @param id Listing provider id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteListingProviderWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteListingProviderRequestBuilder(id);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteListingProvider", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder deleteListingProviderRequestBuilder(@org.eclipse.jdt.annotation.NonNull String id)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/ListingProviders";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "id";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("id", id));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Deletes a live tv recording.
     * 
     * @param recordingId Recording id. (required)
     * @throws ApiException if fails to make API call
     */
    public void deleteRecording(@org.eclipse.jdt.annotation.Nullable UUID recordingId) throws ApiException {
        deleteRecordingWithHttpInfo(recordingId);
    }

    /**
     * Deletes a live tv recording.
     * 
     * @param recordingId Recording id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteRecordingWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID recordingId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteRecordingRequestBuilder(recordingId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteRecording", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder deleteRecordingRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID recordingId)
            throws ApiException {
        // verify the required parameter 'recordingId' is set
        if (recordingId == null) {
            throw new ApiException(400, "Missing the required parameter 'recordingId' when calling deleteRecording");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Recordings/{recordingId}".replace("{recordingId}",
                ApiClient.urlEncode(recordingId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Deletes a tuner host.
     * 
     * @param id Tuner host id. (optional)
     * @throws ApiException if fails to make API call
     */
    public void deleteTunerHost(@org.eclipse.jdt.annotation.NonNull String id) throws ApiException {
        deleteTunerHostWithHttpInfo(id);
    }

    /**
     * Deletes a tuner host.
     * 
     * @param id Tuner host id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteTunerHostWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteTunerHostRequestBuilder(id);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteTunerHost", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder deleteTunerHostRequestBuilder(@org.eclipse.jdt.annotation.NonNull String id)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/TunerHosts";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "id";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("id", id));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Discover tuners.
     * 
     * @param newDevicesOnly Only discover new tuners. (optional, default to false)
     * @return List&lt;TunerHostInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<TunerHostInfo> discoverTuners(@org.eclipse.jdt.annotation.NonNull Boolean newDevicesOnly)
            throws ApiException {
        ApiResponse<List<TunerHostInfo>> localVarResponse = discoverTunersWithHttpInfo(newDevicesOnly);
        return localVarResponse.getData();
    }

    /**
     * Discover tuners.
     * 
     * @param newDevicesOnly Only discover new tuners. (optional, default to false)
     * @return ApiResponse&lt;List&lt;TunerHostInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<TunerHostInfo>> discoverTunersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Boolean newDevicesOnly) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = discoverTunersRequestBuilder(newDevicesOnly);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("discoverTuners", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<TunerHostInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<TunerHostInfo>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<TunerHostInfo>>() {
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

    private HttpRequest.Builder discoverTunersRequestBuilder(@org.eclipse.jdt.annotation.NonNull Boolean newDevicesOnly)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Tuners/Discover";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "newDevicesOnly";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("newDevicesOnly", newDevicesOnly));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Discover tuners.
     * 
     * @param newDevicesOnly Only discover new tuners. (optional, default to false)
     * @return List&lt;TunerHostInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<TunerHostInfo> discvoverTuners(@org.eclipse.jdt.annotation.NonNull Boolean newDevicesOnly)
            throws ApiException {
        ApiResponse<List<TunerHostInfo>> localVarResponse = discvoverTunersWithHttpInfo(newDevicesOnly);
        return localVarResponse.getData();
    }

    /**
     * Discover tuners.
     * 
     * @param newDevicesOnly Only discover new tuners. (optional, default to false)
     * @return ApiResponse&lt;List&lt;TunerHostInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<TunerHostInfo>> discvoverTunersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Boolean newDevicesOnly) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = discvoverTunersRequestBuilder(newDevicesOnly);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("discvoverTuners", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<TunerHostInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<TunerHostInfo>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<TunerHostInfo>>() {
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

    private HttpRequest.Builder discvoverTunersRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull Boolean newDevicesOnly) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Tuners/Discvover";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "newDevicesOnly";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("newDevicesOnly", newDevicesOnly));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets a live tv channel.
     * 
     * @param channelId Channel id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getChannel(@org.eclipse.jdt.annotation.Nullable UUID channelId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        ApiResponse<BaseItemDto> localVarResponse = getChannelWithHttpInfo(channelId, userId);
        return localVarResponse.getData();
    }

    /**
     * Gets a live tv channel.
     * 
     * @param channelId Channel id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getChannelWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID channelId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getChannelRequestBuilder(channelId, userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getChannel", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDto>() {
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

    private HttpRequest.Builder getChannelRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID channelId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // verify the required parameter 'channelId' is set
        if (channelId == null) {
            throw new ApiException(400, "Missing the required parameter 'channelId' when calling getChannel");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Channels/{channelId}".replace("{channelId}",
                ApiClient.urlEncode(channelId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Get channel mapping options.
     * 
     * @param providerId Provider id. (optional)
     * @return ChannelMappingOptionsDto
     * @throws ApiException if fails to make API call
     */
    public ChannelMappingOptionsDto getChannelMappingOptions(@org.eclipse.jdt.annotation.NonNull String providerId)
            throws ApiException {
        ApiResponse<ChannelMappingOptionsDto> localVarResponse = getChannelMappingOptionsWithHttpInfo(providerId);
        return localVarResponse.getData();
    }

    /**
     * Get channel mapping options.
     * 
     * @param providerId Provider id. (optional)
     * @return ApiResponse&lt;ChannelMappingOptionsDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ChannelMappingOptionsDto> getChannelMappingOptionsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String providerId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getChannelMappingOptionsRequestBuilder(providerId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getChannelMappingOptions", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<ChannelMappingOptionsDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<ChannelMappingOptionsDto>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<ChannelMappingOptionsDto>() {
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

    private HttpRequest.Builder getChannelMappingOptionsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull String providerId) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/ChannelMappingOptions";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "providerId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("providerId", providerId));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets default listings provider info.
     * 
     * @return ListingsProviderInfo
     * @throws ApiException if fails to make API call
     */
    public ListingsProviderInfo getDefaultListingProvider() throws ApiException {
        ApiResponse<ListingsProviderInfo> localVarResponse = getDefaultListingProviderWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets default listings provider info.
     * 
     * @return ApiResponse&lt;ListingsProviderInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ListingsProviderInfo> getDefaultListingProviderWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDefaultListingProviderRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDefaultListingProvider", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<ListingsProviderInfo>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<ListingsProviderInfo>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<ListingsProviderInfo>() {
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

    private HttpRequest.Builder getDefaultListingProviderRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/ListingProviders/Default";

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
     * Gets the default values for a new timer.
     * 
     * @param programId Optional. To attach default values based on a program. (optional)
     * @return SeriesTimerInfoDto
     * @throws ApiException if fails to make API call
     */
    public SeriesTimerInfoDto getDefaultTimer(@org.eclipse.jdt.annotation.NonNull String programId)
            throws ApiException {
        ApiResponse<SeriesTimerInfoDto> localVarResponse = getDefaultTimerWithHttpInfo(programId);
        return localVarResponse.getData();
    }

    /**
     * Gets the default values for a new timer.
     * 
     * @param programId Optional. To attach default values based on a program. (optional)
     * @return ApiResponse&lt;SeriesTimerInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<SeriesTimerInfoDto> getDefaultTimerWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String programId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDefaultTimerRequestBuilder(programId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDefaultTimer", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<SeriesTimerInfoDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<SeriesTimerInfoDto>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<SeriesTimerInfoDto>() {
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

    private HttpRequest.Builder getDefaultTimerRequestBuilder(@org.eclipse.jdt.annotation.NonNull String programId)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Timers/Defaults";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "programId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("programId", programId));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Get guid info.
     * 
     * @return GuideInfo
     * @throws ApiException if fails to make API call
     */
    public GuideInfo getGuideInfo() throws ApiException {
        ApiResponse<GuideInfo> localVarResponse = getGuideInfoWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Get guid info.
     * 
     * @return ApiResponse&lt;GuideInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<GuideInfo> getGuideInfoWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getGuideInfoRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getGuideInfo", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<GuideInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<GuideInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<GuideInfo>() {
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

    private HttpRequest.Builder getGuideInfoRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/GuideInfo";

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
     * Gets available lineups.
     * 
     * @param id Provider id. (optional)
     * @param type Provider type. (optional)
     * @param location Location. (optional)
     * @param country Country. (optional)
     * @return List&lt;NameIdPair&gt;
     * @throws ApiException if fails to make API call
     */
    public List<NameIdPair> getLineups(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull String type, @org.eclipse.jdt.annotation.NonNull String location,
            @org.eclipse.jdt.annotation.NonNull String country) throws ApiException {
        ApiResponse<List<NameIdPair>> localVarResponse = getLineupsWithHttpInfo(id, type, location, country);
        return localVarResponse.getData();
    }

    /**
     * Gets available lineups.
     * 
     * @param id Provider id. (optional)
     * @param type Provider type. (optional)
     * @param location Location. (optional)
     * @param country Country. (optional)
     * @return ApiResponse&lt;List&lt;NameIdPair&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<NameIdPair>> getLineupsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull String type, @org.eclipse.jdt.annotation.NonNull String location,
            @org.eclipse.jdt.annotation.NonNull String country) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLineupsRequestBuilder(id, type, location, country);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLineups", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<NameIdPair>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<NameIdPair>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<NameIdPair>>() {
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

    private HttpRequest.Builder getLineupsRequestBuilder(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull String type, @org.eclipse.jdt.annotation.NonNull String location,
            @org.eclipse.jdt.annotation.NonNull String country) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/ListingProviders/Lineups";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "id";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("id", id));
        localVarQueryParameterBaseName = "type";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("type", type));
        localVarQueryParameterBaseName = "location";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("location", location));
        localVarQueryParameterBaseName = "country";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("country", country));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets a live tv recording stream.
     * 
     * @param recordingId Recording id. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getLiveRecordingFile(@org.eclipse.jdt.annotation.Nullable String recordingId) throws ApiException {
        ApiResponse<File> localVarResponse = getLiveRecordingFileWithHttpInfo(recordingId);
        return localVarResponse.getData();
    }

    /**
     * Gets a live tv recording stream.
     * 
     * @param recordingId Recording id. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getLiveRecordingFileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String recordingId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLiveRecordingFileRequestBuilder(recordingId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLiveRecordingFile", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
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

    private HttpRequest.Builder getLiveRecordingFileRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String recordingId) throws ApiException {
        // verify the required parameter 'recordingId' is set
        if (recordingId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'recordingId' when calling getLiveRecordingFile");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/LiveRecordings/{recordingId}/stream".replace("{recordingId}",
                ApiClient.urlEncode(recordingId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "video/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Gets a live tv channel stream.
     * 
     * @param streamId Stream id. (required)
     * @param container Container type. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getLiveStreamFile(@org.eclipse.jdt.annotation.Nullable String streamId,
            @org.eclipse.jdt.annotation.Nullable String container) throws ApiException {
        ApiResponse<File> localVarResponse = getLiveStreamFileWithHttpInfo(streamId, container);
        return localVarResponse.getData();
    }

    /**
     * Gets a live tv channel stream.
     * 
     * @param streamId Stream id. (required)
     * @param container Container type. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getLiveStreamFileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String streamId,
            @org.eclipse.jdt.annotation.Nullable String container) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLiveStreamFileRequestBuilder(streamId, container);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLiveStreamFile", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
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

    private HttpRequest.Builder getLiveStreamFileRequestBuilder(@org.eclipse.jdt.annotation.Nullable String streamId,
            @org.eclipse.jdt.annotation.Nullable String container) throws ApiException {
        // verify the required parameter 'streamId' is set
        if (streamId == null) {
            throw new ApiException(400, "Missing the required parameter 'streamId' when calling getLiveStreamFile");
        }
        // verify the required parameter 'container' is set
        if (container == null) {
            throw new ApiException(400, "Missing the required parameter 'container' when calling getLiveStreamFile");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/LiveStreamFiles/{streamId}/stream.{container}"
                .replace("{streamId}", ApiClient.urlEncode(streamId.toString()))
                .replace("{container}", ApiClient.urlEncode(container.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "video/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Gets available live tv channels.
     * 
     * @param type Optional. Filter by channel type. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param isFavorite Optional. Filter by channels that are favorites, or not. (optional)
     * @param isLiked Optional. Filter by channels that are liked, or not. (optional)
     * @param isDisliked Optional. Filter by channels that are disliked, or not. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes \&quot;Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param sortBy Optional. Key to sort by. (optional)
     * @param sortOrder Optional. Sort order. (optional)
     * @param enableFavoriteSorting Optional. Incorporate favorite and like status into channel sorting. (optional,
     *            default to false)
     * @param addCurrentProgram Optional. Adds current program info to each channel. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getLiveTvChannels(@org.eclipse.jdt.annotation.NonNull ChannelType type,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite, @org.eclipse.jdt.annotation.NonNull Boolean isLiked,
            @org.eclipse.jdt.annotation.NonNull Boolean isDisliked,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull SortOrder sortOrder,
            @org.eclipse.jdt.annotation.NonNull Boolean enableFavoriteSorting,
            @org.eclipse.jdt.annotation.NonNull Boolean addCurrentProgram) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getLiveTvChannelsWithHttpInfo(type, userId, startIndex,
                isMovie, isSeries, isNews, isKids, isSports, limit, isFavorite, isLiked, isDisliked, enableImages,
                imageTypeLimit, enableImageTypes, fields, enableUserData, sortBy, sortOrder, enableFavoriteSorting,
                addCurrentProgram);
        return localVarResponse.getData();
    }

    /**
     * Gets available live tv channels.
     * 
     * @param type Optional. Filter by channel type. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param isFavorite Optional. Filter by channels that are favorites, or not. (optional)
     * @param isLiked Optional. Filter by channels that are liked, or not. (optional)
     * @param isDisliked Optional. Filter by channels that are disliked, or not. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes \&quot;Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param sortBy Optional. Key to sort by. (optional)
     * @param sortOrder Optional. Sort order. (optional)
     * @param enableFavoriteSorting Optional. Incorporate favorite and like status into channel sorting. (optional,
     *            default to false)
     * @param addCurrentProgram Optional. Adds current program info to each channel. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getLiveTvChannelsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull ChannelType type, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isSeries, @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull Boolean isLiked, @org.eclipse.jdt.annotation.NonNull Boolean isDisliked,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull SortOrder sortOrder,
            @org.eclipse.jdt.annotation.NonNull Boolean enableFavoriteSorting,
            @org.eclipse.jdt.annotation.NonNull Boolean addCurrentProgram) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLiveTvChannelsRequestBuilder(type, userId, startIndex, isMovie,
                isSeries, isNews, isKids, isSports, limit, isFavorite, isLiked, isDisliked, enableImages,
                imageTypeLimit, enableImageTypes, fields, enableUserData, sortBy, sortOrder, enableFavoriteSorting,
                addCurrentProgram);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLiveTvChannels", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<BaseItemDtoQueryResult>() {
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

    private HttpRequest.Builder getLiveTvChannelsRequestBuilder(@org.eclipse.jdt.annotation.NonNull ChannelType type,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite, @org.eclipse.jdt.annotation.NonNull Boolean isLiked,
            @org.eclipse.jdt.annotation.NonNull Boolean isDisliked,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull SortOrder sortOrder,
            @org.eclipse.jdt.annotation.NonNull Boolean enableFavoriteSorting,
            @org.eclipse.jdt.annotation.NonNull Boolean addCurrentProgram) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Channels";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "type";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("type", type));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "isMovie";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isMovie", isMovie));
        localVarQueryParameterBaseName = "isSeries";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSeries", isSeries));
        localVarQueryParameterBaseName = "isNews";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isNews", isNews));
        localVarQueryParameterBaseName = "isKids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isKids", isKids));
        localVarQueryParameterBaseName = "isSports";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSports", isSports));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "isFavorite";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isFavorite", isFavorite));
        localVarQueryParameterBaseName = "isLiked";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isLiked", isLiked));
        localVarQueryParameterBaseName = "isDisliked";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isDisliked", isDisliked));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "sortBy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParameterBaseName = "sortOrder";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("sortOrder", sortOrder));
        localVarQueryParameterBaseName = "enableFavoriteSorting";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableFavoriteSorting", enableFavoriteSorting));
        localVarQueryParameterBaseName = "addCurrentProgram";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("addCurrentProgram", addCurrentProgram));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets available live tv services.
     * 
     * @return LiveTvInfo
     * @throws ApiException if fails to make API call
     */
    public LiveTvInfo getLiveTvInfo() throws ApiException {
        ApiResponse<LiveTvInfo> localVarResponse = getLiveTvInfoWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets available live tv services.
     * 
     * @return ApiResponse&lt;LiveTvInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LiveTvInfo> getLiveTvInfoWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLiveTvInfoRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLiveTvInfo", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<LiveTvInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<LiveTvInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<LiveTvInfo>() {
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

    private HttpRequest.Builder getLiveTvInfoRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Info";

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
     * Gets available live tv epgs.
     * 
     * @param channelIds The channels to return guide information for. (optional)
     * @param userId Optional. Filter by user id. (optional)
     * @param minStartDate Optional. The minimum premiere start date. (optional)
     * @param hasAired Optional. Filter by programs that have completed airing, or not. (optional)
     * @param isAiring Optional. Filter by programs that are currently airing, or not. (optional)
     * @param maxStartDate Optional. The maximum premiere start date. (optional)
     * @param minEndDate Optional. The minimum premiere end date. (optional)
     * @param maxEndDate Optional. The maximum premiere end date. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Name, StartDate. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param genres The genres to return guide information for. (optional)
     * @param genreIds The genre ids to return guide information for. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param seriesTimerId Optional. Filter by series timer id. (optional)
     * @param librarySeriesId Optional. Filter by library series id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableTotalRecordCount Retrieve total record count. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getLiveTvPrograms(@org.eclipse.jdt.annotation.NonNull List<UUID> channelIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minStartDate,
            @org.eclipse.jdt.annotation.NonNull Boolean hasAired, @org.eclipse.jdt.annotation.NonNull Boolean isAiring,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxStartDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minEndDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxEndDate,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull List<String> genres,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull UUID librarySeriesId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getLiveTvProgramsWithHttpInfo(channelIds, userId,
                minStartDate, hasAired, isAiring, maxStartDate, minEndDate, maxEndDate, isMovie, isSeries, isNews,
                isKids, isSports, startIndex, limit, sortBy, sortOrder, genres, genreIds, enableImages, imageTypeLimit,
                enableImageTypes, enableUserData, seriesTimerId, librarySeriesId, fields, enableTotalRecordCount);
        return localVarResponse.getData();
    }

    /**
     * Gets available live tv epgs.
     * 
     * @param channelIds The channels to return guide information for. (optional)
     * @param userId Optional. Filter by user id. (optional)
     * @param minStartDate Optional. The minimum premiere start date. (optional)
     * @param hasAired Optional. Filter by programs that have completed airing, or not. (optional)
     * @param isAiring Optional. Filter by programs that are currently airing, or not. (optional)
     * @param maxStartDate Optional. The maximum premiere start date. (optional)
     * @param minEndDate Optional. The minimum premiere end date. (optional)
     * @param maxEndDate Optional. The maximum premiere end date. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Name, StartDate. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param genres The genres to return guide information for. (optional)
     * @param genreIds The genre ids to return guide information for. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param seriesTimerId Optional. Filter by series timer id. (optional)
     * @param librarySeriesId Optional. Filter by library series id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableTotalRecordCount Retrieve total record count. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getLiveTvProgramsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull List<UUID> channelIds, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minStartDate,
            @org.eclipse.jdt.annotation.NonNull Boolean hasAired, @org.eclipse.jdt.annotation.NonNull Boolean isAiring,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxStartDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minEndDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxEndDate,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull List<String> genres,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull UUID librarySeriesId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLiveTvProgramsRequestBuilder(channelIds, userId, minStartDate,
                hasAired, isAiring, maxStartDate, minEndDate, maxEndDate, isMovie, isSeries, isNews, isKids, isSports,
                startIndex, limit, sortBy, sortOrder, genres, genreIds, enableImages, imageTypeLimit, enableImageTypes,
                enableUserData, seriesTimerId, librarySeriesId, fields, enableTotalRecordCount);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLiveTvPrograms", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<BaseItemDtoQueryResult>() {
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

    private HttpRequest.Builder getLiveTvProgramsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull List<UUID> channelIds, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minStartDate,
            @org.eclipse.jdt.annotation.NonNull Boolean hasAired, @org.eclipse.jdt.annotation.NonNull Boolean isAiring,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxStartDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minEndDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxEndDate,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull List<String> genres,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull UUID librarySeriesId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Programs";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "channelIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "channelIds", channelIds));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "minStartDate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minStartDate", minStartDate));
        localVarQueryParameterBaseName = "hasAired";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasAired", hasAired));
        localVarQueryParameterBaseName = "isAiring";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isAiring", isAiring));
        localVarQueryParameterBaseName = "maxStartDate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxStartDate", maxStartDate));
        localVarQueryParameterBaseName = "minEndDate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minEndDate", minEndDate));
        localVarQueryParameterBaseName = "maxEndDate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxEndDate", maxEndDate));
        localVarQueryParameterBaseName = "isMovie";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isMovie", isMovie));
        localVarQueryParameterBaseName = "isSeries";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSeries", isSeries));
        localVarQueryParameterBaseName = "isNews";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isNews", isNews));
        localVarQueryParameterBaseName = "isKids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isKids", isKids));
        localVarQueryParameterBaseName = "isSports";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSports", isSports));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "sortBy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParameterBaseName = "sortOrder";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortOrder", sortOrder));
        localVarQueryParameterBaseName = "genres";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "genres", genres));
        localVarQueryParameterBaseName = "genreIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "genreIds", genreIds));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "seriesTimerId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("seriesTimerId", seriesTimerId));
        localVarQueryParameterBaseName = "librarySeriesId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("librarySeriesId", librarySeriesId));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "enableTotalRecordCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableTotalRecordCount", enableTotalRecordCount));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets a live tv program.
     * 
     * @param programId Program id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getProgram(@org.eclipse.jdt.annotation.Nullable String programId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        ApiResponse<BaseItemDto> localVarResponse = getProgramWithHttpInfo(programId, userId);
        return localVarResponse.getData();
    }

    /**
     * Gets a live tv program.
     * 
     * @param programId Program id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getProgramWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String programId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getProgramRequestBuilder(programId, userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getProgram", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDto>() {
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

    private HttpRequest.Builder getProgramRequestBuilder(@org.eclipse.jdt.annotation.Nullable String programId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // verify the required parameter 'programId' is set
        if (programId == null) {
            throw new ApiException(400, "Missing the required parameter 'programId' when calling getProgram");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Programs/{programId}".replace("{programId}",
                ApiClient.urlEncode(programId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets available live tv epgs.
     * 
     * @param getProgramsDto Request body. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getPrograms(@org.eclipse.jdt.annotation.NonNull GetProgramsDto getProgramsDto)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getProgramsWithHttpInfo(getProgramsDto);
        return localVarResponse.getData();
    }

    /**
     * Gets available live tv epgs.
     * 
     * @param getProgramsDto Request body. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getProgramsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull GetProgramsDto getProgramsDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getProgramsRequestBuilder(getProgramsDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPrograms", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<BaseItemDtoQueryResult>() {
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

    private HttpRequest.Builder getProgramsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull GetProgramsDto getProgramsDto) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Programs";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(getProgramsDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets recommended live tv epgs.
     * 
     * @param userId Optional. filter by user id. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param isAiring Optional. Filter by programs that are currently airing, or not. (optional)
     * @param hasAired Optional. Filter by programs that have completed airing, or not. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param genreIds The genres to return guide information for. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. include user data. (optional)
     * @param enableTotalRecordCount Retrieve total record count. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getRecommendedPrograms(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull Boolean isAiring,
            @org.eclipse.jdt.annotation.NonNull Boolean hasAired, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getRecommendedProgramsWithHttpInfo(userId, limit,
                isAiring, hasAired, isSeries, isMovie, isNews, isKids, isSports, enableImages, imageTypeLimit,
                enableImageTypes, genreIds, fields, enableUserData, enableTotalRecordCount);
        return localVarResponse.getData();
    }

    /**
     * Gets recommended live tv epgs.
     * 
     * @param userId Optional. filter by user id. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param isAiring Optional. Filter by programs that are currently airing, or not. (optional)
     * @param hasAired Optional. Filter by programs that have completed airing, or not. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param genreIds The genres to return guide information for. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. include user data. (optional)
     * @param enableTotalRecordCount Retrieve total record count. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getRecommendedProgramsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean isAiring, @org.eclipse.jdt.annotation.NonNull Boolean hasAired,
            @org.eclipse.jdt.annotation.NonNull Boolean isSeries, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRecommendedProgramsRequestBuilder(userId, limit, isAiring,
                hasAired, isSeries, isMovie, isNews, isKids, isSports, enableImages, imageTypeLimit, enableImageTypes,
                genreIds, fields, enableUserData, enableTotalRecordCount);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRecommendedPrograms", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<BaseItemDtoQueryResult>() {
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

    private HttpRequest.Builder getRecommendedProgramsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull Boolean isAiring,
            @org.eclipse.jdt.annotation.NonNull Boolean hasAired, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Programs/Recommended";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "isAiring";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isAiring", isAiring));
        localVarQueryParameterBaseName = "hasAired";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasAired", hasAired));
        localVarQueryParameterBaseName = "isSeries";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSeries", isSeries));
        localVarQueryParameterBaseName = "isMovie";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isMovie", isMovie));
        localVarQueryParameterBaseName = "isNews";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isNews", isNews));
        localVarQueryParameterBaseName = "isKids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isKids", isKids));
        localVarQueryParameterBaseName = "isSports";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSports", isSports));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "genreIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "genreIds", genreIds));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "enableTotalRecordCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableTotalRecordCount", enableTotalRecordCount));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets a live tv recording.
     * 
     * @param recordingId Recording id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getRecording(@org.eclipse.jdt.annotation.Nullable UUID recordingId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        ApiResponse<BaseItemDto> localVarResponse = getRecordingWithHttpInfo(recordingId, userId);
        return localVarResponse.getData();
    }

    /**
     * Gets a live tv recording.
     * 
     * @param recordingId Recording id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getRecordingWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID recordingId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRecordingRequestBuilder(recordingId, userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRecording", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDto>() {
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

    private HttpRequest.Builder getRecordingRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID recordingId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // verify the required parameter 'recordingId' is set
        if (recordingId == null) {
            throw new ApiException(400, "Missing the required parameter 'recordingId' when calling getRecording");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Recordings/{recordingId}".replace("{recordingId}",
                ApiClient.urlEncode(recordingId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets recording folders.
     * 
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getRecordingFolders(@org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getRecordingFoldersWithHttpInfo(userId);
        return localVarResponse.getData();
    }

    /**
     * Gets recording folders.
     * 
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getRecordingFoldersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRecordingFoldersRequestBuilder(userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRecordingFolders", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<BaseItemDtoQueryResult>() {
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

    private HttpRequest.Builder getRecordingFoldersRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Recordings/Folders";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Get recording group.
     * 
     * @param groupId Group id. (required)
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public void getRecordingGroup(@org.eclipse.jdt.annotation.Nullable UUID groupId) throws ApiException {
        getRecordingGroupWithHttpInfo(groupId);
    }

    /**
     * Get recording group.
     * 
     * @param groupId Group id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> getRecordingGroupWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID groupId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRecordingGroupRequestBuilder(groupId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRecordingGroup", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getRecordingGroupRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID groupId)
            throws ApiException {
        // verify the required parameter 'groupId' is set
        if (groupId == null) {
            throw new ApiException(400, "Missing the required parameter 'groupId' when calling getRecordingGroup");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Recordings/Groups/{groupId}".replace("{groupId}",
                ApiClient.urlEncode(groupId.toString()));

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
     * Gets live tv recording groups.
     * 
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public BaseItemDtoQueryResult getRecordingGroups(@org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getRecordingGroupsWithHttpInfo(userId);
        return localVarResponse.getData();
    }

    /**
     * Gets live tv recording groups.
     * 
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<BaseItemDtoQueryResult> getRecordingGroupsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRecordingGroupsRequestBuilder(userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRecordingGroups", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<BaseItemDtoQueryResult>() {
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

    private HttpRequest.Builder getRecordingGroupsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Recordings/Groups";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets live tv recordings.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param status Optional. Filter by recording status. (optional)
     * @param isInProgress Optional. Filter by recordings that are in progress, or not. (optional)
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isLibraryItem Optional. Filter for is library item. (optional)
     * @param enableTotalRecordCount Optional. Return total record count. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getRecordings(@org.eclipse.jdt.annotation.NonNull String channelId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull RecordingStatus status,
            @org.eclipse.jdt.annotation.NonNull Boolean isInProgress,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isLibraryItem,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getRecordingsWithHttpInfo(channelId, userId, startIndex,
                limit, status, isInProgress, seriesTimerId, enableImages, imageTypeLimit, enableImageTypes, fields,
                enableUserData, isMovie, isSeries, isKids, isSports, isNews, isLibraryItem, enableTotalRecordCount);
        return localVarResponse.getData();
    }

    /**
     * Gets live tv recordings.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param status Optional. Filter by recording status. (optional)
     * @param isInProgress Optional. Filter by recordings that are in progress, or not. (optional)
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isLibraryItem Optional. Filter for is library item. (optional)
     * @param enableTotalRecordCount Optional. Return total record count. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getRecordingsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String channelId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull RecordingStatus status,
            @org.eclipse.jdt.annotation.NonNull Boolean isInProgress,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isLibraryItem,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRecordingsRequestBuilder(channelId, userId, startIndex, limit,
                status, isInProgress, seriesTimerId, enableImages, imageTypeLimit, enableImageTypes, fields,
                enableUserData, isMovie, isSeries, isKids, isSports, isNews, isLibraryItem, enableTotalRecordCount);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRecordings", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<BaseItemDtoQueryResult>() {
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

    private HttpRequest.Builder getRecordingsRequestBuilder(@org.eclipse.jdt.annotation.NonNull String channelId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull RecordingStatus status,
            @org.eclipse.jdt.annotation.NonNull Boolean isInProgress,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isLibraryItem,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Recordings";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "channelId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("channelId", channelId));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "status";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("status", status));
        localVarQueryParameterBaseName = "isInProgress";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isInProgress", isInProgress));
        localVarQueryParameterBaseName = "seriesTimerId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("seriesTimerId", seriesTimerId));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "isMovie";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isMovie", isMovie));
        localVarQueryParameterBaseName = "isSeries";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSeries", isSeries));
        localVarQueryParameterBaseName = "isKids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isKids", isKids));
        localVarQueryParameterBaseName = "isSports";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSports", isSports));
        localVarQueryParameterBaseName = "isNews";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isNews", isNews));
        localVarQueryParameterBaseName = "isLibraryItem";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isLibraryItem", isLibraryItem));
        localVarQueryParameterBaseName = "enableTotalRecordCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableTotalRecordCount", enableTotalRecordCount));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets live tv recording series.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param groupId Optional. Filter by recording group. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param status Optional. Filter by recording status. (optional)
     * @param isInProgress Optional. Filter by recordings that are in progress, or not. (optional)
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param enableTotalRecordCount Optional. Return total record count. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public BaseItemDtoQueryResult getRecordingsSeries(@org.eclipse.jdt.annotation.NonNull String channelId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull String groupId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull RecordingStatus status,
            @org.eclipse.jdt.annotation.NonNull Boolean isInProgress,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getRecordingsSeriesWithHttpInfo(channelId, userId,
                groupId, startIndex, limit, status, isInProgress, seriesTimerId, enableImages, imageTypeLimit,
                enableImageTypes, fields, enableUserData, enableTotalRecordCount);
        return localVarResponse.getData();
    }

    /**
     * Gets live tv recording series.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param groupId Optional. Filter by recording group. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param status Optional. Filter by recording status. (optional)
     * @param isInProgress Optional. Filter by recordings that are in progress, or not. (optional)
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param enableTotalRecordCount Optional. Return total record count. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<BaseItemDtoQueryResult> getRecordingsSeriesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String channelId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String groupId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull RecordingStatus status,
            @org.eclipse.jdt.annotation.NonNull Boolean isInProgress,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRecordingsSeriesRequestBuilder(channelId, userId, groupId,
                startIndex, limit, status, isInProgress, seriesTimerId, enableImages, imageTypeLimit, enableImageTypes,
                fields, enableUserData, enableTotalRecordCount);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRecordingsSeries", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<BaseItemDtoQueryResult>() {
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

    private HttpRequest.Builder getRecordingsSeriesRequestBuilder(@org.eclipse.jdt.annotation.NonNull String channelId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull String groupId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull RecordingStatus status,
            @org.eclipse.jdt.annotation.NonNull Boolean isInProgress,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Recordings/Series";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "channelId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("channelId", channelId));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "groupId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("groupId", groupId));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "status";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("status", status));
        localVarQueryParameterBaseName = "isInProgress";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isInProgress", isInProgress));
        localVarQueryParameterBaseName = "seriesTimerId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("seriesTimerId", seriesTimerId));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "enableTotalRecordCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableTotalRecordCount", enableTotalRecordCount));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets available countries.
     * 
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getSchedulesDirectCountries() throws ApiException {
        ApiResponse<File> localVarResponse = getSchedulesDirectCountriesWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets available countries.
     * 
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getSchedulesDirectCountriesWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSchedulesDirectCountriesRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSchedulesDirectCountries", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
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

    private HttpRequest.Builder getSchedulesDirectCountriesRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/ListingProviders/SchedulesDirect/Countries";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

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
     * Gets a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @return SeriesTimerInfoDto
     * @throws ApiException if fails to make API call
     */
    public SeriesTimerInfoDto getSeriesTimer(@org.eclipse.jdt.annotation.Nullable String timerId) throws ApiException {
        ApiResponse<SeriesTimerInfoDto> localVarResponse = getSeriesTimerWithHttpInfo(timerId);
        return localVarResponse.getData();
    }

    /**
     * Gets a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @return ApiResponse&lt;SeriesTimerInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<SeriesTimerInfoDto> getSeriesTimerWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String timerId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSeriesTimerRequestBuilder(timerId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSeriesTimer", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<SeriesTimerInfoDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<SeriesTimerInfoDto>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<SeriesTimerInfoDto>() {
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

    private HttpRequest.Builder getSeriesTimerRequestBuilder(@org.eclipse.jdt.annotation.Nullable String timerId)
            throws ApiException {
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling getSeriesTimer");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/SeriesTimers/{timerId}".replace("{timerId}",
                ApiClient.urlEncode(timerId.toString()));

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
     * Gets live tv series timers.
     * 
     * @param sortBy Optional. Sort by SortName or Priority. (optional)
     * @param sortOrder Optional. Sort in Ascending or Descending order. (optional)
     * @return SeriesTimerInfoDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public SeriesTimerInfoDtoQueryResult getSeriesTimers(@org.eclipse.jdt.annotation.NonNull String sortBy,
            @org.eclipse.jdt.annotation.NonNull SortOrder sortOrder) throws ApiException {
        ApiResponse<SeriesTimerInfoDtoQueryResult> localVarResponse = getSeriesTimersWithHttpInfo(sortBy, sortOrder);
        return localVarResponse.getData();
    }

    /**
     * Gets live tv series timers.
     * 
     * @param sortBy Optional. Sort by SortName or Priority. (optional)
     * @param sortOrder Optional. Sort in Ascending or Descending order. (optional)
     * @return ApiResponse&lt;SeriesTimerInfoDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<SeriesTimerInfoDtoQueryResult> getSeriesTimersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String sortBy, @org.eclipse.jdt.annotation.NonNull SortOrder sortOrder)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSeriesTimersRequestBuilder(sortBy, sortOrder);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSeriesTimers", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<SeriesTimerInfoDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<SeriesTimerInfoDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<SeriesTimerInfoDtoQueryResult>() {
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

    private HttpRequest.Builder getSeriesTimersRequestBuilder(@org.eclipse.jdt.annotation.NonNull String sortBy,
            @org.eclipse.jdt.annotation.NonNull SortOrder sortOrder) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/SeriesTimers";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "sortBy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("sortBy", sortBy));
        localVarQueryParameterBaseName = "sortOrder";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("sortOrder", sortOrder));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Gets a timer.
     * 
     * @param timerId Timer id. (required)
     * @return TimerInfoDto
     * @throws ApiException if fails to make API call
     */
    public TimerInfoDto getTimer(@org.eclipse.jdt.annotation.Nullable String timerId) throws ApiException {
        ApiResponse<TimerInfoDto> localVarResponse = getTimerWithHttpInfo(timerId);
        return localVarResponse.getData();
    }

    /**
     * Gets a timer.
     * 
     * @param timerId Timer id. (required)
     * @return ApiResponse&lt;TimerInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<TimerInfoDto> getTimerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String timerId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getTimerRequestBuilder(timerId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getTimer", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<TimerInfoDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<TimerInfoDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<TimerInfoDto>() {
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

    private HttpRequest.Builder getTimerRequestBuilder(@org.eclipse.jdt.annotation.Nullable String timerId)
            throws ApiException {
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling getTimer");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Timers/{timerId}".replace("{timerId}", ApiClient.urlEncode(timerId.toString()));

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
     * Gets the live tv timers.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param seriesTimerId Optional. Filter by timers belonging to a series timer. (optional)
     * @param isActive Optional. Filter by timers that are active. (optional)
     * @param isScheduled Optional. Filter by timers that are scheduled. (optional)
     * @return TimerInfoDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public TimerInfoDtoQueryResult getTimers(@org.eclipse.jdt.annotation.NonNull String channelId,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean isActive,
            @org.eclipse.jdt.annotation.NonNull Boolean isScheduled) throws ApiException {
        ApiResponse<TimerInfoDtoQueryResult> localVarResponse = getTimersWithHttpInfo(channelId, seriesTimerId,
                isActive, isScheduled);
        return localVarResponse.getData();
    }

    /**
     * Gets the live tv timers.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param seriesTimerId Optional. Filter by timers belonging to a series timer. (optional)
     * @param isActive Optional. Filter by timers that are active. (optional)
     * @param isScheduled Optional. Filter by timers that are scheduled. (optional)
     * @return ApiResponse&lt;TimerInfoDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<TimerInfoDtoQueryResult> getTimersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String channelId,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean isActive,
            @org.eclipse.jdt.annotation.NonNull Boolean isScheduled) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getTimersRequestBuilder(channelId, seriesTimerId, isActive,
                isScheduled);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getTimers", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<TimerInfoDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<TimerInfoDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<TimerInfoDtoQueryResult>() {
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

    private HttpRequest.Builder getTimersRequestBuilder(@org.eclipse.jdt.annotation.NonNull String channelId,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean isActive,
            @org.eclipse.jdt.annotation.NonNull Boolean isScheduled) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Timers";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "channelId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("channelId", channelId));
        localVarQueryParameterBaseName = "seriesTimerId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("seriesTimerId", seriesTimerId));
        localVarQueryParameterBaseName = "isActive";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isActive", isActive));
        localVarQueryParameterBaseName = "isScheduled";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isScheduled", isScheduled));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

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
     * Get tuner host types.
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws ApiException if fails to make API call
     */
    public List<NameIdPair> getTunerHostTypes() throws ApiException {
        ApiResponse<List<NameIdPair>> localVarResponse = getTunerHostTypesWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Get tuner host types.
     * 
     * @return ApiResponse&lt;List&lt;NameIdPair&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<NameIdPair>> getTunerHostTypesWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getTunerHostTypesRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getTunerHostTypes", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<NameIdPair>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<NameIdPair>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<NameIdPair>>() {
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

    private HttpRequest.Builder getTunerHostTypesRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/TunerHosts/Types";

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
     * Resets a tv tuner.
     * 
     * @param tunerId Tuner id. (required)
     * @throws ApiException if fails to make API call
     */
    public void resetTuner(@org.eclipse.jdt.annotation.Nullable String tunerId) throws ApiException {
        resetTunerWithHttpInfo(tunerId);
    }

    /**
     * Resets a tv tuner.
     * 
     * @param tunerId Tuner id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> resetTunerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String tunerId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = resetTunerRequestBuilder(tunerId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("resetTuner", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder resetTunerRequestBuilder(@org.eclipse.jdt.annotation.Nullable String tunerId)
            throws ApiException {
        // verify the required parameter 'tunerId' is set
        if (tunerId == null) {
            throw new ApiException(400, "Missing the required parameter 'tunerId' when calling resetTuner");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Tuners/{tunerId}/Reset".replace("{tunerId}",
                ApiClient.urlEncode(tunerId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Set channel mappings.
     * 
     * @param setChannelMappingDto The set channel mapping dto. (required)
     * @return TunerChannelMapping
     * @throws ApiException if fails to make API call
     */
    public TunerChannelMapping setChannelMapping(
            @org.eclipse.jdt.annotation.Nullable SetChannelMappingDto setChannelMappingDto) throws ApiException {
        ApiResponse<TunerChannelMapping> localVarResponse = setChannelMappingWithHttpInfo(setChannelMappingDto);
        return localVarResponse.getData();
    }

    /**
     * Set channel mappings.
     * 
     * @param setChannelMappingDto The set channel mapping dto. (required)
     * @return ApiResponse&lt;TunerChannelMapping&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<TunerChannelMapping> setChannelMappingWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable SetChannelMappingDto setChannelMappingDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = setChannelMappingRequestBuilder(setChannelMappingDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("setChannelMapping", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<TunerChannelMapping>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<TunerChannelMapping>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<TunerChannelMapping>() {
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

    private HttpRequest.Builder setChannelMappingRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable SetChannelMappingDto setChannelMappingDto) throws ApiException {
        // verify the required parameter 'setChannelMappingDto' is set
        if (setChannelMappingDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'setChannelMappingDto' when calling setChannelMapping");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/ChannelMappings";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(setChannelMappingDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Updates a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @param seriesTimerInfoDto New series timer info. (optional)
     * @throws ApiException if fails to make API call
     */
    public void updateSeriesTimer(@org.eclipse.jdt.annotation.Nullable String timerId,
            @org.eclipse.jdt.annotation.NonNull SeriesTimerInfoDto seriesTimerInfoDto) throws ApiException {
        updateSeriesTimerWithHttpInfo(timerId, seriesTimerInfoDto);
    }

    /**
     * Updates a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @param seriesTimerInfoDto New series timer info. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateSeriesTimerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String timerId,
            @org.eclipse.jdt.annotation.NonNull SeriesTimerInfoDto seriesTimerInfoDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateSeriesTimerRequestBuilder(timerId, seriesTimerInfoDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateSeriesTimer", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder updateSeriesTimerRequestBuilder(@org.eclipse.jdt.annotation.Nullable String timerId,
            @org.eclipse.jdt.annotation.NonNull SeriesTimerInfoDto seriesTimerInfoDto) throws ApiException {
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling updateSeriesTimer");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/SeriesTimers/{timerId}".replace("{timerId}",
                ApiClient.urlEncode(timerId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(seriesTimerInfoDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Updates a live tv timer.
     * 
     * @param timerId Timer id. (required)
     * @param timerInfoDto New timer info. (optional)
     * @throws ApiException if fails to make API call
     */
    public void updateTimer(@org.eclipse.jdt.annotation.Nullable String timerId,
            @org.eclipse.jdt.annotation.NonNull TimerInfoDto timerInfoDto) throws ApiException {
        updateTimerWithHttpInfo(timerId, timerInfoDto);
    }

    /**
     * Updates a live tv timer.
     * 
     * @param timerId Timer id. (required)
     * @param timerInfoDto New timer info. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateTimerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String timerId,
            @org.eclipse.jdt.annotation.NonNull TimerInfoDto timerInfoDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateTimerRequestBuilder(timerId, timerInfoDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateTimer", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder updateTimerRequestBuilder(@org.eclipse.jdt.annotation.Nullable String timerId,
            @org.eclipse.jdt.annotation.NonNull TimerInfoDto timerInfoDto) throws ApiException {
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling updateTimer");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveTv/Timers/{timerId}".replace("{timerId}", ApiClient.urlEncode(timerId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(timerInfoDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
