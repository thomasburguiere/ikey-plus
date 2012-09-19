import java.io.File;
import java.util.Date;

import main.java.utils.Utils;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * this class allow to delete old generated key files
 * 
 * @author Thomas burguiere
 * 
 */
public class Worker implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		String path = Utils.getBundleConfOverridableElement("generatedKeyFiles.prefix")
				+ Utils.getBundleConfOverridableElement("generatedKeyFiles.folder");
		System.out.println("Deleting the content of " + path);
		File generatedKeyFilesFolder = new File(path);
		if (generatedKeyFilesFolder.exists()) {
			for (String filePath : generatedKeyFilesFolder.list()) {
				File file2delete = new File(generatedKeyFilesFolder, filePath);
				long monthMilliseconds = (long) ((long) Long.parseLong(Utils
						.getBundleConfOverridableElement("generatedKeyFiles.delete.period")) * (long) 1000);
				if (file2delete.lastModified() < (new Date().getTime() - monthMilliseconds)) {
					// delete old files
					file2delete.delete();
				}
			}
		}
	}
}
