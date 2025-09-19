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
package org.openhab.binding.jellyfin.internal.api.generated.legacy;

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
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.DeviceInfo;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.DeviceInfoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.DeviceOptions;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.DeviceOptionsDto;

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
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

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
     * Deletes a device.
     * 
     * @param id Device Id. (required)
     * @throws ApiException if fails to make API call
     */
    public void deleteDevice(@org.eclipse.jdt.annotation.Nullable String id) throws ApiException {
        deleteDevice(id, null);
    }

    /**
     * Deletes a device.
     * 
     * @param id Device Id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteDevice(@org.eclipse.jdt.annotation.Nullable String id, Map<String, String> headers)
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
    public ApiResponse<Void> deleteDeviceWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String id)
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
    public ApiResponse<Void> deleteDeviceWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String id,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteDeviceRequestBuilder(id, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteDevice", localVarResponse);
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

    private HttpRequest.Builder deleteDeviceRequestBuilder(@org.eclipse.jdt.annotation.Nullable String id,
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * @return DeviceInfo
     * @throws ApiException if fails to make API call
     */
    public DeviceInfo getDeviceInfo(@org.eclipse.jdt.annotation.Nullable String id) throws ApiException {
        return getDeviceInfo(id, null);
    }

    /**
     * Get info for a device.
     * 
     * @param id Device Id. (required)
     * @param headers Optional headers to include in the request
     * @return DeviceInfo
     * @throws ApiException if fails to make API call
     */
    public DeviceInfo getDeviceInfo(@org.eclipse.jdt.annotation.Nullable String id, Map<String, String> headers)
            throws ApiException {
        ApiResponse<DeviceInfo> localVarResponse = getDeviceInfoWithHttpInfo(id, headers);
        return localVarResponse.getData();
    }

    /**
     * Get info for a device.
     * 
     * @param id Device Id. (required)
     * @return ApiResponse&lt;DeviceInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceInfo> getDeviceInfoWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String id)
            throws ApiException {
        return getDeviceInfoWithHttpInfo(id, null);
    }

    /**
     * Get info for a device.
     * 
     * @param id Device Id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;DeviceInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceInfo> getDeviceInfoWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String id,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDeviceInfoRequestBuilder(id, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDeviceInfo", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<DeviceInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                DeviceInfo responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<DeviceInfo>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<DeviceInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getDeviceInfoRequestBuilder(@org.eclipse.jdt.annotation.Nullable String id,
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * @return DeviceOptions
     * @throws ApiException if fails to make API call
     */
    public DeviceOptions getDeviceOptions(@org.eclipse.jdt.annotation.Nullable String id) throws ApiException {
        return getDeviceOptions(id, null);
    }

    /**
     * Get options for a device.
     * 
     * @param id Device Id. (required)
     * @param headers Optional headers to include in the request
     * @return DeviceOptions
     * @throws ApiException if fails to make API call
     */
    public DeviceOptions getDeviceOptions(@org.eclipse.jdt.annotation.Nullable String id, Map<String, String> headers)
            throws ApiException {
        ApiResponse<DeviceOptions> localVarResponse = getDeviceOptionsWithHttpInfo(id, headers);
        return localVarResponse.getData();
    }

    /**
     * Get options for a device.
     * 
     * @param id Device Id. (required)
     * @return ApiResponse&lt;DeviceOptions&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceOptions> getDeviceOptionsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String id)
            throws ApiException {
        return getDeviceOptionsWithHttpInfo(id, null);
    }

    /**
     * Get options for a device.
     * 
     * @param id Device Id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;DeviceOptions&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceOptions> getDeviceOptionsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String id,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDeviceOptionsRequestBuilder(id, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDeviceOptions", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<DeviceOptions>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                DeviceOptions responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<DeviceOptions>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<DeviceOptions>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getDeviceOptionsRequestBuilder(@org.eclipse.jdt.annotation.Nullable String id,
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * @param supportsSync Gets or sets a value indicating whether [supports synchronize]. (optional)
     * @param userId Gets or sets the user identifier. (optional)
     * @return DeviceInfoQueryResult
     * @throws ApiException if fails to make API call
     */
    public DeviceInfoQueryResult getDevices(@org.eclipse.jdt.annotation.NonNull Boolean supportsSync,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getDevices(supportsSync, userId, null);
    }

    /**
     * Get Devices.
     * 
     * @param supportsSync Gets or sets a value indicating whether [supports synchronize]. (optional)
     * @param userId Gets or sets the user identifier. (optional)
     * @param headers Optional headers to include in the request
     * @return DeviceInfoQueryResult
     * @throws ApiException if fails to make API call
     */
    public DeviceInfoQueryResult getDevices(@org.eclipse.jdt.annotation.NonNull Boolean supportsSync,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<DeviceInfoQueryResult> localVarResponse = getDevicesWithHttpInfo(supportsSync, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get Devices.
     * 
     * @param supportsSync Gets or sets a value indicating whether [supports synchronize]. (optional)
     * @param userId Gets or sets the user identifier. (optional)
     * @return ApiResponse&lt;DeviceInfoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceInfoQueryResult> getDevicesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Boolean supportsSync, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        return getDevicesWithHttpInfo(supportsSync, userId, null);
    }

    /**
     * Get Devices.
     * 
     * @param supportsSync Gets or sets a value indicating whether [supports synchronize]. (optional)
     * @param userId Gets or sets the user identifier. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;DeviceInfoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceInfoQueryResult> getDevicesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Boolean supportsSync, @org.eclipse.jdt.annotation.NonNull UUID userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDevicesRequestBuilder(supportsSync, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDevices", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<DeviceInfoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                DeviceInfoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<DeviceInfoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<DeviceInfoQueryResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getDevicesRequestBuilder(@org.eclipse.jdt.annotation.NonNull Boolean supportsSync,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Devices";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "supportsSync";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("supportsSync", supportsSync));
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
    public void updateDeviceOptions(@org.eclipse.jdt.annotation.Nullable String id,
            @org.eclipse.jdt.annotation.Nullable DeviceOptionsDto deviceOptionsDto) throws ApiException {
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
    public void updateDeviceOptions(@org.eclipse.jdt.annotation.Nullable String id,
            @org.eclipse.jdt.annotation.Nullable DeviceOptionsDto deviceOptionsDto, Map<String, String> headers)
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
    public ApiResponse<Void> updateDeviceOptionsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String id,
            @org.eclipse.jdt.annotation.Nullable DeviceOptionsDto deviceOptionsDto) throws ApiException {
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
    public ApiResponse<Void> updateDeviceOptionsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String id,
            @org.eclipse.jdt.annotation.Nullable DeviceOptionsDto deviceOptionsDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateDeviceOptionsRequestBuilder(id, deviceOptionsDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateDeviceOptions", localVarResponse);
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

    private HttpRequest.Builder updateDeviceOptionsRequestBuilder(@org.eclipse.jdt.annotation.Nullable String id,
            @org.eclipse.jdt.annotation.Nullable DeviceOptionsDto deviceOptionsDto, Map<String, String> headers)
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
        localVarRequestBuilder.header("Accept", "application/json");

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
