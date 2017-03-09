package neoe.mz;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.tukaani.xz.XZInputStream;

import neoe.util.U;

public class CheckMz {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		new CheckMz().run(args[0]);
	}

	long totalfile = 0, totaldir = 0, totalpartfile = 0, totalbs = 0, archive = 0;
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
			System.out.println(
					String.format("end. total file:%d, total dir:%d, total bytes:%d, total part-file:%d, archive:%d",
							totalfile, totaldir, totalbs, totalpartfile, archive));
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
		archive++;
		DataInputStream in = new DataInputStream(new XZInputStream(fin));
		try {
			while (true) {
				byte type = in.readByte();
				if (type == 0) {
					String name = in.readUTF();
					extraFn = name;
					long size = in.readLong();
					U.safeskip(in, size);
					System.out.println(name);
					totalfile++;
					totalbs += size;
				} else if (type == 1) {
					String name = in.readUTF();
					extraFn = name + "(+)";
					long size = in.readLong();
					long start = in.readLong();
					long len = in.readLong();
					U.safeskip(in, len);
					totalbs += len;
					totalpartfile++;
					if (start == 0) {
						System.out.println(name + "(+)");
						totalfile++;
					}
				} else if (type == 2) {
					String name = in.readUTF();
					extraFn = name;
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
