package neoe.util;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;

public class U {
	public static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void bug() {
		RuntimeException e = new RuntimeException("bug found!");
		e.printStackTrace();
		throw e;
	}

	public static void read(InputStream in, byte[] b, int off, long len) throws IOException {
		while (len > 0) {
			int x = in.read(b, off, (int) len);
			if (x == 0)
				bug();
			len -= x;
			off += x;
		}
		in.close();
	}

	public static void safeskip(DataInputStream in, long size) throws IOException {
		while (size > 0) {
			long actual = in.skip(size);
			size -= actual;
		}
	}

	public static void read(File f, DataOutputStream dos, int fsize) throws IOException {
		FileInputStream in = new FileInputStream(f);
		byte[] buf = new byte[fsize];
		int total = 0;
		int len;
		while ((len = in.read(buf)) > 0) {
			dos.write(buf, 0, len);
			total += len;
		}
		in.close();
		if (total != fsize)
			bug();
	}

	public static void read(File f, DataOutputStream dos, int fsize, long start, int len) throws IOException {
		FileInputStream in = new FileInputStream(f);
		in.skip(start);
		byte[] buf = new byte[len];
		read(in, buf, 0, len);
		dos.write(buf);
		in.close();
	}

	public static void debug(String msg) {
		System.out.println(msg);
	}

	public static String numberStr(long v) {
		DecimalFormat formatter = new DecimalFormat("#,###");
		return formatter.format(v);
	}

	public static String percent(long a, long b) {
		int v = (int) Math.ceil(100 * a / (double) b);
		return v + "%";
	}

	public static void writeFile(DataInputStream in, long size, File dir, String name) throws IOException {
		File f = new File(dir, name);
		U.confirmDir(f);
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
		long len = size;
		byte[] buf = new byte[1024 * 1024];
		int r = -1;
		while (true) {
			if (r == 0)
				bug();
			if (len < 0)
				bug();
			if (len == 0)
				break;
			int min = (int) Math.min(len, buf.length);
			r = in.read(buf, 0, min);
			if (r > 0)
				out.write(buf, 0, r);
			len -= r;
		}
		out.close();
	}

	public static void writeFile(DataInputStream in, long size, long start, long len, File dir, String name)
			throws IOException {
		File f = new File(dir, name);
		RandomAccessFile f1;
		if (!f.exists()) {
			U.confirmDir(f);
			f1 = new RandomAccessFile(f, "rw");
			f1.setLength(size);
		} else {
			f1 = new RandomAccessFile(f, "rw");
		}
		f1.seek(start);
		byte[] buf = new byte[1024 * 1024];
		int r = -1;
		while (true) {
			if (r == 0)
				bug();
			if (len < 0)
				bug();
			if (len == 0)
				break;
			int min = (int) Math.min(len, buf.length);
			r = in.read(buf, 0, min);
			if (r > 0)
				f1.write(buf, 0, r);
			len -= r;
		}
		f1.close();
	}

	public static File confirmDir(File f) {
		File dir = f.getParentFile();
		if (dir == null) {
			dir = new File(".");
		} else {
			dir.mkdirs();
		}
		return dir;
	}
}
