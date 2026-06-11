package at.uastw.energygui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

public class EnergyController {

    // Current Energy Data
    @FXML private Label lblProduced;
    @FXML private Label lblCommunityUsed;
    @FXML private Label lblGridUsed;
    @FXML private Label lblDepletion;
    @FXML private Label lblGridPortion;
    @FXML private Label lblCurrentStatus;

    // Historical Data Aggregation
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private ComboBox<Integer> cbStartHour;
    @FXML private ComboBox<Integer> cbEndHour;
    @FXML private Label lblSumProduced;
    @FXML private Label lblSumCommunityUsed;
    @FXML private Label lblSumGridUsed;
    @FXML private Label lblHistStatus;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        // Fill the hour drop-downs with 0..23
        for (int i = 0; i < 24; i++) {
            cbStartHour.getItems().add(i);
            cbEndHour.getItems().add(i);
        }
        cbStartHour.setValue(0);
        cbEndHour.setValue(23);

        // Default dates so the values are never null
        dpStart.setValue(LocalDate.now().minusDays(1));
        dpEnd.setValue(LocalDate.now());
    }

    @FXML
    protected void onRefreshClicked() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8091/energy/current"))
                    .GET().build();
            String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            JsonNode node = mapper.readTree(body);

            lblProduced.setText(String.format("%.2f kWh", node.get("communityProduced").asDouble()));
            lblCommunityUsed.setText(String.format("%.2f kWh", node.get("communityUsed").asDouble()));
            lblGridUsed.setText(String.format("%.2f kWh", node.get("gridUsed").asDouble()));

            lblDepletion.setText(String.format("Community Pool Depletion: %.2f %%", node.get("communityDepleted").asDouble()));
            lblGridPortion.setText(String.format("Grid Portion: %.2f %%", node.get("gridPortion").asDouble()));

            lblCurrentStatus.setText("");
        } catch (Exception e) {
            lblCurrentStatus.setText("Error fetching current data. Is the backend running?");
        }
    }

    @FXML
    protected void onLoadHistoricalClicked() {
        try {
            String start = dpStart.getValue() + String.format("T%02d:00:00", cbStartHour.getValue());
            String end = dpEnd.getValue() + String.format("T%02d:00:00", cbEndHour.getValue());

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8091/energy/historical?start=" + start + "&end=" + end))
                    .GET().build();
            String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            JsonNode root = mapper.readTree(body);

            double sumProduced = 0;
            double sumCommunityUsed = 0;
            double sumGridUsed = 0;

            for (JsonNode node : root) {
                sumProduced += node.get("communityProduced").asDouble();
                sumCommunityUsed += node.get("communityUsed").asDouble();
                sumGridUsed += node.get("gridUsed").asDouble();
            }

            lblSumProduced.setText(String.format("%.2f kWh", sumProduced));
            lblSumCommunityUsed.setText(String.format("%.2f kWh", sumCommunityUsed));
            lblSumGridUsed.setText(String.format("%.2f kWh", sumGridUsed));

            if (root.size() == 0) {
                lblHistStatus.setText("No historical data found for this time range.");
            } else {
                lblHistStatus.setText("");
            }
        } catch (Exception e) {
            lblHistStatus.setText("Error fetching historical data. Is the backend running?");
        }
    }
}