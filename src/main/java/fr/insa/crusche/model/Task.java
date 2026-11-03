package fr.insa.crusche.model;

import java.sql.Timestamp;

/**
 * A task assigned to an operator on a machine, realizing an operation
 * as part of a specialized goal.
 */
public class Task {

    private int id;
    private String taskId;
    private Timestamp startDate;
    private Timestamp endDate;
    private TaskStatus status;
    private int operatorId;
    private int machineId;
    private int operationId;
    private int specializedGoalId;

    public Task() {
    }

    public Task(int id, String taskId, Timestamp startDate, Timestamp endDate,
                TaskStatus status, int operatorId, int machineId,
                int operationId, int specializedGoalId) {
        this.id = id;
        this.taskId = taskId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.operatorId = operatorId;
        this.machineId = machineId;
        this.operationId = operationId;
        this.specializedGoalId = specializedGoalId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(int operatorId) {
        this.operatorId = operatorId;
    }

    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public int getSpecializedGoalId() {
        return specializedGoalId;
    }

    public void setSpecializedGoalId(int specializedGoalId) {
        this.specializedGoalId = specializedGoalId;
    }
}