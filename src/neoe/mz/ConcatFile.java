package neoe.mz;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import neoe.util.U;

public class ConcatFile {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new ConcatFile().run(args[0]);
	}

	public void run(String fn) throws IOException {
		File f = new File(fn);
		if (f.exists()) {
			System.out.println("already exists:" + fn);
			return;
		}
		String fn1_1 = f.getName() + ".-";
		File dir =  U.confirmDir(f);
		List<File> sub = new ArrayList<File>();
		for (File f2 : dir.listFiles()) {
			String fn2 = f2.getName();
			if (fn2.startsWith(fn1_1)) {
				sub.add(f2);
			}
		}
		System.out.println(String.format("find %d files to concat:", sub.size()));
		for (File f2 : sub) {
			System.out.println(f2.getName());
		}

		// ----------------
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		out.write(C.ZM.getBytes());
		out.writeInt(sub.size());
		for (File f2 : sub) {
			out.writeLong(f2.length());
		}
		byte[] bs = new byte[1024 * 1024 * 8];
		for (File f1 : sub) {
			System.out.println("adding " + f1.getName());
			write(new FileInputStream(f1), out, bs);
		}
		out.close();
		for (File f1 : sub) {
			f1.delete();
		}
		System.out.println("ok");

	}

	private void write(FileInputStream in, OutputStream out, byte[] bs) throws IOException {
		int len;
		while ((len = in.read(bs)) > 0) {
			out.write(bs, 0, len);
		}
		in.close();
	}

}
