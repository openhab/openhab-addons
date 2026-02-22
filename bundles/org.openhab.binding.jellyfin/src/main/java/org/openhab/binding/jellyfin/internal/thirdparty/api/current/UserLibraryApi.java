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
package org.openhab.binding.jellyfin.internal.thirdparty.api.current;

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

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiException;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiResponse;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Configuration;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Pair;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.UserItemDataDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UserLibraryApi {
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
    private final Consumer<HttpResponse<InputStream>> memberVarAsyncResponseInterceptor;

    public UserLibraryApi() {
        this(Configuration.getDefaultApiClient());
    }

    public UserLibraryApi(ApiClient apiClient) {
        memberVarHttpClient = apiClient.getHttpClient();
        memberVarObjectMapper = apiClient.getObjectMapper();
        memberVarBaseUri = apiClient.getBaseUri();
        memberVarInterceptor = apiClient.getRequestInterceptor();
        memberVarReadTimeout = apiClient.getReadTimeout();
        memberVarResponseInterceptor = apiClient.getResponseInterceptor();
        memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
    }

    protected ApiException getApiException(String operationId, HttpResponse<InputStream> response) throws IOException {
        InputStream responseBody = ApiClient.getResponseBody(response);
        String body = null;
        try {
            body = responseBody == null ? null : new String(responseBody.readAllBytes());
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
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
    public File downloadFileFromResponse(HttpResponse<InputStream> response, InputStream responseBody)
            throws ApiException {
        if (responseBody == null) {
            throw new ApiException(new IOException("Response body is empty"));
        }
        try {
            File file = prepareDownloadFile(response);
            java.nio.file.Files.copy(responseBody, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
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
     * Deletes a user&#39;s saved personal rating for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto deleteUserItemRating(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return deleteUserItemRating(itemId, userId, null);
    }

    /**
     * Deletes a user&#39;s saved personal rating for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto deleteUserItemRating(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<UserItemDataDto> localVarResponse = deleteUserItemRatingWithHttpInfo(itemId, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Deletes a user&#39;s saved personal rating for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> deleteUserItemRatingWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId, @org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        return deleteUserItemRatingWithHttpInfo(itemId, userId, null);
    }

    /**
     * Deletes a user&#39;s saved personal rating for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> deleteUserItemRatingWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteUserItemRatingRequestBuilder(itemId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteUserItemRating", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                UserItemDataDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserItemDataDto>() {
                        });

                return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder deleteUserItemRatingRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteUserItemRating");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/UserItems/{itemId}/Rating".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

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
     * Gets intros to play before the main media item plays.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getIntros(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getIntros(itemId, userId, null);
    }

    /**
     * Gets intros to play before the main media item plays.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getIntros(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getIntrosWithHttpInfo(itemId, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets intros to play before the main media item plays.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getIntrosWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getIntrosWithHttpInfo(itemId, userId, null);
    }

    /**
     * Gets intros to play before the main media item plays.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getIntrosWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getIntrosRequestBuilder(itemId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getIntros", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getIntrosRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getIntros");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Intros".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

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
     * Gets an item from a user&#39;s library.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getItem(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getItem(itemId, userId, null);
    }

    /**
     * Gets an item from a user&#39;s library.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getItem(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDto> localVarResponse = getItemWithHttpInfo(itemId, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets an item from a user&#39;s library.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getItemWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getItemWithHttpInfo(itemId, userId, null);
    }

    /**
     * Gets an item from a user&#39;s library.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getItemWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getItemRequestBuilder(itemId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getItem", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                BaseItemDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDto>() {
                        });

                return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getItemRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

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
     * Gets latest media.
     * 
     * @param userId User id. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param isPlayed Filter by items that are played, or not. (optional)
     * @param enableImages Optional. include image information in output. (optional)
     * @param imageTypeLimit Optional. the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. include user data. (optional)
     * @param limit Return item limit. (optional, default to 20)
     * @param groupItems Whether or not to group items into a parent container. (optional, default to true)
     * @return List&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<BaseItemDto> getLatestMedia(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlayed,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer limit, @org.eclipse.jdt.annotation.Nullable Boolean groupItems)
            throws ApiException {
        return getLatestMedia(userId, parentId, fields, includeItemTypes, isPlayed, enableImages, imageTypeLimit,
                enableImageTypes, enableUserData, limit, groupItems, null);
    }

    /**
     * Gets latest media.
     * 
     * @param userId User id. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param isPlayed Filter by items that are played, or not. (optional)
     * @param enableImages Optional. include image information in output. (optional)
     * @param imageTypeLimit Optional. the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. include user data. (optional)
     * @param limit Return item limit. (optional, default to 20)
     * @param groupItems Whether or not to group items into a parent container. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return List&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<BaseItemDto> getLatestMedia(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlayed,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer limit, @org.eclipse.jdt.annotation.Nullable Boolean groupItems,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<BaseItemDto>> localVarResponse = getLatestMediaWithHttpInfo(userId, parentId, fields,
                includeItemTypes, isPlayed, enableImages, imageTypeLimit, enableImageTypes, enableUserData, limit,
                groupItems, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets latest media.
     * 
     * @param userId User id. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param isPlayed Filter by items that are played, or not. (optional)
     * @param enableImages Optional. include image information in output. (optional)
     * @param imageTypeLimit Optional. the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. include user data. (optional)
     * @param limit Return item limit. (optional, default to 20)
     * @param groupItems Whether or not to group items into a parent container. (optional, default to true)
     * @return ApiResponse&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<BaseItemDto>> getLatestMediaWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlayed,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer limit, @org.eclipse.jdt.annotation.Nullable Boolean groupItems)
            throws ApiException {
        return getLatestMediaWithHttpInfo(userId, parentId, fields, includeItemTypes, isPlayed, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, limit, groupItems, null);
    }

    /**
     * Gets latest media.
     * 
     * @param userId User id. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param isPlayed Filter by items that are played, or not. (optional)
     * @param enableImages Optional. include image information in output. (optional)
     * @param imageTypeLimit Optional. the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. include user data. (optional)
     * @param limit Return item limit. (optional, default to 20)
     * @param groupItems Whether or not to group items into a parent container. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<BaseItemDto>> getLatestMediaWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlayed,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer limit, @org.eclipse.jdt.annotation.Nullable Boolean groupItems,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLatestMediaRequestBuilder(userId, parentId, fields,
                includeItemTypes, isPlayed, enableImages, imageTypeLimit, enableImageTypes, enableUserData, limit,
                groupItems, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLatestMedia", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<BaseItemDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<BaseItemDto> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<BaseItemDto>>() {
                        });

                return new ApiResponse<List<BaseItemDto>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getLatestMediaRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlayed,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer limit, @org.eclipse.jdt.annotation.Nullable Boolean groupItems,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/Latest";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "parentId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentId", parentId));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "includeItemTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParameterBaseName = "isPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isPlayed", isPlayed));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "groupItems";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("groupItems", groupItems));

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
     * Gets local trailers for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return List&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<BaseItemDto> getLocalTrailers(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getLocalTrailers(itemId, userId, null);
    }

    /**
     * Gets local trailers for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return List&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<BaseItemDto> getLocalTrailers(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<List<BaseItemDto>> localVarResponse = getLocalTrailersWithHttpInfo(itemId, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets local trailers for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<BaseItemDto>> getLocalTrailersWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getLocalTrailersWithHttpInfo(itemId, userId, null);
    }

    /**
     * Gets local trailers for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<BaseItemDto>> getLocalTrailersWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLocalTrailersRequestBuilder(itemId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLocalTrailers", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<BaseItemDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<BaseItemDto> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<BaseItemDto>>() {
                        });

                return new ApiResponse<List<BaseItemDto>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getLocalTrailersRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getLocalTrailers");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/LocalTrailers".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

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
     * Gets the root folder from a user&#39;s library.
     * 
     * @param userId User id. (optional)
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getRootFolder(@org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getRootFolder(userId, null);
    }

    /**
     * Gets the root folder from a user&#39;s library.
     * 
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getRootFolder(@org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDto> localVarResponse = getRootFolderWithHttpInfo(userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the root folder from a user&#39;s library.
     * 
     * @param userId User id. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getRootFolderWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        return getRootFolderWithHttpInfo(userId, null);
    }

    /**
     * Gets the root folder from a user&#39;s library.
     * 
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getRootFolderWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRootFolderRequestBuilder(userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRootFolder", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                BaseItemDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDto>() {
                        });

                return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getRootFolderRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/Root";

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
     * Gets special features for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return List&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<BaseItemDto> getSpecialFeatures(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getSpecialFeatures(itemId, userId, null);
    }

    /**
     * Gets special features for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return List&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<BaseItemDto> getSpecialFeatures(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<List<BaseItemDto>> localVarResponse = getSpecialFeaturesWithHttpInfo(itemId, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets special features for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<BaseItemDto>> getSpecialFeaturesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId, @org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        return getSpecialFeaturesWithHttpInfo(itemId, userId, null);
    }

    /**
     * Gets special features for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<BaseItemDto>> getSpecialFeaturesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSpecialFeaturesRequestBuilder(itemId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSpecialFeatures", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<BaseItemDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<BaseItemDto> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<BaseItemDto>>() {
                        });

                return new ApiResponse<List<BaseItemDto>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getSpecialFeaturesRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getSpecialFeatures");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/SpecialFeatures".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

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
     * Marks an item as a favorite.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto markFavoriteItem(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return markFavoriteItem(itemId, userId, null);
    }

    /**
     * Marks an item as a favorite.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto markFavoriteItem(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<UserItemDataDto> localVarResponse = markFavoriteItemWithHttpInfo(itemId, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Marks an item as a favorite.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> markFavoriteItemWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return markFavoriteItemWithHttpInfo(itemId, userId, null);
    }

    /**
     * Marks an item as a favorite.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> markFavoriteItemWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = markFavoriteItemRequestBuilder(itemId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("markFavoriteItem", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                UserItemDataDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserItemDataDto>() {
                        });

                return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder markFavoriteItemRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling markFavoriteItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/UserFavoriteItems/{itemId}".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

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
     * Unmarks item as a favorite.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto unmarkFavoriteItem(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return unmarkFavoriteItem(itemId, userId, null);
    }

    /**
     * Unmarks item as a favorite.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto unmarkFavoriteItem(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<UserItemDataDto> localVarResponse = unmarkFavoriteItemWithHttpInfo(itemId, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Unmarks item as a favorite.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> unmarkFavoriteItemWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return unmarkFavoriteItemWithHttpInfo(itemId, userId, null);
    }

    /**
     * Unmarks item as a favorite.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> unmarkFavoriteItemWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = unmarkFavoriteItemRequestBuilder(itemId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("unmarkFavoriteItem", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                UserItemDataDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserItemDataDto>() {
                        });

                return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder unmarkFavoriteItemRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling unmarkFavoriteItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/UserFavoriteItems/{itemId}".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

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
     * Updates a user&#39;s rating for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param likes Whether this
     *            M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Nullable{System.Guid},System.Guid,System.Nullable{System.Boolean})
     *            is likes. (optional)
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto updateUserItemRating(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable Boolean likes)
            throws ApiException {
        return updateUserItemRating(itemId, userId, likes, null);
    }

    /**
     * Updates a user&#39;s rating for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param likes Whether this
     *            M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Nullable{System.Guid},System.Guid,System.Nullable{System.Boolean})
     *            is likes. (optional)
     * @param headers Optional headers to include in the request
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto updateUserItemRating(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable Boolean likes,
            Map<String, String> headers) throws ApiException {
        ApiResponse<UserItemDataDto> localVarResponse = updateUserItemRatingWithHttpInfo(itemId, userId, likes,
                headers);
        return localVarResponse.getData();
    }

    /**
     * Updates a user&#39;s rating for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param likes Whether this
     *            M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Nullable{System.Guid},System.Guid,System.Nullable{System.Boolean})
     *            is likes. (optional)
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> updateUserItemRatingWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable Boolean likes) throws ApiException {
        return updateUserItemRatingWithHttpInfo(itemId, userId, likes, null);
    }

    /**
     * Updates a user&#39;s rating for an item.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param likes Whether this
     *            M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Nullable{System.Guid},System.Guid,System.Nullable{System.Boolean})
     *            is likes. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> updateUserItemRatingWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable Boolean likes, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateUserItemRatingRequestBuilder(itemId, userId, likes, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateUserItemRating", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                UserItemDataDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserItemDataDto>() {
                        });

                return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder updateUserItemRatingRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable Boolean likes,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling updateUserItemRating");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/UserItems/{itemId}/Rating".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "likes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("likes", likes));

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
