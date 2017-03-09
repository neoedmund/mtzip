package neoe.mz.copy;

import java.io.File;
import java.nio.file.Files;

import neoe.mz.FileInfo;
import neoe.util.FileIterator;
import neoe.util.WorkRoom;

public class Copyer {
	public boolean finished;
	private long totalBytes, doneBytes;
	public int totalFile;
	public int totalDir;
	public long timeStart;
	private final Object lock1 = new Object();
	private final Object lock2 = new Object();

	public long getTotalBytes() {
		synchronized (lock1) {
			return totalBytes;
		}
	}

	public long getDoneBytes() {
		synchronized (lock2) {
			return doneBytes;
		}
	}

	private void addTotalBytes(long s) {
		synchronized (lock1) {
			totalBytes += s;
		}
	}

	public void addDoneBytes(long s) {
		synchronized (lock2) {
			doneBytes += s;
		}
	}

	public void run(String dir, String fn, int fileWorkerCnt, int copyWorkerCnt) throws Exception {
		try {
			System.out.println(
					String.format("copy  %s to %s using %s reader %s writer", dir, fn, fileWorkerCnt, copyWorkerCnt));
			timeStart = System.currentTimeMillis();
			WorkRoom copyRoom = new WorkRoom(copyWorkerCnt, FileWriteWorker.class, 1, "write",
					new Object[] { Copyer.this });
			WorkRoom fileRoom = new WorkRoom(fileWorkerCnt, FileReadWorker.class, 300, "read",
					new Object[] { copyRoom });

			FileIterator fi = new FileIterator(dir, null);
			String baseDir = new File(dir).getCanonicalPath();
			totalDir = 0;
			totalFile = 0;
			totalBytes = 0;
			for (File f : fi) {
				if (Files.isSymbolicLink(f.toPath())) {
					debug("skip SymbolicLink:" + f.getAbsolutePath());
					continue;
				}
				String fname = f.getCanonicalPath();
				if (!fname.startsWith(baseDir)) {
					debug("skip Strange Path:" + fname);
					continue;
				}
				String relpath = fname.substring(baseDir.length());
				if (f.isDirectory()) {
					new File(fn, relpath).mkdirs();
					totalDir++;
					continue;
				}
				long fsize = f.length();
				totalFile++;
				addTotalBytes(fsize);
				fileRoom.submit(new FileCopyInfo(f, new File(fn, relpath), fsize));
			}

			fileRoom.close();
			fileRoom.waitFinish();
			copyRoom.close();
			copyRoom.waitFinish();

			long used = System.currentTimeMillis() - timeStart;
			System.out.println(
					"finish in " + used + " ms. dirs:" + totalDir + ", files:" + totalFile + ", bytes:" + totalBytes);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			finished = true;
		}
	}

	private void debug(String msg) {
		System.out.println(msg);
	}
}
