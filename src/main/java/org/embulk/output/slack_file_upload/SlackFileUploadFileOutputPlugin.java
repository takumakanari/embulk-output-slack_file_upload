package org.embulk.output.slack_file_upload;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigSource;
import org.embulk.spi.Exec;
import org.embulk.spi.FileOutputPlugin;
import org.embulk.standards.LocalFileOutputPlugin;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.nio.file.Paths;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;

public class SlackFileUploadFileOutputPlugin
        extends LocalFileOutputPlugin
{
    private final Logger log = Exec.getLogger(getClass());

    public interface PluginTask
            extends LocalFileOutputPlugin.PluginTask
    {
        String getPathPrefix();

        void setPathPrefix(String pathPrefix);

        @Config("file_name_prefix")
        String getFileNamePrefix();

        @Config("api_token")
        String getApiToken();

        @Config("channels")
        List<String> getChannels();

        @Config("title")
        @ConfigDefault("null")
        Optional<String> getTitle();

        @Config("min_lines")
        @ConfigDefault("null")
        Optional<Integer> getMinLines();
    }

    @Override
    public ConfigDiff transaction(ConfigSource config, int taskCount,
            FileOutputPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);

        try {
            String.format(Locale.ENGLISH, task.getSequenceFormat(), 0, 0);
        } catch (IllegalFormatException ex) {
            throw new ConfigException("Invalid sequence_format: parameter for file output plugin",
                    ex);
        }
        task.setPathPrefix(Paths.get(Files.createTempDir().getAbsolutePath(),
                new File(task.getFileNamePrefix()).getName()).toFile().getAbsolutePath());

        ConfigDiff configDiff = resume(task.dump(), taskCount, control);

        doUpload(task);

        return configDiff;
    }


    protected void doUpload(PluginTask task)
    {
        final File rootDir = Paths.get(task.getPathPrefix()).getParent().toFile();
        if (!rootDir.isDirectory()) {
            throw new IllegalStateException(String.format(Locale.ENGLISH,
                    "'%s' is not a directory", rootDir));
        }

        beforeUpload(rootDir);

        final SlackClient client = new SlackClient(task.getApiToken());
        for (File f : rootDir.listFiles()) {
            if (task.getMinLines().isPresent() && !isUploadable(f, task.getMinLines().get())) {
                log.info(String.format(Locale.ENGLISH, "'%s' doesn't meet the min lines to upload", f.getName()));
                continue;
            }
            // TODO retry
            client.fileUpload(f, task.getTitle(), task.getChannels());
        }
    }

    protected void beforeUpload(File rootDir)
    {
        rootDir.deleteOnExit();
        for (File f : rootDir.listFiles()) {
            f.deleteOnExit();
        }
    }

    private boolean isUploadable(File target, int minLines)
    {
        try (FileReader fr = new FileReader(target); LineNumberReader lnr = new LineNumberReader(fr) ) {
            int c = 1;
            while(lnr.readLine() != null) {
                if (c >= minLines) {
                    return true;
                }
                c++;
            }
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return false;
    }
}
