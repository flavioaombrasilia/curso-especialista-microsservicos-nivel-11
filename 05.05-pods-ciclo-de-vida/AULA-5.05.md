# Aula 5.05 — Pods: ciclo de vida e criação

Passo a passo para executar ao vivo.
Pré-requisito: cluster `algadelivery` do Kind no ar (aula 5.04).

> ⚠️ Só existe `gradlew.bat` no projeto. No macOS/Linux, use o `gradle` instalado
> (`gradle clean bootJar`) ou gere o wrapper uma vez com `gradle wrapper`.

---

## 1. Conferir o cluster

```bash
kubectl config current-context      # kind-algadelivery
kubectl get nodes
```

---

## 2. Um Pod simples, sem app própria (aquecimento)

`nginx.pod.yaml`:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx2
  labels:
    app: nginx
spec:
  containers:
  - name: nginx2
    image: nginx:alpine
```

```bash
kubectl apply -f nginx.pod.yaml
kubectl get pods -w                 # acompanhar Pending → Running
kubectl delete -f nginx.pod.yaml
```

> A imagem `nginx:alpine` vem do Docker Hub — por isso funciona sem `kind load`.
> Com a **nossa** imagem, no passo 4, a história muda.

---

## 3. Imperativo × declarativo

```bash
# imperativo: rápido, descartável, não versionável
kubectl run nginx-imperativo --image=nginx:alpine
kubectl get pods
kubectl delete pod nginx-imperativo
```

> O jeito de verdade é o declarativo: manifesto YAML no Git + `kubectl apply -f`.
> É o que a gente usa do passo 4 em diante e no curso inteiro.

---

## 4. Construir a imagem do app do módulo

A app é Spring Boot e responde **quem** atendeu e **qual versão** — é isso que vai
provar failover na 5.08 e rollout na 5.09.

```java
// hellojava/src/main/java/com/algaworks/hello/HelloController.java
@GetMapping("/")
public Map<String, String> hello() throws UnknownHostException {
    return Map.of(
        "servico", "algadelivery-hello",
        "versao",  version,                              // vem de APP_VERSION
        "pod",     InetAddress.getLocalHost().getHostName()  // hostname == nome do Pod
    );
}
```

```dockerfile
# hellojava/Dockerfile
FROM eclipse-temurin:21-jre
ARG APP_VERSION=v1
ENV APP_VERSION=${APP_VERSION}
COPY build/libs/hellojava-0.0.1-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build:

```bash
cd hellojava
gradle clean bootJar                 # jar em build/libs/
docker build --build-arg APP_VERSION=v1 -t hellojava:v1 .
docker images | grep hellojava
```

> 💡 A versão é **assada na imagem**, não no manifesto. Pelo relaxed binding do Spring,
> `APP_VERSION` vira a propriedade `app.version`. Para mudar a versão, muda-se a
> **imagem** — que é exatamente o que um rollout faz (5.09).

---

## 5. ERRO PROPOSITAL — aplicar sem o `kind load`

```bash
kubectl apply -f hellojava/hellojava.pod.yaml
kubectl get pods                     # ErrImagePull → ImagePullBackOff
kubectl describe pod hello2 | tail -20
# Events: Failed to pull image "hellojava:v1" ...
```

**Por quê:** o cluster **não enxerga** o seu Docker. Não achando localmente, o kubelet
foi procurar no Docker Hub. É o erro nº 1 de quem usa Kind.

Corrigir:

```bash
kind load docker-image hellojava:v1 --name algadelivery
kubectl delete -f hellojava/hellojava.pod.yaml
kubectl apply -f hellojava/hellojava.pod.yaml
kubectl get pods                     # Running
```

> Citar sem demonstrar: com a tag `:latest` o `imagePullPolicy` default vira `Always`
> e o Pod tentaria puxar do registry mesmo com a imagem carregada no node. Por isso o
> curso **sempre** usa tag explícita (`v1`, `v2`).

---

## 6. O manifesto do Pod, campo a campo

```yaml
apiVersion: v1        # Pod é do core group
kind: Pod
metadata:
  name: hello2
  labels:             # gancho para a 5.07
    app: hello2
spec:
  containers:
  - name: hello2
    image: hellojava:v1
    ports:
      - containerPort: 8080   # DOCUMENTAÇÃO — não abre nada
```

Provar que `containerPort` é documentação: comentar a linha, `kubectl apply` de novo,
e o `port-forward` do passo 8 continua funcionando.

---

## 7. Inspecionar e depurar

```bash
kubectl get pods -o wide             # IP e node
kubectl get pod hello2 -o yaml | head -30

kubectl describe pod hello2          # PRIMEIRA parada ao depurar
# Events: Scheduled → Pulled → Created → Started  (o fluxo da 5.03, em texto)

kubectl logs hello2                  # banner do Spring Boot
kubectl logs -f hello2               # follow
kubectl logs --previous hello2       # container anterior, se reiniciou
```

---

## 8. Acessar a aplicação (só port-forward nesta aula)

```bash
kubectl port-forward pod/hello2 8080:8080

# noutro terminal:
curl -s localhost:8080
# {"servico":"algadelivery-hello","versao":"v1","pod":"hello2"}
```

> ⚠️ `port-forward` é **túnel de debug**, não é como se expõe aplicação.
> A forma certa — `Service` — é o Módulo 6.

```bash
kubectl exec -it hello2 -- sh
# dentro do container:
hostname                 # hello2 — o hostname É o nome do Pod
env | grep APP_VERSION
exit
```

---

## 9. Ciclo de vida: fases e estados

- **Fases do Pod** (`status.phase`): `Pending` → `Running` → `Succeeded` / `Failed`
  (`Unknown` quando o node some).
- **Estado do container**: `Waiting` / `Running` / `Terminated` — não confundir com a fase.
- **`restartPolicy`**: `Always` (default) · `OnFailure` · `Never`.

```bash
kubectl get pod hello2 -o jsonpath='{.status.phase}{"\n"}'
kubectl get pod hello2 -o jsonpath='{.status.containerStatuses[0].state}{"\n"}'
```

---

## 10. Reiniciar container ≠ recriar Pod (o "aha")

```bash
# terminal 1
kubectl get pod hello2 -w

# terminal 2
kubectl exec hello2 -- kill 1
```

→ `RESTARTS 1`. **Mesmo Pod, mesmo nome, mesmo node, mesmo IP.** Quem reiniciou foi o
**kubelet**, dentro do Pod. Isso **não** é failover.

Agora o contraste:

```bash
kubectl delete pod hello2
kubectl get pods            # sumiu — ninguém recria
```

Pod é **mortal e efêmero**: não se cura sozinho, o IP muda a cada recriação, não
sobrevive à queda do node. Ótimo para aprender e depurar, **errado para produção**.

> **Loop aberto → 5.08:** quem garante que sempre existam *N* réplicas vivas,
> recriando o que morre? Um **controller**. O primeiro é o **ReplicaSet**.

---

## Limpeza

```bash
kubectl delete pod --all
```
