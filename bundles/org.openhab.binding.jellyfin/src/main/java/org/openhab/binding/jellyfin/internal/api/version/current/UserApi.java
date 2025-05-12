package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.AuthenticateUserByName;
import org.openhab.binding.jellyfin.internal.api.version.current.model.AuthenticationResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.CreateUserByName;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ForgotPasswordDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ForgotPasswordPinDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ForgotPasswordResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PinRedeemResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ProblemDetails;
import org.openhab.binding.jellyfin.internal.api.version.current.model.QuickConnectDto;
import java.util.UUID;
import org.openhab.binding.jellyfin.internal.api.version.current.model.UpdateUserPassword;
import org.openhab.binding.jellyfin.internal.api.version.current.model.UserConfiguration;
import org.openhab.binding.jellyfin.internal.api.version.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.UserPolicy;

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
public class UserApi {
    private ApiClient apiClient;

    public UserApi() {
        this(new ApiClient());
    }

    @Autowired
    public UserApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Authenticates a user by name.
     * 
     * <p><b>200</b> - User authenticated.
     * @param authenticateUserByName The M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName) request.
     * @return AuthenticationResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec authenticateUserByNameRequestCreation(AuthenticateUserByName authenticateUserByName) throws WebClientResponseException {
        Object postBody = authenticateUserByName;
        // verify the required parameter 'authenticateUserByName' is set
        if (authenticateUserByName == null) {
            throw new WebClientResponseException("Missing the required parameter 'authenticateUserByName' when calling authenticateUserByName", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<AuthenticationResult> localVarReturnType = new ParameterizedTypeReference<AuthenticationResult>() {};
        return apiClient.invokeAPI("/Users/AuthenticateByName", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Authenticates a user by name.
     * 
     * <p><b>200</b> - User authenticated.
     * @param authenticateUserByName The M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName) request.
     * @return AuthenticationResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<AuthenticationResult> authenticateUserByName(AuthenticateUserByName authenticateUserByName) throws WebClientResponseException {
        ParameterizedTypeReference<AuthenticationResult> localVarReturnType = new ParameterizedTypeReference<AuthenticationResult>() {};
        return authenticateUserByNameRequestCreation(authenticateUserByName).bodyToMono(localVarReturnType);
    }

    /**
     * Authenticates a user by name.
     * 
     * <p><b>200</b> - User authenticated.
     * @param authenticateUserByName The M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName) request.
     * @return ResponseEntity&lt;AuthenticationResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<AuthenticationResult>> authenticateUserByNameWithHttpInfo(AuthenticateUserByName authenticateUserByName) throws WebClientResponseException {
        ParameterizedTypeReference<AuthenticationResult> localVarReturnType = new ParameterizedTypeReference<AuthenticationResult>() {};
        return authenticateUserByNameRequestCreation(authenticateUserByName).toEntity(localVarReturnType);
    }

    /**
     * Authenticates a user by name.
     * 
     * <p><b>200</b> - User authenticated.
     * @param authenticateUserByName The M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName) request.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec authenticateUserByNameWithResponseSpec(AuthenticateUserByName authenticateUserByName) throws WebClientResponseException {
        return authenticateUserByNameRequestCreation(authenticateUserByName);
    }

    /**
     * Authenticates a user with quick connect.
     * 
     * <p><b>200</b> - User authenticated.
     * <p><b>400</b> - Missing token.
     * @param quickConnectDto The Jellyfin.Api.Models.UserDtos.QuickConnectDto request.
     * @return AuthenticationResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec authenticateWithQuickConnectRequestCreation(QuickConnectDto quickConnectDto) throws WebClientResponseException {
        Object postBody = quickConnectDto;
        // verify the required parameter 'quickConnectDto' is set
        if (quickConnectDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'quickConnectDto' when calling authenticateWithQuickConnect", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<AuthenticationResult> localVarReturnType = new ParameterizedTypeReference<AuthenticationResult>() {};
        return apiClient.invokeAPI("/Users/AuthenticateWithQuickConnect", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Authenticates a user with quick connect.
     * 
     * <p><b>200</b> - User authenticated.
     * <p><b>400</b> - Missing token.
     * @param quickConnectDto The Jellyfin.Api.Models.UserDtos.QuickConnectDto request.
     * @return AuthenticationResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<AuthenticationResult> authenticateWithQuickConnect(QuickConnectDto quickConnectDto) throws WebClientResponseException {
        ParameterizedTypeReference<AuthenticationResult> localVarReturnType = new ParameterizedTypeReference<AuthenticationResult>() {};
        return authenticateWithQuickConnectRequestCreation(quickConnectDto).bodyToMono(localVarReturnType);
    }

    /**
     * Authenticates a user with quick connect.
     * 
     * <p><b>200</b> - User authenticated.
     * <p><b>400</b> - Missing token.
     * @param quickConnectDto The Jellyfin.Api.Models.UserDtos.QuickConnectDto request.
     * @return ResponseEntity&lt;AuthenticationResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<AuthenticationResult>> authenticateWithQuickConnectWithHttpInfo(QuickConnectDto quickConnectDto) throws WebClientResponseException {
        ParameterizedTypeReference<AuthenticationResult> localVarReturnType = new ParameterizedTypeReference<AuthenticationResult>() {};
        return authenticateWithQuickConnectRequestCreation(quickConnectDto).toEntity(localVarReturnType);
    }

    /**
     * Authenticates a user with quick connect.
     * 
     * <p><b>200</b> - User authenticated.
     * <p><b>400</b> - Missing token.
     * @param quickConnectDto The Jellyfin.Api.Models.UserDtos.QuickConnectDto request.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec authenticateWithQuickConnectWithResponseSpec(QuickConnectDto quickConnectDto) throws WebClientResponseException {
        return authenticateWithQuickConnectRequestCreation(quickConnectDto);
    }

    /**
     * Creates a user.
     * 
     * <p><b>200</b> - User created.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param createUserByName The create user by name request body.
     * @return UserDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createUserByNameRequestCreation(CreateUserByName createUserByName) throws WebClientResponseException {
        Object postBody = createUserByName;
        // verify the required parameter 'createUserByName' is set
        if (createUserByName == null) {
            throw new WebClientResponseException("Missing the required parameter 'createUserByName' when calling createUserByName", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return apiClient.invokeAPI("/Users/New", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates a user.
     * 
     * <p><b>200</b> - User created.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param createUserByName The create user by name request body.
     * @return UserDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserDto> createUserByName(CreateUserByName createUserByName) throws WebClientResponseException {
        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return createUserByNameRequestCreation(createUserByName).bodyToMono(localVarReturnType);
    }

    /**
     * Creates a user.
     * 
     * <p><b>200</b> - User created.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param createUserByName The create user by name request body.
     * @return ResponseEntity&lt;UserDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserDto>> createUserByNameWithHttpInfo(CreateUserByName createUserByName) throws WebClientResponseException {
        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return createUserByNameRequestCreation(createUserByName).toEntity(localVarReturnType);
    }

    /**
     * Creates a user.
     * 
     * <p><b>200</b> - User created.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param createUserByName The create user by name request body.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createUserByNameWithResponseSpec(CreateUserByName createUserByName) throws WebClientResponseException {
        return createUserByNameRequestCreation(createUserByName);
    }

    /**
     * Deletes a user.
     * 
     * <p><b>204</b> - User deleted.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteUserRequestCreation(UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling deleteUser", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

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
        return apiClient.invokeAPI("/Users/{userId}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Deletes a user.
     * 
     * <p><b>204</b> - User deleted.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteUser(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteUserRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes a user.
     * 
     * <p><b>204</b> - User deleted.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteUserWithHttpInfo(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteUserRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Deletes a user.
     * 
     * <p><b>204</b> - User deleted.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteUserWithResponseSpec(UUID userId) throws WebClientResponseException {
        return deleteUserRequestCreation(userId);
    }

    /**
     * Initiates the forgot password process for a local user.
     * 
     * <p><b>200</b> - Password reset process started.
     * @param forgotPasswordDto The forgot password request containing the entered username.
     * @return ForgotPasswordResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec forgotPasswordRequestCreation(ForgotPasswordDto forgotPasswordDto) throws WebClientResponseException {
        Object postBody = forgotPasswordDto;
        // verify the required parameter 'forgotPasswordDto' is set
        if (forgotPasswordDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'forgotPasswordDto' when calling forgotPassword", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<ForgotPasswordResult> localVarReturnType = new ParameterizedTypeReference<ForgotPasswordResult>() {};
        return apiClient.invokeAPI("/Users/ForgotPassword", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Initiates the forgot password process for a local user.
     * 
     * <p><b>200</b> - Password reset process started.
     * @param forgotPasswordDto The forgot password request containing the entered username.
     * @return ForgotPasswordResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ForgotPasswordResult> forgotPassword(ForgotPasswordDto forgotPasswordDto) throws WebClientResponseException {
        ParameterizedTypeReference<ForgotPasswordResult> localVarReturnType = new ParameterizedTypeReference<ForgotPasswordResult>() {};
        return forgotPasswordRequestCreation(forgotPasswordDto).bodyToMono(localVarReturnType);
    }

    /**
     * Initiates the forgot password process for a local user.
     * 
     * <p><b>200</b> - Password reset process started.
     * @param forgotPasswordDto The forgot password request containing the entered username.
     * @return ResponseEntity&lt;ForgotPasswordResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ForgotPasswordResult>> forgotPasswordWithHttpInfo(ForgotPasswordDto forgotPasswordDto) throws WebClientResponseException {
        ParameterizedTypeReference<ForgotPasswordResult> localVarReturnType = new ParameterizedTypeReference<ForgotPasswordResult>() {};
        return forgotPasswordRequestCreation(forgotPasswordDto).toEntity(localVarReturnType);
    }

    /**
     * Initiates the forgot password process for a local user.
     * 
     * <p><b>200</b> - Password reset process started.
     * @param forgotPasswordDto The forgot password request containing the entered username.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec forgotPasswordWithResponseSpec(ForgotPasswordDto forgotPasswordDto) throws WebClientResponseException {
        return forgotPasswordRequestCreation(forgotPasswordDto);
    }

    /**
     * Redeems a forgot password pin.
     * 
     * <p><b>200</b> - Pin reset process started.
     * @param forgotPasswordPinDto The forgot password pin request containing the entered pin.
     * @return PinRedeemResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec forgotPasswordPinRequestCreation(ForgotPasswordPinDto forgotPasswordPinDto) throws WebClientResponseException {
        Object postBody = forgotPasswordPinDto;
        // verify the required parameter 'forgotPasswordPinDto' is set
        if (forgotPasswordPinDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'forgotPasswordPinDto' when calling forgotPasswordPin", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<PinRedeemResult> localVarReturnType = new ParameterizedTypeReference<PinRedeemResult>() {};
        return apiClient.invokeAPI("/Users/ForgotPassword/Pin", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Redeems a forgot password pin.
     * 
     * <p><b>200</b> - Pin reset process started.
     * @param forgotPasswordPinDto The forgot password pin request containing the entered pin.
     * @return PinRedeemResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<PinRedeemResult> forgotPasswordPin(ForgotPasswordPinDto forgotPasswordPinDto) throws WebClientResponseException {
        ParameterizedTypeReference<PinRedeemResult> localVarReturnType = new ParameterizedTypeReference<PinRedeemResult>() {};
        return forgotPasswordPinRequestCreation(forgotPasswordPinDto).bodyToMono(localVarReturnType);
    }

    /**
     * Redeems a forgot password pin.
     * 
     * <p><b>200</b> - Pin reset process started.
     * @param forgotPasswordPinDto The forgot password pin request containing the entered pin.
     * @return ResponseEntity&lt;PinRedeemResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<PinRedeemResult>> forgotPasswordPinWithHttpInfo(ForgotPasswordPinDto forgotPasswordPinDto) throws WebClientResponseException {
        ParameterizedTypeReference<PinRedeemResult> localVarReturnType = new ParameterizedTypeReference<PinRedeemResult>() {};
        return forgotPasswordPinRequestCreation(forgotPasswordPinDto).toEntity(localVarReturnType);
    }

    /**
     * Redeems a forgot password pin.
     * 
     * <p><b>200</b> - Pin reset process started.
     * @param forgotPasswordPinDto The forgot password pin request containing the entered pin.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec forgotPasswordPinWithResponseSpec(ForgotPasswordPinDto forgotPasswordPinDto) throws WebClientResponseException {
        return forgotPasswordPinRequestCreation(forgotPasswordPinDto);
    }

    /**
     * Gets the user based on auth token.
     * 
     * <p><b>200</b> - User returned.
     * <p><b>400</b> - Token is not owned by a user.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return UserDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getCurrentUserRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return apiClient.invokeAPI("/Users/Me", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the user based on auth token.
     * 
     * <p><b>200</b> - User returned.
     * <p><b>400</b> - Token is not owned by a user.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return UserDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserDto> getCurrentUser() throws WebClientResponseException {
        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return getCurrentUserRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets the user based on auth token.
     * 
     * <p><b>200</b> - User returned.
     * <p><b>400</b> - Token is not owned by a user.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;UserDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserDto>> getCurrentUserWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return getCurrentUserRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets the user based on auth token.
     * 
     * <p><b>200</b> - User returned.
     * <p><b>400</b> - Token is not owned by a user.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getCurrentUserWithResponseSpec() throws WebClientResponseException {
        return getCurrentUserRequestCreation();
    }

    /**
     * Gets a list of publicly visible users for display on a login screen.
     * 
     * <p><b>200</b> - Public users returned.
     * @return List&lt;UserDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPublicUsersRequestCreation() throws WebClientResponseException {
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

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return apiClient.invokeAPI("/Users/Public", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a list of publicly visible users for display on a login screen.
     * 
     * <p><b>200</b> - Public users returned.
     * @return List&lt;UserDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<UserDto> getPublicUsers() throws WebClientResponseException {
        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return getPublicUsersRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets a list of publicly visible users for display on a login screen.
     * 
     * <p><b>200</b> - Public users returned.
     * @return ResponseEntity&lt;List&lt;UserDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<UserDto>>> getPublicUsersWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return getPublicUsersRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets a list of publicly visible users for display on a login screen.
     * 
     * <p><b>200</b> - Public users returned.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPublicUsersWithResponseSpec() throws WebClientResponseException {
        return getPublicUsersRequestCreation();
    }

    /**
     * Gets a user by Id.
     * 
     * <p><b>200</b> - User returned.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @return UserDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getUserByIdRequestCreation(UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getUserById", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

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

        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return apiClient.invokeAPI("/Users/{userId}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a user by Id.
     * 
     * <p><b>200</b> - User returned.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @return UserDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserDto> getUserById(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return getUserByIdRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a user by Id.
     * 
     * <p><b>200</b> - User returned.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @return ResponseEntity&lt;UserDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserDto>> getUserByIdWithHttpInfo(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return getUserByIdRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Gets a user by Id.
     * 
     * <p><b>200</b> - User returned.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getUserByIdWithResponseSpec(UUID userId) throws WebClientResponseException {
        return getUserByIdRequestCreation(userId);
    }

    /**
     * Gets a list of users.
     * 
     * <p><b>200</b> - Users returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional filter by IsHidden&#x3D;true or false.
     * @param isDisabled Optional filter by IsDisabled&#x3D;true or false.
     * @return List&lt;UserDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getUsersRequestCreation(Boolean isHidden, Boolean isDisabled) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isHidden", isHidden));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isDisabled", isDisabled));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return apiClient.invokeAPI("/Users", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a list of users.
     * 
     * <p><b>200</b> - Users returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional filter by IsHidden&#x3D;true or false.
     * @param isDisabled Optional filter by IsDisabled&#x3D;true or false.
     * @return List&lt;UserDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<UserDto> getUsers(Boolean isHidden, Boolean isDisabled) throws WebClientResponseException {
        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return getUsersRequestCreation(isHidden, isDisabled).bodyToFlux(localVarReturnType);
    }

    /**
     * Gets a list of users.
     * 
     * <p><b>200</b> - Users returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional filter by IsHidden&#x3D;true or false.
     * @param isDisabled Optional filter by IsDisabled&#x3D;true or false.
     * @return ResponseEntity&lt;List&lt;UserDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<UserDto>>> getUsersWithHttpInfo(Boolean isHidden, Boolean isDisabled) throws WebClientResponseException {
        ParameterizedTypeReference<UserDto> localVarReturnType = new ParameterizedTypeReference<UserDto>() {};
        return getUsersRequestCreation(isHidden, isDisabled).toEntityList(localVarReturnType);
    }

    /**
     * Gets a list of users.
     * 
     * <p><b>200</b> - Users returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional filter by IsHidden&#x3D;true or false.
     * @param isDisabled Optional filter by IsDisabled&#x3D;true or false.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getUsersWithResponseSpec(Boolean isHidden, Boolean isDisabled) throws WebClientResponseException {
        return getUsersRequestCreation(isHidden, isDisabled);
    }

    /**
     * Updates a user.
     * 
     * <p><b>204</b> - User updated.
     * <p><b>400</b> - User information was not supplied.
     * <p><b>403</b> - User update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userDto The updated user model.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateUserRequestCreation(UserDto userDto, UUID userId) throws WebClientResponseException {
        Object postBody = userDto;
        // verify the required parameter 'userDto' is set
        if (userDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'userDto' when calling updateUser", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Users", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Updates a user.
     * 
     * <p><b>204</b> - User updated.
     * <p><b>400</b> - User information was not supplied.
     * <p><b>403</b> - User update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userDto The updated user model.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateUser(UserDto userDto, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateUserRequestCreation(userDto, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Updates a user.
     * 
     * <p><b>204</b> - User updated.
     * <p><b>400</b> - User information was not supplied.
     * <p><b>403</b> - User update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userDto The updated user model.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateUserWithHttpInfo(UserDto userDto, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateUserRequestCreation(userDto, userId).toEntity(localVarReturnType);
    }

    /**
     * Updates a user.
     * 
     * <p><b>204</b> - User updated.
     * <p><b>400</b> - User information was not supplied.
     * <p><b>403</b> - User update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userDto The updated user model.
     * @param userId The user id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateUserWithResponseSpec(UserDto userDto, UUID userId) throws WebClientResponseException {
        return updateUserRequestCreation(userDto, userId);
    }

    /**
     * Updates a user configuration.
     * 
     * <p><b>204</b> - User configuration updated.
     * <p><b>403</b> - User configuration update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userConfiguration The new user configuration.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateUserConfigurationRequestCreation(UserConfiguration userConfiguration, UUID userId) throws WebClientResponseException {
        Object postBody = userConfiguration;
        // verify the required parameter 'userConfiguration' is set
        if (userConfiguration == null) {
            throw new WebClientResponseException("Missing the required parameter 'userConfiguration' when calling updateUserConfiguration", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Users/Configuration", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Updates a user configuration.
     * 
     * <p><b>204</b> - User configuration updated.
     * <p><b>403</b> - User configuration update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userConfiguration The new user configuration.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateUserConfiguration(UserConfiguration userConfiguration, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateUserConfigurationRequestCreation(userConfiguration, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Updates a user configuration.
     * 
     * <p><b>204</b> - User configuration updated.
     * <p><b>403</b> - User configuration update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userConfiguration The new user configuration.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateUserConfigurationWithHttpInfo(UserConfiguration userConfiguration, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateUserConfigurationRequestCreation(userConfiguration, userId).toEntity(localVarReturnType);
    }

    /**
     * Updates a user configuration.
     * 
     * <p><b>204</b> - User configuration updated.
     * <p><b>403</b> - User configuration update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userConfiguration The new user configuration.
     * @param userId The user id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateUserConfigurationWithResponseSpec(UserConfiguration userConfiguration, UUID userId) throws WebClientResponseException {
        return updateUserConfigurationRequestCreation(userConfiguration, userId);
    }

    /**
     * Updates a user&#39;s password.
     * 
     * <p><b>204</b> - Password successfully reset.
     * <p><b>403</b> - User is not allowed to update the password.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * @param updateUserPassword The M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Nullable{System.Guid},Jellyfin.Api.Models.UserDtos.UpdateUserPassword) request.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateUserPasswordRequestCreation(UpdateUserPassword updateUserPassword, UUID userId) throws WebClientResponseException {
        Object postBody = updateUserPassword;
        // verify the required parameter 'updateUserPassword' is set
        if (updateUserPassword == null) {
            throw new WebClientResponseException("Missing the required parameter 'updateUserPassword' when calling updateUserPassword", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Users/Password", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Updates a user&#39;s password.
     * 
     * <p><b>204</b> - Password successfully reset.
     * <p><b>403</b> - User is not allowed to update the password.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * @param updateUserPassword The M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Nullable{System.Guid},Jellyfin.Api.Models.UserDtos.UpdateUserPassword) request.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateUserPassword(UpdateUserPassword updateUserPassword, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateUserPasswordRequestCreation(updateUserPassword, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Updates a user&#39;s password.
     * 
     * <p><b>204</b> - Password successfully reset.
     * <p><b>403</b> - User is not allowed to update the password.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * @param updateUserPassword The M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Nullable{System.Guid},Jellyfin.Api.Models.UserDtos.UpdateUserPassword) request.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateUserPasswordWithHttpInfo(UpdateUserPassword updateUserPassword, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateUserPasswordRequestCreation(updateUserPassword, userId).toEntity(localVarReturnType);
    }

    /**
     * Updates a user&#39;s password.
     * 
     * <p><b>204</b> - Password successfully reset.
     * <p><b>403</b> - User is not allowed to update the password.
     * <p><b>404</b> - User not found.
     * <p><b>401</b> - Unauthorized
     * @param updateUserPassword The M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Nullable{System.Guid},Jellyfin.Api.Models.UserDtos.UpdateUserPassword) request.
     * @param userId The user id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateUserPasswordWithResponseSpec(UpdateUserPassword updateUserPassword, UUID userId) throws WebClientResponseException {
        return updateUserPasswordRequestCreation(updateUserPassword, userId);
    }

    /**
     * Updates a user policy.
     * 
     * <p><b>204</b> - User policy updated.
     * <p><b>400</b> - User policy was not supplied.
     * <p><b>403</b> - User policy update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userId The user id.
     * @param userPolicy The new user policy.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateUserPolicyRequestCreation(UUID userId, UserPolicy userPolicy) throws WebClientResponseException {
        Object postBody = userPolicy;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling updateUserPolicy", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'userPolicy' is set
        if (userPolicy == null) {
            throw new WebClientResponseException("Missing the required parameter 'userPolicy' when calling updateUserPolicy", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

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
        return apiClient.invokeAPI("/Users/{userId}/Policy", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Updates a user policy.
     * 
     * <p><b>204</b> - User policy updated.
     * <p><b>400</b> - User policy was not supplied.
     * <p><b>403</b> - User policy update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userId The user id.
     * @param userPolicy The new user policy.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateUserPolicy(UUID userId, UserPolicy userPolicy) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateUserPolicyRequestCreation(userId, userPolicy).bodyToMono(localVarReturnType);
    }

    /**
     * Updates a user policy.
     * 
     * <p><b>204</b> - User policy updated.
     * <p><b>400</b> - User policy was not supplied.
     * <p><b>403</b> - User policy update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userId The user id.
     * @param userPolicy The new user policy.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateUserPolicyWithHttpInfo(UUID userId, UserPolicy userPolicy) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateUserPolicyRequestCreation(userId, userPolicy).toEntity(localVarReturnType);
    }

    /**
     * Updates a user policy.
     * 
     * <p><b>204</b> - User policy updated.
     * <p><b>400</b> - User policy was not supplied.
     * <p><b>403</b> - User policy update forbidden.
     * <p><b>401</b> - Unauthorized
     * @param userId The user id.
     * @param userPolicy The new user policy.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateUserPolicyWithResponseSpec(UUID userId, UserPolicy userPolicy) throws WebClientResponseException {
        return updateUserPolicyRequestCreation(userId, userPolicy);
    }
}
