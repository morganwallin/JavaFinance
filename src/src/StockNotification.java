package src;

public class StockNotification {
	enum SaveOrRemove {
		SAVE,
		REMOVE
	}
	enum BelowOrAbove {
		BELOW,
		ABOVE
	}


	private SaveOrRemove saveOrRemove;
	private BelowOrAbove belowOrAbove;
	private String email, symbol;
	private double price;
	StockNotification(String email, String symbol, BelowOrAbove belowOrAbove, double price, SaveOrRemove saveOrRemove) {
		this.email = email;
		this.symbol = symbol;
		this.belowOrAbove = belowOrAbove;
		this.price = price;
		this.saveOrRemove = saveOrRemove;
	}
	
	StockNotification() {
		
	}
	
	@Override
	public String toString() {
		String tmpString = "";
		if(belowOrAbove == BelowOrAbove.BELOW) {
			tmpString += " is less than ";
		}
		else {
			tmpString += " is greater than ";
		}
		return "E-Mail: " + email + "\t\t\tStock notification: " + symbol + tmpString + price;
		
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public BelowOrAbove getBelowOrAbove() {
		return belowOrAbove;
	}
	public void setBelowOrAbove(BelowOrAbove belowOrAbove) {
		this.belowOrAbove = belowOrAbove;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public SaveOrRemove getSaveOrRemove() {
		return saveOrRemove;
	}

	public void setSaveOrRemove(SaveOrRemove saveOrRemove) {
		this.saveOrRemove = saveOrRemove;
	}
}
