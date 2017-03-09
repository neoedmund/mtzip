package neoe.mz;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import neoe.util.U;
import neoe.util.WorkRoom;
import neoe.util.WorkRoom.IWorker;

public abstract class GeneralWorker implements IWorker {
	private Thread thread;
	private ConcurrentLinkedQueue jobs = new ConcurrentLinkedQueue();
	private boolean closed;
	private int index;
	private String name;
	public Throwable error;
	private WorkRoom room;

	@Override
	final public int length() {
		if (error != null)
			throw new RuntimeException(error);
		return jobs.size();
	}

	@Override
	final public void add(Object job) {
		if (error != null)
			throw new RuntimeException(error);
		jobs.add(job);
		// thread.notify();
	}

	@Override
	final public void init(final WorkRoom room, int index, Object initValueForWorkerClass) throws Exception {
		this.index = index;
		this.name = room.name;
		this.room = room;
		doInit(index, initValueForWorkerClass);
		thread = new Thread() {
			public void run() {
				try {
					work();
				} catch (Throwable e) {
					e.printStackTrace();
					room.halt();
					error = e;
				}
			}
		};
		thread.start();

	}

	public abstract void doInit(int index, Object initValueForWorkerClass) throws Exception;

	public abstract void cleanup() throws Exception;

	public abstract void workOn(Object job) throws Exception;

	private int maxLen;
	private boolean halted;

	final private void work() throws Exception {
		while (!closed || jobs.size() > 0) {
			if (error != null)
				return;
			if (halted)
				return;
			try {
				if (jobs.size() > 0) {
					maxLen = Math.max(maxLen, jobs.size());
					Object job = jobs.poll();
					workOn(job);
				} else {
					U.sleep(23);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cleanup();
	}

	@Override
	final public void close() {
		if (error != null)
			throw new RuntimeException(error);
		closed = true;
	}

	@Override
	final public void waitFinish() {
		if (error != null)
			throw new RuntimeException(error);
		try {
			thread.join();
			System.out.println(name + "#" + index + ":maxqueue=" + maxLen);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void halt() {
		closed = true;
		halted = true;
	}

}
