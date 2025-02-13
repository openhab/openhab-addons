# ImageApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**deleteCustomSplashscreen**](ImageApi.md#deleteCustomSplashscreen) | **DELETE** /Branding/Splashscreen | Delete a custom splashscreen. |
| [**deleteItemImage**](ImageApi.md#deleteItemImage) | **DELETE** /Items/{itemId}/Images/{imageType} | Delete an item&#39;s image. |
| [**deleteItemImageByIndex**](ImageApi.md#deleteItemImageByIndex) | **DELETE** /Items/{itemId}/Images/{imageType}/{imageIndex} | Delete an item&#39;s image. |
| [**deleteUserImage**](ImageApi.md#deleteUserImage) | **DELETE** /Users/{userId}/Images/{imageType} | Delete the user&#39;s image. |
| [**deleteUserImageByIndex**](ImageApi.md#deleteUserImageByIndex) | **DELETE** /Users/{userId}/Images/{imageType}/{index} | Delete the user&#39;s image. |
| [**getArtistImage**](ImageApi.md#getArtistImage) | **GET** /Artists/{name}/Images/{imageType}/{imageIndex} | Get artist image by name. |
| [**getGenreImage**](ImageApi.md#getGenreImage) | **GET** /Genres/{name}/Images/{imageType} | Get genre image by name. |
| [**getGenreImageByIndex**](ImageApi.md#getGenreImageByIndex) | **GET** /Genres/{name}/Images/{imageType}/{imageIndex} | Get genre image by name. |
| [**getItemImage**](ImageApi.md#getItemImage) | **GET** /Items/{itemId}/Images/{imageType} | Gets the item&#39;s image. |
| [**getItemImage2**](ImageApi.md#getItemImage2) | **GET** /Items/{itemId}/Images/{imageType}/{imageIndex}/{tag}/{format}/{maxWidth}/{maxHeight}/{percentPlayed}/{unplayedCount} | Gets the item&#39;s image. |
| [**getItemImageByIndex**](ImageApi.md#getItemImageByIndex) | **GET** /Items/{itemId}/Images/{imageType}/{imageIndex} | Gets the item&#39;s image. |
| [**getItemImageInfos**](ImageApi.md#getItemImageInfos) | **GET** /Items/{itemId}/Images | Get item image infos. |
| [**getMusicGenreImage**](ImageApi.md#getMusicGenreImage) | **GET** /MusicGenres/{name}/Images/{imageType} | Get music genre image by name. |
| [**getMusicGenreImageByIndex**](ImageApi.md#getMusicGenreImageByIndex) | **GET** /MusicGenres/{name}/Images/{imageType}/{imageIndex} | Get music genre image by name. |
| [**getPersonImage**](ImageApi.md#getPersonImage) | **GET** /Persons/{name}/Images/{imageType} | Get person image by name. |
| [**getPersonImageByIndex**](ImageApi.md#getPersonImageByIndex) | **GET** /Persons/{name}/Images/{imageType}/{imageIndex} | Get person image by name. |
| [**getSplashscreen**](ImageApi.md#getSplashscreen) | **GET** /Branding/Splashscreen | Generates or gets the splashscreen. |
| [**getStudioImage**](ImageApi.md#getStudioImage) | **GET** /Studios/{name}/Images/{imageType} | Get studio image by name. |
| [**getStudioImageByIndex**](ImageApi.md#getStudioImageByIndex) | **GET** /Studios/{name}/Images/{imageType}/{imageIndex} | Get studio image by name. |
| [**getUserImage**](ImageApi.md#getUserImage) | **GET** /Users/{userId}/Images/{imageType} | Get user profile image. |
| [**getUserImageByIndex**](ImageApi.md#getUserImageByIndex) | **GET** /Users/{userId}/Images/{imageType}/{imageIndex} | Get user profile image. |
| [**headArtistImage**](ImageApi.md#headArtistImage) | **HEAD** /Artists/{name}/Images/{imageType}/{imageIndex} | Get artist image by name. |
| [**headGenreImage**](ImageApi.md#headGenreImage) | **HEAD** /Genres/{name}/Images/{imageType} | Get genre image by name. |
| [**headGenreImageByIndex**](ImageApi.md#headGenreImageByIndex) | **HEAD** /Genres/{name}/Images/{imageType}/{imageIndex} | Get genre image by name. |
| [**headItemImage**](ImageApi.md#headItemImage) | **HEAD** /Items/{itemId}/Images/{imageType} | Gets the item&#39;s image. |
| [**headItemImage2**](ImageApi.md#headItemImage2) | **HEAD** /Items/{itemId}/Images/{imageType}/{imageIndex}/{tag}/{format}/{maxWidth}/{maxHeight}/{percentPlayed}/{unplayedCount} | Gets the item&#39;s image. |
| [**headItemImageByIndex**](ImageApi.md#headItemImageByIndex) | **HEAD** /Items/{itemId}/Images/{imageType}/{imageIndex} | Gets the item&#39;s image. |
| [**headMusicGenreImage**](ImageApi.md#headMusicGenreImage) | **HEAD** /MusicGenres/{name}/Images/{imageType} | Get music genre image by name. |
| [**headMusicGenreImageByIndex**](ImageApi.md#headMusicGenreImageByIndex) | **HEAD** /MusicGenres/{name}/Images/{imageType}/{imageIndex} | Get music genre image by name. |
| [**headPersonImage**](ImageApi.md#headPersonImage) | **HEAD** /Persons/{name}/Images/{imageType} | Get person image by name. |
| [**headPersonImageByIndex**](ImageApi.md#headPersonImageByIndex) | **HEAD** /Persons/{name}/Images/{imageType}/{imageIndex} | Get person image by name. |
| [**headStudioImage**](ImageApi.md#headStudioImage) | **HEAD** /Studios/{name}/Images/{imageType} | Get studio image by name. |
| [**headStudioImageByIndex**](ImageApi.md#headStudioImageByIndex) | **HEAD** /Studios/{name}/Images/{imageType}/{imageIndex} | Get studio image by name. |
| [**headUserImage**](ImageApi.md#headUserImage) | **HEAD** /Users/{userId}/Images/{imageType} | Get user profile image. |
| [**headUserImageByIndex**](ImageApi.md#headUserImageByIndex) | **HEAD** /Users/{userId}/Images/{imageType}/{imageIndex} | Get user profile image. |
| [**postUserImage**](ImageApi.md#postUserImage) | **POST** /Users/{userId}/Images/{imageType} | Sets the user image. |
| [**postUserImageByIndex**](ImageApi.md#postUserImageByIndex) | **POST** /Users/{userId}/Images/{imageType}/{index} | Sets the user image. |
| [**setItemImage**](ImageApi.md#setItemImage) | **POST** /Items/{itemId}/Images/{imageType} | Set item image. |
| [**setItemImageByIndex**](ImageApi.md#setItemImageByIndex) | **POST** /Items/{itemId}/Images/{imageType}/{imageIndex} | Set item image. |
| [**updateItemImageIndex**](ImageApi.md#updateItemImageIndex) | **POST** /Items/{itemId}/Images/{imageType}/{imageIndex}/Index | Updates the index for an item image. |
| [**uploadCustomSplashscreen**](ImageApi.md#uploadCustomSplashscreen) | **POST** /Branding/Splashscreen | Uploads a custom splashscreen.  The body is expected to the image contents base64 encoded. |


<a id="deleteCustomSplashscreen"></a>
# **deleteCustomSplashscreen**
> deleteCustomSplashscreen()

Delete a custom splashscreen.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    try {
      apiInstance.deleteCustomSplashscreen();
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#deleteCustomSplashscreen");
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
| **204** | Successfully deleted the custom splashscreen. |  -  |
| **403** | User does not have permission to delete splashscreen.. |  -  |
| **401** | Unauthorized |  -  |

<a id="deleteItemImage"></a>
# **deleteItemImage**
> deleteItemImage(itemId, imageType, imageIndex)

Delete an item&#39;s image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | The image index.
    try {
      apiInstance.deleteItemImage(itemId, imageType, imageIndex);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#deleteItemImage");
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
| **itemId** | **UUID**| Item id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| The image index. | [optional] |

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
| **204** | Image deleted. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="deleteItemImageByIndex"></a>
# **deleteItemImageByIndex**
> deleteItemImageByIndex(itemId, imageType, imageIndex)

Delete an item&#39;s image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | The image index.
    try {
      apiInstance.deleteItemImageByIndex(itemId, imageType, imageIndex);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#deleteItemImageByIndex");
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
| **itemId** | **UUID**| Item id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| The image index. | |

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
| **204** | Image deleted. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="deleteUserImage"></a>
# **deleteUserImage**
> deleteUserImage(userId, imageType, index)

Delete the user&#39;s image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User Id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | (Unused) Image type.
    Integer index = 56; // Integer | (Unused) Image index.
    try {
      apiInstance.deleteUserImage(userId, imageType, index);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#deleteUserImage");
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
| **userId** | **UUID**| User Id. | |
| **imageType** | [**ImageType**](.md)| (Unused) Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **index** | **Integer**| (Unused) Image index. | [optional] |

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
| **204** | Image deleted. |  -  |
| **403** | User does not have permission to delete the image. |  -  |
| **401** | Unauthorized |  -  |

<a id="deleteUserImageByIndex"></a>
# **deleteUserImageByIndex**
> deleteUserImageByIndex(userId, imageType, index)

Delete the user&#39;s image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User Id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | (Unused) Image type.
    Integer index = 56; // Integer | (Unused) Image index.
    try {
      apiInstance.deleteUserImageByIndex(userId, imageType, index);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#deleteUserImageByIndex");
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
| **userId** | **UUID**| User Id. | |
| **imageType** | [**ImageType**](.md)| (Unused) Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **index** | **Integer**| (Unused) Image index. | |

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
| **204** | Image deleted. |  -  |
| **403** | User does not have permission to delete the image. |  -  |
| **401** | Unauthorized |  -  |

<a id="getArtistImage"></a>
# **getArtistImage**
> File getArtistImage(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get artist image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Artist name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.getArtistImage(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getArtistImage");
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
| **name** | **String**| Artist name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getGenreImage"></a>
# **getGenreImage**
> File getGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex)

Get genre image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Genre name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.getGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getGenreImage");
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
| **name** | **String**| Genre name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getGenreImageByIndex"></a>
# **getGenreImageByIndex**
> File getGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get genre image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Genre name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.getGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getGenreImageByIndex");
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
| **name** | **String**| Genre name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getItemImage"></a>
# **getItemImage**
> File getItemImage(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex)

Gets the item&#39;s image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.getItemImage(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getItemImage");
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
| **itemId** | **UUID**| Item id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **format** | [**ImageFormat**](.md)| Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getItemImage2"></a>
# **getItemImage2**
> File getItemImage2(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Gets the item&#39;s image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer imageIndex = 56; // Integer | Image index.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.getItemImage2(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getItemImage2");
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
| **itemId** | **UUID**| Item id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **maxWidth** | **Integer**| The maximum image width to return. | |
| **maxHeight** | **Integer**| The maximum image height to return. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [enum: Bmp, Gif, Jpg, Png, Webp] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | |
| **imageIndex** | **Integer**| Image index. | |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getItemImageByIndex"></a>
# **getItemImageByIndex**
> File getItemImageByIndex(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer)

Gets the item&#39;s image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.getItemImageByIndex(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getItemImageByIndex");
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
| **itemId** | **UUID**| Item id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **format** | [**ImageFormat**](.md)| Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getItemImageInfos"></a>
# **getItemImageInfos**
> List&lt;ImageInfo&gt; getItemImageInfos(itemId)

Get item image infos.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    try {
      List<ImageInfo> result = apiInstance.getItemImageInfos(itemId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getItemImageInfos");
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
| **itemId** | **UUID**| Item id. | |

### Return type

[**List&lt;ImageInfo&gt;**](ImageInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Item images returned. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMusicGenreImage"></a>
# **getMusicGenreImage**
> File getMusicGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex)

Get music genre image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Music genre name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.getMusicGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getMusicGenreImage");
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
| **name** | **String**| Music genre name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getMusicGenreImageByIndex"></a>
# **getMusicGenreImageByIndex**
> File getMusicGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get music genre image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Music genre name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.getMusicGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getMusicGenreImageByIndex");
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
| **name** | **String**| Music genre name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getPersonImage"></a>
# **getPersonImage**
> File getPersonImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex)

Get person image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Person name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.getPersonImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getPersonImage");
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
| **name** | **String**| Person name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getPersonImageByIndex"></a>
# **getPersonImageByIndex**
> File getPersonImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get person image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Person name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.getPersonImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getPersonImageByIndex");
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
| **name** | **String**| Person name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getSplashscreen"></a>
# **getSplashscreen**
> File getSplashscreen(tag, format, maxWidth, maxHeight, width, height, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, quality)

Generates or gets the splashscreen.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String tag = "tag_example"; // String | Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Integer blur = 56; // Integer | Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Apply a foreground layer on top of the image.
    Integer quality = 90; // Integer | Quality setting, from 0-100.
    try {
      File result = apiInstance.getSplashscreen(tag, format, maxWidth, maxHeight, width, height, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, quality);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getSplashscreen");
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
| **tag** | **String**| Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **blur** | **Integer**| Blur image. | [optional] |
| **backgroundColor** | **String**| Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Apply a foreground layer on top of the image. | [optional] |
| **quality** | **Integer**| Quality setting, from 0-100. | [optional] [default to 90] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Splashscreen returned successfully. |  -  |

<a id="getStudioImage"></a>
# **getStudioImage**
> File getStudioImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex)

Get studio image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Studio name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.getStudioImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getStudioImage");
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
| **name** | **String**| Studio name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getStudioImageByIndex"></a>
# **getStudioImageByIndex**
> File getStudioImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get studio image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Studio name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.getStudioImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getStudioImageByIndex");
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
| **name** | **String**| Studio name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getUserImage"></a>
# **getUserImage**
> File getUserImage(userId, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex)

Get user profile image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.getUserImage(userId, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getUserImage");
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
| **userId** | **UUID**| User id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="getUserImageByIndex"></a>
# **getUserImageByIndex**
> File getUserImageByIndex(userId, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get user profile image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.getUserImageByIndex(userId, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#getUserImageByIndex");
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
| **userId** | **UUID**| User id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headArtistImage"></a>
# **headArtistImage**
> File headArtistImage(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get artist image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Artist name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.headArtistImage(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headArtistImage");
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
| **name** | **String**| Artist name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headGenreImage"></a>
# **headGenreImage**
> File headGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex)

Get genre image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Genre name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.headGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headGenreImage");
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
| **name** | **String**| Genre name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headGenreImageByIndex"></a>
# **headGenreImageByIndex**
> File headGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get genre image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Genre name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.headGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headGenreImageByIndex");
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
| **name** | **String**| Genre name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headItemImage"></a>
# **headItemImage**
> File headItemImage(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex)

Gets the item&#39;s image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.headItemImage(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headItemImage");
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
| **itemId** | **UUID**| Item id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **format** | [**ImageFormat**](.md)| Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headItemImage2"></a>
# **headItemImage2**
> File headItemImage2(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Gets the item&#39;s image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer imageIndex = 56; // Integer | Image index.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.headItemImage2(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headItemImage2");
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
| **itemId** | **UUID**| Item id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **maxWidth** | **Integer**| The maximum image width to return. | |
| **maxHeight** | **Integer**| The maximum image height to return. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [enum: Bmp, Gif, Jpg, Png, Webp] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | |
| **imageIndex** | **Integer**| Image index. | |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headItemImageByIndex"></a>
# **headItemImageByIndex**
> File headItemImageByIndex(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer)

Gets the item&#39;s image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.headItemImageByIndex(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headItemImageByIndex");
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
| **itemId** | **UUID**| Item id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **format** | [**ImageFormat**](.md)| Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headMusicGenreImage"></a>
# **headMusicGenreImage**
> File headMusicGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex)

Get music genre image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Music genre name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.headMusicGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headMusicGenreImage");
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
| **name** | **String**| Music genre name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headMusicGenreImageByIndex"></a>
# **headMusicGenreImageByIndex**
> File headMusicGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get music genre image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Music genre name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.headMusicGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headMusicGenreImageByIndex");
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
| **name** | **String**| Music genre name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headPersonImage"></a>
# **headPersonImage**
> File headPersonImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex)

Get person image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Person name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.headPersonImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headPersonImage");
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
| **name** | **String**| Person name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headPersonImageByIndex"></a>
# **headPersonImageByIndex**
> File headPersonImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get person image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Person name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.headPersonImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headPersonImageByIndex");
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
| **name** | **String**| Person name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headStudioImage"></a>
# **headStudioImage**
> File headStudioImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex)

Get studio image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Studio name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.headStudioImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headStudioImage");
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
| **name** | **String**| Studio name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headStudioImageByIndex"></a>
# **headStudioImageByIndex**
> File headStudioImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get studio image by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    String name = "name_example"; // String | Studio name.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.headStudioImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headStudioImageByIndex");
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
| **name** | **String**| Studio name. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headUserImage"></a>
# **headUserImage**
> File headUserImage(userId, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex)

Get user profile image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    Integer imageIndex = 56; // Integer | Image index.
    try {
      File result = apiInstance.headUserImage(userId, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headUserImage");
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
| **userId** | **UUID**| User id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |
| **imageIndex** | **Integer**| Image index. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="headUserImageByIndex"></a>
# **headUserImageByIndex**
> File headUserImageByIndex(userId, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer)

Get user profile image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Image index.
    String tag = "tag_example"; // String | Optional. Supply the cache tag from the item object to receive strong caching headers.
    ImageFormat format = ImageFormat.fromValue("Bmp"); // ImageFormat | Determines the output format of the image - original,gif,jpg,png.
    Integer maxWidth = 56; // Integer | The maximum image width to return.
    Integer maxHeight = 56; // Integer | The maximum image height to return.
    Double percentPlayed = 3.4D; // Double | Optional. Percent to render for the percent played overlay.
    Integer unplayedCount = 56; // Integer | Optional. Unplayed count overlay to render.
    Integer width = 56; // Integer | The fixed image width to return.
    Integer height = 56; // Integer | The fixed image height to return.
    Integer quality = 56; // Integer | Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
    Integer fillWidth = 56; // Integer | Width of box to fill.
    Integer fillHeight = 56; // Integer | Height of box to fill.
    Boolean cropWhitespace = true; // Boolean | Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
    Boolean addPlayedIndicator = true; // Boolean | Optional. Add a played indicator.
    Integer blur = 56; // Integer | Optional. Blur image.
    String backgroundColor = "backgroundColor_example"; // String | Optional. Apply a background color for transparent images.
    String foregroundLayer = "foregroundLayer_example"; // String | Optional. Apply a foreground layer on top of the image.
    try {
      File result = apiInstance.headUserImageByIndex(userId, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#headUserImageByIndex");
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
| **userId** | **UUID**| User id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Image index. | |
| **tag** | **String**| Optional. Supply the cache tag from the item object to receive strong caching headers. | [optional] |
| **format** | [**ImageFormat**](.md)| Determines the output format of the image - original,gif,jpg,png. | [optional] [enum: Bmp, Gif, Jpg, Png, Webp] |
| **maxWidth** | **Integer**| The maximum image width to return. | [optional] |
| **maxHeight** | **Integer**| The maximum image height to return. | [optional] |
| **percentPlayed** | **Double**| Optional. Percent to render for the percent played overlay. | [optional] |
| **unplayedCount** | **Integer**| Optional. Unplayed count overlay to render. | [optional] |
| **width** | **Integer**| The fixed image width to return. | [optional] |
| **height** | **Integer**| The fixed image height to return. | [optional] |
| **quality** | **Integer**| Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. | [optional] |
| **fillWidth** | **Integer**| Width of box to fill. | [optional] |
| **fillHeight** | **Integer**| Height of box to fill. | [optional] |
| **cropWhitespace** | **Boolean**| Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art. | [optional] |
| **addPlayedIndicator** | **Boolean**| Optional. Add a played indicator. | [optional] |
| **blur** | **Integer**| Optional. Blur image. | [optional] |
| **backgroundColor** | **String**| Optional. Apply a background color for transparent images. | [optional] |
| **foregroundLayer** | **String**| Optional. Apply a foreground layer on top of the image. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream returned. |  -  |
| **404** | Item not found. |  -  |

<a id="postUserImage"></a>
# **postUserImage**
> postUserImage(userId, imageType, index, body)

Sets the user image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User Id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | (Unused) Image type.
    Integer index = 56; // Integer | (Unused) Image index.
    File body = new File("/path/to/file"); // File | 
    try {
      apiInstance.postUserImage(userId, imageType, index, body);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#postUserImage");
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
| **userId** | **UUID**| User Id. | |
| **imageType** | [**ImageType**](.md)| (Unused) Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **index** | **Integer**| (Unused) Image index. | [optional] |
| **body** | **File**|  | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: image/*
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Image updated. |  -  |
| **403** | User does not have permission to delete the image. |  -  |
| **401** | Unauthorized |  -  |

<a id="postUserImageByIndex"></a>
# **postUserImageByIndex**
> postUserImageByIndex(userId, imageType, index, body)

Sets the user image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User Id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | (Unused) Image type.
    Integer index = 56; // Integer | (Unused) Image index.
    File body = new File("/path/to/file"); // File | 
    try {
      apiInstance.postUserImageByIndex(userId, imageType, index, body);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#postUserImageByIndex");
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
| **userId** | **UUID**| User Id. | |
| **imageType** | [**ImageType**](.md)| (Unused) Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **index** | **Integer**| (Unused) Image index. | |
| **body** | **File**|  | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: image/*
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Image updated. |  -  |
| **403** | User does not have permission to delete the image. |  -  |
| **401** | Unauthorized |  -  |

<a id="setItemImage"></a>
# **setItemImage**
> setItemImage(itemId, imageType, body)

Set item image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    File body = new File("/path/to/file"); // File | 
    try {
      apiInstance.setItemImage(itemId, imageType, body);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#setItemImage");
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
| **itemId** | **UUID**| Item id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **body** | **File**|  | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: image/*
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Image saved. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="setItemImageByIndex"></a>
# **setItemImageByIndex**
> setItemImageByIndex(itemId, imageType, imageIndex, body)

Set item image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | (Unused) Image index.
    File body = new File("/path/to/file"); // File | 
    try {
      apiInstance.setItemImageByIndex(itemId, imageType, imageIndex, body);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#setItemImageByIndex");
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
| **itemId** | **UUID**| Item id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| (Unused) Image index. | |
| **body** | **File**|  | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: image/*
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Image saved. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="updateItemImageIndex"></a>
# **updateItemImageIndex**
> updateItemImageIndex(itemId, imageType, imageIndex, newIndex)

Updates the index for an item image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    ImageType imageType = ImageType.fromValue("Primary"); // ImageType | Image type.
    Integer imageIndex = 56; // Integer | Old image index.
    Integer newIndex = 56; // Integer | New image index.
    try {
      apiInstance.updateItemImageIndex(itemId, imageType, imageIndex, newIndex);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#updateItemImageIndex");
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
| **itemId** | **UUID**| Item id. | |
| **imageType** | [**ImageType**](.md)| Image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageIndex** | **Integer**| Old image index. | |
| **newIndex** | **Integer**| New image index. | |

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
| **204** | Image index updated. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="uploadCustomSplashscreen"></a>
# **uploadCustomSplashscreen**
> uploadCustomSplashscreen(body)

Uploads a custom splashscreen.  The body is expected to the image contents base64 encoded.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageApi apiInstance = new ImageApi(defaultClient);
    File body = new File("/path/to/file"); // File | 
    try {
      apiInstance.uploadCustomSplashscreen(body);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageApi#uploadCustomSplashscreen");
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
| **body** | **File**|  | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: image/*
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Successfully uploaded new splashscreen. |  -  |
| **400** | Error reading MimeType from uploaded image. |  -  |
| **403** | User does not have permission to upload splashscreen.. |  -  |
| **401** | Unauthorized |  -  |

