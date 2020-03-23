package src;

import yahoofinance.Stock;

import java.io.IOException;

public class StockObject {
	private String name;
	private String exchange;
	private String price;
	private String currency;
	private String symbol;
	private String changeInPercentSinceYesterday;
	private String ask;
	private String bid;
	private String dailyLow;
	private String dailyHigh;
	private String peNumber;
	private String epsNumber;
	private String header = "";


	StockObject() {
		name = "N/A";
		exchange = "N/A";
		price = "N/A";
		currency = "N/A";
		symbol = "N/A";
		changeInPercentSinceYesterday = "N/A";
		ask = "N/A";
		bid = "N/A";
		dailyLow = "N/A";
		dailyHigh = "N/A";
		peNumber = "N/A";

	}

	StockObject(String header) {
		this.header = header;
	}

	StockObject(Stock stock) {
		// The reason for all the complexity is that some objects in a "stock" can be null, even
		// though the stock is not null. All variables initialized to "N/A" first.
		this();
		if (stock.getName() != null) {
			this.name = stock.getName();
		}
		if (stock.getStockExchange() != null) {
			this.exchange = stock.getStockExchange();
		}
		if (stock.getCurrency() != null) {
			this.currency = stock.getCurrency();
		}
		if (stock.getQuote().getPrice() != null) {
			this.price = stock.getQuote().getPrice().toString();
		}
		if (stock.getSymbol() != null) {
			this.symbol = stock.getSymbol();
		}
		if (stock.getQuote().getChangeInPercent() != null) {
			this.changeInPercentSinceYesterday = stock.getQuote().getChangeInPercent().toString();
		}
		if (stock.getQuote().getAsk() != null) {
			this.ask = stock.getQuote().getAsk().toString();
		}
		if (stock.getQuote().getBid() != null) {
			this.bid = stock.getQuote().getBid().toString();
		}
		if (stock.getQuote().getDayLow() != null) {
			this.dailyLow = stock.getQuote().getDayLow().toString();
		}
		if (stock.getQuote().getDayHigh() != null) {
			this.dailyHigh = stock.getQuote().getDayHigh().toString();
		}
		if (stock.getStats().getPe() != null) {
			this.peNumber = stock.getStats().getPe().toString();
		}
		if (stock.getStats().getEps() != null) {
			this.epsNumber = stock.getStats().getEps().toString();
		}
	}


	private String getStockString() {
		String stockString = "Name: " + getName() + "\n" + "Stock Exchange: " + getExchange() + "\n" + "Price: "
				+ getPrice() + " " + getCurrency() + "\n" + "Symbol: " + getSymbol() + "\nPercent from previous close: "
				+ getChangeInPercentSinceYesterday() + "%\n" + "\nAsk: " + getAsk() + " " + getCurrency() + " \t\tBid: "
				+ getBid() + " " + getCurrency() + "\nDaily low: " + getDailyLow() + " " + getCurrency()
				+ " \tDaily high: " + getDailyHigh() + " " + getCurrency() + "\n" + "PE: " + getPeNumber() + "\t\tEPS: "
				+ getEpsNumber();

		return stockString;
	}


	@Override
	public String toString() {
		if (!header.equals("")) {
			return header;
		}
		return getStockString();
	}

	public void updateStockObject(StockObject stockObject) {
		this.name = stockObject.getName();
		this.exchange = stockObject.getExchange();
		this.price = stockObject.getPrice();
		this.currency = stockObject.getCurrency();
		this.symbol = stockObject.getSymbol();
		this.changeInPercentSinceYesterday = stockObject.getChangeInPercentSinceYesterday();
		this.ask = stockObject.getAsk();
		this.bid = stockObject.getBid();
		this.dailyLow = stockObject.getDailyLow();
		this.dailyHigh = stockObject.getDailyHigh();
		this.peNumber = stockObject.getPeNumber();
		this.epsNumber = stockObject.getEpsNumber();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getChangeInPercentSinceYesterday() {
		return changeInPercentSinceYesterday;
	}

	public void setChangeInPercentSinceYesterday(String changeInPercentSinceYesterday) {
		this.changeInPercentSinceYesterday = changeInPercentSinceYesterday;
	}

	public String getAsk() {
		return ask;
	}

	public void setAsk(String ask) {
		this.ask = ask;
	}

	public String getBid() {
		return bid;
	}

	public void setBid(String bid) {
		this.bid = bid;
	}

	public String getDailyLow() {
		return dailyLow;
	}

	public void setDailyLow(String dailyLow) {
		this.dailyLow = dailyLow;
	}

	public String getDailyHigh() {
		return dailyHigh;
	}

	public void setDailyHigh(String dailyHigh) {
		this.dailyHigh = dailyHigh;
	}

	public String getPeNumber() {
		return peNumber;
	}

	public void setPeNumber(String peNumber) {
		this.peNumber = peNumber;
	}

	public String getEpsNumber() {
		return epsNumber;
	}

	public void setPesNumber(String epsNumber) {
		this.epsNumber = epsNumber;
	}

}
