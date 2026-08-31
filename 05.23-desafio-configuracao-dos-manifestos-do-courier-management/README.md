# 5.23 — Desafio: Realize a configuração dos manifestos do Courier Management

Replicar, para o microsserviço `courier-management`, **tudo** o que foi construído para o
`delivery-tracking` nas aulas **5.21** (setup em Kubernetes) e **5.22** (Metrics Server + HPA).

Parte do estado entregue em `05.22-metrics-server-e-hpa-autoscaling-de-pods` — cluster Kind
`algadelivery` com Traefik, PostgreSQL, Kafka, Metrics Server e o `delivery-tracking` no ar — e
adiciona o **segundo microsserviço do AlgaDelivery**.

> **Por que este desafio existe.** A 5.21 fechou com uma lista explícita de fronteiras, e a
> primeira delas era: *"ninguém consome os eventos — e o `courier-management` não está no ar"*.
> O tópico `deliveries.v1.events` estava sendo alimentado por um produtor sem nenhum consumidor
> do outro lado, e o `POST /api/v1/deliveries` devolvia **502** porque dependia de um serviço que
> não existia. Este desafio fecha as duas pontas. No fim, o AlgaDelivery deixa de ser *um*
> serviço em Kubernetes e passa a ser **um sistema de microsserviços** em Kubernetes.

---

## 0. Ponto de partida: de onde vem o código

O projeto `courier-management/` **não nasce neste repositório** — ele vem do **Nível 9**, no
estado mais avançado que ele alcançou lá: a versão da última aula de desafio, com Gradle,
Dockerfile, pipeline, Semantic Release, Shift Left Quality e Shift Left Security já aplicados.

- Repositório: <https://github.com/algaworks/curso-especialista-microsservicos-nivel-9.git>
- Caminho do projeto base:
  [`05.08-desafio-replique-o-pipeline-seguranca-no-courier-management/courier-management`](https://github.com/algaworks/curso-especialista-microsservicos-nivel-9/tree/main/05.08-desafio-replique-o-pipeline-seguranca-no-courier-management/courier-management)

```bash
git clone https://github.com/algaworks/curso-especialista-microsservicos-nivel-9.git
cp -r curso-especialista-microsservicos-nivel-9/05.08-desafio-replique-o-pipeline-seguranca-no-courier-management/courier-management .
```

**Nenhuma linha de código Java foi alterada neste desafio.** O que existe de novo aqui é a pasta
`courier-management/k8s/`, que não existe no Nível 9 — e uma chave a mais no ConfigMap do
`delivery-tracking` (ver seção 6). Dois arquivos do projeto valem ser abertos em aula, porque é
deles que saem os valores do YAML: `src/main/resources/application.yml` (as variáveis que a
aplicação lê e os endpoints de *health*) e o `Dockerfile` (a porta do `EXPOSE`, o usuário
não-root e o entrypoint que repassa o `JAVA_OPTS`).

> **Contraste que vale dizer em voz alta:** no Nível 9 este mesmo projeto foi usado para ensinar
> *pipeline* — como construir, testar, escanear, assinar e publicar a imagem. Aqui ele é usado
> para ensinar *runtime* — como essa imagem roda, se configura, se descobre, escala e é
> publicada num cluster. É o mesmo artefato visto pelas duas pontas do ciclo de vida.

---

## 1. O enunciado (o que o aluno tem que entregar)

Seis manifestos em `courier-management/k8s/`, mais o banco de dados do serviço:

| # | Entrega | Aula de origem |
|---|---|---|
| 1 | `ConfigMap` com toda a configuração não sensível | 5.12 |
| 2 | `Secret` com as credenciais do banco | 5.13 |
| 3 | `Deployment` com **probes** e **`resources`** desde a primeira versão | 5.09 · 5.18 · 5.19 |
| 4 | `Service` **ClusterIP** | 5.16 |
| 5 | `Ingress` (`ingressClassName: traefik`) com host próprio | 5.20 |
| 6 | `HorizontalPodAutoscaler` em `autoscaling/v2` | 5.22 |
| 7 | O banco `courierdb`, que **não existe** no cluster | 5.14 · 5.15 |

**Regra do desafio:** nenhum conceito novo é permitido. Se a solução precisar de um primitivo
que o módulo não ensinou, ela está errada.

## 2. As quatro diferenças que o desafio força

Copiar os manifestos do `delivery-tracking` e trocar o nome **não funciona**. São quatro
diferenças reais, e cada uma quebra de um jeito diferente:

| # | Diferença | O que quebra se passar batido |
|---|---|---|
| 1 | **A porta é 8081**, não 8080 | a porta aparece em **quatro** lugares: `SERVER_PORT` no ConfigMap, `containerPort` no Deployment, as **três** probes e o `targetPort` do Service. Errar só nas probes deixa o Pod em `0/1` para sempre; errar só no `targetPort` deixa o Pod `1/1` e o Ingress devolvendo 502 |
| 2 | **O Ingress não pode se chamar `algadelivery`** | os manifestos da 5.20 usam `name: algadelivery` para que cada `apply` **substitua** o anterior. Reusar o nome aqui **apaga a regra do `delivery-tracking`** — e o `apply` responde `configured`, sem nenhum aviso |
| 3 | **O banco `courierdb` não existe** | o `POSTGRES_DB` do Secret do PostgreSQL criou `deliverydb`, e apenas no **primeiro boot** de cada PVC. O Pod entra em `CrashLoopBackOff` com `FATAL: database "courierdb" does not exist` |
| 4 | **Não existe `replicas` no Deployment** | é o checklist da 5.22 sendo cumprido: quando existe HPA, o campo sai do manifesto, senão cada `apply` derruba as réplicas que o HPA subiu |

## 3. A solução, arquivo por arquivo

Todos os arquivos estão comentados linha a linha em `courier-management/k8s/`. O resumo:

```
courier-management/k8s/
├── courier-management.configmap.yaml    # broker, porta 8081, Eureka off, actuator
├── courier-management.secret.yaml       # DB_URL -> postgres-1.postgres/courierdb
├── courier-management.deployment.yaml   # sem `replicas`; probes e resources em 8081
├── courier-management.service.yaml      # ClusterIP, porta 80 -> targetPort http (8081)
├── courier-management.ingress.yaml      # host courier-management.algadelivery.test
└── courier-management.hpa.yaml          # cpu 70%, min 2, max 6
```

### 3.1 O nome do Service e a porta 80 não são escolha livre

O `delivery-tracking` chama este serviço com a URL default do próprio código:

```java
@Value("${courier-management.url:http://courier-management}")
```

`http://courier-management` sem porta significa **porta 80**, e o host é o **nome do Service**.
Ou seja: chamar o Service de `courier-mgmt` ou expor na porta 8081 quebra a integração sem
mudar uma linha de YAML do `delivery-tracking`. É o argumento mais concreto possível a favor de
DNS de Service como contrato entre serviços (5.16).

## 4. O banco que não existia: uma instância por serviço

O `StatefulSet` `postgres` da 5.21 subiu com **`replicas: 2`**, e a 5.15 já tinha avisado:
**réplica de StatefulSet não é réplica de dado.** `postgres-0` e `postgres-1` são duas
instâncias PostgreSQL **independentes**, cada uma com o seu PVC — e `postgres-1` estava
**ociosa** desde a 5.21, com um `deliverydb` vazio que ninguém usava.

Este desafio dá uso a ela: o `courier-management` aponta para `postgres-1.postgres`, o que
materializa o padrão **database per service** — cada microsserviço dono do seu esquema, sem
ninguém lendo a tabela do vizinho.

```bash
kubectl -n algadelivery exec postgres-1 -- psql -U postgres -c 'CREATE DATABASE courierdb'
```

> **A armadilha do `/docker-entrypoint-initdb.d`.** A tentação é resolver isso "do jeito
> declarativo": um ConfigMap com um `.sql` montado em `/docker-entrypoint-initdb.d/`. **Não
> funciona aqui**, e o motivo é bom conteúdo: a imagem do PostgreSQL só executa aquele diretório
> quando o `PGDATA` está **vazio**, ou seja, no primeiro boot do PVC. O PVC de `postgres-1` já
> está formatado desde a 5.21. Montar o ConfigMap agora não dá erro nenhum — simplesmente não
> acontece nada, que é o pior tipo de falha. Para vê-lo funcionando é preciso apagar o PVC
> (`kubectl delete pvc dados-postgres-1`) e deixar o StatefulSet recriar.

O Flyway cria as tabelas no primeiro start da aplicação, como fez na 5.21:

```
$ kubectl -n algadelivery exec postgres-1 -- psql -U postgres -d courierdb -c '\dt'
 public | assigned_delivery     | table | postgres
 public | courier               | table | postgres
 public | flyway_schema_history | table | postgres
```

## 5. O caminho assíncrono: o consumidor entra no grupo

O `courier-management` tem um `@KafkaListener(topics = "deliveries.v1.events", groupId =
"courier-management")`. Como ele usa o **mesmo** broker do `delivery-tracking`
(`kafka-0.kafka:9092`), a integração acontece sem nenhuma configuração de rede além do
`KAFKA_BOOTSTRAP_SERVERS` no ConfigMap.

**Duas descobertas medidas no cluster, e as duas valem aula:**

**(a) O consumidor novo começa do fim do tópico.** `spring.kafka.consumer.auto-offset-reset`
vale `latest` por padrão, então um grupo novo **ignora** tudo que já estava lá. Os eventos
publicados na 5.21 **não** são processados quando o Pod sobe. O sintoma é o pior possível para
diagnosticar: aplicou tudo certo, nenhum erro em lugar nenhum, e **nada acontece**.

**(b) Duas réplicas dividem as partições — não processam a mesma mensagem.** O tópico tem 3
partições, e o grupo se distribuiu assim:

```
GROUP              TOPIC                PARTITION  LAG  CONSUMER (host)
courier-management deliveries.v1.events 0          0    10.244.2.16
courier-management deliveries.v1.events 1          0    10.244.2.16
courier-management deliveries.v1.events 2          0    10.244.1.26
```

Daí sai a observação mais fina do desafio, e ela é sobre o **HPA**: cada Pod faz duas coisas —
atende HTTP e consome do Kafka — e os dois papéis **não escalam igual**. O lado HTTP escala
livremente; o lado consumidor tem teto no número de partições (3). Se o HPA subir para 6
réplicas, **três Pods ficam com zero partição**: atendendo HTTP normalmente e sem consumir
mensagem nenhuma. É a armadilha *"escalar o que não tem por quê"* da 5.22, aparecendo num
sistema real. Se o gargalo fosse a fila, a resposta não seria HPA de CPU — seria mais partições
e escala por **LAG** do consumer group, que é o que o KEDA faz.

## 6. O caminho síncrono: por que o 502 da 5.21 **não** morre com um Service

Aqui está o achado técnico do desafio, e ele é contraintuitivo o suficiente para valer um bloco
próprio da aula.

Subir o `courier-management` com um Service ClusterIP chamado `courier-management`, na porta 80,
com dois endpoints prontos — **não conserta** o `POST /api/v1/deliveries`. Medido:

```
java.lang.IllegalArgumentException: Service Instance cannot be null, serviceId: courier-management
```

O motivo: o cliente HTTP do `delivery-tracking` é um `RestClient` anotado com **`@LoadBalanced`**
(Spring Cloud LoadBalancer, que vem transitivamente do `spring-cloud-starter-netflix-eureka-client`).
Para ele, `courier-management` **não é um nome DNS** — é um **serviceId a ser procurado num
service registry**. E o ConfigMap da 5.21 tem `EUREKA_CLIENT_ENABLED: "false"`: não existe
registry, logo não existe instância, logo a chamada morre antes de qualquer pacote sair do Pod.
O nome DNS do Service nunca é consultado.

**A lição que isso ensina é maior que o bug:** em Kubernetes, o **Service já é o service
registry**. Descoberta de serviço via Eureka + client-side load balancing é uma camada que o
cluster torna redundante — e mantê-la ligada não é neutro: ela **intercepta** a resolução de
nome e a quebra.

### 6.1 As duas saídas

| Saída | O que é | Onde entra |
|---|---|---|
| **Registrar a instância à mão** no `SimpleDiscoveryClient` | atalho de laboratório, **sem tocar no código** | é o que está aplicado neste snapshot |
| **Tirar o `@LoadBalanced`** e chamar o nome DNS direto | a resposta arquiteturalmente correta | mudança de código — assunto do Módulo 9 |

A primeira vai no ConfigMap do `delivery-tracking`, via `JAVA_OPTS`:

```yaml
JAVA_OPTS: >-
  -XX:MaxRAMPercentage=75.0
  -Dspring.cloud.discovery.client.simple.instances.courier-management[0].uri=http://courier-management:80
```

Vai como `-D` na JVM, e **não** como variável `SPRING_...`, porque a chave do mapa tem hífen
(`courier-management`) e índice (`[0]`) — e nada disso sobrevive à tradução de variável de
ambiente do Spring Boot. O `docker-entrypoint.sh` da imagem já repassa `$JAVA_OPTS` para o
`java -jar`, então nenhuma mudança de imagem é necessária.

Com isso aplicado, o rascunho volta a funcionar e o repasse vem calculado **pelo outro serviço**:

```
$ curl -X POST .../api/v1/deliveries -d '{...}'
{"id":"3f533bba-...","status":"DRAFT","distanceFee":9.30,"courierPayout":31.00,"totalCost":40.30}
HTTP 201
```

> **Aviso honesto:** mesmo com tudo certo, o rascunho **falha de vez em quando**. O
> `payout-calculation` do `courier-management` sorteia uma exceção em 50% das chamadas e dorme
> até 400 ms, contra um `readTimeout` de 200 ms no cliente. Isso é o cenário de resiliência do
> Nível 9 (Retry + Circuit Breaker), **não** problema de cluster — e o 502/504 que aparece é o
> circuito abrindo, exatamente como projetado. Chame duas ou três vezes.

## 7. Ordem de execução

A ordem importa em dois pontos: o banco antes do Deployment, e o entregador antes do evento.

```bash
# 0. imagem
cd courier-management
./gradlew clean bootJar -Pversion=1.0.0
docker build -t courier-management:1.0.0 .
kind load docker-image courier-management:1.0.0 --name algadelivery

# 1. o banco do servico (ANTES do Deployment, senao e CrashLoopBackOff)
kubectl -n algadelivery exec postgres-1 -- psql -U postgres -c 'CREATE DATABASE courierdb'

# 2. os manifestos
kubectl -n algadelivery apply -f courier-management/k8s/
kubectl -n algadelivery rollout status deployment/courier-management

# 3. o nome no /etc/hosts (5.20)
sudo sh -c 'printf "127.0.0.1\tcourier-management.algadelivery.test\n" >> /etc/hosts'

# 4. o caminho sincrono (opcional — bloco 6)
kubectl -n algadelivery apply -f delivery-tracking/k8s/delivery-tracking.configmap.yaml
kubectl -n algadelivery rollout restart deployment/delivery-tracking
```

E a prova de ponta a ponta, na ordem:

```bash
# 4.1 cadastra um entregador — SEM ISSO o handler do evento estoura NoSuchElementException
curl -X POST http://courier-management.algadelivery.test/api/v1/couriers \
  -H 'Content-Type: application/json' \
  -d '{"name":"Carlos Entregador","phone":"34 99999-0000"}'

# 4.2 cria a entrega (o repasse vem do courier-management, via Service)
curl -X POST http://delivery-tracking.algadelivery.test/api/v1/deliveries \
  -H 'Content-Type: application/json' -d @exemplo-entrega.json

# 4.3 dispara o evento
curl -X POST http://delivery-tracking.algadelivery.test/api/v1/deliveries/$ID/placement

# 4.4 o outro serviço reagiu
kubectl -n algadelivery logs -l app=courier-management --tail=20
curl http://courier-management.algadelivery.test/api/v1/couriers
```

## 8. Validação executada no cluster

Tudo abaixo foi rodado no cluster Kind `algadelivery` (Kubernetes v1.36.1, 3 nodes).

| Verificação | Resultado |
|---|---|
| `gradlew clean bootJar -Pversion=1.0.0` | `BUILD SUCCESSFUL` — `courier-management-1.0.0.jar` |
| `docker build` + `kind load docker-image` | imagem carregada nos 3 nodes |
| `CREATE DATABASE courierdb` em `postgres-1` | OK |
| `rollout status deployment/courier-management` | `successfully rolled out` |
| Réplicas após o primeiro `apply` | **1 → 2**, elevada pelo `minReplicas` do HPA |
| Tabelas criadas pelo Flyway em `courierdb` | `courier`, `assigned_delivery`, `flyway_schema_history` |
| `GET /actuator/health` pelo Ingress | `HTTP 200` · `{"status":"UP"}` |
| `POST /api/v1/couriers` pelo Ingress | `HTTP 201` |
| `POST /api/v1/deliveries` (caminho **síncrono**, via Service) | `HTTP 201` · `courierPayout: 31.00` |
| `POST /{id}/placement` | `HTTP 200` |
| Consumo do evento no `courier-management` | `Received: DeliveryPlacedIntegrationEvent(deliveryId=3f533bba-…)` |
| Atribuição no domínio | `Courier 1458de1b-… assigned to delivery 3f533bba-…` |
| `GET /api/v1/couriers` depois do evento | `pendingDeliveriesQuantity: 1` |
| Partições do `deliveries.v1.events` | **3** · grupo dividido `[0,1]` num Pod e `[2]` no outro · LAG `0` |
| `kubectl -n algadelivery get hpa courier-management` | `cpu: 9%/70%` · `2/6` réplicas |
| Sem o `JAVA_OPTS` de discovery, com o Service de pé | `IllegalArgumentException: Service Instance cannot be null` → **502** |

## 9. Erros para mostrar ao vivo

| Erro | Como provocar | Sintoma |
|---|---|---|
| Banco inexistente | aplicar o Deployment antes do `CREATE DATABASE` | `CrashLoopBackOff` · `FATAL: database "courierdb" does not exist` |
| Porta errada nas probes | trocar `8081` por `8080` nas três probes | Pod `0/1 Running` para sempre, sem restart; `describe` mostra o `startupProbe` falhando |
| Porta errada no Service | `targetPort: 8080` | Pod `1/1 Running` e Ingress devolvendo **502** — o erro mais confuso do conjunto |
| Ingress com nome reusado | `metadata.name: algadelivery` | o `apply` diz `configured` e a regra do `delivery-tracking` **desaparece** |
| Consumidor sem backlog | subir o Pod e conferir o log | nenhum erro, e **nada acontece** (`auto-offset-reset: latest`) |
| Evento sem entregador cadastrado | descomentar o `earliest` sem ter feito `POST /couriers` | `NoSuchElementException` em cada evento do backlog |
| `replicas` brigando com o HPA | pôr `replicas: 2` de volta e aplicar durante um pico | serrote: o `apply` derruba, o HPA sobe |
| `@LoadBalanced` sem registry | não aplicar o `JAVA_OPTS` | **502** com `Service Instance cannot be null`, **mesmo com o Service de pé** |

## 10. O que este desafio **não** resolve

As mesmas fronteiras da 5.21, ainda de pé — e duas novas:

- **Banco como Pod.** Duas instâncias PostgreSQL em `StatefulSet`, sem replicação e sem
  failover de dados. Em produção é **RDS** (Módulo 4/6).
- **Sem TLS e sem domínio real.** `.test` no `/etc/hosts`, HTTP puro. ACM + Cloudflare é
  Módulo 6.
- **Manifestos copiados e colados.** Agora são **doze** arquivos YAML com valores fixos, e a
  duplicação entre os dois serviços ficou impossível de ignorar — os dois Deployments diferem em
  quatro campos. É o argumento do **Helm** (Módulo 7) se escrevendo sozinho.
- **Dois segredos com a mesma senha, mantidos à mão.** `postgres-cred`,
  `delivery-tracking-secret` e agora `courier-management-secret`. Gestor de segredos externo é
  Módulo 8.
- 🆕 **Descoberta de serviço meio-a-meio.** O atalho do `SimpleDiscoveryClient` faz o caminho
  síncrono funcionar, mas a arquitetura correta em Kubernetes é abandonar o client-side load
  balancing. Isso é código, e é conversa do Módulo 9.
- 🆕 **`maxReplicas` maior que o número de partições.** Correto para o tráfego HTTP, inútil para
  a fila. Escala por LAG (KEDA) está fora do escopo do curso.

## 11. Achados nos manifestos das aulas anteriores

Encontrados ao montar este desafio. Os dois primeiros **já estão corrigidos neste snapshot**; o
terceiro fica como decisão do instrutor, porque muda um manifesto de aula já roteirizada.

| # | Achado | Situação |
|---|---|---|
| 1 | `delivery-tracking.ingress.yaml` usava um host terminado em `.algashop.test` — `algashop` é outro projeto, não o AlgaDelivery | ✅ corrigido aqui para `delivery-tracking.algadelivery.test` |
| 2 | `delivery-tracking.ingress.yaml` e `hellojava.ingress.yaml` compartilhavam `metadata.name: algadelivery`, então aplicar um **apagava** o outro | ✅ corrigido aqui: o Ingress da API passou a se chamar `delivery-tracking`. Era inofensivo enquanto só um serviço estava no ar; com dois, é bug |
| 3 | `delivery-tracking.deployment.yaml` ainda tem `replicas: 2` **com HPA aplicado** | ⚠️ **em aberto** — contraria o checklist da própria 5.22 ("Removi o campo `replicas` do Deployment que tem HPA"). Vale corrigir na 5.22, não aqui |

> Consequência prática do achado 2 para a gravação: as **duas** linhas precisam estar no
> `/etc/hosts`, e os dois hosts agora terminam em `.algadelivery.test`:
>
> ```bash
> sudo sh -c 'printf "\n127.0.0.1\tdelivery-tracking.algadelivery.test\n127.0.0.1\tcourier-management.algadelivery.test\n" >> /etc/hosts'
> ```
