package src;

public class FirstStockObjectRequest {
    public StockObject getStockObject() {
        return stockObject;
    }

    public void setStockObject(StockObject stockObject) {
        this.stockObject = stockObject;
    }

    StockObject stockObject;
    FirstStockObjectRequest() {

    }
    FirstStockObjectRequest(StockObject stockObject) {
        this.stockObject = stockObject;
    }
}
