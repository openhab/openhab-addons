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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.AddVirtualFolderDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.CollectionTypeOptions;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaPathDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UpdateLibraryOptionsDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UpdateMediaPathRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.VirtualFolderInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LibraryStructureApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

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
     * Add a media path to a library.
     * 
     * @param mediaPathDto The media path dto. (required)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @throws ApiException if fails to make API call
     */
    public void addMediaPath(@org.eclipse.jdt.annotation.Nullable MediaPathDto mediaPathDto,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        addMediaPathWithHttpInfo(mediaPathDto, refreshLibrary);
    }

    /**
     * Add a media path to a library.
     * 
     * @param mediaPathDto The media path dto. (required)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> addMediaPathWithHttpInfo(@org.eclipse.jdt.annotation.Nullable MediaPathDto mediaPathDto,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = addMediaPathRequestBuilder(mediaPathDto, refreshLibrary);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("addMediaPath", localVarResponse);
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

    private HttpRequest.Builder addMediaPathRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable MediaPathDto mediaPathDto,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
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
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(mediaPathDto);
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
    public void addVirtualFolder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull CollectionTypeOptions collectionType,
            @org.eclipse.jdt.annotation.NonNull List<String> paths,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary,
            @org.eclipse.jdt.annotation.NonNull AddVirtualFolderDto addVirtualFolderDto) throws ApiException {
        addVirtualFolderWithHttpInfo(name, collectionType, paths, refreshLibrary, addVirtualFolderDto);
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
    public ApiResponse<Void> addVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull CollectionTypeOptions collectionType,
            @org.eclipse.jdt.annotation.NonNull List<String> paths,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary,
            @org.eclipse.jdt.annotation.NonNull AddVirtualFolderDto addVirtualFolderDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = addVirtualFolderRequestBuilder(name, collectionType, paths,
                refreshLibrary, addVirtualFolderDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("addVirtualFolder", localVarResponse);
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

    private HttpRequest.Builder addVirtualFolderRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull CollectionTypeOptions collectionType,
            @org.eclipse.jdt.annotation.NonNull List<String> paths,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary,
            @org.eclipse.jdt.annotation.NonNull AddVirtualFolderDto addVirtualFolderDto) throws ApiException {

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
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(addVirtualFolderDto);
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

    /**
     * Gets all virtual folders.
     * 
     * @return List&lt;VirtualFolderInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<VirtualFolderInfo> getVirtualFolders() throws ApiException {
        ApiResponse<List<VirtualFolderInfo>> localVarResponse = getVirtualFoldersWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets all virtual folders.
     * 
     * @return ApiResponse&lt;List&lt;VirtualFolderInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<VirtualFolderInfo>> getVirtualFoldersWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getVirtualFoldersRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getVirtualFolders", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<VirtualFolderInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<VirtualFolderInfo>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<VirtualFolderInfo>>() {
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

    private HttpRequest.Builder getVirtualFoldersRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/VirtualFolders";

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
     * Remove a media path.
     * 
     * @param name The name of the library. (optional)
     * @param path The path to remove. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @throws ApiException if fails to make API call
     */
    public void removeMediaPath(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull String path, @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary)
            throws ApiException {
        removeMediaPathWithHttpInfo(name, path, refreshLibrary);
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
    public ApiResponse<Void> removeMediaPathWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull String path, @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = removeMediaPathRequestBuilder(name, path, refreshLibrary);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("removeMediaPath", localVarResponse);
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

    private HttpRequest.Builder removeMediaPathRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull String path, @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary)
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

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
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
    public void removeVirtualFolder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        removeVirtualFolderWithHttpInfo(name, refreshLibrary);
    }

    /**
     * Removes a virtual folder.
     * 
     * @param name The name of the folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = removeVirtualFolderRequestBuilder(name, refreshLibrary);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("removeVirtualFolder", localVarResponse);
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

    private HttpRequest.Builder removeVirtualFolderRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {

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

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
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
    public void renameVirtualFolder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull String newName,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        renameVirtualFolderWithHttpInfo(name, newName, refreshLibrary);
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
    public ApiResponse<Void> renameVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull String newName,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = renameVirtualFolderRequestBuilder(name, newName, refreshLibrary);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("renameVirtualFolder", localVarResponse);
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

    private HttpRequest.Builder renameVirtualFolderRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull String newName,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
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
            @org.eclipse.jdt.annotation.NonNull UpdateLibraryOptionsDto updateLibraryOptionsDto) throws ApiException {
        updateLibraryOptionsWithHttpInfo(updateLibraryOptionsDto);
    }

    /**
     * Update library options.
     * 
     * @param updateLibraryOptionsDto The library name and options. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateLibraryOptionsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UpdateLibraryOptionsDto updateLibraryOptionsDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateLibraryOptionsRequestBuilder(updateLibraryOptionsDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateLibraryOptions", localVarResponse);
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

    private HttpRequest.Builder updateLibraryOptionsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull UpdateLibraryOptionsDto updateLibraryOptionsDto) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/VirtualFolders/LibraryOptions";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(updateLibraryOptionsDto);
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

    /**
     * Updates a media path.
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateMediaPath(
            @org.eclipse.jdt.annotation.Nullable UpdateMediaPathRequestDto updateMediaPathRequestDto)
            throws ApiException {
        updateMediaPathWithHttpInfo(updateMediaPathRequestDto);
    }

    /**
     * Updates a media path.
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateMediaPathWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UpdateMediaPathRequestDto updateMediaPathRequestDto)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateMediaPathRequestBuilder(updateMediaPathRequestDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateMediaPath", localVarResponse);
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

    private HttpRequest.Builder updateMediaPathRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UpdateMediaPathRequestDto updateMediaPathRequestDto)
            throws ApiException {
        // verify the required parameter 'updateMediaPathRequestDto' is set
        if (updateMediaPathRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'updateMediaPathRequestDto' when calling updateMediaPath");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Library/VirtualFolders/Paths/Update";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(updateMediaPathRequestDto);
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
