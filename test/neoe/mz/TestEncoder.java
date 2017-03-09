package neoe.mz;

import neoe.mz.MzEncoder;

public class TestEncoder {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String dest = "c:/tmp/t5.mz";
		MzEncoder.main(new String[]{"C:/neoe/oss/lab/lab2/projects",dest,"4","4" });
		ConcatFile.main(new String[]{dest});
	}

}
