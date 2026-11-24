package com.sillymedia.securepipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. {@code @SpringBootApplication} turns on component
 * scanning, auto-configuration and the embedded Tomcat server, so
 * {@code mvn spring-boot:run} (or {@code java -jar ...}) just works.
 */
@SpringBootApplication
public class SecurePipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurePipelineApplication.class, args);
    }
}
