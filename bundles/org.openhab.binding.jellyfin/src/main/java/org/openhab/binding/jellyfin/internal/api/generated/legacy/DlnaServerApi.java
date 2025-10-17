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
import java.util.Map;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DlnaServerApi {
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

    public DlnaServerApi() {
        this(Configuration.getDefaultApiClient());
    }

    public DlnaServerApi(ApiClient apiClient) {
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
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getConnectionManager(@org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getConnectionManager(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getConnectionManager(@org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getConnectionManagerWithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getConnectionManagerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return getConnectionManagerWithHttpInfo(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getConnectionManagerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getConnectionManagerRequestBuilder(serverId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getConnectionManager", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getConnectionManagerRequestBuilder(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400, "Missing the required parameter 'serverId' when calling getConnectionManager");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/ConnectionManager".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getConnectionManager2(@org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getConnectionManager2(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getConnectionManager2(@org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getConnectionManager2WithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getConnectionManager2WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return getConnectionManager2WithHttpInfo(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getConnectionManager2WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getConnectionManager2RequestBuilder(serverId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getConnectionManager2", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getConnectionManager2RequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400, "Missing the required parameter 'serverId' when calling getConnectionManager2");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/ConnectionManager/ConnectionManager".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getConnectionManager3(@org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getConnectionManager3(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getConnectionManager3(@org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getConnectionManager3WithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getConnectionManager3WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return getConnectionManager3WithHttpInfo(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getConnectionManager3WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getConnectionManager3RequestBuilder(serverId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getConnectionManager3", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getConnectionManager3RequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400, "Missing the required parameter 'serverId' when calling getConnectionManager3");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/ConnectionManager/ConnectionManager.xml".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getContentDirectory(@org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getContentDirectory(serverId, null);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getContentDirectory(@org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getContentDirectoryWithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getContentDirectoryWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return getContentDirectoryWithHttpInfo(serverId, null);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getContentDirectoryWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getContentDirectoryRequestBuilder(serverId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getContentDirectory", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getContentDirectoryRequestBuilder(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400, "Missing the required parameter 'serverId' when calling getContentDirectory");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/ContentDirectory".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getContentDirectory2(@org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getContentDirectory2(serverId, null);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getContentDirectory2(@org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getContentDirectory2WithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getContentDirectory2WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return getContentDirectory2WithHttpInfo(serverId, null);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getContentDirectory2WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getContentDirectory2RequestBuilder(serverId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getContentDirectory2", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getContentDirectory2RequestBuilder(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400, "Missing the required parameter 'serverId' when calling getContentDirectory2");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/ContentDirectory/ContentDirectory".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getContentDirectory3(@org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getContentDirectory3(serverId, null);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getContentDirectory3(@org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getContentDirectory3WithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getContentDirectory3WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return getContentDirectory3WithHttpInfo(serverId, null);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getContentDirectory3WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getContentDirectory3RequestBuilder(serverId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getContentDirectory3", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getContentDirectory3RequestBuilder(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400, "Missing the required parameter 'serverId' when calling getContentDirectory3");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/ContentDirectory/ContentDirectory.xml".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Get Description Xml.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getDescriptionXml(@org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getDescriptionXml(serverId, null);
    }

    /**
     * Get Description Xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getDescriptionXml(@org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getDescriptionXmlWithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get Description Xml.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getDescriptionXmlWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return getDescriptionXmlWithHttpInfo(serverId, null);
    }

    /**
     * Get Description Xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getDescriptionXmlWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDescriptionXmlRequestBuilder(serverId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDescriptionXml", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getDescriptionXmlRequestBuilder(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400, "Missing the required parameter 'serverId' when calling getDescriptionXml");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/description".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Get Description Xml.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getDescriptionXml2(@org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getDescriptionXml2(serverId, null);
    }

    /**
     * Get Description Xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getDescriptionXml2(@org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getDescriptionXml2WithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get Description Xml.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getDescriptionXml2WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return getDescriptionXml2WithHttpInfo(serverId, null);
    }

    /**
     * Get Description Xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getDescriptionXml2WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDescriptionXml2RequestBuilder(serverId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDescriptionXml2", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getDescriptionXml2RequestBuilder(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400, "Missing the required parameter 'serverId' when calling getDescriptionXml2");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/description.xml".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Gets a server icon.
     * 
     * @param fileName The icon filename. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getIcon(@org.eclipse.jdt.annotation.Nullable String fileName) throws ApiException {
        return getIcon(fileName, null);
    }

    /**
     * Gets a server icon.
     * 
     * @param fileName The icon filename. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getIcon(@org.eclipse.jdt.annotation.Nullable String fileName, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getIconWithHttpInfo(fileName, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a server icon.
     * 
     * @param fileName The icon filename. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getIconWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String fileName)
            throws ApiException {
        return getIconWithHttpInfo(fileName, null);
    }

    /**
     * Gets a server icon.
     * 
     * @param fileName The icon filename. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getIconWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String fileName,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getIconRequestBuilder(fileName, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getIcon", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getIconRequestBuilder(@org.eclipse.jdt.annotation.Nullable String fileName,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'fileName' is set
        if (fileName == null) {
            throw new ApiException(400, "Missing the required parameter 'fileName' when calling getIcon");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/icons/{fileName}".replace("{fileName}", ApiClient.urlEncode(fileName.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Gets a server icon.
     * 
     * @param serverId Server UUID. (required)
     * @param fileName The icon filename. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getIconId(@org.eclipse.jdt.annotation.Nullable String serverId,
            @org.eclipse.jdt.annotation.Nullable String fileName) throws ApiException {
        return getIconId(serverId, fileName, null);
    }

    /**
     * Gets a server icon.
     * 
     * @param serverId Server UUID. (required)
     * @param fileName The icon filename. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getIconId(@org.eclipse.jdt.annotation.Nullable String serverId,
            @org.eclipse.jdt.annotation.Nullable String fileName, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getIconIdWithHttpInfo(serverId, fileName, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a server icon.
     * 
     * @param serverId Server UUID. (required)
     * @param fileName The icon filename. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getIconIdWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId,
            @org.eclipse.jdt.annotation.Nullable String fileName) throws ApiException {
        return getIconIdWithHttpInfo(serverId, fileName, null);
    }

    /**
     * Gets a server icon.
     * 
     * @param serverId Server UUID. (required)
     * @param fileName The icon filename. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getIconIdWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId,
            @org.eclipse.jdt.annotation.Nullable String fileName, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getIconIdRequestBuilder(serverId, fileName, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getIconId", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getIconIdRequestBuilder(@org.eclipse.jdt.annotation.Nullable String serverId,
            @org.eclipse.jdt.annotation.Nullable String fileName, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400, "Missing the required parameter 'serverId' when calling getIconId");
        }
        // verify the required parameter 'fileName' is set
        if (fileName == null) {
            throw new ApiException(400, "Missing the required parameter 'fileName' when calling getIconId");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/icons/{fileName}"
                .replace("{serverId}", ApiClient.urlEncode(serverId.toString()))
                .replace("{fileName}", ApiClient.urlEncode(fileName.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMediaReceiverRegistrar(@org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getMediaReceiverRegistrar(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMediaReceiverRegistrar(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getMediaReceiverRegistrarWithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMediaReceiverRegistrarWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return getMediaReceiverRegistrarWithHttpInfo(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMediaReceiverRegistrarWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMediaReceiverRegistrarRequestBuilder(serverId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMediaReceiverRegistrar", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getMediaReceiverRegistrarRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'serverId' when calling getMediaReceiverRegistrar");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/MediaReceiverRegistrar".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMediaReceiverRegistrar2(@org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getMediaReceiverRegistrar2(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMediaReceiverRegistrar2(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getMediaReceiverRegistrar2WithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMediaReceiverRegistrar2WithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getMediaReceiverRegistrar2WithHttpInfo(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMediaReceiverRegistrar2WithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMediaReceiverRegistrar2RequestBuilder(serverId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMediaReceiverRegistrar2", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getMediaReceiverRegistrar2RequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'serverId' when calling getMediaReceiverRegistrar2");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/MediaReceiverRegistrar/MediaReceiverRegistrar".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMediaReceiverRegistrar3(@org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getMediaReceiverRegistrar3(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMediaReceiverRegistrar3(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getMediaReceiverRegistrar3WithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMediaReceiverRegistrar3WithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return getMediaReceiverRegistrar3WithHttpInfo(serverId, null);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMediaReceiverRegistrar3WithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMediaReceiverRegistrar3RequestBuilder(serverId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMediaReceiverRegistrar3", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getMediaReceiverRegistrar3RequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'serverId' when calling getMediaReceiverRegistrar3");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/MediaReceiverRegistrar/MediaReceiverRegistrar.xml".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Process a connection manager control request.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File processConnectionManagerControlRequest(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return processConnectionManagerControlRequest(serverId, null);
    }

    /**
     * Process a connection manager control request.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File processConnectionManagerControlRequest(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = processConnectionManagerControlRequestWithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Process a connection manager control request.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> processConnectionManagerControlRequestWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return processConnectionManagerControlRequestWithHttpInfo(serverId, null);
    }

    /**
     * Process a connection manager control request.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> processConnectionManagerControlRequestWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = processConnectionManagerControlRequestRequestBuilder(serverId,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("processConnectionManagerControlRequest", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder processConnectionManagerControlRequestRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'serverId' when calling processConnectionManagerControlRequest");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/ConnectionManager/Control".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Process a content directory control request.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File processContentDirectoryControlRequest(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return processContentDirectoryControlRequest(serverId, null);
    }

    /**
     * Process a content directory control request.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File processContentDirectoryControlRequest(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = processContentDirectoryControlRequestWithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Process a content directory control request.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> processContentDirectoryControlRequestWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return processContentDirectoryControlRequestWithHttpInfo(serverId, null);
    }

    /**
     * Process a content directory control request.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> processContentDirectoryControlRequestWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = processContentDirectoryControlRequestRequestBuilder(serverId,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("processContentDirectoryControlRequest", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder processContentDirectoryControlRequestRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'serverId' when calling processContentDirectoryControlRequest");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/ContentDirectory/Control".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
     * Process a media receiver registrar control request.
     * 
     * @param serverId Server UUID. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File processMediaReceiverRegistrarControlRequest(@org.eclipse.jdt.annotation.Nullable String serverId)
            throws ApiException {
        return processMediaReceiverRegistrarControlRequest(serverId, null);
    }

    /**
     * Process a media receiver registrar control request.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File processMediaReceiverRegistrarControlRequest(@org.eclipse.jdt.annotation.Nullable String serverId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = processMediaReceiverRegistrarControlRequestWithHttpInfo(serverId, headers);
        return localVarResponse.getData();
    }

    /**
     * Process a media receiver registrar control request.
     * 
     * @param serverId Server UUID. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> processMediaReceiverRegistrarControlRequestWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String serverId) throws ApiException {
        return processMediaReceiverRegistrarControlRequestWithHttpInfo(serverId, null);
    }

    /**
     * Process a media receiver registrar control request.
     * 
     * @param serverId Server UUID. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> processMediaReceiverRegistrarControlRequestWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = processMediaReceiverRegistrarControlRequestRequestBuilder(serverId,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("processMediaReceiverRegistrarControlRequest", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder processMediaReceiverRegistrarControlRequestRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String serverId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'serverId' when calling processMediaReceiverRegistrarControlRequest");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Dlna/{serverId}/MediaReceiverRegistrar/Control".replace("{serverId}",
                ApiClient.urlEncode(serverId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/xml");

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
}
