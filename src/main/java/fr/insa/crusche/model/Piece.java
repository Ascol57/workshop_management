package fr.insa.crusche.model;

/**
 * A physical instance of a product that moves through a routing.
 */
public class Piece {

    private int id;
    private String pieceRef;
    private int productId;
    private Integer storageLocationId;
    private Integer routingId;
    private Integer currentOperationId;
    private String status;

    public Piece() {
    }

    public Piece(int id, String pieceRef, int productId, Integer storageLocationId,
                 Integer routingId, Integer currentOperationId, String status) {
        this.id = id;
        this.pieceRef = pieceRef;
        this.productId = productId;
        this.storageLocationId = storageLocationId;
        this.routingId = routingId;
        this.currentOperationId = currentOperationId;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPieceRef() {
        return pieceRef;
    }

    public void setPieceRef(String pieceRef) {
        this.pieceRef = pieceRef;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Integer getStorageLocationId() {
        return storageLocationId;
    }

    public void setStorageLocationId(Integer storageLocationId) {
        this.storageLocationId = storageLocationId;
    }

    public Integer getRoutingId() {
        return routingId;
    }

    public void setRoutingId(Integer routingId) {
        this.routingId = routingId;
    }

    public Integer getCurrentOperationId() {
        return currentOperationId;
    }

    public void setCurrentOperationId(Integer currentOperationId) {
        this.currentOperationId = currentOperationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
