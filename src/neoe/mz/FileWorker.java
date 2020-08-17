package neoe.mz;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;

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
			dos.writeUTF(FileInfo.getAttr(f.f));
			dos.writeLong(f.f.lastModified());
			U.read(f.f, dos, (int) f.fsize);
			// System.out.println("0:"+ba.size());
		} else if (f.type == 1) {// part file
			dos.writeByte(f.type);
			dos.writeUTF(f.pathname);
			dos.writeLong(f.fsize);
			dos.writeLong(f.start);
			dos.writeLong(f.len);
			if (f.start == 0) {
				dos.writeUTF(FileInfo.getAttr(f.f));
				dos.writeLong(f.f.lastModified());
			}
			U.read(f.f, dos, (int) f.fsize, f.start, (int) f.len);
			// System.out.println("1:"+ba.size());
		} else if (f.type == 2) {// dir
			dos.writeByte(f.type);
			dos.writeUTF(f.pathname);
			dos.writeUTF(FileInfo.getAttr(f.f));
			dos.writeLong(f.f.lastModified());
			// System.out.println("2:"+ba.size());
		} else if (f.type == 3) { // soft link
			dos.writeByte(f.type);
			String s1, s2;
			dos.writeUTF(s1 = f.pathname);
			dos.writeUTF(s2 = Files.readSymbolicLink(f.f.toPath()).toString());
			dos.writeUTF(FileInfo.getAttr(f.f));
			dos.writeLong(f.f.lastModified());
//			System.out.printf("[d][softlink]%s -> %s\n", s1, s2);
		} else if (f.type == 4) { // hard link
			dos.writeByte(f.type);
			String s1, s2;
			dos.writeUTF(s1 = f.pathname);
			dos.writeUTF(s2 = f.f.getAbsolutePath());
			dos.writeUTF(FileInfo.getAttr(f.f));
			dos.writeLong(f.f.lastModified());
//			System.out.printf("[d][hardlink]%s -> %s\n", s1, s2);
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
