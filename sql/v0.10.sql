USE pm_system;
ALTER TABLE sys_project_member ADD COLUMN status VARCHAR(20) DEFAULT 'confirmed' COMMENT 'confirmed/pending';
