package neoe.mz;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

import neoe.util.U;

public class CheckMz {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		new CheckMz().run(args[0]);
	}

	AtomicLong totalfile = new AtomicLong(0), totaldir = new AtomicLong(0), totalpartfile = new AtomicLong(0),
			totalbs = new AtomicLong(0), archive = new AtomicLong(0);
	boolean finished;
	protected String extraFn;
	protected boolean pause;

	public void run(String fn) throws Exception {
		try {
			File f = new File(fn);
			if (f.isFile() && f.exists()) {
				run0(fn);
			} else {
				String name = f.getName();
				String name2 = name + ".-";
				File dir = U.confirmDir(f);
				File[] fs = dir.listFiles();
				Exception[] err = new Exception[1];
				AtomicInteger errcnt = new AtomicInteger(0);
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
										run0(fxname);
									} catch (IOException e) {
										e.printStackTrace();
										err[0] = e;
										errcnt.incrementAndGet();
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
						if (errcnt.get() > 0) {
							throw new RuntimeException(String.format("have %d errors, like ", errcnt.get()), err[0]);
						}
						System.out.println("check finished");
					}
				}

			}
		} finally {
			finished = true;
		}
	}

	public void run0(String fn) throws IOException {
		try {
			System.out.println("checking " + fn);
			File f = new File(fn);
			FileInputStream fin = new FileInputStream(fn);
			String sig = "" + ((char) fin.read()) + ((char) fin.read()) + ((char) fin.read()) + ((char) fin.read());

			if (sig.equals(C.ZS)) {
				readSingleArchive(fin);
			} else if (sig.equals(C.ZM)) {
				readMultiArchive(fin, f);
			} else {
				System.out.println("unknow file sig '" + sig + "', seems not a Mz file.");
			}

			fin.close();
			System.out.println(String.format(
					"end. total file:%,d, total dir:%,d, total bytes:%,d, total part-file:%,d, archive:%,d",
					totalfile.longValue(), totaldir.longValue(), totalbs.longValue(), totalpartfile.longValue(),
					archive.longValue()));
		} finally {
			finished = true;
		}

	}

	private void readMultiArchive(FileInputStream fin, File f) throws IOException {
		DataInputStream inm = new DataInputStream(fin);
		int cnt = inm.readInt();
		long[] size = new long[cnt];
		for (int i = 0; i < cnt; i++) {
			size[i] = inm.readLong();
			System.out.println(String.format("size[%d]:%d", i, size[i]));
		}
		fin.close();
		long skip = 4 + 4 + cnt * 8;
		for (int i = 0; i < cnt; i++) {
			fin = new FileInputStream(f);
			fin.skip(skip);
			String sig = "" + ((char) fin.read()) + ((char) fin.read()) + ((char) fin.read()) + ((char) fin.read());
			if (sig.equals(C.ZS)) {
				readSingleArchive(fin);
			} else {
				System.out.println("must be sig MZS but got '" + sig + "'");
				break;
			}
			skip += size[i];
		}
	}

	private void readSingleArchive(InputStream fin) throws IOException {
		archive.incrementAndGet();
		DataInputStream in = new DataInputStream(new GZIPInputStream(fin));
		try {
			while (true) {
				byte type = in.readByte();
				if (type == 0) {
					String name = in.readUTF();
					extraFn = name;
					long size = in.readLong();
					String attr = in.readUTF();
					long time = in.readLong();
					U.safeskip(in, size);
//					System.out.println(name);
					totalfile.incrementAndGet();
					totalbs.addAndGet(size);
				} else if (type == 1) {
					String name = in.readUTF();
					extraFn = name + "(+)";
					long size = in.readLong();
					long start = in.readLong();
					long len = in.readLong();
					if (start == 0) {
						String attr = in.readUTF();
						long time = in.readLong();
					}
					U.safeskip(in, len);
					totalbs.addAndGet(len);
					totalpartfile.incrementAndGet();
					if (start == 0) {
//						System.out.println(name + "(+)");
						totalfile.incrementAndGet();
					}
				} else if (type == 2) {
					String name = in.readUTF();
					String attr = in.readUTF();
					long time = in.readLong();
					extraFn = name;
					totaldir.incrementAndGet();
				} else if (type == 3) {// soft
					String name = in.readUTF();
					String link = in.readUTF();
					String attr = in.readUTF();
					long time = in.readLong();
				} else if (type == 4) {// hard
					String name = in.readUTF();
					String link = in.readUTF();
					String attr = in.readUTF();
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
