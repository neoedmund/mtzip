package neoe.mz;

public class Console {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			usage();
			return;
		}
		String p0 = args[0];
		if ("c".equals(p0)) {
			String dest = args[2];
			if (!dest.endsWith(".mz")) {
				dest = dest + ".mz";
			}
			MzEncoder.main(new String[] { args[1], dest, args[3], args[4] });
		} else if ("j".equals(p0)) {
			String dest = args[1];
			new ConcatFile().run(dest);
		} else if ("cj".equals(p0)) {
			String dest = args[2];
			if (!dest.endsWith(".mz")) {
				dest = dest + ".mz";
			}
			MzEncoder.main(new String[] { args[1], dest, args[3], args[4] });
			new ConcatFile().run(dest);
		} else if ("x".equals(p0)) {
			MzDecoder.main(new String[] { args[1], args[2] });
		} else if ("t".equals(p0)) {
			CheckMz.main(new String[] { args[1] });
		} else {
			usage();
		}
	}

	private static void usage() {
		System.out.println(
				"MtZip v1.0\nUsage: \n create archive: c <src-dir> <dest> <file-worker-cnt> <encode-worker-cnt> \n"//
						+ " extract archive: x <src> <dest-dir> \n"//
						+ " test archive: t <file>\n"//
						+ " join archives(optional): j <file>\n"
						+ " create archive and join(c and j): cj <src-dir> <dest> <file-worker-cnt> <encode-worker-cnt> \n");

	}
}
