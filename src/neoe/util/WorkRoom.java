package neoe.util;

import javax.swing.JOptionPane;

import neoe.mz.GeneralWorker;

public class WorkRoom {
	public interface IWorker {

		int length();

		void add(Object job);

		void init(WorkRoom room, int index, Object initValueForWorkerClass) throws Exception;

		void close();

		void waitFinish();

		void halt();

	}

	private int workerCnt;
	private IWorker[] workers;
	private int maxLengthPerWorker;
	public String name;
	private int overQueued;

	public WorkRoom(int workerCnt, Class<? extends IWorker> WorkerClass, int maxLengthPerWorker, String name,
			Object initValueForWorkerClass) throws Exception {
		this.name = name;
		this.workerCnt = workerCnt;
		workers = new IWorker[workerCnt];
		this.maxLengthPerWorker = maxLengthPerWorker;
		for (int i = 0; i < workerCnt; i++) {
			workers[i] = (IWorker) WorkerClass.newInstance();
			workers[i].init(this, i, initValueForWorkerClass);
		}
	}

	public void submit(Object job) {
		int min = maxLengthPerWorker;
		int minWorker = -1;

		while (true) {
			for (int i = 0; i < workerCnt; i++) {
				int len = workers[i].length();
				if (len == 0) {
					minWorker = i;
					break;
				}
				if (len < min) {
					min = len;
					minWorker = i;
				}
			}
			if (minWorker == -1) {
				// debug(name + ":over queued, wait awhile");
				overQueued++;
				U.sleep(20);
			} else {
				workers[minWorker].add(job);
				break;
			}
		}
	}

	private void debug(String m) {
		System.out.println(m);
	}

	public void close() {
		for (int i = 0; i < workerCnt; i++) {
			workers[i].close();
		}

	}

	public void waitFinish() {
		System.out.println(name + " over queued " + overQueued);
		for (int i = 0; i < workerCnt; i++) {
			workers[i].waitFinish();
			Throwable error = ((GeneralWorker) workers[i]).error;
			if (error != null) {
				JOptionPane.showMessageDialog(null, ""+error);
				throw new RuntimeException(error);
			}
		}

	}

	public void halt() {
		System.exit(1);
		for (int i = 0; i < workerCnt; i++) {
			workers[i].halt();
		}

	}
}
