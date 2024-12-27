# BrandingApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getBrandingCss**](BrandingApi.md#getBrandingCss) | **GET** /Branding/Css | Gets branding css. |
| [**getBrandingCss2**](BrandingApi.md#getBrandingCss2) | **GET** /Branding/Css.css | Gets branding css. |
| [**getBrandingOptions**](BrandingApi.md#getBrandingOptions) | **GET** /Branding/Configuration | Gets branding configuration. |


<a id="getBrandingCss"></a>
# **getBrandingCss**
> String getBrandingCss()

Gets branding css.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.BrandingApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    BrandingApi apiInstance = new BrandingApi(defaultClient);
    try {
      String result = apiInstance.getBrandingCss();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling BrandingApi#getBrandingCss");
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

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/css, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Branding css returned. |  -  |
| **204** | No branding css configured. |  -  |

<a id="getBrandingCss2"></a>
# **getBrandingCss2**
> String getBrandingCss2()

Gets branding css.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.BrandingApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    BrandingApi apiInstance = new BrandingApi(defaultClient);
    try {
      String result = apiInstance.getBrandingCss2();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling BrandingApi#getBrandingCss2");
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

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/css, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Branding css returned. |  -  |
| **204** | No branding css configured. |  -  |

<a id="getBrandingOptions"></a>
# **getBrandingOptions**
> BrandingOptions getBrandingOptions()

Gets branding configuration.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.BrandingApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    BrandingApi apiInstance = new BrandingApi(defaultClient);
    try {
      BrandingOptions result = apiInstance.getBrandingOptions();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling BrandingApi#getBrandingOptions");
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

[**BrandingOptions**](BrandingOptions.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Branding configuration returned. |  -  |

