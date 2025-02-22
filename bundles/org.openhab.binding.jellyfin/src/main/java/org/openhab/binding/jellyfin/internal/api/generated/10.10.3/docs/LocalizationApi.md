# LocalizationApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getCountries**](LocalizationApi.md#getCountries) | **GET** /Localization/Countries | Gets known countries. |
| [**getCultures**](LocalizationApi.md#getCultures) | **GET** /Localization/Cultures | Gets known cultures. |
| [**getLocalizationOptions**](LocalizationApi.md#getLocalizationOptions) | **GET** /Localization/Options | Gets localization options. |
| [**getParentalRatings**](LocalizationApi.md#getParentalRatings) | **GET** /Localization/ParentalRatings | Gets known parental ratings. |


<a id="getCountries"></a>
# **getCountries**
> List&lt;CountryInfo&gt; getCountries()

Gets known countries.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LocalizationApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LocalizationApi apiInstance = new LocalizationApi(defaultClient);
    try {
      List<CountryInfo> result = apiInstance.getCountries();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LocalizationApi#getCountries");
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

[**List&lt;CountryInfo&gt;**](CountryInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Known countries returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getCultures"></a>
# **getCultures**
> List&lt;CultureDto&gt; getCultures()

Gets known cultures.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LocalizationApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LocalizationApi apiInstance = new LocalizationApi(defaultClient);
    try {
      List<CultureDto> result = apiInstance.getCultures();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LocalizationApi#getCultures");
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

[**List&lt;CultureDto&gt;**](CultureDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Known cultures returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getLocalizationOptions"></a>
# **getLocalizationOptions**
> List&lt;LocalizationOption&gt; getLocalizationOptions()

Gets localization options.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LocalizationApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LocalizationApi apiInstance = new LocalizationApi(defaultClient);
    try {
      List<LocalizationOption> result = apiInstance.getLocalizationOptions();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LocalizationApi#getLocalizationOptions");
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

[**List&lt;LocalizationOption&gt;**](LocalizationOption.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Localization options returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getParentalRatings"></a>
# **getParentalRatings**
> List&lt;ParentalRating&gt; getParentalRatings()

Gets known parental ratings.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LocalizationApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LocalizationApi apiInstance = new LocalizationApi(defaultClient);
    try {
      List<ParentalRating> result = apiInstance.getParentalRatings();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LocalizationApi#getParentalRatings");
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

[**List&lt;ParentalRating&gt;**](ParentalRating.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Known parental ratings returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

