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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
     * Get Default directory browser.
     * 
     * @return DefaultDirectoryBrowserInfoDto
     * @throws ApiException if fails to make API call
     */
    public DefaultDirectoryBrowserInfoDto getDefaultDirectoryBrowser() throws ApiException {
        ApiResponse<DefaultDirectoryBrowserInfoDto> localVarResponse = getDefaultDirectoryBrowserWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Get Default directory browser.
     * 
     * @return ApiResponse&lt;DefaultDirectoryBrowserInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DefaultDirectoryBrowserInfoDto> getDefaultDirectoryBrowserWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDefaultDirectoryBrowserRequestBuilder();
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
                localVarResponse.body().close();

                return new ApiResponse<DefaultDirectoryBrowserInfoDto>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<DefaultDirectoryBrowserInfoDto>() {
                                        }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getDefaultDirectoryBrowserRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Environment/DefaultDirectoryBrowser";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
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
        ApiResponse<List<FileSystemEntryInfo>> localVarResponse = getDirectoryContentsWithHttpInfo(path, includeFiles,
                includeDirectories);
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
        HttpRequest.Builder localVarRequestBuilder = getDirectoryContentsRequestBuilder(path, includeFiles,
                includeDirectories);
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
                localVarResponse.body().close();

                return new ApiResponse<List<FileSystemEntryInfo>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<FileSystemEntryInfo>>() {
                                        }));
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
            @org.eclipse.jdt.annotation.NonNull Boolean includeDirectories) throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
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
        ApiResponse<List<FileSystemEntryInfo>> localVarResponse = getDrivesWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets available drives from the server&#39;s file system.
     * 
     * @return ApiResponse&lt;List&lt;FileSystemEntryInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<FileSystemEntryInfo>> getDrivesWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDrivesRequestBuilder();
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
                localVarResponse.body().close();

                return new ApiResponse<List<FileSystemEntryInfo>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<FileSystemEntryInfo>>() {
                                        }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getDrivesRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Environment/Drives";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
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
        ApiResponse<List<FileSystemEntryInfo>> localVarResponse = getNetworkSharesWithHttpInfo();
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
        HttpRequest.Builder localVarRequestBuilder = getNetworkSharesRequestBuilder();
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
                localVarResponse.body().close();

                return new ApiResponse<List<FileSystemEntryInfo>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<FileSystemEntryInfo>>() {
                                        }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getNetworkSharesRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Environment/NetworkShares";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
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
        ApiResponse<String> localVarResponse = getParentPathWithHttpInfo(path);
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
        HttpRequest.Builder localVarRequestBuilder = getParentPathRequestBuilder(path);
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
                localVarResponse.body().close();

                return new ApiResponse<String>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<String>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getParentPathRequestBuilder(@org.eclipse.jdt.annotation.Nullable String path)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
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
        validatePathWithHttpInfo(validatePathDto);
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
        HttpRequest.Builder localVarRequestBuilder = validatePathRequestBuilder(validatePathDto);
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
            @org.eclipse.jdt.annotation.Nullable ValidatePathDto validatePathDto) throws ApiException {
        // verify the required parameter 'validatePathDto' is set
        if (validatePathDto == null) {
            throw new ApiException(400, "Missing the required parameter 'validatePathDto' when calling validatePath");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Environment/ValidatePath";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(validatePathDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
