/* The Simple virtual machine which runs an SML program. 
 * 				� 2015 Hamza Muhammad
 */
package simpletron;

//necessary libraries that provide the GUI for the virtual machine along with 
//file reading and user input
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Formatter;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import simpletron.SMLCodes;

public class SimpleMain {
	
	//fixed constant that determines how 'big' an SML program can be
	private static final int MEMORY_CAPACITY = 100;
	//2d array where the TOP row is a 'marker' array, and BOTTOM is actual memory
	private static int[][] memory;
	//marker used to indicate unoccupied memory
	private static final int EMPTY = 0; 
	//marker used to indicate start/end of a string sequence
	private static final int STRING = -2;
	//determines whether we are accessing the 'marker' array or the 'memory' one
	private static final int MEM = 0;
	private static final int MARKER = 1;
	//String constants that hold both standard intro text and button choices
	private static final String intro = 
			"Welcome to Simple!\n� 2015 Hamza Muhammad\n";
	private static final String[] options = new String[] {"Run", "Compile", 
		"Settings", "Exit"};
	private static boolean debug = false;
	private static boolean memoryDump = false;
	private static Formatter f;

	/* In the main method, we first initialize a JFrame to hold all of our GUI
	 * applets. Then, we call the mainMenu method that is essentially the whole
	 * program.
	 */
	public static void main (String [] args) throws IOException {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f = new Formatter(new FileOutputStream("memory_dump.md"));
		mainMenu(frame);
	}

	/* The actual program. Presents a pop-up box with 4 buttons for the user to
	 * select, which determines what the program does.
	 */
	public static void mainMenu(JFrame frame) throws IOException {
		memory = new int[2][MEMORY_CAPACITY];
		int response = JOptionPane.showOptionDialog(frame, intro, 
				"Simple version 1.0", JOptionPane.DEFAULT_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		switch(response) {
			case 0: loadProgram(frame); break;
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
	private static void loadProgram(JFrame frame) throws IOException {
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
					memory[MARKER][index] = -1;
					index++;
				}
				else {
					smlReader.close();
					throw new IllegalArgumentException("INVALID SML INSTRUCTION"
							+ ": ABORTING EXECUTION");
				}
			}
			smlReader.close();
			runProgram(frame);
		}
		else
			mainMenu(frame);
	}
	
//	private static void compileProgram(JFrame frame) {
//		SimpleCompiler sc = new SimpleCompiler(frame);
//		sc.compile();
//	}
	
	/* Parent method that uses a JFrame to host a JComponent (composed of a 
	 * JPanel holding 2 radio button and button) and set it to be visible.
	 */
	private static void showSettings(JFrame frame) {
	    frame = new SettingsCheckBox(frame);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setSize(200,140);
        frame.setResizable(false);
	}
	
	/* Inner class that creates all of the GUI objects necessary for both the
	 * radio buttons and regular buttons to work; certain actions are done when
	 * any of them are pressed.
	 */
	private static class SettingsCheckBox extends JFrame implements ActionListener {
		
		private static final long serialVersionUID = 1L;
		private static JCheckBox debugButton = new JCheckBox("debug mode");
		private static JCheckBox memoryDumpButton = new JCheckBox("memory dump "
				+ "to disk");
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
		    else if (!debugButton.isSelected())
		    	debug = false;
		    if (memoryDumpButton.isSelected())
		    	memoryDump = true;
		    else if (!memoryDumpButton.isSelected())
		    	memoryDump = false;
		    if (command.equals("DONE")) {
		    	try {
		    		dispose();
		    		mainMenu(frame);
		    	} 
		    	catch (IOException e) {
		    		throw new IllegalArgumentException("No file found");
		    	}
		    }
		 }
	}
	
	/* The actual part where the program is executed. While the program is still
	 * running and there is still memory, go through memory and execute each
	 * instruction.
	 */
	private static void runProgram(JFrame frame) throws IOException {
		int instructionCounter = 0, instructionRegister = 0;
		int accumulator = 0;
		int operationCode = 0, operand = 0;
		boolean programEnd = false;
		while (!programEnd && instructionCounter < memory[0].length) {
			instructionRegister = memory[MEM][instructionCounter];
			operationCode = (int) (instructionRegister / MEMORY_CAPACITY);
			operand = (int) (instructionRegister % MEMORY_CAPACITY);
			if (operationCode >= SMLCodes.ADD && operationCode < SMLCodes.BRANCH)
				if (!checkArithmeticErrors(operationCode, accumulator, 
						memory[MEM][operand])) {
					JOptionPane.showMessageDialog(frame, "DIVISION"
							+ "BY ZERO AND/OR MEMORY OVERFLOW: EXECUTION ABORTED", 
							"RUNTIME ERROR", JOptionPane.ERROR_MESSAGE, null);
					programEnd = true;
					
				}
			if (!programEnd) {
				switch (operationCode) {
				case EMPTY:
					programEnd = true;
					break;
				case SMLCodes.READ: 
					String value = JOptionPane.showInputDialog(frame, null, 
							"Input a value", JOptionPane.QUESTION_MESSAGE);
					try {
						int num = Integer.parseInt(value);
						memory[MEM][operand] = num;
					} 
					catch (NumberFormatException e) {
						memory[MARKER][operand] = STRING;
						char[] letters = value.toUpperCase().toCharArray();
						int endVal = letters.length / 2;
						int leftOver = letters.length % 2;
						int wordIndex = 0;
						String temp = "";
						for (int index = 0; index < endVal; index++) {
							temp += (int) letters[wordIndex];
							temp += (int) letters[wordIndex + 1];
							memory[MEM][operand] = Integer.parseInt(temp);
							memory[MARKER][operand] = STRING;
							wordIndex += 2;
							operand++;
							temp = "";
						}
						for (int index = 0; index < leftOver; index++)
							temp += (int) letters[wordIndex + index];
						if (leftOver != 0) {
							memory[MEM][operand] = Integer.parseInt(temp);
							memory[MARKER][operand] = STRING;
							operand++;
						}
					}
					break;
				case SMLCodes.WRITE: //special print case for a string!
					StringBuffer sb = new StringBuffer();	
					if (memory[MARKER][operand] == STRING) {						
						while (memory[MARKER][operand] == STRING) {
							int firstLetter = (int) (memory[MEM][operand] / 100);
							int secondLetter = (int) (memory[MEM][operand] % 100);
							if (firstLetter == 0)
								sb.append((char)secondLetter);
							else
								sb.append((char)firstLetter + "" + 
							(char)secondLetter);
							operand++;							
						}						
						JOptionPane.showMessageDialog(frame, sb.toString(), 
								"Results", JOptionPane.INFORMATION_MESSAGE);						
					}
					else
						JOptionPane.showMessageDialog(frame, memory[MEM][operand], 
								"Results", JOptionPane.INFORMATION_MESSAGE);	
					break;				
				case SMLCodes.NEWLINE:
					System.out.println();
					break;					
				case SMLCodes.LOAD:
					accumulator = memory[MEM][operand];
					break;					
				case SMLCodes.STORE:
					memory[MEM][operand] = accumulator;
					break;					
				case SMLCodes.ADD:
					accumulator += memory[MEM][operand];;
					break;					
				case SMLCodes.SUBTRACT:
					accumulator -= memory[MEM][operand];;
					break;					
				case SMLCodes.DIVIDE:
					accumulator /= memory[MEM][operand];;
					break;					
				case SMLCodes.MULTIPLY:
					accumulator *= memory[MEM][operand];;
					break;					
				case SMLCodes.MODULUS:
					accumulator %= memory[MEM][operand];;
					break;					
				case SMLCodes.POWER:
					accumulator = (int) Math.pow(accumulator, memory[MEM][operand]);
					break;					
				case SMLCodes.BRANCH:
					instructionCounter = operand;
					break;					
				case SMLCodes.BRANCHNEG:
					if (accumulator < 0)
						instructionCounter = operand;
					break;					
				case SMLCodes.BRANCHZERO:
					if (accumulator == 0)
						instructionCounter = operand;
					break;					
				case SMLCodes.HALT:
					JOptionPane.showMessageDialog(frame, null, 
							"PROGRAM PAUSED", JOptionPane.INFORMATION_MESSAGE);
					break;					
				default:
					throw new IllegalArgumentException("Unknown SML instruction."
							+ " Execution aborted.");
				}				
			}			
			if (operationCode != SMLCodes.BRANCH && operationCode != SMLCodes.BRANCHNEG && 
					operationCode != SMLCodes.BRANCHZERO)
				++instructionCounter;			
			if (debug) {
				//for each instruction cycle, print current variable info
				JOptionPane.showMessageDialog(frame, variableInfo(accumulator, 
						instructionCounter, instructionRegister, operationCode, 
						operand).toString(), 
						"Debugging: line " + instructionCounter
						, JOptionPane.INFORMATION_MESSAGE);
				
			}						
		}		
		if (memoryDump) {			
			memoryDumpToFile(accumulator, instructionCounter, instructionRegister, 
					operationCode, operand);		
		}	
		JOptionPane.showMessageDialog(frame, "Program has finished running", 
				"STATUS: "  + (memoryDump ? "MEM DUMP DONE" : "FINISHED"),
				JOptionPane.INFORMATION_MESSAGE);      
        mainMenu(frame);
	}

	// Simply formats variable information in an easy to understand String
	private static StringBuffer variableInfo(int accumulator, int instructionCounter, 
			int instructionRegister, int operationCode, int operand) {		
		StringBuffer sb = new StringBuffer();		
		sb.append("\n*** MEMORY DUMP *** \n");
		sb.append("REGISTERS:\n");
		sb.append(String.format("accumulator          +%04d\n", accumulator));
		sb.append(String.format("instructionCounter    %02d\n", instructionCounter));
		sb.append(String.format("instructionRegister  +%04d\n", instructionRegister));
		sb.append(String.format("operationCode         %02d\n", operationCode));
		sb.append(String.format("operand               %02d\n", operand));	
		return sb;
	}
	
	/* If the user selects memory dump to disk as enabled, all variables and
	 * memory at the end of the program is printed and formatted for readibility
	 * and then printed to a file on the disk titled "memory_dump.txt"
	 */
	private static void memoryDumpToFile(int accumulator, int instructionCounter, 
			int instructionRegister, int operationCode, int operand) 
			throws IOException {	
		f.format("\n*** MEMORY DUMP *** \n");
		f.format("\nREGISTERS:\n");
		f.format("accumulator          +%04d\n", accumulator);
		f.format("instructionCounter    %02d\n", instructionCounter);
		f.format("instructionRegister  +%04d\n", instructionRegister);
		f.format("operationCode         %02d\n", operationCode);
		f.format("operand               %02d\n", operand);
		f.format("\n");
		for (int i = 0; i < 10; i++)
			f.format("%5d ", i);
		f.format("\n");
		for (int i = 0; i < MEMORY_CAPACITY; i += 10) {	
			f.format("%2d ", i);			
			for (int j = 0; j < 10; j++) 
				f.format("+%04d ", memory[MEM][i+j]);			
			f.format("\n");			
		}		
		f.close();
	}
	
	//sees whether an SML instruction is within memory range
	private static boolean isValid(int code) {
		return code > (-100 * MEMORY_CAPACITY) && code < (100 * MEMORY_CAPACITY );
	}
	
	//checks whether an operation results in either an overflow, or divide by 
	//zero exception.
	private static boolean checkArithmeticErrors(int operationCode, int accumulator,
			int operand) {		
		if (operationCode == SMLCodes.DIVIDE && operand == 0)
			return false;		
		if (operationCode == SMLCodes.ADD && ((accumulator + operand > MEMORY_CAPACITY * 100) 
				|| (accumulator + operand < (-100 * MEMORY_CAPACITY))))
			return false;		
		if (operationCode == SMLCodes.SUBTRACT && ((accumulator - operand > MEMORY_CAPACITY * 100) 
				|| (accumulator - operand < (-100 * MEMORY_CAPACITY))))
			return false;		
		if (operationCode == SMLCodes.MULTIPLY && ((accumulator * operand > MEMORY_CAPACITY * 100) 
				|| (accumulator * operand < (-100 * MEMORY_CAPACITY))))
			return false;		
		if (operationCode == SMLCodes.DIVIDE && ((accumulator / operand > MEMORY_CAPACITY * 100) 
				|| (accumulator / operand < (-100 * MEMORY_CAPACITY))))
			return false;		
		return true;		
	}	
}