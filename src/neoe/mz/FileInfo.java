package neoe.mz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

import neoe.util.U;

public class FileInfo {

	public String pathname;

	public String link;
//	public String attr;
//	public long time;

	public long fsize;
	public File f;
	public int type;
	public long len;
	public long start;

	public static String getAttr(File f) throws IOException {
		if (C.isWindows)
			return "";
		return PosixFilePermissions.toString(Files.getPosixFilePermissions(f.toPath()));
	}

	public static void setAttr(File f, String attr) throws IOException {
		if (C.isWindows || attr == null || attr.isEmpty())
			return;
		Files.setPosixFilePermissions(f.toPath(), PosixFilePermissions.fromString(attr));
	}

	public FileInfo(int type, File f, long fsize, String pathname) throws IOException {
		this.type = type;
		this.pathname = pathname;
		this.f = f;
		if (type == 0) {// single file
			this.fsize = fsize;
		} else if (type == 3 || type == 4) {// SymbolicLink. hard link
		} else {
			U.bug();
		}
	}

	public FileInfo(int type, File f, long start, long len, long fsize, String pathname) throws IOException {
		this.type = type;
		this.pathname = pathname;
		if (type == 1) {// file part
			this.f = f;
			this.fsize = fsize;
			this.start = start;
			this.len = len;
		} else {
			U.bug();
		}

	}

	public FileInfo(int type, File f, String dir) {
		if (type != 2)
			U.bug();
		this.type = type;
		this.f = f;
		this.pathname = dir;
	}

}
