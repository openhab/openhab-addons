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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SortOrder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class YearsApi {
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

    public YearsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public YearsApi(ApiClient apiClient) {
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
     * Gets a year.
     * 
     * @param year The year. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getYear(@org.eclipse.jdt.annotation.Nullable Integer year,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getYear(year, userId, null);
    }

    /**
     * Gets a year.
     * 
     * @param year The year. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getYear(@org.eclipse.jdt.annotation.Nullable Integer year,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDto> localVarResponse = getYearWithHttpInfo(year, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a year.
     * 
     * @param year The year. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getYearWithHttpInfo(@org.eclipse.jdt.annotation.Nullable Integer year,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getYearWithHttpInfo(year, userId, null);
    }

    /**
     * Gets a year.
     * 
     * @param year The year. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getYearWithHttpInfo(@org.eclipse.jdt.annotation.Nullable Integer year,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getYearRequestBuilder(year, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getYear", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getYearRequestBuilder(@org.eclipse.jdt.annotation.Nullable Integer year,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'year' is set
        if (year == null) {
            throw new ApiException(400, "Missing the required parameter 'year' when calling getYear");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Years/{year}".replace("{year}", ApiClient.urlEncode(year.toString()));

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
     * Get years.
     * 
     * @param startIndex Skips over a given number of items within the results. Use for paging. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be excluded based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be included based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param userId User Id. (optional)
     * @param recursive Search recursively. (optional, default to true)
     * @param enableImages Optional. Include image information in output. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getYears(@org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Boolean recursive,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages) throws ApiException {
        return getYears(startIndex, limit, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, mediaTypes,
                sortBy, enableUserData, imageTypeLimit, enableImageTypes, userId, recursive, enableImages, null);
    }

    /**
     * Get years.
     * 
     * @param startIndex Skips over a given number of items within the results. Use for paging. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be excluded based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be included based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param userId User Id. (optional)
     * @param recursive Search recursively. (optional, default to true)
     * @param enableImages Optional. Include image information in output. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getYears(@org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Boolean recursive,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages, Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getYearsWithHttpInfo(startIndex, limit, sortOrder,
                parentId, fields, excludeItemTypes, includeItemTypes, mediaTypes, sortBy, enableUserData,
                imageTypeLimit, enableImageTypes, userId, recursive, enableImages, headers);
        return localVarResponse.getData();
    }

    /**
     * Get years.
     * 
     * @param startIndex Skips over a given number of items within the results. Use for paging. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be excluded based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be included based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param userId User Id. (optional)
     * @param recursive Search recursively. (optional, default to true)
     * @param enableImages Optional. Include image information in output. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getYearsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Boolean recursive,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages) throws ApiException {
        return getYearsWithHttpInfo(startIndex, limit, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes,
                mediaTypes, sortBy, enableUserData, imageTypeLimit, enableImageTypes, userId, recursive, enableImages,
                null);
    }

    /**
     * Get years.
     * 
     * @param startIndex Skips over a given number of items within the results. Use for paging. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be excluded based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be included based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param userId User Id. (optional)
     * @param recursive Search recursively. (optional, default to true)
     * @param enableImages Optional. Include image information in output. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getYearsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Boolean recursive,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getYearsRequestBuilder(startIndex, limit, sortOrder, parentId,
                fields, excludeItemTypes, includeItemTypes, mediaTypes, sortBy, enableUserData, imageTypeLimit,
                enableImageTypes, userId, recursive, enableImages, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getYears", localVarResponse);
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

    private HttpRequest.Builder getYearsRequestBuilder(@org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Boolean recursive,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Years";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "sortOrder";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortOrder", sortOrder));
        localVarQueryParameterBaseName = "parentId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentId", parentId));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "excludeItemTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeItemTypes", excludeItemTypes));
        localVarQueryParameterBaseName = "includeItemTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParameterBaseName = "mediaTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "mediaTypes", mediaTypes));
        localVarQueryParameterBaseName = "sortBy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "recursive";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("recursive", recursive));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));

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
