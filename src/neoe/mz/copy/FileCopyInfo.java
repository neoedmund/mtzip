package neoe.mz.copy;

import java.io.File;

public class FileCopyInfo {

	public FileCopyInfo(File from, File to, long len) {
		this.from = from;
		this.to = to;
		this.length = len;
	}

	public File from, to;
	public long length;

}
