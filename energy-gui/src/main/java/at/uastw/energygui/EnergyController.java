package at.uastw.energygui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // Current Energy Data Outlets
    @FXML private Label lblProduced;
    @FXML private Label lblCommunityUsed;
    @FXML private Label lblGridUsed;
    @FXML private Label lblDepletion;
    @FXML private Label lblGridPortion;
    @FXML private Label lblCurrentStatus;

    // Historical Data Aggregation Outlets
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private ComboBox<String> cbStartHour;
    @FXML private ComboBox<String> cbEndHour;
    @FXML private Label lblSumProduced;
    @FXML private Label lblSumCommunityUsed;
    @FXML private Label lblSumGridUsed;
    @FXML private Label lblHistStatus;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        // Populate the hour drop-downs with digital clock formatting (e.g., 00:00, 01:00)
        for (int i = 0; i < 24; i++) {
            String hourStr = String.format("%02d:00", i);
            cbStartHour.getItems().add(hourStr);
            cbEndHour.getItems().add(hourStr);
        }
        cbStartHour.setValue("00:00");
        cbEndHour.setValue("23:00");

        // Set default date range to avoid NullPointerExceptions
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

            // Update live metrics on the UI cards
            lblProduced.setText(String.format("%.2f kWh", node.get("communityProduced").asDouble()));
            lblCommunityUsed.setText(String.format("%.2f kWh", node.get("communityUsed").asDouble()));
            lblGridUsed.setText(String.format("%.2f kWh", node.get("gridUsed").asDouble()));

            // Raw values are sufficient as the layout card headers explicitly define the metrics
            lblDepletion.setText(String.format("%.2f %%", node.get("communityDepleted").asDouble()));
            lblGridPortion.setText(String.format("%.2f %%", node.get("gridPortion").asDouble()));

            lblCurrentStatus.setText("");
        } catch (Exception e) {
            lblCurrentStatus.setText("Error fetching current data. Is the backend running?");
        }
    }

    @FXML
    protected void onLoadHistoricalClicked() {
        try {
            // Strip the ":00" suffix to extract the raw integer value for URL construction
            int startHour = Integer.parseInt(cbStartHour.getValue().replace(":00", ""));
            int endHour = Integer.parseInt(cbEndHour.getValue().replace(":00", ""));

            String start = dpStart.getValue() + String.format("T%02d:00:00", startHour);
            String end = dpEnd.getValue() + String.format("T%02d:00:00", endHour);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8091/energy/historical?start=" + start + "&end=" + end))
                    .GET().build();
            String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            JsonNode root = mapper.readTree(body);

            double sumProduced = 0;
            double sumCommunityUsed = 0;
            double sumGridUsed = 0;

            // Iterate and aggregate historical dataset metrics
            for (JsonNode node : root) {
                sumProduced += node.get("communityProduced").asDouble();
                sumCommunityUsed += node.get("communityUsed").asDouble();
                sumGridUsed += node.get("gridUsed").asDouble();
            }

            lblSumProduced.setText(String.format("%.2f kWh", sumProduced));
            lblSumCommunityUsed.setText(String.format("%.2f kWh", sumCommunityUsed));
            lblSumGridUsed.setText(String.format("%.2f kWh", sumGridUsed));

            if (root.isEmpty()) {
                lblHistStatus.setText("No historical data found for this time range.");
            } else {
                lblHistStatus.setText("");
            }
        } catch (Exception e) {
            lblHistStatus.setText("Error fetching historical data. Is the backend running?");
        }
    }
}