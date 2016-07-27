package simpletron;

import java.util.Scanner;
import java.util.Stack;

public class PostfixEvaluator {
	
	private StringBuffer postfix;	
	private Stack<Integer> evaluationStack;
	private int type;
	private int MEMORY_SIZE;
	private tableEntry[] tempInstruction;
	
	public PostfixEvaluator(StringBuffer postfix, int type, int MEMORY_SIZE) {		
		evaluationStack = new Stack<Integer>();		
		this.postfix = postfix;		
		this.type = type;
		tempInstruction = new tableEntry[1];
		this.MEMORY_SIZE = MEMORY_SIZE;
	}
	
	public int evaluatePostfixExpression() {		
		postfix.append(")");		
		tableEntry currTableEntry = null;		
		Scanner lineRead = new Scanner(postfix.toString());		
		String operator = "";
		int tempLocation = SimpleCompiler.getBackMemoryIndex() - 1;
		while (!operator.equals("(") && lineRead.hasNext()) {	
			String symbol = lineRead.next();
			operator = symbol;
			if ((!operator.equals("(") && !operator.equals(")"))
					&& !InfixToPostfixConverter.isOperator(operator)) {
				try {
					currTableEntry = SimpleCompiler.searchTable((char)Integer.parseInt(symbol));
					evaluationStack.push(currTableEntry.getLocation());
				}
				catch (NumberFormatException e) {
					currTableEntry = SimpleCompiler.searchTable(symbol.toCharArray()[0]);
					evaluationStack.push(currTableEntry.getLocation());
				}
				finally { //have to insert value 
					boolean isVar = false;
					try {
						Integer.parseInt(symbol);
					}
					catch (NumberFormatException e) {
						isVar = true;
					}
					char currType;
					if (isVar)
						currType = 'V';
					else
						currType = 'C';
					currTableEntry = new tableEntry(symbol.toCharArray()[0], currType, SimpleCompiler.getBackMemoryIndex());
					SimpleCompiler.insertTableEntry(currTableEntry);
					evaluationStack.push(currTableEntry.getLocation());
					SimpleCompiler.decrementBackMemoryIndex();
				}
			}
			else if (InfixToPostfixConverter.isOperator(symbol)){	
				//if there are any variables, load variable, then add other value (const or var) to accumulator. 
				//store result in temp location (kept consistent till the end
				int loc1 = evaluationStack.pop();
				int loc2 = evaluationStack.pop(); //evaluate op2 OPERATOR op1
				//load op2 into accumulator
				SimpleCompiler.insertSMLCode((SMLCodes.LOAD * MEMORY_SIZE) + loc2);
				//add op1 into accumulator
				switch(symbol) {
				case "+":
					SimpleCompiler.insertSMLCode((SMLCodes.ADD * MEMORY_SIZE) + loc1);
					break;
				case "-":
					SimpleCompiler.insertSMLCode((SMLCodes.SUBTRACT * MEMORY_SIZE) + loc1);
				case "/":
					SimpleCompiler.insertSMLCode((SMLCodes.DIVIDE * MEMORY_SIZE) + loc1);
					break;
				case "*":
					SimpleCompiler.insertSMLCode((SMLCodes.MULTIPLY * MEMORY_SIZE) + loc1);
					break;
				default:
					//throw an exception here
				}
				//store into temp tableEntry obj
				evaluationStack.push((SMLCodes.STORE * MEMORY_SIZE) + tempLocation);
				tempLocation--;
			}		
		}		
		return evaluationStack.pop();
	}
}