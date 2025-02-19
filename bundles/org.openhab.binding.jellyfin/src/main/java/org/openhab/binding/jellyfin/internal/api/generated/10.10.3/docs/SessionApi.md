# SessionApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addUserToSession**](SessionApi.md#addUserToSession) | **POST** /Sessions/{sessionId}/User/{userId} | Adds an additional user to a session. |
| [**displayContent**](SessionApi.md#displayContent) | **POST** /Sessions/{sessionId}/Viewing | Instructs a session to browse to an item or view. |
| [**getAuthProviders**](SessionApi.md#getAuthProviders) | **GET** /Auth/Providers | Get all auth providers. |
| [**getPasswordResetProviders**](SessionApi.md#getPasswordResetProviders) | **GET** /Auth/PasswordResetProviders | Get all password reset providers. |
| [**getSessions**](SessionApi.md#getSessions) | **GET** /Sessions | Gets a list of sessions. |
| [**play**](SessionApi.md#play) | **POST** /Sessions/{sessionId}/Playing | Instructs a session to play an item. |
| [**postCapabilities**](SessionApi.md#postCapabilities) | **POST** /Sessions/Capabilities | Updates capabilities for a device. |
| [**postFullCapabilities**](SessionApi.md#postFullCapabilities) | **POST** /Sessions/Capabilities/Full | Updates capabilities for a device. |
| [**removeUserFromSession**](SessionApi.md#removeUserFromSession) | **DELETE** /Sessions/{sessionId}/User/{userId} | Removes an additional user from a session. |
| [**reportSessionEnded**](SessionApi.md#reportSessionEnded) | **POST** /Sessions/Logout | Reports that a session has ended. |
| [**reportViewing**](SessionApi.md#reportViewing) | **POST** /Sessions/Viewing | Reports that a session is viewing an item. |
| [**sendFullGeneralCommand**](SessionApi.md#sendFullGeneralCommand) | **POST** /Sessions/{sessionId}/Command | Issues a full general command to a client. |
| [**sendGeneralCommand**](SessionApi.md#sendGeneralCommand) | **POST** /Sessions/{sessionId}/Command/{command} | Issues a general command to a client. |
| [**sendMessageCommand**](SessionApi.md#sendMessageCommand) | **POST** /Sessions/{sessionId}/Message | Issues a command to a client to display a message to the user. |
| [**sendPlaystateCommand**](SessionApi.md#sendPlaystateCommand) | **POST** /Sessions/{sessionId}/Playing/{command} | Issues a playstate command to a client. |
| [**sendSystemCommand**](SessionApi.md#sendSystemCommand) | **POST** /Sessions/{sessionId}/System/{command} | Issues a system command to a client. |


<a id="addUserToSession"></a>
# **addUserToSession**
> addUserToSession(sessionId, userId)

Adds an additional user to a session.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    String sessionId = "sessionId_example"; // String | The session id.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    try {
      apiInstance.addUserToSession(sessionId, userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#addUserToSession");
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
| **sessionId** | **String**| The session id. | |
| **userId** | **UUID**| The user id. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | User added to session. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="displayContent"></a>
# **displayContent**
> displayContent(sessionId, itemType, itemId, itemName)

Instructs a session to browse to an item or view.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    String sessionId = "sessionId_example"; // String | The session Id.
    BaseItemKind itemType = BaseItemKind.fromValue("AggregateFolder"); // BaseItemKind | The type of item to browse to.
    String itemId = "itemId_example"; // String | The Id of the item.
    String itemName = "itemName_example"; // String | The name of the item.
    try {
      apiInstance.displayContent(sessionId, itemType, itemId, itemName);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#displayContent");
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
| **sessionId** | **String**| The session Id. | |
| **itemType** | **BaseItemKind**| The type of item to browse to. | [enum: AggregateFolder, Audio, AudioBook, BasePluginFolder, Book, BoxSet, Channel, ChannelFolderItem, CollectionFolder, Episode, Folder, Genre, ManualPlaylistsFolder, Movie, LiveTvChannel, LiveTvProgram, MusicAlbum, MusicArtist, MusicGenre, MusicVideo, Person, Photo, PhotoAlbum, Playlist, PlaylistsFolder, Program, Recording, Season, Series, Studio, Trailer, TvChannel, TvProgram, UserRootFolder, UserView, Video, Year] |
| **itemId** | **String**| The Id of the item. | |
| **itemName** | **String**| The name of the item. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Instruction sent to session. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getAuthProviders"></a>
# **getAuthProviders**
> List&lt;NameIdPair&gt; getAuthProviders()

Get all auth providers.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    try {
      List<NameIdPair> result = apiInstance.getAuthProviders();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#getAuthProviders");
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

[**List&lt;NameIdPair&gt;**](NameIdPair.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Auth providers retrieved. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getPasswordResetProviders"></a>
# **getPasswordResetProviders**
> List&lt;NameIdPair&gt; getPasswordResetProviders()

Get all password reset providers.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    try {
      List<NameIdPair> result = apiInstance.getPasswordResetProviders();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#getPasswordResetProviders");
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

[**List&lt;NameIdPair&gt;**](NameIdPair.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Password reset providers retrieved. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSessions"></a>
# **getSessions**
> List&lt;SessionInfoDto&gt; getSessions(controllableByUserId, deviceId, activeWithinSeconds)

Gets a list of sessions.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    UUID controllableByUserId = UUID.randomUUID(); // UUID | Filter by sessions that a given user is allowed to remote control.
    String deviceId = "deviceId_example"; // String | Filter by device Id.
    Integer activeWithinSeconds = 56; // Integer | Optional. Filter by sessions that were active in the last n seconds.
    try {
      List<SessionInfoDto> result = apiInstance.getSessions(controllableByUserId, deviceId, activeWithinSeconds);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#getSessions");
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
| **controllableByUserId** | **UUID**| Filter by sessions that a given user is allowed to remote control. | [optional] |
| **deviceId** | **String**| Filter by device Id. | [optional] |
| **activeWithinSeconds** | **Integer**| Optional. Filter by sessions that were active in the last n seconds. | [optional] |

### Return type

[**List&lt;SessionInfoDto&gt;**](SessionInfoDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of sessions returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="play"></a>
# **play**
> play(sessionId, playCommand, itemIds, startPositionTicks, mediaSourceId, audioStreamIndex, subtitleStreamIndex, startIndex)

Instructs a session to play an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    String sessionId = "sessionId_example"; // String | The session id.
    PlayCommand playCommand = PlayCommand.fromValue("PlayNow"); // PlayCommand | The type of play command to issue (PlayNow, PlayNext, PlayLast). Clients who have not yet implemented play next and play last may play now.
    List<UUID> itemIds = Arrays.asList(); // List<UUID> | The ids of the items to play, comma delimited.
    Long startPositionTicks = 56L; // Long | The starting position of the first item.
    String mediaSourceId = "mediaSourceId_example"; // String | Optional. The media source id.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to play.
    Integer subtitleStreamIndex = 56; // Integer | Optional. The index of the subtitle stream to play.
    Integer startIndex = 56; // Integer | Optional. The start index.
    try {
      apiInstance.play(sessionId, playCommand, itemIds, startPositionTicks, mediaSourceId, audioStreamIndex, subtitleStreamIndex, startIndex);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#play");
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
| **sessionId** | **String**| The session id. | |
| **playCommand** | **PlayCommand**| The type of play command to issue (PlayNow, PlayNext, PlayLast). Clients who have not yet implemented play next and play last may play now. | [enum: PlayNow, PlayNext, PlayLast, PlayInstantMix, PlayShuffle] |
| **itemIds** | [**List&lt;UUID&gt;**](UUID.md)| The ids of the items to play, comma delimited. | |
| **startPositionTicks** | **Long**| The starting position of the first item. | [optional] |
| **mediaSourceId** | **String**| Optional. The media source id. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to play. | [optional] |
| **subtitleStreamIndex** | **Integer**| Optional. The index of the subtitle stream to play. | [optional] |
| **startIndex** | **Integer**| Optional. The start index. | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Instruction sent to session. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="postCapabilities"></a>
# **postCapabilities**
> postCapabilities(id, playableMediaTypes, supportedCommands, supportsMediaControl, supportsPersistentIdentifier)

Updates capabilities for a device.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    String id = "id_example"; // String | The session id.
    List<MediaType> playableMediaTypes = Arrays.asList(); // List<MediaType> | A list of playable media types, comma delimited. Audio, Video, Book, Photo.
    List<GeneralCommandType> supportedCommands = Arrays.asList(); // List<GeneralCommandType> | A list of supported remote control commands, comma delimited.
    Boolean supportsMediaControl = false; // Boolean | Determines whether media can be played remotely..
    Boolean supportsPersistentIdentifier = true; // Boolean | Determines whether the device supports a unique identifier.
    try {
      apiInstance.postCapabilities(id, playableMediaTypes, supportedCommands, supportsMediaControl, supportsPersistentIdentifier);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#postCapabilities");
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
| **id** | **String**| The session id. | [optional] |
| **playableMediaTypes** | [**List&lt;MediaType&gt;**](MediaType.md)| A list of playable media types, comma delimited. Audio, Video, Book, Photo. | [optional] |
| **supportedCommands** | [**List&lt;GeneralCommandType&gt;**](GeneralCommandType.md)| A list of supported remote control commands, comma delimited. | [optional] |
| **supportsMediaControl** | **Boolean**| Determines whether media can be played remotely.. | [optional] [default to false] |
| **supportsPersistentIdentifier** | **Boolean**| Determines whether the device supports a unique identifier. | [optional] [default to true] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Capabilities posted. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="postFullCapabilities"></a>
# **postFullCapabilities**
> postFullCapabilities(clientCapabilitiesDto, id)

Updates capabilities for a device.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    ClientCapabilitiesDto clientCapabilitiesDto = new ClientCapabilitiesDto(); // ClientCapabilitiesDto | The MediaBrowser.Model.Session.ClientCapabilities.
    String id = "id_example"; // String | The session id.
    try {
      apiInstance.postFullCapabilities(clientCapabilitiesDto, id);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#postFullCapabilities");
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
| **clientCapabilitiesDto** | [**ClientCapabilitiesDto**](ClientCapabilitiesDto.md)| The MediaBrowser.Model.Session.ClientCapabilities. | |
| **id** | **String**| The session id. | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Capabilities updated. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="removeUserFromSession"></a>
# **removeUserFromSession**
> removeUserFromSession(sessionId, userId)

Removes an additional user from a session.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    String sessionId = "sessionId_example"; // String | The session id.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    try {
      apiInstance.removeUserFromSession(sessionId, userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#removeUserFromSession");
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
| **sessionId** | **String**| The session id. | |
| **userId** | **UUID**| The user id. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | User removed from session. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="reportSessionEnded"></a>
# **reportSessionEnded**
> reportSessionEnded()

Reports that a session has ended.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    try {
      apiInstance.reportSessionEnded();
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#reportSessionEnded");
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

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Session end reported to server. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="reportViewing"></a>
# **reportViewing**
> reportViewing(itemId, sessionId)

Reports that a session is viewing an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    String itemId = "itemId_example"; // String | The item id.
    String sessionId = "sessionId_example"; // String | The session id.
    try {
      apiInstance.reportViewing(itemId, sessionId);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#reportViewing");
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
| **itemId** | **String**| The item id. | |
| **sessionId** | **String**| The session id. | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Session reported to server. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="sendFullGeneralCommand"></a>
# **sendFullGeneralCommand**
> sendFullGeneralCommand(sessionId, generalCommand)

Issues a full general command to a client.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    String sessionId = "sessionId_example"; // String | The session id.
    GeneralCommand generalCommand = new GeneralCommand(); // GeneralCommand | The MediaBrowser.Model.Session.GeneralCommand.
    try {
      apiInstance.sendFullGeneralCommand(sessionId, generalCommand);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#sendFullGeneralCommand");
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
| **sessionId** | **String**| The session id. | |
| **generalCommand** | [**GeneralCommand**](GeneralCommand.md)| The MediaBrowser.Model.Session.GeneralCommand. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Full general command sent to session. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="sendGeneralCommand"></a>
# **sendGeneralCommand**
> sendGeneralCommand(sessionId, command)

Issues a general command to a client.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    String sessionId = "sessionId_example"; // String | The session id.
    GeneralCommandType command = GeneralCommandType.fromValue("MoveUp"); // GeneralCommandType | The command to send.
    try {
      apiInstance.sendGeneralCommand(sessionId, command);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#sendGeneralCommand");
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
| **sessionId** | **String**| The session id. | |
| **command** | **GeneralCommandType**| The command to send. | [enum: MoveUp, MoveDown, MoveLeft, MoveRight, PageUp, PageDown, PreviousLetter, NextLetter, ToggleOsd, ToggleContextMenu, Select, Back, TakeScreenshot, SendKey, SendString, GoHome, GoToSettings, VolumeUp, VolumeDown, Mute, Unmute, ToggleMute, SetVolume, SetAudioStreamIndex, SetSubtitleStreamIndex, ToggleFullscreen, DisplayContent, GoToSearch, DisplayMessage, SetRepeatMode, ChannelUp, ChannelDown, Guide, ToggleStats, PlayMediaSource, PlayTrailers, SetShuffleQueue, PlayState, PlayNext, ToggleOsdMenu, Play, SetMaxStreamingBitrate, SetPlaybackOrder] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | General command sent to session. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="sendMessageCommand"></a>
# **sendMessageCommand**
> sendMessageCommand(sessionId, messageCommand)

Issues a command to a client to display a message to the user.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    String sessionId = "sessionId_example"; // String | The session id.
    MessageCommand messageCommand = new MessageCommand(); // MessageCommand | The MediaBrowser.Model.Session.MessageCommand object containing Header, Message Text, and TimeoutMs.
    try {
      apiInstance.sendMessageCommand(sessionId, messageCommand);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#sendMessageCommand");
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
| **sessionId** | **String**| The session id. | |
| **messageCommand** | [**MessageCommand**](MessageCommand.md)| The MediaBrowser.Model.Session.MessageCommand object containing Header, Message Text, and TimeoutMs. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Message sent. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="sendPlaystateCommand"></a>
# **sendPlaystateCommand**
> sendPlaystateCommand(sessionId, command, seekPositionTicks, controllingUserId)

Issues a playstate command to a client.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    String sessionId = "sessionId_example"; // String | The session id.
    PlaystateCommand command = PlaystateCommand.fromValue("Stop"); // PlaystateCommand | The MediaBrowser.Model.Session.PlaystateCommand.
    Long seekPositionTicks = 56L; // Long | The optional position ticks.
    String controllingUserId = "controllingUserId_example"; // String | The optional controlling user id.
    try {
      apiInstance.sendPlaystateCommand(sessionId, command, seekPositionTicks, controllingUserId);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#sendPlaystateCommand");
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
| **sessionId** | **String**| The session id. | |
| **command** | **PlaystateCommand**| The MediaBrowser.Model.Session.PlaystateCommand. | [enum: Stop, Pause, Unpause, NextTrack, PreviousTrack, Seek, Rewind, FastForward, PlayPause] |
| **seekPositionTicks** | **Long**| The optional position ticks. | [optional] |
| **controllingUserId** | **String**| The optional controlling user id. | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Playstate command sent to session. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="sendSystemCommand"></a>
# **sendSystemCommand**
> sendSystemCommand(sessionId, command)

Issues a system command to a client.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SessionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SessionApi apiInstance = new SessionApi(defaultClient);
    String sessionId = "sessionId_example"; // String | The session id.
    GeneralCommandType command = GeneralCommandType.fromValue("MoveUp"); // GeneralCommandType | The command to send.
    try {
      apiInstance.sendSystemCommand(sessionId, command);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionApi#sendSystemCommand");
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
| **sessionId** | **String**| The session id. | |
| **command** | **GeneralCommandType**| The command to send. | [enum: MoveUp, MoveDown, MoveLeft, MoveRight, PageUp, PageDown, PreviousLetter, NextLetter, ToggleOsd, ToggleContextMenu, Select, Back, TakeScreenshot, SendKey, SendString, GoHome, GoToSettings, VolumeUp, VolumeDown, Mute, Unmute, ToggleMute, SetVolume, SetAudioStreamIndex, SetSubtitleStreamIndex, ToggleFullscreen, DisplayContent, GoToSearch, DisplayMessage, SetRepeatMode, ChannelUp, ChannelDown, Guide, ToggleStats, PlayMediaSource, PlayTrailers, SetShuffleQueue, PlayState, PlayNext, ToggleOsdMenu, Play, SetMaxStreamingBitrate, SetPlaybackOrder] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | System command sent to session. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

