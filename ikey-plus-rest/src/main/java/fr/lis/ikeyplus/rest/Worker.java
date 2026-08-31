package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.util.Date;

/**
 * this class allow to delete old generated key files
 *
 * @author Thomas burguiere
 */
public class Worker implements Job {

    public void execute(final JobExecutionContext context) throws JobExecutionException {

        final String path = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");
        System.out.println("Deleting the content of " + path);
        final File generatedKeyFilesFolder = new File(path);
        if (generatedKeyFilesFolder.exists()) {
            for (final String filePath : generatedKeyFilesFolder.list()) {
                final File file2delete = new File(generatedKeyFilesFolder, filePath);
                // 2592000*1000 is the number of millisecond for 30 days.
                final long monthMilliseconds = (long) ((long) Long.parseLong(IkeyConfig
                        .getBundleConfOverridableElement("generatedKeyFiles.delete.period")) * (long) 1000);
                if (file2delete.lastModified() < (new Date().getTime() - monthMilliseconds)) {
                    // delete old files
                    file2delete.delete();
                }
            }
        }
    }
}
