package at.uastw.percentageservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CurrentPercentageApplication {
    public static void main(String[] args) {
        SpringApplication.run(CurrentPercentageApplication.class, args);
    }
}
