package neoe.mz;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import neoe.util.U;
import neoe.util.WorkRoom;
import neoe.util.WorkRoom.IWorker;

/** read data from file on disk */
public class FileWorker extends GeneralWorker implements IWorker {

	WorkRoom encodeRoom;
	private int bufsize;
	private DataOutputStream dos;
	private ByteArrayOutputStream ba;

	private void initBuffer() {
		dos = new DataOutputStream(ba = new ByteArrayOutputStream((int) (bufsize * 1.3)));
	}

	@Override
	public void doInit(int index, Object initValueForWorkerClass) {
		this.encodeRoom = (WorkRoom) ((Object[]) initValueForWorkerClass)[0];
		this.bufsize = (Integer) ((Object[]) initValueForWorkerClass)[1];
		// buf = new byte[bufsize];
		initBuffer();
	}

	@Override
	public void cleanup() {
		if (ba.size() > 0) {
			encodeRoom.submit(ba.toByteArray());
		}
		dos = null;
		ba = null;

	}

	@Override
	public void workOn(Object job) throws Exception {
		FileInfo f = (FileInfo) job;
		if (f.type == 0) {// whole file
			dos.writeByte(f.type);
			dos.writeUTF(f.pathname);
			dos.writeLong(f.fsize);
			U.read(f.f, dos, (int) f.fsize);
			// System.out.println("0:"+ba.size());
		} else if (f.type == 1) {// part file
			dos.writeByte(f.type);
			dos.writeUTF(f.pathname);
			dos.writeLong(f.fsize);
			dos.writeLong(f.start);
			dos.writeLong(f.len);
			U.read(f.f, dos, (int) f.fsize, f.start, (int) f.len);
			// System.out.println("1:"+ba.size());
		} else if (f.type == 2) {// dir
			dos.writeByte(f.type);
			dos.writeUTF(f.pathname);
			// System.out.println("2:"+ba.size());
		} else {
			U.bug();
		}

		dos.flush();
		if (ba.size() >= bufsize) {
			encodeRoom.submit(ba.toByteArray());
			dos.close();
			initBuffer();
		}

	}

}
