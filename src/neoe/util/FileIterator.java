package neoe.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FileIterator implements Iterable<File> {

	public interface Ignored {

		boolean judge(File f);

	}

	List<File> buf;
	private Ignored ignored;

	public FileIterator(String dir, Ignored ignored) {
		this.ignored = ignored;
		buf = new ArrayList<File>();
		File f = new File(dir);
		buf.add(f);
	}

	@Override
	public Iterator<File> iterator() {
		return new Iterator<File>() {

			@Override
			public boolean hasNext() {
				return buf.size() > 0;
			}

			@Override
			public File next() {
				File f = buf.remove(0);
				if (ignored==null||!ignored.judge(f)) {
					File[] sub = f.listFiles();
					if (sub != null) {
						buf.addAll(Arrays.asList(sub));
					}
				}
				return f;
			}

			@Override
			public void remove() {
			}
		};
	}

}
