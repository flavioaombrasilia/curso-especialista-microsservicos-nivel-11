## Comando de insert de dados no deliverydb

kubectl exec -it postgres-0 -- psql -U postgres -d deliverydb -c "
INSERT INTO delivery (id, status, total_items, distance_fee, courier_payout, total_cost,
  sender_name, sender_street, sender_number, sender_zip_code, sender_phone,
  recipient_name, recipient_street, recipient_number, recipient_zip_code, recipient_phone,
  placed_at, expected_delivery_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'DRAFT', 1, 9.30, 10.00, 19.30,
  'João Silva', 'Rua das Carambolas', '42', '38400-000', '34 99999-1111',
  'Maria Souza', 'Avenida Rondon Pacheco', '1500', '38400-100', '34 98888-2222',
  now(), now() + interval '3 hours');"