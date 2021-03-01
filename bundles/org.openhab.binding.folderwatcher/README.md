# FolderWatcher Binding

This binding is intended to monitor FTP and local folder and its subfolders and notify of new files

## Supported Things

Currently the binding support two types of things: `ftpfolder` and `localfolder`.


## Thing Configuration

The `ftpfolder` thing has the following configuration options:

| Parameter   | Name         | Description                                                                                                            | Required | Default value |
|-------------|--------------|------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| ftpAddress  | FTP server   | IP address of FTP server                                                                                               | yes      | n/a           |
| ftpPort     | FTP port   | Port of FTP server                                                                                                       | yes      | 21            |
| secureMode  | FTP Security | FTP Security                                                                                                           | yes      | None          |
| ftpUsername | Username     | FTP user name                                                                                                          | yes      | n/a           |
| ftpPassword | Password     | FTP password                                                                                                           | yes      | n/a           |
| ftpDir      | RootDir      | Root directory to be watched                                                                                           | yes      | n/a           |
| listRecursiveFtp | List Sub Folders | Allow listing of sub folders                                                                                  | yes      | No            |
| listHidden  | List Hidden  | Allow listing of hidden files                                                                                          | yes      | false         |
| connectionTimeout | Connection timeout, s | Connection timeout for FTP request                                                                      | yes      | 30            |
| pollInterval | Polling interval, s | Interval for polling folder changes                                                                            | yes      | 60            |
| diffHours   | Time stamp difference, h | How many hours back to analyze                                                                             | yes      | 24            |

The `localfolder` thing has the following configuration options:

| Parameter   | Name         | Description                                                                                                            | Required | Default value |
|-------------|--------------|------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| localDir    | Local Directory | Local directory to be watched                                                                                       | yes      | n/a           |
| listHiddenLocal | List Hidden | Allow listing of hidden files                                                                                       | yes      | No            |
| pollIntervalLocal | Polling interval, s | Interval for polling folder changes                                                                       | yes      | 60            |
| listRecursiveLocal | List Sub Folders | Allow listing of sub folders                                                                                | yes      | No            |

## Events

This binding currently supports the following events:

| Channel Type ID | Item Type    | Description                                                                            |
|-----------------|--------------|----------------------------------------------------------------------------------------|
| newftpfile | String       | A new file name discovered on FTP                                                      |
| newlocalfile | String       | A new file name discovered on in local folder                                                      |


## Full Example

Thing configuration:

```java
folderwatcher:localfolder:myLocalFolder [ localDir="/myfolder", pollIntervalLocal=60, listHiddenLocal="false", listRecursiveLocal="false" ]
folderwatcher:ftpfolder:myLocalFolder [ ftpAddress="X.X.X.X", ftpPort=21, secureMode="EXPLICIT", ftpUsername="username", ftpPassword="password",ftpDir="/myfolder/",listHidden="true",listRecursiveFtp="true",connectionTimeout=33,pollInterval=66,diffHours=25]
```

### Using in a rule:

FTP example:

```java
rule "New FTP file"
when 
    Channel 'folderwatcher:ftpfolder:XXXXX:newfile' triggered
then

    logInfo('NewFTPFile', receivedEvent.toString())

end
```

Local folder example:

```java
rule "New Local file"
when 
    Channel 'folderwatcher:localfolder:XXXXX:newfile' triggered
then

    logInfo('NewLocalFile', receivedEvent.toString())

end
```
