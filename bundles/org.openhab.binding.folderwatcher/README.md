# FolderWatcher Binding

This binding is intended to monitor a local folder, FTP and S3 bucket and their subfolders and notify of new files.

## Supported Things

The binding support three types of things: `localfolder`, `ftpfolder` and `s3bucket`.

## Thing Configuration

The `localfolder` thing has the following configuration options:

| Parameter          | Name                        | Description                         | Required | Default value |
| ------------------ | --------------------------- | ----------------------------------- | -------- | ------------- |
| localDir           | Local Directory             | Local directory to be watched       | yes      | n/a           |
| listHiddenLocal    | List Hidden                 | Allow listing of hidden files       | yes      | No            |
| pollIntervalLocal  | Polling interval in seconds | Interval for polling folder changes | yes      | 60            |
| listRecursiveLocal | List Sub Folders            | Allow listing of sub folders        | yes      | No            |

The `ftpfolder` thing has the following configuration options:

| Parameter         | Name                           | Description                         | Required | Default value |
| ----------------- | ------------------------------ | ----------------------------------- | -------- | ------------- |
| ftpAddress        | FTP server                     | IP address of FTP server            | yes      | n/a           |
| ftpPort           | FTP port                       | Port of FTP server                  | yes      | 21            |
| secureMode        | FTP Security                   | FTP Security                        | yes      | None          |
| ftpUsername       | Username                       | FTP user name                       | yes      | n/a           |
| ftpPassword       | Password                       | FTP password                        | yes      | n/a           |
| ftpDir            | RootDir                        | Root directory to be watched        | yes      | n/a           |
| listRecursiveFtp  | List Sub Folders               | Allow listing of sub folders        | yes      | No            |
| listHidden        | List Hidden                    | Allow listing of hidden files       | yes      | false         |
| connectionTimeout | Connection timeout in seconds  | Connection timeout for FTP request  | yes      | 30            |
| pollInterval      | Polling interval in seconds    | Interval for polling folder changes | yes      | 60            |
| diffHours         | Time stamp difference in hours | How many hours back to analyze      | yes      | 24            |

The `s3bucket` thing has the following configuration options:

| Parameter      | Name                 | Description                                        | Required | Default value |
|----------------|----------------------|----------------------------------------------------|----------|---------------|
| s3BucketName   | S3 Bucket Name       | Name of the S3 bucket to be watched                | yes      | n/a           |
| s3Path         | S3 Path              | S3 path (folder) to be monitored                   | no       | n/a           |
| pollIntervalS3 | Polling Interval     | Interval for polling S3 bucket changes, in seconds | yes      | 60            |
| awsKey         | AWS Access Key       | AWS access key                                     | no       | n/a           |
| awsSecret      | AWS Secret           | AWS secret                                         | no       | n/a           |
| awsRegion      | AWS Region           | AWS region of S3 bucket                            | yes      | ""            |
| s3Anonymous    | Anonymous Connection | Connect anonymously (works for public buckets)     | yes      | true          |
## Events

This binding supports the following event:

| Channel Type ID | Item Type | Description                |
|-----------------|-----------|----------------------------|
| newfile         | String    | A new file name discovered |

## Full Example

Thing configuration:

```java
folderwatcher:localfolder:myLocalFolder [ localDir="/myfolder", pollIntervalLocal=60, listHiddenLocal="false", listRecursiveLocal="false" ]
folderwatcher:ftpfolder:myLocalFolder   [ ftpAddress="X.X.X.X", ftpPort=21, secureMode="EXPLICIT", ftpUsername="username", ftpPassword="password", ftpDir="/myfolder/",  listHidden="true", listRecursiveFtp="true", connectionTimeout=33, pollInterval=66, diffHours=25 ]
folderwatcher:s3bucket:myS3bucket       [ s3BucketName="mypublic-bucket", pollIntervalS3=60, awsRegion="us-west-1", s3Anonymous="true" ]

```

### Using in a rule:

Local folder example:

```java
rule "New Local file"
when
    Channel "folderwatcher:localfolder:myLocalFolder:newfile" triggered
then
    logInfo("NewLocalFile", receivedEvent.toString())
end
```

FTP example:

```java
rule "New FTP file"
when
    Channel "folderwatcher:ftpfolder:myFTPFolder:newfile" triggered
then
    logInfo("NewFTPFile", receivedEvent.toString())
end
```

S3 bucket example:

```java
rule "New S3 file"
when
    Channel "folderwatcher:s3bucket:myS3bucket:newfile" triggered
then
    logInfo("NewS3File", receivedEvent.toString())
end
```
