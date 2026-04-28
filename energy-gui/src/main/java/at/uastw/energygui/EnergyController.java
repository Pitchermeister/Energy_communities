package at.uastw.energygui;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EnergyController {

    @FXML private ListView<String> lv_current;
    @FXML private ListView<String> lv_historical;
    @FXML private DatePicker dp_start;
    @FXML private DatePicker dp_end;

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
        String start = dp_start.getValue().toString();
        String end = dp_end.getValue().toString();

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/energy/historical?start=" + start + "T00:00:00&end=" + end + "T23:59:59"))
                .GET().build();

        String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        lv_historical.getItems().clear();
        lv_historical.getItems().add(body);
    }
}