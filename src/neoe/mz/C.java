package neoe.mz;

public class C {
	public static final String ZS = "MgZS";
	public static final String ZM = "MgZM";
	public static final String VER = "v1.2";
	static String osname;
	static String osarch;
	public static boolean isWindows;
	static boolean isX86;

	static {
		osname = System.getProperty("os.name");
		osarch = System.getProperty("os.arch");
		System.getProperty("os.version");
		isWindows = osname.indexOf("Windows") >= 0;
		isX86 = osarch.indexOf("x86") >= 0 && System.getenv("ProgramW6432") == null;
	}
}
