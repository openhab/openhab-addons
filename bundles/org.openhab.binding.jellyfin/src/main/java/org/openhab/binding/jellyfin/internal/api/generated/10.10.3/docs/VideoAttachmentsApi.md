# VideoAttachmentsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getAttachment**](VideoAttachmentsApi.md#getAttachment) | **GET** /Videos/{videoId}/{mediaSourceId}/Attachments/{index} | Get video attachment. |


<a id="getAttachment"></a>
# **getAttachment**
> File getAttachment(videoId, mediaSourceId, index)

Get video attachment.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.VideoAttachmentsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    VideoAttachmentsApi apiInstance = new VideoAttachmentsApi(defaultClient);
    UUID videoId = UUID.randomUUID(); // UUID | Video ID.
    String mediaSourceId = "mediaSourceId_example"; // String | Media Source ID.
    Integer index = 56; // Integer | Attachment Index.
    try {
      File result = apiInstance.getAttachment(videoId, mediaSourceId, index);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling VideoAttachmentsApi#getAttachment");
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
| **videoId** | **UUID**| Video ID. | |
| **mediaSourceId** | **String**| Media Source ID. | |
| **index** | **Integer**| Attachment Index. | |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/octet-stream, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Attachment retrieved. |  -  |
| **404** | Video or attachment not found. |  -  |

