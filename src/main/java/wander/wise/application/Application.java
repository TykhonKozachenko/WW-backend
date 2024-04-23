package wander.wise.application;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import wander.wise.application.config.ApisConfigProperties;
import wander.wise.application.mapper.CommentMapper;
import wander.wise.application.model.Collection;
import wander.wise.application.repository.collection.CollectionRepository;

@SpringBootApplication
@EnableConfigurationProperties(ApisConfigProperties.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
