## Comando de simulação

# terminal 1 — consumidor pendurado no topico, para ver a mensagem chegar ao vivo
kubectl exec -it kafka-0 -- /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic deliveries.v1.events --from-beginning

# terminal 2 — a transicao de estado
curl -i -X POST localhost/api/v1/deliveries/11111111-1111-1111-1111-111111111111/placement
# HTTP/1.1 200