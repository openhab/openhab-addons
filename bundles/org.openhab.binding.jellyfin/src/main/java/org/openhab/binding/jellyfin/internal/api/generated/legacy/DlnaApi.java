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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.DeviceProfile;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.DeviceProfileInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DlnaApi {
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

    public DlnaApi() {
        this(Configuration.getDefaultApiClient());
    }

    public DlnaApi(ApiClient apiClient) {
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
     * Creates a profile.
     * 
     * @param deviceProfile Device profile. (optional)
     * @throws ApiException if fails to make API call
     */
    public void createProfile(@org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile) throws ApiException {
        createProfile(deviceProfile, null);
    }

    /**
     * Creates a profile.
     * 
     * @param deviceProfile Device profile. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void createProfile(@org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile,
            Map<String, String> headers) throws ApiException {
        createProfileWithHttpInfo(deviceProfile, headers);
    }

    /**
     * Creates a profile.
     * 
     * @param deviceProfile Device profile. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> createProfileWithHttpInfo(@org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile)
            throws ApiException {
        return createProfileWithHttpInfo(deviceProfile, null);
    }

    /**
     * Creates a profile.
     * 
     * @param deviceProfile Device profile. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> createProfileWithHttpInfo(@org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = createProfileRequestBuilder(deviceProfile, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("createProfile", localVarResponse);
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

    private HttpRequest.Builder createProfileRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/Profiles";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(deviceProfile);
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
     * Deletes a profile.
     * 
     * @param profileId Profile id. (required)
     * @throws ApiException if fails to make API call
     */
    public void deleteProfile(@org.eclipse.jdt.annotation.Nullable String profileId) throws ApiException {
        deleteProfile(profileId, null);
    }

    /**
     * Deletes a profile.
     * 
     * @param profileId Profile id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteProfile(@org.eclipse.jdt.annotation.Nullable String profileId, Map<String, String> headers)
            throws ApiException {
        deleteProfileWithHttpInfo(profileId, headers);
    }

    /**
     * Deletes a profile.
     * 
     * @param profileId Profile id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteProfileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String profileId)
            throws ApiException {
        return deleteProfileWithHttpInfo(profileId, null);
    }

    /**
     * Deletes a profile.
     * 
     * @param profileId Profile id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteProfileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String profileId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteProfileRequestBuilder(profileId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteProfile", localVarResponse);
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

    private HttpRequest.Builder deleteProfileRequestBuilder(@org.eclipse.jdt.annotation.Nullable String profileId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'profileId' is set
        if (profileId == null) {
            throw new ApiException(400, "Missing the required parameter 'profileId' when calling deleteProfile");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/Profiles/{profileId}".replace("{profileId}",
                ApiClient.urlEncode(profileId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Gets the default profile.
     * 
     * @return DeviceProfile
     * @throws ApiException if fails to make API call
     */
    public DeviceProfile getDefaultProfile() throws ApiException {
        return getDefaultProfile(null);
    }

    /**
     * Gets the default profile.
     * 
     * @param headers Optional headers to include in the request
     * @return DeviceProfile
     * @throws ApiException if fails to make API call
     */
    public DeviceProfile getDefaultProfile(Map<String, String> headers) throws ApiException {
        ApiResponse<DeviceProfile> localVarResponse = getDefaultProfileWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the default profile.
     * 
     * @return ApiResponse&lt;DeviceProfile&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceProfile> getDefaultProfileWithHttpInfo() throws ApiException {
        return getDefaultProfileWithHttpInfo(null);
    }

    /**
     * Gets the default profile.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;DeviceProfile&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceProfile> getDefaultProfileWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDefaultProfileRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDefaultProfile", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<DeviceProfile>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                DeviceProfile responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<DeviceProfile>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<DeviceProfile>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getDefaultProfileRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/Profiles/Default";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Gets a single profile.
     * 
     * @param profileId Profile Id. (required)
     * @return DeviceProfile
     * @throws ApiException if fails to make API call
     */
    public DeviceProfile getProfile(@org.eclipse.jdt.annotation.Nullable String profileId) throws ApiException {
        return getProfile(profileId, null);
    }

    /**
     * Gets a single profile.
     * 
     * @param profileId Profile Id. (required)
     * @param headers Optional headers to include in the request
     * @return DeviceProfile
     * @throws ApiException if fails to make API call
     */
    public DeviceProfile getProfile(@org.eclipse.jdt.annotation.Nullable String profileId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<DeviceProfile> localVarResponse = getProfileWithHttpInfo(profileId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a single profile.
     * 
     * @param profileId Profile Id. (required)
     * @return ApiResponse&lt;DeviceProfile&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceProfile> getProfileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String profileId)
            throws ApiException {
        return getProfileWithHttpInfo(profileId, null);
    }

    /**
     * Gets a single profile.
     * 
     * @param profileId Profile Id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;DeviceProfile&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DeviceProfile> getProfileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String profileId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getProfileRequestBuilder(profileId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getProfile", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<DeviceProfile>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                DeviceProfile responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<DeviceProfile>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<DeviceProfile>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getProfileRequestBuilder(@org.eclipse.jdt.annotation.Nullable String profileId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'profileId' is set
        if (profileId == null) {
            throw new ApiException(400, "Missing the required parameter 'profileId' when calling getProfile");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/Profiles/{profileId}".replace("{profileId}",
                ApiClient.urlEncode(profileId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Get profile infos.
     * 
     * @return List&lt;DeviceProfileInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<DeviceProfileInfo> getProfileInfos() throws ApiException {
        return getProfileInfos(null);
    }

    /**
     * Get profile infos.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;DeviceProfileInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<DeviceProfileInfo> getProfileInfos(Map<String, String> headers) throws ApiException {
        ApiResponse<List<DeviceProfileInfo>> localVarResponse = getProfileInfosWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Get profile infos.
     * 
     * @return ApiResponse&lt;List&lt;DeviceProfileInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<DeviceProfileInfo>> getProfileInfosWithHttpInfo() throws ApiException {
        return getProfileInfosWithHttpInfo(null);
    }

    /**
     * Get profile infos.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;DeviceProfileInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<DeviceProfileInfo>> getProfileInfosWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getProfileInfosRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getProfileInfos", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<DeviceProfileInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<DeviceProfileInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<DeviceProfileInfo>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<DeviceProfileInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getProfileInfosRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/ProfileInfos";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Updates a profile.
     * 
     * @param profileId Profile id. (required)
     * @param deviceProfile Device profile. (optional)
     * @throws ApiException if fails to make API call
     */
    public void updateProfile(@org.eclipse.jdt.annotation.Nullable String profileId,
            @org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile) throws ApiException {
        updateProfile(profileId, deviceProfile, null);
    }

    /**
     * Updates a profile.
     * 
     * @param profileId Profile id. (required)
     * @param deviceProfile Device profile. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateProfile(@org.eclipse.jdt.annotation.Nullable String profileId,
            @org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile, Map<String, String> headers)
            throws ApiException {
        updateProfileWithHttpInfo(profileId, deviceProfile, headers);
    }

    /**
     * Updates a profile.
     * 
     * @param profileId Profile id. (required)
     * @param deviceProfile Device profile. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateProfileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String profileId,
            @org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile) throws ApiException {
        return updateProfileWithHttpInfo(profileId, deviceProfile, null);
    }

    /**
     * Updates a profile.
     * 
     * @param profileId Profile id. (required)
     * @param deviceProfile Device profile. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateProfileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String profileId,
            @org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateProfileRequestBuilder(profileId, deviceProfile, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateProfile", localVarResponse);
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

    private HttpRequest.Builder updateProfileRequestBuilder(@org.eclipse.jdt.annotation.Nullable String profileId,
            @org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'profileId' is set
        if (profileId == null) {
            throw new ApiException(400, "Missing the required parameter 'profileId' when calling updateProfile");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/Profiles/{profileId}".replace("{profileId}",
                ApiClient.urlEncode(profileId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(deviceProfile);
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
