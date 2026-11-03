package fr.insa.crusche;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.aura.Aura;

@SpringBootApplication
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css") // Your custom styles
@Push
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder application) {

        return application.sources(Application.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
