package org.forwoods.docuwiki.documentationWiki.jobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.forwoods.docuwiki.documentationWiki.core.SquadClassLoader;
import org.knowm.sundial.Job;
import org.knowm.sundial.annotations.CronTrigger;
import org.knowm.sundial.exceptions.JobInterruptException;

@CronTrigger(cron = "0 0 1 ? * MON")
//@SimpleTrigger(repeatInterval = 5, timeUnit = TimeUnit.SECONDS)
public class XMLDownloadJob extends Job{

	private static URL url;
	private static File docSaveLocation;
	private static SquadClassLoader squadLoader;

	@Override
	public void doRun() throws JobInterruptException {	
		squadLoader.clearCachedClasses();
		try (
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream fos = new FileOutputStream(docSaveLocation);
			) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static URL getUrl() {
		return url;
	}

	public static void setUrl(URL url) {
		XMLDownloadJob.url = url;
	}

	public static File getDocSaveLocation() {
		return docSaveLocation;
	}

	public static void setDocSaveLocation(File docSaveLocation) {
		XMLDownloadJob.docSaveLocation = docSaveLocation;
	}

	public static SquadClassLoader getSquadLoader() {
		return squadLoader;
	}

	public static void setSquadLoader(SquadClassLoader squadLoader) {
		XMLDownloadJob.squadLoader = squadLoader;
	}

}
