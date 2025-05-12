package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.ProblemDetails;
import org.openhab.binding.jellyfin.internal.api.version.current.model.TaskInfo;
import org.openhab.binding.jellyfin.internal.api.version.current.model.TaskTriggerInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class ScheduledTasksApi {
    private ApiClient apiClient;

    public ScheduledTasksApi() {
        this(new ApiClient());
    }

    @Autowired
    public ScheduledTasksApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Get task by id.
     * 
     * <p><b>200</b> - Task retrieved.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @return TaskInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTaskRequestCreation(String taskId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'taskId' is set
        if (taskId == null) {
            throw new WebClientResponseException("Missing the required parameter 'taskId' when calling getTask", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("taskId", taskId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<TaskInfo> localVarReturnType = new ParameterizedTypeReference<TaskInfo>() {};
        return apiClient.invokeAPI("/ScheduledTasks/{taskId}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get task by id.
     * 
     * <p><b>200</b> - Task retrieved.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @return TaskInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TaskInfo> getTask(String taskId) throws WebClientResponseException {
        ParameterizedTypeReference<TaskInfo> localVarReturnType = new ParameterizedTypeReference<TaskInfo>() {};
        return getTaskRequestCreation(taskId).bodyToMono(localVarReturnType);
    }

    /**
     * Get task by id.
     * 
     * <p><b>200</b> - Task retrieved.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @return ResponseEntity&lt;TaskInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TaskInfo>> getTaskWithHttpInfo(String taskId) throws WebClientResponseException {
        ParameterizedTypeReference<TaskInfo> localVarReturnType = new ParameterizedTypeReference<TaskInfo>() {};
        return getTaskRequestCreation(taskId).toEntity(localVarReturnType);
    }

    /**
     * Get task by id.
     * 
     * <p><b>200</b> - Task retrieved.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTaskWithResponseSpec(String taskId) throws WebClientResponseException {
        return getTaskRequestCreation(taskId);
    }

    /**
     * Get tasks.
     * 
     * <p><b>200</b> - Scheduled tasks retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional filter tasks that are hidden, or not.
     * @param isEnabled Optional filter tasks that are enabled, or not.
     * @return List&lt;TaskInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTasksRequestCreation(Boolean isHidden, Boolean isEnabled) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isHidden", isHidden));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isEnabled", isEnabled));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<TaskInfo> localVarReturnType = new ParameterizedTypeReference<TaskInfo>() {};
        return apiClient.invokeAPI("/ScheduledTasks", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get tasks.
     * 
     * <p><b>200</b> - Scheduled tasks retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional filter tasks that are hidden, or not.
     * @param isEnabled Optional filter tasks that are enabled, or not.
     * @return List&lt;TaskInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<TaskInfo> getTasks(Boolean isHidden, Boolean isEnabled) throws WebClientResponseException {
        ParameterizedTypeReference<TaskInfo> localVarReturnType = new ParameterizedTypeReference<TaskInfo>() {};
        return getTasksRequestCreation(isHidden, isEnabled).bodyToFlux(localVarReturnType);
    }

    /**
     * Get tasks.
     * 
     * <p><b>200</b> - Scheduled tasks retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional filter tasks that are hidden, or not.
     * @param isEnabled Optional filter tasks that are enabled, or not.
     * @return ResponseEntity&lt;List&lt;TaskInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<TaskInfo>>> getTasksWithHttpInfo(Boolean isHidden, Boolean isEnabled) throws WebClientResponseException {
        ParameterizedTypeReference<TaskInfo> localVarReturnType = new ParameterizedTypeReference<TaskInfo>() {};
        return getTasksRequestCreation(isHidden, isEnabled).toEntityList(localVarReturnType);
    }

    /**
     * Get tasks.
     * 
     * <p><b>200</b> - Scheduled tasks retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional filter tasks that are hidden, or not.
     * @param isEnabled Optional filter tasks that are enabled, or not.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTasksWithResponseSpec(Boolean isHidden, Boolean isEnabled) throws WebClientResponseException {
        return getTasksRequestCreation(isHidden, isEnabled);
    }

    /**
     * Start specified task.
     * 
     * <p><b>204</b> - Task started.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec startTaskRequestCreation(String taskId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'taskId' is set
        if (taskId == null) {
            throw new WebClientResponseException("Missing the required parameter 'taskId' when calling startTask", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("taskId", taskId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/ScheduledTasks/Running/{taskId}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Start specified task.
     * 
     * <p><b>204</b> - Task started.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> startTask(String taskId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return startTaskRequestCreation(taskId).bodyToMono(localVarReturnType);
    }

    /**
     * Start specified task.
     * 
     * <p><b>204</b> - Task started.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> startTaskWithHttpInfo(String taskId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return startTaskRequestCreation(taskId).toEntity(localVarReturnType);
    }

    /**
     * Start specified task.
     * 
     * <p><b>204</b> - Task started.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec startTaskWithResponseSpec(String taskId) throws WebClientResponseException {
        return startTaskRequestCreation(taskId);
    }

    /**
     * Stop specified task.
     * 
     * <p><b>204</b> - Task stopped.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec stopTaskRequestCreation(String taskId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'taskId' is set
        if (taskId == null) {
            throw new WebClientResponseException("Missing the required parameter 'taskId' when calling stopTask", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("taskId", taskId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/ScheduledTasks/Running/{taskId}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Stop specified task.
     * 
     * <p><b>204</b> - Task stopped.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> stopTask(String taskId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return stopTaskRequestCreation(taskId).bodyToMono(localVarReturnType);
    }

    /**
     * Stop specified task.
     * 
     * <p><b>204</b> - Task stopped.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> stopTaskWithHttpInfo(String taskId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return stopTaskRequestCreation(taskId).toEntity(localVarReturnType);
    }

    /**
     * Stop specified task.
     * 
     * <p><b>204</b> - Task stopped.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec stopTaskWithResponseSpec(String taskId) throws WebClientResponseException {
        return stopTaskRequestCreation(taskId);
    }

    /**
     * Update specified task triggers.
     * 
     * <p><b>204</b> - Task triggers updated.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @param taskTriggerInfo Triggers.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateTaskRequestCreation(String taskId, List<TaskTriggerInfo> taskTriggerInfo) throws WebClientResponseException {
        Object postBody = taskTriggerInfo;
        // verify the required parameter 'taskId' is set
        if (taskId == null) {
            throw new WebClientResponseException("Missing the required parameter 'taskId' when calling updateTask", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'taskTriggerInfo' is set
        if (taskTriggerInfo == null) {
            throw new WebClientResponseException("Missing the required parameter 'taskTriggerInfo' when calling updateTask", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("taskId", taskId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/ScheduledTasks/{taskId}/Triggers", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Update specified task triggers.
     * 
     * <p><b>204</b> - Task triggers updated.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @param taskTriggerInfo Triggers.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateTask(String taskId, List<TaskTriggerInfo> taskTriggerInfo) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateTaskRequestCreation(taskId, taskTriggerInfo).bodyToMono(localVarReturnType);
    }

    /**
     * Update specified task triggers.
     * 
     * <p><b>204</b> - Task triggers updated.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @param taskTriggerInfo Triggers.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateTaskWithHttpInfo(String taskId, List<TaskTriggerInfo> taskTriggerInfo) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateTaskRequestCreation(taskId, taskTriggerInfo).toEntity(localVarReturnType);
    }

    /**
     * Update specified task triggers.
     * 
     * <p><b>204</b> - Task triggers updated.
     * <p><b>404</b> - Task not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param taskId Task Id.
     * @param taskTriggerInfo Triggers.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateTaskWithResponseSpec(String taskId, List<TaskTriggerInfo> taskTriggerInfo) throws WebClientResponseException {
        return updateTaskRequestCreation(taskId, taskTriggerInfo);
    }
}
