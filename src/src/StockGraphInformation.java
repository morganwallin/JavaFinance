package src;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;






public class StockGraphInformation extends Application {
    StockGraphInformation() {

    }
    StockGraphInformation(Stock stock) {
        try {
            System.out.println(stock.getHistory());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Override public void start(Stage stage) {
        final LineChart lineChart = new LineChart(
                new NumberAxis(), new NumberAxis(),
                FXCollections.observableArrayList(
                        new XYChart.Series(
                                "My portfolio",
                                FXCollections.observableArrayList(
                                        plot(0, 14, 15, 24, 34, 36, 22, 55, 43, 17, 29, 25)
                                )
                        )
                )
        );
        lineChart.setCursor(Cursor.CROSSHAIR);

        lineChart.setTitle("Stock Monitoring, 2013");

        stage.setScene(new Scene(lineChart, 500, 400));
        stage.show();

        System.out.println("test 1 = "+lineChart.getProperties());
    }

    /** @return plotted y values for monotonically increasing integer x values, starting from x=1 */
    public ObservableList<XYChart.Data<Integer, Integer>> plot(Integer... y) {
        final ObservableList<XYChart.Data<Integer, Integer>> dataset = FXCollections.observableArrayList();
        int i = 0;
        List<Integer> list = Arrays.asList(y);
        int min = Collections.min(list);
        int max = Collections.max(list);
        int minThreshold = 5;
        int maxThreshold = 5;

        while (i < y.length) {
            final XYChart.Data<Integer, Integer> data = new XYChart.Data<>(i + 1, y[i]);
            int topMargin = 0;
            if(y[i] <= min + minThreshold) {
                topMargin = -50;
            } else if (y[i] >= max - maxThreshold) {
                topMargin = 50;
            }
            StackPane stackPane = new HoveredThresholdNode(
                    (i == 0) ? 0 : y[i-1],
                    y[i],
                    topMargin
            );

            data.setNode(stackPane);

            dataset.add(data);
            i++;
        }

        return dataset;
    }

    /** a node which displays a value on hover, but is otherwise empty */
    class HoveredThresholdNode extends StackPane {
        HoveredThresholdNode(int priorValue, int value, int topMargin) {
            setPrefSize(15, 15);

            final Label label = createDataThresholdLabel(priorValue, value);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().setAll(label);
                    setCursor(Cursor.NONE);
                    toFront();
                    setMargin(label, new Insets(topMargin,0,0,0));
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().clear();
                    setCursor(Cursor.CROSSHAIR);
                }
            });

        }

        private Label createDataThresholdLabel(int priorValue, int value) {
            final Label label = new Label(value + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

            if (priorValue == 0) {
                label.setTextFill(Color.DARKGRAY);
            } else if (value > priorValue) {
                label.setTextFill(Color.FORESTGREEN);
            } else {
                label.setTextFill(Color.FIREBRICK);
            }

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }

    public static void main(String[] args) { launch(args); }
}