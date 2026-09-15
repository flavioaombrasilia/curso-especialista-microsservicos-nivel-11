package com.algaworks.hello;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// boot artificialmente lento, para a demo da startupProbe — controlado por
// APP_STARTUP_DELAY (relaxed binding, callback a 5.05), sem alterar a imagem
@Component
class BootLento {

    BootLento(@Value("${app.startup-delay:0}") long segundos) throws InterruptedException {
        if (segundos > 0) {
            Thread.sleep(Duration.ofSeconds(segundos).toMillis());
        }
    }
}
