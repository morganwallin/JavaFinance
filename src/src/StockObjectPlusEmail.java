package src;

public class StockObjectPlusEmail {
    StockObject stockObject;
    String email;
    SaveOrRemove saveOrRemove;
    enum SaveOrRemove {
        SAVE,
        REMOVE
    }
    StockObjectPlusEmail() {

    }



    StockObjectPlusEmail(StockObject stockObject, String email, SaveOrRemove saveOrRemove) {
        this.stockObject = stockObject;
        this.email = email;
        this.saveOrRemove = saveOrRemove;
    }

    public StockObject getStockObject() {
        return stockObject;
    }

    public void setStockObject(StockObject stockObject) {
        this.stockObject = stockObject;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public SaveOrRemove getSaveOrRemove() {
        return saveOrRemove;
    }

    public void setSaveOrRemove(SaveOrRemove saveOrRemove) {
        this.saveOrRemove = saveOrRemove;
    }
}
