package src;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

//Alpha vantage API KEY XMZBKLDI65RQYS6D

public class KryoServer {
    private Server server = new Server(10000000, 10000000);
    private StockDatabase stockDatabase = new StockDatabase("notificationDatabase.db");
    private ArrayList<StockNotification> notificationArrayList = new ArrayList<>();
    private Disposable notificationDisposable = new Disposable() {
        volatile boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    };
    private Disposable timerDisposable = new Disposable() {
        volatile boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return false;
        }
    };

    private KryoServer() {
        Kryo kryo = server.getKryo();
        KryoHelper.registerKryoClasses(kryo);
        try {
            server.start();
            server.bind(54555, 54777);
        } catch (IOException e) {
            System.err.println("Error starting server, aborting... is it already started?");
            return;
        }

        stockDatabase.connect();

        server.addListener(new Listener() {

            public void received(Connection connection, Object object) {

                if (object instanceof String) {
                    timerDisposable.dispose();
                    try {
                        Stock stock = YahooFinance.get((String) object);
                        if (stock == null || !stock.isValid()) {
                            server.sendToTCP(connection.getID(),
                                    new ErrorMessage("searchStockError", "Couldn't find stock"));
                        } else {

                            StockObject stockObj = new StockObject(stock);
                            timerDisposable = Observable.interval(5, TimeUnit.SECONDS, Schedulers.io())
                                    .take(Long.MAX_VALUE)
                                    .map(v -> Long.MAX_VALUE - v)
                                    .subscribe(
                                            onNext -> {
                                                server.sendToTCP(connection.getID(), stockObj);
                                            },
                                            onError -> {
                                                //do on error
                                            },
                                            () -> {
                                                //do on complete
                                            },
                                            onSubscribe -> {
                                                server.sendToTCP(connection.getID(), new FirstStockObjectRequest(stockObj));
                                            });


                        }


                    } catch (IOException e) {
                        server.sendToTCP(connection.getID(),
                                new ErrorMessage("searchStockError", "Couldn't find stock"));
                    }
                }

                if (object instanceof StockObjectPlusEmail) {
                    timerDisposable.dispose();
                    StockObjectPlusEmail stockObjectPlusEmail = (StockObjectPlusEmail) object;
                    if (stockObjectPlusEmail.getSaveOrRemove() == StockObjectPlusEmail.SaveOrRemove.REMOVE) {
                        removeStockObject(stockObjectPlusEmail);
                    } else {
                        saveStockObject(connection, stockObjectPlusEmail);
                    }

                }

                if (object instanceof StockNotification) {
                    timerDisposable.dispose();
                    StockNotification stockNotification = (StockNotification) object;
                    if (stockNotification.getSaveOrRemove() == StockNotification.SaveOrRemove.REMOVE) {
                        removeStockNotification(stockNotification);
                    } else if (stockNotification.getSaveOrRemove() == StockNotification.SaveOrRemove.SAVE) {
                        saveStockNotification(connection, stockNotification);
                    }

                }

                if (object instanceof RequestStockList) {
                    timerDisposable.dispose();
                    RequestStockList rst = (RequestStockList) object;
                    if (rst.getStockType() == RequestStockList.StockType.STOCK_NOTIFICATION) {
                        sendStockNotificationList(connection, rst);
                    } else if (rst.getStockType() == RequestStockList.StockType.STOCK_OBJECT) {
                        timerDisposable = Observable.interval(5, TimeUnit.SECONDS, Schedulers.io())
                                .take(Long.MAX_VALUE)
                                .map(v -> Long.MAX_VALUE - v)
                                .subscribe(
                                        onNext -> {
                                            sendStockObjectList(connection, rst, false);
                                        },
                                        onError -> {
                                            //do on error
                                        },
                                        () -> {
                                            //do on complete
                                        },
                                        onSubscribe -> {
                                            sendStockObjectList(connection, rst, true);
                                        });

                    }
                }

                if(object instanceof StockGraphRequest) {
                    StockGraphRequest sgr = (StockGraphRequest) object;
                    Stock stock;
                    StockGraphInformation stockGraphInformation;
                    try {
                        stock = YahooFinance.get(sgr.getStockSymbol());
                        stockGraphInformation = new StockGraphInformation(stock);
                        server.sendToTCP(connection.getID(), stockGraphInformation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }




            }
        });

        JFrame frame = new JFrame("Stock Exchange server");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        /*SendEmail sendEmail = new SendEmail("morgan_wallin@hotmail.com", "subject", "text");
                                                Thread thread = new Thread(sendEmail);
                                                thread.start();*/
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent evt) {
                //server.close();
                server.stop();
                notificationDisposable.dispose();
            }
        });
        JToggleButton startNotificationServerButton = new JToggleButton("Start notification server");


        startNotificationServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                if (btn.isSelected()) {
                    startNotificationServerButton.setText("Stop notification server");

                    notificationDisposable = Observable.interval(30, TimeUnit.SECONDS, Schedulers.io())
                            .take(Long.MAX_VALUE)
                            .map(v -> Long.MAX_VALUE - v)
                            .subscribe(
                                    onNext -> {
                                        checkIfNotificationGotTriggered();
                                    },
                                    onError -> {
                                        //do on error
                                    },
                                    () -> {
                                        //do on complete
                                    },
                                    onSubscribe -> initializeNotificationArrayList());
                } else {
                    startNotificationServerButton.setText("Start notification server");
                    notificationDisposable.dispose();
                }
            }
        });


        frame.getContentPane().add(new JLabel("Close to stop the stocks\n and notification server."));
        frame.getContentPane().add(startNotificationServerButton);
        frame.setSize(320, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new KryoServer();
    }

    public static StockObject createStockObjectFromStockNotification(StockNotification stockNotification) {
        Stock stock = null;
        try {
            stock = YahooFinance.get(stockNotification.getSymbol());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (stock != null) {
            stock.getQuote().setPrice(BigDecimal.valueOf(stockNotification.getPrice()));
            return new StockObject(stock);
        } else {
            return new StockObject();
        }
    }

    private void checkIfNotificationGotTriggered() {
        ArrayList<StockNotification> newArrayList = new ArrayList<>();
        notificationArrayList.forEach(notification -> {
            StockObject stockObject = createStockObjectFromStockNotification(notification);
            try {
                stockObject.setPrice(YahooFinance.get(stockObject.getSymbol()).getQuote().getPrice().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            SendEmail sendEmail;
            if (stockObject.getPrice().equals("N/A") || stockObject.getPrice() == null || stockObject.getPrice().equals("")) {
            } else {
                try {
                    double price = Double.parseDouble(stockObject.getPrice());
                    String notificationMessage = "Your monitored stock " + stockObject.getSymbol() + " currently costs " + stockObject.getPrice() +
                            " and is therefore ";
                    if (notification.getBelowOrAbove() == StockNotification.BelowOrAbove.BELOW) {
                        if (price < notification.getPrice()) {
                            notificationMessage += "below your threshold of " + notification.getPrice() + ".";
                            sendEmail = new SendEmail(notification.getEmail(), "Stock Notification Alert", notificationMessage);
                            Thread notificationThread = new Thread(sendEmail);
                            notificationThread.start();

                        } else {
                            newArrayList.add(notification);
                        }
                    } else {
                        if (price > notification.getPrice()) {
                            notificationMessage += "above your threshold of " + notification.getPrice() + ".";
                            sendEmail = new SendEmail(notification.getEmail(), "Stock Notification Alert", notificationMessage);
                            Thread notificationThread = new Thread(sendEmail);
                            notificationThread.start();
                        } else {
                            newArrayList.add(notification);
                        }
                    }
                } catch (NullPointerException | NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });

        notificationArrayList = newArrayList;

    }

    private void initializeNotificationArrayList() {
        ResultSet rs = stockDatabase.selectAllNotifications();

        try {
            while (rs.next()) {

                StockNotification.BelowOrAbove belowOrAbove = StockNotification.BelowOrAbove.ABOVE;
                if (rs.getString("below_or_above").equals("Below")) {
                    belowOrAbove = StockNotification.BelowOrAbove.BELOW;
                }

                StockNotification stockNotification = new StockNotification(rs.getString("email"), rs.getString("stocksymbol"),
                        belowOrAbove, rs.getDouble("price"), StockNotification.SaveOrRemove.SAVE);

                notificationArrayList.add(stockNotification);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void sendStockNotificationList(Connection connection, RequestStockList rst) {
        ResultSet resultSet = stockDatabase.selectStocksWithEmail(rst.getEmail(), RequestStockList.StockType.STOCK_NOTIFICATION);
        ArrayList<StockNotification> resultArrayList = new ArrayList<StockNotification>();
        try {
            while (resultSet.next()) {
                StockNotification.BelowOrAbove belowOrAbove = StockNotification.BelowOrAbove.ABOVE;
                if (resultSet.getString("below_or_above").equals("Below")) {
                    belowOrAbove = StockNotification.BelowOrAbove.BELOW;
                }
                StockNotification sn = new StockNotification(rst.getEmail(), resultSet.getString("stocksymbol"),
                        belowOrAbove, resultSet.getDouble("price"), StockNotification.SaveOrRemove.SAVE);
                resultArrayList.add(sn);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        server.sendToTCP(connection.getID(), resultArrayList);

        if (resultArrayList.isEmpty()) {
            server.sendToTCP(connection.getID(),
                    new ErrorMessage("loadNotificationError", "No saved notifications found"));
        }
    }

    private void sendStockObjectList(Connection connection, RequestStockList rst, boolean firstSend) {
        ResultSet resultSet = stockDatabase.selectStocksWithEmail(rst.getEmail(), RequestStockList.StockType.STOCK_OBJECT);
        ArrayList<StockObjectPlusEmail> resultArrayList = new ArrayList<StockObjectPlusEmail>();
        try {
            while (resultSet.next()) {
                Stock stock = null;
                try {
                    stock = YahooFinance.get(resultSet.getString("stocksymbol"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (stock == null) {
                    server.sendToTCP(connection.getID(),
                            new ErrorMessage("searchStockError", "Could not load list from email"));
                    timerDisposable.dispose();
                    return;
                }
                StockObjectPlusEmail stockObjectPlusEmail = new StockObjectPlusEmail(new StockObject(stock), rst.getEmail(), StockObjectPlusEmail.SaveOrRemove.SAVE);
                resultArrayList.add(stockObjectPlusEmail);

            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!firstSend) {
            server.sendToTCP(connection.getID(), resultArrayList);
        } else {
            FirstStockObjectListRequest firstStockObjectListRequest = new FirstStockObjectListRequest(resultArrayList);
            server.sendToTCP(connection.getID(), firstStockObjectListRequest);
        }

        if (resultArrayList.isEmpty()) {
            server.sendToTCP(connection.getID(),
                    new ErrorMessage("searchStockError", "No saved stocks found"));
            timerDisposable.dispose();
        }
    }

    private void removeStockNotification(StockNotification stockNotification) {
        stockDatabase.removeNotification(stockNotification);
        notificationArrayList.removeIf(e -> e.getSymbol().equals(stockNotification.getSymbol()) && e.getEmail().equals(stockNotification.getEmail())
                && e.getBelowOrAbove() == stockNotification.getBelowOrAbove());
    }

    private void removeStockObject(StockObjectPlusEmail stockObjectPlusEmail) {
        stockDatabase.removeStockObject(stockObjectPlusEmail);
    }

    private void saveStockNotification(Connection connection, StockNotification stockNotification) {
        Stock stock = null;
        try {
            stock = YahooFinance.get(stockNotification.getSymbol());
            if (stock == null) {
                server.sendToTCP(connection.getID(),
                        new ErrorMessage("saveNotificationError", "Could not find stock to save"));
                return;
            }
        } catch (Exception e) {
            server.sendToTCP(connection.getID(),
                    new ErrorMessage("saveNotificationError", "Could not find stock to save"));
            return;
        }

        if (stockDatabase.duplicateEntryExists(stockNotification)) {
            server.sendToTCP(connection.getID(),
                    new ErrorMessage("saveNotificationError", "Entry already found, updated values"));

            notificationArrayList.removeIf(e -> e.getSymbol().equals(stockNotification.getSymbol()) && e.getEmail().equals(stockNotification.getEmail())
                    && e.getBelowOrAbove() == stockNotification.getBelowOrAbove());
        }
        stockDatabase.insertStockNotification(stockNotification);

        notificationArrayList.add(stockNotification);
    }

    private void saveStockObject(Connection connection, StockObjectPlusEmail stockObjectPlusEmail) {
        Stock stock = null;
        try {
            stock = YahooFinance.get(stockObjectPlusEmail.getStockObject().getSymbol());
            if (stock == null) {
                server.sendToTCP(connection.getID(),
                        new ErrorMessage("searchStockError", "Could not find stock to save"));
                return;
            }
        } catch (Exception e) {
            server.sendToTCP(connection.getID(),
                    new ErrorMessage("searchStockError", "Could not find stock to save"));
            return;
        }

        if (stockDatabase.duplicateEntryExists(stockObjectPlusEmail)) {
            server.sendToTCP(connection.getID(),
                    new ErrorMessage("searchStockError", "Entry already found, updated values"));
        }
        stockDatabase.insertStockObjectToPersonalList(stockObjectPlusEmail);
    }


}
