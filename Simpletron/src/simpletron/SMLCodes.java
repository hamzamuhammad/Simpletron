package simpletron;

public final class SMLCodes {
	
	//constants that are 'instructions' that an SML program can do, translated
	//into 2-digit integers
	public static final int READ = 10;
	public static final int WRITE = 11;
	public static final int NEWLINE = 12;
	public static final int LOAD = 20;
	public static final int STORE = 21;
	public static final int ADD = 30;
	public static final int SUBTRACT = 31;
	public static final int DIVIDE = 32;
	public static final int MULTIPLY = 33;
	public static final int MODULUS = 34;
	public static final int POWER = 35;
	public static final int BRANCH = 40;
	public static final int BRANCHNEG = 41;
	public static final int BRANCHZERO = 42;
	public static final int HALT = 43;
	public static final int EMPTY = 0;
	
	private SMLCodes() {
		throw new AssertionError();
	}
}