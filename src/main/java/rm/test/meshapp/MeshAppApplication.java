package rm.test.meshapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeshAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeshAppApplication.class, args);
    }

}
