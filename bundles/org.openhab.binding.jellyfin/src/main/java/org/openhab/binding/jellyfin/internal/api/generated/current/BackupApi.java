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
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BackupManifestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BackupOptionsDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BackupRestoreRequestDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BackupApi {
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

    public BackupApi() {
        this(Configuration.getDefaultApiClient());
    }

    public BackupApi(ApiClient apiClient) {
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
     * Creates a new Backup.
     * 
     * @param backupOptionsDto The backup options. (optional)
     * @return BackupManifestDto
     * @throws ApiException if fails to make API call
     */
    public BackupManifestDto createBackup(@org.eclipse.jdt.annotation.NonNull BackupOptionsDto backupOptionsDto)
            throws ApiException {
        return createBackup(backupOptionsDto, null);
    }

    /**
     * Creates a new Backup.
     * 
     * @param backupOptionsDto The backup options. (optional)
     * @param headers Optional headers to include in the request
     * @return BackupManifestDto
     * @throws ApiException if fails to make API call
     */
    public BackupManifestDto createBackup(@org.eclipse.jdt.annotation.NonNull BackupOptionsDto backupOptionsDto,
            Map<String, String> headers) throws ApiException {
        ApiResponse<BackupManifestDto> localVarResponse = createBackupWithHttpInfo(backupOptionsDto, headers);
        return localVarResponse.getData();
    }

    /**
     * Creates a new Backup.
     * 
     * @param backupOptionsDto The backup options. (optional)
     * @return ApiResponse&lt;BackupManifestDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BackupManifestDto> createBackupWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull BackupOptionsDto backupOptionsDto) throws ApiException {
        return createBackupWithHttpInfo(backupOptionsDto, null);
    }

    /**
     * Creates a new Backup.
     * 
     * @param backupOptionsDto The backup options. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BackupManifestDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BackupManifestDto> createBackupWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull BackupOptionsDto backupOptionsDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = createBackupRequestBuilder(backupOptionsDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("createBackup", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BackupManifestDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BackupManifestDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BackupManifestDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BackupManifestDto>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder createBackupRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull BackupOptionsDto backupOptionsDto, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Backup/Create";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(backupOptionsDto);
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
     * Gets the descriptor from an existing archive is present.
     * 
     * @param path The data to start a restore process. (required)
     * @return BackupManifestDto
     * @throws ApiException if fails to make API call
     */
    public BackupManifestDto getBackup(@org.eclipse.jdt.annotation.Nullable String path) throws ApiException {
        return getBackup(path, null);
    }

    /**
     * Gets the descriptor from an existing archive is present.
     * 
     * @param path The data to start a restore process. (required)
     * @param headers Optional headers to include in the request
     * @return BackupManifestDto
     * @throws ApiException if fails to make API call
     */
    public BackupManifestDto getBackup(@org.eclipse.jdt.annotation.Nullable String path, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BackupManifestDto> localVarResponse = getBackupWithHttpInfo(path, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the descriptor from an existing archive is present.
     * 
     * @param path The data to start a restore process. (required)
     * @return ApiResponse&lt;BackupManifestDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BackupManifestDto> getBackupWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String path)
            throws ApiException {
        return getBackupWithHttpInfo(path, null);
    }

    /**
     * Gets the descriptor from an existing archive is present.
     * 
     * @param path The data to start a restore process. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BackupManifestDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BackupManifestDto> getBackupWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String path,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getBackupRequestBuilder(path, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getBackup", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BackupManifestDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BackupManifestDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BackupManifestDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BackupManifestDto>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getBackupRequestBuilder(@org.eclipse.jdt.annotation.Nullable String path,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'path' is set
        if (path == null) {
            throw new ApiException(400, "Missing the required parameter 'path' when calling getBackup");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Backup/Manifest";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "path";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("path", path));

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
     * Gets a list of all currently present backups in the backup directory.
     * 
     * @return List&lt;BackupManifestDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<BackupManifestDto> listBackups() throws ApiException {
        return listBackups(null);
    }

    /**
     * Gets a list of all currently present backups in the backup directory.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;BackupManifestDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<BackupManifestDto> listBackups(Map<String, String> headers) throws ApiException {
        ApiResponse<List<BackupManifestDto>> localVarResponse = listBackupsWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a list of all currently present backups in the backup directory.
     * 
     * @return ApiResponse&lt;List&lt;BackupManifestDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<BackupManifestDto>> listBackupsWithHttpInfo() throws ApiException {
        return listBackupsWithHttpInfo(null);
    }

    /**
     * Gets a list of all currently present backups in the backup directory.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;BackupManifestDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<BackupManifestDto>> listBackupsWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = listBackupsRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("listBackups", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<BackupManifestDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<BackupManifestDto> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<BackupManifestDto>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<BackupManifestDto>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder listBackupsRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Backup";

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
     * Restores to a backup by restarting the server and applying the backup.
     * 
     * @param backupRestoreRequestDto The data to start a restore process. (required)
     * @throws ApiException if fails to make API call
     */
    public void startRestoreBackup(@org.eclipse.jdt.annotation.Nullable BackupRestoreRequestDto backupRestoreRequestDto)
            throws ApiException {
        startRestoreBackup(backupRestoreRequestDto, null);
    }

    /**
     * Restores to a backup by restarting the server and applying the backup.
     * 
     * @param backupRestoreRequestDto The data to start a restore process. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void startRestoreBackup(@org.eclipse.jdt.annotation.Nullable BackupRestoreRequestDto backupRestoreRequestDto,
            Map<String, String> headers) throws ApiException {
        startRestoreBackupWithHttpInfo(backupRestoreRequestDto, headers);
    }

    /**
     * Restores to a backup by restarting the server and applying the backup.
     * 
     * @param backupRestoreRequestDto The data to start a restore process. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> startRestoreBackupWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable BackupRestoreRequestDto backupRestoreRequestDto) throws ApiException {
        return startRestoreBackupWithHttpInfo(backupRestoreRequestDto, null);
    }

    /**
     * Restores to a backup by restarting the server and applying the backup.
     * 
     * @param backupRestoreRequestDto The data to start a restore process. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> startRestoreBackupWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable BackupRestoreRequestDto backupRestoreRequestDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = startRestoreBackupRequestBuilder(backupRestoreRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("startRestoreBackup", localVarResponse);
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

    private HttpRequest.Builder startRestoreBackupRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable BackupRestoreRequestDto backupRestoreRequestDto,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'backupRestoreRequestDto' is set
        if (backupRestoreRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'backupRestoreRequestDto' when calling startRestoreBackup");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Backup/Restore";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(backupRestoreRequestDto);
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
