package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.DeviceProfile;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.DeviceProfileInfo;
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
public class DlnaApi {
    private ApiClient apiClient;

    public DlnaApi() {
        this(new ApiClient());
    }

    @Autowired
    public DlnaApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Creates a profile.
     * 
     * <p>
     * <b>204</b> - Device profile created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param deviceProfile Device profile.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createProfileRequestCreation(DeviceProfile deviceProfile) throws WebClientResponseException {
        Object postBody = deviceProfile;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Dlna/Profiles", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates a profile.
     * 
     * <p>
     * <b>204</b> - Device profile created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param deviceProfile Device profile.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> createProfile(DeviceProfile deviceProfile) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return createProfileRequestCreation(deviceProfile).bodyToMono(localVarReturnType);
    }

    /**
     * Creates a profile.
     * 
     * <p>
     * <b>204</b> - Device profile created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param deviceProfile Device profile.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> createProfileWithHttpInfo(DeviceProfile deviceProfile)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return createProfileRequestCreation(deviceProfile).toEntity(localVarReturnType);
    }

    /**
     * Creates a profile.
     * 
     * <p>
     * <b>204</b> - Device profile created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param deviceProfile Device profile.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createProfileWithResponseSpec(DeviceProfile deviceProfile) throws WebClientResponseException {
        return createProfileRequestCreation(deviceProfile);
    }

    /**
     * Deletes a profile.
     * 
     * <p>
     * <b>204</b> - Device profile deleted.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteProfileRequestCreation(String profileId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'profileId' is set
        if (profileId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'profileId' when calling deleteProfile",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("profileId", profileId);

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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Dlna/Profiles/{profileId}", HttpMethod.DELETE, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Deletes a profile.
     * 
     * <p>
     * <b>204</b> - Device profile deleted.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteProfile(String profileId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteProfileRequestCreation(profileId).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes a profile.
     * 
     * <p>
     * <b>204</b> - Device profile deleted.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteProfileWithHttpInfo(String profileId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteProfileRequestCreation(profileId).toEntity(localVarReturnType);
    }

    /**
     * Deletes a profile.
     * 
     * <p>
     * <b>204</b> - Device profile deleted.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteProfileWithResponseSpec(String profileId) throws WebClientResponseException {
        return deleteProfileRequestCreation(profileId);
    }

    /**
     * Gets the default profile.
     * 
     * <p>
     * <b>200</b> - Default device profile returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return DeviceProfile
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDefaultProfileRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<DeviceProfile> localVarReturnType = new ParameterizedTypeReference<DeviceProfile>() {
        };
        return apiClient.invokeAPI("/Dlna/Profiles/Default", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets the default profile.
     * 
     * <p>
     * <b>200</b> - Default device profile returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return DeviceProfile
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<DeviceProfile> getDefaultProfile() throws WebClientResponseException {
        ParameterizedTypeReference<DeviceProfile> localVarReturnType = new ParameterizedTypeReference<DeviceProfile>() {
        };
        return getDefaultProfileRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets the default profile.
     * 
     * <p>
     * <b>200</b> - Default device profile returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;DeviceProfile&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<DeviceProfile>> getDefaultProfileWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<DeviceProfile> localVarReturnType = new ParameterizedTypeReference<DeviceProfile>() {
        };
        return getDefaultProfileRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets the default profile.
     * 
     * <p>
     * <b>200</b> - Default device profile returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDefaultProfileWithResponseSpec() throws WebClientResponseException {
        return getDefaultProfileRequestCreation();
    }

    /**
     * Gets a single profile.
     * 
     * <p>
     * <b>200</b> - Device profile returned.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile Id.
     * @return DeviceProfile
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getProfileRequestCreation(String profileId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'profileId' is set
        if (profileId == null) {
            throw new WebClientResponseException("Missing the required parameter 'profileId' when calling getProfile",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("profileId", profileId);

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

        ParameterizedTypeReference<DeviceProfile> localVarReturnType = new ParameterizedTypeReference<DeviceProfile>() {
        };
        return apiClient.invokeAPI("/Dlna/Profiles/{profileId}", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets a single profile.
     * 
     * <p>
     * <b>200</b> - Device profile returned.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile Id.
     * @return DeviceProfile
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<DeviceProfile> getProfile(String profileId) throws WebClientResponseException {
        ParameterizedTypeReference<DeviceProfile> localVarReturnType = new ParameterizedTypeReference<DeviceProfile>() {
        };
        return getProfileRequestCreation(profileId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a single profile.
     * 
     * <p>
     * <b>200</b> - Device profile returned.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile Id.
     * @return ResponseEntity&lt;DeviceProfile&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<DeviceProfile>> getProfileWithHttpInfo(String profileId)
            throws WebClientResponseException {
        ParameterizedTypeReference<DeviceProfile> localVarReturnType = new ParameterizedTypeReference<DeviceProfile>() {
        };
        return getProfileRequestCreation(profileId).toEntity(localVarReturnType);
    }

    /**
     * Gets a single profile.
     * 
     * <p>
     * <b>200</b> - Device profile returned.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile Id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getProfileWithResponseSpec(String profileId) throws WebClientResponseException {
        return getProfileRequestCreation(profileId);
    }

    /**
     * Get profile infos.
     * 
     * <p>
     * <b>200</b> - Device profile infos returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;DeviceProfileInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getProfileInfosRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<DeviceProfileInfo> localVarReturnType = new ParameterizedTypeReference<DeviceProfileInfo>() {
        };
        return apiClient.invokeAPI("/Dlna/ProfileInfos", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get profile infos.
     * 
     * <p>
     * <b>200</b> - Device profile infos returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;DeviceProfileInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<DeviceProfileInfo> getProfileInfos() throws WebClientResponseException {
        ParameterizedTypeReference<DeviceProfileInfo> localVarReturnType = new ParameterizedTypeReference<DeviceProfileInfo>() {
        };
        return getProfileInfosRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get profile infos.
     * 
     * <p>
     * <b>200</b> - Device profile infos returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;DeviceProfileInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<DeviceProfileInfo>>> getProfileInfosWithHttpInfo()
            throws WebClientResponseException {
        ParameterizedTypeReference<DeviceProfileInfo> localVarReturnType = new ParameterizedTypeReference<DeviceProfileInfo>() {
        };
        return getProfileInfosRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get profile infos.
     * 
     * <p>
     * <b>200</b> - Device profile infos returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getProfileInfosWithResponseSpec() throws WebClientResponseException {
        return getProfileInfosRequestCreation();
    }

    /**
     * Updates a profile.
     * 
     * <p>
     * <b>204</b> - Device profile updated.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile id.
     * @param deviceProfile Device profile.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateProfileRequestCreation(String profileId, DeviceProfile deviceProfile)
            throws WebClientResponseException {
        Object postBody = deviceProfile;
        // verify the required parameter 'profileId' is set
        if (profileId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'profileId' when calling updateProfile",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("profileId", profileId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Dlna/Profiles/{profileId}", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Updates a profile.
     * 
     * <p>
     * <b>204</b> - Device profile updated.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile id.
     * @param deviceProfile Device profile.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateProfile(String profileId, DeviceProfile deviceProfile) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateProfileRequestCreation(profileId, deviceProfile).bodyToMono(localVarReturnType);
    }

    /**
     * Updates a profile.
     * 
     * <p>
     * <b>204</b> - Device profile updated.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile id.
     * @param deviceProfile Device profile.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateProfileWithHttpInfo(String profileId, DeviceProfile deviceProfile)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateProfileRequestCreation(profileId, deviceProfile).toEntity(localVarReturnType);
    }

    /**
     * Updates a profile.
     * 
     * <p>
     * <b>204</b> - Device profile updated.
     * <p>
     * <b>404</b> - Device profile not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param profileId Profile id.
     * @param deviceProfile Device profile.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateProfileWithResponseSpec(String profileId, DeviceProfile deviceProfile)
            throws WebClientResponseException {
        return updateProfileRequestCreation(profileId, deviceProfile);
    }
}
