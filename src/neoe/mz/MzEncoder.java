package neoe.mz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import neoe.util.FileIterator;
import neoe.util.U;
import neoe.util.WorkRoom;

public class MzEncoder {

	/**
	 * @param args
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		new MzEncoder().run(args[0], args[1], args.length > 2 ? Integer.parseInt(args[2]) : 1,
				args.length > 3 ? Integer.parseInt(args[3]) : 1);
		// new ConcatFile().run(args[1]);
	}

	final static int BLOCK_SIZE = 1 * 1024 * 1024;// according to Compression
													// Ratio vs. Chunk Size,
													// size should not be too
													// large
	public boolean finished;
	private long totalBytes, doneBytes;
	public int totalFile;
	public int totalSoftLink, totalHardLink;
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

	public void run(String dir, String fn, int fileWorkerCnt, int encodeWorkerCnt) throws Exception {
		try {
			System.out.println(String.format("encode %s to %s using %s fileworkers %s encodeworkers", dir, fn,
					fileWorkerCnt, encodeWorkerCnt));
			timeStart = System.currentTimeMillis();
			deleteTargetFiles(fn);
			WorkRoom encodeRoom = new WorkRoom(encodeWorkerCnt, EncodeWorker.class, 1, "EncodeRoom",
					new Object[] { fn, this });
			WorkRoom fileRoom = new WorkRoom(fileWorkerCnt, FileWorker.class, 300, "FileRoom",
					new Object[] { encodeRoom, BLOCK_SIZE });

			FileIterator fi = new FileIterator(dir, null);
			String baseDir = new File(dir).getAbsolutePath();
			boolean isSingleFile = new File(dir).isFile();
			totalDir = 0;
			totalFile = 0;
			totalBytes = 0;
			for (File f : fi) {
				if (Files.isSymbolicLink(f.toPath())) {
					String relpath = f.getParentFile().getAbsolutePath().substring(baseDir.length()) + "/"
							+ f.getName();
					fileRoom.submit(new FileInfo(3, f, 0, relpath));
					totalSoftLink++;
					continue;
				}
				String fname = f.getAbsolutePath();
				String relpath = isSingleFile ? f.getName() : fname.substring(baseDir.length());

				if (!fname.startsWith(baseDir)) {
					// hard link
					String relpath2 = f.getParentFile().getAbsolutePath().substring(baseDir.length()) + "/"
							+ f.getName();
					fileRoom.submit(new FileInfo(4, f, 0, relpath2));
					totalHardLink++;
					continue;
				}

				if (f.isDirectory()) {
					fileRoom.submit(new FileInfo(2, f, relpath));
					totalDir++;
					continue;
				}
				long fsize = f.length();
				totalFile++;
				addTotalBytes(fsize);
				if (fsize <= BLOCK_SIZE) {
					fileRoom.submit(new FileInfo(0, f, fsize, relpath));
				} else {
					long cnt = fsize / BLOCK_SIZE;

					for (long i = 0; i < cnt; i++) {
						fileRoom.submit(new FileInfo(1, f, BLOCK_SIZE * i, BLOCK_SIZE, fsize,
								fname.substring(baseDir.length())));
					}
					if (fsize % BLOCK_SIZE > 0) {
						fileRoom.submit(new FileInfo(1, f, BLOCK_SIZE * cnt, fsize - BLOCK_SIZE * cnt, fsize, relpath));
					}
				}
			}

			fileRoom.close();
			fileRoom.waitFinish();
			encodeRoom.close();
			encodeRoom.waitFinish();

			long used = System.currentTimeMillis() - timeStart;
			System.out.printf("finish in %,d ms. dirs: %,d, files: %,d, bytes: %,d, soft links: %,d, hard links: %,d\n",
					used, totalDir, totalFile, totalBytes, totalSoftLink, totalHardLink);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			finished = true;
		}
	}

	private void deleteTargetFiles(String fn) {
		File f = new File(fn);
		String fn1_1 = f.getName() + ".-";
		File dir = U.confirmDir(f);
		if (dir == null) {
			dir = new File(".");
		} else {
			dir.mkdirs();
		}
		List<File> sub = new ArrayList<File>();
		for (File f2 : dir.listFiles()) {
			String fn2 = f2.getName();
			if (fn2.startsWith(fn1_1)) {
				sub.add(f2);
			}
		}
		System.out.println(String.format("find %d files to delete:", sub.size()));
		for (File f2 : sub) {
			System.out.println(f2.getName());
			f2.delete();
		}
	}

	private void debug(String msg) {
		System.out.println(msg);
	}

}
