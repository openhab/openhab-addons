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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFilter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PersonsApi {
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

    public PersonsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public PersonsApi(ApiClient apiClient) {
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
     * Get person by name.
     * 
     * @param name Person name. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getPerson(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getPerson(name, userId, null);
    }

    /**
     * Get person by name.
     * 
     * @param name Person name. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     */
    public BaseItemDto getPerson(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDto> localVarResponse = getPersonWithHttpInfo(name, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get person by name.
     * 
     * @param name Person name. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getPersonWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getPersonWithHttpInfo(name, userId, null);
    }

    /**
     * Get person by name.
     * 
     * @param name Person name. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDto> getPersonWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPersonRequestBuilder(name, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPerson", localVarResponse);
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

    private HttpRequest.Builder getPersonRequestBuilder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getPerson");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Persons/{name}".replace("{name}", ApiClient.urlEncode(name.toString()));

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
     * Gets all persons.
     * 
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm The search term. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. userId is required. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param excludePersonTypes Optional. If specified results will be filtered to exclude those containing the
     *            specified PersonType. Allows multiple, comma-delimited. (optional)
     * @param personTypes Optional. If specified results will be filtered to include only those containing the specified
     *            PersonType. Allows multiple, comma-delimited. (optional)
     * @param appearsInItemId Optional. If specified, person results will be filtered on items related to said persons.
     *            (optional)
     * @param userId User id. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getPersons(@org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> excludePersonTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull UUID appearsInItemId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages) throws ApiException {
        return getPersons(limit, searchTerm, fields, filters, isFavorite, enableUserData, imageTypeLimit,
                enableImageTypes, excludePersonTypes, personTypes, appearsInItemId, userId, enableImages, null);
    }

    /**
     * Gets all persons.
     * 
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm The search term. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. userId is required. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param excludePersonTypes Optional. If specified results will be filtered to exclude those containing the
     *            specified PersonType. Allows multiple, comma-delimited. (optional)
     * @param personTypes Optional. If specified results will be filtered to include only those containing the specified
     *            PersonType. Allows multiple, comma-delimited. (optional)
     * @param appearsInItemId Optional. If specified, person results will be filtered on items related to said persons.
     *            (optional)
     * @param userId User id. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getPersons(@org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> excludePersonTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull UUID appearsInItemId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages, Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getPersonsWithHttpInfo(limit, searchTerm, fields,
                filters, isFavorite, enableUserData, imageTypeLimit, enableImageTypes, excludePersonTypes, personTypes,
                appearsInItemId, userId, enableImages, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets all persons.
     * 
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm The search term. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. userId is required. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param excludePersonTypes Optional. If specified results will be filtered to exclude those containing the
     *            specified PersonType. Allows multiple, comma-delimited. (optional)
     * @param personTypes Optional. If specified results will be filtered to include only those containing the specified
     *            PersonType. Allows multiple, comma-delimited. (optional)
     * @param appearsInItemId Optional. If specified, person results will be filtered on items related to said persons.
     *            (optional)
     * @param userId User id. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getPersonsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> excludePersonTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull UUID appearsInItemId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages) throws ApiException {
        return getPersonsWithHttpInfo(limit, searchTerm, fields, filters, isFavorite, enableUserData, imageTypeLimit,
                enableImageTypes, excludePersonTypes, personTypes, appearsInItemId, userId, enableImages, null);
    }

    /**
     * Gets all persons.
     * 
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm The search term. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. userId is required. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param excludePersonTypes Optional. If specified results will be filtered to exclude those containing the
     *            specified PersonType. Allows multiple, comma-delimited. (optional)
     * @param personTypes Optional. If specified results will be filtered to include only those containing the specified
     *            PersonType. Allows multiple, comma-delimited. (optional)
     * @param appearsInItemId Optional. If specified, person results will be filtered on items related to said persons.
     *            (optional)
     * @param userId User id. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getPersonsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> excludePersonTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull UUID appearsInItemId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPersonsRequestBuilder(limit, searchTerm, fields, filters,
                isFavorite, enableUserData, imageTypeLimit, enableImageTypes, excludePersonTypes, personTypes,
                appearsInItemId, userId, enableImages, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPersons", localVarResponse);
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

    private HttpRequest.Builder getPersonsRequestBuilder(@org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> excludePersonTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull UUID appearsInItemId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Persons";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "searchTerm";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("searchTerm", searchTerm));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "filters";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "filters", filters));
        localVarQueryParameterBaseName = "isFavorite";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isFavorite", isFavorite));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "excludePersonTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludePersonTypes", excludePersonTypes));
        localVarQueryParameterBaseName = "personTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "personTypes", personTypes));
        localVarQueryParameterBaseName = "appearsInItemId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("appearsInItemId", appearsInItemId));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
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
