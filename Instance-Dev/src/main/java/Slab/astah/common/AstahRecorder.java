/**
 * Recorder.java
 * Created at 2012/05/11
 * Copyright(c) 2012 Yoshiaki Matsuzawa All Rights Reserved
 */
package Slab.astah.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author macchan
 * 
 */
public class AstahRecorder {

	private static AstahRecorder instance;

	public static AstahRecorder getInstance() {
		if (instance == null) {
			instance = new AstahRecorder();
		}
		return instance;
	}

	private final DateFormat formatter = new SimpleDateFormat(
			"yyyyMMddHHmmssSSS");

	public void record(String contents) {
		try {
			String date = formatter.format(new Date());
			String log = date + "\t" + contents;
			File f = getRecordFile();
			FileOutputStream fos = new FileOutputStream(f, true);
			PrintStream ps = new PrintStream(fos);
			ps.println(log);
			fos.close();
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
	}

	private File getRecordFile() {
		return new File(getRecordDirX(), "astah.log");
	}

	private File getRecordDirX() {
		File dir = new File(System.getProperty("user.home"), ".astahrecordX");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
}
