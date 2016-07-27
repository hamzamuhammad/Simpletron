package simpletron;

public class tableEntry {
	
	private int symbol;
	private char type;
	private int location;
	
	public tableEntry(int symbol, char type, int location) {
		this.setSymbol(symbol);
		this.setType(type);
		this.setLocation(location);
	}

	public int getSymbol() {
		return symbol;
	}

	public void setSymbol(int symbol) {
		this.symbol = symbol;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}
}
