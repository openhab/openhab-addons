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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.AllThemeMediaResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.CollectionType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemCounts;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.LibraryOptionsResultDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaUpdateInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SortOrder;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ThemeMediaResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LibraryApi {
    /**
     * Utility class for extending HttpRequest.Builder functionality.
     */
    private static class HttpRequestBuilderExtensions {
        /**
         * Adds additional headers to the provided HttpRequest.Builder. Useful for adding method/endpoint specific
         * headers.
         *
         * @param builder the HttpRequest.Builder to which headers will be added
         * @param headers a map of header names and values to add; may be null
         * @return the same HttpRequest.Builder instance with the additional headers set
         */
        static HttpRequest.Builder withAdditionalHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }
            return builder;
        }
    }

    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public LibraryApi() {
        this(Configuration.getDefaultApiClient());
    }

    public LibraryApi(ApiClient apiClient) {
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
     * Download file from the given response.
     *
     * @param response Response
     * @return File
     * @throws ApiException If fail to read file content from response and write to disk
     */
    public File downloadFileFromResponse(HttpResponse<InputStream> response) throws ApiException {
        try {
            File file = prepareDownloadFile(response);
            java.nio.file.Files.copy(response.body(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return file;
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    /**
     * <p>
     * Prepare the file for download from the response.
     * </p>
     *
     * @param response a {@link java.net.http.HttpResponse} object.
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    private File prepareDownloadFile(HttpResponse<InputStream> response) throws IOException {
        String filename = null;
        java.util.Optional<String> contentDisposition = response.headers().firstValue("Content-Disposition");
        if (contentDisposition.isPresent() && !"".equals(contentDisposition.get())) {
            // Get filename from the Content-Disposition header.
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("filename=['\"]?([^'\"\\s]+)['\"]?");
            java.util.regex.Matcher matcher = pattern.matcher(contentDisposition.get());
            if (matcher.find())
                filename = matcher.group(1);
        }
        File file = null;
        if (filename != null) {
            java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("swagger-gen-native");
            java.nio.file.Path filePath = java.nio.file.Files.createFile(tempDir.resolve(filename));
            file = filePath.toFile();
            tempDir.toFile().deleteOnExit(); // best effort cleanup
            file.deleteOnExit(); // best effort cleanup
        } else {
            file = java.nio.file.Files.createTempFile("download-", "").toFile();
            file.deleteOnExit(); // best effort cleanup
        }
        return file;
    }

    /**
     * Deletes an item from the library and filesystem.
     * 
     * @param itemId The item id. (required)
     * @throws ApiException if fails to make API call
     */
    public void deleteItem(@org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        deleteItem(itemId, null);
    }

    /**
     * Deletes an item from the library and filesystem.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteItem(@org.eclipse.jdt.annotation.Nullable UUID itemId, Map<String, String> headers)
            throws ApiException {
        deleteItemWithHttpInfo(itemId, headers);
    }

    /**
     * Deletes an item from the library and filesystem.
     * 
     * @param itemId The item id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        return deleteItemWithHttpInfo(itemId, null);
    }

    /**
     * Deletes an item from the library and filesystem.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteItemRequestBuilder(itemId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteItem", localVarResponse);
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

    private HttpRequest.Builder deleteItemRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Deletes items from the library and filesystem.
     * 
     * @param ids The item ids. (optional)
     * @throws ApiException if fails to make API call
     */
    public void deleteItems(@org.eclipse.jdt.annotation.NonNull List<UUID> ids) throws ApiException {
        deleteItems(ids, null);
    }

    /**
     * Deletes items from the library and filesystem.
     * 
     * @param ids The item ids. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteItems(@org.eclipse.jdt.annotation.NonNull List<UUID> ids, Map<String, String> headers)
            throws ApiException {
        deleteItemsWithHttpInfo(ids, headers);
    }

    /**
     * Deletes items from the library and filesystem.
     * 
     * @param ids The item ids. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteItemsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull List<UUID> ids)
            throws ApiException {
        return deleteItemsWithHttpInfo(ids, null);
    }

    /**
     * Deletes items from the library and filesystem.
     * 
     * @param ids The item ids. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteItemsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull List<UUID> ids,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteItemsRequestBuilder(ids, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteItems", localVarResponse);
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

    private HttpRequest.Builder deleteItemsRequestBuilder(@org.eclipse.jdt.annotation.NonNull List<UUID> ids,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items";

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

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets all parents of an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return List&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<BaseItemDto> getAncestors(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getAncestors(itemId, userId, null);
    }

    /**
     * Gets all parents of an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param headers Optional headers to include in the request
     * @return List&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<BaseItemDto> getAncestors(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<List<BaseItemDto>> localVarResponse = getAncestorsWithHttpInfo(itemId, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets all parents of an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return ApiResponse&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<BaseItemDto>> getAncestorsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getAncestorsWithHttpInfo(itemId, userId, null);
    }

    /**
     * Gets all parents of an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<BaseItemDto>> getAncestorsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getAncestorsRequestBuilder(itemId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getAncestors", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<BaseItemDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<BaseItemDto> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<BaseItemDto>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<BaseItemDto>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getAncestorsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getAncestors");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Ancestors".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets critic review for an item.
     * 
     * @param itemId (required)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public BaseItemDtoQueryResult getCriticReviews(@org.eclipse.jdt.annotation.Nullable String itemId)
            throws ApiException {
        return getCriticReviews(itemId, null);
    }

    /**
     * Gets critic review for an item.
     * 
     * @param itemId (required)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public BaseItemDtoQueryResult getCriticReviews(@org.eclipse.jdt.annotation.Nullable String itemId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getCriticReviewsWithHttpInfo(itemId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets critic review for an item.
     * 
     * @param itemId (required)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<BaseItemDtoQueryResult> getCriticReviewsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String itemId) throws ApiException {
        return getCriticReviewsWithHttpInfo(itemId, null);
    }

    /**
     * Gets critic review for an item.
     * 
     * @param itemId (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<BaseItemDtoQueryResult> getCriticReviewsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String itemId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getCriticReviewsRequestBuilder(itemId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getCriticReviews", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getCriticReviewsRequestBuilder(@org.eclipse.jdt.annotation.Nullable String itemId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getCriticReviews");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/CriticReviews".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Downloads item media.
     * 
     * @param itemId The item id. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getDownload(@org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        return getDownload(itemId, null);
    }

    /**
     * Downloads item media.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getDownload(@org.eclipse.jdt.annotation.Nullable UUID itemId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getDownloadWithHttpInfo(itemId, headers);
        return localVarResponse.getData();
    }

    /**
     * Downloads item media.
     * 
     * @param itemId The item id. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getDownloadWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        return getDownloadWithHttpInfo(itemId, null);
    }

    /**
     * Downloads item media.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getDownloadWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDownloadRequestBuilder(itemId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDownload", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getDownloadRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getDownload");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Download".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "video/*, audio/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Get the original file of an item.
     * 
     * @param itemId The item id. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getFile(@org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        return getFile(itemId, null);
    }

    /**
     * Get the original file of an item.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getFile(@org.eclipse.jdt.annotation.Nullable UUID itemId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getFileWithHttpInfo(itemId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get the original file of an item.
     * 
     * @param itemId The item id. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getFileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        return getFileWithHttpInfo(itemId, null);
    }

    /**
     * Get the original file of an item.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getFileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getFileRequestBuilder(itemId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getFile", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getFileRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getFile");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/File".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "video/*, audio/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Get item counts.
     * 
     * @param userId Optional. Get counts from a specific user&#39;s library. (optional)
     * @param isFavorite Optional. Get counts of favorite items. (optional)
     * @return ItemCounts
     * @throws ApiException if fails to make API call
     */
    public ItemCounts getItemCounts(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite) throws ApiException {
        return getItemCounts(userId, isFavorite, null);
    }

    /**
     * Get item counts.
     * 
     * @param userId Optional. Get counts from a specific user&#39;s library. (optional)
     * @param isFavorite Optional. Get counts of favorite items. (optional)
     * @param headers Optional headers to include in the request
     * @return ItemCounts
     * @throws ApiException if fails to make API call
     */
    public ItemCounts getItemCounts(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite, Map<String, String> headers) throws ApiException {
        ApiResponse<ItemCounts> localVarResponse = getItemCountsWithHttpInfo(userId, isFavorite, headers);
        return localVarResponse.getData();
    }

    /**
     * Get item counts.
     * 
     * @param userId Optional. Get counts from a specific user&#39;s library. (optional)
     * @param isFavorite Optional. Get counts of favorite items. (optional)
     * @return ApiResponse&lt;ItemCounts&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ItemCounts> getItemCountsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite) throws ApiException {
        return getItemCountsWithHttpInfo(userId, isFavorite, null);
    }

    /**
     * Get item counts.
     * 
     * @param userId Optional. Get counts from a specific user&#39;s library. (optional)
     * @param isFavorite Optional. Get counts of favorite items. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;ItemCounts&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ItemCounts> getItemCountsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getItemCountsRequestBuilder(userId, isFavorite, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getItemCounts", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<ItemCounts>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                ItemCounts responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<ItemCounts>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<ItemCounts>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getItemCountsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/Counts";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "isFavorite";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isFavorite", isFavorite));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets the library options info.
     * 
     * @param libraryContentType Library content type. (optional)
     * @param isNewLibrary Whether this is a new library. (optional, default to false)
     * @return LibraryOptionsResultDto
     * @throws ApiException if fails to make API call
     */
    public LibraryOptionsResultDto getLibraryOptionsInfo(
            @org.eclipse.jdt.annotation.NonNull CollectionType libraryContentType,
            @org.eclipse.jdt.annotation.NonNull Boolean isNewLibrary) throws ApiException {
        return getLibraryOptionsInfo(libraryContentType, isNewLibrary, null);
    }

    /**
     * Gets the library options info.
     * 
     * @param libraryContentType Library content type. (optional)
     * @param isNewLibrary Whether this is a new library. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return LibraryOptionsResultDto
     * @throws ApiException if fails to make API call
     */
    public LibraryOptionsResultDto getLibraryOptionsInfo(
            @org.eclipse.jdt.annotation.NonNull CollectionType libraryContentType,
            @org.eclipse.jdt.annotation.NonNull Boolean isNewLibrary, Map<String, String> headers) throws ApiException {
        ApiResponse<LibraryOptionsResultDto> localVarResponse = getLibraryOptionsInfoWithHttpInfo(libraryContentType,
                isNewLibrary, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the library options info.
     * 
     * @param libraryContentType Library content type. (optional)
     * @param isNewLibrary Whether this is a new library. (optional, default to false)
     * @return ApiResponse&lt;LibraryOptionsResultDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LibraryOptionsResultDto> getLibraryOptionsInfoWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull CollectionType libraryContentType,
            @org.eclipse.jdt.annotation.NonNull Boolean isNewLibrary) throws ApiException {
        return getLibraryOptionsInfoWithHttpInfo(libraryContentType, isNewLibrary, null);
    }

    /**
     * Gets the library options info.
     * 
     * @param libraryContentType Library content type. (optional)
     * @param isNewLibrary Whether this is a new library. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;LibraryOptionsResultDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LibraryOptionsResultDto> getLibraryOptionsInfoWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull CollectionType libraryContentType,
            @org.eclipse.jdt.annotation.NonNull Boolean isNewLibrary, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLibraryOptionsInfoRequestBuilder(libraryContentType,
                isNewLibrary, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLibraryOptionsInfo", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<LibraryOptionsResultDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                LibraryOptionsResultDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<LibraryOptionsResultDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<LibraryOptionsResultDto>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getLibraryOptionsInfoRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull CollectionType libraryContentType,
            @org.eclipse.jdt.annotation.NonNull Boolean isNewLibrary, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Libraries/AvailableOptions";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "libraryContentType";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("libraryContentType", libraryContentType));
        localVarQueryParameterBaseName = "isNewLibrary";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isNewLibrary", isNewLibrary));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets all user media folders.
     * 
     * @param isHidden Optional. Filter by folders that are marked hidden, or not. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getMediaFolders(@org.eclipse.jdt.annotation.NonNull Boolean isHidden)
            throws ApiException {
        return getMediaFolders(isHidden, null);
    }

    /**
     * Gets all user media folders.
     * 
     * @param isHidden Optional. Filter by folders that are marked hidden, or not. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getMediaFolders(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getMediaFoldersWithHttpInfo(isHidden, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets all user media folders.
     * 
     * @param isHidden Optional. Filter by folders that are marked hidden, or not. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getMediaFoldersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Boolean isHidden) throws ApiException {
        return getMediaFoldersWithHttpInfo(isHidden, null);
    }

    /**
     * Gets all user media folders.
     * 
     * @param isHidden Optional. Filter by folders that are marked hidden, or not. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getMediaFoldersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Boolean isHidden, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMediaFoldersRequestBuilder(isHidden, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMediaFolders", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getMediaFoldersRequestBuilder(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/MediaFolders";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "isHidden";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isHidden", isHidden));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets a list of physical paths from virtual folders.
     * 
     * @return List&lt;String&gt;
     * @throws ApiException if fails to make API call
     */
    public List<String> getPhysicalPaths() throws ApiException {
        return getPhysicalPaths(null);
    }

    /**
     * Gets a list of physical paths from virtual folders.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;String&gt;
     * @throws ApiException if fails to make API call
     */
    public List<String> getPhysicalPaths(Map<String, String> headers) throws ApiException {
        ApiResponse<List<String>> localVarResponse = getPhysicalPathsWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a list of physical paths from virtual folders.
     * 
     * @return ApiResponse&lt;List&lt;String&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<String>> getPhysicalPathsWithHttpInfo() throws ApiException {
        return getPhysicalPathsWithHttpInfo(null);
    }

    /**
     * Gets a list of physical paths from virtual folders.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;String&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<String>> getPhysicalPathsWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPhysicalPathsRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPhysicalPaths", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<String>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<String> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<String>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<String>>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getPhysicalPathsRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/PhysicalPaths";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarAlbums(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarAlbums(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarAlbums(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getSimilarAlbumsWithHttpInfo(itemId, excludeArtistIds,
                userId, limit, fields, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarAlbumsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarAlbumsWithHttpInfo(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarAlbumsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSimilarAlbumsRequestBuilder(itemId, excludeArtistIds, userId,
                limit, fields, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSimilarAlbums", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getSimilarAlbumsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getSimilarAlbums");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Albums/{itemId}/Similar".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "excludeArtistIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeArtistIds", excludeArtistIds));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarArtists(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarArtists(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarArtists(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getSimilarArtistsWithHttpInfo(itemId, excludeArtistIds,
                userId, limit, fields, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarArtistsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarArtistsWithHttpInfo(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarArtistsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSimilarArtistsRequestBuilder(itemId, excludeArtistIds, userId,
                limit, fields, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSimilarArtists", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getSimilarArtistsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getSimilarArtists");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Artists/{itemId}/Similar".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "excludeArtistIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeArtistIds", excludeArtistIds));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarItems(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarItems(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarItems(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getSimilarItemsWithHttpInfo(itemId, excludeArtistIds,
                userId, limit, fields, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarItemsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarItemsWithHttpInfo(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarItemsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSimilarItemsRequestBuilder(itemId, excludeArtistIds, userId,
                limit, fields, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSimilarItems", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getSimilarItemsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getSimilarItems");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Similar".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "excludeArtistIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeArtistIds", excludeArtistIds));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarMovies(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarMovies(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarMovies(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getSimilarMoviesWithHttpInfo(itemId, excludeArtistIds,
                userId, limit, fields, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarMoviesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarMoviesWithHttpInfo(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarMoviesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSimilarMoviesRequestBuilder(itemId, excludeArtistIds, userId,
                limit, fields, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSimilarMovies", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getSimilarMoviesRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getSimilarMovies");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Movies/{itemId}/Similar".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "excludeArtistIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeArtistIds", excludeArtistIds));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarShows(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarShows(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarShows(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getSimilarShowsWithHttpInfo(itemId, excludeArtistIds,
                userId, limit, fields, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarShowsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarShowsWithHttpInfo(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarShowsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSimilarShowsRequestBuilder(itemId, excludeArtistIds, userId,
                limit, fields, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSimilarShows", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getSimilarShowsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getSimilarShows");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Shows/{itemId}/Similar".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "excludeArtistIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeArtistIds", excludeArtistIds));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarTrailers(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarTrailers(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSimilarTrailers(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getSimilarTrailersWithHttpInfo(itemId, excludeArtistIds,
                userId, limit, fields, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarTrailersWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getSimilarTrailersWithHttpInfo(itemId, excludeArtistIds, userId, limit, fields, null);
    }

    /**
     * Gets similar items.
     * 
     * @param itemId The item id. (required)
     * @param excludeArtistIds Exclude artist ids. (optional)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSimilarTrailersWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSimilarTrailersRequestBuilder(itemId, excludeArtistIds, userId,
                limit, fields, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSimilarTrailers", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getSimilarTrailersRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getSimilarTrailers");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Trailers/{itemId}/Similar".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "excludeArtistIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeArtistIds", excludeArtistIds));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Get theme songs and videos for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @return AllThemeMediaResult
     * @throws ApiException if fails to make API call
     */
    public AllThemeMediaResult getThemeMedia(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder) throws ApiException {
        return getThemeMedia(itemId, userId, inheritFromParent, sortBy, sortOrder, null);
    }

    /**
     * Get theme songs and videos for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @param headers Optional headers to include in the request
     * @return AllThemeMediaResult
     * @throws ApiException if fails to make API call
     */
    public AllThemeMediaResult getThemeMedia(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder, Map<String, String> headers)
            throws ApiException {
        ApiResponse<AllThemeMediaResult> localVarResponse = getThemeMediaWithHttpInfo(itemId, userId, inheritFromParent,
                sortBy, sortOrder, headers);
        return localVarResponse.getData();
    }

    /**
     * Get theme songs and videos for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @return ApiResponse&lt;AllThemeMediaResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<AllThemeMediaResult> getThemeMediaWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder) throws ApiException {
        return getThemeMediaWithHttpInfo(itemId, userId, inheritFromParent, sortBy, sortOrder, null);
    }

    /**
     * Get theme songs and videos for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;AllThemeMediaResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<AllThemeMediaResult> getThemeMediaWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getThemeMediaRequestBuilder(itemId, userId, inheritFromParent,
                sortBy, sortOrder, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getThemeMedia", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<AllThemeMediaResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                AllThemeMediaResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<AllThemeMediaResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<AllThemeMediaResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getThemeMediaRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getThemeMedia");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/ThemeMedia".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "inheritFromParent";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("inheritFromParent", inheritFromParent));
        localVarQueryParameterBaseName = "sortBy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParameterBaseName = "sortOrder";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortOrder", sortOrder));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Get theme songs for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @return ThemeMediaResult
     * @throws ApiException if fails to make API call
     */
    public ThemeMediaResult getThemeSongs(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder) throws ApiException {
        return getThemeSongs(itemId, userId, inheritFromParent, sortBy, sortOrder, null);
    }

    /**
     * Get theme songs for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @param headers Optional headers to include in the request
     * @return ThemeMediaResult
     * @throws ApiException if fails to make API call
     */
    public ThemeMediaResult getThemeSongs(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder, Map<String, String> headers)
            throws ApiException {
        ApiResponse<ThemeMediaResult> localVarResponse = getThemeSongsWithHttpInfo(itemId, userId, inheritFromParent,
                sortBy, sortOrder, headers);
        return localVarResponse.getData();
    }

    /**
     * Get theme songs for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @return ApiResponse&lt;ThemeMediaResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ThemeMediaResult> getThemeSongsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder) throws ApiException {
        return getThemeSongsWithHttpInfo(itemId, userId, inheritFromParent, sortBy, sortOrder, null);
    }

    /**
     * Get theme songs for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;ThemeMediaResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ThemeMediaResult> getThemeSongsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getThemeSongsRequestBuilder(itemId, userId, inheritFromParent,
                sortBy, sortOrder, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getThemeSongs", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<ThemeMediaResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                ThemeMediaResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<ThemeMediaResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<ThemeMediaResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getThemeSongsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getThemeSongs");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/ThemeSongs".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "inheritFromParent";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("inheritFromParent", inheritFromParent));
        localVarQueryParameterBaseName = "sortBy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParameterBaseName = "sortOrder";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortOrder", sortOrder));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Get theme videos for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @return ThemeMediaResult
     * @throws ApiException if fails to make API call
     */
    public ThemeMediaResult getThemeVideos(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder) throws ApiException {
        return getThemeVideos(itemId, userId, inheritFromParent, sortBy, sortOrder, null);
    }

    /**
     * Get theme videos for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @param headers Optional headers to include in the request
     * @return ThemeMediaResult
     * @throws ApiException if fails to make API call
     */
    public ThemeMediaResult getThemeVideos(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder, Map<String, String> headers)
            throws ApiException {
        ApiResponse<ThemeMediaResult> localVarResponse = getThemeVideosWithHttpInfo(itemId, userId, inheritFromParent,
                sortBy, sortOrder, headers);
        return localVarResponse.getData();
    }

    /**
     * Get theme videos for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @return ApiResponse&lt;ThemeMediaResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ThemeMediaResult> getThemeVideosWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder) throws ApiException {
        return getThemeVideosWithHttpInfo(itemId, userId, inheritFromParent, sortBy, sortOrder, null);
    }

    /**
     * Get theme videos for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     *            (optional, default to false)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param sortOrder Optional. Sort Order - Ascending, Descending. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;ThemeMediaResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ThemeMediaResult> getThemeVideosWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getThemeVideosRequestBuilder(itemId, userId, inheritFromParent,
                sortBy, sortOrder, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getThemeVideos", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<ThemeMediaResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                ThemeMediaResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<ThemeMediaResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<ThemeMediaResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getThemeVideosRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean inheritFromParent,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getThemeVideos");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/ThemeVideos".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "inheritFromParent";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("inheritFromParent", inheritFromParent));
        localVarQueryParameterBaseName = "sortBy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParameterBaseName = "sortOrder";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortOrder", sortOrder));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param tmdbId The tmdbId. (optional)
     * @param imdbId The imdbId. (optional)
     * @throws ApiException if fails to make API call
     */
    public void postAddedMovies(@org.eclipse.jdt.annotation.NonNull String tmdbId,
            @org.eclipse.jdt.annotation.NonNull String imdbId) throws ApiException {
        postAddedMovies(tmdbId, imdbId, null);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param tmdbId The tmdbId. (optional)
     * @param imdbId The imdbId. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void postAddedMovies(@org.eclipse.jdt.annotation.NonNull String tmdbId,
            @org.eclipse.jdt.annotation.NonNull String imdbId, Map<String, String> headers) throws ApiException {
        postAddedMoviesWithHttpInfo(tmdbId, imdbId, headers);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param tmdbId The tmdbId. (optional)
     * @param imdbId The imdbId. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postAddedMoviesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String tmdbId,
            @org.eclipse.jdt.annotation.NonNull String imdbId) throws ApiException {
        return postAddedMoviesWithHttpInfo(tmdbId, imdbId, null);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param tmdbId The tmdbId. (optional)
     * @param imdbId The imdbId. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postAddedMoviesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String tmdbId,
            @org.eclipse.jdt.annotation.NonNull String imdbId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = postAddedMoviesRequestBuilder(tmdbId, imdbId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("postAddedMovies", localVarResponse);
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

    private HttpRequest.Builder postAddedMoviesRequestBuilder(@org.eclipse.jdt.annotation.NonNull String tmdbId,
            @org.eclipse.jdt.annotation.NonNull String imdbId, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/Movies/Added";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tmdbId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tmdbId", tmdbId));
        localVarQueryParameterBaseName = "imdbId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imdbId", imdbId));

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

        localVarRequestBuilder.header("Accept", "text/html");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * @param tvdbId The tvdbId. (optional)
     * @throws ApiException if fails to make API call
     */
    public void postAddedSeries(@org.eclipse.jdt.annotation.NonNull String tvdbId) throws ApiException {
        postAddedSeries(tvdbId, null);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * @param tvdbId The tvdbId. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void postAddedSeries(@org.eclipse.jdt.annotation.NonNull String tvdbId, Map<String, String> headers)
            throws ApiException {
        postAddedSeriesWithHttpInfo(tvdbId, headers);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * @param tvdbId The tvdbId. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postAddedSeriesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String tvdbId)
            throws ApiException {
        return postAddedSeriesWithHttpInfo(tvdbId, null);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * @param tvdbId The tvdbId. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postAddedSeriesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String tvdbId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = postAddedSeriesRequestBuilder(tvdbId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("postAddedSeries", localVarResponse);
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

    private HttpRequest.Builder postAddedSeriesRequestBuilder(@org.eclipse.jdt.annotation.NonNull String tvdbId,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/Series/Added";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tvdbId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tvdbId", tvdbId));

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

        localVarRequestBuilder.header("Accept", "text/html");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param mediaUpdateInfoDto The update paths. (required)
     * @throws ApiException if fails to make API call
     */
    public void postUpdatedMedia(@org.eclipse.jdt.annotation.Nullable MediaUpdateInfoDto mediaUpdateInfoDto)
            throws ApiException {
        postUpdatedMedia(mediaUpdateInfoDto, null);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param mediaUpdateInfoDto The update paths. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void postUpdatedMedia(@org.eclipse.jdt.annotation.Nullable MediaUpdateInfoDto mediaUpdateInfoDto,
            Map<String, String> headers) throws ApiException {
        postUpdatedMediaWithHttpInfo(mediaUpdateInfoDto, headers);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param mediaUpdateInfoDto The update paths. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postUpdatedMediaWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable MediaUpdateInfoDto mediaUpdateInfoDto) throws ApiException {
        return postUpdatedMediaWithHttpInfo(mediaUpdateInfoDto, null);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param mediaUpdateInfoDto The update paths. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postUpdatedMediaWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable MediaUpdateInfoDto mediaUpdateInfoDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = postUpdatedMediaRequestBuilder(mediaUpdateInfoDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("postUpdatedMedia", localVarResponse);
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

    private HttpRequest.Builder postUpdatedMediaRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable MediaUpdateInfoDto mediaUpdateInfoDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'mediaUpdateInfoDto' is set
        if (mediaUpdateInfoDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'mediaUpdateInfoDto' when calling postUpdatedMedia");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/Media/Updated";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(mediaUpdateInfoDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param tmdbId The tmdbId. (optional)
     * @param imdbId The imdbId. (optional)
     * @throws ApiException if fails to make API call
     */
    public void postUpdatedMovies(@org.eclipse.jdt.annotation.NonNull String tmdbId,
            @org.eclipse.jdt.annotation.NonNull String imdbId) throws ApiException {
        postUpdatedMovies(tmdbId, imdbId, null);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param tmdbId The tmdbId. (optional)
     * @param imdbId The imdbId. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void postUpdatedMovies(@org.eclipse.jdt.annotation.NonNull String tmdbId,
            @org.eclipse.jdt.annotation.NonNull String imdbId, Map<String, String> headers) throws ApiException {
        postUpdatedMoviesWithHttpInfo(tmdbId, imdbId, headers);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param tmdbId The tmdbId. (optional)
     * @param imdbId The imdbId. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postUpdatedMoviesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String tmdbId,
            @org.eclipse.jdt.annotation.NonNull String imdbId) throws ApiException {
        return postUpdatedMoviesWithHttpInfo(tmdbId, imdbId, null);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * @param tmdbId The tmdbId. (optional)
     * @param imdbId The imdbId. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postUpdatedMoviesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String tmdbId,
            @org.eclipse.jdt.annotation.NonNull String imdbId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = postUpdatedMoviesRequestBuilder(tmdbId, imdbId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("postUpdatedMovies", localVarResponse);
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

    private HttpRequest.Builder postUpdatedMoviesRequestBuilder(@org.eclipse.jdt.annotation.NonNull String tmdbId,
            @org.eclipse.jdt.annotation.NonNull String imdbId, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/Movies/Updated";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tmdbId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tmdbId", tmdbId));
        localVarQueryParameterBaseName = "imdbId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imdbId", imdbId));

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

        localVarRequestBuilder.header("Accept", "text/html");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * @param tvdbId The tvdbId. (optional)
     * @throws ApiException if fails to make API call
     */
    public void postUpdatedSeries(@org.eclipse.jdt.annotation.NonNull String tvdbId) throws ApiException {
        postUpdatedSeries(tvdbId, null);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * @param tvdbId The tvdbId. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void postUpdatedSeries(@org.eclipse.jdt.annotation.NonNull String tvdbId, Map<String, String> headers)
            throws ApiException {
        postUpdatedSeriesWithHttpInfo(tvdbId, headers);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * @param tvdbId The tvdbId. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postUpdatedSeriesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String tvdbId)
            throws ApiException {
        return postUpdatedSeriesWithHttpInfo(tvdbId, null);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * @param tvdbId The tvdbId. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postUpdatedSeriesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String tvdbId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = postUpdatedSeriesRequestBuilder(tvdbId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("postUpdatedSeries", localVarResponse);
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

    private HttpRequest.Builder postUpdatedSeriesRequestBuilder(@org.eclipse.jdt.annotation.NonNull String tvdbId,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/Series/Updated";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tvdbId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tvdbId", tvdbId));

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

        localVarRequestBuilder.header("Accept", "text/html");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Starts a library scan.
     * 
     * @throws ApiException if fails to make API call
     */
    public void refreshLibrary() throws ApiException {
        refreshLibrary(null);
    }

    /**
     * Starts a library scan.
     * 
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void refreshLibrary(Map<String, String> headers) throws ApiException {
        refreshLibraryWithHttpInfo(headers);
    }

    /**
     * Starts a library scan.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> refreshLibraryWithHttpInfo() throws ApiException {
        return refreshLibraryWithHttpInfo(null);
    }

    /**
     * Starts a library scan.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> refreshLibraryWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = refreshLibraryRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("refreshLibrary", localVarResponse);
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

    private HttpRequest.Builder refreshLibraryRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/Refresh";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/html");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
