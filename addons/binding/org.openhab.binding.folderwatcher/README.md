# FolderWatcher Binding

This binding is intended to monitor FTP folder and its subfolders and notify of new files

## Supported Things

Currently, the binding supports a single type of Thing, being the ftpfolder Thing.


## Thing Configuration

The thing has the following configuration options:

| Parameter   | Name         | Description                                                                                                            | Required | Default value |
|-------------|--------------|------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| ftpAddress  | FTP server   | IP address of FTP server                                                                                               | yes      | n/a           |
| ftpUsername | Username     | FTP user name                                                                                                          | yes      | n/a           |
| ftpPassword | Password     | FTP password                                                                                                           | yes      | n/a           |
| ftpDir      | RootDir      | Root directory to be watched                                                                                           | yes      | n/a           |
| listHidden  | List Hidden  | Allow listing of hidden files                                                                                          | yes      | false         |
| connectionTimeout | Connection timeout, s | Connection timeout for FTP request                                                                      | yes      | 30            |
| pollInterval | Polling interval, s | Interval for polling folder changes                                                                            | yes      | 60            |
| diffHours   | Time stamp difference, h | How many hours back to analyze                                                                             | yes      | 24            |


## Channels

This binding currently supports the following channels:

| Channel Type ID | Item Type    | Description                                                                            |
|-----------------|--------------|----------------------------------------------------------------------------------------|
| newftpfile | String       | A new file name discovered on FTP                                                      |


## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
