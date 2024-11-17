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
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PluginInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PluginsApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public PluginsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public PluginsApi(ApiClient apiClient) {
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
     * Disable a plugin.
     * 
     * @param pluginId Plugin id. (required)
     * @param version Plugin version. (required)
     * @throws ApiException if fails to make API call
     */
    public void disablePlugin(@org.eclipse.jdt.annotation.Nullable UUID pluginId,
            @org.eclipse.jdt.annotation.Nullable String version) throws ApiException {
        disablePluginWithHttpInfo(pluginId, version);
    }

    /**
     * Disable a plugin.
     * 
     * @param pluginId Plugin id. (required)
     * @param version Plugin version. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> disablePluginWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID pluginId,
            @org.eclipse.jdt.annotation.Nullable String version) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = disablePluginRequestBuilder(pluginId, version);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("disablePlugin", localVarResponse);
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

    private HttpRequest.Builder disablePluginRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID pluginId,
            @org.eclipse.jdt.annotation.Nullable String version) throws ApiException {
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new ApiException(400, "Missing the required parameter 'pluginId' when calling disablePlugin");
        }
        // verify the required parameter 'version' is set
        if (version == null) {
            throw new ApiException(400, "Missing the required parameter 'version' when calling disablePlugin");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Plugins/{pluginId}/{version}/Disable"
                .replace("{pluginId}", ApiClient.urlEncode(pluginId.toString()))
                .replace("{version}", ApiClient.urlEncode(version.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Enables a disabled plugin.
     * 
     * @param pluginId Plugin id. (required)
     * @param version Plugin version. (required)
     * @throws ApiException if fails to make API call
     */
    public void enablePlugin(@org.eclipse.jdt.annotation.Nullable UUID pluginId,
            @org.eclipse.jdt.annotation.Nullable String version) throws ApiException {
        enablePluginWithHttpInfo(pluginId, version);
    }

    /**
     * Enables a disabled plugin.
     * 
     * @param pluginId Plugin id. (required)
     * @param version Plugin version. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> enablePluginWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID pluginId,
            @org.eclipse.jdt.annotation.Nullable String version) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = enablePluginRequestBuilder(pluginId, version);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("enablePlugin", localVarResponse);
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

    private HttpRequest.Builder enablePluginRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID pluginId,
            @org.eclipse.jdt.annotation.Nullable String version) throws ApiException {
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new ApiException(400, "Missing the required parameter 'pluginId' when calling enablePlugin");
        }
        // verify the required parameter 'version' is set
        if (version == null) {
            throw new ApiException(400, "Missing the required parameter 'version' when calling enablePlugin");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Plugins/{pluginId}/{version}/Enable"
                .replace("{pluginId}", ApiClient.urlEncode(pluginId.toString()))
                .replace("{version}", ApiClient.urlEncode(version.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Gets plugin configuration.
     * 
     * @param pluginId Plugin id. (required)
     * @return Object
     * @throws ApiException if fails to make API call
     */
    public Object getPluginConfiguration(@org.eclipse.jdt.annotation.Nullable UUID pluginId) throws ApiException {
        ApiResponse<Object> localVarResponse = getPluginConfigurationWithHttpInfo(pluginId);
        return localVarResponse.getData();
    }

    /**
     * Gets plugin configuration.
     * 
     * @param pluginId Plugin id. (required)
     * @return ApiResponse&lt;Object&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Object> getPluginConfigurationWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID pluginId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPluginConfigurationRequestBuilder(pluginId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPluginConfiguration", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<Object>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<Object>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<Object>() {
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

    private HttpRequest.Builder getPluginConfigurationRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID pluginId)
            throws ApiException {
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'pluginId' when calling getPluginConfiguration");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Plugins/{pluginId}/Configuration".replace("{pluginId}",
                ApiClient.urlEncode(pluginId.toString()));

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
     * Gets a plugin&#39;s image.
     * 
     * @param pluginId Plugin id. (required)
     * @param version Plugin version. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getPluginImage(@org.eclipse.jdt.annotation.Nullable UUID pluginId,
            @org.eclipse.jdt.annotation.Nullable String version) throws ApiException {
        ApiResponse<File> localVarResponse = getPluginImageWithHttpInfo(pluginId, version);
        return localVarResponse.getData();
    }

    /**
     * Gets a plugin&#39;s image.
     * 
     * @param pluginId Plugin id. (required)
     * @param version Plugin version. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getPluginImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID pluginId,
            @org.eclipse.jdt.annotation.Nullable String version) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPluginImageRequestBuilder(pluginId, version);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPluginImage", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
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

    private HttpRequest.Builder getPluginImageRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID pluginId,
            @org.eclipse.jdt.annotation.Nullable String version) throws ApiException {
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new ApiException(400, "Missing the required parameter 'pluginId' when calling getPluginImage");
        }
        // verify the required parameter 'version' is set
        if (version == null) {
            throw new ApiException(400, "Missing the required parameter 'version' when calling getPluginImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Plugins/{pluginId}/{version}/Image"
                .replace("{pluginId}", ApiClient.urlEncode(pluginId.toString()))
                .replace("{version}", ApiClient.urlEncode(version.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Gets a plugin&#39;s manifest.
     * 
     * @param pluginId Plugin id. (required)
     * @throws ApiException if fails to make API call
     */
    public void getPluginManifest(@org.eclipse.jdt.annotation.Nullable UUID pluginId) throws ApiException {
        getPluginManifestWithHttpInfo(pluginId);
    }

    /**
     * Gets a plugin&#39;s manifest.
     * 
     * @param pluginId Plugin id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> getPluginManifestWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID pluginId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPluginManifestRequestBuilder(pluginId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPluginManifest", localVarResponse);
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

    private HttpRequest.Builder getPluginManifestRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID pluginId)
            throws ApiException {
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new ApiException(400, "Missing the required parameter 'pluginId' when calling getPluginManifest");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Plugins/{pluginId}/Manifest".replace("{pluginId}",
                ApiClient.urlEncode(pluginId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Gets a list of currently installed plugins.
     * 
     * @return List&lt;PluginInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<PluginInfo> getPlugins() throws ApiException {
        ApiResponse<List<PluginInfo>> localVarResponse = getPluginsWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets a list of currently installed plugins.
     * 
     * @return ApiResponse&lt;List&lt;PluginInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<PluginInfo>> getPluginsWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPluginsRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPlugins", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<PluginInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<PluginInfo>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<PluginInfo>>() {
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

    private HttpRequest.Builder getPluginsRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Plugins";

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
     * Uninstalls a plugin.
     * 
     * @param pluginId Plugin id. (required)
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public void uninstallPlugin(@org.eclipse.jdt.annotation.Nullable UUID pluginId) throws ApiException {
        uninstallPluginWithHttpInfo(pluginId);
    }

    /**
     * Uninstalls a plugin.
     * 
     * @param pluginId Plugin id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> uninstallPluginWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID pluginId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = uninstallPluginRequestBuilder(pluginId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("uninstallPlugin", localVarResponse);
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

    private HttpRequest.Builder uninstallPluginRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID pluginId)
            throws ApiException {
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new ApiException(400, "Missing the required parameter 'pluginId' when calling uninstallPlugin");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Plugins/{pluginId}".replace("{pluginId}", ApiClient.urlEncode(pluginId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Uninstalls a plugin by version.
     * 
     * @param pluginId Plugin id. (required)
     * @param version Plugin version. (required)
     * @throws ApiException if fails to make API call
     */
    public void uninstallPluginByVersion(@org.eclipse.jdt.annotation.Nullable UUID pluginId,
            @org.eclipse.jdt.annotation.Nullable String version) throws ApiException {
        uninstallPluginByVersionWithHttpInfo(pluginId, version);
    }

    /**
     * Uninstalls a plugin by version.
     * 
     * @param pluginId Plugin id. (required)
     * @param version Plugin version. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> uninstallPluginByVersionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID pluginId,
            @org.eclipse.jdt.annotation.Nullable String version) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = uninstallPluginByVersionRequestBuilder(pluginId, version);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("uninstallPluginByVersion", localVarResponse);
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

    private HttpRequest.Builder uninstallPluginByVersionRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UUID pluginId, @org.eclipse.jdt.annotation.Nullable String version)
            throws ApiException {
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'pluginId' when calling uninstallPluginByVersion");
        }
        // verify the required parameter 'version' is set
        if (version == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'version' when calling uninstallPluginByVersion");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Plugins/{pluginId}/{version}"
                .replace("{pluginId}", ApiClient.urlEncode(pluginId.toString()))
                .replace("{version}", ApiClient.urlEncode(version.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Updates plugin configuration.
     * Accepts plugin configuration as JSON body.
     * 
     * @param pluginId Plugin id. (required)
     * @throws ApiException if fails to make API call
     */
    public void updatePluginConfiguration(@org.eclipse.jdt.annotation.Nullable UUID pluginId) throws ApiException {
        updatePluginConfigurationWithHttpInfo(pluginId);
    }

    /**
     * Updates plugin configuration.
     * Accepts plugin configuration as JSON body.
     * 
     * @param pluginId Plugin id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updatePluginConfigurationWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID pluginId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updatePluginConfigurationRequestBuilder(pluginId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updatePluginConfiguration", localVarResponse);
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

    private HttpRequest.Builder updatePluginConfigurationRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UUID pluginId) throws ApiException {
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'pluginId' when calling updatePluginConfiguration");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Plugins/{pluginId}/Configuration".replace("{pluginId}",
                ApiClient.urlEncode(pluginId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
}
