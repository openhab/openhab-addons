package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.EndPointInfo;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.LogFile;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.PublicSystemInfo;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.SystemInfo;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.WakeOnLanInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class SystemApi {
    private ApiClient apiClient;

    public SystemApi() {
        this(new ApiClient());
    }

    @Autowired
    public SystemApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets information about the request endpoint.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return EndPointInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getEndpointInfoRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<EndPointInfo> localVarReturnType = new ParameterizedTypeReference<EndPointInfo>() {
        };
        return apiClient.invokeAPI("/System/Endpoint", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets information about the request endpoint.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return EndPointInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<EndPointInfo> getEndpointInfo() throws WebClientResponseException {
        ParameterizedTypeReference<EndPointInfo> localVarReturnType = new ParameterizedTypeReference<EndPointInfo>() {
        };
        return getEndpointInfoRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets information about the request endpoint.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;EndPointInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<EndPointInfo>> getEndpointInfoWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<EndPointInfo> localVarReturnType = new ParameterizedTypeReference<EndPointInfo>() {
        };
        return getEndpointInfoRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets information about the request endpoint.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getEndpointInfoWithResponseSpec() throws WebClientResponseException {
        return getEndpointInfoRequestCreation();
    }

    /**
     * Gets a log file.
     * 
     * <p>
     * <b>200</b> - Log file retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the log file to get.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLogFileRequestCreation(String name) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getLogFile",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "name", name));

        final String[] localVarAccepts = { "text/plain" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/System/Logs/Log", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a log file.
     * 
     * <p>
     * <b>200</b> - Log file retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the log file to get.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getLogFile(String name) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getLogFileRequestCreation(name).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a log file.
     * 
     * <p>
     * <b>200</b> - Log file retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the log file to get.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getLogFileWithHttpInfo(String name) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getLogFileRequestCreation(name).toEntity(localVarReturnType);
    }

    /**
     * Gets a log file.
     * 
     * <p>
     * <b>200</b> - Log file retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the log file to get.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLogFileWithResponseSpec(String name) throws WebClientResponseException {
        return getLogFileRequestCreation(name);
    }

    /**
     * Pings the system.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return String
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPingSystemRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {
        };
        return apiClient.invokeAPI("/System/Ping", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Pings the system.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return String
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<String> getPingSystem() throws WebClientResponseException {
        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {
        };
        return getPingSystemRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Pings the system.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return ResponseEntity&lt;String&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<String>> getPingSystemWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {
        };
        return getPingSystemRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Pings the system.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPingSystemWithResponseSpec() throws WebClientResponseException {
        return getPingSystemRequestCreation();
    }

    /**
     * Gets public information about the server.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return PublicSystemInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPublicSystemInfoRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        ParameterizedTypeReference<PublicSystemInfo> localVarReturnType = new ParameterizedTypeReference<PublicSystemInfo>() {
        };
        return apiClient.invokeAPI("/System/Info/Public", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets public information about the server.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return PublicSystemInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<PublicSystemInfo> getPublicSystemInfo() throws WebClientResponseException {
        ParameterizedTypeReference<PublicSystemInfo> localVarReturnType = new ParameterizedTypeReference<PublicSystemInfo>() {
        };
        return getPublicSystemInfoRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets public information about the server.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return ResponseEntity&lt;PublicSystemInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<PublicSystemInfo>> getPublicSystemInfoWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<PublicSystemInfo> localVarReturnType = new ParameterizedTypeReference<PublicSystemInfo>() {
        };
        return getPublicSystemInfoRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets public information about the server.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPublicSystemInfoWithResponseSpec() throws WebClientResponseException {
        return getPublicSystemInfoRequestCreation();
    }

    /**
     * Gets a list of available server log files.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;LogFile&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getServerLogsRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<LogFile> localVarReturnType = new ParameterizedTypeReference<LogFile>() {
        };
        return apiClient.invokeAPI("/System/Logs", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a list of available server log files.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;LogFile&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<LogFile> getServerLogs() throws WebClientResponseException {
        ParameterizedTypeReference<LogFile> localVarReturnType = new ParameterizedTypeReference<LogFile>() {
        };
        return getServerLogsRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets a list of available server log files.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;LogFile&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<LogFile>>> getServerLogsWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<LogFile> localVarReturnType = new ParameterizedTypeReference<LogFile>() {
        };
        return getServerLogsRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets a list of available server log files.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getServerLogsWithResponseSpec() throws WebClientResponseException {
        return getServerLogsRequestCreation();
    }

    /**
     * Gets information about the server.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return SystemInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSystemInfoRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<SystemInfo> localVarReturnType = new ParameterizedTypeReference<SystemInfo>() {
        };
        return apiClient.invokeAPI("/System/Info", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets information about the server.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return SystemInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<SystemInfo> getSystemInfo() throws WebClientResponseException {
        ParameterizedTypeReference<SystemInfo> localVarReturnType = new ParameterizedTypeReference<SystemInfo>() {
        };
        return getSystemInfoRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets information about the server.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;SystemInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<SystemInfo>> getSystemInfoWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<SystemInfo> localVarReturnType = new ParameterizedTypeReference<SystemInfo>() {
        };
        return getSystemInfoRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets information about the server.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSystemInfoWithResponseSpec() throws WebClientResponseException {
        return getSystemInfoRequestCreation();
    }

    /**
     * Gets wake on lan information.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;WakeOnLanInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     * @deprecated
     */
    @Deprecated
    private ResponseSpec getWakeOnLanInfoRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<WakeOnLanInfo> localVarReturnType = new ParameterizedTypeReference<WakeOnLanInfo>() {
        };
        return apiClient.invokeAPI("/System/WakeOnLanInfo", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets wake on lan information.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;WakeOnLanInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<WakeOnLanInfo> getWakeOnLanInfo() throws WebClientResponseException {
        ParameterizedTypeReference<WakeOnLanInfo> localVarReturnType = new ParameterizedTypeReference<WakeOnLanInfo>() {
        };
        return getWakeOnLanInfoRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets wake on lan information.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;WakeOnLanInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<WakeOnLanInfo>>> getWakeOnLanInfoWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<WakeOnLanInfo> localVarReturnType = new ParameterizedTypeReference<WakeOnLanInfo>() {
        };
        return getWakeOnLanInfoRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets wake on lan information.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getWakeOnLanInfoWithResponseSpec() throws WebClientResponseException {
        return getWakeOnLanInfoRequestCreation();
    }

    /**
     * Pings the system.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return String
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec postPingSystemRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {
        };
        return apiClient.invokeAPI("/System/Ping", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Pings the system.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return String
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<String> postPingSystem() throws WebClientResponseException {
        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {
        };
        return postPingSystemRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Pings the system.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return ResponseEntity&lt;String&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<String>> postPingSystemWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {
        };
        return postPingSystemRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Pings the system.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec postPingSystemWithResponseSpec() throws WebClientResponseException {
        return postPingSystemRequestCreation();
    }

    /**
     * Restarts the application.
     * 
     * <p>
     * <b>204</b> - Server restarted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec restartApplicationRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/System/Restart", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Restarts the application.
     * 
     * <p>
     * <b>204</b> - Server restarted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> restartApplication() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return restartApplicationRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Restarts the application.
     * 
     * <p>
     * <b>204</b> - Server restarted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> restartApplicationWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return restartApplicationRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Restarts the application.
     * 
     * <p>
     * <b>204</b> - Server restarted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec restartApplicationWithResponseSpec() throws WebClientResponseException {
        return restartApplicationRequestCreation();
    }

    /**
     * Shuts down the application.
     * 
     * <p>
     * <b>204</b> - Server shut down.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec shutdownApplicationRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/System/Shutdown", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Shuts down the application.
     * 
     * <p>
     * <b>204</b> - Server shut down.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> shutdownApplication() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return shutdownApplicationRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Shuts down the application.
     * 
     * <p>
     * <b>204</b> - Server shut down.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> shutdownApplicationWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return shutdownApplicationRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Shuts down the application.
     * 
     * <p>
     * <b>204</b> - Server shut down.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec shutdownApplicationWithResponseSpec() throws WebClientResponseException {
        return shutdownApplicationRequestCreation();
    }
}
