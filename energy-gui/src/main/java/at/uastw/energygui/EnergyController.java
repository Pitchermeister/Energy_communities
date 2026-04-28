package at.uastw.energygui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class EnergyController {

    @FXML private ListView<String> lv_current;
    @FXML private ListView<String> lv_historical;
    @FXML private DatePicker dp_start;
    @FXML private DatePicker dp_end;
    @FXML private ComboBox<String> cb_start_hour;
    @FXML private ComboBox<String> cb_end_hour;

    @FXML
    public void initialize() {
        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d:00", i));
        }
        cb_start_hour.setItems(FXCollections.observableArrayList(hours));
        cb_end_hour.setItems(FXCollections.observableArrayList(hours));
        cb_start_hour.setValue("00:00");
        cb_end_hour.setValue("23:00");
    }

    @FXML
    protected void onRefreshClicked() throws Exception {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/energy/current"))
                .GET().build();

        String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        lv_current.getItems().clear();
        lv_current.getItems().add(body);
    }

    @FXML
    protected void onLoadHistoricalClicked() throws Exception {
        String start = dp_start.getValue().toString() + "T" + cb_start_hour.getValue() + ":00";
        String end = dp_end.getValue().toString() + "T" + cb_end_hour.getValue() + ":00";

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/energy/historical?start=" + start + "&end=" + end))
                .GET().build();

        String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        lv_historical.getItems().clear();
        lv_historical.getItems().add(body);
    }
}