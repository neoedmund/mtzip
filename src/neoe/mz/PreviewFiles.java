package neoe.mz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import neoe.util.FileIterator;
import neoe.util.U;

public class PreviewFiles {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new PreviewFiles().run(args[0]);

	}

	public boolean finished;
	public int totalDir = 0;
	public int totalFile = 0;
	public long totalBytes = 0;

	public synchronized long getTotalBytes() {
		return totalBytes;
	}

	private synchronized void addTotalBytes(long s) {
		totalBytes += s;
	}

	public Object[] run(String dir) throws IOException {
		System.out.println("scanning " + dir);
		long t1 = System.currentTimeMillis();
		FileIterator fi = new FileIterator(dir, null);
		String baseDir = new File(dir).getCanonicalPath();

		for (File f : fi) {
			if (Files.isSymbolicLink(f.toPath())) {
				U.debug("skip SymbolicLink:" + f.getAbsolutePath());
				continue;
			}
			String fname = f.getCanonicalPath();
			if (!fname.startsWith(baseDir)) {
				U.debug("skip Strange Path:" + fname);
				continue;
			}
			// String relpath = fname.substring(baseDir.length());
			if (f.isDirectory()) {
				totalDir++;
				continue;
			}
			long fsize = f.length();
			totalFile++;
			addTotalBytes(fsize);
		}

		long used = System.currentTimeMillis() - t1;
		System.out.println("scan finished in " + used + " ms. dirs:" + totalDir + ", files:" + totalFile + ", bytes:"
				+ totalBytes);
		finished = true;
		return new Object[] { totalDir, totalFile, totalBytes };

	}
}
