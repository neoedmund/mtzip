package neoe.mz;

import java.io.File;

import neoe.util.U;

public class FileInfo {

	public String pathname;
	public long fsize;
	public File f;
	public int type;
	public long len;
	public long start;

	public FileInfo(int type, File f, long fsize, String pathname) {
		if (type != 0)
			U.bug();
		this.type = type;
		this.f = f;
		this.fsize = fsize;
		this.pathname = pathname;
	}

	public FileInfo(int type, File f, long start, long len, long fsize, String pathname) {
		if (type != 1)
			U.bug();
		this.type = type;
		this.f = f;
		this.fsize = fsize;
		this.pathname = pathname;
		this.start = start;
		this.len = len;
	}

	public FileInfo(int type, String dir) {
		if (type != 2)
			U.bug();
		this.type = type;
		this.pathname = dir;
	}

}
