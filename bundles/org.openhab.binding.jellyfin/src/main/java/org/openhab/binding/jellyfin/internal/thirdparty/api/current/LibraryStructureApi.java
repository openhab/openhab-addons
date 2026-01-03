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
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiException;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiResponse;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Configuration;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Pair;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.AddVirtualFolderDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.CollectionTypeOptions;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.MediaPathDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.UpdateLibraryOptionsDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.UpdateMediaPathRequestDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.VirtualFolderInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LibraryStructureApi {
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

    public LibraryStructureApi() {
        this(Configuration.getDefaultApiClient());
    }

    public LibraryStructureApi(ApiClient apiClient) {
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
     * Add a media path to a library.
     * 
     * @param mediaPathDto The media path dto. (required)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @throws ApiException if fails to make API call
     */
    public void addMediaPath(@org.eclipse.jdt.annotation.NonNull MediaPathDto mediaPathDto,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary) throws ApiException {
        addMediaPath(mediaPathDto, refreshLibrary, null);
    }

    /**
     * Add a media path to a library.
     * 
     * @param mediaPathDto The media path dto. (required)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void addMediaPath(@org.eclipse.jdt.annotation.NonNull MediaPathDto mediaPathDto,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {
        addMediaPathWithHttpInfo(mediaPathDto, refreshLibrary, headers);
    }

    /**
     * Add a media path to a library.
     * 
     * @param mediaPathDto The media path dto. (required)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> addMediaPathWithHttpInfo(@org.eclipse.jdt.annotation.NonNull MediaPathDto mediaPathDto,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary) throws ApiException {
        return addMediaPathWithHttpInfo(mediaPathDto, refreshLibrary, null);
    }

    /**
     * Add a media path to a library.
     * 
     * @param mediaPathDto The media path dto. (required)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> addMediaPathWithHttpInfo(@org.eclipse.jdt.annotation.NonNull MediaPathDto mediaPathDto,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = addMediaPathRequestBuilder(mediaPathDto, refreshLibrary, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("addMediaPath", localVarResponse);
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

    private HttpRequest.Builder addMediaPathRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull MediaPathDto mediaPathDto,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'mediaPathDto' is set
        if (mediaPathDto == null) {
            throw new ApiException(400, "Missing the required parameter 'mediaPathDto' when calling addMediaPath");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/VirtualFolders/Paths";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "refreshLibrary";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("refreshLibrary", refreshLibrary));

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
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(mediaPathDto);
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
     * Adds a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param collectionType The type of the collection. (optional)
     * @param paths The paths of the virtual folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param addVirtualFolderDto The library options. (optional)
     * @throws ApiException if fails to make API call
     */
    public void addVirtualFolder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable CollectionTypeOptions collectionType,
            @org.eclipse.jdt.annotation.Nullable List<String> paths,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary,
            @org.eclipse.jdt.annotation.Nullable AddVirtualFolderDto addVirtualFolderDto) throws ApiException {
        addVirtualFolder(name, collectionType, paths, refreshLibrary, addVirtualFolderDto, null);
    }

    /**
     * Adds a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param collectionType The type of the collection. (optional)
     * @param paths The paths of the virtual folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param addVirtualFolderDto The library options. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void addVirtualFolder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable CollectionTypeOptions collectionType,
            @org.eclipse.jdt.annotation.Nullable List<String> paths,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary,
            @org.eclipse.jdt.annotation.Nullable AddVirtualFolderDto addVirtualFolderDto, Map<String, String> headers)
            throws ApiException {
        addVirtualFolderWithHttpInfo(name, collectionType, paths, refreshLibrary, addVirtualFolderDto, headers);
    }

    /**
     * Adds a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param collectionType The type of the collection. (optional)
     * @param paths The paths of the virtual folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param addVirtualFolderDto The library options. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> addVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable CollectionTypeOptions collectionType,
            @org.eclipse.jdt.annotation.Nullable List<String> paths,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary,
            @org.eclipse.jdt.annotation.Nullable AddVirtualFolderDto addVirtualFolderDto) throws ApiException {
        return addVirtualFolderWithHttpInfo(name, collectionType, paths, refreshLibrary, addVirtualFolderDto, null);
    }

    /**
     * Adds a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param collectionType The type of the collection. (optional)
     * @param paths The paths of the virtual folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param addVirtualFolderDto The library options. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> addVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable CollectionTypeOptions collectionType,
            @org.eclipse.jdt.annotation.Nullable List<String> paths,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary,
            @org.eclipse.jdt.annotation.Nullable AddVirtualFolderDto addVirtualFolderDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = addVirtualFolderRequestBuilder(name, collectionType, paths,
                refreshLibrary, addVirtualFolderDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("addVirtualFolder", localVarResponse);
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

    private HttpRequest.Builder addVirtualFolderRequestBuilder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable CollectionTypeOptions collectionType,
            @org.eclipse.jdt.annotation.Nullable List<String> paths,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary,
            @org.eclipse.jdt.annotation.Nullable AddVirtualFolderDto addVirtualFolderDto, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/VirtualFolders";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "name";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("name", name));
        localVarQueryParameterBaseName = "collectionType";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("collectionType", collectionType));
        localVarQueryParameterBaseName = "paths";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "paths", paths));
        localVarQueryParameterBaseName = "refreshLibrary";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("refreshLibrary", refreshLibrary));

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
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(addVirtualFolderDto);
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
     * Gets all virtual folders.
     * 
     * @return List&lt;VirtualFolderInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<VirtualFolderInfo> getVirtualFolders() throws ApiException {
        return getVirtualFolders(null);
    }

    /**
     * Gets all virtual folders.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;VirtualFolderInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<VirtualFolderInfo> getVirtualFolders(Map<String, String> headers) throws ApiException {
        ApiResponse<List<VirtualFolderInfo>> localVarResponse = getVirtualFoldersWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets all virtual folders.
     * 
     * @return ApiResponse&lt;List&lt;VirtualFolderInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<VirtualFolderInfo>> getVirtualFoldersWithHttpInfo() throws ApiException {
        return getVirtualFoldersWithHttpInfo(null);
    }

    /**
     * Gets all virtual folders.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;VirtualFolderInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<VirtualFolderInfo>> getVirtualFoldersWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getVirtualFoldersRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getVirtualFolders", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<VirtualFolderInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<VirtualFolderInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<VirtualFolderInfo>>() {
                        });

                return new ApiResponse<List<VirtualFolderInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getVirtualFoldersRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/VirtualFolders";

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
     * Remove a media path.
     * 
     * @param name The name of the library. (optional)
     * @param path The path to remove. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @throws ApiException if fails to make API call
     */
    public void removeMediaPath(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String path,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary) throws ApiException {
        removeMediaPath(name, path, refreshLibrary, null);
    }

    /**
     * Remove a media path.
     * 
     * @param name The name of the library. (optional)
     * @param path The path to remove. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void removeMediaPath(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String path,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {
        removeMediaPathWithHttpInfo(name, path, refreshLibrary, headers);
    }

    /**
     * Remove a media path.
     * 
     * @param name The name of the library. (optional)
     * @param path The path to remove. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeMediaPathWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String path,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary) throws ApiException {
        return removeMediaPathWithHttpInfo(name, path, refreshLibrary, null);
    }

    /**
     * Remove a media path.
     * 
     * @param name The name of the library. (optional)
     * @param path The path to remove. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeMediaPathWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String path,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = removeMediaPathRequestBuilder(name, path, refreshLibrary, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("removeMediaPath", localVarResponse);
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

    private HttpRequest.Builder removeMediaPathRequestBuilder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String path,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/VirtualFolders/Paths";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "name";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("name", name));
        localVarQueryParameterBaseName = "path";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("path", path));
        localVarQueryParameterBaseName = "refreshLibrary";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("refreshLibrary", refreshLibrary));

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

        localVarRequestBuilder.header("Accept", "text/html");

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
     * Removes a virtual folder.
     * 
     * @param name The name of the folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @throws ApiException if fails to make API call
     */
    public void removeVirtualFolder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary) throws ApiException {
        removeVirtualFolder(name, refreshLibrary, null);
    }

    /**
     * Removes a virtual folder.
     * 
     * @param name The name of the folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void removeVirtualFolder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {
        removeVirtualFolderWithHttpInfo(name, refreshLibrary, headers);
    }

    /**
     * Removes a virtual folder.
     * 
     * @param name The name of the folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary) throws ApiException {
        return removeVirtualFolderWithHttpInfo(name, refreshLibrary, null);
    }

    /**
     * Removes a virtual folder.
     * 
     * @param name The name of the folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = removeVirtualFolderRequestBuilder(name, refreshLibrary, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("removeVirtualFolder", localVarResponse);
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

    private HttpRequest.Builder removeVirtualFolderRequestBuilder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/VirtualFolders";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "name";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("name", name));
        localVarQueryParameterBaseName = "refreshLibrary";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("refreshLibrary", refreshLibrary));

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

        localVarRequestBuilder.header("Accept", "text/html");

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
     * Renames a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param newName The new name. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @throws ApiException if fails to make API call
     */
    public void renameVirtualFolder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String newName,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary) throws ApiException {
        renameVirtualFolder(name, newName, refreshLibrary, null);
    }

    /**
     * Renames a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param newName The new name. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void renameVirtualFolder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String newName,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {
        renameVirtualFolderWithHttpInfo(name, newName, refreshLibrary, headers);
    }

    /**
     * Renames a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param newName The new name. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> renameVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String newName,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary) throws ApiException {
        return renameVirtualFolderWithHttpInfo(name, newName, refreshLibrary, null);
    }

    /**
     * Renames a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param newName The new name. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> renameVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String newName,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = renameVirtualFolderRequestBuilder(name, newName, refreshLibrary,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("renameVirtualFolder", localVarResponse);
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

    private HttpRequest.Builder renameVirtualFolderRequestBuilder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String newName,
            @org.eclipse.jdt.annotation.Nullable Boolean refreshLibrary, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/VirtualFolders/Name";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "name";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("name", name));
        localVarQueryParameterBaseName = "newName";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("newName", newName));
        localVarQueryParameterBaseName = "refreshLibrary";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("refreshLibrary", refreshLibrary));

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
     * Update library options.
     * 
     * @param updateLibraryOptionsDto The library name and options. (optional)
     * @throws ApiException if fails to make API call
     */
    public void updateLibraryOptions(
            @org.eclipse.jdt.annotation.Nullable UpdateLibraryOptionsDto updateLibraryOptionsDto) throws ApiException {
        updateLibraryOptions(updateLibraryOptionsDto, null);
    }

    /**
     * Update library options.
     * 
     * @param updateLibraryOptionsDto The library name and options. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateLibraryOptions(
            @org.eclipse.jdt.annotation.Nullable UpdateLibraryOptionsDto updateLibraryOptionsDto,
            Map<String, String> headers) throws ApiException {
        updateLibraryOptionsWithHttpInfo(updateLibraryOptionsDto, headers);
    }

    /**
     * Update library options.
     * 
     * @param updateLibraryOptionsDto The library name and options. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateLibraryOptionsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UpdateLibraryOptionsDto updateLibraryOptionsDto) throws ApiException {
        return updateLibraryOptionsWithHttpInfo(updateLibraryOptionsDto, null);
    }

    /**
     * Update library options.
     * 
     * @param updateLibraryOptionsDto The library name and options. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateLibraryOptionsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UpdateLibraryOptionsDto updateLibraryOptionsDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateLibraryOptionsRequestBuilder(updateLibraryOptionsDto,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateLibraryOptions", localVarResponse);
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

    private HttpRequest.Builder updateLibraryOptionsRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UpdateLibraryOptionsDto updateLibraryOptionsDto,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/VirtualFolders/LibraryOptions";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(updateLibraryOptionsDto);
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
     * Updates a media path.
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateMediaPath(@org.eclipse.jdt.annotation.NonNull UpdateMediaPathRequestDto updateMediaPathRequestDto)
            throws ApiException {
        updateMediaPath(updateMediaPathRequestDto, null);
    }

    /**
     * Updates a media path.
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateMediaPath(@org.eclipse.jdt.annotation.NonNull UpdateMediaPathRequestDto updateMediaPathRequestDto,
            Map<String, String> headers) throws ApiException {
        updateMediaPathWithHttpInfo(updateMediaPathRequestDto, headers);
    }

    /**
     * Updates a media path.
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateMediaPathWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UpdateMediaPathRequestDto updateMediaPathRequestDto)
            throws ApiException {
        return updateMediaPathWithHttpInfo(updateMediaPathRequestDto, null);
    }

    /**
     * Updates a media path.
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateMediaPathWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UpdateMediaPathRequestDto updateMediaPathRequestDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateMediaPathRequestBuilder(updateMediaPathRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateMediaPath", localVarResponse);
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

    private HttpRequest.Builder updateMediaPathRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull UpdateMediaPathRequestDto updateMediaPathRequestDto,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'updateMediaPathRequestDto' is set
        if (updateMediaPathRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'updateMediaPathRequestDto' when calling updateMediaPath");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/VirtualFolders/Paths/Update";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(updateMediaPathRequestDto);
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
