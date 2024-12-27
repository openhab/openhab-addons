

# ServerConfiguration

Represents the server configuration.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**logFileRetentionDays** | **Integer** | Gets or sets the number of days we should retain log files. |  [optional] |
|**isStartupWizardCompleted** | **Boolean** | Gets or sets a value indicating whether this instance is first run. |  [optional] |
|**cachePath** | **String** | Gets or sets the cache path. |  [optional] |
|**previousVersion** | **String** | Gets or sets the last known version that was ran using the configuration. |  [optional] |
|**previousVersionStr** | **String** | Gets or sets the stringified PreviousVersion to be stored/loaded,  because System.Version itself isn&#39;t xml-serializable. |  [optional] |
|**enableMetrics** | **Boolean** | Gets or sets a value indicating whether to enable prometheus metrics exporting. |  [optional] |
|**enableNormalizedItemByNameIds** | **Boolean** |  |  [optional] |
|**isPortAuthorized** | **Boolean** | Gets or sets a value indicating whether this instance is port authorized. |  [optional] |
|**quickConnectAvailable** | **Boolean** | Gets or sets a value indicating whether quick connect is available for use on this server. |  [optional] |
|**enableCaseSensitiveItemIds** | **Boolean** | Gets or sets a value indicating whether [enable case sensitive item ids]. |  [optional] |
|**disableLiveTvChannelUserDataName** | **Boolean** |  |  [optional] |
|**metadataPath** | **String** | Gets or sets the metadata path. |  [optional] |
|**metadataNetworkPath** | **String** |  |  [optional] |
|**preferredMetadataLanguage** | **String** | Gets or sets the preferred metadata language. |  [optional] |
|**metadataCountryCode** | **String** | Gets or sets the metadata country code. |  [optional] |
|**sortReplaceCharacters** | **List&lt;String&gt;** | Gets or sets characters to be replaced with a &#39; &#39; in strings to create a sort name. |  [optional] |
|**sortRemoveCharacters** | **List&lt;String&gt;** | Gets or sets characters to be removed from strings to create a sort name. |  [optional] |
|**sortRemoveWords** | **List&lt;String&gt;** | Gets or sets words to be removed from strings to create a sort name. |  [optional] |
|**minResumePct** | **Integer** | Gets or sets the minimum percentage of an item that must be played in order for playstate to be updated. |  [optional] |
|**maxResumePct** | **Integer** | Gets or sets the maximum percentage of an item that can be played while still saving playstate. If this percentage is crossed playstate will be reset to the beginning and the item will be marked watched. |  [optional] |
|**minResumeDurationSeconds** | **Integer** | Gets or sets the minimum duration that an item must have in order to be eligible for playstate updates.. |  [optional] |
|**minAudiobookResume** | **Integer** | Gets or sets the minimum minutes of a book that must be played in order for playstate to be updated. |  [optional] |
|**maxAudiobookResume** | **Integer** | Gets or sets the remaining minutes of a book that can be played while still saving playstate. If this percentage is crossed playstate will be reset to the beginning and the item will be marked watched. |  [optional] |
|**libraryMonitorDelay** | **Integer** | Gets or sets the delay in seconds that we will wait after a file system change to try and discover what has been added/removed  Some delay is necessary with some items because their creation is not atomic.  It involves the creation of several  different directories and files. |  [optional] |
|**imageSavingConvention** | **ImageSavingConvention** | Gets or sets the image saving convention. |  [optional] |
|**metadataOptions** | [**List&lt;MetadataOptions&gt;**](MetadataOptions.md) |  |  [optional] |
|**skipDeserializationForBasicTypes** | **Boolean** |  |  [optional] |
|**serverName** | **String** |  |  [optional] |
|**uiCulture** | **String** |  |  [optional] |
|**saveMetadataHidden** | **Boolean** |  |  [optional] |
|**contentTypes** | [**List&lt;NameValuePair&gt;**](NameValuePair.md) |  |  [optional] |
|**remoteClientBitrateLimit** | **Integer** |  |  [optional] |
|**enableFolderView** | **Boolean** |  |  [optional] |
|**enableGroupingIntoCollections** | **Boolean** |  |  [optional] |
|**displaySpecialsWithinSeasons** | **Boolean** |  |  [optional] |
|**codecsUsed** | **List&lt;String&gt;** |  |  [optional] |
|**pluginRepositories** | [**List&lt;RepositoryInfo&gt;**](RepositoryInfo.md) |  |  [optional] |
|**enableExternalContentInSuggestions** | **Boolean** |  |  [optional] |
|**imageExtractionTimeoutMs** | **Integer** |  |  [optional] |
|**pathSubstitutions** | [**List&lt;PathSubstitution&gt;**](PathSubstitution.md) |  |  [optional] |
|**enableSlowResponseWarning** | **Boolean** | Gets or sets a value indicating whether slow server responses should be logged as a warning. |  [optional] |
|**slowResponseThresholdMs** | **Long** | Gets or sets the threshold for the slow response time warning in ms. |  [optional] |
|**corsHosts** | **List&lt;String&gt;** | Gets or sets the cors hosts. |  [optional] |
|**activityLogRetentionDays** | **Integer** | Gets or sets the number of days we should retain activity logs. |  [optional] |
|**libraryScanFanoutConcurrency** | **Integer** | Gets or sets the how the library scan fans out. |  [optional] |
|**libraryMetadataRefreshConcurrency** | **Integer** | Gets or sets the how many metadata refreshes can run concurrently. |  [optional] |
|**removeOldPlugins** | **Boolean** | Gets or sets a value indicating whether older plugins should automatically be deleted from the plugin folder. |  [optional] |
|**allowClientLogUpload** | **Boolean** | Gets or sets a value indicating whether clients should be allowed to upload logs. |  [optional] |



