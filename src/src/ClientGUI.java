package src;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.reactivex.Observable;
import io.reactivex.Single;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class ClientGUI extends Application {
    /*
     * General program members
     */
    private boolean connected = false, validSubmission = true, recentlyAddedAStock = false, singleStockView = true;
    private Single<Client> client = Single.just(new Client(10000000, 10000000));
    private final Kryo kryo = client.blockingGet().getKryo();
    private StockObject currentStock;

    /*
     * Left VBox (Settings, searchfields, options, buttons etc) members
     */
    private VBox centerVbox = new VBox();
    private Text searchStockText = new Text("Search stock:"), searchStockErrorText = new Text(""), emailText = new Text("E-Mail:");
    private TextField searchStockTextField = new TextField();
    private ButtonBar timeButtonBar = new ButtonBar(), buttonBarNotification = new ButtonBar();
    private Button saveStockButton = new Button("Save stock to personal list"), stockListButton = new Button("Personal stock list"),
            removeStockButton = new Button("Remove highlighted stock");
    private ToggleButton notificationSettingsButton = new ToggleButton("Notification settings");
    private Text submitNotificationRequestErrorText = new Text(""), loadNotificationsRequestErrorText = new Text("");
    private TextField notificationStockSymbol = new TextField(), notificationPrice = new TextField(),
            notificationEmail = new TextField();
    private Button loadNotificationList = new Button("Load notifications list"),
            submitNotificationRequestButton = new Button("Save notification"),
            removeNotificationButton = new Button("Remove highlighted notification");
    private ToggleButton abovePriceThreshold = new ToggleButton("Above"), belowPriceThreshold = new ToggleButton("Below");
    private ToggleGroup aboveOrBelow = new ToggleGroup();
    private Text usageText = new Text(
            "\n\n\n\n\n\n\nUsage: Search for a stock\n"
                    + "in the search bar at the top.\n"
                    + "For saving and loading\n"
                    + "personal/notification lists,\n"
                    + "e-mail needs to be supplied\n"
                    + "in the box labeled \"E-mail\"");

    /*
     * Observables
     */
    private Observable<ButtonBase> mainButtons = Observable.just(saveStockButton, stockListButton, notificationSettingsButton);
    private Observable<Node> allNotificationObjectsObservable = Observable.just(buttonBarNotification,
            submitNotificationRequestButton, loadNotificationList, notificationStockSymbol, notificationPrice,
            submitNotificationRequestErrorText, loadNotificationsRequestErrorText, removeNotificationButton);
    private Observable<TextField> requiredNotificationTextFields = Observable.just(notificationStockSymbol, notificationPrice,
            notificationEmail);
    private Observable<Text> errorMessageObservable = Observable.just(submitNotificationRequestErrorText, loadNotificationsRequestErrorText,
            searchStockErrorText);

    /*
     * ListViews for displaying stocks (main window)
     */
    private ListView<StockNotification> stockNotificationListView = new ListView<StockNotification>();
    private ListView<StockObject> stockObjectListView = new ListView<StockObject>();
    private StockNotification selectedStockNotification;


    public static void main(String[] args) {
        launch(args);
    }

    private void setupTimeButtonBar() {
        ToggleGroup timeSetting = new ToggleGroup();
        ToggleButton weekly = new ToggleButton("W"), monthly = new ToggleButton("M"), yearly = new ToggleButton("Y");
        Observable.just(weekly, monthly, yearly).subscribe(tb -> {
            tb.setMinWidth(40);
            tb.setToggleGroup(timeSetting);
        });

        timeButtonBar.getButtons().addAll(weekly, monthly, yearly);
        timeButtonBar.setButtonMinWidth(40);
        timeButtonBar.setMaxWidth(130);
    }

    private void setupMainButtons() {
        mainButtons.subscribe(tb -> tb.setMinWidth(180));
    }

    private void resetErrorMessages() {
        errorMessageObservable.subscribe(errorText -> errorText.setText(""));
    }

    private void setupNotificationObjects() {
        errorMessageObservable.subscribe(errorText -> errorText.setStroke(Color.RED));
        notificationStockSymbol.setPromptText("Stock Symbol:");
        notificationPrice.setPromptText("Price:");
        notificationEmail.setPromptText("E-Mail:");
        belowPriceThreshold.setSelected(true);
        removeNotificationButton.setDisable(true);
        removeStockButton.setDisable(true);
        saveStockButton.setDisable(true);

        Observable.just(notificationStockSymbol, notificationPrice, notificationEmail, loadNotificationList,
                submitNotificationRequestButton, removeNotificationButton, removeStockButton).subscribe(obj -> {
            obj.setMinWidth(180);
        });

        Observable.just(abovePriceThreshold, belowPriceThreshold).subscribe(toggleButton -> {
            toggleButton.setToggleGroup(aboveOrBelow);
            toggleButton.setMinWidth(40);
        });

        buttonBarNotification.getButtons().addAll(belowPriceThreshold, abovePriceThreshold);
        buttonBarNotification.setButtonMinWidth(60);
        buttonBarNotification.setMaxWidth(130);

        allNotificationObjectsObservable.subscribe(obj -> {
            obj.setVisible(false);
        });
    }

    private void setupSearchBar() {
        searchStockErrorText.setStroke(Color.RED);
        searchStockTextField.setPromptText("Search stock:");
        searchStockTextField.setText("");
    }

    private void setupStage(Stage primaryStage) {
        VBox objects = new VBox(10, searchStockText, searchStockTextField, searchStockErrorText, emailText,
                notificationEmail, stockListButton, saveStockButton, removeStockButton, notificationSettingsButton, new Text(""),
                loadNotificationsRequestErrorText, loadNotificationList, removeNotificationButton,
                submitNotificationRequestErrorText, notificationStockSymbol, notificationPrice, buttonBarNotification,
                submitNotificationRequestButton, usageText);
        Single.just(objects).subscribe(b -> {
            b.setPadding(new Insets(5));
            b.setStyle("-fx-background-color: #999; -fx-focus-color: transparent; -fx-faint-focus-color: transparent");
            b.setPrefWidth(200);
        });

        stockObjectListView.setPrefSize(1200, 800);
        stockNotificationListView.setPrefSize(1200, 800);
        centerVbox.getChildren().add(stockObjectListView);
        // Set up scene/pane/stage

        BorderPane pane = new BorderPane();
        Single.just(pane).subscribe(p -> {
            p.setLeft(objects);
            BorderPane.setAlignment(stockObjectListView, Pos.TOP_LEFT);
            p.setCenter(centerVbox);
        });

        Scene scene = new Scene(pane, 1200, 800);

        Single.just(primaryStage).subscribe(p -> {
            p.setTitle("Stock Exchanger");
            p.setScene(scene);
            p.setResizable(false);
            p.show();
        });
    }

    private boolean emailWasSupplied() {
        if (notificationEmail.getText().equals("")) {
            searchStockErrorText.setText("You need to supply an email.");
            return false;
        }
        return true;
    }

    private void requiredFieldsForNotificationsWereSupplied() {
        validSubmission = true;
        requiredNotificationTextFields.subscribe(textField -> {
            if (textField.getText().equals("")) {
                validSubmission = false;
            }
        });
    }

    private Double parsePrice(String priceString) {
        try {
            Double priceDouble = Double.parseDouble(notificationPrice.getText());
            if (priceDouble <= 0) {
                validSubmission = false;
            }
            return priceDouble;

        } catch (Exception e) {
            validSubmission = false;
        }
        return -1.0;
    }

    private StockNotification.BelowOrAbove getSelectedAboveOrBelow() {
        if (abovePriceThreshold.isSelected()) {
            return StockNotification.BelowOrAbove.ABOVE;
        } else {
            return StockNotification.BelowOrAbove.BELOW;
        }
    }

    private void setupActionListeners() {
        notificationSettingsButton.setOnAction(action -> {
            resetErrorMessages();
            if (!notificationSettingsButton.isSelected()) {
                allNotificationObjectsObservable.subscribe(obj -> obj.setVisible(false));
            } else {
                allNotificationObjectsObservable.subscribe(obj -> obj.setVisible(true));
                updateStockNotificationInput(StockNotification.BelowOrAbove.BELOW);
            }
        });

        stockListButton.setOnAction(action -> {
            recentlyAddedAStock = false;
            resetErrorMessages();
            if (emailWasSupplied()) {
                client.subscribe(c -> c.sendTCP(new RequestStockList(notificationEmail.getText(), RequestStockList.StockType.STOCK_OBJECT)));
            }
        });

        loadNotificationList.setOnAction(action -> {
            recentlyAddedAStock = false;
            resetErrorMessages();
            if (emailWasSupplied()) {
                client.subscribe(c -> c.sendTCP(new RequestStockList(notificationEmail.getText(), RequestStockList.StockType.STOCK_NOTIFICATION)));
            }
        });

        saveStockButton.setOnAction(action -> {
            resetErrorMessages();
            if (emailWasSupplied()) {
                recentlyAddedAStock = true;
                client.subscribe(c -> {
                    c.sendTCP(new StockObjectPlusEmail(currentStock, notificationEmail.getText(), StockObjectPlusEmail.SaveOrRemove.SAVE));
                    c.sendTCP(new RequestStockList(notificationEmail.getText(), RequestStockList.StockType.STOCK_OBJECT));
                });
            }
        });

        removeStockButton.setOnAction(action -> {
            resetErrorMessages();
            if (emailWasSupplied()) {
                recentlyAddedAStock = false;
                client.subscribe(c -> {
                    c.sendTCP(new StockObjectPlusEmail(currentStock, notificationEmail.getText(), StockObjectPlusEmail.SaveOrRemove.REMOVE));
                    c.sendTCP(new RequestStockList(notificationEmail.getText(), RequestStockList.StockType.STOCK_OBJECT));
                });
            }
        });


        searchStockTextField.setOnAction(action -> {
            recentlyAddedAStock = false;
            resetErrorMessages();
            client.subscribe(c -> c.sendTCP(searchStockTextField.getText()));
        });

        submitNotificationRequestButton.setOnAction(action -> {
            resetErrorMessages();
            validSubmission = true;
            requiredFieldsForNotificationsWereSupplied();
            Double priceDouble = parsePrice(notificationPrice.getText());
            if (!validSubmission) {
                submitNotificationRequestErrorText.setText("Incomplete submission");
                return;
            }

            StockNotification.BelowOrAbove toggleButtonName = getSelectedAboveOrBelow();
            StockNotification stockNotification = new StockNotification(notificationEmail.getText(),
                    notificationStockSymbol.getText().toUpperCase(), toggleButtonName, priceDouble, StockNotification.SaveOrRemove.SAVE);

            client.subscribe(c -> {
                recentlyAddedAStock = true;
                c.sendTCP(stockNotification);
                c.sendTCP(new RequestStockList(notificationEmail.getText(), RequestStockList.StockType.STOCK_NOTIFICATION));
            });
        });

        aboveOrBelow.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            resetErrorMessages();
            if (newVal == null)
                oldVal.setSelected(true);
        });

        removeNotificationButton.setOnAction(action -> {
            resetErrorMessages();
            recentlyAddedAStock = false;
            client.subscribe(c -> {
                c.sendTCP(selectedStockNotification);
                c.sendTCP(new RequestStockList(notificationEmail.getText(), RequestStockList.StockType.STOCK_NOTIFICATION));
            });
        });

        stockObjectListView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<StockObject>() {

                    @Override
                    public void changed(ObservableValue<? extends StockObject> observable,
                                        StockObject oldValue, StockObject newValue) {
                        if (stockObjectListView.getItems().isEmpty() || newValue == null) {
                            setCurrentStockIsNull();
                            return;
                        }

                        if(centerVbox.getChildren().size() == 1) {
                            setSingleStockView(true);
                        }
                        else {
                            setSingleStockView(false);
                        }

                        currentStock = newValue;
                        double priceDouble = parsePrice(currentStock.getPrice());
                        selectedStockNotification = new StockNotification(notificationEmail.getText(), currentStock.getSymbol(), StockNotification.BelowOrAbove.BELOW, priceDouble, StockNotification.SaveOrRemove.SAVE);

                        if (!recentlyAddedAStock) {
                            updateStockNotificationInput(selectedStockNotification.getBelowOrAbove());
                        }
                        recentlyAddedAStock = false;


                    }
                });

        stockNotificationListView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<StockNotification>() {

                    @Override
                    public void changed(ObservableValue<? extends StockNotification> observable,
                                        StockNotification oldValue, StockNotification newValue) {

                        setSingleStockView(false);
                        if (!stockNotificationListView.getSelectionModel().isEmpty()) {
                            selectedStockNotification = newValue;
                            selectedStockNotification.setSaveOrRemove(StockNotification.SaveOrRemove.REMOVE);
                            currentStock = KryoServer.createStockObjectFromStockNotification(selectedStockNotification);
                            if (!recentlyAddedAStock) {
                                updateStockNotificationInput(selectedStockNotification.getBelowOrAbove());
                            }
                            recentlyAddedAStock = false;
                        }
                    }
                });

    }


    @Override
    public void start(Stage primaryStage) {

        setupClient();
        if (!connected) {
            setupAlertError();
            return;
        }
        setupTimeButtonBar();
        setupMainButtons();
        setupNotificationObjects();
        setupActionListeners();
        setupSearchBar();
        setupStage(primaryStage);
        resetErrorMessages();
    }

    private void setupAlertError() {
        Alert alert = new Alert(AlertType.ERROR, "Could not connect to server.", ButtonType.OK);
        alert.showAndWait();
    }

    private void setupClient() {
        KryoHelper.registerKryoClasses(kryo);

        client.subscribe(c -> {
            c.addListener(new Listener() {
                public void received(Connection connection, Object object) {

                    if (object instanceof ErrorMessage) {
                        ErrorMessage errorMsg = (ErrorMessage) object;
                        if (errorMsg.getWhichTextObject().equals("saveNotificationError")) {
                            submitNotificationRequestErrorText.setText(errorMsg.getMsg());
                        } else if (errorMsg.getWhichTextObject().equals("loadNotificationError")) {
                            loadNotificationsRequestErrorText.setText(errorMsg.getMsg());
                        } else if (errorMsg.getWhichTextObject().equals("searchStockError")) {
                            setCurrentStockIsNull();
                            searchStockErrorText.setText(errorMsg.getMsg());
                        }
                    }

                    if (object instanceof StockObject) {
                        StockObject stockObject = (StockObject) object;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                setSingleStockView(true);
                                if(!stockObjectListView.getItems().isEmpty()) {
                                    stockObjectListView.getItems().get(0).updateStockObject(stockObject);
                                }
                                //stockObjectListView.getItems().setAll(stockObject);
                                //centerVbox.getChildren().setAll(stockObjectListView);
                            }
                        });
                    }
                    if(object instanceof FirstStockObjectRequest) {
                        StockObject stockObject = ((FirstStockObjectRequest) object).getStockObject();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                setSingleStockView(true);
                                stockObjectListView.getItems().setAll(stockObject);
                                centerVbox.getChildren().setAll(stockObjectListView);
                                stockObjectListView.getSelectionModel().selectFirst();
                            }
                        });
                    }


                    if (object instanceof FirstStockObjectListRequest) {

                        setSingleStockView(false);
                        FirstStockObjectListRequest stockList = new FirstStockObjectListRequest(((FirstStockObjectListRequest) object).getArrayList());
                        if (!stockList.getArrayList().isEmpty()) {
                            ArrayList<StockObjectPlusEmail> newArrayList = stockList.getArrayList();
                            ArrayList<StockObject> stockObjectArrayList = new ArrayList<>();
                            Label label = new Label("\tPersonal list for " + notificationEmail.getText());
                            newArrayList.forEach(stock -> {
                                stockObjectArrayList.add(stock.getStockObject());
                            });

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    stockObjectListView.getItems().setAll((FXCollections.observableArrayList(stockObjectArrayList)));
                                    centerVbox.getChildren().setAll(label, stockObjectListView);
                                    currentStock = stockObjectListView.getItems().get(0);
                                    stockObjectListView.getSelectionModel().selectFirst();
                                }
                            });


                        } else {
                            setObjectListIsEmpty();
                        }
                    }

                    if (object instanceof ArrayList) {

                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                ArrayList<?> arrayList = (ArrayList<?>) object;
                                if (!arrayList.isEmpty()) {
                                    if (arrayList.get(0) instanceof StockNotification) {
                                        //arrayList = (ArrayList<StockNotification>) object;
                                        ArrayList<StockNotification> newArrayList = (ArrayList<StockNotification>) arrayList;
                                        stockNotificationListView.getItems().setAll((FXCollections.observableArrayList(newArrayList)));
                                        Label label = new Label("\tStock notification list for " + notificationEmail.getText());
                                        centerVbox.getChildren().setAll(label, stockNotificationListView);
                                        currentStock = KryoServer.createStockObjectFromStockNotification(newArrayList.get(0));
                                        stockNotificationListView.getSelectionModel().selectFirst();
                                        setSingleStockView(false);

                                    } else if (arrayList.get(0) instanceof StockObjectPlusEmail) {
                                        setSingleStockView(false);
                                        ArrayList<StockObjectPlusEmail> newArrayList = (ArrayList<StockObjectPlusEmail>) object;
                                        for (int it = 1; it < stockObjectListView.getItems().size(); it++) {
                                            for (int idx = 1; idx < newArrayList.size(); idx++) {
                                                if (stockObjectListView.getItems().get(it) == newArrayList.get(idx).getStockObject()) {
                                                    stockObjectListView.getItems().get(it).updateStockObject(newArrayList.get(idx).getStockObject());
                                                }
                                            }

                                        }
                                    }
                                } else {
                                    setObjectListIsEmpty();
                                }
                            }
                        });
                    }
                }
            });
        });

        client.subscribe(c ->

        {
            try {
                c.start();
                c.connect(5000, "localhost", 54555, 54777);
                connected = true;
            } catch (
                    IOException e) {
                connected = false;

            }
        });
    }

    private void setSingleStockView(boolean bool) {
        singleStockView = bool;
        if(singleStockView) {
            removeNotificationButton.setDisable(true);
            removeStockButton.setDisable(true);
            submitNotificationRequestButton.setDisable(false);
            saveStockButton.setDisable(false);
        }
        else {
            if(centerVbox.getChildren().contains(stockNotificationListView)) {
                if(stockNotificationListView.getItems().isEmpty()) {
                    removeNotificationButton.setDisable(true);
                    submitNotificationRequestButton.setDisable(false);
                    removeStockButton.setDisable(true);
                    saveStockButton.setDisable(true);
                    selectedStockNotification = null;
                    currentStock = null;
                }
                else {
                    removeNotificationButton.setDisable(false);
                    submitNotificationRequestButton.setDisable(false);
                    removeStockButton.setDisable(true);
                    saveStockButton.setDisable(false);
                }
            }
            else if(centerVbox.getChildren().contains(stockObjectListView)) {
                if(stockObjectListView.getItems().isEmpty()) {
                    removeNotificationButton.setDisable(true);
                    removeStockButton.setDisable(true);
                    submitNotificationRequestButton.setDisable(false);
                    saveStockButton.setDisable(false);
                    selectedStockNotification = null;
                    currentStock = null;
                }
                else {
                    removeNotificationButton.setDisable(true);
                    removeStockButton.setDisable(false);
                    submitNotificationRequestButton.setDisable(false);
                    saveStockButton.setDisable(false);
                }
            }
        }
    }

    private void setCurrentStockIsNull() {
        saveStockButton.setDisable(true);
        removeNotificationButton.setDisable(true);
        selectedStockNotification = null;
        currentStock = null;
        removeStockButton.setDisable(true);
    }

    private void setCurrentStockIsNotNull(StockObject stockObject) {
        currentStock = stockObject;

        saveStockButton.setDisable(false);
    }

    private void setObjectListIsEmpty() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                setCurrentStockIsNull();
                stockObjectListView.getItems().clear();
                centerVbox.getChildren().setAll(stockObjectListView);
            }
        });

    }


    private void updateStockNotificationInput(StockNotification.BelowOrAbove belowOrAbove) {
        if (currentStock == null) {
            notificationStockSymbol.setText("");
            notificationPrice.setText("");
        } else if (notificationSettingsButton.isSelected()) {
            notificationStockSymbol.setText(currentStock.getSymbol());
            notificationPrice.setText(currentStock.getPrice());
            if (belowOrAbove == StockNotification.BelowOrAbove.BELOW) {
                aboveOrBelow.selectToggle(belowPriceThreshold);
            } else {
                aboveOrBelow.selectToggle(abovePriceThreshold);
            }
        }
    }

}