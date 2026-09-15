package com.algaworks.hello;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cache em disco: cada chave vira um arquivo dentro de app.cache-dir.
 * O diretorio vem de fora (APP_CACHE_DIR) para que o manifesto decida
 * se ele cai na camada gravavel do container ou dentro de um volume.
 */
@RestController
@RequestMapping("/cache")
public class CacheController {

    private final Path diretorio;

    public CacheController(@Value("${app.cache-dir}") String cacheDir) throws IOException {
        this.diretorio = Files.createDirectories(Path.of(cacheDir));
    }

    @GetMapping
    public Map<String, Object> listar() throws IOException {
        try (Stream<Path> arquivos = Files.list(diretorio)) {
            return Map.of(
                "pod", hostname(),
                "diretorio", diretorio.toString(),
                "chaves", arquivos.map(arquivo -> arquivo.getFileName().toString()).sorted().toList()
            );
        }
    }

    @PutMapping("/{chave}")
    public Map<String, String> gravar(@PathVariable String chave, @RequestBody String valor) throws IOException {
        Files.writeString(arquivoDe(chave), valor);
        return Map.of(
            "gravou", chave,
            "em", diretorio.toString(),
            "pod", hostname()
        );
    }

    @GetMapping("/{chave}")
    public ResponseEntity<String> ler(@PathVariable String chave) throws IOException {
        Path arquivo = arquivoDe(chave);
        return Files.exists(arquivo)
            ? ResponseEntity.ok(Files.readString(arquivo))
            : ResponseEntity.notFound().build();
    }

    // so o ultimo segmento vira nome de arquivo: nenhuma chave escapa do diretorio
    private Path arquivoDe(String chave) {
        return diretorio.resolve(Path.of(chave).getFileName());
    }

    private String hostname() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }
}
