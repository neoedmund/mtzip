package neoe.mz;

public class TestDecoder {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		System.out.println("TestEncoderLinux start");
//		PreviewFiles.main(new String[]{"E:/tmp/linux-3.9.6"});
//		MxzEncoder.main(new String[]{"E:/tmp/linux-3.9.6","e:/tmp/linux.test.mxz","8","8" });
//		CheckMxz.main(new String[]{"e:/tmp/linux.test.mxz.-0"});
//		CheckMxz.main(new String[]{"e:/tmp/linux.test.mxz.-1"});
//		new File("e:/tmp/linux.test.mxz").delete();
//		ConcatFile.main(new String[]{"e:/tmp/linux.test.mxz"});
//		CheckMxz.main(new String[]{"e:/tmp/linux.test.mxz"});		
		MzDecoder.main(new String[]{"c:/tmp/t5.mz", "c:/tmp/t5dir"});
		System.out.println("TestDecoder end");
	}

}
