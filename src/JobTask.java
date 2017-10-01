import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class JobTask {

    private final static double rateOfArrival = 17.98;
    private final static int retransmissionTimeMean = 10;

    private static boolean isSystemUnderProcess = false;

    private static int CONSTANT_BUFFER_SIZE = 5;
    static int CONSTANT_INTERARRIVAL_TIME = 6;
    static int CONSTANT_RETRANSMISSION_TIME = 5;
    private static int CONSTANT_SERVICE_TIME = 11;
    private static int CONSTANT_TERMINATION_TIME = 1000;
    private static int CONSTANT_MAX_CLIENTS = 1000;
    private static float seed = 5;

    private final static int EVENT_RETRANSMISSION_ARRIVAL = 11;
    private final static int EVENT_NEW_ARRIVAL = 12;
    private final static int EVENT_SERVICE_COMPLETION = 13;

    private static float master_clock = 0;
    private static JobClient next_new_arrival = new JobClient(2);
    public static float service_completion_time;
    private static JobClient currentServiced;
    private static Queue<JobClient> queue = new LinkedList<>();
    private static Queue<JobClient> retransmission_queue = new LinkedList<>();
    private static Queue<JobClient> processed_clients = new LinkedList<>();

    private static String result = "";

    private static PriorityQueue<Float> statT = new PriorityQueue<>();
    private static PriorityQueue<Float> statD = new PriorityQueue<>();

    public JobTask() {
        resetData();
    }

    private void resetData() {
        isSystemUnderProcess = false;
        master_clock = 0;
        next_new_arrival = new JobClient(2);
        service_completion_time = 0;
        JobClient currentServiced = null;
        queue = new LinkedList<>();
        retransmission_queue = new LinkedList<>();
        processed_clients = new LinkedList<>();
        result = "";
        PriorityQueue<Float> statT = new PriorityQueue<>();
        PriorityQueue<Float> statD = new PriorityQueue<>();
    }


    public static void begin(){
        next_event();
    }

    private static void next_event() {

        String string = "" + master_clock + "\t\t" + next_new_arrival.getArrivalTime() + "\t\t" + service_completion_time + "\t\t" + queue.size() + "\t\t" + "[";
        for (JobClient client:
             retransmission_queue) {
            string += client.getNextRetransmissionTime() + ",";
        } string += "]";
//        System.out.println(string);
        result += string + "\n";
//        if (master_clock >= CONSTANT_TERMINATION_TIME){
        if (processed_clients.size() == CONSTANT_MAX_CLIENTS){ //STOP PROCESS
            postProcess();
            // SAVE TO FILE
//            try(  PrintWriter out = new PrintWriter( "output.txt" )  ){
//                out.println( result );
//                System.out.println("File saved");
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
            return;
        }

        int event = getNextEvent();
        if (event == EVENT_RETRANSMISSION_ARRIVAL)
            doRetransmissionArrival();
        else if (event == EVENT_SERVICE_COMPLETION)
            doServiceCompletion();
        else
            doNextNewArrival();

        next_event();
    }

    private static void postProcess(){
        JobResults result = new JobResults();

        int sizeT = statT.size();
        int sizeD = statD.size();
        //finding average
        int sumT = 0;
        int sumD = 0;
        for (float stat: statT){
            sumT += stat;
        }
//        System.out.println("sizeT value = " + sizeT);
//        System.out.println("T average = " + sumT/sizeT);
        if (sizeT != 0)
            result.setT(sumT/sizeT);
        for (int i = 0; i < 0.95*sizeT; i++) {
            statT.remove();
        }
//        System.out.println("95th of T = " + statT.remove().toString());
        if (statT.size() != 0)
            result.setT95(statT.remove());
        else
            result.setT95(0);
        for (float stat : statD) {
            sumD += stat;
        }
//        System.out.println("sizeD value = " + sizeD);
//        System.out.println("D average = " + sumD/sizeD);
        if (sizeD != 0)
            result.setD(sumD/sizeD);
        for (int i = 0; i < 0.95*sizeD; i++){
            statD.remove();
        }
//        System.out.println("P value = " + master_clock);
        if (statD.size() != 0)
            result.setD95(statD.remove());
        else
            result.setD95(0);
        result.setP(master_clock);

        Main.s.add(result);
    }

    private static JobClient generate_new_arrival() {
//        System.out.println("Generate new arrival called");
        Random rand = new Random();
        float  number = (float) (-(rateOfArrival) * Math.log(rand.nextFloat()));
        float time = master_clock + number;
        JobClient client = new JobClient(time);
        return client;
    }

    private static float generateRetransmissionTime(){
        Random rand = new Random();
        float  number = (float) (-(retransmissionTimeMean) * Math.log(rand.nextFloat()));
        float time = master_clock + number;
        return time;
    }


    public static int getNextEvent() {
        if (service_completion_time == 0 && retransmission_queue.peek() == null)
            return EVENT_NEW_ARRIVAL;
        else if (retransmission_queue.peek() == null){
            if (next_new_arrival.getArrivalTime() <= service_completion_time) {
                return EVENT_NEW_ARRIVAL;
            }
            else {
                if (isSystemUnderProcess)
                    return EVENT_SERVICE_COMPLETION;
                else
                    return EVENT_NEW_ARRIVAL;
            }
        }else if (service_completion_time == 0){
            if (retransmission_queue.peek().getNextRetransmissionTime() <= next_new_arrival.getArrivalTime()) {
                return EVENT_RETRANSMISSION_ARRIVAL;
            }
            else {
                return EVENT_NEW_ARRIVAL;
            }
        }else{
            if (retransmission_queue.peek().getNextRetransmissionTime() <= next_new_arrival.getArrivalTime() && retransmission_queue.peek().getNextRetransmissionTime() <= service_completion_time) {
                return EVENT_RETRANSMISSION_ARRIVAL;
            }
            else if (next_new_arrival.getArrivalTime() <= service_completion_time) {
                return EVENT_NEW_ARRIVAL;
            }
            else {
                return EVENT_SERVICE_COMPLETION;
            }
        }
    }

    private static void doRetransmissionArrival() {
        JobClient client = retransmission_queue.remove();
        master_clock = client.getNextRetransmissionTime();
        if (queue.size() == CONSTANT_BUFFER_SIZE) {// queue is full
            client.setNextRetransmissionTime(generateRetransmissionTime());
            client.incrementRetransmissions();
            retransmission_queue.add(client);
        }else{
            if (isSystemUnderProcess) {
                client.setQueueEntryTime(master_clock);
                queue.add(client);
            }
            else{
                client.setStartTime(master_clock);
                currentServiced = client;
                isSystemUnderProcess = true;
            }
        }
    }

    private static void doServiceCompletion() {
        master_clock = service_completion_time;
        if (currentServiced != null) {
            currentServiced.setStopTime(master_clock);
            processed_clients.add(currentServiced);
            if (currentServiced.getArrivalTime() < currentServiced.getStopTime())
                statT.add((currentServiced.getStopTime() - currentServiced.getArrivalTime()));
            if (currentServiced.getArrivalTime() < currentServiced.getQueueEntryTime())
                statD.add(currentServiced.getQueueEntryTime() - currentServiced.getArrivalTime());
        }

        if (queue.size() == 0) {
            isSystemUnderProcess = false;
        }
        else {
            JobClient client = queue.remove();
            client.setStartTime(master_clock);
            currentServiced = client;
            service_completion_time = (master_clock + CONSTANT_SERVICE_TIME);
        }
    }

    private static void doNextNewArrival() {
//        System.out.println("Do next new arrival called");
        master_clock = next_new_arrival.getArrivalTime();
        if (isSystemUnderProcess){
            if (queue.size() == CONSTANT_BUFFER_SIZE) {// queue is full
                next_new_arrival.setNextRetransmissionTime(generateRetransmissionTime());
                retransmission_queue.add(next_new_arrival);
            }
            else
                next_new_arrival.setQueueEntryTime(master_clock);
                queue.add(next_new_arrival);
        }else{
            isSystemUnderProcess = true;
            next_new_arrival.setStartTime(master_clock);
            service_completion_time = master_clock + CONSTANT_SERVICE_TIME;
        }
        next_new_arrival = generate_new_arrival();
    }

    private void setSeed(float seed){
        this.seed = seed;
    }
}
