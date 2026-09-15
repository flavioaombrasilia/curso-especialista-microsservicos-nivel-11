## Comando para gerar carga de CPU

kubectl run gerador-de-carga --rm -it --restart=Never \
  --image=busybox:1.37 -- \
  sh -c 'while true; do wget -q -O- "http://hellojava/admin/cpu?ms=1000" >/dev/null; done'