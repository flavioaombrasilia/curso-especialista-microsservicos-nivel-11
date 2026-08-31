package com.algaworks.hello;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class SaudeController {

    private final ApplicationEventPublisher publicador;
    private final EstadoTravamento estadoTravamento;

    public SaudeController(ApplicationEventPublisher publicador, EstadoTravamento estadoTravamento) {
        this.publicador = publicador;
        this.estadoTravamento = estadoTravamento;
    }

    // 1. tira/devolve o Pod ao rodizio do Service — a aplicacao continua viva
    @PostMapping("/readiness/{estado}")
    public String readiness(@PathVariable String estado) {
        ReadinessState novo = "pronto".equals(estado)
                ? ReadinessState.ACCEPTING_TRAFFIC
                : ReadinessState.REFUSING_TRAFFIC;
        AvailabilityChangeEvent.publish(publicador, this, novo);
        return "readiness = " + novo;
    }

    // 2. trava DE VERDADE: todo endpoint passa a responder 503 (ver TravamentoFilter),
    // processo continua de pe — so quem devolve isso e o restart do kubelet
    @PostMapping("/liveness/quebrar")
    public String quebrar() {
        estadoTravamento.quebrar();
        AvailabilityChangeEvent.publish(publicador, this, LivenessState.BROKEN);
        return "liveness = BROKEN (app travada)";
    }

    // so para teste local, fora do cluster — no cluster ninguem chama isto,
    // quem conserta e o kubelet matando e recriando o container
    @PostMapping("/liveness/consertar")
    public String consertar() {
        estadoTravamento.consertar();
        AvailabilityChangeEvent.publish(publicador, this, LivenessState.CORRECT);
        return "liveness = CORRECT";
    }
}
