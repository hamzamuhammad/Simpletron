package simpletron;

import java.util.Scanner;
import java.util.Stack;

public class InfixToPostfixConverter {
	
	private Stack<String> conversionStack;	
	private StringBuffer infix, postfix;	
	private static final String operators = "+-*/^%";	
	private static String[] operatorPrecedence = {"^", "/%*", "+-"};
	
	public InfixToPostfixConverter(StringBuffer infix) {		
		conversionStack = new Stack<String>();		
		this.infix = infix;		
		postfix = new StringBuffer();		
	}
	
	public StringBuffer convertToPostfix() {		
		conversionStack.push("(");		
		infix.append(" )");				
		Scanner line = new Scanner(infix.toString());		
		while (!conversionStack.isEmpty() && line.hasNext()) {			
			String currWord = line.next();
			try {
				int num = Integer.parseInt(currWord);			
				postfix.append(num);
				postfix.append(" ");
			}
			catch (NumberFormatException e) {
				if (currWord.equals("("))
					conversionStack.push(currWord);
				else if (isOperator(currWord)) {
					boolean isValid = true;
					while (!conversionStack.isEmpty() && isValid) {
						if (isOperator(conversionStack.peek()) && precedence(currWord, conversionStack.peek())) {
							postfix.append(conversionStack.pop());
							postfix.append(" ");
						}
						else
							isValid = false;
					}
					conversionStack.push(currWord);
				}
				else if (currWord.equals(")")) {
					boolean isLeftParentheses = false;
					while (!conversionStack.isEmpty() && !isLeftParentheses) {
						if (!conversionStack.peek().equals("(")) {
							postfix.append(conversionStack.pop());
							postfix.append(" ");
						}
						else
							isLeftParentheses = true;
					}
					if (isLeftParentheses)
						conversionStack.pop();
				}
				else {
					postfix.append(currWord);
					postfix.append(" ");
				}
			}
		}		
		line.close();		
		return postfix;		
	}
	
	public static boolean isOperator(String operator) {
		return operators.indexOf(operator) != -1;
	}
	
	private boolean precedence(String operator1, String operator2) {		
		int firstIndex = 0, secondIndex = 0;
		int count = 0;	
		for (String operator : operatorPrecedence) {		
			if (operator.indexOf(operator1) != -1)
				firstIndex = count;
			if (operator.indexOf(operator2) != -1)
				secondIndex = count;			
			count++;			
		}		
		return firstIndex > secondIndex;		
	}
}