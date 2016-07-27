/* The Simple virtual machine which runs an SML program. 
 * 				© 2015 Hamza Muhammad
 */
package simpletron;

//necessary libraries that provide the GUI for the virtual machine along with 
//file reading and user input
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Interpreter {
	
	//fixed constant that determines how 'big' an SML program can be
	private static final int MEMORY_CAPACITY = 100;
	//2d array where the TOP row is a 'marker' array, and BOTTOM is actual memory
	private static int[][] memory = new int[2][MEMORY_CAPACITY];
	
	//constants that are 'instructions' that an SML program can do, translated
	//into 2-digit integers
	private static final int READ = 10;
	private static final int WRITE = 11;
	private static final int NEWLINE = 12;
	private static final int LOAD = 20;
	private static final int STORE = 21;
	private static final int ADD = 30;
	private static final int SUBTRACT = 31;
	private static final int DIVIDE = 32;
	private static final int MULTIPLY = 33;
	private static final int MODULUS = 34;
	private static final int POWER = 35;
	private static final int BRANCH = 40;
	private static final int BRANCHNEG = 41;
	private static final int BRANCHZERO = 42;
	private static final int HALT = 43;
	//marker used to indicate end of program
	private static final int EOF = -1; 
	//marker used to indicate start/end of a string sequence
	private static final int STRING = -2;
	
	//determines whether we are accessing the 'marker' array or the 'memory' one
	private static final int MEM = 0;
	private static final int MARKER = 1;
	
	private static final String intro = "Welcome to Simple!\n© 2015 Hamza Muhammad\n";
	private static final String[] options = new String[] {"Run", "Compile", "Settings", "Exit"};

	private static boolean debug = false;
	private static boolean memoryDump = false;


	/* In the main method, we first initialize a JFrame to hold all of our GUI
	 * applets. Then, we call the mainMenu method that is essentially the whole
	 * program.
	 */
	public static void main (String [] args) throws IOException {
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		mainMenu(frame);

	}

	private static void mainMenu(JFrame frame) throws FileNotFoundException {
		
		int response = JOptionPane.showOptionDialog(frame, intro, 
				"Simple version 1.0", JOptionPane.DEFAULT_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		
		switch(response) {
			case 0: loadProgram(memory, frame); break;
			//case 1: compileProgram(); **TO-DO TOMORROW**
			case 2: showSettings(frame); break;
			case 3: System.exit(0); break;
			default: throw new IllegalArgumentException("Unknown command");
		}
		
	}
	
	/* Important method which presents the user with a dialog box from which
	 * to select an SML file. That file is then loaded into memory, and then
	 * runProgram is called.
	 */
	private static void loadProgram(int[][] memory, JFrame frame) throws FileNotFoundException {
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		fileChooser.setDialogTitle("Please select a .sml file to run");
		
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			Scanner smlReader = new Scanner(fileChooser.getSelectedFile());
			int index = 0;
			while (smlReader.hasNextLine()) {
				String line = smlReader.nextLine();
				int instruction = Integer.parseInt(line);
				if (isValid(instruction)) {
					memory[MEM][index] = instruction;
					index++;
				}
				else
					throw new IllegalArgumentException("INVALID SML INSTRUCTION"
							+ ": ABORTING EXECUTION");
			}
			runProgram(frame);
		}
		else
			mainMenu(frame);
		
	}
	
//	private static void compileProgram(JFrame frame) {
//		SimpleCompiler sc = new SimpleCompiler(frame);
//		sc.compile();
//	}
	
	private static void showSettings(JFrame frame) {
		
	    frame = new SettingsCheckBox(frame);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setSize(200,140);
        frame.setResizable(false);
        System.out.println(debug + " " + memoryDump);
        
	}
	
	private static class SettingsCheckBox extends JFrame implements ActionListener {

		private static final long serialVersionUID = 1L;
		
		private static JCheckBox debugButton = new JCheckBox("debug mode");
		private static JCheckBox memoryDumpButton = new JCheckBox("memory dump to disk");
		
		private static JButton doneButton = new JButton("Done");
		
		private static JPanel p = new JPanel();

		private JFrame frame;
		
		public SettingsCheckBox(JFrame frame) {
			
			this.frame = frame;
			
			setTitle("Settings");
			setSize(300, 200);

			p.add(debugButton);
			p.add(memoryDumpButton);
			p.add(doneButton);
			
			debugButton.addActionListener(this);
			memoryDumpButton.addActionListener(this);
			doneButton.addActionListener(this);
			doneButton.setActionCommand("DONE");
			
			getContentPane().add(p, "Center");
			
		}
		
		public void actionPerformed(ActionEvent evt) {
			String command = evt.getActionCommand();
		    if (debugButton.isSelected())
		    	debug = true;
		    if (memoryDumpButton.isSelected())
		    	memoryDump = true;
		    if (command.equals("DONE")) {
		    	try {
		    		dispose();
		    		mainMenu(frame);
		    	} catch (FileNotFoundException e) {
		    		throw new IllegalArgumentException("No file found");
		    	}
		    }
		 }
		
	}
	

	
	private static void runProgram(int[] memory, Scanner input) {
		int instructionCounter = 0, instructionRegister = 0;
		int accumulator = 0;
		int operationCode = 0, operand = 0;
		boolean programEnd = false;
		while (!programEnd && instructionCounter < memory.length) {
			instructionRegister = memory[instructionCounter];
			operationCode = (int) (instructionRegister / MEMORY_CAPACITY);
			operand = (int) (instructionRegister % MEMORY_CAPACITY);
			if (operationCode >= ADD && operationCode < BRANCH)
				if (!checkArithmeticErrors(operationCode, accumulator, memory[operand])) {
					System.out.println("\n*** Accumulator overflow and/or Divide by Zero ***");
					System.out.println("*** Simpletron execution abnormally terminated ***\n");
					programEnd = true;
				}
			if (!programEnd) {
				switch (operationCode) {
				case EMPTY:
					programEnd = true;
					break;
				case READ: 
					System.out.println("Enter a value.");
					String value = input.nextLine();
					try {
						int num = Integer.parseInt(value);
						memory[operand] = num;
					} 
					catch (NumberFormatException e) {
						String starter = STRING + "";
						starter += value.length();
						memory[operand] = Integer.parseInt(starter);
						operand++;
						char[] letters = value.toUpperCase().toCharArray();
						int endVal = letters.length / 2;
						int leftOver = letters.length % 2;
						int wordIndex = 0;
						String temp = "";
						for (int index = 0; index < endVal; index++) {
							temp += (int) letters[wordIndex];
							temp += (int) letters[wordIndex + 1];
							memory[operand] = Integer.parseInt(temp);
							wordIndex += 2;
							operand++;
							temp = "";
						}
						for (int index = 0; index < leftOver; index++) {
							temp += (int) letters[wordIndex + index];
						}
						if (leftOver != 0) {
							memory[operand] = Integer.parseInt(temp);
							operand++;
						}
					}
					break;
				case WRITE: //special print case for a string!
					String printValue = memory[operand] + "";
					if (printValue.substring(0, 2).equals(STRING + "")) {
						double numLetters = Double.parseDouble(printValue.substring(2)) / 2.0;
						operand++;
						for (int index = 0; index < numLetters; index ++) {
							int firstLetter = (int) (memory[operand] / 100);
							int secondLetter = (int) (memory[operand] % 100);
							if (firstLetter == 0)
								System.out.print((char)secondLetter);
							else
								System.out.print((char)firstLetter + "" + (char)secondLetter);
							operand++;
						}
					}
					else
						System.out.println("\n" + memory[operand]);
					break;
				case NEWLINE:
					System.out.println();
					break;
				case LOAD:
					accumulator = memory[operand];
					break;
				case STORE:
					memory[operand] = accumulator;
					break;
				case ADD:
					accumulator += memory[operand];
					break;
				case SUBTRACT:
					accumulator -= memory[operand];
					break;
				case DIVIDE:
					accumulator /= memory[operand];
					break;
				case MULTIPLY:
					accumulator *= memory[operand];
					break;
				case MODULUS:
					accumulator %= memory[operand];
					break;
				case POWER:
					accumulator = (int) Math.pow(accumulator, memory[operand]);
					break;
				case BRANCH:
					instructionCounter = operand;
					break;
				case BRANCHNEG:
					if (accumulator < 0)
						instructionCounter = operand;
					break;
				case BRANCHZERO:
					if (accumulator == 0)
						instructionCounter = operand;
					break;
				case HALT:
					System.out.println("\n*** Simpletron execution terminated ***");
					break;
				default:
					System.out.println("*** Unknown SML instruction ***");
					System.out.println("*** Simpletron execution abnormally terminated ***\n");
					programEnd = true;
				}
			}
			if (operationCode != BRANCH && operationCode != BRANCHNEG && operationCode != BRANCHZERO)
				++instructionCounter;
		}
		memoryDump(accumulator, instructionCounter, instructionRegister, operationCode, operand, memory);
		System.out.println("\n*** Program execution completed ***");
	}

	private static void memoryDump(double accumulator, int instructionCounter, double instructionRegister, int operationCode, int operand, double[] memory) {
		System.out.println("\n*** MEMORY DUMP *** \n");
		System.out.println("REGISTERS:");
		System.out.printf("accumulator          +%04f\n", accumulator);
		System.out.printf("instructionCounter          %02d\n", instructionCounter);
		System.out.printf("instructionRegister  +%04f\n", instructionRegister);
		System.out.printf("operationCode               %02d\n", operationCode);
		System.out.printf("operand                     %02d\n", operand );
		System.out.println("\nMEMORY:");
		System.out.print("   ");
		for (int i = 0; i < 10; i++)
			System.out.printf("%9d ", i);
		System.out.println();
		for (int i = 0; i < MEMORY_CAPACITY; i += 10) {
			System.out.printf("%2d ", i);
			for (int j = 0; j < 10; j++) {
				System.out.printf("+%04.6f ", memory[i+j]);
			}
			System.out.println();
		}
	}
	
	private static boolean isValid(int code) {
		return code > (-100 * MEMORY_CAPACITY) && code < (100 * MEMORY_CAPACITY );
	}
	
	private static boolean checkArithmeticErrors(int operationCode, int accumulator, int operand) {
		if (operationCode == DIVIDE && operand == 0)
			return false;
		if (operationCode == ADD && (accumulator + operand > MEMORY_CAPACITY * 100) || (accumulator + operand < (-100 * MEMORY_CAPACITY)))
			return false;
		if (operationCode == SUBTRACT && (accumulator - operand > MEMORY_CAPACITY * 100) || (accumulator - operand < (-100 * MEMORY_CAPACITY)))
			return false;
		if (operationCode == MULTIPLY && (accumulator * operand > MEMORY_CAPACITY * 100) || (accumulator * operand < (-100 * MEMORY_CAPACITY)))
			return false;
		if (operationCode == DIVIDE && (accumulator / operand > MEMORY_CAPACITY * 100) || (accumulator / operand < (-100 * MEMORY_CAPACITY)))
			return false;
		return true;
	}
}