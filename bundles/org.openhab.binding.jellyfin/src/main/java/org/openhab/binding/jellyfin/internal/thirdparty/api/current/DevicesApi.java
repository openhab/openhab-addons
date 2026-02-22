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
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.DeviceInfoDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.DeviceInfoDtoQueryResult;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.DeviceOptionsDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DevicesApi {
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

    public DevicesApi() {
        this(Configuration.getDefaultApiClient());
    }

    public DevicesApi(ApiClient apiClient) {
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
     * Deletes a device.
     * 
     * @param id Device Id. (required)
     * @throws ApiException if fails to make API call
     */
    public void deleteDevice(@org.eclipse.jdt.annotation.NonNull String id) throws ApiException {
        deleteDevice(id, null);
    }

    /**
     * Deletes a device.
     * 
     * @param id Device Id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteDevice(@org.eclipse.jdt.annotation.NonNull String id, Map<String, String> headers)
            throws ApiException {
        deleteDeviceWithHttpInfo(id, headers);
    }

    /**
     * Deletes a device.
     * 
     * @param id Device Id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteDeviceWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id)
            throws ApiException {
        return deleteDeviceWithHttpInfo(id, null);
    }

    /**
     * Deletes a device.
     * 
     * @param id Device Id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteDeviceWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteDeviceRequestBuilder(id, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteDevice", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
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

    private HttpRequest.Builder deleteDeviceRequestBuilder(@org.eclipse.jdt.annotation.NonNull String id,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new ApiException(400, "Missing the required parameter 'id' when calling deleteDevice");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Devices";

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
     * Get info for a device.
     * 
     * @param id Device Id. (required)
     * @return DeviceInfoDto
     * @throws ApiException if fails to make API call
     */
    public DeviceInfoDto getDeviceInfo(@org.eclipse.jdt.annotation.NonNull String id) throws ApiException {
        return getDeviceInfo(id, null);
    }

    /**
     * Get info for a device.
     * 
     * @param id Device Id. (required)
     * @param headers Optional headers to include in the request
     * @return DeviceInfoDto
     * @throws ApiException if fails to make API call
     */
    public DeviceInfoDto getDeviceInfo(@org.eclipse.jdt.annotation.NonNull String id, Map<String, String> headers)
            throws ApiException {
        ApiResponse<DeviceInfoDto> localVarResponse = getDeviceInfoWithHttpInfo(id, headers);
        return localVarResponse.getData();
    }

    /**
     * Get info for a device.
     * 
     * @param id Device Id. (required)
     * @return ApiResponse&lt;DeviceInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceInfoDto> getDeviceInfoWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id)
            throws ApiException {
        return getDeviceInfoWithHttpInfo(id, null);
    }

    /**
     * Get info for a device.
     * 
     * @param id Device Id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;DeviceInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceInfoDto> getDeviceInfoWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDeviceInfoRequestBuilder(id, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDeviceInfo", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<DeviceInfoDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                DeviceInfoDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<DeviceInfoDto>() {
                        });

                return new ApiResponse<DeviceInfoDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getDeviceInfoRequestBuilder(@org.eclipse.jdt.annotation.NonNull String id,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new ApiException(400, "Missing the required parameter 'id' when calling getDeviceInfo");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Devices/Info";

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
     * Get options for a device.
     * 
     * @param id Device Id. (required)
     * @return DeviceOptionsDto
     * @throws ApiException if fails to make API call
     */
    public DeviceOptionsDto getDeviceOptions(@org.eclipse.jdt.annotation.NonNull String id) throws ApiException {
        return getDeviceOptions(id, null);
    }

    /**
     * Get options for a device.
     * 
     * @param id Device Id. (required)
     * @param headers Optional headers to include in the request
     * @return DeviceOptionsDto
     * @throws ApiException if fails to make API call
     */
    public DeviceOptionsDto getDeviceOptions(@org.eclipse.jdt.annotation.NonNull String id, Map<String, String> headers)
            throws ApiException {
        ApiResponse<DeviceOptionsDto> localVarResponse = getDeviceOptionsWithHttpInfo(id, headers);
        return localVarResponse.getData();
    }

    /**
     * Get options for a device.
     * 
     * @param id Device Id. (required)
     * @return ApiResponse&lt;DeviceOptionsDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceOptionsDto> getDeviceOptionsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id)
            throws ApiException {
        return getDeviceOptionsWithHttpInfo(id, null);
    }

    /**
     * Get options for a device.
     * 
     * @param id Device Id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;DeviceOptionsDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceOptionsDto> getDeviceOptionsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDeviceOptionsRequestBuilder(id, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDeviceOptions", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<DeviceOptionsDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                DeviceOptionsDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<DeviceOptionsDto>() {
                        });

                return new ApiResponse<DeviceOptionsDto>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getDeviceOptionsRequestBuilder(@org.eclipse.jdt.annotation.NonNull String id,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new ApiException(400, "Missing the required parameter 'id' when calling getDeviceOptions");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Devices/Options";

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
     * Get Devices.
     * 
     * @param userId Gets or sets the user identifier. (optional)
     * @return DeviceInfoDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public DeviceInfoDtoQueryResult getDevices(@org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getDevices(userId, null);
    }

    /**
     * Get Devices.
     * 
     * @param userId Gets or sets the user identifier. (optional)
     * @param headers Optional headers to include in the request
     * @return DeviceInfoDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public DeviceInfoDtoQueryResult getDevices(@org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<DeviceInfoDtoQueryResult> localVarResponse = getDevicesWithHttpInfo(userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get Devices.
     * 
     * @param userId Gets or sets the user identifier. (optional)
     * @return ApiResponse&lt;DeviceInfoDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceInfoDtoQueryResult> getDevicesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getDevicesWithHttpInfo(userId, null);
    }

    /**
     * Get Devices.
     * 
     * @param userId Gets or sets the user identifier. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;DeviceInfoDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceInfoDtoQueryResult> getDevicesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDevicesRequestBuilder(userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDevices", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<DeviceInfoDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                DeviceInfoDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<DeviceInfoDtoQueryResult>() {
                        });

                return new ApiResponse<DeviceInfoDtoQueryResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getDevicesRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Devices";

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
     * Update device options.
     * 
     * @param id Device Id. (required)
     * @param deviceOptionsDto Device Options. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateDeviceOptions(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull DeviceOptionsDto deviceOptionsDto) throws ApiException {
        updateDeviceOptions(id, deviceOptionsDto, null);
    }

    /**
     * Update device options.
     * 
     * @param id Device Id. (required)
     * @param deviceOptionsDto Device Options. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateDeviceOptions(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull DeviceOptionsDto deviceOptionsDto, Map<String, String> headers)
            throws ApiException {
        updateDeviceOptionsWithHttpInfo(id, deviceOptionsDto, headers);
    }

    /**
     * Update device options.
     * 
     * @param id Device Id. (required)
     * @param deviceOptionsDto Device Options. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateDeviceOptionsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull DeviceOptionsDto deviceOptionsDto) throws ApiException {
        return updateDeviceOptionsWithHttpInfo(id, deviceOptionsDto, null);
    }

    /**
     * Update device options.
     * 
     * @param id Device Id. (required)
     * @param deviceOptionsDto Device Options. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateDeviceOptionsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull DeviceOptionsDto deviceOptionsDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateDeviceOptionsRequestBuilder(id, deviceOptionsDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateDeviceOptions", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
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

    private HttpRequest.Builder updateDeviceOptionsRequestBuilder(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull DeviceOptionsDto deviceOptionsDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new ApiException(400, "Missing the required parameter 'id' when calling updateDeviceOptions");
        }
        // verify the required parameter 'deviceOptionsDto' is set
        if (deviceOptionsDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'deviceOptionsDto' when calling updateDeviceOptions");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Devices/Options";

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

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(deviceOptionsDto);
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
}
