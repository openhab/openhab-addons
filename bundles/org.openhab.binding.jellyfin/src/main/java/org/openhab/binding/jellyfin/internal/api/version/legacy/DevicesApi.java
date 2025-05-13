package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.DeviceInfo;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.DeviceInfoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.DeviceOptions;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.DeviceOptionsDto;
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

import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class DevicesApi {
    private ApiClient apiClient;

    public DevicesApi() {
        this(new ApiClient());
    }

    @Autowired
    public DevicesApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Deletes a device.
     * 
     * <p>
     * <b>204</b> - Device deleted.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteDeviceRequestCreation(String id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling deleteDevice",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Devices", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Deletes a device.
     * 
     * <p>
     * <b>204</b> - Device deleted.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteDevice(String id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteDeviceRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes a device.
     * 
     * <p>
     * <b>204</b> - Device deleted.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteDeviceWithHttpInfo(String id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteDeviceRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Deletes a device.
     * 
     * <p>
     * <b>204</b> - Device deleted.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteDeviceWithResponseSpec(String id) throws WebClientResponseException {
        return deleteDeviceRequestCreation(id);
    }

    /**
     * Get info for a device.
     * 
     * <p>
     * <b>200</b> - Device info retrieved.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @return DeviceInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDeviceInfoRequestCreation(String id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getDeviceInfo",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<DeviceInfo> localVarReturnType = new ParameterizedTypeReference<DeviceInfo>() {
        };
        return apiClient.invokeAPI("/Devices/Info", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get info for a device.
     * 
     * <p>
     * <b>200</b> - Device info retrieved.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @return DeviceInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<DeviceInfo> getDeviceInfo(String id) throws WebClientResponseException {
        ParameterizedTypeReference<DeviceInfo> localVarReturnType = new ParameterizedTypeReference<DeviceInfo>() {
        };
        return getDeviceInfoRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Get info for a device.
     * 
     * <p>
     * <b>200</b> - Device info retrieved.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @return ResponseEntity&lt;DeviceInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<DeviceInfo>> getDeviceInfoWithHttpInfo(String id) throws WebClientResponseException {
        ParameterizedTypeReference<DeviceInfo> localVarReturnType = new ParameterizedTypeReference<DeviceInfo>() {
        };
        return getDeviceInfoRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Get info for a device.
     * 
     * <p>
     * <b>200</b> - Device info retrieved.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDeviceInfoWithResponseSpec(String id) throws WebClientResponseException {
        return getDeviceInfoRequestCreation(id);
    }

    /**
     * Get options for a device.
     * 
     * <p>
     * <b>200</b> - Device options retrieved.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @return DeviceOptions
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDeviceOptionsRequestCreation(String id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getDeviceOptions",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<DeviceOptions> localVarReturnType = new ParameterizedTypeReference<DeviceOptions>() {
        };
        return apiClient.invokeAPI("/Devices/Options", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get options for a device.
     * 
     * <p>
     * <b>200</b> - Device options retrieved.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @return DeviceOptions
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<DeviceOptions> getDeviceOptions(String id) throws WebClientResponseException {
        ParameterizedTypeReference<DeviceOptions> localVarReturnType = new ParameterizedTypeReference<DeviceOptions>() {
        };
        return getDeviceOptionsRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Get options for a device.
     * 
     * <p>
     * <b>200</b> - Device options retrieved.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @return ResponseEntity&lt;DeviceOptions&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<DeviceOptions>> getDeviceOptionsWithHttpInfo(String id)
            throws WebClientResponseException {
        ParameterizedTypeReference<DeviceOptions> localVarReturnType = new ParameterizedTypeReference<DeviceOptions>() {
        };
        return getDeviceOptionsRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Get options for a device.
     * 
     * <p>
     * <b>200</b> - Device options retrieved.
     * <p>
     * <b>404</b> - Device not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDeviceOptionsWithResponseSpec(String id) throws WebClientResponseException {
        return getDeviceOptionsRequestCreation(id);
    }

    /**
     * Get Devices.
     * 
     * <p>
     * <b>200</b> - Devices retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param supportsSync Gets or sets a value indicating whether [supports synchronize].
     * @param userId Gets or sets the user identifier.
     * @return DeviceInfoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDevicesRequestCreation(Boolean supportsSync, UUID userId)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "supportsSync", supportsSync));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<DeviceInfoQueryResult> localVarReturnType = new ParameterizedTypeReference<DeviceInfoQueryResult>() {
        };
        return apiClient.invokeAPI("/Devices", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get Devices.
     * 
     * <p>
     * <b>200</b> - Devices retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param supportsSync Gets or sets a value indicating whether [supports synchronize].
     * @param userId Gets or sets the user identifier.
     * @return DeviceInfoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<DeviceInfoQueryResult> getDevices(Boolean supportsSync, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<DeviceInfoQueryResult> localVarReturnType = new ParameterizedTypeReference<DeviceInfoQueryResult>() {
        };
        return getDevicesRequestCreation(supportsSync, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Get Devices.
     * 
     * <p>
     * <b>200</b> - Devices retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param supportsSync Gets or sets a value indicating whether [supports synchronize].
     * @param userId Gets or sets the user identifier.
     * @return ResponseEntity&lt;DeviceInfoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<DeviceInfoQueryResult>> getDevicesWithHttpInfo(Boolean supportsSync, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<DeviceInfoQueryResult> localVarReturnType = new ParameterizedTypeReference<DeviceInfoQueryResult>() {
        };
        return getDevicesRequestCreation(supportsSync, userId).toEntity(localVarReturnType);
    }

    /**
     * Get Devices.
     * 
     * <p>
     * <b>200</b> - Devices retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param supportsSync Gets or sets a value indicating whether [supports synchronize].
     * @param userId Gets or sets the user identifier.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDevicesWithResponseSpec(Boolean supportsSync, UUID userId)
            throws WebClientResponseException {
        return getDevicesRequestCreation(supportsSync, userId);
    }

    /**
     * Update device options.
     * 
     * <p>
     * <b>204</b> - Device options updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @param deviceOptionsDto Device Options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateDeviceOptionsRequestCreation(String id, DeviceOptionsDto deviceOptionsDto)
            throws WebClientResponseException {
        Object postBody = deviceOptionsDto;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling updateDeviceOptions",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'deviceOptionsDto' is set
        if (deviceOptionsDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'deviceOptionsDto' when calling updateDeviceOptions",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Devices/Options", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Update device options.
     * 
     * <p>
     * <b>204</b> - Device options updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @param deviceOptionsDto Device Options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateDeviceOptions(String id, DeviceOptionsDto deviceOptionsDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateDeviceOptionsRequestCreation(id, deviceOptionsDto).bodyToMono(localVarReturnType);
    }

    /**
     * Update device options.
     * 
     * <p>
     * <b>204</b> - Device options updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @param deviceOptionsDto Device Options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateDeviceOptionsWithHttpInfo(String id, DeviceOptionsDto deviceOptionsDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateDeviceOptionsRequestCreation(id, deviceOptionsDto).toEntity(localVarReturnType);
    }

    /**
     * Update device options.
     * 
     * <p>
     * <b>204</b> - Device options updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Device Id.
     * @param deviceOptionsDto Device Options.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateDeviceOptionsWithResponseSpec(String id, DeviceOptionsDto deviceOptionsDto)
            throws WebClientResponseException {
        return updateDeviceOptionsRequestCreation(id, deviceOptionsDto);
    }
}
