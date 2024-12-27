

# QuickConnectResult

Stores the state of an quick connect request.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**authenticated** | **Boolean** | Gets or sets a value indicating whether this request is authorized. |  [optional] |
|**secret** | **String** | Gets the secret value used to uniquely identify this request. Can be used to retrieve authentication information. |  [optional] |
|**code** | **String** | Gets the user facing code used so the user can quickly differentiate this request from others. |  [optional] |
|**deviceId** | **String** | Gets the requesting device id. |  [optional] |
|**deviceName** | **String** | Gets the requesting device name. |  [optional] |
|**appName** | **String** | Gets the requesting app name. |  [optional] |
|**appVersion** | **String** | Gets the requesting app version. |  [optional] |
|**dateAdded** | **OffsetDateTime** | Gets or sets the DateTime that this request was created. |  [optional] |



