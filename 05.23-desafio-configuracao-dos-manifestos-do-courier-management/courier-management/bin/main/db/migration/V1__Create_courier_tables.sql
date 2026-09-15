-- Tabela de entregadores
CREATE TABLE courier (
    id UUID PRIMARY KEY,

    -- Dados do entregador
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,

    -- Contadores de entregas
    fulfilled_deliveries_quantity INTEGER NOT NULL DEFAULT 0,
    pending_deliveries_quantity INTEGER NOT NULL DEFAULT 0,

    last_fulfilled_delivery_at TIMESTAMP WITH TIME ZONE
);

-- Entregas atribuídas a um entregador
CREATE TABLE assigned_delivery (
    id UUID PRIMARY KEY,
    courier_id UUID NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_assigned_delivery_courier
        FOREIGN KEY (courier_id) REFERENCES courier(id) ON DELETE CASCADE
);

-- A busca do entregador mais ocioso ordena por esta coluna
CREATE INDEX idx_courier_last_fulfilled_delivery_at ON courier(last_fulfilled_delivery_at);
CREATE INDEX idx_assigned_delivery_courier_id ON assigned_delivery(courier_id);

-- Contadores não podem ficar negativos
ALTER TABLE courier ADD CONSTRAINT chk_courier_fulfilled_deliveries_quantity
    CHECK (fulfilled_deliveries_quantity >= 0);

ALTER TABLE courier ADD CONSTRAINT chk_courier_pending_deliveries_quantity
    CHECK (pending_deliveries_quantity >= 0);
