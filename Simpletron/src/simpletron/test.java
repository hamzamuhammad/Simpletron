package simpletron;

import javax.swing.JOptionPane;

public class test {
	public static void main (String [] args) {
//		StringBuffer test = new StringBuffer();
//		test.append("( 11 + 22 ) * 33 - a / 55");
//		
//		InfixToPostfixConverter i = new InfixToPostfixConverter(test);
//		System.out.println(i.convertToPostfix().toString());
		System.out.println(Integer.parseInt("a"));
		
	}

	private static String reverse(String s){
		if (s.length() == 1)
			return s;
		else
			return reverse(s.substring(1)) +  s.charAt(0);
		
	}
}
