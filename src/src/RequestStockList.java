package src;

public class RequestStockList {
    private StockType stockType;
    private String email;
    RequestStockList() {

    }



    RequestStockList(String email, StockType stockType) {
        this.email = email;
        this.stockType = stockType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public StockType getStockType() {
        return stockType;
    }

    public void setStockType(StockType stockType) {
        this.stockType = stockType;
    }

    enum StockType {
        STOCK_OBJECT,
        STOCK_NOTIFICATION
    }
}
