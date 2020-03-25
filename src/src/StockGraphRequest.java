package src;

import yahoofinance.Stock;

public class StockGraphRequest {
    private String stockSymbol;
    StockGraphRequest() {

    }
    StockGraphRequest(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }
}
