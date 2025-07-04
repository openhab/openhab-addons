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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TaskInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TaskTriggerInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ScheduledTasksApi {
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
     * Get task by id.
     * 
     * @param taskId Task Id. (required)
     * @return TaskInfo
     * @throws ApiException if fails to make API call
     */
    public TaskInfo getTask(@org.eclipse.jdt.annotation.Nullable String taskId) throws ApiException {
        ApiResponse<TaskInfo> localVarResponse = getTaskWithHttpInfo(taskId);
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
        HttpRequest.Builder localVarRequestBuilder = getTaskRequestBuilder(taskId);
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
                localVarResponse.body().close();

                return new ApiResponse<TaskInfo>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<TaskInfo>() {
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

    private HttpRequest.Builder getTaskRequestBuilder(@org.eclipse.jdt.annotation.Nullable String taskId)
            throws ApiException {
        // verify the required parameter 'taskId' is set
        if (taskId == null) {
            throw new ApiException(400, "Missing the required parameter 'taskId' when calling getTask");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/ScheduledTasks/{taskId}".replace("{taskId}", ApiClient.urlEncode(taskId.toString()));

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
     * Get tasks.
     * 
     * @param isHidden Optional filter tasks that are hidden, or not. (optional)
     * @param isEnabled Optional filter tasks that are enabled, or not. (optional)
     * @return List&lt;TaskInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<TaskInfo> getTasks(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isEnabled) throws ApiException {
        ApiResponse<List<TaskInfo>> localVarResponse = getTasksWithHttpInfo(isHidden, isEnabled);
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
        HttpRequest.Builder localVarRequestBuilder = getTasksRequestBuilder(isHidden, isEnabled);
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
                localVarResponse.body().close();

                return new ApiResponse<List<TaskInfo>>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<TaskInfo>>() {
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

    private HttpRequest.Builder getTasksRequestBuilder(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isEnabled) throws ApiException {

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
     * Start specified task.
     * 
     * @param taskId Task Id. (required)
     * @throws ApiException if fails to make API call
     */
    public void startTask(@org.eclipse.jdt.annotation.Nullable String taskId) throws ApiException {
        startTaskWithHttpInfo(taskId);
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
        HttpRequest.Builder localVarRequestBuilder = startTaskRequestBuilder(taskId);
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

    private HttpRequest.Builder startTaskRequestBuilder(@org.eclipse.jdt.annotation.Nullable String taskId)
            throws ApiException {
        // verify the required parameter 'taskId' is set
        if (taskId == null) {
            throw new ApiException(400, "Missing the required parameter 'taskId' when calling startTask");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/ScheduledTasks/Running/{taskId}".replace("{taskId}",
                ApiClient.urlEncode(taskId.toString()));

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
     * Stop specified task.
     * 
     * @param taskId Task Id. (required)
     * @throws ApiException if fails to make API call
     */
    public void stopTask(@org.eclipse.jdt.annotation.Nullable String taskId) throws ApiException {
        stopTaskWithHttpInfo(taskId);
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
        HttpRequest.Builder localVarRequestBuilder = stopTaskRequestBuilder(taskId);
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

    private HttpRequest.Builder stopTaskRequestBuilder(@org.eclipse.jdt.annotation.Nullable String taskId)
            throws ApiException {
        // verify the required parameter 'taskId' is set
        if (taskId == null) {
            throw new ApiException(400, "Missing the required parameter 'taskId' when calling stopTask");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/ScheduledTasks/Running/{taskId}".replace("{taskId}",
                ApiClient.urlEncode(taskId.toString()));

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
     * Update specified task triggers.
     * 
     * @param taskId Task Id. (required)
     * @param taskTriggerInfo Triggers. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateTask(@org.eclipse.jdt.annotation.Nullable String taskId,
            @org.eclipse.jdt.annotation.Nullable List<TaskTriggerInfo> taskTriggerInfo) throws ApiException {
        updateTaskWithHttpInfo(taskId, taskTriggerInfo);
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
        HttpRequest.Builder localVarRequestBuilder = updateTaskRequestBuilder(taskId, taskTriggerInfo);
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
            @org.eclipse.jdt.annotation.Nullable List<TaskTriggerInfo> taskTriggerInfo) throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(taskTriggerInfo);
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
