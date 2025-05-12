package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.DeviceInfoDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.DeviceInfoDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.DeviceOptionsDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ProblemDetails;
import java.util.UUID;

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
     * <p><b>204</b> - Device deleted.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteDeviceRequestCreation(String id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling deleteDevice", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Devices", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Deletes a device.
     * 
     * <p><b>204</b> - Device deleted.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteDevice(String id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteDeviceRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes a device.
     * 
     * <p><b>204</b> - Device deleted.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteDeviceWithHttpInfo(String id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteDeviceRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Deletes a device.
     * 
     * <p><b>204</b> - Device deleted.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
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
     * <p><b>200</b> - Device info retrieved.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @return DeviceInfoDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDeviceInfoRequestCreation(String id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getDeviceInfo", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<DeviceInfoDto> localVarReturnType = new ParameterizedTypeReference<DeviceInfoDto>() {};
        return apiClient.invokeAPI("/Devices/Info", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get info for a device.
     * 
     * <p><b>200</b> - Device info retrieved.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @return DeviceInfoDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<DeviceInfoDto> getDeviceInfo(String id) throws WebClientResponseException {
        ParameterizedTypeReference<DeviceInfoDto> localVarReturnType = new ParameterizedTypeReference<DeviceInfoDto>() {};
        return getDeviceInfoRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Get info for a device.
     * 
     * <p><b>200</b> - Device info retrieved.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @return ResponseEntity&lt;DeviceInfoDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<DeviceInfoDto>> getDeviceInfoWithHttpInfo(String id) throws WebClientResponseException {
        ParameterizedTypeReference<DeviceInfoDto> localVarReturnType = new ParameterizedTypeReference<DeviceInfoDto>() {};
        return getDeviceInfoRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Get info for a device.
     * 
     * <p><b>200</b> - Device info retrieved.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
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
     * <p><b>200</b> - Device options retrieved.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @return DeviceOptionsDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDeviceOptionsRequestCreation(String id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getDeviceOptions", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<DeviceOptionsDto> localVarReturnType = new ParameterizedTypeReference<DeviceOptionsDto>() {};
        return apiClient.invokeAPI("/Devices/Options", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get options for a device.
     * 
     * <p><b>200</b> - Device options retrieved.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @return DeviceOptionsDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<DeviceOptionsDto> getDeviceOptions(String id) throws WebClientResponseException {
        ParameterizedTypeReference<DeviceOptionsDto> localVarReturnType = new ParameterizedTypeReference<DeviceOptionsDto>() {};
        return getDeviceOptionsRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Get options for a device.
     * 
     * <p><b>200</b> - Device options retrieved.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @return ResponseEntity&lt;DeviceOptionsDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<DeviceOptionsDto>> getDeviceOptionsWithHttpInfo(String id) throws WebClientResponseException {
        ParameterizedTypeReference<DeviceOptionsDto> localVarReturnType = new ParameterizedTypeReference<DeviceOptionsDto>() {};
        return getDeviceOptionsRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Get options for a device.
     * 
     * <p><b>200</b> - Device options retrieved.
     * <p><b>404</b> - Device not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
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
     * <p><b>200</b> - Devices retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Gets or sets the user identifier.
     * @return DeviceInfoDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDevicesRequestCreation(UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<DeviceInfoDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<DeviceInfoDtoQueryResult>() {};
        return apiClient.invokeAPI("/Devices", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get Devices.
     * 
     * <p><b>200</b> - Devices retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Gets or sets the user identifier.
     * @return DeviceInfoDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<DeviceInfoDtoQueryResult> getDevices(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<DeviceInfoDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<DeviceInfoDtoQueryResult>() {};
        return getDevicesRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Get Devices.
     * 
     * <p><b>200</b> - Devices retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Gets or sets the user identifier.
     * @return ResponseEntity&lt;DeviceInfoDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<DeviceInfoDtoQueryResult>> getDevicesWithHttpInfo(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<DeviceInfoDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<DeviceInfoDtoQueryResult>() {};
        return getDevicesRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Get Devices.
     * 
     * <p><b>200</b> - Devices retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Gets or sets the user identifier.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDevicesWithResponseSpec(UUID userId) throws WebClientResponseException {
        return getDevicesRequestCreation(userId);
    }

    /**
     * Update device options.
     * 
     * <p><b>204</b> - Device options updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @param deviceOptionsDto Device Options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateDeviceOptionsRequestCreation(String id, DeviceOptionsDto deviceOptionsDto) throws WebClientResponseException {
        Object postBody = deviceOptionsDto;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling updateDeviceOptions", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'deviceOptionsDto' is set
        if (deviceOptionsDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'deviceOptionsDto' when calling updateDeviceOptions", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));
        
        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Devices/Options", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Update device options.
     * 
     * <p><b>204</b> - Device options updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @param deviceOptionsDto Device Options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateDeviceOptions(String id, DeviceOptionsDto deviceOptionsDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateDeviceOptionsRequestCreation(id, deviceOptionsDto).bodyToMono(localVarReturnType);
    }

    /**
     * Update device options.
     * 
     * <p><b>204</b> - Device options updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @param deviceOptionsDto Device Options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateDeviceOptionsWithHttpInfo(String id, DeviceOptionsDto deviceOptionsDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateDeviceOptionsRequestCreation(id, deviceOptionsDto).toEntity(localVarReturnType);
    }

    /**
     * Update device options.
     * 
     * <p><b>204</b> - Device options updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id Device Id.
     * @param deviceOptionsDto Device Options.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateDeviceOptionsWithResponseSpec(String id, DeviceOptionsDto deviceOptionsDto) throws WebClientResponseException {
        return updateDeviceOptionsRequestCreation(id, deviceOptionsDto);
    }
}
