package neoe.mz.copy;

import java.io.IOException;

import neoe.mz.GeneralWorker;
import neoe.util.FileUtil;
import neoe.util.WorkRoom.IWorker;

public class FileWriteWorker extends GeneralWorker implements IWorker {

	 

	private Copyer srcObj;

	@Override
	public void doInit(int index, Object initValueForWorkerClass) throws IOException {
		Object[] param = (Object[]) initValueForWorkerClass;
		srcObj = (Copyer) param[0];
	}

	@Override
	public void cleanup() throws Exception {
	 
	}

	@Override
	public void workOn(Object job) throws Exception {
		FileCopyInfo info = (FileCopyInfo) job;
		FileUtil.copy(info.from, info.to);
		srcObj.addDoneBytes(info.length);
	}

}
