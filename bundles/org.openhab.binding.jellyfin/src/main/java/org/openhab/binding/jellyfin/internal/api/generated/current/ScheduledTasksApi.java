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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TaskInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TaskTriggerInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ScheduledTasksApi {
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

    public ScheduledTasksApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ScheduledTasksApi(ApiClient apiClient) {
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
     * Get task by id.
     * 
     * @param taskId Task Id. (required)
     * @return TaskInfo
     * @throws ApiException if fails to make API call
     */
    public TaskInfo getTask(@org.eclipse.jdt.annotation.Nullable String taskId) throws ApiException {
        return getTask(taskId, null);
    }

    /**
     * Get task by id.
     * 
     * @param taskId Task Id. (required)
     * @param headers Optional headers to include in the request
     * @return TaskInfo
     * @throws ApiException if fails to make API call
     */
    public TaskInfo getTask(@org.eclipse.jdt.annotation.Nullable String taskId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<TaskInfo> localVarResponse = getTaskWithHttpInfo(taskId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get task by id.
     * 
     * @param taskId Task Id. (required)
     * @return ApiResponse&lt;TaskInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<TaskInfo> getTaskWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String taskId)
            throws ApiException {
        return getTaskWithHttpInfo(taskId, null);
    }

    /**
     * Get task by id.
     * 
     * @param taskId Task Id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;TaskInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<TaskInfo> getTaskWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String taskId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getTaskRequestBuilder(taskId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getTask", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<TaskInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                TaskInfo responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<TaskInfo>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<TaskInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getTaskRequestBuilder(@org.eclipse.jdt.annotation.Nullable String taskId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'taskId' is set
        if (taskId == null) {
            throw new ApiException(400, "Missing the required parameter 'taskId' when calling getTask");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/ScheduledTasks/{taskId}".replace("{taskId}", ApiClient.urlEncode(taskId.toString()));

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
     * Get tasks.
     * 
     * @param isHidden Optional filter tasks that are hidden, or not. (optional)
     * @param isEnabled Optional filter tasks that are enabled, or not. (optional)
     * @return List&lt;TaskInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<TaskInfo> getTasks(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isEnabled) throws ApiException {
        return getTasks(isHidden, isEnabled, null);
    }

    /**
     * Get tasks.
     * 
     * @param isHidden Optional filter tasks that are hidden, or not. (optional)
     * @param isEnabled Optional filter tasks that are enabled, or not. (optional)
     * @param headers Optional headers to include in the request
     * @return List&lt;TaskInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<TaskInfo> getTasks(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isEnabled, Map<String, String> headers) throws ApiException {
        ApiResponse<List<TaskInfo>> localVarResponse = getTasksWithHttpInfo(isHidden, isEnabled, headers);
        return localVarResponse.getData();
    }

    /**
     * Get tasks.
     * 
     * @param isHidden Optional filter tasks that are hidden, or not. (optional)
     * @param isEnabled Optional filter tasks that are enabled, or not. (optional)
     * @return ApiResponse&lt;List&lt;TaskInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<TaskInfo>> getTasksWithHttpInfo(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isEnabled) throws ApiException {
        return getTasksWithHttpInfo(isHidden, isEnabled, null);
    }

    /**
     * Get tasks.
     * 
     * @param isHidden Optional filter tasks that are hidden, or not. (optional)
     * @param isEnabled Optional filter tasks that are enabled, or not. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;TaskInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<TaskInfo>> getTasksWithHttpInfo(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isEnabled, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getTasksRequestBuilder(isHidden, isEnabled, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getTasks", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<TaskInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<TaskInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<TaskInfo>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<TaskInfo>>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getTasksRequestBuilder(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isEnabled, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/ScheduledTasks";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "isHidden";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isHidden", isHidden));
        localVarQueryParameterBaseName = "isEnabled";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isEnabled", isEnabled));

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
     * Start specified task.
     * 
     * @param taskId Task Id. (required)
     * @throws ApiException if fails to make API call
     */
    public void startTask(@org.eclipse.jdt.annotation.Nullable String taskId) throws ApiException {
        startTask(taskId, null);
    }

    /**
     * Start specified task.
     * 
     * @param taskId Task Id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void startTask(@org.eclipse.jdt.annotation.Nullable String taskId, Map<String, String> headers)
            throws ApiException {
        startTaskWithHttpInfo(taskId, headers);
    }

    /**
     * Start specified task.
     * 
     * @param taskId Task Id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> startTaskWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String taskId)
            throws ApiException {
        return startTaskWithHttpInfo(taskId, null);
    }

    /**
     * Start specified task.
     * 
     * @param taskId Task Id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> startTaskWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String taskId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = startTaskRequestBuilder(taskId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("startTask", localVarResponse);
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

    private HttpRequest.Builder startTaskRequestBuilder(@org.eclipse.jdt.annotation.Nullable String taskId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'taskId' is set
        if (taskId == null) {
            throw new ApiException(400, "Missing the required parameter 'taskId' when calling startTask");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/ScheduledTasks/Running/{taskId}".replace("{taskId}",
                ApiClient.urlEncode(taskId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Stop specified task.
     * 
     * @param taskId Task Id. (required)
     * @throws ApiException if fails to make API call
     */
    public void stopTask(@org.eclipse.jdt.annotation.Nullable String taskId) throws ApiException {
        stopTask(taskId, null);
    }

    /**
     * Stop specified task.
     * 
     * @param taskId Task Id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void stopTask(@org.eclipse.jdt.annotation.Nullable String taskId, Map<String, String> headers)
            throws ApiException {
        stopTaskWithHttpInfo(taskId, headers);
    }

    /**
     * Stop specified task.
     * 
     * @param taskId Task Id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> stopTaskWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String taskId)
            throws ApiException {
        return stopTaskWithHttpInfo(taskId, null);
    }

    /**
     * Stop specified task.
     * 
     * @param taskId Task Id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> stopTaskWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String taskId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = stopTaskRequestBuilder(taskId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("stopTask", localVarResponse);
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

    private HttpRequest.Builder stopTaskRequestBuilder(@org.eclipse.jdt.annotation.Nullable String taskId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'taskId' is set
        if (taskId == null) {
            throw new ApiException(400, "Missing the required parameter 'taskId' when calling stopTask");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/ScheduledTasks/Running/{taskId}".replace("{taskId}",
                ApiClient.urlEncode(taskId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Update specified task triggers.
     * 
     * @param taskId Task Id. (required)
     * @param taskTriggerInfo Triggers. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateTask(@org.eclipse.jdt.annotation.Nullable String taskId,
            @org.eclipse.jdt.annotation.Nullable List<TaskTriggerInfo> taskTriggerInfo) throws ApiException {
        updateTask(taskId, taskTriggerInfo, null);
    }

    /**
     * Update specified task triggers.
     * 
     * @param taskId Task Id. (required)
     * @param taskTriggerInfo Triggers. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateTask(@org.eclipse.jdt.annotation.Nullable String taskId,
            @org.eclipse.jdt.annotation.Nullable List<TaskTriggerInfo> taskTriggerInfo, Map<String, String> headers)
            throws ApiException {
        updateTaskWithHttpInfo(taskId, taskTriggerInfo, headers);
    }

    /**
     * Update specified task triggers.
     * 
     * @param taskId Task Id. (required)
     * @param taskTriggerInfo Triggers. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateTaskWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String taskId,
            @org.eclipse.jdt.annotation.Nullable List<TaskTriggerInfo> taskTriggerInfo) throws ApiException {
        return updateTaskWithHttpInfo(taskId, taskTriggerInfo, null);
    }

    /**
     * Update specified task triggers.
     * 
     * @param taskId Task Id. (required)
     * @param taskTriggerInfo Triggers. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateTaskWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String taskId,
            @org.eclipse.jdt.annotation.Nullable List<TaskTriggerInfo> taskTriggerInfo, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateTaskRequestBuilder(taskId, taskTriggerInfo, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateTask", localVarResponse);
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

    private HttpRequest.Builder updateTaskRequestBuilder(@org.eclipse.jdt.annotation.Nullable String taskId,
            @org.eclipse.jdt.annotation.Nullable List<TaskTriggerInfo> taskTriggerInfo, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'taskId' is set
        if (taskId == null) {
            throw new ApiException(400, "Missing the required parameter 'taskId' when calling updateTask");
        }
        // verify the required parameter 'taskTriggerInfo' is set
        if (taskTriggerInfo == null) {
            throw new ApiException(400, "Missing the required parameter 'taskTriggerInfo' when calling updateTask");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/ScheduledTasks/{taskId}/Triggers".replace("{taskId}",
                ApiClient.urlEncode(taskId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(taskTriggerInfo);
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
