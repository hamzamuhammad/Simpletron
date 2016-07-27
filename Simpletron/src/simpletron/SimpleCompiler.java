package simpletron;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SimpleCompiler {
	
	private JFrame frame;
	private static tableEntry[] symbolTable;
	private int[] flags;
	private static int[][] SMLArray;
	private static int tableIndex;
	private static int backMemoryIndex;
	private static int frontMemoryIndex;
	private final int MEMORY_SIZE;
	private final static int MEM = 0;
	private final int MARKER = 1;
	private final char CONSTANT = 'C';
	private final char LINE_NUM = 'L';
	private final char VARIABLE = 'V';
	private boolean secondPass = false;
	
	public SimpleCompiler(JFrame frame, int MEMORY_SIZE) {
		this.frame = frame;
		this.MEMORY_SIZE = MEMORY_SIZE;
		SMLArray = new int[2][MEMORY_SIZE];
		symbolTable = new tableEntry[MEMORY_SIZE];
		flags = new int[MEMORY_SIZE];
		tableIndex = 0;
		backMemoryIndex = 0;
	}
	
	public void readFile() throws IOException {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		fileChooser.setDialogTitle("Please select a .sl file to run");
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION)
			loadFile(fileChooser.getSelectedFile());
		else
			SimpleMain.mainMenu(frame);
	}
	
	private void loadFile(File f) throws FileNotFoundException {
		Scanner slReader = new Scanner(f);
		int frontMemoryIndex = 0;
		backMemoryIndex = MEMORY_SIZE - 1;
		int tableIndex = 0;
		boolean isValid = true; //means loading of file is OKAY so far (no errors)
		while (slReader.hasNextLine() && isValid) { //loop thru the file's lines
			String currLine = slReader.nextLine(); //current line we are on
			Scanner lineReader = new Scanner(currLine); //read thru line
			try {
				int lineNum = Integer.parseInt(lineReader.next());
				symbolTable[tableIndex] = new tableEntry(lineNum, LINE_NUM, frontMemoryIndex);
			}
			catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(frame, "NO LINE NUMBER AS FIRST TOKEN"
						+ "OF LINE", "COMPILE ERROR", JOptionPane.ERROR_MESSAGE, null);
				isValid = false;
			}
			if (!lineReader.hasNext("rem") && isValid) { //means SML code will be added!
				String token = lineReader.next();
				char var = ' ';
				int loc = 0;
				switch(token) {
					case "input":
						var = lineReader.next().toCharArray()[0];
						loc = searchTable(var).getLocation();
						if (loc == 0) {
							symbolTable[tableIndex] = new tableEntry((int)var, VARIABLE,
									backMemoryIndex);
						}
						else
							symbolTable[loc].setLocation(backMemoryIndex);
						SMLArray[MEM][frontMemoryIndex] = (SMLCodes.READ * MEMORY_SIZE) 
								+ backMemoryIndex;
						backMemoryIndex--;
						frontMemoryIndex++;
						break;
					case "print":
						var = lineReader.next().toCharArray()[0];
						loc = searchTable(var).getLocation();
						SMLArray[MEM][frontMemoryIndex] = (SMLCodes.WRITE * MEMORY_SIZE)
								+ loc;
						frontMemoryIndex++;
						break;
					case "end":
						SMLArray[MEM][frontMemoryIndex] = SMLCodes.EMPTY;
						frontMemoryIndex++;
						break;
					case "goto": //unsolved reference is when location is not in symbol table
						int reference = Integer.parseInt(lineReader.next());
						if (reference != searchTable((char)reference).getLocation()) {
							secondPass = true;
							flags[frontMemoryIndex] = reference;
							SMLArray[MEM][frontMemoryIndex] = (SMLCodes.BRANCH * MEMORY_SIZE);
						}
						else {
							reference = searchTable((char)reference).getLocation();
							SMLArray[MEM][frontMemoryIndex] = (SMLCodes.BRANCH * MEMORY_SIZE)
									+ reference;
						}
						frontMemoryIndex++;
						break;
					case "let":
						//END RESULT MUST BE STORE VALUE OF EXPRESSION IN VAR ex. y = y + 1, store (y+1) in y
				}
			}
			tableIndex++;
		}
		
		
		
		
		slReader.close();
	}
	
	public static tableEntry searchTable(char symbolVar) {
		tableEntry temp = null;
		for (tableEntry te : symbolTable) {
			if (te.getSymbol() == symbolVar)
				 temp = te;
		}
		return temp;
	}
	
	public static void insertTableEntry(tableEntry temp) {
		symbolTable[tableIndex] = temp;
		tableIndex++;
	}
	
	public static int getBackMemoryIndex() {
		return backMemoryIndex;
	}
	
	public static void decrementBackMemoryIndex() {
		backMemoryIndex--;
	}
	
	public static void incrementFrontMemoryIndex() {
		frontMemoryIndex++;
	}
	
	public static void insertSMLCode(int code) {
		SMLArray[MEM][frontMemoryIndex] = code;
		frontMemoryIndex++;
	}
	public int[][] compile() {
		
		return SMLArray;
	}


}