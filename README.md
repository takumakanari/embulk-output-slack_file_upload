# Slack File Upload file output plugin for Embulk

Upload files to Slack.

## Overview

* **Plugin type**: file output
* **Load all or nothing**: no
* **Resume supported**: yes
* **Cleanup supported**: yes

## Configuration

- **file_name_prefix**: File name prefix of the upload files (string, required)
- **sequence_format**: Format of the sequence number of the output files (integer, default: `%03d.%02d`)
- **file_ext**: Path suffix of the output files (e.g. "csv") (string, required)
- **api_token**: Token of Slack API (string, required)
- **channels**: The list of channels to upload (array, required)
- **title**: Title to upload (array, default: `null`)
- **min_lines**: When set, file will be uploaded only if total lines of each file >= min_lines (array, default: `null`)

## Example

```yaml
out:
  type: slack_file_upload
  file_name_prefix: report
  file_ext: csv
  api_token: abcdefghijklmn0123456789
  channels: [room1, room2]
  title: daily report
  min_lines: 1
  formatter:
    type: csv
    header_line: true
    charset: UTF-8
    newline: CRLF
```


## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```
