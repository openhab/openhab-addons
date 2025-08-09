package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.GenericType;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.AuthenticateUserByName;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.AuthenticationResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.CreateUserByName;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ForgotPasswordDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ForgotPasswordPinDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ForgotPasswordResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PinRedeemResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.QuickConnectDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UpdateUserPassword;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserConfiguration;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserPolicy;

public class UserApi {
    private ApiClient apiClient;

    public UserApi() {
        this(Configuration.getDefaultApiClient());
    }

    public UserApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get the API client
     *
     * @return API client
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Set the API client
     *
     * @param apiClient an instance of API client
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Authenticates a user by name.
     * 
     * @param authenticateUserByName The
     *            M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName)
     *            request. (required)
     * @return AuthenticationResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User authenticated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public AuthenticationResult authenticateUserByName(
            @org.eclipse.jdt.annotation.Nullable AuthenticateUserByName authenticateUserByName) throws ApiException {
        return authenticateUserByNameWithHttpInfo(authenticateUserByName).getData();
    }

    /**
     * Authenticates a user by name.
     * 
     * @param authenticateUserByName The
     *            M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName)
     *            request. (required)
     * @return ApiResponse&lt;AuthenticationResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User authenticated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<AuthenticationResult> authenticateUserByNameWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable AuthenticateUserByName authenticateUserByName) throws ApiException {
        // Check required parameters
        if (authenticateUserByName == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'authenticateUserByName' when calling authenticateUserByName");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        GenericType<AuthenticationResult> localVarReturnType = new GenericType<AuthenticationResult>() {
        };
        return apiClient.invokeAPI("UserApi.authenticateUserByName", "/Users/AuthenticateByName", "POST",
                new ArrayList<>(), authenticateUserByName, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Authenticates a user with quick connect.
     * 
     * @param quickConnectDto The Jellyfin.Api.Models.UserDtos.QuickConnectDto request. (required)
     * @return AuthenticationResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User authenticated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Missing token.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public AuthenticationResult authenticateWithQuickConnect(
            @org.eclipse.jdt.annotation.Nullable QuickConnectDto quickConnectDto) throws ApiException {
        return authenticateWithQuickConnectWithHttpInfo(quickConnectDto).getData();
    }

    /**
     * Authenticates a user with quick connect.
     * 
     * @param quickConnectDto The Jellyfin.Api.Models.UserDtos.QuickConnectDto request. (required)
     * @return ApiResponse&lt;AuthenticationResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User authenticated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Missing token.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<AuthenticationResult> authenticateWithQuickConnectWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable QuickConnectDto quickConnectDto) throws ApiException {
        // Check required parameters
        if (quickConnectDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'quickConnectDto' when calling authenticateWithQuickConnect");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        GenericType<AuthenticationResult> localVarReturnType = new GenericType<AuthenticationResult>() {
        };
        return apiClient.invokeAPI("UserApi.authenticateWithQuickConnect", "/Users/AuthenticateWithQuickConnect",
                "POST", new ArrayList<>(), quickConnectDto, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Creates a user.
     * 
     * @param createUserByName The create user by name request body. (required)
     * @return UserDto
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User created.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public UserDto createUserByName(@org.eclipse.jdt.annotation.Nullable CreateUserByName createUserByName)
            throws ApiException {
        return createUserByNameWithHttpInfo(createUserByName).getData();
    }

    /**
     * Creates a user.
     * 
     * @param createUserByName The create user by name request body. (required)
     * @return ApiResponse&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User created.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<UserDto> createUserByNameWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable CreateUserByName createUserByName) throws ApiException {
        // Check required parameters
        if (createUserByName == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'createUserByName' when calling createUserByName");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<UserDto> localVarReturnType = new GenericType<UserDto>() {
        };
        return apiClient.invokeAPI("UserApi.createUserByName", "/Users/New", "POST", new ArrayList<>(),
                createUserByName, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Deletes a user.
     * 
     * @param userId The user id. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>User not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void deleteUser(@org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        deleteUserWithHttpInfo(userId);
    }

    /**
     * Deletes a user.
     * 
     * @param userId The user id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>User not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> deleteUserWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        // Check required parameters
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling deleteUser");
        }

        // Path parameters
        String localVarPath = "/Users/{userId}".replaceAll("\\{userId}", apiClient.escapeString(userId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("UserApi.deleteUser", localVarPath, "DELETE", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Initiates the forgot password process for a local user.
     * 
     * @param forgotPasswordDto The forgot password request containing the entered username. (required)
     * @return ForgotPasswordResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Password reset process started.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ForgotPasswordResult forgotPassword(@org.eclipse.jdt.annotation.Nullable ForgotPasswordDto forgotPasswordDto)
            throws ApiException {
        return forgotPasswordWithHttpInfo(forgotPasswordDto).getData();
    }

    /**
     * Initiates the forgot password process for a local user.
     * 
     * @param forgotPasswordDto The forgot password request containing the entered username. (required)
     * @return ApiResponse&lt;ForgotPasswordResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Password reset process started.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<ForgotPasswordResult> forgotPasswordWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordDto forgotPasswordDto) throws ApiException {
        // Check required parameters
        if (forgotPasswordDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'forgotPasswordDto' when calling forgotPassword");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        GenericType<ForgotPasswordResult> localVarReturnType = new GenericType<ForgotPasswordResult>() {
        };
        return apiClient.invokeAPI("UserApi.forgotPassword", "/Users/ForgotPassword", "POST", new ArrayList<>(),
                forgotPasswordDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Redeems a forgot password pin.
     * 
     * @param forgotPasswordPinDto The forgot password pin request containing the entered pin. (required)
     * @return PinRedeemResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Pin reset process started.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public PinRedeemResult forgotPasswordPin(
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordPinDto forgotPasswordPinDto) throws ApiException {
        return forgotPasswordPinWithHttpInfo(forgotPasswordPinDto).getData();
    }

    /**
     * Redeems a forgot password pin.
     * 
     * @param forgotPasswordPinDto The forgot password pin request containing the entered pin. (required)
     * @return ApiResponse&lt;PinRedeemResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Pin reset process started.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<PinRedeemResult> forgotPasswordPinWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordPinDto forgotPasswordPinDto) throws ApiException {
        // Check required parameters
        if (forgotPasswordPinDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'forgotPasswordPinDto' when calling forgotPasswordPin");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        GenericType<PinRedeemResult> localVarReturnType = new GenericType<PinRedeemResult>() {
        };
        return apiClient.invokeAPI("UserApi.forgotPasswordPin", "/Users/ForgotPassword/Pin", "POST", new ArrayList<>(),
                forgotPasswordPinDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets the user based on auth token.
     * 
     * @return UserDto
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Token is not owned by a user.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public UserDto getCurrentUser() throws ApiException {
        return getCurrentUserWithHttpInfo().getData();
    }

    /**
     * Gets the user based on auth token.
     * 
     * @return ApiResponse&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Token is not owned by a user.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<UserDto> getCurrentUserWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<UserDto> localVarReturnType = new GenericType<UserDto>() {
        };
        return apiClient.invokeAPI("UserApi.getCurrentUser", "/Users/Me", "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a list of publicly visible users for display on a login screen.
     * 
     * @return List&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Public users returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public List<UserDto> getPublicUsers() throws ApiException {
        return getPublicUsersWithHttpInfo().getData();
    }

    /**
     * Gets a list of publicly visible users for display on a login screen.
     * 
     * @return ApiResponse&lt;List&lt;UserDto&gt;&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Public users returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<List<UserDto>> getPublicUsersWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<List<UserDto>> localVarReturnType = new GenericType<List<UserDto>>() {
        };
        return apiClient.invokeAPI("UserApi.getPublicUsers", "/Users/Public", "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets a user by Id.
     * 
     * @param userId The user id. (required)
     * @return UserDto
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>User not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public UserDto getUserById(@org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getUserByIdWithHttpInfo(userId).getData();
    }

    /**
     * Gets a user by Id.
     * 
     * @param userId The user id. (required)
     * @return ApiResponse&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>User not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<UserDto> getUserByIdWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        // Check required parameters
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling getUserById");
        }

        // Path parameters
        String localVarPath = "/Users/{userId}".replaceAll("\\{userId}", apiClient.escapeString(userId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<UserDto> localVarReturnType = new GenericType<UserDto>() {
        };
        return apiClient.invokeAPI("UserApi.getUserById", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a list of users.
     * 
     * @param isHidden Optional filter by IsHidden&#x3D;true or false. (optional)
     * @param isDisabled Optional filter by IsDisabled&#x3D;true or false. (optional)
     * @return List&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Users returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public List<UserDto> getUsers(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isDisabled) throws ApiException {
        return getUsersWithHttpInfo(isHidden, isDisabled).getData();
    }

    /**
     * Gets a list of users.
     * 
     * @param isHidden Optional filter by IsHidden&#x3D;true or false. (optional)
     * @param isDisabled Optional filter by IsDisabled&#x3D;true or false. (optional)
     * @return ApiResponse&lt;List&lt;UserDto&gt;&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Users returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<List<UserDto>> getUsersWithHttpInfo(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isDisabled) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "isHidden", isHidden));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isDisabled", isDisabled));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<UserDto>> localVarReturnType = new GenericType<List<UserDto>>() {
        };
        return apiClient.invokeAPI("UserApi.getUsers", "/Users", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Updates a user.
     * 
     * @param userDto The updated user model. (required)
     * @param userId The user id. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>User information was not supplied.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User update forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void updateUser(@org.eclipse.jdt.annotation.Nullable UserDto userDto,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        updateUserWithHttpInfo(userDto, userId);
    }

    /**
     * Updates a user.
     * 
     * @param userDto The updated user model. (required)
     * @param userId The user id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>User information was not supplied.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User update forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> updateUserWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UserDto userDto,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (userDto == null) {
            throw new ApiException(400, "Missing the required parameter 'userDto' when calling updateUser");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("UserApi.updateUser", "/Users", "POST", localVarQueryParams, userDto,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Updates a user configuration.
     * 
     * @param userConfiguration The new user configuration. (required)
     * @param userId The user id. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User configuration updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User configuration update forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void updateUserConfiguration(@org.eclipse.jdt.annotation.Nullable UserConfiguration userConfiguration,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        updateUserConfigurationWithHttpInfo(userConfiguration, userId);
    }

    /**
     * Updates a user configuration.
     * 
     * @param userConfiguration The new user configuration. (required)
     * @param userId The user id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User configuration updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User configuration update forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> updateUserConfigurationWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UserConfiguration userConfiguration,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (userConfiguration == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'userConfiguration' when calling updateUserConfiguration");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("UserApi.updateUserConfiguration", "/Users/Configuration", "POST",
                localVarQueryParams, userConfiguration, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Updates a user&#39;s password.
     * 
     * @param updateUserPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Nullable{System.Guid},Jellyfin.Api.Models.UserDtos.UpdateUserPassword)
     *            request. (required)
     * @param userId The user id. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Password successfully reset.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User is not allowed to update the password.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>User not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void updateUserPassword(@org.eclipse.jdt.annotation.Nullable UpdateUserPassword updateUserPassword,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        updateUserPasswordWithHttpInfo(updateUserPassword, userId);
    }

    /**
     * Updates a user&#39;s password.
     * 
     * @param updateUserPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Nullable{System.Guid},Jellyfin.Api.Models.UserDtos.UpdateUserPassword)
     *            request. (required)
     * @param userId The user id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Password successfully reset.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User is not allowed to update the password.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>User not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> updateUserPasswordWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UpdateUserPassword updateUserPassword,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (updateUserPassword == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'updateUserPassword' when calling updateUserPassword");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("UserApi.updateUserPassword", "/Users/Password", "POST", localVarQueryParams,
                updateUserPassword, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Updates a user policy.
     * 
     * @param userId The user id. (required)
     * @param userPolicy The new user policy. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User policy updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>User policy was not supplied.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User policy update forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void updateUserPolicy(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserPolicy userPolicy) throws ApiException {
        updateUserPolicyWithHttpInfo(userId, userPolicy);
    }

    /**
     * Updates a user policy.
     * 
     * @param userId The user id. (required)
     * @param userPolicy The new user policy. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User policy updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>User policy was not supplied.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User policy update forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> updateUserPolicyWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserPolicy userPolicy) throws ApiException {
        // Check required parameters
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling updateUserPolicy");
        }
        if (userPolicy == null) {
            throw new ApiException(400, "Missing the required parameter 'userPolicy' when calling updateUserPolicy");
        }

        // Path parameters
        String localVarPath = "/Users/{userId}/Policy".replaceAll("\\{userId}",
                apiClient.escapeString(userId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("UserApi.updateUserPolicy", localVarPath, "POST", new ArrayList<>(), userPolicy,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }
}
