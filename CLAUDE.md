# CLAUDE.md

Este arquivo orienta o Claude Code (claude.ai/code) ao trabalhar com o código deste repositório.

## Visão geral

Repositório do curso **"Microsserviços em Produção na AWS — Nível 11"** (Algaworks). O projeto prático é o **AlgaDelivery**, uma plataforma de microsserviços implantada na AWS. Cada diretório numerado (ex.: `03.07-trabalhando-com-variaveis/`) é um snapshot autocontido de uma aula específica, construído incrementalmente sobre o anterior. Toda a infraestrutura é escrita em **OpenTofu** (arquivos `.tofu`).

A ementa completa do curso está em `.EMENTA-NIVEL-11-AWS.md` — trate-a como a fonte da verdade para a estrutura de módulos/aulas.

## Estrutura do curso (13 módulos)

| # | Módulo | Status | Aulas |
|---|---|---|---|
| 1 | Fundamentos de Cloud Computing | ✅ finalizado | 7 |
| 2 | Fundamentos de Redes na AWS | ✅ finalizado | 19 |
| 3 | Infraestrutura como Código (IaC) na AWS | ✅ finalizado | 21 |
| 4 | IaC Modular e Serviços Gerenciados na AWS | ✅ finalizado | 21 |
| 5 | Kubernetes: Fundamentos e Workloads | 🎥 em gravação | 26 |
| 6 | Provisionando o cluster EKS na AWS com OpenTofu | 📋 ementa planejada | 14 |
| 7 | Ferramentas de Deploy e GitOps | 📋 ementa planejada | 16 |
| 8 | Configuração Externalizada e Gestão de Segredos | 📋 ementa planejada | 10 |
| 9 | Deploy do Backend do AlgaDelivery na AWS | 📋 ementa planejada | 6 |
| 10 | Deploy do Frontend do AlgaDelivery (S3 + CloudFront) | 📋 ementa planejada | 6 |
| 11 | Observabilidade em Produção: Prometheus, Grafana e Elastic APM | 📋 ementa planejada | 19 |
| 12 | Apêndice — Deploy Algashop | 🔜 a planejar | — |

**Arco narrativo:** Fundamentos de cloud → Redes na AWS → Fundamentos de IaC → IaC modular + serviços gerenciados → **Kubernetes completo em cluster local com Kind** (workloads, storage, rede, escala e teste de carga) → **Provisionar EKS** (ALB Ingress + ACM) → Ferramentas de deploy (Helm/ArgoCD) → Configuração externalizada → Deploy do backend do AlgaDelivery em infra real → Deploy do frontend do AlgaDelivery (S3 + CloudFront) → Observá-lo em produção (Prometheus/Grafana + Elastic APM) → Apêndice.

> **Fusão M5 + M6 (decisão do instrutor):** os antigos Módulos 5 (Fundamentos e Workloads) e 6 (Rede, Escala e Segurança) foram **unificados em um único Módulo 5 de 26 aulas**, e todos os módulos seguintes foram renumerados (−1). O objetivo foi reduzir a contagem total de aulas e deixar o bloco de Kubernetes mais leve. Ver "Conteúdo cortado na fusão M5+M6" em Lacunas conhecidas.

## Princípios pedagógicos

Estes princípios emergiram do design do curso e devem guiar sugestões de conteúdo/código:

1. **Conceito → Implementação** — conceitos abstratos são ensinados imediatamente antes de sua implementação concreta, não separadamente. Quando o conceito é curto demais para um vídeo próprio, ele vira o **bloco de abertura** da aula do primitivo, em vez de uma aula solta:
   - O Módulo 5 abre com Containers vs VMs (`5.01`) e a intro ao K8s (`5.02`) como *construção argumentativa* para "por isso o K8s existe"
   - **Failover** (conceito) abre a aula de **ReplicaSet** (`5.08 ReplicaSet: failover e disponibilidade de Pods`) — as duas eram aulas separadas e foram fundidas: o conceito sozinho não sustentava um vídeo. O bloco de abertura carrega a demo da dor (`kubectl drain --force` num Pod sem `ownerReferences`), o argumento MTTR > MTBF e a disponibilidade em série (0,999⁵ ≈ 99,5%)
   - **Self Healing** (conceito) e **Probes** (o mecanismo que o implementa) foram fundidos numa aula só, `5.19` — o conceito é o bloco de abertura
   - **Ferramenta → aplicação da ferramenta**: o k6 é ensinado como ferramenta em `5.22` e só depois aplicado ao cluster em `5.23` para validar HPA e self-healing
2. **Console primeiro, depois IaC** — muitos serviços AWS são introduzidos manualmente pelo Console e depois reprovisionados com OpenTofu. Padrão visível no Módulo 4 (RDS, ElastiCache, MSK) e que continua no Módulo 6 (EKS, node groups, add-ons, EBS CSI, ACM).
3. **Sem mini-labs nem desafio no Módulo 1** — o módulo é 100% conceitual (7 aulas, de `1.01` a `1.07 Shared Responsibility Model`). Todo o hands-on começa a partir do Módulo 2, com a criação da conta AWS (`2.02`).
4. **O Módulo 4 tem dois desafios** — `4.16 Desafio: Criando Módulo ElastiCache` e `4.19 Desafio: Modulo MSK`. Padrão: primeiro entregar o serviço hands-on, depois desafiar o aluno a modularizá-lo.
5. **Conceitos de cloud ficam no Módulo 1; primitivos de computação vão para o Módulo 5** — VMs e Containers foram movidos para a abertura do Módulo 5 (`5.01`) para ficarem contíguos ao K8s. O Módulo 1 agora é exclusivamente "o que é cloud + propriedades operacionais + quando migrar". As aulas de **Cloud Native/CNCF** e os três **Service Instance Patterns** foram **removidas**: a pitada de CNCF foi absorvida em 1 slide da `5.02`, e os Service Instance Patterns saíram inteiros (junto com a aula "Aplicando Service Instance per Container com Pods", que fechava aquele arco — não confundir com o `5.17` atual, que é o setup do AlgaDelivery).
6. **Fundamentos de rede ficam no Módulo 2, não duplicados no Módulo 1** — os itens 2.05 (fundamentos de VPC) e 2.06 (CIDR) cobrem IP/DNS/subnets, então o Módulo 1 omite esses tópicos.
7. **Arco de storage contíguo** — volumes efêmeros (`5.11`), PV/PVC/StorageClass (`5.14`) e StatefulSets (`5.15`) são vizinhos porque `volumeClaimTemplates` é incompreensível sem PVC. Os callbacks do Módulo 6 (EBS CSI Driver) e do Módulo 11 (Elasticsearch stateful) seguem válidos.
8. **Labels e Selectors precedem os controllers** — `Namespaces, Labels, Selectors e Annotations` (`5.07`) vem antes porque `selector.matchLabels` é pré-requisito de ReplicaSet (`5.08`), Deployment (`5.09`) e Service (`5.16`).
9. **A aula de integração fecha o primeiro arco** — `5.17 Setup do AlgaDelivery em k8s (api + db)` não ensina primitivo novo: ela obriga o aluno a compor Deployment + StatefulSet + PVC + ConfigMap + Secret + Service numa aplicação real. É o ponto de virada entre "primitivos isolados" e "operar a aplicação" (Ingress, probes, limites, HPA, carga).

### Lacunas conhecidas (intencionais, não omissões)

- **IAM** — introduzido pontualmente nos Módulos 6/8 apenas onde é necessário (IRSA no setup do EKS, segredos); sem módulo dedicado.
- **FinOps** — apenas `1.06 Cloud Providers, precificação, Free Tier e Calculadora da AWS` e `2.03 Criando budgets para controle de custo`; sem cobertura mais profunda planejada.

### Conteúdo cortado na fusão M5+M6

Os tópicos abaixo estavam nas ementas antigas dos Módulos 5 e 6 e **não sobreviveram** à unificação. Não são esquecimento — foram cortados para enxugar o bloco de Kubernetes. Registrados aqui porque alguns têm dependentes vivos em módulos posteriores:

| Tópico cortado | Estava em | Dependente que ficou órfão |
|---|---|---|
| Sidecar Pattern | antigo `5.16` | — (o multi-container de `5.06` continua ensinado) |
| DaemonSets | antigo `5.17` | — |
| Node Selector, Affinity e Anti-Affinity | antigo `6.10–6.11` | ⚠️ `11.03` (node group de tooling isolado por affinity) |
| Taints e Tolerations (+ `cordon`/`drain`) | antigo `6.12` | ⚠️ `11.03`; a demo de `drain` de `5.08` (ReplicaSet) |
| PodDisruptionBudget | antigo `6.13` | ⚠️ upgrade de node group no Módulo 6 |
| Service Accounts e RBAC | antigo `6.14` | ⚠️ IRSA em `6.08` (AWS Load Balancer Controller) |

**Onde isso dói:** a aula `11.03 Node group dedicado de tooling` foi escrita como *callback* a taints/tolerations/affinity — sem essas aulas, ela passa a ser a **primeira** apresentação desses conceitos e precisa ensiná-los inline. O mesmo vale para RBAC/ServiceAccount antes do IRSA em `6.08`.

O DNS interno do cluster (antigo `6.02`) e o Headless Service (antigo `6.03`) foram **absorvidos** em `5.16 Services`, não cortados.

## Comandos OpenTofu

Todos os comandos usam `tofu`, não `terraform`.

```bash
tofu init       # inicializa providers e módulos
tofu validate   # valida a sintaxe da configuração
tofu fmt -recursive  # formata todos os arquivos .tofu
tofu plan       # prévia das mudanças
tofu apply      # aplica as mudanças
tofu destroy    # destrói todos os recursos
```

Para aulas com a estrutura multi-ambiente (`envs/dev`, `envs/prod`), execute os comandos de dentro do diretório do ambiente:

```bash
cd <aula>/envs/dev
tofu init
tofu plan
tofu apply
```

## Arquitetura

### Progressão das aulas

- **Módulo 3 (03.XX) — Infraestrutura como Código (IaC) na AWS:** fundamentos do OpenTofu + provisionamento de uma rede completa do zero. Estrutura flat, um único diretório com `main.tofu`, `variables.tofu`, `outputs.tofu`, `providers.tofu`. Constrói até: VPC + subnets públicas/privadas + IGW + NAT Gateway + route tables + Security Groups (Bastion + Private) + EC2 (pública + privada), tudo via OpenTofu. Termina com testes de conectividade e destroy.
- **Módulo 4 (04.XX) — IaC Modular e Serviços Gerenciados na AWS:** modularização (dynamic blocks, for expressions, lifecycle) e serviços gerenciados. Introduz `modules/vpc`, `modules/ec2`, `modules/rds`, `modules/elasticache`, `modules/msk`. Estrutura multi-ambiente (`envs/dev`, `envs/prod`) a partir de `04.07-explorando-multi-env`.

### Nomenclatura dos diretórios de aula

Os diretórios seguem `<MM>.<NN>-<titulo-em-kebab-case>/`:
- `03.07-trabalhando-com-variaveis/`
- `04.13-configurando-backup-e-manutencao-no-rds/`

A numeração segue a ementa. Variantes ocasionais `*-final/` indicam o estado "após a correção" de uma aula (ex.: `04.13-configurando-backup-e-manutencao-no-rds-final/`).

### Estrutura final do Módulo 4 (a partir de `04.07-explorando-multi-env`)

```
<aula>/
├── modules/
│   ├── vpc/          # VPC, subnets (públicas/privadas), IGW, NAT Gateway, route tables
│   ├── ec2/          # Key pair (TLS gerado), EIP, instância EC2, security group
│   ├── rds/          # RDS PostgreSQL: parameter groups, backup, janela de manutenção, senha segura
│   ├── elasticache/  # ElastiCache Valkey
│   └── msk/          # Amazon MSK (Kafka)
├── envs/
│   ├── dev/          # Root module do ambiente de desenvolvimento
│   └── prod/         # Root module do ambiente de produção
```

Cada diretório `envs/<env>/` é um root module independente. Os arquivos de composição crescem conforme os módulos são adicionados: `main.tofu`, `vpc.tofu`, `ec2.tofu`, `rds.tofu`, `elasticache.tofu`, `msk.tofu`, `data.tofu`, `local.tofu`, `variables.tofu`, `outputs.tofu`, `providers.tofu`.

### Fluxo de dados entre módulos

```
module.vpc.vpc_id             → module.ec2, module.rds, module.elasticache, module.msk (placement, SG)
module.vpc.public_subnet_ids  → module.ec2 (placement do bastion host)
module.vpc.private_subnet_ids → module.rds, module.elasticache, module.msk (placement privado)
module.vpc.vpc_cidr           → module.ec2, module.rds, module.elasticache, module.msk (regras CIDR do SG)
```

### Estado remoto

O estado é armazenado no S3. O backend é configurado em `providers.tofu`:
- Bucket: `algadelivery-tfstate`
- Key: `algadelivery/${var.environment}/remote.tfstate`

### Principais padrões de design

- O local **`common_tags`** propaga `Environment` e `ManagedBy = "OpenTofu"` para todos os recursos via `merge(var.common_tags, { Name = "..." })`
- **Dynamic blocks** para regras de ingress/egress de security group — as regras são passadas como variáveis `list(object({...}))` (introduzido em `04.02-utilizando-dynamic-blocks`)
- **For expressions e type constraints** introduzidos em `04.04-tipos-de-coleção-e-for-expressions`
- **`ngw_regional_mode`**: variável booleana traduzida para `"regional"` ou `"zonal"` nos locals, controlando o modo de disponibilidade do NAT Gateway
- **`lifecycle { prevent_destroy = true; ignore_changes = [ami] }`** nas instâncias EC2 a partir de `04.08-utilizando-o-lifecycle`
- **Key pairs SSH** são geradas em tempo de apply via `tls_private_key` + `aws_key_pair` + `local_sensitive_file` (`.pem` gravado em `path.root`, no gitignore)
- **Meu IP público** é obtido dinamicamente via `data.http.my_public_ip` e usado nas regras de SSH do bastion
- **Senhas seguras do RDS**: gerenciadas via `random_password` (introduzido em `04.11-gerenciando-senhas-com-segurança-no-rds`) — nunca commitadas; outputs sensíveis
- **Backup e janela de manutenção do RDS**: configurados via variáveis dedicadas do módulo a partir de `04.13-configurando-backup-e-manutencao-no-rds`

### Providers (Módulo 4 — atual)

| Provider | Versão | Uso |
|---|---|---|
| `hashicorp/aws` | `~> 6.0` | Todos os recursos AWS (VPC, EC2, RDS, ElastiCache, MSK, etc.) |
| `hashicorp/http` | `~> 3.0` | Obter o IP público do chamador |
| `hashicorp/tls` | `~> 4.0` | Gerar key pairs RSA |
| `hashicorp/local` | `~> 2.0` | Gravar arquivos `.pem` em disco |
| `hashicorp/random` | `~> 3.0` | Gerar senhas seguras do RDS |

### Providers planejados (Módulos 5+)

| Provider | Quando é introduzido | Uso |
|---|---|---|
| `hashicorp/kubernetes` | Módulo 6 (setup do EKS) | Recursos Kubernetes (namespaces, service accounts, etc.) |
| `hashicorp/helm` | Módulo 6 (AWS Load Balancer Controller em `6.08`) → toda instalação Helm real do curso usa o provider | Todos os releases Helm provisionados via IaC, não via Helm CLI |

## Módulos planejados (ainda não registrados)

### Módulo 5 — Kubernetes: Fundamentos e Workloads (local com Kind)

**Todo o conteúdo de Kubernetes do curso, em 26 aulas, 100% local com Kind.** Resultado da fusão dos antigos Módulos 5 (Fundamentos e Workloads) e 6 (Rede, Escala e Segurança). O aluno sai daqui sabendo fazer deploy, expor, escalar e testar sob carga uma aplicação real — sem nunca ter tocado na AWS.

O módulo tem quatro arcos:

**Arco 1 — Ponte conceitual e primitivos (`5.01–5.10`)**
- `5.01` Containers vs VMs: isolamento, portabilidade e trade-offs (as 3 antigas aulas de introdução — VMs, Containers, Containers-vs-VMs — fundidas em uma, já que Docker é pré-requisito do curso)
- `5.02` Introdução ao Kubernetes: o que é e por que existe (o modelo declarativo/control loop; a antiga aula de CNCF virou 1 slide aqui)
- `5.03` Arquitetura do K8s, Control Plane e Worker Nodes
- `5.04` Setup: kubectl + Kind + cluster local
- `5.05` Pods: ciclo de vida e criação — fases, `restartPolicy`, `kubectl describe/logs`
- `5.06` Pods multi-container: compartilhamento de network namespace e volumes
- `5.07` Namespaces, Labels, Selectors e Annotations — pré-requisito de `matchLabels` para tudo que vem depois
- `5.08` ReplicaSet: failover e disponibilidade de Pods — **fusão** da antiga aula-conceito de Failover com o ReplicaSet (ver princípio 1). Carrega a demo da dor, o argumento MTTR > MTBF e a disponibilidade em série (0,999⁵ ≈ 99,5%)
- `5.09` Deployments: gerenciando ReplicaSets e estratégias (Recreate e Rolling Update)
- `5.10` Revisões e rollback de Deployments — `rollout history/undo`, `revisionHistoryLimit`, `kubernetes.io/change-cause`, `pause/resume`, `progressDeadlineSeconds`

**Arco 2 — Configuração e storage (`5.11–5.15`)**
- `5.11` Volumes efêmeros: emptyDir, hostPath e Downward API — vem antes de ConfigMaps/Secrets para que a montagem como volume não deva explicação
- `5.12` ConfigMaps / `5.13` Secrets — `envFrom`, `env.valueFrom`, montagem como volume, `immutable`, e a exceção do `subPath` na propagação
- `5.14` Persistent Volumes, PVC e Storage Classes — o contrato "PVC é o pedido, PV é o recurso, StorageClass é o fabricante"; no Kind o provisioner é o `local-path` (StorageClass default `standard`)
- `5.15` StatefulSets: identidade estável, ordenação e `volumeClaimTemplates` (depende do PVC de `5.14`). Fecha com o contraste "failover de dados é RDS Multi-AZ, não K8s" — paga a dívida conceitual do Módulo 4

**Arco 3 — Rede e a aplicação real (`5.16–5.18`)**
- `5.16` Services: ClusterIP, NodePort e Headless — o problema do IP efêmero, Endpoints/EndpointSlice, DNS interno (`<svc>.<ns>.svc.cluster.local`). O Headless fecha o loop aberto pelo `serviceName` do StatefulSet em `5.15`
- `5.17` **Setup do AlgaDelivery em k8s (api + db)** — aula de integração, o ponto de virada do módulo (ver princípio 9): Deployment para a API, StatefulSet + PVC para o Postgres, ConfigMap + Secret para config/credenciais, Services para a comunicação interna
- `5.18` Ingress: **Traefik v3** com path-based e host-based routing — o limite L4 do Service e por que existe L7. Só local, com Kind (`extraPortMappings` 30080→80 / 30443→443 + Service NodePort fixo). O `rewrite-target` vira CRD `Middleware`, e é daí que sai o contraste que sustenta a aula: **Ingress sem anotação é padrão e viaja para o ALB; anotação e CRD são proprietários e se jogam fora**. Na AWS o ingress é o ALB (Módulo 6)
  - **Por que Traefik e não ingress-nginx:** o ingress-nginx foi anunciado como aposentado em 11/11/2025 e efetivamente aposentado em março/2026 (última release `controller-v1.15.1` em 19/03/2026, repo arquivado em 23/03/2026). O curso mantém o bloco histórico explicando isso, mas **não grava em cima de projeto morto**. Traefik foi escolhido em vez de Gateway API porque precisa ensinar o **recurso `Ingress`** — é o que o aluno vai escrever no Módulo 6 com `ingressClassName: alb`

**Arco 4 — Operar sob carga (`5.19–5.26`)**
- `5.19` Self Healing com Readiness, Liveness e Startup Probes (conceito + mecanismo fundidos). Readiness tira o Pod do Endpoints — callback forte a `5.16`
- `5.20` Resource Requests, Limits e QoS Classes — CPU faz throttling, memória mata (OOMKilled/137); Guaranteed, Burstable, BestEffort e a ordem de eviction
- `5.21` Metrics Server e HPA: autoscaling de Pods — a % do HPA é sobre o **request**, então `5.20` é pré-requisito duro
- `5.22` **Grafana k6**: a ferramenta — instalação, anatomia do script, VUs, `stages`, `thresholds` e leitura das métricas (separada da aplicação para a ferramenta ser ensinada antes de usada)
- `5.23` **Load testing com k6** — aplica o k6 para validar empiricamente HPA, self-healing e disponibilidade. É aqui que mora a tabela de "noves" de disponibilidade, porque os `thresholds` do k6 *são* error budget executável. Apenas autoscaling em nível de pod; autoscaling de node (Cluster Autoscaler/Karpenter) foi intencionalmente cortado do Módulo 6
- `5.24` Jobs e CronJobs: tarefas pontuais e agendadas
- `5.25` Lens: IDE visual para gestão do cluster
- `5.26` Rancher: plataforma de gestão de clusters — **posição ainda em avaliação pelo instrutor**

### Módulo 6 — Provisionando o cluster EKS na AWS com OpenTofu

Provisionamento de infra puro antes de qualquer deploy de aplicação. Cada componente gerenciado segue o padrão **Console → OpenTofu**:
- Cluster EKS, node groups, add-ons + EBS CSI Driver
- **Sem node autoscaling** — Cluster Autoscaler / Karpenter foram intencionalmente cortados. O curso ensina autoscaling em nível de pod (HPA) no Módulo 5 (`5.21`) mas NÃO cobre autoscaling em nível de node no cluster EKS
- **Core EKS add-ons** (VPC CNI, CoreDNS, kube-proxy) já vêm pré-instalados com o cluster (self-managed quando criados via API/OpenTofu, EKS-managed quando criados via Console). As duas aulas de add-ons (`6.06` Console, `6.07` OpenTofu) cobrem promovê-los para EKS-managed add-ons **e ensinam o EBS CSI Driver nas mesmas aulas** — o CSI Driver é ele mesmo um EKS add-on (NÃO default; necessário para o PersistentVolume do Elasticsearch stateful no Módulo 11), então é incorporado em vez de ganhar seu próprio par Console/OpenTofu
- **AWS Load Balancer Controller** (instalado via Helm provider, precisa de IRSA/IAM) → **ALB Ingress** para roteamento L7 por path/host (`6.08–6.09`). Esta é a stack de ingress nativa da AWS e **substitui o antigo NGINX Controller + NLB + Kong**. O ingress local é ensinado **apenas no Módulo 5** (`5.18`, Traefik v3 no Kind, onde o ALB não existe) — a narrativa é "aprender o recurso `Ingress` localmente → trocar só o `ingressClassName` e as anotações na AWS"
- Conta Cloudflare + nameservers (`6.10–6.11`)
- **DNS (record do ALB) + ACM + HTTPS juntos**, separados em Console → OpenTofu: o fluxo TLS completo — record DNS-para-ALB + request/validação do ACM + HTTPS no ALB Ingress (cert aplicado via annotation) — feito no **Console** (`6.12`, Cloudflare + AWS) e recriado como **OpenTofu** (`6.13`). Fundido em uma aula cada porque a validação DNS do ACM, o record DNS do ALB e o binding do cert ao Ingress são um único fluxo contínuo (a validação e o record do ALB são ambos criados no Cloudflare). O **conceito de ACM é ensinado inline** no início de `6.12` (sem aula de conceito standalone — mesmo padrão inline de M11.03), já que TLS gerenciado/validação DNS é apenas alguns minutos de teoria
- Rancher (UI opcional de gestão de cluster)

**Sem API Gateway:** o curso usa intencionalmente **ALB Ingress puro (roteamento + TLS apenas)**, não um API gateway. O Kong foi totalmente removido. Auth/rate-limiting NÃO são tratados na borda — ficam no nível da aplicação (Spring). Se esses recursos de borda forem necessários um dia, o caminho ALB-native é AWS WAF + OIDC/Cognito (fora do escopo).

### Módulo 7 — Ferramentas de Deploy e GitOps

16 aulas. **O Kustomize foi totalmente removido** (as 3 aulas de Kustomize foram excluídas) — o Helm cobre tanto templating quanto config multi-ambiente via `values`, então o Kustomize era redundante. O módulo flui de desafios de gerenciamento de manifestos (`7.02`) → Helm (`7.03–7.08`) → GitOps/ArgoCD (`7.09–7.16`), terminando em `7.16 Continuous Deployment na prática com GitLab e ArgoCD`. (O antigo bloco de Kong/API-Gateway foi removido antes, quando a stack de ingress migrou para ALB — o roteamento agora vive no ALB Ingress, Módulo 6 / Módulo 9. O Argo Rollouts também foi removido.)

**Regra crítica:** `7.03–7.05` ensinam o Helm CLI conceitualmente (anatomia do chart, `helm install/upgrade/rollback`), mas **toda instalação Helm real do curso usa o Helm provider do OpenTofu, não o CLI** — ex.: AWS Load Balancer Controller (`6.08`), ArgoCD (`7.11 Instalando ArgoCD via Helm provider no OpenTofu`) e, no Módulo 11, o kube-prometheus-stack (`11.05`) e o operator ECK (`11.11`).

### Módulo 9 — Deploy do backend do AlgaDelivery (usa a infra preparada no Módulo 6)

Módulo enxuto (6 aulas). Foca puramente em USAR o que já foi ensinado/provisionado — NÃO reensina nem reprovisiona nada. Cortado de 10 → 6 aulas removendo tudo que já foi coberto em outro lugar:
- **ALB Ingress** — o mecanismo foi ensinado no Módulo 6 (`6.08–6.09`); aqui é apenas mais um manifesto templatizado **dentro do Helm Chart do AlgaDelivery** (`9.02`), NÃO uma aula standalone
- **Externalização de config (Parameter Store)** — totalmente ensinada no Módulo 8 (`8.03–8.08`, incl. Spring Cloud AWS); os paths específicos do AlgaDelivery são conectados nos `values` do Helm (`9.03`), NÃO reensinados
- **O ArgoCD NÃO é instalado aqui** — foi instalado uma vez no Módulo 7 (`7.11`, via Helm provider do OpenTofu no EKS). O Módulo 9 apenas registra a **Application** do AlgaDelivery (`9.04`). A antiga `9.07 Instalando ArgoCD` era uma duplicata literal de `7.11` e foi removida
- O que resta é genuinamente novo: Helm Charts da aplicação (`9.02`, incl. Ingress), values multi-ambiente (`9.03`), ArgoCD Application (`9.04`), a pipeline GitOps completa (`9.05`), teste em produção (`9.06` — a "Conclusão e próximos passos" do curso inteiro fica ao final do Módulo 11)

### Módulo 10 — Deploy do Frontend do AlgaDelivery (S3 + CloudFront)

Módulo dedicado ao frontend (6 aulas). A SPA é **estática** — hospedada em S3 + CloudFront, **não** no cluster. Vem depois do Módulo 9 porque consome a API já exposta pelo ALB Ingress (`9.02`). Abre com uma **introdução ao projeto** (`10.01`: rodar o frontend localmente, visão geral da pipeline, o que vai ser feito deploy) e um **deploy completo de S3 privado + CloudFront + OAC no Console** (`10.02`) — o setup seguro adequado, não descartável — para uma vitória rápida. O par **Console → OpenTofu** é simétrico: `10.02` faz no Console, `10.03` recria a mesma stack (S3 privado + CloudFront + OAC) como código em **uma aula** (conceitos de CDN/cache/OAC explicados inline — sem aula de conceitos standalone, já que `10.02` já demonstrou; e a bucket policy do OAC fecha na mesma aula porque o ARN da distribution existe lá). Contrasta deliberadamente com o deploy do backend: **CI push-based** (GitLab → `s3 sync` → invalidação do CloudFront), NÃO GitOps/ArgoCD. A stack fica neste módulo (não separada no Módulo 6) já que a config do CloudFront é fortemente acoplada à app frontend.
- `10.03` **Criando S3 privado + CloudFront com OAC com OpenTofu** — bucket privado (public-access-block + `BucketOwnerEnforced`) + distribution CloudFront + OAC + bucket policy, tudo em um
- **Cert ACM em `us-east-1`** (`10.04`, obrigatório para CloudFront independente da região da infra — contraste com o ACM regional para Ingress no Módulo 6) + domínio customizado via Cloudflare DNS
- `10.05` **Routing de SPA** (custom error responses 403/404 do CloudFront → `index.html`) + API base URL do frontend apontando para o ALB Ingress + CORS
- `10.06` Pipeline GitLab CI: build → `s3 sync` → invalidação do CloudFront; cache busting / versionamento de assets + teste em prod
- Termina em `10.06` (frontend no ar); a "Conclusão e próximos passos" do curso inteiro fecha o Módulo 11

### Módulo 11 — Observabilidade em Produção: Prometheus, Grafana e Elastic APM

Último módulo de ensino antes do apêndice (19 aulas). Observa o AlgaDelivery totalmente implantado (backend M10 + frontend M11) em produção. **Dois sistemas complementares, NÃO redundantes** — essa distinção precisa ser ensinada explicitamente:
- **Métricas (pull)**: o Prometheus faz scraping de cpu/mem/rede via container/cluster/host através de exporters + Alertmanager → dashboards no Grafana. Instalado via `kube-prometheus-stack` (Helm provider / OpenTofu).
- **APM (push)**: Elastic APM Java agent no serviço `invoice` → APM Server (processa, indexa, ILM) → Elasticsearch (armazenamento) → Kibana (UI de APM).

Decisões de design principais (do instrutor):
- O escopo é **métricas + APM apenas** (sem logging centralizado / Filebeat) — fiel ao diagrama de referência.
- Elastic Stack instalado via **operator ECK** (Elastic Cloud on Kubernetes) em `11.11` (conceito + CRDs + install fundidos), ele mesmo um `helm_release` no OpenTofu; ES/Kibana/APM Server gerenciados como CRDs.
- **Elasticsearch single-node** (simplicidade didática, lab mais barato) com PersistentVolume via EBS CSI Driver.

Callbacks fortes que fecham o arco Conceito→Implementação do curso:
- **Node group dedicado `node-tooling`** (EKS managed node group, Módulo 6) isolado com **taints/tolerations + node affinity**. ⚠️ **Atenção após a fusão M5+M6:** taints/tolerations e affinity **deixaram de ser ensinados** (eram o antigo `6.10–6.12`). Esta aula (`11.03`) passou a ser a **primeira** apresentação desses conceitos e precisa ensiná-los inline, não apenas fazer callback.
- O Elasticsearch é **stateful** → PersistentVolumes/StorageClass (`5.14`, Módulo 5) + EBS CSI Driver (Módulo 6).
- Todas as instalações via **Helm provider no OpenTofu** (regra do curso a partir de `7.11`).
- O Java agent é conectado nos values do Helm Chart do `invoice` (Módulo 9).
- Fecha com `11.19 Conclusão e próximos passos` (todo o sistema observado em produção).

## Trabalhando com este repositório

### Ao criar um novo diretório de aula

1. Confirme o número e o título da aula contra `.EMENTA-NIVEL-11-AWS.md`
2. Copie a estrutura da aula adjacente anterior (as aulas são snapshots incrementais)
3. Aplique APENAS o diff descrito pelo título da aula — não refatore arquivos não relacionados
4. Use a convenção de nomenclatura `<MM>.<NN>-<titulo-em-kebab-case>/`

### Ao trabalhar com código dos Módulos 5+ (planejados)

- O Módulo 5 já tem diretórios de `05.04` a `05.11`; os demais ainda não existem. Se for pedido para fazer o scaffold, siga o padrão existente (snapshots incrementais, um dir por aula).
- Os **roteiros de aula** do Módulo 5 ficam em `roteiros/modulo-05/roteiro-<M>.<NN>.md` — leia os vizinhos antes de escrever um novo, o estilo é padronizado.
- Para releases Helm nos Módulos 6/7/9, use por padrão recursos `helm_release` via OpenTofu, NÃO comandos shell `helm install`.

### Ao atualizar a ementa

O arquivo da ementa (`.EMENTA-NIVEL-11-AWS.md`) rastreia os badges de status dos módulos:
- 🎥 _em gravação_ — Módulo 5 atualmente
- ✅ _finalizado_ — Módulos 1, 2, 3, 4
- 📋 _ementa planejada_ — Módulos 6–11
- 🔜 _a planejar_ — Módulo 12 (Apêndice; ementa ainda não definida)

Renumerar módulos é frágil porque os números de aula `M.NN` se repetem entre módulos. Edite os módulos em ordem reversa (do maior para o menor) para evitar colisões.

## Regras do .gitignore

Os itens a seguir são excluídos e nunca devem ser commitados:
- `.terraform/`, `.terraform.lock.hcl`
- `*.tfstate`, `*.tfstate.backup`
- `*.tfvars`, `*.tfvars.json` (use `*.example.tfvars` para templates)
- `*.pem` (chaves privadas SSH geradas)
- `*.tfplan`, `*.plan`
