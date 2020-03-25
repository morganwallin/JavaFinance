package src;

import com.esotericsoftware.kryo.Kryo;
import io.reactivex.Single;

import java.util.ArrayList;

public class KryoHelper {
    static public void registerKryoClasses(Kryo kryo) {

            Single.just(kryo).subscribe(k -> {
                k.register(String.class);
                k.register(Double.class);
                k.register(ErrorMessage.class);
                k.register(RequestStockList.class);
                k.register(ArrayList.class);
                k.register(StockObject.class);
                k.register(StockNotification.class);
                k.register(StockObjectPlusEmail.class);
                k.register(StockNotification.SaveOrRemove.class);
                k.register(RequestStockList.StockType.class);
                k.register(StockObjectPlusEmail.SaveOrRemove.class);
                k.register(StockNotification.BelowOrAbove.class);
                k.register(FirstStockObjectListRequest.class);
                k.register(FirstStockObjectRequest.class);
                k.register(StockGraphInformation.class);
                k.register(StockGraphRequest.class);
            });

    }
}
