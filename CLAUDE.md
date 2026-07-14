# CLAUDE.md

Este arquivo orienta o Claude Code (claude.ai/code) ao trabalhar com o código deste repositório.

## Visão geral

Repositório do curso **"Microsserviços em Produção na AWS — Nível 11"** (Algaworks). O projeto prático é o **AlgaDelivery**, uma plataforma de microsserviços implantada na AWS. Cada diretório numerado (ex.: `03.07-trabalhando-com-variaveis/`) é um snapshot autocontido de uma aula específica, construído incrementalmente sobre o anterior. Toda a infraestrutura é escrita em **OpenTofu** (arquivos `.tofu`).

A ementa completa do curso está em `EMENTA-NIVEL-11-AWS.md` — trate-a como a fonte da verdade para a estrutura de módulos/aulas.

## Estrutura do curso (13 módulos)

| # | Módulo | Status | Aulas |
|---|---|---|---|
| 1 | Fundamentos de Cloud Computing | ✅ finalizado | 13 |
| 2 | Fundamentos de Redes na AWS | ✅ finalizado | 19 |
| 3 | Infraestrutura como Código (IaC) na AWS | ✅ finalizado | 21 |
| 4 | IaC Modular e Serviços Gerenciados na AWS | ✅ finalizado | 21 |
| 5 | Kubernetes: Fundamentos e Workloads | 🎥 em gravação | 17 |
| 6 | Kubernetes: Rede, Escala e Segurança | 📋 ementa planejada | 17 |
| 7 | Provisionando o cluster EKS na AWS com OpenTofu | 📋 ementa planejada | 14 |
| 8 | Ferramentas de Deploy e GitOps | 📋 ementa planejada | 16 |
| 9 | Configuração Externalizada e Gestão de Segredos | 📋 ementa planejada | 10 |
| 10 | Deploy do Backend do AlgaDelivery na AWS | 📋 ementa planejada | 6 |
| 11 | Deploy do Frontend do AlgaDelivery (S3 + CloudFront) | 📋 ementa planejada | 6 |
| 12 | Observabilidade em Produção: Prometheus, Grafana e Elastic APM | 📋 ementa planejada | 19 |
| 13 | Apêndice — Deploy Algashop | 🔜 a planejar | — |

**Arco narrativo:** Fundamentos de cloud → Redes na AWS → Fundamentos de IaC → IaC modular + serviços gerenciados → Kubernetes fundamentos + workloads (local com Kind) → Kubernetes rede, escala, scheduling e segurança → **Provisionar EKS** (ALB Ingress + ACM) → Ferramentas de deploy (Helm/ArgoCD) → Configuração externalizada → Deploy do backend do AlgaDelivery em infra real → Deploy do frontend do AlgaDelivery (S3 + CloudFront) → Observá-lo em produção (Prometheus/Grafana + Elastic APM) → Apêndice.

## Princípios pedagógicos

Estes princípios emergiram do design do curso e devem guiar sugestões de conteúdo/código:

1. **Conceito → Implementação** — conceitos abstratos são ensinados imediatamente antes de sua implementação concreta, não separadamente:
   - O Módulo 5 abre com VMs/Containers/CNCF/Service Instance Patterns como *construção argumentativa* para "por isso o K8s existe"
   - **Failover** (conceito) é ensinado logo antes do **ReplicaSet** (5.11, o primitivo do K8s que o implementa — mesmo módulo)
   - **Self Healing** (conceito) é ensinado logo antes das **Probes** (6.06, o mecanismo que o implementa — Módulo 6)
2. **Console primeiro, depois IaC** — muitos serviços AWS são introduzidos manualmente pelo Console e depois reprovisionados com OpenTofu. Padrão visível no Módulo 4 (RDS, ElastiCache, MSK) e que continua no Módulo 7 (EKS, node groups, add-ons, EBS CSI, ACM).
3. **Sem mini-labs** no Módulo 1 — apenas o desafio final (`1.13. Desafio: desenhar arquitetura on-premise vs cloud`). Todo o hands-on começa a partir do Módulo 2.
4. **O Módulo 4 tem dois desafios** — `4.16 Desafio: Criando Módulo ElastiCache` e `4.19 Desafio: Modulo MSK`. Padrão: primeiro entregar o serviço hands-on, depois desafiar o aluno a modularizá-lo.
5. **Conceitos de cloud ficam no Módulo 1; primitivos de computação vão para o Módulo 5** — VMs, Containers, CNCF e Service Instance Patterns foram movidos para a abertura do Módulo 5 para ficarem contíguos ao K8s. O Módulo 1 agora é exclusivamente "o que é cloud + propriedades operacionais + quando migrar".
6. **Fundamentos de rede ficam no Módulo 2, não duplicados no Módulo 1** — os itens 2.05 (fundamentos de VPC) e 2.06 (CIDR) cobrem IP/DNS/subnets, então o Módulo 1 omite esses tópicos.

### Lacunas conhecidas (intencionais, não omissões)

- **IAM** — introduzido pontualmente nos Módulos 6/7/9 apenas onde é necessário (RBAC do K8s, setup do EKS, segredos); sem módulo dedicado.
- **FinOps** — apenas `1.07 Modelos de precificação`; sem cobertura mais profunda planejada.

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
| `hashicorp/kubernetes` | Módulo 7 (setup do EKS) | Recursos Kubernetes (namespaces, service accounts, etc.) |
| `hashicorp/helm` | Módulo 7 (AWS Load Balancer Controller em `7.08`) → toda instalação Helm real do curso usa o provider | Todos os releases Helm provisionados via IaC, não via Helm CLI |

## Módulos planejados (ainda não registrados)

### Módulo 5 — Kubernetes: Fundamentos e Workloads (local com Kind)

Primeira metade do conteúdo de K8s (17 aulas). Abre com uma **ponte conceitual** (primitivos de computação → padrões de orquestração → K8s), depois cobre o setup do cluster e todos os workloads — o aluno consegue *fazer deploy* de uma aplicação ao final.
- `5.01` Containers vs VMs: o problema que o K8s resolve (as 3 antigas aulas de introdução — VMs, Containers, Containers-vs-VMs — fundidas em uma, já que Docker é pré-requisito do curso)
- `5.02` Cloud Native + ecossistema CNCF
- `5.03–5.05` Service Instance Patterns (Host [single + multiple] → VM → Container) — argumenta a *necessidade* do K8s
- `5.06` Arquitetura do K8s, Control Plane, Worker Nodes
- `5.07` Setup: kubectl + Kind + cluster local
- `5.08` Pods: ciclo de vida, criação e multi-container (multi-container fundido aqui)
- `5.09` Sidecar Pattern na prática — o padrão nomeado construído sobre o mecanismo multi-container de `5.08` (movido do Módulo 6 para cá, mantendo mecanismo → padrão contíguos)
- `5.10` Failover (conceito) → `5.11` ReplicaSet (implementação) — pareamento mantido intacto
- `5.12` Deployments e estratégias de deployment (Deployments + Rolling/Recreate/Rollback fundidos em um)
- `5.13` ConfigMaps e Secrets (movido para perto de Deployments para o workload ser parametrizado antes da exposição)
- `5.14–5.16` StatefulSets, DaemonSets, Jobs/CronJobs
- `5.17` Aplicando Service Instance per Container com Pods — fecha o arco dos Service Instance Patterns

### Módulo 6 — Kubernetes: Rede, Escala e Segurança (local com Kind)

Segunda metade do conteúdo de K8s (17 aulas). Cobre como *operar, expor, escalar e proteger* os workloads do Módulo 5.
- `6.01` Namespaces, Labels, Selectors e Annotations (Namespaces fundido com Labels/Selectors) — abre o módulo direto nas preocupações de Services; a antiga `6.01 Service Deployment Platform` foi removida por ser redundante com `5.12` Deployments, e o Sidecar foi para `5.09`
- `6.02` Services: ClusterIP e NodePort (ClusterIP + NodePort fundidos)
- `6.03–6.05` Headless Services (callback aos StatefulSets em 5.14), Service Discovery/DNS, Ingress (NGINX)
- `6.06` Self Healing com Readiness, Liveness e Startup Probes (fundidos) — pareamento conceito de Self Healing → Probes
- `6.07–6.08` Resource Requests/Limits/QoS, Metrics Server + HPA
- `6.09` **Grafana k6** — a própria ferramenta de teste de carga: instalação, scripts, VUs, stages, thresholds e leitura de métricas (separada da antiga aula única de K6 para a ferramenta ser ensinada antes de aplicada)
- `6.10` **Load testing com k6** — aplica o k6 para validar empiricamente o autoscaling do HPA, self-healing e disponibilidade (apenas autoscaling em nível de pod; autoscaling em nível de node via Cluster Autoscaler/Karpenter foi intencionalmente cortado do Módulo 7)
- `6.11–6.13` Node Selector, Affinity e Anti-Affinity (node + pod fundidos em uma aula), Taints e Tolerations
- `6.14` Persistent Volumes, PVC, Storage Classes
- `6.15` Service Accounts e RBAC (identidade + autorização fundidos)
- `6.16` Lens: GUI/IDE desktop para gestão de cluster (Port-forward e Proxy NÃO são ensinados aqui — aparecem indiretamente em outra aula)
- `6.17` Rancher: plataforma web de gestão de cluster (separada do Lens — escopo diferente: UI de gestão multi-cluster vs. GUI desktop local)

### Módulo 7 — Provisionando o cluster EKS na AWS com OpenTofu

Provisionamento de infra puro antes de qualquer deploy de aplicação. Cada componente gerenciado segue o padrão **Console → OpenTofu**:
- Cluster EKS, node groups, add-ons + EBS CSI Driver
- **Sem node autoscaling** — Cluster Autoscaler / Karpenter foram intencionalmente cortados. O curso ensina autoscaling em nível de pod (HPA) no Módulo 6 mas NÃO cobre autoscaling em nível de node no cluster EKS
- **Core EKS add-ons** (VPC CNI, CoreDNS, kube-proxy) já vêm pré-instalados com o cluster (self-managed quando criados via API/OpenTofu, EKS-managed quando criados via Console). As duas aulas de add-ons (`7.06` Console, `7.07` OpenTofu) cobrem promovê-los para EKS-managed add-ons **e ensinam o EBS CSI Driver nas mesmas aulas** — o CSI Driver é ele mesmo um EKS add-on (NÃO default; necessário para o PersistentVolume do Elasticsearch stateful no Módulo 12), então é incorporado em vez de ganhar seu próprio par Console/OpenTofu
- **AWS Load Balancer Controller** (instalado via Helm provider, precisa de IRSA/IAM) → **ALB Ingress** para roteamento L7 por path/host (`7.08–7.09`). Esta é a stack de ingress nativa da AWS e **substitui o antigo NGINX Controller + NLB + Kong**. O NGINX Ingress agora é ensinado **apenas no Módulo 6** (Kind local, onde o ALB não existe) — a narrativa é "aprender NGINX Ingress localmente → usar ALB Ingress na AWS"
- Conta Cloudflare + nameservers (`7.10–7.11`)
- **DNS (record do ALB) + ACM + HTTPS juntos**, separados em Console → OpenTofu: o fluxo TLS completo — record DNS-para-ALB + request/validação do ACM + HTTPS no ALB Ingress (cert aplicado via annotation) — feito no **Console** (`7.12`, Cloudflare + AWS) e recriado como **OpenTofu** (`7.13`). Fundido em uma aula cada porque a validação DNS do ACM, o record DNS do ALB e o binding do cert ao Ingress são um único fluxo contínuo (a validação e o record do ALB são ambos criados no Cloudflare). O **conceito de ACM é ensinado inline** no início de `7.12` (sem aula de conceito standalone — mesmo padrão inline de M11.03), já que TLS gerenciado/validação DNS é apenas alguns minutos de teoria
- Rancher (UI opcional de gestão de cluster)

**Sem API Gateway:** o curso usa intencionalmente **ALB Ingress puro (roteamento + TLS apenas)**, não um API gateway. O Kong foi totalmente removido. Auth/rate-limiting NÃO são tratados na borda — ficam no nível da aplicação (Spring). Se esses recursos de borda forem necessários um dia, o caminho ALB-native é AWS WAF + OIDC/Cognito (fora do escopo).

### Módulo 8 — Ferramentas de Deploy e GitOps

16 aulas. **O Kustomize foi totalmente removido** (as 3 aulas de Kustomize foram excluídas) — o Helm cobre tanto templating quanto config multi-ambiente via `values`, então o Kustomize era redundante. O módulo flui de desafios de gerenciamento de manifestos (`8.02`) → Helm (`8.03–8.08`) → GitOps/ArgoCD (`8.09–8.16`), terminando em `8.16 Continuous Deployment na prática com GitLab e ArgoCD`. (O antigo bloco de Kong/API-Gateway foi removido antes, quando a stack de ingress migrou para ALB — o roteamento agora vive no ALB Ingress, Módulo 7 / Módulo 10. O Argo Rollouts também foi removido.)

**Regra crítica:** `8.03–8.05` ensinam o Helm CLI conceitualmente (anatomia do chart, `helm install/upgrade/rollback`), mas **toda instalação Helm real do curso usa o Helm provider do OpenTofu, não o CLI** — ex.: AWS Load Balancer Controller (`7.08`), ArgoCD (`8.11 Instalando ArgoCD via Helm provider no OpenTofu`) e, no Módulo 12, o kube-prometheus-stack (`12.05`) e o operator ECK (`12.11`).

### Módulo 10 — Deploy do backend do AlgaDelivery (usa a infra preparada no Módulo 7)

Módulo enxuto (6 aulas). Foca puramente em USAR o que já foi ensinado/provisionado — NÃO reensina nem reprovisiona nada. Cortado de 10 → 6 aulas removendo tudo que já foi coberto em outro lugar:
- **ALB Ingress** — o mecanismo foi ensinado no Módulo 7 (`7.08–7.09`); aqui é apenas mais um manifesto templatizado **dentro do Helm Chart do AlgaDelivery** (`10.02`), NÃO uma aula standalone
- **Externalização de config (Parameter Store)** — totalmente ensinada no Módulo 9 (`9.03–9.08`, incl. Spring Cloud AWS); os paths específicos do AlgaDelivery são conectados nos `values` do Helm (`10.03`), NÃO reensinados
- **O ArgoCD NÃO é instalado aqui** — foi instalado uma vez no Módulo 8 (`8.11`, via Helm provider do OpenTofu no EKS). O Módulo 10 apenas registra a **Application** do AlgaDelivery (`10.04`). A antiga `10.07 Instalando ArgoCD` era uma duplicata literal de `8.11` e foi removida
- O que resta é genuinamente novo: Helm Charts da aplicação (`10.02`, incl. Ingress), values multi-ambiente (`10.03`), ArgoCD Application (`10.04`), a pipeline GitOps completa (`10.05`), teste em produção (`10.06` — a "Conclusão e próximos passos" do curso inteiro fica ao final do Módulo 12)

### Módulo 11 — Deploy do Frontend do AlgaDelivery (S3 + CloudFront)

Módulo dedicado ao frontend (6 aulas). A SPA é **estática** — hospedada em S3 + CloudFront, **não** no cluster. Vem depois do Módulo 10 porque consome a API já exposta pelo ALB Ingress (`10.02`). Abre com uma **introdução ao projeto** (`11.01`: rodar o frontend localmente, visão geral da pipeline, o que vai ser feito deploy) e um **deploy completo de S3 privado + CloudFront + OAC no Console** (`11.02`) — o setup seguro adequado, não descartável — para uma vitória rápida. O par **Console → OpenTofu** é simétrico: `11.02` faz no Console, `11.03` recria a mesma stack (S3 privado + CloudFront + OAC) como código em **uma aula** (conceitos de CDN/cache/OAC explicados inline — sem aula de conceitos standalone, já que `11.02` já demonstrou; e a bucket policy do OAC fecha na mesma aula porque o ARN da distribution existe lá). Contrasta deliberadamente com o deploy do backend: **CI push-based** (GitLab → `s3 sync` → invalidação do CloudFront), NÃO GitOps/ArgoCD. A stack fica neste módulo (não separada no Módulo 7) já que a config do CloudFront é fortemente acoplada à app frontend.
- `11.03` **Criando S3 privado + CloudFront com OAC com OpenTofu** — bucket privado (public-access-block + `BucketOwnerEnforced`) + distribution CloudFront + OAC + bucket policy, tudo em um
- **Cert ACM em `us-east-1`** (`11.04`, obrigatório para CloudFront independente da região da infra — contraste com o ACM regional para Ingress no Módulo 7) + domínio customizado via Cloudflare DNS
- `11.05` **Routing de SPA** (custom error responses 403/404 do CloudFront → `index.html`) + API base URL do frontend apontando para o ALB Ingress + CORS
- `11.06` Pipeline GitLab CI: build → `s3 sync` → invalidação do CloudFront; cache busting / versionamento de assets + teste em prod
- Termina em `11.06` (frontend no ar); a "Conclusão e próximos passos" do curso inteiro fecha o Módulo 12

### Módulo 12 — Observabilidade em Produção: Prometheus, Grafana e Elastic APM

Último módulo de ensino antes do apêndice (20 aulas). Observa o AlgaDelivery totalmente implantado (backend M10 + frontend M11) em produção. **Dois sistemas complementares, NÃO redundantes** — essa distinção precisa ser ensinada explicitamente:
- **Métricas (pull)**: o Prometheus faz scraping de cpu/mem/rede via container/cluster/host através de exporters + Alertmanager → dashboards no Grafana. Instalado via `kube-prometheus-stack` (Helm provider / OpenTofu).
- **APM (push)**: Elastic APM Java agent no serviço `invoice` → APM Server (processa, indexa, ILM) → Elasticsearch (armazenamento) → Kibana (UI de APM).

Decisões de design principais (do instrutor):
- O escopo é **métricas + APM apenas** (sem logging centralizado / Filebeat) — fiel ao diagrama de referência.
- Elastic Stack instalado via **operator ECK** (Elastic Cloud on Kubernetes) em `12.11` (conceito + CRDs + install fundidos), ele mesmo um `helm_release` no OpenTofu; ES/Kibana/APM Server gerenciados como CRDs.
- **Elasticsearch single-node** (simplicidade didática, lab mais barato) com PersistentVolume via EBS CSI Driver.

Callbacks fortes que fecham o arco Conceito→Implementação do curso:
- **Node group dedicado `node-tooling`** (EKS managed node group, Módulo 7) isolado com **taints/tolerations + node affinity** (Módulo 6) — os alunos aplicaram isso abstratamente no M6/M7; aqui protege a stack de observabilidade.
- O Elasticsearch é **stateful** → PersistentVolumes/StorageClass (Módulo 6) + EBS CSI Driver (Módulo 7).
- Todas as instalações via **Helm provider no OpenTofu** (regra do curso a partir de `8.11`).
- O Java agent é conectado nos values do Helm Chart do `invoice` (Módulo 10).
- Fecha com `12.19 Conclusão e próximos passos` (todo o sistema observado em produção).

## Trabalhando com este repositório

### Ao criar um novo diretório de aula

1. Confirme o número e o título da aula contra `EMENTA-NIVEL-11-AWS.md`
2. Copie a estrutura da aula adjacente anterior (as aulas são snapshots incrementais)
3. Aplique APENAS o diff descrito pelo título da aula — não refatore arquivos não relacionados
4. Use a convenção de nomenclatura `<MM>.<NN>-<titulo-em-kebab-case>/`

### Ao trabalhar com código dos Módulos 5+ (planejados)

- Esses diretórios ainda não existem. Se for pedido para fazer o scaffold, siga o padrão existente (snapshots incrementais, um dir por aula).
- Para releases Helm nos Módulos 7/8/10, use por padrão recursos `helm_release` via OpenTofu, NÃO comandos shell `helm install`.

### Ao atualizar a ementa

O arquivo da ementa (`EMENTA-NIVEL-11-AWS.md`) rastreia os badges de status dos módulos:
- 🎥 _em gravação_ — Módulo 5 atualmente
- ✅ _finalizado_ — Módulos 1, 2, 3, 4
- 📋 _ementa planejada_ — Módulos 6–12
- 🔜 _a planejar_ — Módulo 13 (ementa ainda não definida)

Renumerar módulos é frágil porque os números de aula `M.NN` se repetem entre módulos. Edite os módulos em ordem reversa (do maior para o menor) para evitar colisões.

## Regras do .gitignore

Os itens a seguir são excluídos e nunca devem ser commitados:
- `.terraform/`, `.terraform.lock.hcl`
- `*.tfstate`, `*.tfstate.backup`
- `*.tfvars`, `*.tfvars.json` (use `*.example.tfvars` para templates)
- `*.pem` (chaves privadas SSH geradas)
- `*.tfplan`, `*.plan`
