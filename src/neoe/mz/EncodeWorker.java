package neoe.mz;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import neoe.util.U;
import neoe.util.WorkRoom.IWorker;

public class EncodeWorker extends GeneralWorker implements IWorker {

	private GZIPOutputStream xzout;
	private FileOutputStream out;
	private MzEncoder srcObj;

	@Override
	public void doInit(int index, Object initValueForWorkerClass) throws IOException {
		Object[] param = (Object[]) initValueForWorkerClass;
		String fn0 = (String) param[0];
		srcObj = (MzEncoder) param[1];
		String fn = fn0 + ".-" + index;
		File f = new File(fn);
		U.confirmDir(f);		
		out = new FileOutputStream(f);
		out.write(C.ZS.getBytes());
		out.flush();
		xzout = new GZIPOutputStream(new BufferedOutputStream(out, 64*1024*1024));
	}

	@Override
	public void cleanup() throws Exception {
		xzout.write(-1);
		xzout.finish();
		xzout.close();
		out.close();
	}

	@Override
	public void workOn(Object job) throws Exception {
		byte[] bs = (byte[]) job;
		xzout.write(bs, 0, bs.length);
		srcObj.addDoneBytes(bs.length);
	}

}
