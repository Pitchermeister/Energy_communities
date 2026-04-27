package at.uastw.energygui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EnergyController {

    @FXML
    private Label lb_current;

    @FXML
    private TextField tf_start;

    @FXML
    private TextField tf_end;

    @FXML
    private TextArea ta_historical;

    @FXML
    protected void onRefreshClicked() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8091/energy/current"))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            lb_current.setText(formatCurrent(response.body()));

        } catch (Exception e) {
            lb_current.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    protected void onLoadHistoricalClicked() {
        try {
            String start = tf_start.getText();
            String end = tf_end.getText();

            String url = "http://localhost:8091/energy/historical?start=" + start + "&end=" + end;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ta_historical.setText(formatHistorical(response.body()));

        } catch (Exception e) {
            ta_historical.setText("Error: " + e.getMessage());
        }
    }

    private String formatCurrent(String json) {
        // turns the JSON into readable lines
        return json
                .replace("{", "")
                .replace("}", "")
                .replace("\"", "")
                .replace(",", "\n");
    }

    private String formatHistorical(String json) {
        // one block per entry, separated by a line
        String[] entries = json.split("\\},\\{");
        StringBuilder sb = new StringBuilder();
        for (String entry : entries) {
            String clean = entry
                    .replace("[", "")
                    .replace("]", "")
                    .replace("{", "")
                    .replace("}", "")
                    .replace("\"", "")
                    .replace(",", "\n");
            sb.append(clean).append("\n\n");
        }
        return sb.toString().trim();
    }
}
