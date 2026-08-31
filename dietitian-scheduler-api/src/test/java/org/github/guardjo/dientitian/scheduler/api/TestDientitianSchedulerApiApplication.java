package org.github.guardjo.dientitian.scheduler.api;

import org.springframework.boot.SpringApplication;

public class TestDientitianSchedulerApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(DientitianSchedulerApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
