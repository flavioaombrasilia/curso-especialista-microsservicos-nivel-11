## Prova de ponta a ponta — os dois caminhos entre os microsserviços

Pré-requisitos: manifestos aplicados, `courierdb` criado em `postgres-1`, e as duas linhas no
`/etc/hosts` (`delivery-tracking.algadelivery.test` e `courier-management.algadelivery.test`).

### 1. Cadastrar um entregador (SEMPRE primeiro)

Sem nenhum entregador no banco, o handler do evento estoura `NoSuchElementException` —
`courierRepository.findTop1ByOrderByLastFulfilledDeliveryAtAsc().orElseThrow()`.

```bash
curl -s -X POST http://courier-management.algadelivery.test/api/v1/couriers \
  -H 'Content-Type: application/json' \
  -d '{"name":"Carlos Entregador","phone":"34 99999-0000"}' | jq
# HTTP 201 — guarde o "id"
```

### 2. Caminho SÍNCRONO — HTTP entre serviços, pelo Service ClusterIP

O `distanceFee`/`courierPayout` da resposta foi calculado pelo `courier-management`.
Se devolver 502/504, chame de novo: o `payout-calculation` falha de propósito em ~50% das vezes.

```bash
curl -s -X POST http://delivery-tracking.algadelivery.test/api/v1/deliveries \
  -H 'Content-Type: application/json' -d @exemplo-entrega.json | jq '{id, status, courierPayout}'
# { "id": "...", "status": "DRAFT", "courierPayout": 31.00 }
```

### 3. Caminho ASSÍNCRONO — evento pelo Kafka

```bash
ID=<o id devolvido acima>
curl -i -X POST http://delivery-tracking.algadelivery.test/api/v1/deliveries/$ID/placement
# HTTP/1.1 200
```

### 4. A prova do outro lado

```bash
kubectl -n algadelivery logs -l app=courier-management --tail=30 | grep -E "Received|assigned"
# Received: DeliveryPlacedIntegrationEvent(occurredAt=..., deliveryId=<ID>)
# Courier <courierId> assigned to delivery <ID>

curl -s http://courier-management.algadelivery.test/api/v1/couriers | jq '.content[0]'
# "pendingDeliveriesQuantity": 1
```

### 5. Quem consumiu o quê (a divisão de partições)

```bash
kubectl -n algadelivery exec kafka-0 -- /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --describe --topic deliveries.v1.events
# PartitionCount: 3

kubectl -n algadelivery exec kafka-0 -- /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --describe --group courier-management
# duas replicas, tres particoes: um Pod fica com [0,1] e o outro com [2]
```

### 6. Gerador de carga para o HPA

O `courier-management` não tem endpoint de queima de CPU como o `hellojava`. A carga aqui é a
listagem paginada, que passa pelo banco — e é justamente por isso que ela ensina: o gargalo
deste serviço não é CPU da aplicação.

```bash
kubectl -n algadelivery run gerador-courier --rm -it --restart=Never \
  --image=busybox:1.37 -- \
  sh -c 'while true; do wget -q -O- "http://courier-management/api/v1/couriers?size=100" >/dev/null; done'

# em outro terminal
kubectl -n algadelivery get hpa courier-management -w
kubectl -n algadelivery top pods -l app=courier-management
```
