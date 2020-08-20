package neoe.mz;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import neoe.util.U;

public class MzDecoder {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		new MzDecoder().run(args[0], args[1]);

	}

	long totalfile, totaldir, totalpartfile, totalbs, archive, totalSoftLink, totalHardLink;
	private File dir;
	boolean finished;
	protected String extraFn;
	long t1;
	boolean pause;
	List links = Collections.synchronizedList(new ArrayList());
	List dirTimes = Collections.synchronizedList(new ArrayList());

	public void run(String fn, final String outDir) throws Exception {
		try {
			File f = new File(fn);
			if (f.isFile() && f.exists()) {
				run0(fn, outDir);
			} else {
				String name = f.getName();
				String name2 = name + ".-";
				File dir = U.confirmDir(f);
				File[] fs = dir.listFiles();
				Exception[] err = new Exception[1];
				if (fs != null) {
					List<Thread> ts = new ArrayList<Thread>();
					int i = 0;
					for (File fx : fs) {
						String name1 = fx.getName();
						if (name1.startsWith(name2)) {
							final String fxname = fx.getAbsolutePath();
							Thread t;
							ts.add(t = new Thread() {
								public void run() {
									try {
										run0(fxname, outDir);
									} catch (IOException e) {
										e.printStackTrace();
										err[0] = e;
									}
								}
							});
							t.start();
							i++;
						}
					}
					if (i == 0) {
						System.out.println("Archive file(s) not found.");
					} else {
						for (Thread t : ts) {
							t.join();
						}
						if (err[0] != null) {
							throw new RuntimeException("something wrong:", err[0]);
						}
					}
				}

			}
			if (!links.isEmpty()) {
				System.out.printf("creating %,d links\n", links.size());
				links.sort(new Comparator() {
					@Override
					public int compare(Object o1, Object o2) {
						Object[] r1 = (Object[]) o1;
						Object[] r2 = (Object[]) o2;
						return ((String) r1[0]).compareTo((String) r2[0]);
					}
				});
				Set<File> dirs = new HashSet<>();
				int real = 0;
				for (Object o : links) {
					Object[] r = (Object[]) o;
					System.out.println("[d]" + r[0] + " => " + r[1]);
					File f2 = new File(dir, (String) r[0]);
					if (f2.exists())
						continue;
					File dir = f2.getParentFile();
					if (!dirs.contains(dir)) {
						dir.mkdirs();
						dirs.add(dir);
					}
					try {
						Files.createSymbolicLink(f2.toPath(), Path.of((String) r[1]));
						// FileInfo.setAttr(f2, (String) r[2]);
						f2.setLastModified((long) r[3]);
					} catch (Exception ex) {
						System.err.println("[e]" + r[0] + " => " + r[1] + ", ex=" + ex);
					}
					real++;
				}
				System.out.printf("created %,d links\n", real);
			}
			if (!dirTimes.isEmpty()) {
				System.out.println("setting dir timestamps:" + dirTimes.size());
				dirTimes.sort(new Comparator() {
					@Override
					public int compare(Object o1, Object o2) {
						Object[] r1 = (Object[]) o1;
						Object[] r2 = (Object[]) o2;
						return -((File) r1[0]).compareTo((File) r2[0]);// reverse, sub first?
					}
				});
				for (Object o : dirTimes) {
					Object[] r = (Object[]) o;
					File f2 = (File) r[0];
					long ts = (long) r[1];
					f2.setLastModified(ts);
					// System.out.printf("set %s time to %s\n", f2.getAbsoluteFile(), new Date(ts));
				}
			}
		} finally {
			finished = true;
		}
	}

	public void run0(String fn, String outDir) throws IOException {

		dir = new File(outDir);
		dir.mkdirs();
		t1 = System.currentTimeMillis();
		System.out.println("extracting " + fn);
		File f = new File(fn);
		FileInputStream fin = new FileInputStream(fn);
		String sig = "" + ((char) fin.read()) + ((char) fin.read()) + ((char) fin.read()) + ((char) fin.read());

		if (sig.equals(C.ZS)) {
			readSingleArchive(fin, 0);
		} else if (sig.equals(C.ZM)) {
			readMultiArchive(fin, f);
		} else {
			System.out.println("unknow file sig '" + sig + "', seems not a MXZ file.");
		}
		fin.close();
		System.out.println(String.format(
				"end. total file:%,d, total dir:%,d, total bytes:%,d, total part-file:%,d, soft link:%,d, hard links:%,d , archive:%d",
				totalfile, totaldir, totalbs, totalpartfile, totalSoftLink, totalHardLink, archive));

	}

	private void readMultiArchive(FileInputStream fin, final File f) throws IOException {
		DataInputStream inm = new DataInputStream(fin);
		int cnt = inm.readInt();
		long[] size = new long[cnt];
		for (int i = 0; i < cnt; i++) {
			size[i] = inm.readLong();
			System.out.println(String.format("size[%d]:%d", i, size[i]));
		}
		fin.close();
		long skip = 4 + 4 + cnt * 8;
		Thread th[] = new Thread[cnt];
		for (int i = 0; i < cnt; i++) {
			final int i1 = i;
			final long skip1 = skip;
			th[i] = new Thread() {
				public void run() {
					try {
						readPart(i1, f, skip1);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			th[i].start();
			skip += size[i];
		}
		for (int i = 0; i < cnt; i++) {
			try {
				th[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void readPart(int i, File f, long skip) throws IOException {

		FileInputStream fin = new FileInputStream(f);
		fin.skip(skip);
		String sig = "" + ((char) fin.read()) + ((char) fin.read()) + ((char) fin.read()) + ((char) fin.read());
		if (sig.equals(C.ZS)) {
			readSingleArchive(fin, i);
		} else {
			fin.close();
			throw new RuntimeException("must be sig 'MZS' but got '" + sig + "'");

		}
		fin.close();
	}

	private void readSingleArchive(InputStream fin, int ti) throws IOException {
		archive++;
		DataInputStream in = new DataInputStream(new GZIPInputStream(new BufferedInputStream(fin, 1024 * 1024 * 8)));
		try {
			while (true) {
				while (pause) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				byte type = in.readByte();
				if (type == 0) {// single file
					String name = in.readUTF();
					extraFn = name;
					long size = in.readLong();
					String attr = in.readUTF();
					long time = in.readLong();
					File f = U.writeFile(in, size, dir, name);
					FileInfo.setAttr(f, attr);
					f.setLastModified(time);
//					System.out.printf("[%d]%s\n", ti, name);
					totalfile++;
					totalbs += size;
				} else if (type == 1) {
					String name = in.readUTF();
					extraFn = name + "(+)";
					long size = in.readLong();
					long start = in.readLong();
					long len = in.readLong();
					String attr = null;
					long time = 0;
					if (start == 0) {
						attr = in.readUTF();
						time = in.readLong();
					}
					File f = U.writeFile(in, size, start, len, dir, name);
					if (start == 0) {
						FileInfo.setAttr(f, attr);
						f.setLastModified(time);
					}
					totalbs += len;
					totalpartfile++;
					if (start == 0) {
						System.out.println(name + "(+)");
						totalfile++;
					}
				} else if (type == 2) {// dir
					String name = in.readUTF();
					String attr = in.readUTF();
					long time = in.readLong();
					extraFn = name;
					File f = new File(dir, name);
					f.mkdirs();
					FileInfo.setAttr(f, attr);
					dirTimes.add(new Object[] { f, time });
					// f.setLastModified(time);//it will be set if sub is set?
					// System.out.printf("[d]set %s time to %s\n", f.getAbsoluteFile(), new
					// Date(time));
					totaldir++;
				} else if (type == 3) {// soft
					String name = in.readUTF();
					String link = in.readUTF();
					String attr = in.readUTF();
					long time = in.readLong();
					links.add(new Object[] { name, link, attr, time });
					totalSoftLink++;
				} else if (type == 4) {// hard
					String name = in.readUTF();
					String link = in.readUTF();
					String attr = in.readUTF();
					long time = in.readLong();
					boolean hardLinkIsBroken = true;
					if (hardLinkIsBroken) {
						links.add(new Object[] { name, link, attr, time });
					} else {
						System.err.printf("[d][x]%s => %s\n", name, link);
						File f = new File(dir, name);
						f.getParentFile().mkdirs();
						Files.createLink(f.toPath(), Path.of(link));
					}
					totalHardLink++;
				} else if (type == -1) {
					System.out.println("end of archive");
					break;
				} else {
					throw new RuntimeException("unknow type " + type + ", maybe the archive is not correct.");
				}
			}
		} catch (EOFException eof) {
			System.out.println("eof");
		}

	}
}
