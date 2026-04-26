package at.uastw.energyapi.controller;

import at.uastw.energyapi.dto.EnergyDto;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private List<EnergyDto> historicalData = new ArrayList<>(List.of(
        new EnergyDto("2025-01-10T14:00:00", 18.05,  18.05,  1.076, 100.00, 5.63),
        new EnergyDto("2025-01-10T13:00:00", 15.015, 14.033, 2.049, 93.45,  12.12),
        new EnergyDto("2025-01-10T12:00:00", 12.300, 11.800, 3.200, 95.93,  21.33),
        new EnergyDto("2025-01-10T11:00:00", 10.500, 10.500, 4.100, 100.00, 28.09),
        new EnergyDto("2025-01-10T10:00:00", 8.200,  7.900,  5.300, 96.34,  40.15)
    ));

    @GetMapping("/current")
    public EnergyDto getCurrent() {
        return historicalData.get(0);
    }

    @GetMapping("/historical")
    public List<EnergyDto> getHistorical(@RequestParam String start, @RequestParam String end) {
        return historicalData;
    }
}
