package com.algaworks.hello;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

// estado compartilhado entre o SaudeController (quem quebra/conserta) e o
// TravamentoFilter (quem aplica a quebra em toda requisicao HTTP)
@Component
class EstadoTravamento {

    private final AtomicBoolean quebrado = new AtomicBoolean(false);

    void quebrar() {
        quebrado.set(true);
    }

    void consertar() {
        quebrado.set(false);
    }

    boolean estaQuebrado() {
        return quebrado.get();
    }
}
