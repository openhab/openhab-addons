# UserApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**authenticateUserByName**](UserApi.md#authenticateUserByName) | **POST** /Users/AuthenticateByName | Authenticates a user by name. |
| [**authenticateWithQuickConnect**](UserApi.md#authenticateWithQuickConnect) | **POST** /Users/AuthenticateWithQuickConnect | Authenticates a user with quick connect. |
| [**createUserByName**](UserApi.md#createUserByName) | **POST** /Users/New | Creates a user. |
| [**deleteUser**](UserApi.md#deleteUser) | **DELETE** /Users/{userId} | Deletes a user. |
| [**forgotPassword**](UserApi.md#forgotPassword) | **POST** /Users/ForgotPassword | Initiates the forgot password process for a local user. |
| [**forgotPasswordPin**](UserApi.md#forgotPasswordPin) | **POST** /Users/ForgotPassword/Pin | Redeems a forgot password pin. |
| [**getCurrentUser**](UserApi.md#getCurrentUser) | **GET** /Users/Me | Gets the user based on auth token. |
| [**getPublicUsers**](UserApi.md#getPublicUsers) | **GET** /Users/Public | Gets a list of publicly visible users for display on a login screen. |
| [**getUserById**](UserApi.md#getUserById) | **GET** /Users/{userId} | Gets a user by Id. |
| [**getUsers**](UserApi.md#getUsers) | **GET** /Users | Gets a list of users. |
| [**updateUser**](UserApi.md#updateUser) | **POST** /Users | Updates a user. |
| [**updateUserConfiguration**](UserApi.md#updateUserConfiguration) | **POST** /Users/Configuration | Updates a user configuration. |
| [**updateUserPassword**](UserApi.md#updateUserPassword) | **POST** /Users/Password | Updates a user&#39;s password. |
| [**updateUserPolicy**](UserApi.md#updateUserPolicy) | **POST** /Users/{userId}/Policy | Updates a user policy. |


<a id="authenticateUserByName"></a>
# **authenticateUserByName**
> AuthenticationResult authenticateUserByName(authenticateUserByName)

Authenticates a user by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    UserApi apiInstance = new UserApi(defaultClient);
    AuthenticateUserByName authenticateUserByName = new AuthenticateUserByName(); // AuthenticateUserByName | The M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName) request.
    try {
      AuthenticationResult result = apiInstance.authenticateUserByName(authenticateUserByName);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#authenticateUserByName");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **authenticateUserByName** | [**AuthenticateUserByName**](AuthenticateUserByName.md)| The M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName) request. | |

### Return type

[**AuthenticationResult**](AuthenticationResult.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User authenticated. |  -  |

<a id="authenticateWithQuickConnect"></a>
# **authenticateWithQuickConnect**
> AuthenticationResult authenticateWithQuickConnect(quickConnectDto)

Authenticates a user with quick connect.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    UserApi apiInstance = new UserApi(defaultClient);
    QuickConnectDto quickConnectDto = new QuickConnectDto(); // QuickConnectDto | The Jellyfin.Api.Models.UserDtos.QuickConnectDto request.
    try {
      AuthenticationResult result = apiInstance.authenticateWithQuickConnect(quickConnectDto);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#authenticateWithQuickConnect");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **quickConnectDto** | [**QuickConnectDto**](QuickConnectDto.md)| The Jellyfin.Api.Models.UserDtos.QuickConnectDto request. | |

### Return type

[**AuthenticationResult**](AuthenticationResult.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User authenticated. |  -  |
| **400** | Missing token. |  -  |

<a id="createUserByName"></a>
# **createUserByName**
> UserDto createUserByName(createUserByName)

Creates a user.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserApi apiInstance = new UserApi(defaultClient);
    CreateUserByName createUserByName = new CreateUserByName(); // CreateUserByName | The create user by name request body.
    try {
      UserDto result = apiInstance.createUserByName(createUserByName);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#createUserByName");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **createUserByName** | [**CreateUserByName**](CreateUserByName.md)| The create user by name request body. | |

### Return type

[**UserDto**](UserDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User created. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="deleteUser"></a>
# **deleteUser**
> deleteUser(userId)

Deletes a user.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserApi apiInstance = new UserApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    try {
      apiInstance.deleteUser(userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#deleteUser");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **userId** | **UUID**| The user id. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | User deleted. |  -  |
| **404** | User not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="forgotPassword"></a>
# **forgotPassword**
> ForgotPasswordResult forgotPassword(forgotPasswordDto)

Initiates the forgot password process for a local user.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    UserApi apiInstance = new UserApi(defaultClient);
    ForgotPasswordDto forgotPasswordDto = new ForgotPasswordDto(); // ForgotPasswordDto | The forgot password request containing the entered username.
    try {
      ForgotPasswordResult result = apiInstance.forgotPassword(forgotPasswordDto);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#forgotPassword");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **forgotPasswordDto** | [**ForgotPasswordDto**](ForgotPasswordDto.md)| The forgot password request containing the entered username. | |

### Return type

[**ForgotPasswordResult**](ForgotPasswordResult.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Password reset process started. |  -  |

<a id="forgotPasswordPin"></a>
# **forgotPasswordPin**
> PinRedeemResult forgotPasswordPin(forgotPasswordPinDto)

Redeems a forgot password pin.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    UserApi apiInstance = new UserApi(defaultClient);
    ForgotPasswordPinDto forgotPasswordPinDto = new ForgotPasswordPinDto(); // ForgotPasswordPinDto | The forgot password pin request containing the entered pin.
    try {
      PinRedeemResult result = apiInstance.forgotPasswordPin(forgotPasswordPinDto);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#forgotPasswordPin");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **forgotPasswordPinDto** | [**ForgotPasswordPinDto**](ForgotPasswordPinDto.md)| The forgot password pin request containing the entered pin. | |

### Return type

[**PinRedeemResult**](PinRedeemResult.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Pin reset process started. |  -  |

<a id="getCurrentUser"></a>
# **getCurrentUser**
> UserDto getCurrentUser()

Gets the user based on auth token.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserApi apiInstance = new UserApi(defaultClient);
    try {
      UserDto result = apiInstance.getCurrentUser();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#getCurrentUser");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**UserDto**](UserDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User returned. |  -  |
| **400** | Token is not owned by a user. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getPublicUsers"></a>
# **getPublicUsers**
> List&lt;UserDto&gt; getPublicUsers()

Gets a list of publicly visible users for display on a login screen.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    UserApi apiInstance = new UserApi(defaultClient);
    try {
      List<UserDto> result = apiInstance.getPublicUsers();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#getPublicUsers");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;UserDto&gt;**](UserDto.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Public users returned. |  -  |

<a id="getUserById"></a>
# **getUserById**
> UserDto getUserById(userId)

Gets a user by Id.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserApi apiInstance = new UserApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    try {
      UserDto result = apiInstance.getUserById(userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#getUserById");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **userId** | **UUID**| The user id. | |

### Return type

[**UserDto**](UserDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User returned. |  -  |
| **404** | User not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getUsers"></a>
# **getUsers**
> List&lt;UserDto&gt; getUsers(isHidden, isDisabled)

Gets a list of users.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserApi apiInstance = new UserApi(defaultClient);
    Boolean isHidden = true; // Boolean | Optional filter by IsHidden=true or false.
    Boolean isDisabled = true; // Boolean | Optional filter by IsDisabled=true or false.
    try {
      List<UserDto> result = apiInstance.getUsers(isHidden, isDisabled);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#getUsers");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **isHidden** | **Boolean**| Optional filter by IsHidden&#x3D;true or false. | [optional] |
| **isDisabled** | **Boolean**| Optional filter by IsDisabled&#x3D;true or false. | [optional] |

### Return type

[**List&lt;UserDto&gt;**](UserDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Users returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="updateUser"></a>
# **updateUser**
> updateUser(userDto, userId)

Updates a user.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserApi apiInstance = new UserApi(defaultClient);
    UserDto userDto = new UserDto(); // UserDto | The updated user model.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    try {
      apiInstance.updateUser(userDto, userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#updateUser");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **userDto** | [**UserDto**](UserDto.md)| The updated user model. | |
| **userId** | **UUID**| The user id. | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | User updated. |  -  |
| **400** | User information was not supplied. |  -  |
| **403** | User update forbidden. |  -  |
| **401** | Unauthorized |  -  |

<a id="updateUserConfiguration"></a>
# **updateUserConfiguration**
> updateUserConfiguration(userConfiguration, userId)

Updates a user configuration.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserApi apiInstance = new UserApi(defaultClient);
    UserConfiguration userConfiguration = new UserConfiguration(); // UserConfiguration | The new user configuration.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    try {
      apiInstance.updateUserConfiguration(userConfiguration, userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#updateUserConfiguration");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **userConfiguration** | [**UserConfiguration**](UserConfiguration.md)| The new user configuration. | |
| **userId** | **UUID**| The user id. | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | User configuration updated. |  -  |
| **403** | User configuration update forbidden. |  -  |
| **401** | Unauthorized |  -  |

<a id="updateUserPassword"></a>
# **updateUserPassword**
> updateUserPassword(updateUserPassword, userId)

Updates a user&#39;s password.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserApi apiInstance = new UserApi(defaultClient);
    UpdateUserPassword updateUserPassword = new UpdateUserPassword(); // UpdateUserPassword | The M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Nullable{System.Guid},Jellyfin.Api.Models.UserDtos.UpdateUserPassword) request.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    try {
      apiInstance.updateUserPassword(updateUserPassword, userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#updateUserPassword");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **updateUserPassword** | [**UpdateUserPassword**](UpdateUserPassword.md)| The M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Nullable{System.Guid},Jellyfin.Api.Models.UserDtos.UpdateUserPassword) request. | |
| **userId** | **UUID**| The user id. | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Password successfully reset. |  -  |
| **403** | User is not allowed to update the password. |  -  |
| **404** | User not found. |  -  |
| **401** | Unauthorized |  -  |

<a id="updateUserPolicy"></a>
# **updateUserPolicy**
> updateUserPolicy(userId, userPolicy)

Updates a user policy.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserApi apiInstance = new UserApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    UserPolicy userPolicy = new UserPolicy(); // UserPolicy | The new user policy.
    try {
      apiInstance.updateUserPolicy(userId, userPolicy);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserApi#updateUserPolicy");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **userId** | **UUID**| The user id. | |
| **userPolicy** | [**UserPolicy**](UserPolicy.md)| The new user policy. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | User policy updated. |  -  |
| **400** | User policy was not supplied. |  -  |
| **403** | User policy update forbidden. |  -  |
| **401** | Unauthorized |  -  |

