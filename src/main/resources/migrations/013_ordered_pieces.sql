ALTER TABLE piece
    DROP CHECK piece_chk_1,
    ADD CONSTRAINT chk_piece_status
        CHECK (status IN ('ORDERED', 'AVAILABLE', 'IN_TRANSFORMATION', 'COMPLETED'));
