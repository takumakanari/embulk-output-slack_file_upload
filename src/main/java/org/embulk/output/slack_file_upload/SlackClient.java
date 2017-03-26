package org.embulk.output.slack_file_upload;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.request.files.FilesUploadRequest;
import com.github.seratch.jslack.api.methods.response.files.FilesUploadResponse;
import com.google.common.base.Optional;
import org.embulk.spi.Exec;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class SlackClient
{
    private static final Slack SLACK = Slack.getInstance();

    private final Logger log = Exec.getLogger(getClass());

    private final String apiToken;

    public SlackClient(String apiToken)
    {
        this.apiToken = apiToken;
    }

    public com.github.seratch.jslack.api.model.File fileUpload(File file, Optional<String> title, List<String> channels)
    {
        final String destTitle = title.or(file.getName());
        log.info(String.format(Locale.ENGLISH, "Uploading '%s'(%s) -> %s", destTitle, file,
                channels));

        FilesUploadRequest req = FilesUploadRequest.builder()
                .file(file)
                .title(destTitle)
                .token(apiToken)
                .filename(file.getName())
                .channels(channels).build();

        final FilesUploadResponse response;
        try {
           response = SLACK.methods().filesUpload(req);
        }
        catch (Exception e) {
            throw new SlackClientRuntimeException(e);
        }

        if (!response.isOk()) {
            throw new SlackClientRuntimeException(String.format(Locale.ENGLISH,
                    "Got error from Slack API, error='%s', warning='%s'", response.getError(), response.getWarning()));
        }

        return response.getFile();
    }

    public static class SlackClientRuntimeException extends RuntimeException
    {
        public SlackClientRuntimeException(Throwable cause)
        {
            super(cause);
        }

        public SlackClientRuntimeException(String cause)
        {
            super(cause);
        }
    }
}
