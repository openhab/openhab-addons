# PlaybackReportingActivityApi

All URIs are relative to *http://nuc.ehrendingen:8096*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**customQuery**](PlaybackReportingActivityApi.md#customQuery) | **POST** /user_usage_stats/submit_custom_query |  |
| [**getBreakdownReport**](PlaybackReportingActivityApi.md#getBreakdownReport) | **GET** /user_usage_stats/{breakdownType}/BreakdownReport |  |
| [**getDurationHistogramReport**](PlaybackReportingActivityApi.md#getDurationHistogramReport) | **GET** /user_usage_stats/DurationHistogramReport |  |
| [**getHourlyReport**](PlaybackReportingActivityApi.md#getHourlyReport) | **GET** /user_usage_stats/HourlyReport |  |
| [**getJellyfinUsers**](PlaybackReportingActivityApi.md#getJellyfinUsers) | **GET** /user_usage_stats/user_list |  |
| [**getMovieReport**](PlaybackReportingActivityApi.md#getMovieReport) | **GET** /user_usage_stats/MoviesReport |  |
| [**getTvShowsReport**](PlaybackReportingActivityApi.md#getTvShowsReport) | **GET** /user_usage_stats/GetTvShowsReport |  |
| [**getTypeFilterList**](PlaybackReportingActivityApi.md#getTypeFilterList) | **GET** /user_usage_stats/type_filter_list |  |
| [**getUsageStats**](PlaybackReportingActivityApi.md#getUsageStats) | **GET** /user_usage_stats/PlayActivity |  |
| [**getUserReport**](PlaybackReportingActivityApi.md#getUserReport) | **GET** /user_usage_stats/user_activity |  |
| [**getUserReportData**](PlaybackReportingActivityApi.md#getUserReportData) | **GET** /user_usage_stats/{userId}/{date}/GetItems |  |
| [**ignoreListAdd**](PlaybackReportingActivityApi.md#ignoreListAdd) | **GET** /user_usage_stats/user_manage/add |  |
| [**ignoreListRemove**](PlaybackReportingActivityApi.md#ignoreListRemove) | **GET** /user_usage_stats/user_manage/remove |  |
| [**loadBackup**](PlaybackReportingActivityApi.md#loadBackup) | **GET** /user_usage_stats/load_backup |  |
| [**pruneUnknownUsers**](PlaybackReportingActivityApi.md#pruneUnknownUsers) | **GET** /user_usage_stats/user_manage/prune |  |
| [**saveBackup**](PlaybackReportingActivityApi.md#saveBackup) | **GET** /user_usage_stats/save_backup |  |


<a id="customQuery"></a>
# **customQuery**
> Map&lt;String, Object&gt; customQuery(customQueryData)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    CustomQueryData customQueryData = new CustomQueryData(); // CustomQueryData | 
    try {
      Map<String, Object> result = apiInstance.customQuery(customQueryData);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#customQuery");
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
| **customQueryData** | [**CustomQueryData**](CustomQueryData.md)|  | [optional] |

### Return type

**Map&lt;String, Object&gt;**

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getBreakdownReport"></a>
# **getBreakdownReport**
> getBreakdownReport(breakdownType, days, endDate, timezoneOffset)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    String breakdownType = "breakdownType_example"; // String | 
    Integer days = 56; // Integer | 
    OffsetDateTime endDate = OffsetDateTime.now(); // OffsetDateTime | 
    Float timezoneOffset = 3.4F; // Float | 
    try {
      apiInstance.getBreakdownReport(breakdownType, days, endDate, timezoneOffset);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#getBreakdownReport");
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
| **breakdownType** | **String**|  | |
| **days** | **Integer**|  | [optional] |
| **endDate** | **OffsetDateTime**|  | [optional] |
| **timezoneOffset** | **Float**|  | [optional] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getDurationHistogramReport"></a>
# **getDurationHistogramReport**
> getDurationHistogramReport(days, endDate, filter)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    Integer days = 56; // Integer | 
    OffsetDateTime endDate = OffsetDateTime.now(); // OffsetDateTime | 
    String filter = "filter_example"; // String | 
    try {
      apiInstance.getDurationHistogramReport(days, endDate, filter);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#getDurationHistogramReport");
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
| **days** | **Integer**|  | [optional] |
| **endDate** | **OffsetDateTime**|  | [optional] |
| **filter** | **String**|  | [optional] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getHourlyReport"></a>
# **getHourlyReport**
> getHourlyReport(days, endDate, filter, timezoneOffset)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    Integer days = 56; // Integer | 
    OffsetDateTime endDate = OffsetDateTime.now(); // OffsetDateTime | 
    String filter = "filter_example"; // String | 
    Float timezoneOffset = 3.4F; // Float | 
    try {
      apiInstance.getHourlyReport(days, endDate, filter, timezoneOffset);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#getHourlyReport");
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
| **days** | **Integer**|  | [optional] |
| **endDate** | **OffsetDateTime**|  | [optional] |
| **filter** | **String**|  | [optional] |
| **timezoneOffset** | **Float**|  | [optional] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getJellyfinUsers"></a>
# **getJellyfinUsers**
> getJellyfinUsers()



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    try {
      apiInstance.getJellyfinUsers();
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#getJellyfinUsers");
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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMovieReport"></a>
# **getMovieReport**
> getMovieReport(days, endDate, timezoneOffset)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    Integer days = 56; // Integer | 
    OffsetDateTime endDate = OffsetDateTime.now(); // OffsetDateTime | 
    Float timezoneOffset = 3.4F; // Float | 
    try {
      apiInstance.getMovieReport(days, endDate, timezoneOffset);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#getMovieReport");
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
| **days** | **Integer**|  | [optional] |
| **endDate** | **OffsetDateTime**|  | [optional] |
| **timezoneOffset** | **Float**|  | [optional] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getTvShowsReport"></a>
# **getTvShowsReport**
> getTvShowsReport(days, endDate, timezoneOffset)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    Integer days = 56; // Integer | 
    OffsetDateTime endDate = OffsetDateTime.now(); // OffsetDateTime | 
    Float timezoneOffset = 3.4F; // Float | 
    try {
      apiInstance.getTvShowsReport(days, endDate, timezoneOffset);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#getTvShowsReport");
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
| **days** | **Integer**|  | [optional] |
| **endDate** | **OffsetDateTime**|  | [optional] |
| **timezoneOffset** | **Float**|  | [optional] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getTypeFilterList"></a>
# **getTypeFilterList**
> getTypeFilterList()



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    try {
      apiInstance.getTypeFilterList();
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#getTypeFilterList");
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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getUsageStats"></a>
# **getUsageStats**
> getUsageStats(days, endDate, filter, dataType, timezoneOffset)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    Integer days = 56; // Integer | 
    OffsetDateTime endDate = OffsetDateTime.now(); // OffsetDateTime | 
    String filter = "filter_example"; // String | 
    String dataType = "dataType_example"; // String | 
    Float timezoneOffset = 3.4F; // Float | 
    try {
      apiInstance.getUsageStats(days, endDate, filter, dataType, timezoneOffset);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#getUsageStats");
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
| **days** | **Integer**|  | [optional] |
| **endDate** | **OffsetDateTime**|  | [optional] |
| **filter** | **String**|  | [optional] |
| **dataType** | **String**|  | [optional] |
| **timezoneOffset** | **Float**|  | [optional] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getUserReport"></a>
# **getUserReport**
> getUserReport(days, endDate, timezoneOffset)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    Integer days = 56; // Integer | 
    OffsetDateTime endDate = OffsetDateTime.now(); // OffsetDateTime | 
    Float timezoneOffset = 3.4F; // Float | 
    try {
      apiInstance.getUserReport(days, endDate, timezoneOffset);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#getUserReport");
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
| **days** | **Integer**|  | [optional] |
| **endDate** | **OffsetDateTime**|  | [optional] |
| **timezoneOffset** | **Float**|  | [optional] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getUserReportData"></a>
# **getUserReportData**
> getUserReportData(userId, date, filter, timezoneOffset)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    String userId = "userId_example"; // String | 
    String date = "date_example"; // String | 
    String filter = "filter_example"; // String | 
    Float timezoneOffset = 3.4F; // Float | 
    try {
      apiInstance.getUserReportData(userId, date, filter, timezoneOffset);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#getUserReportData");
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
| **userId** | **String**|  | |
| **date** | **String**|  | |
| **filter** | **String**|  | [optional] |
| **timezoneOffset** | **Float**|  | [optional] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="ignoreListAdd"></a>
# **ignoreListAdd**
> Boolean ignoreListAdd(id)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    String id = "id_example"; // String | 
    try {
      Boolean result = apiInstance.ignoreListAdd(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#ignoreListAdd");
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
| **id** | **String**|  | [optional] |

### Return type

**Boolean**

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="ignoreListRemove"></a>
# **ignoreListRemove**
> Boolean ignoreListRemove(id)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    String id = "id_example"; // String | 
    try {
      Boolean result = apiInstance.ignoreListRemove(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#ignoreListRemove");
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
| **id** | **String**|  | [optional] |

### Return type

**Boolean**

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="loadBackup"></a>
# **loadBackup**
> List&lt;String&gt; loadBackup(backupFilePath)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    String backupFilePath = "backupFilePath_example"; // String | 
    try {
      List<String> result = apiInstance.loadBackup(backupFilePath);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#loadBackup");
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
| **backupFilePath** | **String**|  | [optional] |

### Return type

**List&lt;String&gt;**

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="pruneUnknownUsers"></a>
# **pruneUnknownUsers**
> Boolean pruneUnknownUsers()



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    try {
      Boolean result = apiInstance.pruneUnknownUsers();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#pruneUnknownUsers");
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

**Boolean**

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="saveBackup"></a>
# **saveBackup**
> List&lt;String&gt; saveBackup()



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaybackReportingActivityApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaybackReportingActivityApi apiInstance = new PlaybackReportingActivityApi(defaultClient);
    try {
      List<String> result = apiInstance.saveBackup();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaybackReportingActivityApi#saveBackup");
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

**List&lt;String&gt;**

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

