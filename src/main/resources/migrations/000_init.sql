-- Workshop Management System - Initial Relational Schema
-- Generated from uml/relational_schema.mermaid
-- Uses IF NOT EXISTS for idempotency (re-runnable without error)
-- Uses CHECK constraints to validate enumerations

CREATE TABLE IF NOT EXISTS workshop (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS app_user (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    login VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('MANAGER', 'OUVRIER', 'ADMINISTRATEUR')),
    workshop_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_user_login UNIQUE (login),
    CONSTRAINT fk_user_workshop FOREIGN KEY (workshop_id)
        REFERENCES workshop (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS equipment (
    id INT NOT NULL AUTO_INCREMENT,
    equipment_ref VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    workshop_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_equipment_workshop FOREIGN KEY (workshop_id)
        REFERENCES workshop (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS machine (
    id INT NOT NULL AUTO_INCREMENT,
    type VARCHAR(50) NOT NULL,
    hourly_cost FLOAT NOT NULL,
    x FLOAT NOT NULL,
    y FLOAT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPERATIONAL', 'BROKEN', 'UNDER_MAINTENANCE', 'AVAILABLE', 'OCCUPIED')),
    equipment_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_machine_equipment FOREIGN KEY (equipment_id)
        REFERENCES equipment (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS workstation (
    id INT NOT NULL AUTO_INCREMENT,
    equipment_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_workstation_equipment FOREIGN KEY (equipment_id)
        REFERENCES equipment (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS product (
    id INT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    workshop_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_workshop FOREIGN KEY (workshop_id)
        REFERENCES workshop (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS routing (
    id INT NOT NULL AUTO_INCREMENT,
    routing_ref VARCHAR(50) NOT NULL,
    product_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_routing_product FOREIGN KEY (product_id)
        REFERENCES product (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS operation (
    id INT NOT NULL AUTO_INCREMENT,
    operation_ref VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    equipment_id INT NOT NULL,
    duration FLOAT NOT NULL,
    routing_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_operation_equipment FOREIGN KEY (equipment_id)
        REFERENCES equipment (id) ON DELETE RESTRICT,
    CONSTRAINT fk_operation_routing FOREIGN KEY (routing_id)
        REFERENCES routing (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS general_goal (
    id INT NOT NULL AUTO_INCREMENT,
    target_quantity INT NOT NULL,
    status VARCHAR(12) NOT NULL CHECK (status IN ('IN_PROGRESS', 'ACHIEVED', 'CANCELLED')),
    workshop_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_general_goal_workshop FOREIGN KEY (workshop_id)
        REFERENCES workshop (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS specialized_goal (
    id INT NOT NULL AUTO_INCREMENT,
    step VARCHAR(255) NOT NULL,
    status VARCHAR(12) NOT NULL CHECK (status IN ('IN_PROGRESS', 'ACHIEVED', 'CANCELLED')),
    general_goal_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_specialized_goal_general FOREIGN KEY (general_goal_id)
        REFERENCES general_goal (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS operator (
    id INT NOT NULL AUTO_INCREMENT,
    hourly_cost FLOAT NOT NULL,
    status VARCHAR(10) NOT NULL CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'PAUSED', 'OFFLINE')),
    user_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_operator_user FOREIGN KEY (user_id)
        REFERENCES app_user (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS task (
    id INT NOT NULL AUTO_INCREMENT,
    task_id VARCHAR(50) NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    status VARCHAR(12) NOT NULL CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'SUSPENDED')),
    operator_id INT NOT NULL,
    machine_id INT NOT NULL,
    operation_id INT NOT NULL,
    specialized_goal_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_task_operator FOREIGN KEY (operator_id)
        REFERENCES operator (id) ON DELETE RESTRICT,
    CONSTRAINT fk_task_machine FOREIGN KEY (machine_id)
        REFERENCES machine (id) ON DELETE RESTRICT,
    CONSTRAINT fk_task_operation FOREIGN KEY (operation_id)
        REFERENCES operation (id) ON DELETE RESTRICT,
    CONSTRAINT fk_task_specialized_goal FOREIGN KEY (specialized_goal_id)
        REFERENCES specialized_goal (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS workshop_manager (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_workshop_manager_user FOREIGN KEY (user_id)
        REFERENCES app_user (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

-- Junction table: operator skilled for machine
CREATE TABLE IF NOT EXISTS operator_machine (
    operator_id INT NOT NULL,
    machine_id INT NOT NULL,
    PRIMARY KEY (operator_id, machine_id),
    CONSTRAINT fk_operator_machine_operator FOREIGN KEY (operator_id)
        REFERENCES operator (id) ON DELETE CASCADE,
    CONSTRAINT fk_operator_machine_machine FOREIGN KEY (machine_id)
        REFERENCES machine (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

-- Junction table: workstation groups machine
CREATE TABLE IF NOT EXISTS workstation_machine (
    workstation_id INT NOT NULL,
    machine_id INT NOT NULL,
    PRIMARY KEY (workstation_id, machine_id),
    CONSTRAINT fk_workstation_machine_workstation FOREIGN KEY (workstation_id)
        REFERENCES workstation (id) ON DELETE CASCADE,
    CONSTRAINT fk_workstation_machine_machine FOREIGN KEY (machine_id)
        REFERENCES machine (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

-- Junction table: routing includes equipment
CREATE TABLE IF NOT EXISTS routing_equipment (
    routing_id INT NOT NULL,
    equipment_id INT NOT NULL,
    PRIMARY KEY (routing_id, equipment_id),
    CONSTRAINT fk_routing_equipment_routing FOREIGN KEY (routing_id)
        REFERENCES routing (id) ON DELETE CASCADE,
    CONSTRAINT fk_routing_equipment_equipment FOREIGN KEY (equipment_id)
        REFERENCES equipment (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

-- Additional indexes for relationships
CREATE INDEX idx_equipment_workshop ON equipment (workshop_id);
CREATE INDEX idx_machine_equipment ON machine (equipment_id);
CREATE INDEX idx_workstation_equipment ON workstation (equipment_id);
CREATE INDEX idx_product_workshop ON product (workshop_id);
CREATE INDEX idx_general_goal_workshop ON general_goal (workshop_id);
CREATE INDEX idx_specialized_goal_general ON specialized_goal (general_goal_id);
CREATE INDEX idx_task_operator ON task (operator_id);
CREATE INDEX idx_task_machine ON task (machine_id);
CREATE INDEX idx_task_operation ON task (operation_id);
CREATE INDEX idx_task_specialized_goal ON task (specialized_goal_id);
CREATE INDEX idx_operator_machine ON operator_machine (operator_id);
CREATE INDEX idx_workstation_machine ON workstation_machine (workstation_id);
CREATE INDEX idx_routing_equipment ON routing_equipment (routing_id);