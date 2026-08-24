# Aula 5.06 — Pods multi-container: rede e volumes compartilhados

Passo a passo para executar ao vivo.
Pré-requisito: cluster `algadelivery` no ar e imagem `hellojava:v1` já carregada (aula 5.05).

---

## 1. Recap rápido — o Pod de um container só

```bash
kubectl apply -f hellojava/hellojava.pod.yaml
kubectl get pods            # READY 1/1
```

> Na 5.05 a gente disse que o Pod **embrulha um ou mais containers** que compartilham
> rede e volumes — mas só usou um. Hoje a gente paga essa dívida.

---

## 2. O manifesto multi-container

`hellojava/hellojava-multicontainer.pod.yaml` — três containers, dois papéis:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: hello-multicontainer
  labels:
    app: hello-multicontainer
spec:
  initContainers:
  - name: wait-for-dns
    image: busybox:1.37
    command: ["sh", "-c"]
    args:
      - |
        echo "[init] configurando a aplicação..."
        sleep 5
        echo "[init] aplicação configurada!"
  containers:
  - name: hello
    image: hellojava:v1
    ports:
      - containerPort: 8080
  # Sidecar proxy: recebe o trafego externo na porta 80 e repassa para a
  # aplicacao em localhost:8080. A app nunca precisa ser exposta diretamente.
  - name: proxy
    image: nginx:alpine
    ports:
      - containerPort: 80
    command: ["sh", "-c"]
    args:
      - |
        cat > /etc/nginx/conf.d/default.conf <<'EOF'
        server {
          listen 80;
          listen [::]:80;
          location / {
            proxy_pass http://localhost:8080;
            add_header X-Servido-Por "sidecar-proxy";
          }
        }
        EOF
        nginx -g 'daemon off;'
```

Dois conceitos distintos no mesmo arquivo:

| | `initContainers` | `containers` |
|---|---|---|
| quando roda | **antes**, em sequência | depois, em paralelo |
| término | precisa **terminar com 0** | ficam de pé o tempo todo |
| uso típico | preparar terreno, esperar dependência | app + auxiliares |

---

## 3. Aplicar e ver o init container segurando o Pod

```bash
kubectl apply -f hellojava/hellojava-multicontainer.pod.yaml

# em outro terminal, acompanhar as transições:
kubectl get pod hello-multicontainer -w
# Init:0/1 → PodInitializing → Running   READY 2/2
```

Ler o log do init (ele já terminou, mas o log fica):

```bash
kubectl logs hello-multicontainer -c wait-for-dns
# [init] configurando a aplicação...
# [init] aplicação configurada!
```

> `READY 2/2` conta só os `containers`, não os `initContainers` — eles já cumpriram
> o papel e saíram.

---

## 4. ERRO PROPOSITAL — `kubectl logs` sem `-c`

```bash
kubectl logs hello-multicontainer
# error: a container name must be specified for pod hello-multicontainer,
# choose one of: [hello proxy] and one of the init containers: [wait-for-dns]
```

Num Pod multi-container o `-c` deixa de ser opcional. Corrigindo:

```bash
kubectl logs hello-multicontainer -c hello
kubectl logs hello-multicontainer -c proxy
kubectl logs hello-multicontainer --all-containers=true --prefix
```

---

## 5. O "aha" — os containers compartilham `localhost`

O `proxy` escuta na 80 e repassa para `localhost:8080`. **Sem IP, sem DNS, sem Service.**

```bash
kubectl port-forward pod/hello-multicontainer 8080:80

# noutro terminal — repare que estamos batendo na 80 do proxy:
curl -i localhost:8080
# HTTP/1.1 200 OK
# X-Servido-Por: sidecar-proxy
# {"servico":"algadelivery-hello","versao":"v1","pod":"hello-multicontainer"}
```

O header `X-Servido-Por` prova que a resposta passou pelo nginx; o corpo prova que
quem respondeu de verdade foi o Java.

Provar de dentro, container a container:

```bash
kubectl exec -it hello-multicontainer -c proxy -- sh
# dentro do proxy:
wget -qO- localhost:8080     # alcança o Java — mesmo network namespace
hostname                     # hello-multicontainer — mesmo hostname dos dois
exit
```

> Dois containers, **um** IP, **um** hostname, **um** `localhost`.
> Em máquinas separadas isso não existiria — é o Pod que garante.

---

## 6. Ciclo de vida compartilhado

Os containers de um Pod nascem, morrem e são agendados **juntos, no mesmo node**.

```bash
kubectl get pod hello-multicontainer -o wide     # um IP, um node para os dois

# derrubar só o proxy:
kubectl exec hello-multicontainer -c proxy -- kill 1
kubectl get pod hello-multicontainer             # RESTARTS sobe, Pod continua o mesmo
```

O kubelet reinicia **o container**, não o Pod. Mas se o Pod for deletado, os dois vão
junto — não dá para escalar ou reiniciar um sem o outro. É o principal trade-off:
**colocar dois processos no mesmo Pod é acoplá-los para sempre.**

---

## 7. Quando (não) usar

**Use** quando o container auxiliar precisa mesmo estar colado ao principal:
proxy, coletor de log/métrica, sincronizador de arquivos, recarregador de config.

**Não use** para dois serviços que só "conversam entre si" — isso são dois Pods e,
a partir do Módulo 6, um `Service` entre eles.

> **Loop aberto → 5.16:** esse arranjo — um container auxiliar acoplado ao principal,
> compartilhando rede e disco — tem nome, tem casos clássicos em produção e vale uma
> aula inteira: o **Sidecar Pattern**. A gente formaliza na **5.16**, depois que você
> souber trabalhar com volumes (`emptyDir`, na 5.11). Por ora, guarde o mecanismo:
> **um Pod, vários containers, mesma rede, mesmo disco.**

---

## Limpeza

```bash
kubectl delete -f hellojava/hellojava-multicontainer.pod.yaml
kubectl delete -f hellojava/hellojava.pod.yaml
```
