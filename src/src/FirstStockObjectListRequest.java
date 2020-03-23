package src;

import java.util.ArrayList;

public class FirstStockObjectListRequest {
    private ArrayList<StockObjectPlusEmail> arrayList = new ArrayList<>();


    public ArrayList<StockObjectPlusEmail> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<StockObjectPlusEmail> arrayList) {
        this.arrayList = arrayList;
    }



    FirstStockObjectListRequest() {

    }

    FirstStockObjectListRequest(ArrayList<StockObjectPlusEmail> arrayList) {
        this.arrayList = arrayList;
    }
}
