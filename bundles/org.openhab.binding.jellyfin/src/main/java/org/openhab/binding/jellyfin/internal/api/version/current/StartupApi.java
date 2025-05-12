package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.StartupConfigurationDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.StartupRemoteAccessDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.StartupUserDto;

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
public class StartupApi {
    private ApiClient apiClient;

    public StartupApi() {
        this(new ApiClient());
    }

    @Autowired
    public StartupApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Completes the startup wizard.
     * 
     * <p><b>204</b> - Startup wizard completed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec completeWizardRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Startup/Complete", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Completes the startup wizard.
     * 
     * <p><b>204</b> - Startup wizard completed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> completeWizard() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return completeWizardRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Completes the startup wizard.
     * 
     * <p><b>204</b> - Startup wizard completed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> completeWizardWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return completeWizardRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Completes the startup wizard.
     * 
     * <p><b>204</b> - Startup wizard completed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec completeWizardWithResponseSpec() throws WebClientResponseException {
        return completeWizardRequestCreation();
    }

    /**
     * Gets the first user.
     * 
     * <p><b>200</b> - Initial user retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return StartupUserDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getFirstUserRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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

        ParameterizedTypeReference<StartupUserDto> localVarReturnType = new ParameterizedTypeReference<StartupUserDto>() {};
        return apiClient.invokeAPI("/Startup/User", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the first user.
     * 
     * <p><b>200</b> - Initial user retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return StartupUserDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<StartupUserDto> getFirstUser() throws WebClientResponseException {
        ParameterizedTypeReference<StartupUserDto> localVarReturnType = new ParameterizedTypeReference<StartupUserDto>() {};
        return getFirstUserRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets the first user.
     * 
     * <p><b>200</b> - Initial user retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;StartupUserDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<StartupUserDto>> getFirstUserWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<StartupUserDto> localVarReturnType = new ParameterizedTypeReference<StartupUserDto>() {};
        return getFirstUserRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets the first user.
     * 
     * <p><b>200</b> - Initial user retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getFirstUserWithResponseSpec() throws WebClientResponseException {
        return getFirstUserRequestCreation();
    }

    /**
     * Gets the first user.
     * 
     * <p><b>200</b> - Initial user retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return StartupUserDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getFirstUser2RequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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

        ParameterizedTypeReference<StartupUserDto> localVarReturnType = new ParameterizedTypeReference<StartupUserDto>() {};
        return apiClient.invokeAPI("/Startup/FirstUser", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the first user.
     * 
     * <p><b>200</b> - Initial user retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return StartupUserDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<StartupUserDto> getFirstUser2() throws WebClientResponseException {
        ParameterizedTypeReference<StartupUserDto> localVarReturnType = new ParameterizedTypeReference<StartupUserDto>() {};
        return getFirstUser2RequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets the first user.
     * 
     * <p><b>200</b> - Initial user retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;StartupUserDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<StartupUserDto>> getFirstUser2WithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<StartupUserDto> localVarReturnType = new ParameterizedTypeReference<StartupUserDto>() {};
        return getFirstUser2RequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets the first user.
     * 
     * <p><b>200</b> - Initial user retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getFirstUser2WithResponseSpec() throws WebClientResponseException {
        return getFirstUser2RequestCreation();
    }

    /**
     * Gets the initial startup wizard configuration.
     * 
     * <p><b>200</b> - Initial startup wizard configuration retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return StartupConfigurationDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getStartupConfigurationRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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

        ParameterizedTypeReference<StartupConfigurationDto> localVarReturnType = new ParameterizedTypeReference<StartupConfigurationDto>() {};
        return apiClient.invokeAPI("/Startup/Configuration", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the initial startup wizard configuration.
     * 
     * <p><b>200</b> - Initial startup wizard configuration retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return StartupConfigurationDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<StartupConfigurationDto> getStartupConfiguration() throws WebClientResponseException {
        ParameterizedTypeReference<StartupConfigurationDto> localVarReturnType = new ParameterizedTypeReference<StartupConfigurationDto>() {};
        return getStartupConfigurationRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets the initial startup wizard configuration.
     * 
     * <p><b>200</b> - Initial startup wizard configuration retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;StartupConfigurationDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<StartupConfigurationDto>> getStartupConfigurationWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<StartupConfigurationDto> localVarReturnType = new ParameterizedTypeReference<StartupConfigurationDto>() {};
        return getStartupConfigurationRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets the initial startup wizard configuration.
     * 
     * <p><b>200</b> - Initial startup wizard configuration retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getStartupConfigurationWithResponseSpec() throws WebClientResponseException {
        return getStartupConfigurationRequestCreation();
    }

    /**
     * Sets remote access and UPnP.
     * 
     * <p><b>204</b> - Configuration saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupRemoteAccessDto The startup remote access dto.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec setRemoteAccessRequestCreation(StartupRemoteAccessDto startupRemoteAccessDto) throws WebClientResponseException {
        Object postBody = startupRemoteAccessDto;
        // verify the required parameter 'startupRemoteAccessDto' is set
        if (startupRemoteAccessDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'startupRemoteAccessDto' when calling setRemoteAccess", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Startup/RemoteAccess", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Sets remote access and UPnP.
     * 
     * <p><b>204</b> - Configuration saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupRemoteAccessDto The startup remote access dto.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> setRemoteAccess(StartupRemoteAccessDto startupRemoteAccessDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setRemoteAccessRequestCreation(startupRemoteAccessDto).bodyToMono(localVarReturnType);
    }

    /**
     * Sets remote access and UPnP.
     * 
     * <p><b>204</b> - Configuration saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupRemoteAccessDto The startup remote access dto.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> setRemoteAccessWithHttpInfo(StartupRemoteAccessDto startupRemoteAccessDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setRemoteAccessRequestCreation(startupRemoteAccessDto).toEntity(localVarReturnType);
    }

    /**
     * Sets remote access and UPnP.
     * 
     * <p><b>204</b> - Configuration saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupRemoteAccessDto The startup remote access dto.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec setRemoteAccessWithResponseSpec(StartupRemoteAccessDto startupRemoteAccessDto) throws WebClientResponseException {
        return setRemoteAccessRequestCreation(startupRemoteAccessDto);
    }

    /**
     * Sets the initial startup wizard configuration.
     * 
     * <p><b>204</b> - Configuration saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupConfigurationDto The updated startup configuration.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateInitialConfigurationRequestCreation(StartupConfigurationDto startupConfigurationDto) throws WebClientResponseException {
        Object postBody = startupConfigurationDto;
        // verify the required parameter 'startupConfigurationDto' is set
        if (startupConfigurationDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'startupConfigurationDto' when calling updateInitialConfiguration", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Startup/Configuration", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Sets the initial startup wizard configuration.
     * 
     * <p><b>204</b> - Configuration saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupConfigurationDto The updated startup configuration.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateInitialConfiguration(StartupConfigurationDto startupConfigurationDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateInitialConfigurationRequestCreation(startupConfigurationDto).bodyToMono(localVarReturnType);
    }

    /**
     * Sets the initial startup wizard configuration.
     * 
     * <p><b>204</b> - Configuration saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupConfigurationDto The updated startup configuration.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateInitialConfigurationWithHttpInfo(StartupConfigurationDto startupConfigurationDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateInitialConfigurationRequestCreation(startupConfigurationDto).toEntity(localVarReturnType);
    }

    /**
     * Sets the initial startup wizard configuration.
     * 
     * <p><b>204</b> - Configuration saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupConfigurationDto The updated startup configuration.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateInitialConfigurationWithResponseSpec(StartupConfigurationDto startupConfigurationDto) throws WebClientResponseException {
        return updateInitialConfigurationRequestCreation(startupConfigurationDto);
    }

    /**
     * Sets the user name and password.
     * 
     * <p><b>204</b> - Updated user name and password.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupUserDto The DTO containing username and password.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateStartupUserRequestCreation(StartupUserDto startupUserDto) throws WebClientResponseException {
        Object postBody = startupUserDto;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Startup/User", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Sets the user name and password.
     * 
     * <p><b>204</b> - Updated user name and password.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupUserDto The DTO containing username and password.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateStartupUser(StartupUserDto startupUserDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateStartupUserRequestCreation(startupUserDto).bodyToMono(localVarReturnType);
    }

    /**
     * Sets the user name and password.
     * 
     * <p><b>204</b> - Updated user name and password.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupUserDto The DTO containing username and password.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateStartupUserWithHttpInfo(StartupUserDto startupUserDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateStartupUserRequestCreation(startupUserDto).toEntity(localVarReturnType);
    }

    /**
     * Sets the user name and password.
     * 
     * <p><b>204</b> - Updated user name and password.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startupUserDto The DTO containing username and password.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateStartupUserWithResponseSpec(StartupUserDto startupUserDto) throws WebClientResponseException {
        return updateStartupUserRequestCreation(startupUserDto);
    }
}
