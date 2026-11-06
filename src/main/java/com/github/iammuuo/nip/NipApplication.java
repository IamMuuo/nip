package com.github.iammuuo.nip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class NipApplication {

  public static void main(String[] args) {
    SpringApplication.run(NipApplication.class, args);
  }

}
