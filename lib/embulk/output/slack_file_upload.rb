Embulk::JavaPlugin.register_output(
  "slack_file_upload", "org.embulk.output.slack_file_upload.SlackFileUploadFileOutputPlugin",
  File.expand_path('../../../../classpath', __FILE__))
