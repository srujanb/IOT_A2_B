public class JobClient {

    static final int NOT_ARRIVED = 0;
    static final int WAITING = 1;
    static final int QUEUED = 2;
    static final int IN_PROCESS = 3;
    static final int FINISHED = 4;

    float arrivalTime;

    float queueEntryTime;

    float startTime;

    float stopTime;

    float nextRetransmissionTime;

    float retransmissions = 0;

    int status = NOT_ARRIVED;

    public JobClient(float arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public float getNextRetransmissionTime() {
        return nextRetransmissionTime;
    }

    public void setNextRetransmissionTime(float nextRetransmissionTime) {
        this.nextRetransmissionTime = nextRetransmissionTime;
    }

    public void incrementRetransmissions(){
        setRetransmissions(getRetransmissions() + 1);
    }

    public float getRetransmissions() {
        return retransmissions;
    }

    public void setRetransmissions(float retransmissions) {
        this.retransmissions = retransmissions;
    }

    public float getQueueEntryTime() {
        return queueEntryTime;
    }

    public void setQueueEntryTime(float queueEntryTime) {
        status = QUEUED;
        this.queueEntryTime = queueEntryTime;
    }

    public float getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(float arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public float getStartTime() {
        return startTime;
    }
    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }
    public float getStopTime() {
        return stopTime;
    }

    public void setStopTime(float stopTime) {
        this.stopTime = stopTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
