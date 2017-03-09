package neoe.mz.copy;

import neoe.mz.GeneralWorker;
import neoe.util.WorkRoom;
import neoe.util.WorkRoom.IWorker;

public class FileReadWorker extends GeneralWorker implements IWorker {

	WorkRoom nextRoot;
 

 
	@Override
	public void doInit(int index, Object initValueForWorkerClass) {
		this.nextRoot = (WorkRoom) ((Object[]) initValueForWorkerClass)[0];
	}

	@Override
	public void cleanup() {
 

	}

	@Override
	public void workOn(Object job) throws Exception {
		FileCopyInfo f = (FileCopyInfo) job;
		nextRoot.submit(f);
	}

}
