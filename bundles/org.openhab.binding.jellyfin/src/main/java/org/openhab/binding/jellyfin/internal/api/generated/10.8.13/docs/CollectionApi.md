# CollectionApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addToCollection**](CollectionApi.md#addToCollection) | **POST** /Collections/{collectionId}/Items | Adds items to a collection. |
| [**createCollection**](CollectionApi.md#createCollection) | **POST** /Collections | Creates a new collection. |
| [**removeFromCollection**](CollectionApi.md#removeFromCollection) | **DELETE** /Collections/{collectionId}/Items | Removes items from a collection. |


<a id="addToCollection"></a>
# **addToCollection**
> addToCollection(collectionId, ids)

Adds items to a collection.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.CollectionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    CollectionApi apiInstance = new CollectionApi(defaultClient);
    UUID collectionId = UUID.randomUUID(); // UUID | The collection id.
    List<UUID> ids = Arrays.asList(); // List<UUID> | Item ids, comma delimited.
    try {
      apiInstance.addToCollection(collectionId, ids);
    } catch (ApiException e) {
      System.err.println("Exception when calling CollectionApi#addToCollection");
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
| **collectionId** | **UUID**| The collection id. | |
| **ids** | [**List&lt;UUID&gt;**](UUID.md)| Item ids, comma delimited. | |

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
| **204** | Items added to collection. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="createCollection"></a>
# **createCollection**
> CollectionCreationResult createCollection(name, ids, parentId, isLocked)

Creates a new collection.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.CollectionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    CollectionApi apiInstance = new CollectionApi(defaultClient);
    String name = "name_example"; // String | The name of the collection.
    List<String> ids = Arrays.asList(); // List<String> | Item Ids to add to the collection.
    UUID parentId = UUID.randomUUID(); // UUID | Optional. Create the collection within a specific folder.
    Boolean isLocked = false; // Boolean | Whether or not to lock the new collection.
    try {
      CollectionCreationResult result = apiInstance.createCollection(name, ids, parentId, isLocked);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling CollectionApi#createCollection");
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
| **name** | **String**| The name of the collection. | [optional] |
| **ids** | [**List&lt;String&gt;**](String.md)| Item Ids to add to the collection. | [optional] |
| **parentId** | **UUID**| Optional. Create the collection within a specific folder. | [optional] |
| **isLocked** | **Boolean**| Whether or not to lock the new collection. | [optional] [default to false] |

### Return type

[**CollectionCreationResult**](CollectionCreationResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Collection created. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="removeFromCollection"></a>
# **removeFromCollection**
> removeFromCollection(collectionId, ids)

Removes items from a collection.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.CollectionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    CollectionApi apiInstance = new CollectionApi(defaultClient);
    UUID collectionId = UUID.randomUUID(); // UUID | The collection id.
    List<UUID> ids = Arrays.asList(); // List<UUID> | Item ids, comma delimited.
    try {
      apiInstance.removeFromCollection(collectionId, ids);
    } catch (ApiException e) {
      System.err.println("Exception when calling CollectionApi#removeFromCollection");
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
| **collectionId** | **UUID**| The collection id. | |
| **ids** | [**List&lt;UUID&gt;**](UUID.md)| Item ids, comma delimited. | |

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
| **204** | Items removed from collection. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

