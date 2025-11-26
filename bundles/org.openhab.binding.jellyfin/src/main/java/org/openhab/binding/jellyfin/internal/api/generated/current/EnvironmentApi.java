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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.DefaultDirectoryBrowserInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.FileSystemEntryInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ValidatePathDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class EnvironmentApi {
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

    public EnvironmentApi() {
        this(Configuration.getDefaultApiClient());
    }

    public EnvironmentApi(ApiClient apiClient) {
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
     * Get Default directory browser.
     * 
     * @return DefaultDirectoryBrowserInfoDto
     * @throws ApiException if fails to make API call
     */
    public DefaultDirectoryBrowserInfoDto getDefaultDirectoryBrowser() throws ApiException {
        return getDefaultDirectoryBrowser(null);
    }

    /**
     * Get Default directory browser.
     * 
     * @param headers Optional headers to include in the request
     * @return DefaultDirectoryBrowserInfoDto
     * @throws ApiException if fails to make API call
     */
    public DefaultDirectoryBrowserInfoDto getDefaultDirectoryBrowser(Map<String, String> headers) throws ApiException {
        ApiResponse<DefaultDirectoryBrowserInfoDto> localVarResponse = getDefaultDirectoryBrowserWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Get Default directory browser.
     * 
     * @return ApiResponse&lt;DefaultDirectoryBrowserInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DefaultDirectoryBrowserInfoDto> getDefaultDirectoryBrowserWithHttpInfo() throws ApiException {
        return getDefaultDirectoryBrowserWithHttpInfo(null);
    }

    /**
     * Get Default directory browser.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;DefaultDirectoryBrowserInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DefaultDirectoryBrowserInfoDto> getDefaultDirectoryBrowserWithHttpInfo(
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDefaultDirectoryBrowserRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDefaultDirectoryBrowser", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<DefaultDirectoryBrowserInfoDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                DefaultDirectoryBrowserInfoDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody,
                                new TypeReference<DefaultDirectoryBrowserInfoDto>() {
                                });

                localVarResponse.body().close();

                return new ApiResponse<DefaultDirectoryBrowserInfoDto>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getDefaultDirectoryBrowserRequestBuilder(Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Environment/DefaultDirectoryBrowser";

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
     * Gets the contents of a given directory in the file system.
     * 
     * @param path The path. (required)
     * @param includeFiles An optional filter to include or exclude files from the results. true/false. (optional,
     *            default to false)
     * @param includeDirectories An optional filter to include or exclude folders from the results. true/false.
     *            (optional, default to false)
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<FileSystemEntryInfo> getDirectoryContents(@org.eclipse.jdt.annotation.Nullable String path,
            @org.eclipse.jdt.annotation.NonNull Boolean includeFiles,
            @org.eclipse.jdt.annotation.NonNull Boolean includeDirectories) throws ApiException {
        return getDirectoryContents(path, includeFiles, includeDirectories, null);
    }

    /**
     * Gets the contents of a given directory in the file system.
     * 
     * @param path The path. (required)
     * @param includeFiles An optional filter to include or exclude files from the results. true/false. (optional,
     *            default to false)
     * @param includeDirectories An optional filter to include or exclude folders from the results. true/false.
     *            (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<FileSystemEntryInfo> getDirectoryContents(@org.eclipse.jdt.annotation.Nullable String path,
            @org.eclipse.jdt.annotation.NonNull Boolean includeFiles,
            @org.eclipse.jdt.annotation.NonNull Boolean includeDirectories, Map<String, String> headers)
            throws ApiException {
        ApiResponse<List<FileSystemEntryInfo>> localVarResponse = getDirectoryContentsWithHttpInfo(path, includeFiles,
                includeDirectories, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the contents of a given directory in the file system.
     * 
     * @param path The path. (required)
     * @param includeFiles An optional filter to include or exclude files from the results. true/false. (optional,
     *            default to false)
     * @param includeDirectories An optional filter to include or exclude folders from the results. true/false.
     *            (optional, default to false)
     * @return ApiResponse&lt;List&lt;FileSystemEntryInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<FileSystemEntryInfo>> getDirectoryContentsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String path, @org.eclipse.jdt.annotation.NonNull Boolean includeFiles,
            @org.eclipse.jdt.annotation.NonNull Boolean includeDirectories) throws ApiException {
        return getDirectoryContentsWithHttpInfo(path, includeFiles, includeDirectories, null);
    }

    /**
     * Gets the contents of a given directory in the file system.
     * 
     * @param path The path. (required)
     * @param includeFiles An optional filter to include or exclude files from the results. true/false. (optional,
     *            default to false)
     * @param includeDirectories An optional filter to include or exclude folders from the results. true/false.
     *            (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;FileSystemEntryInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<FileSystemEntryInfo>> getDirectoryContentsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String path, @org.eclipse.jdt.annotation.NonNull Boolean includeFiles,
            @org.eclipse.jdt.annotation.NonNull Boolean includeDirectories, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDirectoryContentsRequestBuilder(path, includeFiles,
                includeDirectories, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDirectoryContents", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<FileSystemEntryInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<FileSystemEntryInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<FileSystemEntryInfo>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<FileSystemEntryInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getDirectoryContentsRequestBuilder(@org.eclipse.jdt.annotation.Nullable String path,
            @org.eclipse.jdt.annotation.NonNull Boolean includeFiles,
            @org.eclipse.jdt.annotation.NonNull Boolean includeDirectories, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'path' is set
        if (path == null) {
            throw new ApiException(400, "Missing the required parameter 'path' when calling getDirectoryContents");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Environment/DirectoryContents";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "path";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("path", path));
        localVarQueryParameterBaseName = "includeFiles";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("includeFiles", includeFiles));
        localVarQueryParameterBaseName = "includeDirectories";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("includeDirectories", includeDirectories));

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
     * Gets available drives from the server&#39;s file system.
     * 
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<FileSystemEntryInfo> getDrives() throws ApiException {
        return getDrives(null);
    }

    /**
     * Gets available drives from the server&#39;s file system.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<FileSystemEntryInfo> getDrives(Map<String, String> headers) throws ApiException {
        ApiResponse<List<FileSystemEntryInfo>> localVarResponse = getDrivesWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets available drives from the server&#39;s file system.
     * 
     * @return ApiResponse&lt;List&lt;FileSystemEntryInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<FileSystemEntryInfo>> getDrivesWithHttpInfo() throws ApiException {
        return getDrivesWithHttpInfo(null);
    }

    /**
     * Gets available drives from the server&#39;s file system.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;FileSystemEntryInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<FileSystemEntryInfo>> getDrivesWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDrivesRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDrives", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<FileSystemEntryInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<FileSystemEntryInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<FileSystemEntryInfo>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<FileSystemEntryInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getDrivesRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Environment/Drives";

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
     * Gets network paths.
     * 
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public List<FileSystemEntryInfo> getNetworkShares() throws ApiException {
        return getNetworkShares(null);
    }

    /**
     * Gets network paths.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public List<FileSystemEntryInfo> getNetworkShares(Map<String, String> headers) throws ApiException {
        ApiResponse<List<FileSystemEntryInfo>> localVarResponse = getNetworkSharesWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets network paths.
     * 
     * @return ApiResponse&lt;List&lt;FileSystemEntryInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<List<FileSystemEntryInfo>> getNetworkSharesWithHttpInfo() throws ApiException {
        return getNetworkSharesWithHttpInfo(null);
    }

    /**
     * Gets network paths.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;FileSystemEntryInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<List<FileSystemEntryInfo>> getNetworkSharesWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getNetworkSharesRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getNetworkShares", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<FileSystemEntryInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<FileSystemEntryInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<FileSystemEntryInfo>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<FileSystemEntryInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getNetworkSharesRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Environment/NetworkShares";

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
     * Gets the parent path of a given path.
     * 
     * @param path The path. (required)
     * @return String
     * @throws ApiException if fails to make API call
     */
    public String getParentPath(@org.eclipse.jdt.annotation.Nullable String path) throws ApiException {
        return getParentPath(path, null);
    }

    /**
     * Gets the parent path of a given path.
     * 
     * @param path The path. (required)
     * @param headers Optional headers to include in the request
     * @return String
     * @throws ApiException if fails to make API call
     */
    public String getParentPath(@org.eclipse.jdt.annotation.Nullable String path, Map<String, String> headers)
            throws ApiException {
        ApiResponse<String> localVarResponse = getParentPathWithHttpInfo(path, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the parent path of a given path.
     * 
     * @param path The path. (required)
     * @return ApiResponse&lt;String&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<String> getParentPathWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String path)
            throws ApiException {
        return getParentPathWithHttpInfo(path, null);
    }

    /**
     * Gets the parent path of a given path.
     * 
     * @param path The path. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;String&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<String> getParentPathWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String path,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getParentPathRequestBuilder(path, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getParentPath", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<String>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                String responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<String>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<String>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getParentPathRequestBuilder(@org.eclipse.jdt.annotation.Nullable String path,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'path' is set
        if (path == null) {
            throw new ApiException(400, "Missing the required parameter 'path' when calling getParentPath");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Environment/ParentPath";

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
     * Validates path.
     * 
     * @param validatePathDto Validate request object. (required)
     * @throws ApiException if fails to make API call
     */
    public void validatePath(@org.eclipse.jdt.annotation.Nullable ValidatePathDto validatePathDto) throws ApiException {
        validatePath(validatePathDto, null);
    }

    /**
     * Validates path.
     * 
     * @param validatePathDto Validate request object. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void validatePath(@org.eclipse.jdt.annotation.Nullable ValidatePathDto validatePathDto,
            Map<String, String> headers) throws ApiException {
        validatePathWithHttpInfo(validatePathDto, headers);
    }

    /**
     * Validates path.
     * 
     * @param validatePathDto Validate request object. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> validatePathWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ValidatePathDto validatePathDto) throws ApiException {
        return validatePathWithHttpInfo(validatePathDto, null);
    }

    /**
     * Validates path.
     * 
     * @param validatePathDto Validate request object. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> validatePathWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ValidatePathDto validatePathDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = validatePathRequestBuilder(validatePathDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("validatePath", localVarResponse);
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

    private HttpRequest.Builder validatePathRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable ValidatePathDto validatePathDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'validatePathDto' is set
        if (validatePathDto == null) {
            throw new ApiException(400, "Missing the required parameter 'validatePathDto' when calling validatePath");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Environment/ValidatePath";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(validatePathDto);
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
