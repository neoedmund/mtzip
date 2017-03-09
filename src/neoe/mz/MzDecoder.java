package neoe.mz;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

	long totalfile = 0, totaldir = 0, totalpartfile = 0, totalbs = 0, archive = 0;
	private File dir;
	boolean finished;
	protected String extraFn;
	long t1;
	boolean pause;

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
					}
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
		System.out.println(
				String.format("end. total file:%d, total dir:%d, total bytes:%d, total part-file:%d, archive:%d",
						totalfile, totaldir, totalbs, totalpartfile, archive));

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
				if (type == 0) {
					String name = in.readUTF();
					extraFn = name;
					long size = in.readLong();
					U.writeFile(in, size, dir, name);
					System.out.printf("[%d]%s\n", ti, name);
					totalfile++;
					totalbs += size;
				} else if (type == 1) {
					String name = in.readUTF();
					extraFn = name + "(+)";
					long size = in.readLong();
					long start = in.readLong();
					long len = in.readLong();
					U.writeFile(in, size, start, len, dir, name);
					totalbs += len;
					totalpartfile++;
					if (start == 0) {
						System.out.println(name + "(+)");
						totalfile++;
					}
				} else if (type == 2) {
					String name = in.readUTF();
					extraFn = name;
					new File(dir, name).mkdirs();
					totaldir++;
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
