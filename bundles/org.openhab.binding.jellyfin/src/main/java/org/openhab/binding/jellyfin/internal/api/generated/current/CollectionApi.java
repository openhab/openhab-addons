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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.CollectionCreationResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class CollectionApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public CollectionApi() {
        this(Configuration.getDefaultApiClient());
    }

    public CollectionApi(ApiClient apiClient) {
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
     * Adds items to a collection.
     * 
     * @param collectionId The collection id. (required)
     * @param ids Item ids, comma delimited. (required)
     * @throws ApiException if fails to make API call
     */
    public void addToCollection(@org.eclipse.jdt.annotation.Nullable UUID collectionId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids) throws ApiException {
        addToCollectionWithHttpInfo(collectionId, ids);
    }

    /**
     * Adds items to a collection.
     * 
     * @param collectionId The collection id. (required)
     * @param ids Item ids, comma delimited. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> addToCollectionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID collectionId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = addToCollectionRequestBuilder(collectionId, ids);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("addToCollection", localVarResponse);
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

    private HttpRequest.Builder addToCollectionRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID collectionId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids) throws ApiException {
        // verify the required parameter 'collectionId' is set
        if (collectionId == null) {
            throw new ApiException(400, "Missing the required parameter 'collectionId' when calling addToCollection");
        }
        // verify the required parameter 'ids' is set
        if (ids == null) {
            throw new ApiException(400, "Missing the required parameter 'ids' when calling addToCollection");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Collections/{collectionId}/Items".replace("{collectionId}",
                ApiClient.urlEncode(collectionId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "ids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "ids", ids));

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
     * Creates a new collection.
     * 
     * @param name The name of the collection. (optional)
     * @param ids Item Ids to add to the collection. (optional)
     * @param parentId Optional. Create the collection within a specific folder. (optional)
     * @param isLocked Whether or not to lock the new collection. (optional, default to false)
     * @return CollectionCreationResult
     * @throws ApiException if fails to make API call
     */
    public CollectionCreationResult createCollection(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull List<String> ids, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull Boolean isLocked) throws ApiException {
        ApiResponse<CollectionCreationResult> localVarResponse = createCollectionWithHttpInfo(name, ids, parentId,
                isLocked);
        return localVarResponse.getData();
    }

    /**
     * Creates a new collection.
     * 
     * @param name The name of the collection. (optional)
     * @param ids Item Ids to add to the collection. (optional)
     * @param parentId Optional. Create the collection within a specific folder. (optional)
     * @param isLocked Whether or not to lock the new collection. (optional, default to false)
     * @return ApiResponse&lt;CollectionCreationResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<CollectionCreationResult> createCollectionWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String name, @org.eclipse.jdt.annotation.NonNull List<String> ids,
            @org.eclipse.jdt.annotation.NonNull UUID parentId, @org.eclipse.jdt.annotation.NonNull Boolean isLocked)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = createCollectionRequestBuilder(name, ids, parentId, isLocked);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("createCollection", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<CollectionCreationResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<CollectionCreationResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<CollectionCreationResult>() {
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

    private HttpRequest.Builder createCollectionRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull List<String> ids, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull Boolean isLocked) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Collections";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "name";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("name", name));
        localVarQueryParameterBaseName = "ids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "ids", ids));
        localVarQueryParameterBaseName = "parentId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentId", parentId));
        localVarQueryParameterBaseName = "isLocked";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isLocked", isLocked));

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
     * Removes items from a collection.
     * 
     * @param collectionId The collection id. (required)
     * @param ids Item ids, comma delimited. (required)
     * @throws ApiException if fails to make API call
     */
    public void removeFromCollection(@org.eclipse.jdt.annotation.Nullable UUID collectionId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids) throws ApiException {
        removeFromCollectionWithHttpInfo(collectionId, ids);
    }

    /**
     * Removes items from a collection.
     * 
     * @param collectionId The collection id. (required)
     * @param ids Item ids, comma delimited. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeFromCollectionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID collectionId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = removeFromCollectionRequestBuilder(collectionId, ids);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("removeFromCollection", localVarResponse);
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

    private HttpRequest.Builder removeFromCollectionRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UUID collectionId, @org.eclipse.jdt.annotation.Nullable List<UUID> ids)
            throws ApiException {
        // verify the required parameter 'collectionId' is set
        if (collectionId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'collectionId' when calling removeFromCollection");
        }
        // verify the required parameter 'ids' is set
        if (ids == null) {
            throw new ApiException(400, "Missing the required parameter 'ids' when calling removeFromCollection");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Collections/{collectionId}/Items".replace("{collectionId}",
                ApiClient.urlEncode(collectionId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "ids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "ids", ids));

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
}
