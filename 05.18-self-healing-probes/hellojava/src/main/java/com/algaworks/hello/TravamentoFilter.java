package com.algaworks.hello;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// simula travamento real (nao so o status do Actuator): enquanto EstadoTravamento
// estiver quebrado, TODO endpoint responde 503 -- inclusive /actuator/health/liveness
// e /actuator/health/readiness, que e o que a probe do Kubernetes le. Excecao:
// /admin/** continua vivo, e o canal que permite reverter fora do cluster sem
// reiniciar a JVM (no cluster, quem conserta e o restart do kubelet).
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TravamentoFilter extends OncePerRequestFilter {

    private final EstadoTravamento estado;

    TravamentoFilter(EstadoTravamento estado) {
        this.estado = estado;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (estado.estaQuebrado() && !request.getRequestURI().startsWith("/admin")) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "aplicacao travada (simulacao)");
            return;
        }
        chain.doFilter(request, response);
    }
}
