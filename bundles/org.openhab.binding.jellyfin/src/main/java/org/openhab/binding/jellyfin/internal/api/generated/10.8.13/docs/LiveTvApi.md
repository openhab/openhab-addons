# LiveTvApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addListingProvider**](LiveTvApi.md#addListingProvider) | **POST** /LiveTv/ListingProviders | Adds a listings provider. |
| [**addTunerHost**](LiveTvApi.md#addTunerHost) | **POST** /LiveTv/TunerHosts | Adds a tuner host. |
| [**cancelSeriesTimer**](LiveTvApi.md#cancelSeriesTimer) | **DELETE** /LiveTv/SeriesTimers/{timerId} | Cancels a live tv series timer. |
| [**cancelTimer**](LiveTvApi.md#cancelTimer) | **DELETE** /LiveTv/Timers/{timerId} | Cancels a live tv timer. |
| [**createSeriesTimer**](LiveTvApi.md#createSeriesTimer) | **POST** /LiveTv/SeriesTimers | Creates a live tv series timer. |
| [**createTimer**](LiveTvApi.md#createTimer) | **POST** /LiveTv/Timers | Creates a live tv timer. |
| [**deleteListingProvider**](LiveTvApi.md#deleteListingProvider) | **DELETE** /LiveTv/ListingProviders | Delete listing provider. |
| [**deleteRecording**](LiveTvApi.md#deleteRecording) | **DELETE** /LiveTv/Recordings/{recordingId} | Deletes a live tv recording. |
| [**deleteTunerHost**](LiveTvApi.md#deleteTunerHost) | **DELETE** /LiveTv/TunerHosts | Deletes a tuner host. |
| [**discoverTuners**](LiveTvApi.md#discoverTuners) | **GET** /LiveTv/Tuners/Discover | Discover tuners. |
| [**discvoverTuners**](LiveTvApi.md#discvoverTuners) | **GET** /LiveTv/Tuners/Discvover | Discover tuners. |
| [**getChannel**](LiveTvApi.md#getChannel) | **GET** /LiveTv/Channels/{channelId} | Gets a live tv channel. |
| [**getChannelMappingOptions**](LiveTvApi.md#getChannelMappingOptions) | **GET** /LiveTv/ChannelMappingOptions | Get channel mapping options. |
| [**getDefaultListingProvider**](LiveTvApi.md#getDefaultListingProvider) | **GET** /LiveTv/ListingProviders/Default | Gets default listings provider info. |
| [**getDefaultTimer**](LiveTvApi.md#getDefaultTimer) | **GET** /LiveTv/Timers/Defaults | Gets the default values for a new timer. |
| [**getGuideInfo**](LiveTvApi.md#getGuideInfo) | **GET** /LiveTv/GuideInfo | Get guid info. |
| [**getLineups**](LiveTvApi.md#getLineups) | **GET** /LiveTv/ListingProviders/Lineups | Gets available lineups. |
| [**getLiveRecordingFile**](LiveTvApi.md#getLiveRecordingFile) | **GET** /LiveTv/LiveRecordings/{recordingId}/stream | Gets a live tv recording stream. |
| [**getLiveStreamFile**](LiveTvApi.md#getLiveStreamFile) | **GET** /LiveTv/LiveStreamFiles/{streamId}/stream.{container} | Gets a live tv channel stream. |
| [**getLiveTvChannels**](LiveTvApi.md#getLiveTvChannels) | **GET** /LiveTv/Channels | Gets available live tv channels. |
| [**getLiveTvInfo**](LiveTvApi.md#getLiveTvInfo) | **GET** /LiveTv/Info | Gets available live tv services. |
| [**getLiveTvPrograms**](LiveTvApi.md#getLiveTvPrograms) | **GET** /LiveTv/Programs | Gets available live tv epgs. |
| [**getProgram**](LiveTvApi.md#getProgram) | **GET** /LiveTv/Programs/{programId} | Gets a live tv program. |
| [**getPrograms**](LiveTvApi.md#getPrograms) | **POST** /LiveTv/Programs | Gets available live tv epgs. |
| [**getRecommendedPrograms**](LiveTvApi.md#getRecommendedPrograms) | **GET** /LiveTv/Programs/Recommended | Gets recommended live tv epgs. |
| [**getRecording**](LiveTvApi.md#getRecording) | **GET** /LiveTv/Recordings/{recordingId} | Gets a live tv recording. |
| [**getRecordingFolders**](LiveTvApi.md#getRecordingFolders) | **GET** /LiveTv/Recordings/Folders | Gets recording folders. |
| [**getRecordingGroup**](LiveTvApi.md#getRecordingGroup) | **GET** /LiveTv/Recordings/Groups/{groupId} | Get recording group. |
| [**getRecordingGroups**](LiveTvApi.md#getRecordingGroups) | **GET** /LiveTv/Recordings/Groups | Gets live tv recording groups. |
| [**getRecordings**](LiveTvApi.md#getRecordings) | **GET** /LiveTv/Recordings | Gets live tv recordings. |
| [**getRecordingsSeries**](LiveTvApi.md#getRecordingsSeries) | **GET** /LiveTv/Recordings/Series | Gets live tv recording series. |
| [**getSchedulesDirectCountries**](LiveTvApi.md#getSchedulesDirectCountries) | **GET** /LiveTv/ListingProviders/SchedulesDirect/Countries | Gets available countries. |
| [**getSeriesTimer**](LiveTvApi.md#getSeriesTimer) | **GET** /LiveTv/SeriesTimers/{timerId} | Gets a live tv series timer. |
| [**getSeriesTimers**](LiveTvApi.md#getSeriesTimers) | **GET** /LiveTv/SeriesTimers | Gets live tv series timers. |
| [**getTimer**](LiveTvApi.md#getTimer) | **GET** /LiveTv/Timers/{timerId} | Gets a timer. |
| [**getTimers**](LiveTvApi.md#getTimers) | **GET** /LiveTv/Timers | Gets the live tv timers. |
| [**getTunerHostTypes**](LiveTvApi.md#getTunerHostTypes) | **GET** /LiveTv/TunerHosts/Types | Get tuner host types. |
| [**resetTuner**](LiveTvApi.md#resetTuner) | **POST** /LiveTv/Tuners/{tunerId}/Reset | Resets a tv tuner. |
| [**setChannelMapping**](LiveTvApi.md#setChannelMapping) | **POST** /LiveTv/ChannelMappings | Set channel mappings. |
| [**updateSeriesTimer**](LiveTvApi.md#updateSeriesTimer) | **POST** /LiveTv/SeriesTimers/{timerId} | Updates a live tv series timer. |
| [**updateTimer**](LiveTvApi.md#updateTimer) | **POST** /LiveTv/Timers/{timerId} | Updates a live tv timer. |


<a id="addListingProvider"></a>
# **addListingProvider**
> ListingsProviderInfo addListingProvider(pw, validateListings, validateLogin, listingsProviderInfo)

Adds a listings provider.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String pw = "pw_example"; // String | Password.
    Boolean validateListings = false; // Boolean | Validate listings.
    Boolean validateLogin = false; // Boolean | Validate login.
    ListingsProviderInfo listingsProviderInfo = new ListingsProviderInfo(); // ListingsProviderInfo | New listings info.
    try {
      ListingsProviderInfo result = apiInstance.addListingProvider(pw, validateListings, validateLogin, listingsProviderInfo);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#addListingProvider");
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
| **pw** | **String**| Password. | [optional] |
| **validateListings** | **Boolean**| Validate listings. | [optional] [default to false] |
| **validateLogin** | **Boolean**| Validate login. | [optional] [default to false] |
| **listingsProviderInfo** | [**ListingsProviderInfo**](ListingsProviderInfo.md)| New listings info. | [optional] |

### Return type

[**ListingsProviderInfo**](ListingsProviderInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Created listings provider returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="addTunerHost"></a>
# **addTunerHost**
> TunerHostInfo addTunerHost(tunerHostInfo)

Adds a tuner host.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    TunerHostInfo tunerHostInfo = new TunerHostInfo(); // TunerHostInfo | New tuner host.
    try {
      TunerHostInfo result = apiInstance.addTunerHost(tunerHostInfo);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#addTunerHost");
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
| **tunerHostInfo** | [**TunerHostInfo**](TunerHostInfo.md)| New tuner host. | [optional] |

### Return type

[**TunerHostInfo**](TunerHostInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Created tuner host returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="cancelSeriesTimer"></a>
# **cancelSeriesTimer**
> cancelSeriesTimer(timerId)

Cancels a live tv series timer.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String timerId = "timerId_example"; // String | Timer id.
    try {
      apiInstance.cancelSeriesTimer(timerId);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#cancelSeriesTimer");
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
| **timerId** | **String**| Timer id. | |

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
| **204** | Timer cancelled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="cancelTimer"></a>
# **cancelTimer**
> cancelTimer(timerId)

Cancels a live tv timer.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String timerId = "timerId_example"; // String | Timer id.
    try {
      apiInstance.cancelTimer(timerId);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#cancelTimer");
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
| **timerId** | **String**| Timer id. | |

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
| **204** | Timer deleted. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="createSeriesTimer"></a>
# **createSeriesTimer**
> createSeriesTimer(seriesTimerInfoDto)

Creates a live tv series timer.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    SeriesTimerInfoDto seriesTimerInfoDto = new SeriesTimerInfoDto(); // SeriesTimerInfoDto | New series timer info.
    try {
      apiInstance.createSeriesTimer(seriesTimerInfoDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#createSeriesTimer");
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
| **seriesTimerInfoDto** | [**SeriesTimerInfoDto**](SeriesTimerInfoDto.md)| New series timer info. | [optional] |

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
| **204** | Series timer info created. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="createTimer"></a>
# **createTimer**
> createTimer(timerInfoDto)

Creates a live tv timer.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    TimerInfoDto timerInfoDto = new TimerInfoDto(); // TimerInfoDto | New timer info.
    try {
      apiInstance.createTimer(timerInfoDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#createTimer");
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
| **timerInfoDto** | [**TimerInfoDto**](TimerInfoDto.md)| New timer info. | [optional] |

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
| **204** | Timer created. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="deleteListingProvider"></a>
# **deleteListingProvider**
> deleteListingProvider(id)

Delete listing provider.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String id = "id_example"; // String | Listing provider id.
    try {
      apiInstance.deleteListingProvider(id);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#deleteListingProvider");
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
| **id** | **String**| Listing provider id. | [optional] |

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
| **204** | Listing provider deleted. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="deleteRecording"></a>
# **deleteRecording**
> deleteRecording(recordingId)

Deletes a live tv recording.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    UUID recordingId = UUID.randomUUID(); // UUID | Recording id.
    try {
      apiInstance.deleteRecording(recordingId);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#deleteRecording");
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
| **recordingId** | **UUID**| Recording id. | |

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
| **204** | Recording deleted. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="deleteTunerHost"></a>
# **deleteTunerHost**
> deleteTunerHost(id)

Deletes a tuner host.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String id = "id_example"; // String | Tuner host id.
    try {
      apiInstance.deleteTunerHost(id);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#deleteTunerHost");
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
| **id** | **String**| Tuner host id. | [optional] |

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
| **204** | Tuner host deleted. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="discoverTuners"></a>
# **discoverTuners**
> List&lt;TunerHostInfo&gt; discoverTuners(newDevicesOnly)

Discover tuners.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    Boolean newDevicesOnly = false; // Boolean | Only discover new tuners.
    try {
      List<TunerHostInfo> result = apiInstance.discoverTuners(newDevicesOnly);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#discoverTuners");
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
| **newDevicesOnly** | **Boolean**| Only discover new tuners. | [optional] [default to false] |

### Return type

[**List&lt;TunerHostInfo&gt;**](TunerHostInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Tuners returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="discvoverTuners"></a>
# **discvoverTuners**
> List&lt;TunerHostInfo&gt; discvoverTuners(newDevicesOnly)

Discover tuners.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    Boolean newDevicesOnly = false; // Boolean | Only discover new tuners.
    try {
      List<TunerHostInfo> result = apiInstance.discvoverTuners(newDevicesOnly);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#discvoverTuners");
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
| **newDevicesOnly** | **Boolean**| Only discover new tuners. | [optional] [default to false] |

### Return type

[**List&lt;TunerHostInfo&gt;**](TunerHostInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Tuners returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getChannel"></a>
# **getChannel**
> BaseItemDto getChannel(channelId, userId)

Gets a live tv channel.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    UUID channelId = UUID.randomUUID(); // UUID | Channel id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Attach user data.
    try {
      BaseItemDto result = apiInstance.getChannel(channelId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getChannel");
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
| **channelId** | **UUID**| Channel id. | |
| **userId** | **UUID**| Optional. Attach user data. | [optional] |

### Return type

[**BaseItemDto**](BaseItemDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Live tv channel returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getChannelMappingOptions"></a>
# **getChannelMappingOptions**
> ChannelMappingOptionsDto getChannelMappingOptions(providerId)

Get channel mapping options.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String providerId = "providerId_example"; // String | Provider id.
    try {
      ChannelMappingOptionsDto result = apiInstance.getChannelMappingOptions(providerId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getChannelMappingOptions");
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
| **providerId** | **String**| Provider id. | [optional] |

### Return type

[**ChannelMappingOptionsDto**](ChannelMappingOptionsDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Channel mapping options returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getDefaultListingProvider"></a>
# **getDefaultListingProvider**
> ListingsProviderInfo getDefaultListingProvider()

Gets default listings provider info.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    try {
      ListingsProviderInfo result = apiInstance.getDefaultListingProvider();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getDefaultListingProvider");
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

[**ListingsProviderInfo**](ListingsProviderInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Default listings provider info returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getDefaultTimer"></a>
# **getDefaultTimer**
> SeriesTimerInfoDto getDefaultTimer(programId)

Gets the default values for a new timer.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String programId = "programId_example"; // String | Optional. To attach default values based on a program.
    try {
      SeriesTimerInfoDto result = apiInstance.getDefaultTimer(programId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getDefaultTimer");
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
| **programId** | **String**| Optional. To attach default values based on a program. | [optional] |

### Return type

[**SeriesTimerInfoDto**](SeriesTimerInfoDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Default values returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getGuideInfo"></a>
# **getGuideInfo**
> GuideInfo getGuideInfo()

Get guid info.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    try {
      GuideInfo result = apiInstance.getGuideInfo();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getGuideInfo");
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

[**GuideInfo**](GuideInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Guid info returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getLineups"></a>
# **getLineups**
> List&lt;NameIdPair&gt; getLineups(id, type, location, country)

Gets available lineups.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String id = "id_example"; // String | Provider id.
    String type = "type_example"; // String | Provider type.
    String location = "location_example"; // String | Location.
    String country = "country_example"; // String | Country.
    try {
      List<NameIdPair> result = apiInstance.getLineups(id, type, location, country);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getLineups");
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
| **id** | **String**| Provider id. | [optional] |
| **type** | **String**| Provider type. | [optional] |
| **location** | **String**| Location. | [optional] |
| **country** | **String**| Country. | [optional] |

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
| **200** | Available lineups returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getLiveRecordingFile"></a>
# **getLiveRecordingFile**
> File getLiveRecordingFile(recordingId)

Gets a live tv recording stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String recordingId = "recordingId_example"; // String | Recording id.
    try {
      File result = apiInstance.getLiveRecordingFile(recordingId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getLiveRecordingFile");
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
| **recordingId** | **String**| Recording id. | |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: video/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Recording stream returned. |  -  |
| **404** | Recording not found. |  -  |

<a id="getLiveStreamFile"></a>
# **getLiveStreamFile**
> File getLiveStreamFile(streamId, container)

Gets a live tv channel stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String streamId = "streamId_example"; // String | Stream id.
    String container = "container_example"; // String | Container type.
    try {
      File result = apiInstance.getLiveStreamFile(streamId, container);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getLiveStreamFile");
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
| **streamId** | **String**| Stream id. | |
| **container** | **String**| Container type. | |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: video/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Stream returned. |  -  |
| **404** | Stream not found. |  -  |

<a id="getLiveTvChannels"></a>
# **getLiveTvChannels**
> BaseItemDtoQueryResult getLiveTvChannels(type, userId, startIndex, isMovie, isSeries, isNews, isKids, isSports, limit, isFavorite, isLiked, isDisliked, enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData, sortBy, sortOrder, enableFavoriteSorting, addCurrentProgram)

Gets available live tv channels.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    ChannelType type = ChannelType.fromValue("TV"); // ChannelType | Optional. Filter by channel type.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user and attach user data.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Boolean isMovie = true; // Boolean | Optional. Filter for movies.
    Boolean isSeries = true; // Boolean | Optional. Filter for series.
    Boolean isNews = true; // Boolean | Optional. Filter for news.
    Boolean isKids = true; // Boolean | Optional. Filter for kids.
    Boolean isSports = true; // Boolean | Optional. Filter for sports.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    Boolean isFavorite = true; // Boolean | Optional. Filter by channels that are favorites, or not.
    Boolean isLiked = true; // Boolean | Optional. Filter by channels that are liked, or not.
    Boolean isDisliked = true; // Boolean | Optional. Filter by channels that are disliked, or not.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | \"Optional. The image types to include in the output.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    List<String> sortBy = Arrays.asList(); // List<String> | Optional. Key to sort by.
    SortOrder sortOrder = SortOrder.fromValue("Ascending"); // SortOrder | Optional. Sort order.
    Boolean enableFavoriteSorting = false; // Boolean | Optional. Incorporate favorite and like status into channel sorting.
    Boolean addCurrentProgram = true; // Boolean | Optional. Adds current program info to each channel.
    try {
      BaseItemDtoQueryResult result = apiInstance.getLiveTvChannels(type, userId, startIndex, isMovie, isSeries, isNews, isKids, isSports, limit, isFavorite, isLiked, isDisliked, enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData, sortBy, sortOrder, enableFavoriteSorting, addCurrentProgram);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getLiveTvChannels");
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
| **type** | [**ChannelType**](.md)| Optional. Filter by channel type. | [optional] [enum: TV, Radio] |
| **userId** | **UUID**| Optional. Filter by user and attach user data. | [optional] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **isMovie** | **Boolean**| Optional. Filter for movies. | [optional] |
| **isSeries** | **Boolean**| Optional. Filter for series. | [optional] |
| **isNews** | **Boolean**| Optional. Filter for news. | [optional] |
| **isKids** | **Boolean**| Optional. Filter for kids. | [optional] |
| **isSports** | **Boolean**| Optional. Filter for sports. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **isFavorite** | **Boolean**| Optional. Filter by channels that are favorites, or not. | [optional] |
| **isLiked** | **Boolean**| Optional. Filter by channels that are liked, or not. | [optional] |
| **isDisliked** | **Boolean**| Optional. Filter by channels that are disliked, or not. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| \&quot;Optional. The image types to include in the output. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **sortBy** | [**List&lt;String&gt;**](String.md)| Optional. Key to sort by. | [optional] |
| **sortOrder** | [**SortOrder**](.md)| Optional. Sort order. | [optional] [enum: Ascending, Descending] |
| **enableFavoriteSorting** | **Boolean**| Optional. Incorporate favorite and like status into channel sorting. | [optional] [default to false] |
| **addCurrentProgram** | **Boolean**| Optional. Adds current program info to each channel. | [optional] [default to true] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Available live tv channels returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getLiveTvInfo"></a>
# **getLiveTvInfo**
> LiveTvInfo getLiveTvInfo()

Gets available live tv services.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    try {
      LiveTvInfo result = apiInstance.getLiveTvInfo();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getLiveTvInfo");
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

[**LiveTvInfo**](LiveTvInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Available live tv services returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getLiveTvPrograms"></a>
# **getLiveTvPrograms**
> BaseItemDtoQueryResult getLiveTvPrograms(channelIds, userId, minStartDate, hasAired, isAiring, maxStartDate, minEndDate, maxEndDate, isMovie, isSeries, isNews, isKids, isSports, startIndex, limit, sortBy, sortOrder, genres, genreIds, enableImages, imageTypeLimit, enableImageTypes, enableUserData, seriesTimerId, librarySeriesId, fields, enableTotalRecordCount)

Gets available live tv epgs.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    List<UUID> channelIds = Arrays.asList(); // List<UUID> | The channels to return guide information for.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id.
    OffsetDateTime minStartDate = OffsetDateTime.now(); // OffsetDateTime | Optional. The minimum premiere start date.
    Boolean hasAired = true; // Boolean | Optional. Filter by programs that have completed airing, or not.
    Boolean isAiring = true; // Boolean | Optional. Filter by programs that are currently airing, or not.
    OffsetDateTime maxStartDate = OffsetDateTime.now(); // OffsetDateTime | Optional. The maximum premiere start date.
    OffsetDateTime minEndDate = OffsetDateTime.now(); // OffsetDateTime | Optional. The minimum premiere end date.
    OffsetDateTime maxEndDate = OffsetDateTime.now(); // OffsetDateTime | Optional. The maximum premiere end date.
    Boolean isMovie = true; // Boolean | Optional. Filter for movies.
    Boolean isSeries = true; // Boolean | Optional. Filter for series.
    Boolean isNews = true; // Boolean | Optional. Filter for news.
    Boolean isKids = true; // Boolean | Optional. Filter for kids.
    Boolean isSports = true; // Boolean | Optional. Filter for sports.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<String> sortBy = Arrays.asList(); // List<String> | Optional. Specify one or more sort orders, comma delimited. Options: Name, StartDate.
    List<SortOrder> sortOrder = Arrays.asList(); // List<SortOrder> | Sort Order - Ascending,Descending.
    List<String> genres = Arrays.asList(); // List<String> | The genres to return guide information for.
    List<UUID> genreIds = Arrays.asList(); // List<UUID> | The genre ids to return guide information for.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    String seriesTimerId = "seriesTimerId_example"; // String | Optional. Filter by series timer id.
    UUID librarySeriesId = UUID.randomUUID(); // UUID | Optional. Filter by library series id.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableTotalRecordCount = true; // Boolean | Retrieve total record count.
    try {
      BaseItemDtoQueryResult result = apiInstance.getLiveTvPrograms(channelIds, userId, minStartDate, hasAired, isAiring, maxStartDate, minEndDate, maxEndDate, isMovie, isSeries, isNews, isKids, isSports, startIndex, limit, sortBy, sortOrder, genres, genreIds, enableImages, imageTypeLimit, enableImageTypes, enableUserData, seriesTimerId, librarySeriesId, fields, enableTotalRecordCount);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getLiveTvPrograms");
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
| **channelIds** | [**List&lt;UUID&gt;**](UUID.md)| The channels to return guide information for. | [optional] |
| **userId** | **UUID**| Optional. Filter by user id. | [optional] |
| **minStartDate** | **OffsetDateTime**| Optional. The minimum premiere start date. | [optional] |
| **hasAired** | **Boolean**| Optional. Filter by programs that have completed airing, or not. | [optional] |
| **isAiring** | **Boolean**| Optional. Filter by programs that are currently airing, or not. | [optional] |
| **maxStartDate** | **OffsetDateTime**| Optional. The maximum premiere start date. | [optional] |
| **minEndDate** | **OffsetDateTime**| Optional. The minimum premiere end date. | [optional] |
| **maxEndDate** | **OffsetDateTime**| Optional. The maximum premiere end date. | [optional] |
| **isMovie** | **Boolean**| Optional. Filter for movies. | [optional] |
| **isSeries** | **Boolean**| Optional. Filter for series. | [optional] |
| **isNews** | **Boolean**| Optional. Filter for news. | [optional] |
| **isKids** | **Boolean**| Optional. Filter for kids. | [optional] |
| **isSports** | **Boolean**| Optional. Filter for sports. | [optional] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **sortBy** | [**List&lt;String&gt;**](String.md)| Optional. Specify one or more sort orders, comma delimited. Options: Name, StartDate. | [optional] |
| **sortOrder** | [**List&lt;SortOrder&gt;**](SortOrder.md)| Sort Order - Ascending,Descending. | [optional] |
| **genres** | [**List&lt;String&gt;**](String.md)| The genres to return guide information for. | [optional] |
| **genreIds** | [**List&lt;UUID&gt;**](UUID.md)| The genre ids to return guide information for. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **seriesTimerId** | **String**| Optional. Filter by series timer id. | [optional] |
| **librarySeriesId** | **UUID**| Optional. Filter by library series id. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableTotalRecordCount** | **Boolean**| Retrieve total record count. | [optional] [default to true] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Live tv epgs returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getProgram"></a>
# **getProgram**
> BaseItemDto getProgram(programId, userId)

Gets a live tv program.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String programId = "programId_example"; // String | Program id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Attach user data.
    try {
      BaseItemDto result = apiInstance.getProgram(programId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getProgram");
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
| **programId** | **String**| Program id. | |
| **userId** | **UUID**| Optional. Attach user data. | [optional] |

### Return type

[**BaseItemDto**](BaseItemDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Program returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getPrograms"></a>
# **getPrograms**
> BaseItemDtoQueryResult getPrograms(getProgramsDto)

Gets available live tv epgs.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    GetProgramsDto getProgramsDto = new GetProgramsDto(); // GetProgramsDto | Request body.
    try {
      BaseItemDtoQueryResult result = apiInstance.getPrograms(getProgramsDto);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getPrograms");
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
| **getProgramsDto** | [**GetProgramsDto**](GetProgramsDto.md)| Request body. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Live tv epgs returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRecommendedPrograms"></a>
# **getRecommendedPrograms**
> BaseItemDtoQueryResult getRecommendedPrograms(userId, limit, isAiring, hasAired, isSeries, isMovie, isNews, isKids, isSports, enableImages, imageTypeLimit, enableImageTypes, genreIds, fields, enableUserData, enableTotalRecordCount)

Gets recommended live tv epgs.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | Optional. filter by user id.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    Boolean isAiring = true; // Boolean | Optional. Filter by programs that are currently airing, or not.
    Boolean hasAired = true; // Boolean | Optional. Filter by programs that have completed airing, or not.
    Boolean isSeries = true; // Boolean | Optional. Filter for series.
    Boolean isMovie = true; // Boolean | Optional. Filter for movies.
    Boolean isNews = true; // Boolean | Optional. Filter for news.
    Boolean isKids = true; // Boolean | Optional. Filter for kids.
    Boolean isSports = true; // Boolean | Optional. Filter for sports.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    List<UUID> genreIds = Arrays.asList(); // List<UUID> | The genres to return guide information for.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableUserData = true; // Boolean | Optional. include user data.
    Boolean enableTotalRecordCount = true; // Boolean | Retrieve total record count.
    try {
      BaseItemDtoQueryResult result = apiInstance.getRecommendedPrograms(userId, limit, isAiring, hasAired, isSeries, isMovie, isNews, isKids, isSports, enableImages, imageTypeLimit, enableImageTypes, genreIds, fields, enableUserData, enableTotalRecordCount);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getRecommendedPrograms");
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
| **userId** | **UUID**| Optional. filter by user id. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **isAiring** | **Boolean**| Optional. Filter by programs that are currently airing, or not. | [optional] |
| **hasAired** | **Boolean**| Optional. Filter by programs that have completed airing, or not. | [optional] |
| **isSeries** | **Boolean**| Optional. Filter for series. | [optional] |
| **isMovie** | **Boolean**| Optional. Filter for movies. | [optional] |
| **isNews** | **Boolean**| Optional. Filter for news. | [optional] |
| **isKids** | **Boolean**| Optional. Filter for kids. | [optional] |
| **isSports** | **Boolean**| Optional. Filter for sports. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **genreIds** | [**List&lt;UUID&gt;**](UUID.md)| The genres to return guide information for. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableUserData** | **Boolean**| Optional. include user data. | [optional] |
| **enableTotalRecordCount** | **Boolean**| Retrieve total record count. | [optional] [default to true] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Recommended epgs returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRecording"></a>
# **getRecording**
> BaseItemDto getRecording(recordingId, userId)

Gets a live tv recording.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    UUID recordingId = UUID.randomUUID(); // UUID | Recording id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Attach user data.
    try {
      BaseItemDto result = apiInstance.getRecording(recordingId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getRecording");
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
| **recordingId** | **UUID**| Recording id. | |
| **userId** | **UUID**| Optional. Attach user data. | [optional] |

### Return type

[**BaseItemDto**](BaseItemDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Recording returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRecordingFolders"></a>
# **getRecordingFolders**
> BaseItemDtoQueryResult getRecordingFolders(userId)

Gets recording folders.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user and attach user data.
    try {
      BaseItemDtoQueryResult result = apiInstance.getRecordingFolders(userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getRecordingFolders");
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
| **userId** | **UUID**| Optional. Filter by user and attach user data. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Recording folders returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRecordingGroup"></a>
# **getRecordingGroup**
> getRecordingGroup(groupId)

Get recording group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    UUID groupId = UUID.randomUUID(); // UUID | Group id.
    try {
      apiInstance.getRecordingGroup(groupId);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getRecordingGroup");
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
| **groupId** | **UUID**| Group id. | |

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
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRecordingGroups"></a>
# **getRecordingGroups**
> BaseItemDtoQueryResult getRecordingGroups(userId)

Gets live tv recording groups.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user and attach user data.
    try {
      BaseItemDtoQueryResult result = apiInstance.getRecordingGroups(userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getRecordingGroups");
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
| **userId** | **UUID**| Optional. Filter by user and attach user data. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Recording groups returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRecordings"></a>
# **getRecordings**
> BaseItemDtoQueryResult getRecordings(channelId, userId, startIndex, limit, status, isInProgress, seriesTimerId, enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData, isMovie, isSeries, isKids, isSports, isNews, isLibraryItem, enableTotalRecordCount)

Gets live tv recordings.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String channelId = "channelId_example"; // String | Optional. Filter by channel id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user and attach user data.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    RecordingStatus status = RecordingStatus.fromValue("New"); // RecordingStatus | Optional. Filter by recording status.
    Boolean isInProgress = true; // Boolean | Optional. Filter by recordings that are in progress, or not.
    String seriesTimerId = "seriesTimerId_example"; // String | Optional. Filter by recordings belonging to a series timer.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Boolean isMovie = true; // Boolean | Optional. Filter for movies.
    Boolean isSeries = true; // Boolean | Optional. Filter for series.
    Boolean isKids = true; // Boolean | Optional. Filter for kids.
    Boolean isSports = true; // Boolean | Optional. Filter for sports.
    Boolean isNews = true; // Boolean | Optional. Filter for news.
    Boolean isLibraryItem = true; // Boolean | Optional. Filter for is library item.
    Boolean enableTotalRecordCount = true; // Boolean | Optional. Return total record count.
    try {
      BaseItemDtoQueryResult result = apiInstance.getRecordings(channelId, userId, startIndex, limit, status, isInProgress, seriesTimerId, enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData, isMovie, isSeries, isKids, isSports, isNews, isLibraryItem, enableTotalRecordCount);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getRecordings");
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
| **channelId** | **String**| Optional. Filter by channel id. | [optional] |
| **userId** | **UUID**| Optional. Filter by user and attach user data. | [optional] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **status** | [**RecordingStatus**](.md)| Optional. Filter by recording status. | [optional] [enum: New, InProgress, Completed, Cancelled, ConflictedOk, ConflictedNotOk, Error] |
| **isInProgress** | **Boolean**| Optional. Filter by recordings that are in progress, or not. | [optional] |
| **seriesTimerId** | **String**| Optional. Filter by recordings belonging to a series timer. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **isMovie** | **Boolean**| Optional. Filter for movies. | [optional] |
| **isSeries** | **Boolean**| Optional. Filter for series. | [optional] |
| **isKids** | **Boolean**| Optional. Filter for kids. | [optional] |
| **isSports** | **Boolean**| Optional. Filter for sports. | [optional] |
| **isNews** | **Boolean**| Optional. Filter for news. | [optional] |
| **isLibraryItem** | **Boolean**| Optional. Filter for is library item. | [optional] |
| **enableTotalRecordCount** | **Boolean**| Optional. Return total record count. | [optional] [default to true] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Live tv recordings returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRecordingsSeries"></a>
# **getRecordingsSeries**
> BaseItemDtoQueryResult getRecordingsSeries(channelId, userId, groupId, startIndex, limit, status, isInProgress, seriesTimerId, enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData, enableTotalRecordCount)

Gets live tv recording series.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String channelId = "channelId_example"; // String | Optional. Filter by channel id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user and attach user data.
    String groupId = "groupId_example"; // String | Optional. Filter by recording group.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    RecordingStatus status = RecordingStatus.fromValue("New"); // RecordingStatus | Optional. Filter by recording status.
    Boolean isInProgress = true; // Boolean | Optional. Filter by recordings that are in progress, or not.
    String seriesTimerId = "seriesTimerId_example"; // String | Optional. Filter by recordings belonging to a series timer.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Boolean enableTotalRecordCount = true; // Boolean | Optional. Return total record count.
    try {
      BaseItemDtoQueryResult result = apiInstance.getRecordingsSeries(channelId, userId, groupId, startIndex, limit, status, isInProgress, seriesTimerId, enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData, enableTotalRecordCount);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getRecordingsSeries");
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
| **channelId** | **String**| Optional. Filter by channel id. | [optional] |
| **userId** | **UUID**| Optional. Filter by user and attach user data. | [optional] |
| **groupId** | **String**| Optional. Filter by recording group. | [optional] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **status** | [**RecordingStatus**](.md)| Optional. Filter by recording status. | [optional] [enum: New, InProgress, Completed, Cancelled, ConflictedOk, ConflictedNotOk, Error] |
| **isInProgress** | **Boolean**| Optional. Filter by recordings that are in progress, or not. | [optional] |
| **seriesTimerId** | **String**| Optional. Filter by recordings belonging to a series timer. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **enableTotalRecordCount** | **Boolean**| Optional. Return total record count. | [optional] [default to true] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Live tv recordings returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSchedulesDirectCountries"></a>
# **getSchedulesDirectCountries**
> File getSchedulesDirectCountries()

Gets available countries.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    try {
      File result = apiInstance.getSchedulesDirectCountries();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getSchedulesDirectCountries");
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

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Available countries returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSeriesTimer"></a>
# **getSeriesTimer**
> SeriesTimerInfoDto getSeriesTimer(timerId)

Gets a live tv series timer.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String timerId = "timerId_example"; // String | Timer id.
    try {
      SeriesTimerInfoDto result = apiInstance.getSeriesTimer(timerId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getSeriesTimer");
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
| **timerId** | **String**| Timer id. | |

### Return type

[**SeriesTimerInfoDto**](SeriesTimerInfoDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Series timer returned. |  -  |
| **404** | Series timer not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSeriesTimers"></a>
# **getSeriesTimers**
> SeriesTimerInfoDtoQueryResult getSeriesTimers(sortBy, sortOrder)

Gets live tv series timers.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String sortBy = "sortBy_example"; // String | Optional. Sort by SortName or Priority.
    SortOrder sortOrder = SortOrder.fromValue("Ascending"); // SortOrder | Optional. Sort in Ascending or Descending order.
    try {
      SeriesTimerInfoDtoQueryResult result = apiInstance.getSeriesTimers(sortBy, sortOrder);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getSeriesTimers");
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
| **sortBy** | **String**| Optional. Sort by SortName or Priority. | [optional] |
| **sortOrder** | [**SortOrder**](.md)| Optional. Sort in Ascending or Descending order. | [optional] [enum: Ascending, Descending] |

### Return type

[**SeriesTimerInfoDtoQueryResult**](SeriesTimerInfoDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Timers returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getTimer"></a>
# **getTimer**
> TimerInfoDto getTimer(timerId)

Gets a timer.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String timerId = "timerId_example"; // String | Timer id.
    try {
      TimerInfoDto result = apiInstance.getTimer(timerId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getTimer");
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
| **timerId** | **String**| Timer id. | |

### Return type

[**TimerInfoDto**](TimerInfoDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Timer returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getTimers"></a>
# **getTimers**
> TimerInfoDtoQueryResult getTimers(channelId, seriesTimerId, isActive, isScheduled)

Gets the live tv timers.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String channelId = "channelId_example"; // String | Optional. Filter by channel id.
    String seriesTimerId = "seriesTimerId_example"; // String | Optional. Filter by timers belonging to a series timer.
    Boolean isActive = true; // Boolean | Optional. Filter by timers that are active.
    Boolean isScheduled = true; // Boolean | Optional. Filter by timers that are scheduled.
    try {
      TimerInfoDtoQueryResult result = apiInstance.getTimers(channelId, seriesTimerId, isActive, isScheduled);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getTimers");
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
| **channelId** | **String**| Optional. Filter by channel id. | [optional] |
| **seriesTimerId** | **String**| Optional. Filter by timers belonging to a series timer. | [optional] |
| **isActive** | **Boolean**| Optional. Filter by timers that are active. | [optional] |
| **isScheduled** | **Boolean**| Optional. Filter by timers that are scheduled. | [optional] |

### Return type

[**TimerInfoDtoQueryResult**](TimerInfoDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getTunerHostTypes"></a>
# **getTunerHostTypes**
> List&lt;NameIdPair&gt; getTunerHostTypes()

Get tuner host types.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    try {
      List<NameIdPair> result = apiInstance.getTunerHostTypes();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#getTunerHostTypes");
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
| **200** | Tuner host types returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="resetTuner"></a>
# **resetTuner**
> resetTuner(tunerId)

Resets a tv tuner.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String tunerId = "tunerId_example"; // String | Tuner id.
    try {
      apiInstance.resetTuner(tunerId);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#resetTuner");
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
| **tunerId** | **String**| Tuner id. | |

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
| **204** | Tuner reset. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="setChannelMapping"></a>
# **setChannelMapping**
> TunerChannelMapping setChannelMapping(setChannelMappingDto)

Set channel mappings.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    SetChannelMappingDto setChannelMappingDto = new SetChannelMappingDto(); // SetChannelMappingDto | The set channel mapping dto.
    try {
      TunerChannelMapping result = apiInstance.setChannelMapping(setChannelMappingDto);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#setChannelMapping");
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
| **setChannelMappingDto** | [**SetChannelMappingDto**](SetChannelMappingDto.md)| The set channel mapping dto. | |

### Return type

[**TunerChannelMapping**](TunerChannelMapping.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Created channel mapping returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="updateSeriesTimer"></a>
# **updateSeriesTimer**
> updateSeriesTimer(timerId, seriesTimerInfoDto)

Updates a live tv series timer.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String timerId = "timerId_example"; // String | Timer id.
    SeriesTimerInfoDto seriesTimerInfoDto = new SeriesTimerInfoDto(); // SeriesTimerInfoDto | New series timer info.
    try {
      apiInstance.updateSeriesTimer(timerId, seriesTimerInfoDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#updateSeriesTimer");
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
| **timerId** | **String**| Timer id. | |
| **seriesTimerInfoDto** | [**SeriesTimerInfoDto**](SeriesTimerInfoDto.md)| New series timer info. | [optional] |

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
| **204** | Series timer updated. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="updateTimer"></a>
# **updateTimer**
> updateTimer(timerId, timerInfoDto)

Updates a live tv timer.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LiveTvApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LiveTvApi apiInstance = new LiveTvApi(defaultClient);
    String timerId = "timerId_example"; // String | Timer id.
    TimerInfoDto timerInfoDto = new TimerInfoDto(); // TimerInfoDto | New timer info.
    try {
      apiInstance.updateTimer(timerId, timerInfoDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling LiveTvApi#updateTimer");
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
| **timerId** | **String**| Timer id. | |
| **timerInfoDto** | [**TimerInfoDto**](TimerInfoDto.md)| New timer info. | [optional] |

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
| **204** | Timer updated. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

