package at.uastw.energygui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EnergyController {

    @FXML private ListView<String> lv_current;
    @FXML private ListView<String> lv_historical;
    @FXML private DatePicker dp_start;
    @FXML private DatePicker dp_end;
    @FXML private ComboBox<String> cb_start_hour;
    @FXML private ComboBox<String> cb_end_hour;

    // The tool to read the raw JSON
    private final ObjectMapper mapper = new ObjectMapper();

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

        // UI TRICK: Set default dates so they are never null!
        // This completely prevents NullPointerExceptions.
        dp_start.setValue(LocalDate.now().minusDays(1)); // Default to Yesterday
        dp_end.setValue(LocalDate.now()); // Default to Today
    }

    @FXML
    protected void onRefreshClicked() {
        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8091/energy/current"))
                    .GET().build();

            String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            lv_current.getItems().clear();

            // Read the JSON and format it nicely
            JsonNode root = mapper.readTree(body);
            lv_current.getItems().add(formatJsonNode(root));

        } catch (Exception e) {
            lv_current.getItems().clear();
            lv_current.getItems().add("Error fetching data. Is the backend running?");
        }
    }

    @FXML
    protected void onLoadHistoricalClicked() {
        try {
            // Because we set defaults in initialize(), dp_start and dp_end will never be empty here!
            String start = dp_start.getValue().toString() + "T" + cb_start_hour.getValue() + ":00";
            String end = dp_end.getValue().toString() + "T" + cb_end_hour.getValue() + ":00";

            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8091/energy/historical?start=" + start + "&end=" + end))
                    .GET().build();

            String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            lv_historical.getItems().clear();

            JsonNode root = mapper.readTree(body);

            // If it's a list of history, loop through and add each one
            if (root.isArray()) {
                for (JsonNode node : root) {
                    lv_historical.getItems().add(formatJsonNode(node));
                }
            } else {
                lv_historical.getItems().add(formatJsonNode(root));
            }

            // Tell the user if the database returned an empty list
            if (lv_historical.getItems().isEmpty()) {
                lv_historical.getItems().add("No historical data found for this time range.");
            }

        } catch (Exception e) {
            lv_historical.getItems().clear();
            lv_historical.getItems().add("Error fetching data. Is the backend running?");
        }
    }

    // HELPER: Advanced 3-Line JSON Formatter (Lambda-Free)
    private String formatJsonNode(JsonNode node) {
        String dateStr = "";
        String timeStr = "";
        StringBuilder energyLines = new StringBuilder();
        StringBuilder percentLines = new StringBuilder();

        // FIX: Replaced the Lambda with a standard Iterator loop!
        java.util.Iterator<java.util.Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            java.util.Map.Entry<String, JsonNode> entry = fields.next();
            String rawKey = entry.getKey();
            JsonNode valNode = entry.getValue();

            // 1. Regex trick: Split CamelCase (e.g. "gridPortion" -> "grid Portion") and capitalize
            String formattedKey = rawKey.replaceAll("([a-z])([A-Z]+)", "$1 $2");
            formattedKey = formattedKey.substring(0, 1).toUpperCase() + formattedKey.substring(1);

            // 2. Extract and split Date/Time
            if (rawKey.toLowerCase().contains("date") || rawKey.toLowerCase().contains("time") || rawKey.toLowerCase().contains("hour")) {
                String rawVal = valNode.asText();
                if (rawVal.contains("T")) {
                    String[] parts = rawVal.split("T");
                    dateStr = parts[0];
                    timeStr = parts[1];
                } else {
                    dateStr = rawVal;
                }
            }
            // 3. Round Doubles and assign units (kWh vs %)
            else if (valNode.isNumber()) {
                double val = valNode.asDouble();
                // Format to exactly 2 decimal places
                String formattedVal = String.format(java.util.Locale.US, "%.2f", val);

                if (rawKey.toLowerCase().contains("depl") || rawKey.toLowerCase().contains("port") || rawKey.toLowerCase().contains("percent")) {
                    percentLines.append(formattedKey).append(": ").append(formattedVal).append(" %  |  ");
                } else {
                    // Default to kWh for produced/used
                    energyLines.append(formattedKey).append(": ").append(formattedVal).append(" kWh  |  ");
                }
            } else {
                // Catch-all for basic strings
                energyLines.append(formattedKey).append(": ").append(valNode.asText()).append("  |  ");
            }
        }

        // 4. Assemble the final 3-line String
        StringBuilder finalString = new StringBuilder();
        if (!dateStr.isEmpty()) {
            finalString.append("Date: ").append(dateStr);
            if (!timeStr.isEmpty()) {
                finalString.append("  |  Time: ").append(timeStr);
            }
            finalString.append("\n");
        }

        // Add energy stats and remove the trailing "  |  "
        if (energyLines.length() > 0) {
            finalString.append(energyLines.substring(0, Math.max(0, energyLines.length() - 5))).append("\n");
        }

        // Add percentages and remove the trailing "  |  "
        if (percentLines.length() > 0) {
            finalString.append(percentLines.substring(0, Math.max(0, percentLines.length() - 5)));
        }

        return finalString.toString().trim();
    }
}