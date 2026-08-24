# Aula 5.07 — Namespaces, Labels, Selectors e Annotations

Passo a passo para executar ao vivo. O `hellojava/hellojava.pod.yaml` está no estado da
aula 5.06 (só `app: hellojava`) — as labels e annotations são digitadas durante a gravação.

---

## 1. Criar o namespace e se mudar para ele

```bash
kubectl get namespaces

kubectl create namespace algadelivery
kubectl config set-context --current --namespace=algadelivery

# confirmar em qual namespace você está
kubectl config view --minify | grep namespace:
```

> Isso muda o namespace **padrão do seu contexto** (kubeconfig) — some o `-n algadelivery`
> de todo comando daqui pra frente. É conveniência de quem opera, não config do cluster.

---

## 2. Ajustar o `hellojava/hellojava.pod.yaml`

**Antes** (como veio da 5.06):

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: hellojava
  labels:
    app: hellojava
spec:
  containers:
  - name: hellojava
    image: hellojava:v1
    ports:
      - containerPort: 8080
```

**Depois** — digitar as labels e as annotations ao vivo:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: hellojava
  # Labels: metadados IDENTIFICADORES. Curtos, validados, indexados.
  # É por aqui que selectors e controllers encontram este Pod.
  labels:
    app: hellojava
    tier: backend
    env: dev
    # convenção da comunidade — é o que as ferramentas esperam encontrar
    app.kubernetes.io/name: hellojava
    app.kubernetes.io/version: "v1"
    app.kubernetes.io/component: api
    app.kubernetes.io/part-of: algadelivery
  # Annotations: metadados NÃO-identificadores. Podem ser longos e estruturados.
  # NÃO dá para selecionar por annotation.
  annotations:
    time-responsavel: "AlgaDelivery / backend"
    descricao: "API de exemplo do modulo 5 — usada para demonstrar workloads"
    runbook: "https://wiki.interna.algadelivery.com/runbooks/hellojava"
    # loop aberto para a 5.10: aqui é só texto colado no objeto.
    # Num Deployment, vira a coluna CHANGE-CAUSE do `kubectl rollout history`.
    kubernetes.io/change-cause: "subindo a v1 do hellojava"
spec:
  containers:
  - name: hellojava
    image: hellojava:v1
    ports:
      - containerPort: 8080
```

Aplicar e conferir:

```bash
kubectl apply -f hellojava/hellojava.pod.yaml
kubectl get pods
```

---

## 3. Labels: ver, adicionar e trocar ao vivo

```bash
kubectl get pods --show-labels
kubectl get pods -L app,tier,env          # uma coluna por label

kubectl label pod hellojava time=backend     # adicionar
kubectl label pod hellojava env=staging --overwrite   # trocar valor existente
kubectl label pod hellojava time-            # remover (sufixo "-")
```

---

## 4. Selectors: consultando por label

```bash
# equality-based
kubectl get pods -l app=hellojava
kubectl get pods -l 'env!=prod'

# set-based
kubectl get pods -l 'env in (dev,staging)'
kubectl get pods -l 'tier notin (db)'
kubectl get pods -l app                   # só a existência da chave

# combinar filtros = AND
kubectl get pods -l app=hellojava,tier=backend

# o mesmo mecanismo serve para agir em lote
kubectl label pods -l tier=backend revisado=sim
```

---

## 5. `selector.matchLabels` — como um controller adota Pods (clímax)

Só mostrar o trecho no editor; **não** aplicar — o ReplicaSet inteiro é a aula 5.08.

```yaml
spec:
  selector:
    matchLabels:
      app: hellojava        # <- o controller procura Pods com esta label
  template:
    metadata:
      labels:
        app: hellojava      # <- os Pods criados nascem com esta label → casam
    spec:
      containers:
        - name: hellojava
          image: hellojava:v1
```

---

## 6. Annotations ao vivo

```bash
kubectl annotate pod hellojava dono="squad-entregas"
kubectl describe pod hellojava | sed -n '/Annotations:/,/^[A-Z]/p'

kubectl annotate pod hellojava dono="squad-pedidos" --overwrite
kubectl annotate pod hellojava dono-          # remover
```

---

## 7. Os três erros silenciosos (nenhum devolve mensagem de erro)

**7.1 "Meu Pod sumiu" — esquecer o namespace**

```bash
kubectl config set-context --current --namespace=default
kubectl get pods                  # No resources found in default namespace.
kubectl get pods -A | grep hellojava # está lá, em algadelivery
kubectl config set-context --current --namespace=algadelivery
```

**7.2 Selector que não casa com ninguém**

```bash
kubectl get pods -l app=hellojav     # No resources found — zero reclamação
kubectl get pods --show-labels    # comparar caractere a caractere (case-sensitive)
kubectl get pods -l app=Hellojava    # também não casa
```

**7.3 Tentar selecionar por annotation**

```bash
kubectl get pods -l kubernetes.io/change-cause="subindo a v1 do hellojava"
# No resources found — mesmo com a annotation visível no describe

kubectl get pods -l time=backend  # mesma informação como label → aparece
```

---

## 8. Acessar a app (só port-forward nesta aula)

```bash
kubectl port-forward pod/hellojava 8080:8080
curl http://localhost:8080
```

---

## Limpeza

```bash
kubectl delete pods -l app=hellojava
# ou, ao final do módulo:
# kubectl delete namespace algadelivery
```
