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
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.PackageInfo;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.RepositoryInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PackageApi {
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

    public PackageApi() {
        this(Configuration.getDefaultApiClient());
    }

    public PackageApi(ApiClient apiClient) {
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
     * Cancels a package installation.
     * 
     * @param packageId Installation Id. (required)
     * @throws ApiException if fails to make API call
     */
    public void cancelPackageInstallation(@org.eclipse.jdt.annotation.NonNull UUID packageId) throws ApiException {
        cancelPackageInstallation(packageId, null);
    }

    /**
     * Cancels a package installation.
     * 
     * @param packageId Installation Id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void cancelPackageInstallation(@org.eclipse.jdt.annotation.NonNull UUID packageId,
            Map<String, String> headers) throws ApiException {
        cancelPackageInstallationWithHttpInfo(packageId, headers);
    }

    /**
     * Cancels a package installation.
     * 
     * @param packageId Installation Id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> cancelPackageInstallationWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID packageId)
            throws ApiException {
        return cancelPackageInstallationWithHttpInfo(packageId, null);
    }

    /**
     * Cancels a package installation.
     * 
     * @param packageId Installation Id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> cancelPackageInstallationWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID packageId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = cancelPackageInstallationRequestBuilder(packageId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("cancelPackageInstallation", localVarResponse);
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

    private HttpRequest.Builder cancelPackageInstallationRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull UUID packageId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'packageId' is set
        if (packageId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'packageId' when calling cancelPackageInstallation");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Packages/Installing/{packageId}".replace("{packageId}",
                ApiClient.urlEncode(packageId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Gets a package by name or assembly GUID.
     * 
     * @param name The name of the package. (required)
     * @param assemblyGuid The GUID of the associated assembly. (optional)
     * @return PackageInfo
     * @throws ApiException if fails to make API call
     */
    public PackageInfo getPackageInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.Nullable UUID assemblyGuid) throws ApiException {
        return getPackageInfo(name, assemblyGuid, null);
    }

    /**
     * Gets a package by name or assembly GUID.
     * 
     * @param name The name of the package. (required)
     * @param assemblyGuid The GUID of the associated assembly. (optional)
     * @param headers Optional headers to include in the request
     * @return PackageInfo
     * @throws ApiException if fails to make API call
     */
    public PackageInfo getPackageInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.Nullable UUID assemblyGuid, Map<String, String> headers) throws ApiException {
        ApiResponse<PackageInfo> localVarResponse = getPackageInfoWithHttpInfo(name, assemblyGuid, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a package by name or assembly GUID.
     * 
     * @param name The name of the package. (required)
     * @param assemblyGuid The GUID of the associated assembly. (optional)
     * @return ApiResponse&lt;PackageInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PackageInfo> getPackageInfoWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.Nullable UUID assemblyGuid) throws ApiException {
        return getPackageInfoWithHttpInfo(name, assemblyGuid, null);
    }

    /**
     * Gets a package by name or assembly GUID.
     * 
     * @param name The name of the package. (required)
     * @param assemblyGuid The GUID of the associated assembly. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;PackageInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PackageInfo> getPackageInfoWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.Nullable UUID assemblyGuid, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPackageInfoRequestBuilder(name, assemblyGuid, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPackageInfo", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<PackageInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                PackageInfo responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<PackageInfo>() {
                        });

                return new ApiResponse<PackageInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getPackageInfoRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.Nullable UUID assemblyGuid, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getPackageInfo");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Packages/{name}".replace("{name}", ApiClient.urlEncode(name.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "assemblyGuid";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("assemblyGuid", assemblyGuid));

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
     * Gets available packages.
     * 
     * @return List&lt;PackageInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<PackageInfo> getPackages() throws ApiException {
        return getPackages(null);
    }

    /**
     * Gets available packages.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;PackageInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<PackageInfo> getPackages(Map<String, String> headers) throws ApiException {
        ApiResponse<List<PackageInfo>> localVarResponse = getPackagesWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets available packages.
     * 
     * @return ApiResponse&lt;List&lt;PackageInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<PackageInfo>> getPackagesWithHttpInfo() throws ApiException {
        return getPackagesWithHttpInfo(null);
    }

    /**
     * Gets available packages.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;PackageInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<PackageInfo>> getPackagesWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPackagesRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPackages", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<PackageInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<PackageInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<PackageInfo>>() {
                        });

                return new ApiResponse<List<PackageInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getPackagesRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Packages";

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
     * Gets all package repositories.
     * 
     * @return List&lt;RepositoryInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RepositoryInfo> getRepositories() throws ApiException {
        return getRepositories(null);
    }

    /**
     * Gets all package repositories.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;RepositoryInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RepositoryInfo> getRepositories(Map<String, String> headers) throws ApiException {
        ApiResponse<List<RepositoryInfo>> localVarResponse = getRepositoriesWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets all package repositories.
     * 
     * @return ApiResponse&lt;List&lt;RepositoryInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RepositoryInfo>> getRepositoriesWithHttpInfo() throws ApiException {
        return getRepositoriesWithHttpInfo(null);
    }

    /**
     * Gets all package repositories.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RepositoryInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RepositoryInfo>> getRepositoriesWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRepositoriesRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRepositories", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<RepositoryInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<RepositoryInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RepositoryInfo>>() {
                        });

                return new ApiResponse<List<RepositoryInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getRepositoriesRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Repositories";

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
     * Installs a package.
     * 
     * @param name Package name. (required)
     * @param assemblyGuid GUID of the associated assembly. (optional)
     * @param version Optional version. Defaults to latest version. (optional)
     * @param repositoryUrl Optional. Specify the repository to install from. (optional)
     * @throws ApiException if fails to make API call
     */
    public void installPackage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.Nullable UUID assemblyGuid, @org.eclipse.jdt.annotation.Nullable String version,
            @org.eclipse.jdt.annotation.Nullable String repositoryUrl) throws ApiException {
        installPackage(name, assemblyGuid, version, repositoryUrl, null);
    }

    /**
     * Installs a package.
     * 
     * @param name Package name. (required)
     * @param assemblyGuid GUID of the associated assembly. (optional)
     * @param version Optional version. Defaults to latest version. (optional)
     * @param repositoryUrl Optional. Specify the repository to install from. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void installPackage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.Nullable UUID assemblyGuid, @org.eclipse.jdt.annotation.Nullable String version,
            @org.eclipse.jdt.annotation.Nullable String repositoryUrl, Map<String, String> headers)
            throws ApiException {
        installPackageWithHttpInfo(name, assemblyGuid, version, repositoryUrl, headers);
    }

    /**
     * Installs a package.
     * 
     * @param name Package name. (required)
     * @param assemblyGuid GUID of the associated assembly. (optional)
     * @param version Optional version. Defaults to latest version. (optional)
     * @param repositoryUrl Optional. Specify the repository to install from. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> installPackageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.Nullable UUID assemblyGuid, @org.eclipse.jdt.annotation.Nullable String version,
            @org.eclipse.jdt.annotation.Nullable String repositoryUrl) throws ApiException {
        return installPackageWithHttpInfo(name, assemblyGuid, version, repositoryUrl, null);
    }

    /**
     * Installs a package.
     * 
     * @param name Package name. (required)
     * @param assemblyGuid GUID of the associated assembly. (optional)
     * @param version Optional version. Defaults to latest version. (optional)
     * @param repositoryUrl Optional. Specify the repository to install from. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> installPackageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.Nullable UUID assemblyGuid, @org.eclipse.jdt.annotation.Nullable String version,
            @org.eclipse.jdt.annotation.Nullable String repositoryUrl, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = installPackageRequestBuilder(name, assemblyGuid, version,
                repositoryUrl, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("installPackage", localVarResponse);
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

    private HttpRequest.Builder installPackageRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.Nullable UUID assemblyGuid, @org.eclipse.jdt.annotation.Nullable String version,
            @org.eclipse.jdt.annotation.Nullable String repositoryUrl, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling installPackage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Packages/Installed/{name}".replace("{name}", ApiClient.urlEncode(name.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "assemblyGuid";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("assemblyGuid", assemblyGuid));
        localVarQueryParameterBaseName = "version";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("version", version));
        localVarQueryParameterBaseName = "repositoryUrl";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("repositoryUrl", repositoryUrl));

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
     * Sets the enabled and existing package repositories.
     * 
     * @param repositoryInfo The list of package repositories. (required)
     * @throws ApiException if fails to make API call
     */
    public void setRepositories(@org.eclipse.jdt.annotation.NonNull List<RepositoryInfo> repositoryInfo)
            throws ApiException {
        setRepositories(repositoryInfo, null);
    }

    /**
     * Sets the enabled and existing package repositories.
     * 
     * @param repositoryInfo The list of package repositories. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void setRepositories(@org.eclipse.jdt.annotation.NonNull List<RepositoryInfo> repositoryInfo,
            Map<String, String> headers) throws ApiException {
        setRepositoriesWithHttpInfo(repositoryInfo, headers);
    }

    /**
     * Sets the enabled and existing package repositories.
     * 
     * @param repositoryInfo The list of package repositories. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setRepositoriesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull List<RepositoryInfo> repositoryInfo) throws ApiException {
        return setRepositoriesWithHttpInfo(repositoryInfo, null);
    }

    /**
     * Sets the enabled and existing package repositories.
     * 
     * @param repositoryInfo The list of package repositories. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setRepositoriesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull List<RepositoryInfo> repositoryInfo, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = setRepositoriesRequestBuilder(repositoryInfo, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("setRepositories", localVarResponse);
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

    private HttpRequest.Builder setRepositoriesRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull List<RepositoryInfo> repositoryInfo, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'repositoryInfo' is set
        if (repositoryInfo == null) {
            throw new ApiException(400, "Missing the required parameter 'repositoryInfo' when calling setRepositories");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Repositories";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(repositoryInfo);
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
