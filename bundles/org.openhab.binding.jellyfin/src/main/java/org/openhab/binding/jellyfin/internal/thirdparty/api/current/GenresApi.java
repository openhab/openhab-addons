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
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SortOrder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class GenresApi {
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

    public GenresApi() {
        this(Configuration.getDefaultApiClient());
    }

    public GenresApi(ApiClient apiClient) {
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
     * Gets a genre, by name.
     * 
     * @param genreName The genre name. (required)
     * @param userId The user id. (optional)
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getGenre(@org.eclipse.jdt.annotation.NonNull String genreName,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getGenre(genreName, userId, null);
    }

    /**
     * Gets a genre, by name.
     * 
     * @param genreName The genre name. (required)
     * @param userId The user id. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getGenre(@org.eclipse.jdt.annotation.NonNull String genreName,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDto> localVarResponse = getGenreWithHttpInfo(genreName, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a genre, by name.
     * 
     * @param genreName The genre name. (required)
     * @param userId The user id. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getGenreWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String genreName,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getGenreWithHttpInfo(genreName, userId, null);
    }

    /**
     * Gets a genre, by name.
     * 
     * @param genreName The genre name. (required)
     * @param userId The user id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getGenreWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String genreName,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getGenreRequestBuilder(genreName, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getGenre", localVarResponse);
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

    private HttpRequest.Builder getGenreRequestBuilder(@org.eclipse.jdt.annotation.NonNull String genreName,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'genreName' is set
        if (genreName == null) {
            throw new ApiException(400, "Missing the required parameter 'genreName' when calling getGenre");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Genres/{genreName}".replace("{genreName}", ApiClient.urlEncode(genreName.toString()));

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
     * Gets all genres from a given item, folder, or the entire library.
     * 
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm The search term. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered in based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param userId User id. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param enableTotalRecordCount Optional. Include total record count. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getGenres(@org.eclipse.jdt.annotation.Nullable Integer startIndex,
            @org.eclipse.jdt.annotation.Nullable Integer limit, @org.eclipse.jdt.annotation.Nullable String searchTerm,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isFavorite,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWith,
            @org.eclipse.jdt.annotation.Nullable String nameLessThan,
            @org.eclipse.jdt.annotation.Nullable List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.Nullable List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Boolean enableTotalRecordCount) throws ApiException {
        return getGenres(startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes,
                isFavorite, imageTypeLimit, enableImageTypes, userId, nameStartsWithOrGreater, nameStartsWith,
                nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount, null);
    }

    /**
     * Gets all genres from a given item, folder, or the entire library.
     * 
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm The search term. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered in based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param userId User id. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param enableTotalRecordCount Optional. Include total record count. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getGenres(@org.eclipse.jdt.annotation.Nullable Integer startIndex,
            @org.eclipse.jdt.annotation.Nullable Integer limit, @org.eclipse.jdt.annotation.Nullable String searchTerm,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isFavorite,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWith,
            @org.eclipse.jdt.annotation.Nullable String nameLessThan,
            @org.eclipse.jdt.annotation.Nullable List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.Nullable List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Boolean enableTotalRecordCount, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getGenresWithHttpInfo(startIndex, limit, searchTerm,
                parentId, fields, excludeItemTypes, includeItemTypes, isFavorite, imageTypeLimit, enableImageTypes,
                userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages,
                enableTotalRecordCount, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets all genres from a given item, folder, or the entire library.
     * 
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm The search term. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered in based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param userId User id. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param enableTotalRecordCount Optional. Include total record count. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getGenresWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable String searchTerm, @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isFavorite,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWith,
            @org.eclipse.jdt.annotation.Nullable String nameLessThan,
            @org.eclipse.jdt.annotation.Nullable List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.Nullable List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Boolean enableTotalRecordCount) throws ApiException {
        return getGenresWithHttpInfo(startIndex, limit, searchTerm, parentId, fields, excludeItemTypes,
                includeItemTypes, isFavorite, imageTypeLimit, enableImageTypes, userId, nameStartsWithOrGreater,
                nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount, null);
    }

    /**
     * Gets all genres from a given item, folder, or the entire library.
     * 
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm The search term. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered in based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param userId User id. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param enableTotalRecordCount Optional. Include total record count. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getGenresWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable String searchTerm, @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isFavorite,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWith,
            @org.eclipse.jdt.annotation.Nullable String nameLessThan,
            @org.eclipse.jdt.annotation.Nullable List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.Nullable List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Boolean enableTotalRecordCount, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getGenresRequestBuilder(startIndex, limit, searchTerm, parentId,
                fields, excludeItemTypes, includeItemTypes, isFavorite, imageTypeLimit, enableImageTypes, userId,
                nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages,
                enableTotalRecordCount, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getGenres", localVarResponse);
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

    private HttpRequest.Builder getGenresRequestBuilder(@org.eclipse.jdt.annotation.Nullable Integer startIndex,
            @org.eclipse.jdt.annotation.Nullable Integer limit, @org.eclipse.jdt.annotation.Nullable String searchTerm,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isFavorite,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWith,
            @org.eclipse.jdt.annotation.Nullable String nameLessThan,
            @org.eclipse.jdt.annotation.Nullable List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.Nullable List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Boolean enableTotalRecordCount, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Genres";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "searchTerm";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("searchTerm", searchTerm));
        localVarQueryParameterBaseName = "parentId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentId", parentId));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "excludeItemTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeItemTypes", excludeItemTypes));
        localVarQueryParameterBaseName = "includeItemTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParameterBaseName = "isFavorite";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isFavorite", isFavorite));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "nameStartsWithOrGreater";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("nameStartsWithOrGreater", nameStartsWithOrGreater));
        localVarQueryParameterBaseName = "nameStartsWith";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("nameStartsWith", nameStartsWith));
        localVarQueryParameterBaseName = "nameLessThan";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("nameLessThan", nameLessThan));
        localVarQueryParameterBaseName = "sortBy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParameterBaseName = "sortOrder";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortOrder", sortOrder));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
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
}
