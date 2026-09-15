-- Create delivery table
CREATE TABLE delivery (
    id UUID PRIMARY KEY,
    courier_id UUID,
    status VARCHAR(50) NOT NULL,
    
    -- Timestamps
    placed_at TIMESTAMP WITH TIME ZONE,
    assigned_at TIMESTAMP WITH TIME ZONE,
    expected_delivery_at TIMESTAMP WITH TIME ZONE,
    fulfilled_at TIMESTAMP WITH TIME ZONE,
    
    -- Financial data
    distance_fee DECIMAL(10,2),
    courier_payout DECIMAL(10,2),
    total_cost DECIMAL(10,2),
    
    -- Items count
    total_items INTEGER,
    
    -- Sender contact point (embedded)
    sender_zip_code VARCHAR(20),
    sender_street VARCHAR(255),
    sender_number VARCHAR(50),
    sender_complement VARCHAR(255),
    sender_name VARCHAR(255),
    sender_phone VARCHAR(20),
    
    -- Recipient contact point (embedded)
    recipient_zip_code VARCHAR(20),
    recipient_street VARCHAR(255),
    recipient_number VARCHAR(50),
    recipient_complement VARCHAR(255),
    recipient_name VARCHAR(255),
    recipient_phone VARCHAR(20),
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create item table
CREATE TABLE item (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    delivery_id UUID NOT NULL,
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_item_delivery FOREIGN KEY (delivery_id) REFERENCES delivery(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_delivery_status ON delivery(status);
CREATE INDEX idx_delivery_courier_id ON delivery(courier_id);
CREATE INDEX idx_delivery_placed_at ON delivery(placed_at);
CREATE INDEX idx_item_delivery_id ON item(delivery_id);

-- Add constraints
ALTER TABLE delivery ADD CONSTRAINT chk_delivery_status 
    CHECK (status IN ('DRAFT', 'WAITING_FOR_COURIER', 'IN_TRANSIT', 'DELIVERED'));

ALTER TABLE item ADD CONSTRAINT chk_item_quantity_positive 
    CHECK (quantity > 0);