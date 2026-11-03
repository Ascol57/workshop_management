package fr.insa.crusche.model;

/**
 * A specialized goal is a step in the decomposition of a general goal.
 * It generates tasks to be performed by operators.
 */
public class SpecializedGoal {

    private int id;
    private String step;
    private GoalStatus status;
    private int generalGoalId;
    private java.util.List<Task> tasks = new java.util.ArrayList<>();

    public SpecializedGoal() {
    }

    public SpecializedGoal(int id, String step, GoalStatus status, int generalGoalId) {
        this.id = id;
        this.step = step;
        this.status = status;
        this.generalGoalId = generalGoalId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public int getGeneralGoalId() {
        return generalGoalId;
    }

    public void setGeneralGoalId(int generalGoalId) {
        this.generalGoalId = generalGoalId;
    }

    public java.util.List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(java.util.List<Task> tasks) {
        this.tasks = tasks;
    }
}