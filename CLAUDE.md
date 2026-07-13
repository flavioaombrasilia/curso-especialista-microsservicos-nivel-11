# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Course repository for **"Microsserviços em Produção na AWS — Nível 11"** (Algaworks). The practical project is **AlgaDelivery**, a microservices platform deployed on AWS. Each numbered directory (e.g. `03.07-trabalhando-com-variaveis/`) is a self-contained snapshot for a specific lesson, incrementally building on the previous one. All infrastructure is written in **OpenTofu** (`.tofu` files).

The full course outline lives in `EMENTA-NIVEL-11-AWS.md` — treat that as the source of truth for module/lesson structure.

## Course Structure (13 modules)

| # | Module | Status | Aulas |
|---|---|---|---|
| 1 | Fundamentos de Cloud Computing | 🎥 em gravação | 13 |
| 2 | Fundamentos de Redes na AWS | ✅ finalizado | 19 |
| 3 | Infraestrutura como Código (IaC) na AWS | ✅ finalizado | 21 |
| 4 | IaC Modular e Serviços Gerenciados na AWS | ✅ finalizado | 21 |
| 5 | Kubernetes: Fundamentos e Workloads | 📋 ementa planejada | 17 |
| 6 | Kubernetes: Rede, Escala e Segurança | 📋 ementa planejada | 17 |
| 7 | Provisionando o cluster EKS na AWS com OpenTofu | 📋 ementa planejada | 14 |
| 8 | Ferramentas de Deploy e GitOps | 📋 ementa planejada | 16 |
| 9 | Configuração Externalizada e Gestão de Segredos | 📋 ementa planejada | 10 |
| 10 | Deploy do Backend do AlgaDelivery na AWS | 📋 ementa planejada | 6 |
| 11 | Deploy do Frontend do AlgaDelivery (S3 + CloudFront) | 📋 ementa planejada | 6 |
| 12 | Observabilidade em Produção: Prometheus, Grafana e Elastic APM | 📋 ementa planejada | 19 |
| 13 | Apêndice — Deploy Algashop | 📋 ementa planejada | — |

**Narrative arc:** Cloud foundations → AWS networking → IaC fundamentals → Modular IaC + managed services → Kubernetes fundamentals + workloads (local with Kind) → Kubernetes networking, scaling, scheduling & security → **Provision EKS** (ALB Ingress + ACM) → Deploy tooling (Helm/ArgoCD) → External config → Deploy AlgaDelivery backend on real infra → Deploy AlgaDelivery frontend (S3 + CloudFront) → Observe it in production (Prometheus/Grafana + Elastic APM) → Appendix.

## Pedagogical Principles

These principles emerged from the course design and should guide content/code suggestions:

1. **Conceito → Implementação** — abstract concepts are taught immediately before their concrete implementation, not separately:
   - Module 5 opens with VMs/Containers/CNCF/Service Instance Patterns as the *argumentative buildup* to "por isso K8s exists"
   - **Failover** (concept) is taught right before **ReplicaSet** (5.11, the K8s primitive that implements it — same module)
   - **Self Healing** (concept) is taught right before **Probes** (6.06, the mechanism that implements it — Module 6)
2. **Console first, then IaC** — many AWS services are introduced manually via Console, then re-provisioned with OpenTofu. Pattern visible in Module 4 (RDS, ElastiCache, MSK), continues in Module 7 (EKS, node groups, add-ons, EBS CSI, ACM).
3. **No mini-labs** in Module 1 — only the final challenge (`1.13. Desafio: desenhar arquitetura on-premise vs cloud`). All hands-on starts from Module 2.
4. **Module 4 has two challenges** — `4.16 Desafio: Criando Módulo ElastiCache` and `4.19 Desafio: Modulo MSK`. Pattern: provide the service hands-on first, then challenge the student to modularize it.
5. **Cloud concepts stay in Module 1; compute primitives go to Module 5** — VMs, Containers, CNCF, and Service Instance Patterns moved to Module 5 opening so they're contiguous with K8s. Module 1 is now exclusively "what is cloud + operational properties + when to migrate."
6. **Network basics stay in Module 2, not duplicated in Module 1** — Module 2.05 (VPC fundamentals) and Module 2.06 (CIDR) cover IP/DNS/subnets, so Module 1 omits them.

### Known gaps (intentional, not omissions)

- **IAM** — introduced pointwise in Modules 6/7/9 only where needed (K8s RBAC, EKS setup, secrets); no dedicated module.
- **FinOps** — only `1.07 Modelos de precificação`; no deeper coverage planned.

## OpenTofu Commands

All commands use `tofu`, not `terraform`.

```bash
tofu init       # initialize providers and modules
tofu validate   # validate configuration syntax
tofu fmt -recursive  # format all .tofu files
tofu plan       # preview changes
tofu apply      # apply changes
tofu destroy    # destroy all resources
```

For lessons with the multi-environment structure (`envs/dev`, `envs/prod`), run commands from inside the environment directory:

```bash
cd <lesson>/envs/dev
tofu init
tofu plan
tofu apply
```

## Architecture

### Lesson Progression

- **Module 3 (03.XX) — Infraestrutura como Código (IaC) na AWS:** OpenTofu fundamentals + provisioning a complete network from scratch. Flat structure, single directory with `main.tofu`, `variables.tofu`, `outputs.tofu`, `providers.tofu`. Builds up to: VPC + public/private subnets + IGW + NAT Gateway + route tables + Security Groups (Bastion + Private) + EC2 (public + private), all via OpenTofu. Ends with connectivity tests and destroy.
- **Module 4 (04.XX) — IaC Modular e Serviços Gerenciados na AWS:** Modularization (dynamic blocks, for expressions, lifecycle) and managed services. Introduces `modules/vpc`, `modules/ec2`, `modules/rds`, `modules/elasticache`, `modules/msk`. Multi-env structure (`envs/dev`, `envs/prod`) from `04.07-explorando-multi-env` onward.

### Lesson Directory Naming

Directories follow `<MM>.<NN>-<kebab-case-title>/`:
- `03.07-trabalhando-com-variaveis/`
- `04.13-configurando-backup-e-manutencao-no-rds/`

Numbering follows the ementa. Occasional `*-final/` variants indicate the "after fixing" state of an aula (e.g. `04.13-configurando-backup-e-manutencao-no-rds-final/`).

### Module 4 Final Structure (from `04.07-explorando-multi-env` onward)

```
<lesson>/
├── modules/
│   ├── vpc/          # VPC, subnets (public/private), IGW, NAT Gateway, route tables
│   ├── ec2/          # Key pair (TLS generated), EIP, EC2 instance, security group
│   ├── rds/          # RDS PostgreSQL: parameter groups, backup, maintenance window, secure password
│   ├── elasticache/  # ElastiCache Valkey
│   └── msk/          # Amazon MSK (Kafka)
├── envs/
│   ├── dev/          # Root module for development environment
│   └── prod/         # Root module for production environment
```

Each `envs/<env>/` directory is an independent root module. Composition files grow as modules are added: `main.tofu`, `vpc.tofu`, `ec2.tofu`, `rds.tofu`, `elasticache.tofu`, `msk.tofu`, `data.tofu`, `local.tofu`, `variables.tofu`, `outputs.tofu`, `providers.tofu`.

### Module Data Flow

```
module.vpc.vpc_id             → module.ec2, module.rds, module.elasticache, module.msk (placement, SG)
module.vpc.public_subnet_ids  → module.ec2 (bastion host placement)
module.vpc.private_subnet_ids → module.rds, module.elasticache, module.msk (private placement)
module.vpc.vpc_cidr           → module.ec2, module.rds, module.elasticache, module.msk (SG CIDR rules)
```

### Remote State

State is stored in S3. The backend is configured in `providers.tofu`:
- Bucket: `algadelivery-tfstate`
- Key: `algadelivery/${var.environment}/remote.tfstate`

### Key Design Patterns

- **`common_tags`** local propagates `Environment` and `ManagedBy = "OpenTofu"` to all resources via `merge(var.common_tags, { Name = "..." })`
- **Dynamic blocks** for security group ingress/egress rules — rules are passed as `list(object({...}))` variables (introduced `04.02-utilizando-dynamic-blocks`)
- **For expressions and type constraints** introduced in `04.04-tipos-de-coleção-e-for-expressions`
- **`ngw_regional_mode`**: boolean variable translated to `"regional"` or `"zonal"` in locals, controlling NAT Gateway availability mode
- **`lifecycle { prevent_destroy = true; ignore_changes = [ami] }`** on EC2 instances starting from `04.08-utilizando-o-lifecycle`
- **SSH key pairs** are generated at apply time via `tls_private_key` + `aws_key_pair` + `local_sensitive_file` (`.pem` written to `path.root`, gitignored)
- **My public IP** is fetched dynamically via `data.http.my_public_ip` and used in bastion SSH rules
- **RDS secure passwords**: managed via `random_password` (introduced in `04.11-gerenciando-senhas-com-segurança-no-rds`) — never committed; sensitive outputs
- **RDS backup & maintenance window**: configured via dedicated module variables from `04.13-configurando-backup-e-manutencao-no-rds` onward

### Providers (Module 4 — current)

| Provider | Version | Use |
|---|---|---|
| `hashicorp/aws` | `~> 6.0` | All AWS resources (VPC, EC2, RDS, ElastiCache, MSK, etc.) |
| `hashicorp/http` | `~> 3.0` | Fetch caller's public IP |
| `hashicorp/tls` | `~> 4.0` | Generate RSA key pairs |
| `hashicorp/local` | `~> 2.0` | Write `.pem` files to disk |
| `hashicorp/random` | `~> 3.0` | Generate secure RDS passwords |

### Planned Providers (Modules 5+)

| Provider | When introduced | Use |
|---|---|---|
| `hashicorp/kubernetes` | Module 7 (EKS setup) | Kubernetes resources (namespaces, service accounts, etc.) |
| `hashicorp/helm` | Module 7 (AWS Load Balancer Controller at `7.08`) → every actual Helm install in the course uses the provider | All Helm releases provisioned via IaC, not Helm CLI |

## Planned Modules (not yet recorded)

### Module 5 — Kubernetes: Fundamentos e Workloads (local with Kind)

First half of the K8s content (17 aulas). Opens with a **conceptual bridge** (compute primitives → orchestration patterns → K8s), then covers the cluster setup and all workloads — the student can *deploy* an application by the end.
- `5.01` Containers vs VMs: o problema que o K8s resolve (the 3 old intro aulas — VMs, Containers, Containers-vs-VMs — merged into one, since Docker is a course prerequisite)
- `5.02` Cloud Native + ecossistema CNCF
- `5.03–5.05` Service Instance Patterns (Host [single + multiple] → VM → Container) — argues the *need* for K8s
- `5.06` K8s architecture, Control Plane, Worker Nodes
- `5.07` Setup: kubectl + Kind + cluster local
- `5.08` Pods: ciclo de vida, criação e multi-container (multi-container merged in)
- `5.09` Sidecar Pattern na prática — the named pattern built on the multi-container mechanism from `5.08` (moved here from Module 6, so mechanism → pattern stays contiguous)
- `5.10` Failover (concept) → `5.11` ReplicaSet (implementation) — pairing kept intact
- `5.12` Deployments e estratégias de deployment (Deployments + Rolling/Recreate/Rollback merged into one)
- `5.13` ConfigMaps e Secrets (moved next to Deployments so the workload is parameterized before exposure)
- `5.14–5.16` StatefulSets, DaemonSets, Jobs/CronJobs
- `5.17` Aplicando Service Instance per Container com Pods — closes the Service Instance Patterns arc

### Module 6 — Kubernetes: Rede, Escala e Segurança (local with Kind)

Second half of the K8s content (17 aulas). Covers how to *operate, expose, scale and protect* the workloads from Module 5.
- `6.01` Namespaces, Labels, Selectors e Annotations (Namespaces merged with Labels/Selectors) — opens the module directly on Services concerns; the old `6.01 Service Deployment Platform` was dropped as redundant with `5.12` Deployments, and Sidecar moved to `5.09`
- `6.02` Services: ClusterIP e NodePort (ClusterIP + NodePort merged)
- `6.03–6.05` Headless Services (callback to StatefulSets in 5.14), Service Discovery/DNS, Ingress (NGINX)
- `6.06` Self Healing com Readiness, Liveness e Startup Probes (merged) — Self Healing concept → Probes pairing
- `6.07–6.08` Resource Requests/Limits/QoS, Metrics Server + HPA
- `6.09` **Grafana k6** — the load-testing tool itself: install, scripts, VUs, stages, thresholds and reading metrics (split from the old single K6 aula so tooling is taught before it's applied)
- `6.10` **Load testing com k6** — applies k6 to empirically validate HPA autoscaling, self-healing and availability (pod-level autoscaling only; node-level autoscaling via Cluster Autoscaler/Karpenter was intentionally cut from Module 7)
- `6.11–6.13` Node Selector, Affinity e Anti-Affinity (node + pod merged into one aula), Taints e Tolerations
- `6.14` Persistent Volumes, PVC, Storage Classes
- `6.15` Service Accounts e RBAC (identity + authorization merged)
- `6.16` Lens: desktop GUI/IDE for cluster management (Port-forward and Proxy are NOT taught here — they're shown indirectly in another aula)
- `6.17` Rancher: web-based cluster management platform (split from Lens — different scope: multi-cluster management UI vs. local desktop GUI)

### Module 7 — Provisionando o cluster EKS na AWS com OpenTofu

Pure infrastructure provisioning before any application deploy. Each managed component follows the **Console → OpenTofu** pattern:
- EKS cluster, node groups, add-ons + EBS CSI Driver
- **No node autoscaling** — Cluster Autoscaler / Karpenter were intentionally cut. The course teaches pod-level autoscaling (HPA) in Module 6 but does NOT cover node-level autoscaling on the EKS cluster
- **Core EKS add-ons** (VPC CNI, CoreDNS, kube-proxy) come pre-installed with the cluster (self-managed when created via API/OpenTofu, EKS-managed when created via Console). The two add-ons lessons (`7.06` Console, `7.07` OpenTofu) cover promoting them to EKS-managed add-ons **and teach the EBS CSI Driver in the same lessons** — the CSI Driver is itself an EKS add-on (NOT a default; needed for the stateful Elasticsearch PersistentVolume in Module 12), so it's folded in rather than given its own Console/OpenTofu pair
- **AWS Load Balancer Controller** (install via Helm provider, needs IRSA/IAM) → **ALB Ingress** for L7 path/host routing (`7.08–7.09`). This is the native-AWS ingress stack — it **replaces the old NGINX Controller + NLB + Kong**. NGINX Ingress is now taught **only in Module 6** (local Kind, where ALB doesn't exist) — the narrative is "learn NGINX Ingress locally → use ALB Ingress on AWS"
- Cloudflare account + nameservers (`7.10–7.11`)
- **DNS (ALB record) + ACM + HTTPS together**, split Console → OpenTofu: the full TLS flow — DNS-to-ALB record + ACM request/validation + HTTPS on the ALB Ingress (cert applied via annotation) — done in the **Console** (`7.12`, Cloudflare + AWS) and re-created as **OpenTofu** (`7.13`). Merged into one lesson each because ACM DNS validation, the ALB DNS record and the cert-to-Ingress binding are one continuous workflow (validation and the ALB record are both created in Cloudflare). The **ACM concept is taught inline** at the start of `7.12` (no standalone concept aula — same inline pattern as M11.03), since managed-TLS/DNS-validation is only a few minutes of theory
- Rancher (optional cluster management UI)

**No API Gateway:** the course intentionally uses **plain ALB Ingress (routing + TLS only)**, not an API gateway. Kong was fully removed. Auth/rate-limiting are NOT handled at the edge — they stay at the application level (Spring). If those edge features are ever needed, the ALB-native path is AWS WAF + OIDC/Cognito (not in scope).

### Module 8 — Ferramentas de Deploy e GitOps

16 aulas. **Kustomize was dropped entirely** (the 3 Kustomize aulas removed) — Helm covers both templating and multi-env config via `values`, so Kustomize was redundant. The module flows manifest-management challenges (`8.02`) → Helm (`8.03–8.08`) → GitOps/ArgoCD (`8.09–8.16`), ending at `8.16 Continuous Deployment na prática com GitLab e ArgoCD`. (The old Kong/API-Gateway block was removed earlier when the ingress stack moved to ALB — routing now lives in the ALB Ingress, Module 7 / Module 10. Argo Rollouts was also dropped.)

**Critical rule:** `8.03–8.05` teach Helm CLI conceptually (chart anatomy, `helm install/upgrade/rollback`), but **every actual Helm install in the course uses the OpenTofu Helm provider, not the CLI** — e.g. AWS Load Balancer Controller (`7.08`), ArgoCD (`8.11 Instalando ArgoCD via Helm provider no OpenTofu`), and in Module 12 the kube-prometheus-stack (`12.05`) and the ECK operator (`12.11`).

### Module 10 — Deploy AlgaDelivery backend (uses the prepared infra from Module 7)

Slim module (6 aulas). Focuses purely on USING what was already taught/provisioned — does NOT re-teach or re-provision anything. Cut from 10 → 6 aulas by removing everything already covered elsewhere:
- **ALB Ingress** — the mechanism was taught in Module 7 (`7.08–7.09`); here it's just another templated manifest **inside the AlgaDelivery Helm Chart** (`10.02`), NOT a standalone lesson
- **Config externalization (Parameter Store)** — fully taught in Module 9 (`9.03–9.08`, incl. Spring Cloud AWS); the AlgaDelivery-specific paths are wired into the Helm `values` (`10.03`), NOT re-taught
- **ArgoCD is NOT installed here** — it was installed once in Module 8 (`8.11`, via OpenTofu Helm provider on EKS). Module 10 only registers the AlgaDelivery **Application** (`10.04`). The old `10.07 Instalando ArgoCD` was a literal duplicate of `8.11` and was removed
- What remains is genuinely new: Helm Charts of the app (`10.02`, incl. Ingress), multi-env values (`10.03`), ArgoCD Application (`10.04`), the full GitOps pipeline (`10.05`), production test (`10.06` — the course-wide "Conclusão e próximos passos" is at the end of Module 12)

### Module 11 — Deploy do Frontend do AlgaDelivery (S3 + CloudFront)

Dedicated frontend module (6 aulas). The SPA is **static** — hosted on S3 + CloudFront, **not** on the cluster. Comes after Module 10 because it consumes the API already exposed by the ALB Ingress (`10.03`). Opens with a **project intro** (`11.01`: run the frontend locally, pipeline overview, what gets deployed) and a full **private S3 + CloudFront + OAC Console deploy** (`11.02`) — the proper secure setup, not a throwaway — for an early win. The **Console → OpenTofu** pair is symmetric: `11.02` does it in the Console, `11.03` re-creates the exact same stack (private S3 + CloudFront + OAC) as code in **one lesson** (CDN/cache/OAC concepts explained inline — no standalone concepts aula, since `11.02` already demoed it; and the OAC bucket policy closes in the same lesson because the distribution ARN exists there). Deliberately contrasts with the backend deploy: **push-based CI** (GitLab → `s3 sync` → CloudFront invalidation), NOT GitOps/ArgoCD. The stack is kept in this module (not split into Module 7) since the CloudFront config is tightly coupled to the frontend app.
- `11.03` **Criando S3 privado + CloudFront com OAC com OpenTofu** — private bucket (public-access-block + `BucketOwnerEnforced`) + CloudFront distribution + OAC + bucket policy, all in one
- **ACM cert in `us-east-1`** (`11.04`, mandatory for CloudFront regardless of the infra's region — contrast with the regional ACM for Ingress in Module 7) + custom domain via Cloudflare DNS
- `11.05` **SPA routing** (CloudFront custom error responses 403/404 → `index.html`) + frontend API base URL pointing at the ALB Ingress + CORS
- `11.06` GitLab CI pipeline: build → `s3 sync` → CloudFront invalidation; cache busting / asset versioning + prod test
- Ends at `11.06` (frontend live); the course-wide "Conclusão e próximos passos" closes Module 12

### Module 12 — Observabilidade em Produção: Prometheus, Grafana e Elastic APM

Final teaching module before the appendix (20 aulas). Observes the fully-deployed AlgaDelivery (backend M10 + frontend M11) in production. **Two complementary systems, NOT redundant** — this distinction must be taught explicitly:
- **Metrics (pull)**: Prometheus scrapes cpu/mem/network from container/cluster/host via exporters + Alertmanager → Grafana dashboards. Installed via `kube-prometheus-stack` (Helm provider / OpenTofu).
- **APM (push)**: Elastic APM Java agent on the `invoice` service → APM Server (processes, indexes, ILM) → Elasticsearch (storage) → Kibana (APM UI).

Key design decisions (per instructor):
- Scope is **metrics + APM only** (no centralized logging / Filebeat) — faithful to the reference diagram.
- Elastic Stack installed via the **ECK operator** (Elastic Cloud on Kubernetes) at `12.11` (concept + CRDs + install merged), itself a `helm_release` in OpenTofu; ES/Kibana/APM Server managed as CRDs.
- **Elasticsearch single-node** (didactic simplicity, cheaper lab) with a PersistentVolume via the EBS CSI Driver.

Strong callbacks that close the course's Conceito→Implementação arc:
- **Dedicated `node-tooling` node group** (EKS managed node group, Module 7) isolated with **taints/tolerations + node affinity** (Module 6) — students applied these abstractly in M6/M7, here they protect the observability stack.
- Elasticsearch is **stateful** → PersistentVolumes/StorageClass (Module 6) + EBS CSI Driver (Module 7).
- All installs via the **Helm provider in OpenTofu** (course rule from `8.11`).
- The Java agent is wired into the `invoice` Helm Chart values (Module 10).
- Closes with `12.19 Conclusão e próximos passos` (whole system observed in production).

## Working with This Repo

### When asked to create a new lesson directory

1. Confirm the lesson number and title against `EMENTA-NIVEL-11-AWS.md`
2. Copy structure from the previous adjacent lesson (lessons are incremental snapshots)
3. Apply ONLY the diff described by the lesson title — do not refactor unrelated files
4. Use the naming convention `<MM>.<NN>-<kebab-case-title>/`

### When working on Module 5+ code (planned)

- These directories don't exist yet. If asked to scaffold, follow the existing pattern (incremental snapshots, one dir per aula).
- For Helm releases in Modules 7/8/10, default to `helm_release` resources via OpenTofu, NOT `helm install` shell commands.

### When updating the ementa

The ementa file (`EMENTA-NIVEL-11-AWS.md`) tracks module status badges:
- 🎥 _em gravação_ — Module 1 currently
- ✅ _finalizado_ — Modules 2, 3, 4
- 📋 _ementa planejada_ — Modules 5–13

Renumbering modules is fragile because aula numbers `M.NN` repeat across modules. Edit modules in reverse order (highest first) to avoid collisions.

## .gitignore Rules

The following are excluded and must never be committed:
- `.terraform/`, `.terraform.lock.hcl`
- `*.tfstate`, `*.tfstate.backup`
- `*.tfvars`, `*.tfvars.json` (use `*.example.tfvars` for templates)
- `*.pem` (generated SSH private keys)
- `*.tfplan`, `*.plan`
