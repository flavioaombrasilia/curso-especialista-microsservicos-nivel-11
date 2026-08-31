package com.algaworks.hello;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @Value("${app.version:v1}")
    private String version;

    // porta efetiva em que o servidor subiu: vem do ConfigMap (APP_PORT -> SERVER_PORT)
    @Value("${server.port}")
    private String porta;

    @GetMapping({"/", "/hello", "/rastreio"})
    public Map<String, String> hello() throws UnknownHostException {
        String pod = InetAddress.getLocalHost().getHostName();
        // so aparece quando LOG_LEVEL abre o logger da aplicacao para DEBUG
        log.debug("requisicao respondida pelo pod {} na porta {}", pod, porta);
        log.info("GET / respondido");
        return Map.of(
            "servico", "algadelivery-hello",
            "versao",  version,
            "porta",   porta,
            "pod",     pod
        );
    }
}
