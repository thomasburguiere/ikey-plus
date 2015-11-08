package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

/**
 * this class allow to delete old generated key files
 *
 * @author Thomas burguiere
 */
public class Worker implements Job {

    public void execute(JobExecutionContext context) throws JobExecutionException {

        String path = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");
        System.out.println("Deleting the content of " + path);
        File generatedKeyFilesFolder = new File(path);
        if (generatedKeyFilesFolder.exists()) {
            final String[] fileList = generatedKeyFilesFolder.list();
            if (fileList != null) {
                for (String filePath : fileList) {
                    File file2delete = new File(generatedKeyFilesFolder, filePath);
                    // 2592000*1000 is the number of millisecond for 30 days.
                    long monthMilliseconds = Long.parseLong(IkeyConfig
                            .getBundleConfOverridableElement("generatedKeyFiles.delete.period")) * (long) 1000;
                    if (file2delete.lastModified() < (new Date().getTime() - monthMilliseconds)) {
                        // delete old files
                        try {
                            Files.delete(file2delete.toPath());
                        } catch (IOException e) {
                            throw new JobExecutionException(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }
}
