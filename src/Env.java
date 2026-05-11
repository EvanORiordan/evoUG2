import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Evan O'Riordan (e.oriordan3@universityofgalway.ie)<br>
 * School of Computer Science<br>
 * University of Galway<br>
 */
public class Env extends Thread{ // environment simulator
    static String game = "DG"; // default game is DG.
    static double M; // UG/DG prize
    static String space = "grid"; // indicates what kind of space the population will reside within
    static int length; // length of space; in 2D grid, len = num rows
    static int width; // width of space; in 2D grid, wid = num cols i.e. num agents per row
    static String neighType; // indicates type of neighbourhood agents will have.
    static int neighRadius = 1; // radius of neighbourhood
    static int neighSize; // size of neighbourhood (assuming randomNeigh neighType) i.e. num random neighbours
    static int N; // population size
    static int run; // current run
    static int runs; // number of experiment runs to occur
    Agent[] pop; // array of agents; assumes 2D space
    double mean_p; // mean proposal value; informally referred to as the performance of an experiment
//    double mean_q; // mean acceptance threshold
    double mean_u; // mean utility
    double mean_k; // mean k of population
    double sigma_p; // standard deviation of p
//    double sigma_q; // standard deviation of q
    double sigma_u; // standard deviation of utility
    double sigma_k; // standard deviation of k
    double max_p; // highest p in population at a time
    double variance_p; // the variance of p from the mean p of a population
//    int pBin1; // p in [0, 0.2]
//    int pBin2; // p in (0.2, 0.4]
//    int pBin3; // p in (0.4, 0.6]
//    int pBin4; // p in (0.6, 0.8]
//    int pBin5; // p in (0.8, 1]
    int[] pBins;
    double skewness_p; // skewness of p in a population
    int gen; // current generation
    static int gens; // number of generations of evolution to occur per experiment run
    static int rounds; // number of rounds of play and EWL
    static String UF; // utility function: indicates how utility is calculated
    static String VP = ""; // variable parameter: indicates which parameter will be varied in experiment series.
    static String[] variations = new String[]{};
    static int exp; // indicates how far along we are through the experiment series
    static int exps = 1; // number of experiments in series
    static FileWriter fw;
    static BufferedReader br;
    static Scanner scanner = new Scanner(System.in);
    static String config_filename = "config.csv";
//    static DecimalFormat DF1 = Agent.getDF1(); // formats numbers to 1 decimal place
//    static DecimalFormat DF2 = Agent.getDF2(); // formats numbers to 2 decimal place
    static DecimalFormat DF4 = Agent.getDF4(); // formats numbers to 4 decimal places
    static String general_path = "C:\\Users\\Evan O'Riordan\\Documents\\csv_data"; // path where all datasets are stored.
    static String specific_path; // specific path of 1 dataset.
    static String exp_path; // address where stats for current experiment are stored
    static String run_path; // address where stats for current run are stored
    static boolean writePGenStats;
    static boolean writeUGenStats;
    static boolean writeKGenStats;
    static boolean writePRunStats;
    static boolean writeURunStats;
    static boolean writeKRunStats;
    static boolean writePosData =
//        true;
        false;
    static int writeRate = 0; // write data every x gens
    static String EWT = ""; // EWT: edge weight type
    static String EWL = ""; // EWL function
    static double ROC = 0; // rate of change: fixed learning amount to EW
    static String evo; // indicates which evolution function to call
    static String sel; // indicates which selection function to call
    static double EN = 0.0; // evolutionary noise
    static String mut = ""; // indicates which mutation function to call
    static double mutRate = 0.0; // probability of mutation
    static double mutBound = 0.0; // denotes max mutation possible
    static String genType; // indicates the type of generation the algorithm will employ.
    static int ER = 0; // evolution rate: used in various ways to denote how often generations occur
    static int NIS = 0; // num inner steps: number of inner steps per generation using the monte carlo method; usually is set to value of N
    static String RWT = ""; // roulette wheel type
    static String RA = ""; // rewire away
    static String RT = ""; // rewire to
    static double RP = 0.0; // rewire probability parameter.
//    static int injRound; // injection round: indicates at which round strategy injection will occur. 0 ==> no injection.
    static double injP = 0.0; // injection p: indicates p value to be injected
    static int injSize = 0; // injection cluster size: indicates size of cluster to be injected
    static double cost; // the cost of punishing.
    static double fine; // the fine for being punished.
    static String PP = ""; // punishment probability function
    static int CI; // configuration index
    static boolean NU; // indicates whether individuals can have negative utility.
    static double PN1; // indicates how much noise is present during the noisy punishment function.
    static double PN2; // probability of agents making the opposite choice regarding punishment.
    static double LR; // learning rate
    static double EF; // enhancement factor
    static ArrayList<String> configs = new ArrayList<>(); // stores configurations
    static ArrayList<String> timestamps = new ArrayList<>();
    int num_puns = 0;
    static String PS = ""; // punishment severity function
    static String V = ""; // vindictiveness
    static double leeway;
    static double threshold; // affects threshold PP case
    static boolean punish = false;
    static double RN1 = 0; // rewiring noise
    static double RN2; // 2nd form of rewiring noise



    /**
     * Main method of Java program.
      */
    public static void main(String[] args) {
        
        // load configurations.
        try {
            br = new BufferedReader(new FileReader(config_filename));
            String line; // initialises String to store rows of data
            br.readLine(); // ignores the row of headings
            while ((line = br.readLine()) != null) {
                configs.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // account for when args are vs aren't given.
        int[] config_line_nums;
        if (args.length == 0) {
            config_line_nums = new int[1];
            System.out.print("[REQUEST] Insert configuration line number: ");
            config_line_nums[0] = scanner.nextInt();
        } else {
            config_line_nums = new int[args.length];
            for (int i = 0; i < args.length; i++) {
                config_line_nums[i] = Integer.parseInt(args[i]);
            }
        }

        // commence experimentation.
        String console_str = "[INFO] Configuration line number(s) selected/loaded: ";
        for (int config_line_num : config_line_nums) {
            console_str += config_line_num + " ";
        }
        System.out.println(console_str);
        for (int i = 0; i < config_line_nums.length; i++) {
            System.out.println("[INFO] Current configuration line number: " + config_line_nums[i]);
            try {
                configEnv(config_line_nums[i]);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("[INFO] Invalid configuration number.");
                exit(1);
            }
            LocalDateTime start_timestamp = LocalDateTime.now();
            String start_timestamp_string = start_timestamp.getYear()
                        + "-" + start_timestamp.getMonthValue()
                        + "-" + start_timestamp.getDayOfMonth()
                        + "_" + start_timestamp.getHour()
                        + "-" + start_timestamp.getMinute()
                        + "-" + start_timestamp.getSecond();
            timestamps.add(start_timestamp_string);
//            specific_path += "\\" + start_timestamp_string;
            specific_path = general_path + "\\" + start_timestamp_string;
            if (writeRate > 0) {
                try {
                    Files.createDirectories(Paths.get(specific_path)); // create stats storage folder
                } catch (IOException e) {
                    e.printStackTrace();
                }
                printPath();
            }
//            System.out.println("Start experimentation...\nStarting timestamp: " + start_timestamp);
            experimentSeries();
            LocalDateTime finish_timestamp = LocalDateTime.now(); // marks the end of the main algorithm's runtime
//            System.out.println("Finishing timestamp: " + finish_timestamp);
            Duration duration = Duration.between(start_timestamp, finish_timestamp);
            long secondsElapsed = duration.toSeconds();
            long minutesElapsed = duration.toMinutes();
            long hoursElapsed = duration.toHours();
            System.out.println("[INFO] Time elapsed: " + hoursElapsed + " hours, " + minutesElapsed % 60 + " minutes, " + secondsElapsed % 60 + " seconds");
        }
        if (writeRate > 0) {
            String console_output = "[INFO] IDs:";
            for (String timestamp : timestamps) {
                console_output += "\n" + timestamp;
            }
            console_output += "\n[INFO] IDs for jupyter:\ncsv_data_address+'/" + timestamps.get(0) + "'";
            for (int j = 1; j < timestamps.size(); j++) {
                console_output += "\n,csv_data_address+'/" + timestamps.get(j) + "'";
            }
            System.out.println(console_output);
        }

        // terminate program as intended.
        exit(0);
    }



    /**
     * Runs an experiment series.
     */
    public static void experimentSeries() {
        for (exp = 1; exp <= exps; exp++) {
            exp_path = specific_path + "\\exp" + exp;
            createDataFolders();
            experiment(); // run an experiment of the series
            if (exp <= variations.length) { // do not try to vary after the last experiment has ended
                System.out.print("[INFO] Varying "+VP+": ");
                switch (VP) {
                    case "EWL" -> setEWL(variations[exp - 1]);
                    case "RA" -> setRA(variations[exp - 1]);
                    case "RT" -> setRT(variations[exp - 1]);
                    case "sel" -> setSel(variations[exp - 1]);
                    case "evo" -> setEvo(variations[exp - 1]);
                    case "EN" -> setEN(variations[exp - 1]);
                    case "EWT" -> setEWT(variations[exp - 1]);
                    case "gens" -> setGens(variations[exp - 1]);
                    case "length" -> {
                        setLength(variations[exp - 1]);
                        if (space.equals("grid")) {
                            setWidth();
                        }
                        setN();
                    }
                    case "ER" -> setER(variations[exp - 1]);
                    case "ROC" -> setROC(variations[exp - 1]);
                    case "RP" -> setRP(variations[exp - 1]);
                    case "mutRate" -> setMutRate(variations[exp - 1]);
                    case "mutBound" -> setMutBound(variations[exp - 1]);
                    case "UF" -> setUF(variations[exp - 1]);
                    case "PP" -> setPP(variations[exp - 1]);
                    case "EF" -> setEF(variations[exp - 1]);
                    case "cost" -> setCost(variations[exp - 1]);
                    case "fine" -> setFine(variations[exp - 1]);
                    case "NU" -> setNU(variations[exp - 1]);
                    case "PN1" -> setPN1(variations[exp - 1]);
                    case "PN2" -> setPN2(variations[exp - 1]);
                    case "M" -> setM(variations[exp - 1]);
                    case "V" -> setV(variations[exp - 1]);
                    case "leeway" -> setLeeway(variations[exp - 1]);
                    case "threshold" -> setThreshold(variations[exp - 1]);
                    case "RN1" -> setRN1(variations[exp - 1]);
                    case "RN2" -> setRN2(variations[exp - 1]);
                }
            }
        }
    }



    /**
     * Run an experiment.
     * An experiment consists of runs.
     * Data is collected after each experiment.
     */
    public static void experiment() {
        for (run = 1; run <= runs; run++) {
            run_path = exp_path + "\\run" + run;
            Env pop = new Env();
            pop.start();
        }
        writeSettings();
        writeSeriesStats();
        writeVP();
    }



    /**
     * Core algorithm of the program:
     * (1) Initialise population.
     * (2) Initialise neighbourhoods and weights.
     * (3) Generations of population pass.
     */
    @Override
    public void start() {
        // initialise population and edge network.
        initRandomPop();
        for (int i=0;i<N;i++) {
            getNeighbours(pop[i]);
        }
        if (writePosData && run == 1) {
            writePosData();
        }
        if (!EWL.equals("")) {
            for (int i=0;i<N;i++) {
                initialiseEdgeWeights(pop[i]);
            }
        }

        // testing Agent.toString().
        String string = pop[0].toString();

        // get stats for gen 0.
        calculateStats();
        writeGenAndRunStats();

        // activate population.
        switch (genType) {
            case "ER" -> ER();
            case "MCv1" -> MCv1();
            case "MCv2" -> MCv2();
            case "oneGuyEvo" -> oneGuyEvo();
        }
        writeExpStats();
    }



    /**
     * play Dictator Games
     * @param a focal agent
     */
    public void play(Agent a) {
        ArrayList<Agent> omega_a = a.getOmega(); // neighbourhood of a
        for (int i = 0; i < a.getK(); i++) {
            Agent b = omega_a.get(i); // neighbour of a
            switch (EWT) {
                default -> {
                    switch (game) {
                        case "UG", "DG" -> UG(a, b);
                    }
                }
                case "prevention"-> {
                    ArrayList <Agent> omega_b = b.getOmega(); // neighbourhood of b
                    for (int j = 0; j < b.getK(); j++) {
                        Agent c = omega_b.get(j); // neighbour of b
                        if (a.equals(c)) { // find a in omega_b
                            double w_ba = b.getEdgeWeights().get(j); // weight of edge from b to a
                            double random_double = ThreadLocalRandom.current().nextDouble();
                            if (w_ba > random_double) {
                                switch (game) {
                                    case "UG", "DG" -> UG(a, b);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }


    /**
     * Proposer initiates ultimatum game interaction with responder.<br>
     * If p_a >= q_b, b accepts the proposal made by a, otherwise b rejects it.<br>
     * @param a proposer
     * @param b responder
     */
    public void UG(Agent a, Agent b) {
        double p_a = a.getP();
        double q_b = b.getQ();
        double pi_a = 0.0;
        double pi_b = 0.0;
        if (p_a >= q_b) {
            pi_b = M * p_a;
            pi_a = M - pi_b;
        }
        updateUtility(a, pi_a);
        updateUtility(b, pi_b);
    }




    public void initialiseEdgeWeights(Agent agent) {
        ArrayList <Double> edge_weights = new ArrayList<>();
        for (int i = 0; i< agent.getK(); i++) {
            edge_weights.add(1.0);
        }
        agent.setEdgeWeights(edge_weights);
    }



    /**
     * Edge Weight Learning (EWL)
     */
    public void EWL(Agent a) {
        ArrayList<Double> weights = a.getEdgeWeights();
        ArrayList<Agent> omega_a = a.getOmega();
        for (int i = 0; i < a.getK(); i++) {
            Agent b = omega_a.get(i);
            double w_ab = weights.get(i); // weight from a to b
            w_ab += calculateLearning(a, b);
            if (w_ab > 1.0) {
                w_ab = 1.0;
            } else if (w_ab < 0.0) {
                w_ab = 0.0;
            }
            weights.set(i, w_ab); // replace old weight value
        }
    }



    public double calculateLearning(Agent a, Agent b) {
        double learning = 0.0;
        double p_a = a.getP();
        double p_b = b.getP();
        double u_a = a.getU();
        double u_b = b.getU();

        switch (EWL) {
            case "PROC" -> {
                if (p_a < p_b) {// if b fairer than a, increase weight
                    learning = ROC;
                } else if (p_a > p_b) { // else if a fairer than b, decrease weight
                    learning = -ROC;
                } // else no change
            }
            case "PD" -> learning = p_b - p_a;
            case "PED" -> learning = Math.exp(p_b - p_a);
            case "UD" -> learning = u_b - u_a;
            case "UED" -> learning = Math.exp(u_b - u_a);
            case "PDRv1" -> { // PDR: proposal value difference random.
                double diff = p_b - p_a;
                if (p_a < p_b) {
                    learning = ThreadLocalRandom.current().nextDouble(diff);
                } else if (p_a > p_b) {
                    learning = -ThreadLocalRandom.current().nextDouble(-(diff)); // the minuses work around exceptions. ultimately, learning will be assigned a negative value.
                }
            }
            case "PDRv2" -> { // alternate implementation of PDR. i think v2 functions identically to v1.
                if (p_a < p_b) {
                    learning = ThreadLocalRandom.current().nextDouble(p_b - p_a);
                } else if (p_a > p_b) {
                    learning = -ThreadLocalRandom.current().nextDouble(p_a - p_b);
                }
            }
            case "PEDv2" -> {
                if (p_a < p_b) { // if a unfairer than b, raise weight
                    learning = Math.pow((p_b - p_a), Math.exp(1));
                } else { // if a fairer than b, reduce weight
                    learning = - Math.pow((p_a - p_b), Math.exp(1));
                }
            }
            case "PEDv4" -> {
                double exp = Math.exp(Math.abs(p_b - p_a));
                if (p_a < p_b) {
                    learning = exp;
                } else if (p_a > p_b) {
                    learning = -exp;
                } else{
                    learning = 0.0;
                }
            }
            case "PStepwise" -> {
                if (p_a < p_b) {
                    learning = 1.0;
                } else if (p_a > p_b) {
                    learning = -1.0;
                }
            }
            case "UROC" ->{
                if (u_a > u_b) {
                    learning = ROC;
                } else if (u_a < u_b) {
                    learning = -ROC;
                }
            }
            case "PEAD" -> learning = Math.exp(Math.abs(p_b - p_a));
            case "PPEAD" -> {
                if (p_a < p_b) {
                    learning = Math.exp(Math.abs(p_b - p_a));
                } else if (p_a > p_b) {
                    learning = -Math.exp(Math.abs(p_b - p_a));
                }
            }
            case "PPED"->{
                if (p_a < p_b) {
                    learning = Math.exp(p_b - p_a);
                } else if (p_a > p_b) {
                    learning = -Math.exp(p_b - p_a);
                }
            }
            case "UUEAD" ->{
                if (u_a > u_b) {
                    learning = Math.exp(Math.abs(b.getU() - a.getU()));
                } else if (u_a < u_b) {
                    learning = -Math.exp(Math.abs(b.getU() - a.getU()));
                }
            }
            case "UStepwise" ->{
                if (u_a > u_b) {
                    learning = 1.0;
                } else if (u_a < u_b) {
                    learning = -1.0;
                }
            }
            case "PDhalf" -> learning = (p_b - p_a) / 2;
            case "PDdouble" -> learning = (p_b - p_a) * 2;
            case "PDtriple" -> learning = (p_b - p_a) * 3;

            // even if p_a > p_b, as long as p_b > 0.5 and p_a > 0.5, learning > 0. thus, learning can be
            // positive even though b gives less.
            case "test1" -> {
                if (p_a > 0.5 && p_b > 0.5) {
                    learning = 1;
                } else {
                    learning = p_b - p_a;
                }
            }

            // even if p_a > p_b, as long as p_b > 0.5, learning > 0. thus, learning can be positive even though b gives less.
            case "test2" -> {
                if (p_b > 0.5) {
                    learning = 1;
                } else {
                    learning = p_b - p_a;
                }
            }

            /*
            * this doesnt make sense. why would a being generous imply w increases?
            * even if p_b ~= 0, as long as p_a > 0.5, w increases!
            * */
            case "test3" -> {
                if (p_a > 0.5) {
                    learning = 1;
                } else {
                    learning = p_b - p_a;
                }
            }

            // EWL as a function of the p diff and the distance of p from 0.5.
            // want to have less of high p guys punishing other high p guys.

            // if p_a > p_b, learning is negative. then, if p_b > 0.5, halve the negative learning.
            case "test4" -> {
                learning = p_b - p_a;
                if (p_a > p_b && p_b > 0.5) { // if p_a > p_b and p_b > 0.5, then logically, p_a > 0.5. the condition is essentially "if p_a > p_b > 0.5".
                    learning *= 0.5;
                }
            }

            // GOAL: THE GREATER PB IS THAN 0.5, THE HIGHER LEARNING WILL BE.
            case "test5" -> {
                learning = p_b - p_a;
                if (p_a > p_b && p_b > 0.5) {
                    // at this point, learning < 0.
                    double x = p_b - 0.5; // higher p_b ==> higher x.
                    double y = 1 - x; // higher x ==> lower y.
                    learning *= y; // higher y ==> lower learning (since learning < 0).
                    // e.g. p_b = 0.9 ==> x = 0.4 ==> y = 0.6 ==> learning *= 0.6.
                    // e.g. p_b = 0.55 ==> x = 0.05 ==> y = 0.95 ==> learning *= 0.95.
                }
            }

            case "test6" -> {
                if (p_a > p_b && p_b > 0.5) {
                    /*
                    * learning will always be positive.
                    * perhaps 2 is too small of a denominator.
                    * if it was 3, learning would be negative if p_a was way bigger than p_b.
                     */
                    learning = p_b - (p_a / 2);

                } else {
                    learning = p_b - p_a;
                }
            }

            // same as test6 except denominator = 3, not 2.
            case "test7" -> {
                if (p_a > p_b && p_b > 0.5) {
                    learning = p_b - (p_a / 3);
                } else {
                    learning = p_b - p_a;
                }
            }

            // same as test6 except denominator = 4, not 2.
            case "test8" -> {
                if (p_a > p_b && p_b > 0.5) {
                    learning = p_b - (p_a / 4);
                } else {
                    learning = p_b - p_a;
                }
            }

            // same as test1 except first half of first clause is p_a > p_b rather than p_a > 0.5.
            // same as test6 except first consequence is learning = 1 rather than learning = p_b - (p_a / 2).
            case "test9" -> {
                if (p_a > p_b && p_b > 0.5) {
                    learning = 1;
                } else {
                    learning = p_b - p_a;
                }
            }

            // if a is fairer than b but b gives more than half, do not modify weight.
            case "test10" -> {
                if (p_a > p_b && p_b > 0.5) {
                    learning = 0;
                } else {
                    learning = p_b - p_a;
                }
            }

        }
        return learning;
    }





    /**
     * Roulette wheel (RW) selection function.
     * canditates are child and its neighbours.
     * probability of selection depends directly on fitness in
     * comparison to other candidates in the pool.
     * fitter ==> greater probability.
     * @param child agent undergoing roulette wheel selection
     * @return parent
     */
    public Agent selRW(Agent child) {
        Agent parent = child; // if no neighbour is selected, child is parent by default.
        ArrayList <Agent> pool = new ArrayList<>(child.getOmega()); // pool of candidates for parent
        pool.add(child);
        int size = pool.size();
        double[] pockets = new double[size];
        double roulette_total = 0;
        for (int i = 0; i < size; i++) {
            switch (RWT) {
                case "normal" -> pockets[i] = pool.get(i).getU();
                case "expo" -> pockets[i] = Math.exp(pool.get(i).getU() * EN);
            }
            roulette_total += pockets[i];
        }
        double random_double = ThreadLocalRandom.current().nextDouble();
        double tally = 0;
        for (int i = 0; i < size - 1; i++) { // if no neighbour candidate is selected, child is selected.
            tally += pockets[i];
            double percent_taken = tally / roulette_total; // how much space in the wheel has been taken up so far
            if (random_double < percent_taken) { // if true, the ball landed in the neighbour candidate's slot
                parent = pool.get(i); // select neighbour candidate as parent
                break;
            }
        }
        return parent;
    }



    /**
     * child selects fittest neighbour that is fitter than them.
     * if no such neighbours exist, child is parent by default.
      */
    public Agent selElitist(Agent a) {
        ArrayList<Agent> omega_a = a.getOmega();
        Agent parent = a;
        for (int i = 0; i < a.getK(); i++) {
            Agent neighbour = omega_a.get(i);
            if (neighbour.getU() > parent.getU()) { // if candidate fitter than parent, parent is set to candidate
                parent = neighbour;
            }
        }
        return parent;
    }



    /**
     * sel and evo effectively occur at once in one function.
     * crossover where one child adopts midway point between two parent strategies.
     */
    public void crossover(Agent child) {

//        // how to select parents?
//
//        // select two fittest neighbours?
//        ArrayList <Agent> omega = child.getOmega();
//        Agent parent1 = child; // fittest neighbour
//        Agent parent2 = child; // second-fittest neighbour
//        for (int i=0;i<omega.size();i++) {
//            Agent neighbour = omega.get(i);
//            double neighbour_u = neighbour.getU();
//            double parent2_u = parent2.getU();
//            if (neighbour_u > parent2_u) {
//                parent2 = omega.get(i);
//                parent2_u = parent2.getU();
//                double parent1_mean_score = parent1.getU();
//                if (parent2_u > parent1_mean_score) {
//                    Agent temp = parent1;
//                    parent1 = parent2;
//                    parent2 = temp;
//                }
//            }
//        }
//
//        double p1 = parent1.getP();
//        double p2 = parent2.getP();
//        double new_p = (p1 + p2) / 2;
//        child.setP(new_p);

        System.out.println("this function is currently out of commission...");
    }




    /**
     * Child wholly copies parent's DG strategy.
     */
    public void evoCopy(Agent child, Agent parent) {
        child.setP(parent.getOldP());
    }



    /**
     * Use noise to move child strategy in direction of parent strategy.
     */
    public void evoApproach(Agent child, Agent parent) {
//        int ID = child.getID();
//        int parent_ID = parent.getID();
//        double p = child.getP();
//        double q = child.getQ();
//        double parent_old_p = parent.getOldP();
//        double parent_old_q = parent.getOldQ();
//
//        // do not approach evolve if parent is child
//        if (parent_ID != ID) {
//
//            // for attribute, if parent is lower, reduce child; else, increase.
//            double approach = ThreadLocalRandom.current().nextDouble(noise);
//            if (parent_old_p < p) {
//                approach *= -1;
//            }
//            double new_p = p + approach;
//            if (parent_old_q < q) {
//                approach *= -1;
//            }
//            double new_q = q + approach;
//
////            setStrategy(child, new_p, new_q);
//            child.setP(new_p);
//            child.setQ(new_q);
//        }

        System.out.println("this function is currently decommissioned...");
    }



    /**
     * Mutation rate parameter determines the probability for mutation to occur.
     * @return boolean indicating whether mutation will occur
     */
    public boolean mutationCheck() {
        double random_double = ThreadLocalRandom.current().nextDouble();
        return random_double < mutRate;
    }



    /**
     * Child's attributes are randomly and independently generated.
     */
    public void mutGlobal(Agent child) {
        if (mutationCheck()) {
            switch (game) {
                case "UG" -> {
                    double new_p = ThreadLocalRandom.current().nextDouble();
                    double new_q = ThreadLocalRandom.current().nextDouble();
                    child.setP(new_p);
                    child.setQ(new_q);
                }
                case "DG" -> {
                    double new_p = ThreadLocalRandom.current().nextDouble();
                    child.setP(new_p);
                }
            }
        }
    }



    /**
     * Slight mutations are independently applied to child's attributes.
     */
    public void mutLocal(Agent child) {
        if (mutationCheck()) {
            switch (game) {
                case "UG" -> {
                    double p = child.getP();
                    double q = child.getQ();
                    double new_p = ThreadLocalRandom.current().nextDouble(p - mutBound, p + mutBound);
                    double new_q = ThreadLocalRandom.current().nextDouble(q - mutBound, q + mutBound);
                    child.setP(new_p);
                    child.setQ(new_q);
                }
                case "DG" -> {
                    double p = child.getP();
                    double new_p = ThreadLocalRandom.current().nextDouble(p - mutBound, p + mutBound);
                    child.setP(new_p);
                }
            }
        }
    }



    // the config num should be set to the line number the config is on in config.csv.
    public static void configEnv(int config_line_num) {
        String[] settings = configs.get(config_line_num - 2).split(",");
        CI = 0;
        System.out.println("[INFO] Settings:");
        setRuns(settings[CI++]);
        setGame();
        setM(settings[CI++]);
        setLength(settings[CI++]);
        if (space.equals("grid")) {
            setWidth();
        }
        setN();
        setNeighType(settings[CI++]);
        setGenType(settings[CI++]);
        setER(settings[CI++]);
        setGens(settings[CI++]);
        setEWT(CI!=settings.length? settings[CI++]: "");
        setRP(CI!=settings.length? settings[CI++]: "");
        setRA(CI!=settings.length? settings[CI++]: "");
        setRT(CI!=settings.length? settings[CI++]: "");
        setRN1(CI!=settings.length? settings[CI++]: "");
        setRN2(CI!=settings.length? settings[CI++]: "");
        setPP(CI!=settings.length? settings[CI++]: "");
        setPS(CI!=settings.length? settings[CI++]: "");
        setEF(CI!=settings.length? settings[CI++]: "");
        setCost(CI!=settings.length? settings[CI++]: "");
        setFine(CI!=settings.length? settings[CI++]: "");
        setNU(CI!=settings.length? settings[CI++]: "");
        setPN1(CI!=settings.length? settings[CI++]: "");
        setPN2(CI!=settings.length? settings[CI++]: "");
        setV(CI!=settings.length? settings[CI++]: "");
        setLeeway(CI!=settings.length? settings[CI++]: "");
        setThreshold(CI!=settings.length? settings[CI++]: "");
        setEWL(CI!=settings.length? settings[CI++]: "");
        setROC(CI!=settings.length? settings[CI++]: "");
        setEvo(settings[CI++]);
        setSel(settings[CI++]);
        setRWT(CI!=settings.length? settings[CI++]: "");
        setEN(CI!=settings.length? settings[CI++]: "");
        setMut(CI!=settings.length? settings[CI++]: "");
        setMutRate(CI!=settings.length? settings[CI++]: "");
        setMutBound(CI!=settings.length? settings[CI++]: "");
        setUF(settings[CI++]);
        setWritePGenStats(CI!=settings.length? settings[CI++]: "");
        setWriteUGenStats(CI!=settings.length? settings[CI++]: "");
        setWriteKGenStats(CI!=settings.length? settings[CI++]: "");
        setWritePRunStats(CI!=settings.length? settings[CI++]: "");
        setWriteURunStats(CI!=settings.length? settings[CI++]: "");
        setWriteKRunStats(CI!=settings.length? settings[CI++]: "");
        setWriteRate(CI!=settings.length? settings[CI++]: "");
        setVP(CI!=settings.length? settings[CI++]: "");
        setVariations(CI!=settings.length? settings[CI++]: "");
    }



    /**
     * Initialises a lattice grid population of agents with randomly generated strategies.
     */
    public void initRandomPop() {
        pop = new Agent[N];
        Agent.setCount(0);
        int index = 0;
        switch (space) {
            case "grid" -> {
                for (int y=0;y<length;y++) {
                    for (int x=0;x<width;x++) {
                        Agent new_agent = null;
                        switch (game) {
                            case "UG" -> {
                                double p = ThreadLocalRandom.current().nextDouble();
                                double q = ThreadLocalRandom.current().nextDouble();
                                new_agent = new Agent(x, y, p, q);
                            }
                            case "DG" -> {
                                double p = ThreadLocalRandom.current().nextDouble();
                                new_agent = new Agent(x, y, p, 0.0);
                            }
                            case "PD" -> {}
                        }

                        // assign vindictiveness
                        switch (V) {
                            case "random" -> {
                                double v = ThreadLocalRandom.current().nextDouble();
                                new_agent.setV(v);
                            }
                            case "1" -> new_agent.setV(1); // all agents have max vindictiveness
                            case "0" -> new_agent.setV(0); // no agents punish.
                        }

                        pop[index] = new_agent;
                        index++;
                    }
                }
            }
            case "hex" -> {}
        }
    }



    public static void createDataFolders() {
        try {
            if (writeRate > 0) {
                Files.createDirectories(Paths.get(exp_path));
            }
            for (int i=1;i<=runs;i++) {
                if (writePGenStats || writeUGenStats || writeKGenStats || writePRunStats || writeURunStats || writeKRunStats) { // add run stat writing params to this check
                    Files.createDirectories(Paths.get(exp_path + "\\run" + i));
                }
                if (writePGenStats || writeUGenStats || writeKGenStats) {
                    Files.createDirectories(Paths.get(exp_path + "\\run" + i + "\\gen_stats"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * Prints path of experiment stats folder.
     */
    public static void printPath() {
        System.out.println("[INFO] Address of experimentation data: " + specific_path);
    }



    /**
     * Assigns adjacent neighbour to agent's neighbourhood.<br>
     * d denotes Manhattan distance for von Neumann neighbourhood or Chebyshev distance
     * for Moore neighbourhood.
    */
    public void adjacentNeigh(Agent a) {
//        ArrayList<Agent> omega_a = new ArrayList<>();
        double y = a.getY();
        double x = a.getX();
        for (int i=1;i<=neighRadius;i++) {
            double x_plus = adjustPosition(x, i, width);
            double x_minus = adjustPosition(x, -i, width);
            double y_plus = adjustPosition(y, i, length);
            double y_minus = adjustPosition(y, -i, length);
//            omega_a.add(findAgentByPos(y, x_plus));
            a.addNeighbour(findAgentByPos(y, x_plus));
            a.addNeighbour(findAgentByPos(y, x_minus));
            a.addNeighbour(findAgentByPos(y_plus, x));
            a.addNeighbour(findAgentByPos(y_minus, x));
            if (neighType.equals("dia")) {
                if (i > 1) {
                    double x_plus_minus = adjustPosition(x_plus, -1.0, width);
                    double x_minus_plus = adjustPosition(x_minus, 1.0, width);
                    double y_plus_minus = adjustPosition(y_plus, -1.0, length);
                    double y_minus_plus = adjustPosition(y_minus, 1.0, length);
                    a.addNeighbour(findAgentByPos(y_plus_minus, x_plus_minus));
                    a.addNeighbour(findAgentByPos(y_minus_plus, x_plus_minus));
                    a.addNeighbour(findAgentByPos(y_minus_plus, x_minus_plus));
                    a.addNeighbour(findAgentByPos(y_plus_minus, x_minus_plus));
                }
            }
            if (neighType.equals("Moore")) {
                a.addNeighbour(findAgentByPos(y_plus, x_plus));
                a.addNeighbour(findAgentByPos(y_minus, x_plus));
                a.addNeighbour(findAgentByPos(y_minus, x_minus));
                a.addNeighbour(findAgentByPos(y_plus, x_minus));
            }
        }
//        a.setOmega(omega_a);
//        a.setK(a.getOmega().size());
    }



    /**
     * Randomly assigns either uni-directional or bi-directional edges to agent.<br>
     * Assumes 2D square lattice grid population structure.
     */
    public void randomNeigh(Agent a, int size) {
        ArrayList<Agent> omega_a = a.getOmega();
        Set<Integer> IDs = new HashSet<>();
        while(IDs.size() < size) {
            int ID = ThreadLocalRandom.current().nextInt(N);
            IDs.add(ID);
        }
        for (int ID: IDs) {
            a.addNeighbour(findAgentByID(ID));
        }
//        a.setK(a.getOmega().size());
    }



    // assign all other agents to neighbourhood
    public void allPopNeigh(Agent a) {
        ArrayList <Agent> omega_a = a.getOmega();
        int ID = a.getID();
        for (int i=0;i<N;i++) {
            Agent agent2 = pop[i];
            int ID2 = agent2.getID();
            if (ID != ID2) {
                a.addNeighbour(agent2);
            }
        }
//        a.setK(a.getOmega().size());
    }



    public void calculateMeanP() {
        mean_p = 0;
        for (int i = 0; i < N; i++) {
            mean_p += pop[i].getP();
        }
        mean_p /= N;
    }

    public void calculateSigmaP() {
        sigma_p = 0;
        for (int i = 0; i < N; i++) {
            sigma_p += Math.pow(pop[i].getP() - mean_p, 2);
        }
        sigma_p = Math.pow(sigma_p / N, 0.5);
    }

    public void calculateVarianceP(){
        variance_p = 0;
        for (int i = 0; i < N; i++) {
            variance_p += Math.pow(pop[i].getP() - mean_p, 2);
        }
        variance_p /= N;
    }

    public void calculatePBins(){
        pBins = new int[] {
                0, // pBin1: p in [0, 0.2]
                0, // pBin2: p in (0.2, 0.4]
                0, // pBin3: p in (0.4, 0.6]
                0, // pBin4: p in (0.6, 0.8]
                0  // pBin5: p in (0.8, 1]
        };
//        pBin1 = 0;
//        pBin2 = 0;
//        pBin3 = 0;
//        pBin4 = 0;
//        pBin5 = 0;
        for (int i = 0; i < N; i++) {
            double p = pop[i].getP();
            if (p <= 0.2) {
//                pBin1++;
                pBins[0]++;
            } else if (p <= 0.4) {
//                pBin2++;
                pBins[1]++;
            } else if (p <= 0.6) {
//                pBin3++;
                pBins[2]++;
            } else if (p <= 0.8) {
//                pBin4++;
                pBins[3]++;
            } else {
//                pBin5++;
                pBins[4]++;
            }
        }
    }

    public void calculateSkewnessP(){
        int index_biggest_bin = 0;
        for (int i = 1; i < pBins.length; i++) {
            if (pBins[index_biggest_bin] < pBins[i]) {
                index_biggest_bin = i;
            }
        }
        int mode = pBins[index_biggest_bin];
        skewness_p = (mean_p - mode) / sigma_p;
    }

    public void calculateMeanU() {
        mean_u = 0;
        for (int i = 0; i < N; i++) {
            mean_u += pop[i].getU();
        }
        mean_u /= N;
    }

    public void calculateSigmaU() {
        sigma_u = 0;
        for (int i = 0; i < N; i++) {
            sigma_u += Math.pow(pop[i].getU() - mean_u, 2);
        }
        sigma_u = Math.pow(sigma_u / N, 0.5);
    }

    public void calculateMeanK() {
        mean_k = 0;
        for (int i = 0; i < N; i++) {
            mean_k += pop[i].getK();
        }
        mean_k /= N;
    }

    public void calculateSigmaK() {
        sigma_k = 0;
        for (int i = 0; i < N; i++) {
            sigma_k += Math.pow(pop[i].getK() - mean_k, 2);
        }
        sigma_k = Math.sqrt(sigma_k / N);
    }



    public void prepare() {
        for (int i=0;i<N;i++) {
            Agent a = pop[i];
            a.setU(0);
            a.setOldP(a.getP());
            max_p = 0.0;
        }
    }



    // writes IDs and positions of agents
    public void writePosData() {
        try {
            fw = new FileWriter(exp_path + "\\pos_data.csv", false);
            String s = "";
            for (int y=length-1;y>=0;y--) {
                for (int x=0;x<width;x++) {
                    Agent a = findAgentByPos(y,x);
                    int ID = a.getID();
                    s += ID;
                    if (x + 1 < width)
                        s += ",";
                }
                s += "\n";
            }
            fw.append(s);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * Finds an agent given the integer ID parameter.<br>
     * Currently not used.
     * @param ID of the agent to find
     * @return agent object with the given ID
     */
    public Agent findAgentByID(int ID) {
        Agent agent = null;
        for (int i=0;i<N;i++) {
            Agent agent2 = pop[i];
            int ID2 = agent2.getID();
            if (ID == ID2) {
                agent = agent2;
                break;
            }
        }

        return agent;
    }



    /**
     * find agent by position in the grid.<br>
     * e.g. if you call findAgentByPos(5, 2), it returns the agent at position (2, 5).
     * @param y y co-ordinate of the agent
     * @param x x co-ordinate of the agent
     * @return Agent object at position (x, y) in the grid.
     */
    public Agent findAgentByPos(double y, double x) {
        Agent agent = null;
        boolean found = false;
        int i=0;
        do{
            Agent agent2 =pop[i];
            double y2= agent2.getY();
            double x2= agent2.getX();
            if (y2==y && x2==x) {
                agent = agent2;
                found=true;
            }
            i++;
        }
        while(!found);
        return agent;
    }



    /**
     * Assign same strategy to cluster of agents within the grid.
     */
    public void injectStrategyCluster() {
        for (int i = 0; i < injSize; i++) {
            for (int j = 0; j < injSize; j++) {
                Agent agent = findAgentByPos(j, i);
                agent.setP(injP);
            }
        }
    }



    // adjust position with respect to periodic boundaries
    public double adjustPosition(double position, double adjustment, int max) {
        double new_position = (((position + adjustment) % max) + max) % max;
        return new_position;
    }



    /**
     * Find new neighbour by randomly choosing a neighbour of a neighbour.<br>
     * New neighbour cannot be rewirer or already a neighbour.<br>
     */
    public void RTLocal(Agent a, int num_rewires) {
//        ArrayList<Agent> pool = new ArrayList<>(); // pool of candidates the rewirer might rewire to
//        ArrayList<Agent> omega_a = a.getOmega(); // omega_a denotes neighbourhood of rewirer.
//        for (Agent b: omega_a) { // b denotes neighbour of rewirer
//            ArrayList<Agent> omega_b = b.getOmega(); // omega_b denotes neighbourhood of neighbour of rewirer.
//            for (Agent c: omega_b) { // c denotes neighbour of the neighbour of rewirer.
//                if (!c.equals(a)) { // do not add c to pool if c = a
//                    boolean add_to_pool = true; // boolean tracking whether c should be added to pool or not.
//                    for (Agent d : pool) { // d denotes candidate in pool
//                        if (c.equals(d)) { // if c = d, c must already be in the pool, therefore do not add c to the pool.
//                            add_to_pool = false;
//                            break;
//                        }
//                    }
//                    if (!add_to_pool) continue; // move on to next c if this c has already been ruled out of contention.
//                    for (Agent e : omega_a) { // e denotes neighbour of rewirer.
//                        if (c.equals(e)) { // if c = e, c must already be in omega_a, so you do not want to add c to pool.
//                            add_to_pool = false;
//                            break;
//                        }
//                    }
//                    if (add_to_pool) pool.add(c); // if deemed valid, add c to pool.
//                }
//            }
//        }
//        if (pool.isEmpty()) {
//            RTPop(a, num_rewires); // if pool empty, default to rewiring to a random agent in the pop.
//        } else{ // connect to local agent.
//            for (int rewires_done = 0; rewires_done < num_rewires; rewires_done++) {
//                Agent f = pool.get(ThreadLocalRandom.current().nextInt(pool.size())); // f denotes new neighbour of a.
//                omega_a.add(f); // connect a to f.
//                a.getEdgeWeights().add(1.0);
//                f.getOmega().add(a); // connect f to a.
//                f.getEdgeWeights().add(1.0);
//            }
//        }

        System.out.println("Out of commission for now...");
    }




    public void RTPop(Agent rewirer, int num_rewires_to_do) {
        // find new neighbour by randomly choosing agents from the population.
        for (int num_rewires_done = 0; num_rewires_done < num_rewires_to_do; num_rewires_done++) {
            ArrayList<Agent> omega_a = rewirer.getOmega();
            Agent new_neighbour = null;
            boolean same_agent = true;
            boolean already_neighbours = true;
            while(same_agent || already_neighbours) { // new neighbour cannot be rewirer or already neighbours with rewirer.
                new_neighbour = pop[ThreadLocalRandom.current().nextInt(pop.length)];
                same_agent = new_neighbour.equals(rewirer);
                if (omega_a.size() == 0) { // if a has no neighbours, connect to new neighbour
                    already_neighbours = false;
                } else {
                    for (Agent existing_neighbour : omega_a) {
                        already_neighbours = new_neighbour.equals(existing_neighbour);
                        if (already_neighbours) {
                            break;
                        }
                    }
                }
            }

            // connect rewirer to new neighbour.
            rewirer.addNeighbour(new_neighbour);
            rewirer.getEdgeWeights().add(1.0);
            new_neighbour.getOmega().add(rewirer);
            new_neighbour.getEdgeWeights().add(1.0);

        }
    }



    public static void writeSettings() {
        if (writeRate > 0) {
            String settings = "";
            if (exp == 1) {
                settings += "runs";
                settings += ",M";
                settings += ",space";
                settings += ",length";
                settings += ",width";
                settings += ",N";
                settings += ",neighType";
                settings += ",genType";
                settings += ER != 0? ",ER": "";
                settings += NIS != 0? ",NIS": "";
                settings += ",gens";
                settings += rounds != 0? ",rounds": "";
                settings += EWT.isEmpty()? "": ",EWT";

                settings += EWT.equals("rewire")? ",RP": "";
                settings += EWT.equals("rewire")? ",RA": "";
                settings += EWT.equals("rewire")? ",RT": "";
                settings += EWT.equals("rewire")? ",RN1": "";
                settings += EWT.equals("rewire") && (RA.equals("FD") || RA.equals("expo"))? ",RN2": "";

                settings += PP.isEmpty()? "": ",PP";
                settings += PS.isEmpty()? "": ",PS";
                settings += EF != 0.0? ",EF": "";
                settings += cost != 0.0? ",cost": "";
                settings += fine != 0.0? ",fine": "";
                settings += !PP.equals("")? ",NU": "";
                settings += PP.equals("noisy")? ",PN1": "";
//                settings += PN2 != 0.0? ",PN2": "";
                settings += !PP.equals("")? ",PN2": "";
                settings += V.equals("")? "": ",V";
                settings += leeway != 0.0? ",leeway": "";

//                settings += !PP.equals("")? ",threshold": ""; // should the check not be if we are using a threshold-based PP?

                settings += PP.equals("thresholds") || PP.equals("thresholdUpper") || PP.equals("thresholdLower")? ",threshold": "";

                settings += EWL.isEmpty()? "": ",EWL";
                settings += ROC == 0.0? "": ",ROC";
                settings += ",evo";
                settings += ",sel";
                settings += !RWT.isEmpty()? ",RWT": "";
                settings += RWT.equals("expo") || evo.equals("FD")? ",EN": "";
                settings += mut.isEmpty()? "": ",mut";
                settings += mut.isEmpty()? "": ",mutRate";
                settings += mutBound != 0.0? ",mutBound": "";
                settings += ",UF";
            }
            settings += "\n";
            settings += runs;
            settings += "," + M;
            settings += "," + space;
            settings += "," + length;
            settings += "," + width;
            settings += "," + N;
            settings += "," + neighType;
            settings += "," + genType;
            settings += ER != 0? "," + ER: "";
            settings += NIS != 0? "," + NIS: "";
            settings += "," + gens;
            settings += rounds != 0? "," + rounds: "";
            settings += EWT.isEmpty()? "": "," + EWT;

            settings += EWT.equals("rewire")? "," + RP: "";
            settings += EWT.equals("rewire")? "," + RA: "";
            settings += EWT.equals("rewire")? "," + RT: "";
            settings += EWT.equals("rewire")? "," + RN1: "";
            settings += EWT.equals("rewire") && (RA.equals("FD") || RA.equals("expo"))? "," + RN2: "";

            settings += PP.isEmpty()? "": "," + PP;
            settings += PS.isEmpty()? "": "," + PS;
            settings += EF != 0.0? "," + EF: "";
            settings += cost != 0.0? "," + cost: "";
            settings += fine != 0.0? "," + fine: "";
            settings += !PP.equals("")? "," + NU: "";
            settings += PP.equals("noisy")? "," + PN1: "";
//            settings += PN2 != 0.0? "," + PN2: "";
            settings += !PP.equals("")? "," + PN2: "";
            settings += V.equals("")? "": "," + V;
            settings += leeway != 0.0? "," + leeway: "";

//            settings += !PP.equals("")? "," + threshold: "";

            settings += PP.equals("thresholds") || PP.equals("thresholdUpper") || PP.equals("thresholdLower")? "," + threshold: "";

            settings += EWL.isEmpty()? "": "," + EWL;
            settings += ROC == 0.0? "": "," + ROC;
            settings += "," + evo;
            settings += "," + sel;
            settings += !RWT.isEmpty()? "," + RWT: "";
            settings += RWT.equals("expo") || evo.equals("FD")? "," + EN: "";
            settings += mut.isEmpty()? "": "," + mut;
            settings += mut.isEmpty()? "": "," + mutRate;
            settings += mutBound != 0.0? "," + mutBound: "";
            settings += "," + UF;
            try {
                fw = new FileWriter(specific_path + "\\" + "settings.csv", true);
                fw.append(settings);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
                exit(1);
            }
        }
    }



    /**
     * Write and calculate stats of series. Documents how the series went.
     * Reads and writes local data.
     */
    public static void writeSeriesStats() {
        if (writeRate > 0 && (writePRunStats || writeURunStats || writeKRunStats)) {
            String output = "";
            if (exp == 1) {
                if (writePRunStats) {
                    output += "mean avg p,sigma avg p,";
                }
                if (writeURunStats) {
                    output += "mean avg u,sigma avg u,";
                }
                if (writeKRunStats) {
                    output += "mean sigma k,";
                }
                if (punish) {
                    output += "mean num puns,";
                }
                output = removeTrailingComma(output);
            }
            double mean_avg_p = 0.0;
            double sigma_avg_p = 0.0;
            double mean_avg_u = 0.0;
            double sigma_avg_u = 0.0;
            double mean_sigma_k = 0.0;
            int mean_num_puns = 0;
            double[] mean_p_values = new double[runs];
            double[] mean_u_values = new double[runs];
            double[] sigma_k_values = new double[runs];
            int[] num_puns_values = new int[runs];
            try {
                fw = new FileWriter(specific_path + "\\series_stats.csv", true);
                br = new BufferedReader(new FileReader(exp_path + "\\exp_stats.csv"));
                br.readLine();
                for (int i=0;i<runs;i++) {
                    String row = br.readLine();
                    String[] row_contents = row.split(",");
                    int j = 0;
                    if (writePRunStats) {
                        mean_p_values[i] = Double.parseDouble(row_contents[j]);
                        j++; // move past mean p
                        j++; // move past sigma p
                        j++; // move past max p
                    }
                    if (writeURunStats) {
                        mean_u_values[i] = Double.parseDouble(row_contents[j]);
                        j++; // move past mean u
                        j++; // move past sigma u
                    }
                    if (writeKRunStats) {
                        sigma_k_values[i] = Double.parseDouble(row_contents[j]);
                        j++; // move past sigma k
                    }
                    if (punish) {
                        num_puns_values[i] = Integer.parseInt(row_contents[j]);
//                         j++; // enable to move past num puns
                    }
                }
                for (int i=0;i<runs;i++) {
                    if (writePRunStats) {
                        mean_avg_p += mean_p_values[i];
                    }
                    if (writeURunStats) {
                        mean_avg_u += mean_u_values[i];
                    }
                    if (writeKRunStats) {
                        mean_sigma_k += sigma_k_values[i];
                    }
                    if (punish) {
                        mean_num_puns += num_puns_values[i];
                    }
                }
//                mean_avg_p /= runs;
//                mean_avg_u /= runs;
                if (writePRunStats) {
                    mean_avg_p /= runs;
                }
                if (writeURunStats) {
                    mean_avg_u /= runs;
                }
                if (writeKRunStats) {
                    mean_sigma_k /= runs;
                }
                if (punish) {
                    mean_num_puns /= runs;
                }
                for (int i=0;i<runs;i++) {
                    sigma_avg_p += Math.pow(mean_p_values[i] - mean_avg_p, 2);
                    sigma_avg_u += Math.pow(mean_u_values[i] - mean_avg_u, 2);
                }
                sigma_avg_p = Math.pow(sigma_avg_p / runs, 0.5);
                sigma_avg_u = Math.pow(sigma_avg_u / runs, 0.5);
                output += "\n";
                if (writePRunStats) {
                    output += DF4.format(mean_avg_p) + "," + DF4.format(sigma_avg_p) + ",";
                }
                if (writeURunStats) {
                    output += DF4.format(mean_avg_u) + "," + DF4.format(sigma_avg_u) + ",";
                }
                if (writeKRunStats) {
                    output += DF4.format(mean_sigma_k) + ",";
                }
                if (punish) {
                    output += mean_num_puns + ",";
                }
                output = removeTrailingComma(output);
                fw.append(output);
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // display info in console
            if (writePRunStats || writeURunStats) {
                String console_output = "[STATS] exp: "+exp;
                if (writePRunStats) {
                    console_output += "; mean avg p: " + DF4.format(mean_avg_p);
                }
                if (writeURunStats) {
                    console_output += "; mean avg u: " + DF4.format(mean_avg_u);
                }
                if (writeKRunStats) {
                    console_output += "; mean sigma k: " + DF4.format(mean_sigma_k);
                }
                if (punish) {
                    console_output += "; mean num puns: " + mean_num_puns;
                }
                System.out.println(console_output);
            }
        }
    }


    public void updateUtility(Agent agent, double payoff) {
        switch (UF) {
            case "cumulative" -> agent.setU(agent.getU() + payoff);
            case "normalised" -> agent.setU(agent.getU() + (payoff / agent.getK()));
            case "divideBy8" -> agent.setU(agent.getU() + (payoff / 8));
        }
    }



    public Agent selRandomNeigh(Agent child) {
        Agent parent = child;
        int k = child.getK();
        if (k > 0) {
            int random_int = ThreadLocalRandom.current().nextInt(k);
//            parent = child.getOmega().get(random_int);
            try{
                parent = child.getOmega().get(random_int);
            }catch(IndexOutOfBoundsException e){
                System.out.println("BP");
            }
        }
        return parent;
    }



    public Agent selRandomPop() {
        return findAgentByID(ThreadLocalRandom.current().nextInt(N));
    }





    public void rewire(Agent a) {
        int num_rewires = 0;
        ArrayList<Agent> omega_a = a.getOmega();
        ArrayList<Double> weights = a.getEdgeWeights();

        boolean rewire = RP > ThreadLocalRandom.current().nextDouble();
        if (rewire) {

            // sever edges.
            for (int i = 0; i < a.getK(); i++) {
                Agent b = omega_a.get(i); // neighbour of a
                double w_ab = weights.get(i); // weight of edge from a to b
                double rewire_prob = 0; // probability of a disconnecting from b
                switch (RA) {
                    case "linear" -> rewire_prob = 1 - w_ab;
                    case "smoothstep" -> rewire_prob = 1 - (3 * Math.pow(w_ab, 2) - 2 * Math.pow(w_ab, 3));
                    case "smootherstep" -> rewire_prob = 1 - (6 * Math.pow(w_ab, 5) - 15 * Math.pow(w_ab, 4) + 10 * Math.pow(w_ab, 3));
                    case "on0" -> rewire_prob = w_ab == 0.0? 1.0: 0.0;
                    case "expo" -> rewire_prob = Math.exp(-RN2 * w_ab);
                    case "FD" -> rewire_prob = 1 / (1 + Math.exp((a.getU() - b.getU()) / RN2));
                    case "linearR" -> rewire_prob = w_ab < 1.0? ThreadLocalRandom.current().nextDouble(1 - w_ab): 0.0;
                }

                boolean rewire2 = rewire_prob > ThreadLocalRandom.current().nextDouble();
                if (RN1 > ThreadLocalRandom.current().nextDouble()) {
                    if (rewire2) {
                        rewire2 = false;
                    } else {
                        rewire2 = true;
                    }
                }

                if (rewire2) {
                    ArrayList<Agent> omega_b = b.getOmega();
                    for (int j = 0; j < b.getK(); j++) {
                        Agent neighbour = omega_b.get(j); // neighbour of b
                        if (neighbour.equals(a)) {
                            a.removeNeighbourViaIndex(i);
                            weights.remove(i);
                            b.removeNeighbourViaIndex(j);
                            b.getEdgeWeights().remove(j);
                            num_rewires++;

                            // this ensures that the loop function properly despite the size of the neighbourhood decreasing.
                            i--;

                            break;
                        }
                    }
                }
            }

            // form new edges.

            // this is what was causing the problem with guys trying to get more neighbours while having neighbourhoods
            // that were the pop excluding themselves. this was a loop when there was already a properly placed and
            // functioning loop in RTPop().
    //        for (int i = 0; i < num_rewires; i++) {

            if (num_rewires > 0) {
                switch (RT) {
                    case "pop" -> RTPop(a, num_rewires);
                    case "local" -> RTLocal(a, num_rewires);
                }
            }
        }
    }



    /**
     * evoUDN: evolution based on normalised utility difference.<br>
     * Probability to evolve equals utility difference divided by k.<br>
     * Child fitter than parent mean no chance.<br>
     * Fitter parent means greater probability.<br>
     * Greater child k means lesser probability.<br>
     * Inspired by cardinot2016optional.<br>
     */
    public void evoUDN(Agent child, Agent parent) {
        double prob_evolve = (parent.getU() - child.getU()) / child.getK();
        if (prob_evolve > ThreadLocalRandom.current().nextDouble()) {
            evoCopy(child, parent);
        }
    }



    /**
     * Write stats of experiment.
     * 1 file per exp.
     */
    public void writeExpStats() {
        if (writeRate > 0 && (writePRunStats || writeURunStats || writeKRunStats)) {
            String output = "";
            try {
                fw = new FileWriter(exp_path + "\\exp_stats.csv", true);
                br = new BufferedReader(new FileReader(run_path + "\\run_stats.csv"));
                String line = br.readLine();
                if (run == 1) {

                    // remove gen column
                    String[] row_contents = line.split(",");
                    String no_gen_str = "";
                    for (int i=1;i<row_contents.length;i++) {
                        no_gen_str += row_contents[i] + ",";
                    }
                    no_gen_str = removeTrailingComma(no_gen_str);
                    output += no_gen_str;
                }

                // skip to the last row of run stats
                for (int i = 0; i <= gens / writeRate; i++) {
                    line = br.readLine();
                }

                // remove gen column
                String[] row_contents = line.split(",");
                String no_gen_str = "";
                for (int i=1;i<row_contents.length;i++) {
                    no_gen_str += row_contents[i] + ",";
                }
                no_gen_str = removeTrailingComma(no_gen_str);
                output += "\n" + no_gen_str;
                fw.append(output);
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // display info in console
            if (writePRunStats || writeURunStats) {
                String console_output = "[STATS] exp: "+exp+"; run: "+run;
                if (writePRunStats) {
                    console_output += "; mean p: " + DF4.format(mean_p);
                }
                if (writeURunStats) {
                    console_output += "; mean u: " + DF4.format(mean_u);
                }
                if (punish) {
                    console_output += "; num puns: " + num_puns;
                }
                System.out.println(console_output);
            }
        }
    }



    public void calculateMaxP() {
        for (int i=0;i<N;i++) {
            double p = pop[i].getP();
            if (p > max_p)
                max_p = p;
        }
    }



    public Agent sel(Agent child) {
        Agent parent = null;
        switch (sel) {
            case "RW" -> parent = selRW(child);
            case "elitist" -> parent = selElitist(child);
            case "randomNeigh" -> parent = selRandomNeigh(child);
            case "randomPop" -> parent = selRandomPop();
//            case "crossover" -> crossover(child);
//            case "RW2" -> parent = selRW2(child);
            case "rank" -> parent = selRank(child);
        }
        return parent;
    }


    public void evo(Agent child, Agent parent) {
        switch (evo) {
            case "copy" -> evoCopy(child, parent);
//            case "approach" -> evoApproach(child, parent);
            case "copyFitter" -> evoCopyFitter(child, parent);
            case "UD" -> evoUD(child, parent);
            case "UDN" -> evoUDN(child, parent);
            case "FD" -> evoFD(child, parent);
        }
    }



    public void mut(Agent child) {
        switch (mut) {
            case "global" -> mutGlobal(child);
            case "local" -> mutLocal(child);
        }
    }



    /**
     * Calculate aggregate stats e.g. mean p.
     */
    public void calculateStats() {
        if (writePGenStats) {
            for (Agent agent : pop) {
                agent.calculateMeanPOmega();
            }
        }
        if (writePRunStats) {
            calculateMeanP();
            calculateSigmaP();
            calculateMaxP();
            calculateVarianceP();
            calculatePBins();
            calculateSkewnessP();
        }
        if (writeURunStats) {
            calculateMeanU();
            calculateSigmaU();
        }
        if (writeKRunStats) {
            calculateMeanK();
            calculateSigmaK();
        }
    }



    /**
     * Calls the gen and run stat writing functions every writeRate gens.<br><br>
     * This function is typically called at the end of a gen.<br><br>
     * Writes gen stats e.g. write p_x for all agents x in the pop at gen t.<br><br>
     * Writes run stats e.g. write mean(p) of the pop at gen t.<br><br>
     * Whether a stat is recorded depends on the writing params.<br>
     */
    public void writeGenAndRunStats() {
        if (writeRate != 0 && (gen == 0 || gen % writeRate == 0)) {
            if (writePGenStats || writeUGenStats || writeKGenStats) writeGenStats();
            if (writePRunStats || writeURunStats || writeKRunStats) writeRunStats();
        }
    }



    /**
     * Child copies parent if parent fitter.
     */
    public void evoCopyFitter(Agent child, Agent parent) {
        if (parent.getU() > child.getU()) {
            evoCopy(child, parent);
        }
    }



    /**
     * evoUDN: evolution based on utility difference.<br>
     */
    public void evoUD(Agent child, Agent parent) {
        double random_number = ThreadLocalRandom.current().nextDouble();
        double prob_evolve = parent.getU() - child.getU();
        if (random_number < prob_evolve)
            evoCopy(child, parent);
    }




    /**
     * Writes the attributes (p, u, k) of all agents in the pop at gen t.
     * 1 file per gen.
     */
    public void writeGenStats() {
        String s = "";
        if (writePGenStats) {
            s += "p,mean p omega,";
        }
        if (writeUGenStats) {
            s += "u,";
        }
        if (writeKGenStats) {
            s += "k,";
        }
        s = removeTrailingComma(s);
        for (Agent agent : pop) {
            s += "\n";
            if (writePGenStats) {
                s += DF4.format(agent.getP()) + "," + DF4.format(agent.getMeanPOmega()) + ",";
            }
            if (writeUGenStats) {
                s += DF4.format(agent.getU()) + ",";
            }
            if (writeKGenStats) {
                s += DF4.format(agent.getK()) + ",";
            }
            s = removeTrailingComma(s);
        }
        try {
            fw = new FileWriter(run_path + "\\gen_stats\\gen" + gen + ".csv");
            fw.append(s);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        }
    }


    /**
     * Write aggregate stats of a run at gen t.
     * 1 file per run.
     */
    public void writeRunStats() {
        String s = "";
        if (gen == 0) { // apply headings to file before writing data // stop extra headings from printing...
            s += "gen,";
            if (writePRunStats) {
//                s += "mean p,sigma p,max p,";
                s += "mean p,sigma p,max p,variance p,pBin1,pBin2,pBin3,pBin4,pBin5,skewness p,";
            }
            if (writeURunStats) {
                s += "mean u,sigma u,";
            }
            if (writeKRunStats) {
                s += "sigma k,";
            }
            if (punish) {
                s += "num puns,";
            }
            s = removeTrailingComma(s);
            s += "\n";
        }
        s += gen + ",";
        if (writePRunStats) {
//            s += DF4.format(mean_p) + "," + DF4.format(sigma_p) + "," + DF4.format(max_p) + ",";
            s += DF4.format(mean_p) + "," + DF4.format(sigma_p) + "," + DF4.format(max_p) + ","
                    + DF4.format(variance_p) + "," + pBins[0] + "," + pBins[1] + "," + pBins[2] + "," + pBins[3] + ","
                    + pBins[4] + "," + DF4.format(skewness_p) + ",";
        }
        if (writeURunStats) {
            s += DF4.format(mean_u) + "," + DF4.format(sigma_u) + ",";
        }
        if (writeKRunStats) {
            s += DF4.format(sigma_k) + ",";
        }
        if (punish) {
            s += num_puns + ",";
        }
        s = removeTrailingComma(s);
        s+="\n";
        try {
            fw = new FileWriter(run_path + "\\run_stats.csv", true);
            fw.append(s);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        }
    }



    /**
     * If last char of string is a comma, get rid of it.
      */
    public static String removeTrailingComma(String s) {
        if (s.length() > 0 && s.charAt(s.length() - 1) == ',') {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }


    public static void exit(int status) {
        if (status == 0) {
            System.out.println("[INFO] Normal termination...");
        } else if (status == 1) {
            System.out.println("[INFO] Terminating due to error...");
        }
        System.out.println("shutting down at " + LocalDateTime.now());
        Runtime.getRuntime().exit(status);
    }


    /**
     * probability of selection does not depend directly on the fitness
     * of an individual (like with RW sel)
     * but rather their fitness rank within the population/neighbourhood.
     * compare fitness of candidates.
     * higher rank ==> higher probability of being selected.
     * candidates for selection are child and its neighbours.
      */
    public Agent selRank(Agent child) {
        // get candidates
        Agent parent = child; // if no neighbour is selected, child is parent by default
        ArrayList <Agent> pool = new ArrayList<>(child.getOmega()); // pool of candidates for parent
        pool.add(child);

        // get utilities of candidates
        int size = pool.size();
        double[] utilities = new double[size];
        for (int i=0;i<size;i++) {
            utilities[i] = pool.get(i).getU();
        }

        // determine ranks
        int[] ranks = new int[size];
        for (int i=0;i<size;i++) {
            ranks[i] = 1;
        }
        for (int i=0;i<size;i++) {
            for (int j=0;j<size;j++) {
                if (i != j) {
                    if (utilities[i] > utilities[j]) {
                        ranks[i]++;
                    }
                }
            }
        }

        // select parent.
        // probability of selection = rank / T_n
        // (where T_n denotes nth triangular number and n denotes candidate pool size).
        // lowest rank: 1.
        double total = 0;
        for (int i = 0; i < size; i++) {
            total += ranks[i];
        }
        double random_double = ThreadLocalRandom.current().nextDouble(total);
        double tally = 0;
        for (int i = 0; i < size - 1; i++) {
            tally += ranks[i];
            if (random_double < tally) {
                parent = pool.get(i); // select candidate as parent
                break;
            }
        }
        return parent;
    }



    /**
     * Fermi-Dirac evolution function.
     * requires a parameter K for selection intensity.
     * get value of K from noise env param.
     * fitter parent ==> higher chance of evolution.
     * the closer K is to infinity, the more the child accounts for the parent's fitness.
     * the closer K is to 0.0, the less the child accounts for fitness,
     * in which case unfit individuals are about as likely to be imitated as fit individuals.
     * evolve_probability = 1 / (1 + Math.exp(child.getU() - parent.getU() / K));
     */
    public void evoFD(Agent child, Agent parent) {
        double random_double = ThreadLocalRandom.current().nextDouble();
        double K = EN;
        double u_x = child.getU();
        double u_y = parent.getU();
        double u_diff = u_x - u_y;
        double divided_by_K = u_diff / K;
        double exponential = Math.exp(divided_by_K);
        double denominator = 1 + exponential;
        double evolve_probability = 1 / denominator;
        if (random_double < evolve_probability) {
            evoCopy(child, parent);
        }
    }




    public void punish(Agent a) {
        ArrayList<Double> weights = a.getEdgeWeights();
        ArrayList<Agent> omega_a = a.getOmega();
        for (int i=0;i<a.getK();i++) {
            Agent b = omega_a.get(i);
            double w_ab = 0.0;
            if (EWT.equals("punish")) {
                w_ab = weights.get(i);
            }
            double u_a = a.getU();
            double u_b = b.getU();
            double v_a = a.getV();
            double p_a = a.getP();
            double p_b = b.getP();
//            double punish_prob = calculatePunishProb(w_ab, u_a, u_b, v_a);
            double punish_prob = calculatePunishProb(w_ab, u_a, u_b, v_a, p_a, p_b);
            double random_double = ThreadLocalRandom.current().nextDouble();
            boolean punish = punish_prob > random_double;
            if (PN2 != 0.0) {
                double random_double2 = ThreadLocalRandom.current().nextDouble();
                if (PN2 > random_double2) {
                    if (punish) {
                        punish = false;
                    } else {
                        punish = true;
                    }
                }
            }
            if (punish) {
                calculatePunishSeverity(a,b,u_a,u_b,w_ab);
                num_puns++;
            }
        }
    }



    public static void setCost(String value) {
        boolean set = false;
        switch (PP) {
            case "sweetspot" -> set = true;
        }
        switch (PS) {
            case "normal", "weighted", "utility", "lowNoise", "mediumNoise", "highNoise", "EF", "EF+ER", "percent", "smallerPercent" -> set = true;
        }
        if (set) {
            try {
                cost = Double.parseDouble(value);
                System.out.println("cost = "+cost);
            } catch (NumberFormatException e) {
                System.out.println("invalid cost: must be a double");
                exit(1);
            }
        }
    }

    public static void setFine(String value) {
        boolean set = false;
        switch (PP) {
            case "sweetspotCostFine" -> set = true;
        }
        switch (PS) {
            case "normal", "weighted", "utility" -> set = true;
        }
        if (set) {
            try {
                fine = Double.parseDouble(value);
                System.out.println("fine = " + fine);
            } catch (NumberFormatException e) {
                System.out.println("invalid fine: must be a double");
                exit(1);
            }
        }
    }

    public static void setPP(String value) {
        boolean set = false;
        switch (EWT) {
            case "punish" -> {
                switch (value) {
                    case "linear", "smoothstep", "smootherstep", "on0", "noisy", "thresholds", "thresholdUpper", "thresholdLower", "Uv1", "Uv2", "sweetspot" -> set = true;
                }
            }
            default -> {
                switch (value) {
                    case "P", "PD", "leeway" -> set = true;
                }
            }
        }
        if (set) {
            switch (value) {
                case "linear", "smoothstep", "smootherstep", "on0", "noisy", "thresholds", "thresholdUpper", "thresholdLower", "Uv1", "Uv2", "sweetspot", "P", "PD", "leeway" -> {
                    PP = value;
                    System.out.println("PP = "+PP);
                    punish = true;
                }
                default -> {
                    System.out.println("invalid PP");
                    exit(1);
                }
            }
        }
    }

    public static void setMutRate(String value) {
        switch (mut) {
            case "global", "local" -> {
                try {
                    mutRate = Double.parseDouble(value);
                    System.out.println("mutRate = "+mutRate);
                } catch (NumberFormatException e) {
                    System.out.println("invalid mutRate");
                    exit(1);
                }
                if (mutRate < 0 || mutRate > 1) {
                    System.out.println("invalid mutRate: must be within the interval [0, 1].");
                    exit(1);
                }
            }
        }
    }

    // if EWL is not PROC or UROC, nothing happens.
    public static void setROC(String value) {
        switch (EWL) {
            case "PROC", "UROC" -> { // value must be valid if EWL is PROC or UROC.
                try {
                    ROC = Double.parseDouble(value);
                    System.out.println("ROC = "+ROC);
                } catch (NumberFormatException e) {
                    System.out.println("invalid ROC: must be a double");
                    exit(1);
                }
                if (ROC < 0 || ROC > 1) {
                    System.out.println("invalid ROC: must be within the interval [0, 1].");
                    exit(1);
                }
            }
        }
    }

    public static void setGens(String value) {
        try {
            gens = Integer.parseInt(value);
            System.out.println("gens = "+gens);
        } catch (NumberFormatException e) {
            System.out.println("invalid gens: must be an integer");
            exit(1);
        }
        if (gens < 1) {
            System.out.println("invalid gens: must be >= 1.");
            exit(1);
        }
    }

    public static void setER(String value) {
        switch (genType) {
            case "ER" -> {
                try {
                    ER = Integer.parseInt(value);
                    System.out.println("ER = "+ER);
                } catch (NumberFormatException e) {
                    System.out.println("invalid ER: must be an integer");
                    exit(1);
                }
                if (ER < 1) {
                    System.out.println("invalid ER: must be >= 1.");
                    exit(1);
                }
            }
        }
    }

    public static void setLength(String value) {
        try {
            length = Integer.parseInt(value);
            System.out.println("length = "+length);
        } catch (NumberFormatException e) {
            System.out.println("invalid length: must be an integer");
            exit(1);
        }
        if (length < 3) {
            System.out.println("invalid length: must be >= 3.");
            exit(1);
        }
    }

    public static void setRuns(String value) {
        try {
            runs = Integer.parseInt(value);
            System.out.println("runs = "+runs);
        } catch (NumberFormatException e) {
            System.out.println("invalid runs: must be an integer");
            exit(1);
        }
        if (runs < 1) {
            System.out.println("invalid runs: must be >= 1.");
            exit(1);
        }
    }

    public static void setMutBound(String value) {
        switch (mut) {
            case "local" -> {
                try {
                    mutBound = Double.parseDouble(value);
                    System.out.println("mutBound = "+mutBound);
                } catch (NumberFormatException e) {
                    System.out.println("invalid mutBound");
                    exit(1);
                }
                if (mutBound <= 0 || mutBound > 1) {
                    System.out.println("invalid mutBound: must be within the interval (0, 1].");
                    exit(1);
                }
            }
        }
    }

    public static void setMut(String value) {
        switch (value) {
            case "global", "local" -> {
                mut = value;
                System.out.println("mut = "+mut);
            }
            default -> System.out.println("no mut");
        }
    }

    public static void setEWT(String value) {
        switch (value) {
//            case "prevention", "punish", "rewire" -> {
            case "prevention", "rewire" -> {
                EWT = value;
                Agent.setEWT(EWT);
                System.out.println("EWT = "+EWT);
            }
            case "punish" -> {
                EWT = value;
                Agent.setEWT(EWT);
                System.out.println("EWT = "+EWT);
                punish = true;
            }
            default -> System.out.println("no EWT");
        }
    }

    public static void setEWL(String value) {
        boolean set = false;
        switch (EWT) {
            case "punish", "prevention", "rewire" -> set = true;
        }
        if (set) {
            switch (value) {
                case "PROC", "UROC", "PD", "UD", "PDhalf", "PDR", "PDRv2", "PDdouble", "PDtriple", "test1", "test2", "test3", "test4", "test5", "test6", "test7", "test8", "test9", "test10" -> {
                    EWL = value;
                    System.out.println("EWL = "+EWL);
                }
                default -> {
                    System.out.println("invalid EWL");
                    exit(1);
                }
            }
        }
    }

    public static void setSel(String value) {
        switch (value) {

            // case where value is valid.
            case "RW", "elitist", "randomNeigh", "randomPop", "rank" -> {
                sel = value;
                System.out.println("sel = "+sel);
            }

            // case where value is invalid.
            default -> {
                System.out.println("invalid sel");
                exit(1);
            }
        }
    }

    public static void setRWT(String value) {
        switch (sel) {
            case "RW" -> {
                switch (value) {
                    case "expo", "normal" -> {
                        RWT = value;
                        System.out.println("RWT = "+RWT);
                    }
                    default -> {
                        System.out.println("invalid RWT");
                        exit(1);
                    }
                }
            }
        }
    }

    // the func leaves room for more sel funcs utilising noise, including one's not based on RW sel.
    public static void setEN(String value) {
        boolean assign = false;
        switch (sel) {
            case "RW" -> {
                switch (RWT) {
                    case "expo" -> assign = true;
                }
            }
        }
        switch (evo) {
            case "FD" -> assign = true;
        }
        if (assign) {
            try {
                EN = Double.parseDouble(value);
                System.out.println("EN = "+EN);
            } catch (NumberFormatException e) {
                System.out.println("invalid EN: must be a double");
                exit(1);
            }
        }
    }

    public static void setUF(String value) {
        switch (value) {
            case "cumulative", "normalised", "divideBy8" -> {
                UF = value;
                System.out.println("UF = "+UF);
            }
            default -> {
                System.out.println("invalid UF");
                exit(1);
            }
        }

    }

    // write file with VP data.
    public static void writeVP() {
        if (writeRate > 0) {
            String output = "";

            // includes the column header for the variable parameter.
            if (exp == 1) {
                output += VP;
            }

            // includes the current value of the variable parameter.
            switch (VP) {
                case "ER" -> output += "\n" + ER;
                case "NIS" -> output += "\n" + NIS;
                case "ROC" -> output += "\n" + ROC;
                case "length" -> output += "\n" + length;
                case "RP" -> output += "\n" + RP;
                case "gens" -> output += "\n" + gens;
                case "EWL" -> output += "\n" + EWL;
                case "EWT" -> output += "\n" + EWT;
                case "RA" -> output += "\n" + RA;
                case "RT" -> output += "\n" + RT;
                case "sel" -> output += "\n" + sel;
                case "evo" -> output += "\n" + evo;
                case "EN" -> output += "\n" + EN;
                case "mutRate" -> output += "\n" + mutRate;
                case "mutBound" -> output += "\n" + mutBound;
                case "UF" -> output += "\n" + UF;
                case "PP" -> output += "\n" + PP;
                case "cost" -> output += "\n" + cost;
                case "fine" -> output += "\n" + fine;
                case "NU" -> output += "\n" + NU;
                case "PN1" -> output += "\n" + PN1;
                case "PN2" -> output += "\n" + PN2;
                case "EF" -> output += "\n" + EF;
                case "leeway" -> output += "\n" + leeway;
                case "threshold" -> output += "\n" + threshold;
                case "RN1" -> output += "\n" + RN1;
                case "RN2" -> output += "\n" + RN2;
            }

            // create the file and write the data.
            try {
                fw = new FileWriter(specific_path + "\\" + "VP.csv", true);
                fw.append(output);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
                exit(1);
            }
        }
    }

    public static void setVP(String value) {
        switch (value) {
            case    "ER",
                    "ROC",
                    "length",
                    "RP",
                    "gens",
                    "EWL",
                    "RA",
                    "RT",
                    "evo",
                    "sel",
                    "EN",
                    "mut",
                    "mutRate",
                    "mutBound",
                    "UF",
                    "PP",
                    "cost",
                    "fine",
                    "NU",
                    "PN1",
                    "PN2",
                    "M",
                    "EF",
                    "leeway",
                    "threshold",
                    "RN1",
                    "RN2"
                    -> {
                VP = value;
                System.out.println("VP = "+VP);
            }
            default -> System.out.println("[INFO] No parameter variation scheduled with this configuration.");
        }
    }

    public void getNeighbours(Agent agent) {
        switch (neighType) {
            case "VN", "Moore", "dia" -> adjacentNeigh(agent);
            case "random" -> randomNeigh(agent, neighSize);
            case "all" -> allPopNeigh(agent);
        }
    }

    public void ER() {
        rounds = 0;
        for (gen = 1; gen <= gens; gen++) { // gens
            for (int round = 0; round < ER; round++) { // rounds
                for (int i = 0; i < N; i++) {
                    play(pop[i]); // play DG
                }
                if (!EWL.isEmpty()) {
                    for (int i = 0; i < N; i++) {
                        EWL(pop[i]); // edge weight learning
                    }
                }
                rounds++;
            }
            switch (EWT) {
                case "rewire" -> {
                    for (int i=0;i<N;i++) {
                        rewire(pop[i]);
                    }
                }
            }
            if (punish) {
                for (int i=0;i<N;i++) {
                    punish(pop[i]);
                }
            }
            for (int i=0;i<N;i++) {
                Agent child = pop[i];
                Agent parent = sel(child);
                if (!child.equals(parent)) {
                    evo(child, parent);
                    mut(child);
                }
            }
            calculateStats(); // calculate stats at end of gen
            writeGenAndRunStats(); // write gen and run stats at end of gen
            prepare(); // reset certain attributes at end of gen
        }
    }

    public void MCv1() {
//        for (gen = 1; gen <= gens; gen++) { // MC outer loop
//            for (int i = 0; i < N; i++) play(pop[i]);
//            for (int i=0;i<N;i++) updateUtility(pop[i]);
//            for (int i = 0; i < NIS; i++) { // MC inner loop
//                Agent child = selRandomPop();
//                EWL(child); // EWL inside or outside inner step loop?
//                if (EWT.equals("rewire")) rewire(child); // rewire if applicable
//                Agent parent = sel(child);
//                if (evo(child, parent))
//                    mut(child);
//            }
//            calculateStats();
//            writeGenAndRunStats();
//            prepare(); // reset certain attributes at end of gen
//        }

        System.out.println("[INFO] MCv1() has been decommissioned for now...");
    }

    public void MCv2() {
//        for (gen=1;gen<=gens;gen++) {
//            for (int i=0;i<NIS;i++) {
//                int random_int = ThreadLocalRandom.current().nextInt(N);
//                Agent agent = findAgentByID(random_int);
//                play(agent);
//                Agent parent = selRandomNeigh(agent);
//                evo(agent, parent);
//            }
//        }

        System.out.println("[INFO] MCv2() has been decommissioned for now...");
    }

    // one agent may evolve per gen.
    public void oneGuyEvo() {
//        for (gen=1;gen<=gens;gen++) {
//            for (int i=0;i<N;i++) {
//                play(pop[i]); // play DG
//            }
//            if (!EWL.isEmpty()) {
//                for (int i=0;i<N;i++) {
//                    EWL(pop[i]);
//                }
//            }
//            switch (EWT) {
//                case "rewire" -> {
//                    for (int i=0;i<N;i++) {
//                        rewire(pop[i]);
//                    }
//                }
//                case "punish" -> {
//                    for (int i=0;i<N;i++) {
//                        punish(pop[i]);
//                    }
//                }
//            }
//            int random_int = ThreadLocalRandom.current().nextInt(N);
//            Agent child = findAgentByID(random_int);
//            Agent parent = sel(child);
//            if (!child.equals(parent)) {
//                evo(child, parent);
//                mut(child);
//            }
//            calculateStats(); // calculate stats at end of gen
//            writeGenAndRunStats(); // write gen and run stats at end of gen
//            prepare(); // reset certain attributes at end of gen
//        }

        System.out.println("oneGuyEvo() is currently decommissioned...");
    }

    public static void setVariations(String value) {
        if (!value.isEmpty()) {

            // automatically generate variations using start, end, step.
            // INCOMPLETE... (NOTE TO SELF: SEE STRINGSPLIT TEST FILE FOR WIP)
//            if (value.startsWith("!")) {}


            variations = value.split(";");
            for (int i=0;i<variations.length;i++) {
                System.out.println("variation"+(i+1)+"="+variations[i]);
            }
            exps = variations.length + 1;
        }
    }

    public static void setWritePGenStats(String value) {
        try {
            if (value.equals("1")) {
                writePGenStats = true;
                System.out.println("writePGenStats = "+writePGenStats);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[INFO] Will not record p gen stats.");
        }
    }

    public static void setWriteUGenStats(String value) {
        try {
            if (value.equals("1")) {
                writeUGenStats = true;
                System.out.println("writeUGenStats="+writeUGenStats);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[INFO] Will not record u gen stats.");
        }
    }

    public static void setWriteKGenStats(String value) {
        try {
            if (value.equals("1")) {
                writeKGenStats = true;
                System.out.println("writeKGenStats="+writeKGenStats);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[INFO] Will not record k gen stats.");
        }
    }

    public static void setWritePRunStats(String value) {
        try {
            if (value.equals("1")) {
                writePRunStats = true;
                System.out.println("writePRunStats = "+writePRunStats);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[INFO] Will not record p run stats.");
        }
    }

    public static void setWriteURunStats(String value) {
        try {
            if (value.equals("1")) {
                writeURunStats = true;
                System.out.println("writeURunStats = "+writeURunStats);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[INFO] Will not record u run stats.");
        }
    }

    public static void setWriteKRunStats(String value) {
        try {
            if (value.equals("1")) {
                writeKRunStats = true;
                System.out.println("writeKRunStats = "+writeKRunStats);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[INFO] Will not record k run stats.");
        }
    }

    public static void setWriteRate(String value) {
        if (writePGenStats || writeUGenStats || writeKGenStats || writePRunStats || writeURunStats || writeKRunStats) {
            try {
                writeRate = Integer.parseInt(value);
                System.out.println("writeRate = "+writeRate);
            } catch (NumberFormatException e) {
                System.out.println("invalid writeRate");
                exit(1);
            }
        }
    }

    public static void setWidth() {
        width = length;
//        System.out.println("width="+width);
    }

    public static void setN() {
        N = length * width;
//        System.out.println("N="+N);
    }

    // this function just initialises Agent.game.
    public static void setGame() {
        Agent.setGame(game);
//        System.out.println("game="+game);
    }

    public static void setRP(String value) {
        switch (EWT) {
            case "rewire" -> {
                try {
                    RP = Double.parseDouble(value);
                    System.out.println("RP = "+RP);
                } catch (NumberFormatException e) {
                    System.out.println("invalid RP: must be a double");
                    exit(1);
                }
                if (RP < 0 || RP > 1) {
                    System.out.println("invalid RP: must be within the interval [0, 1].");
                    exit(1);
                }
            }
        }
    }

    public static void setRA(String value) {
        switch (EWT) {
            case "rewire" -> {
                switch (value) {
                    case "smoothstep", "smootherstep", "linear", "on0", "FD", "expo", "linearR" -> {
                        RA = value;
                        System.out.println("RA = "+RA);
                    }
                    default -> {
                        System.out.println("invalid RA");
                        exit(1);
                    }
                }
            }
        }
    }

    public static void setRT(String value) {
        switch (EWT) { // value must be valid if EWT = rewire.
            case "rewire" -> {
                switch (value) {

                    // case where value is valid.
                    case "local", "pop" -> {
                        RT = value;
                        System.out.println("RT = "+RT);
                    }

                    // case where value is invalid.
                    default -> {
                        System.out.println("invalid RT");
                        exit(1);
                    }
                }
            }
        }
    }

    public static void setEvo(String value) {
        switch (value) {

            // case where value is valid.
            case "copy",
                    "copyFitter",
                    "FD",
//                    "approach",
                    "UD",
                    "UDN" -> {
                evo = value;
                System.out.println("evo = "+evo);
            }

            // case where value is invalid.
            default -> {
                System.out.println("invalid evo");
                exit(1);
            }
        }
    }

    public static void setGenType(String value) {
        switch (value) {

            // case where value is valid.
            case "ER", "MCv1", "MCv2", "oneGuyEvo"-> {
                genType = value;
                System.out.println("genType = "+genType);
            }

            // case where value is invalid.
            default -> {
                System.out.println("invalid genType");
                exit(1);
            }
        }
    }

    public static void setNU(String value) {
        if (punish) {
            switch (value) {
                case "1" -> {
                    NU = true;
                    Agent.setNU(NU);
                    System.out.println("NU = "+NU);
                }
                case "0" -> {
                    NU = false;
                    Agent.setNU(NU);
                    System.out.println("NU = "+NU);
                }
                default -> {
                    System.out.println("invalid NU");
                    exit(1);
                }
            }
        }

    }

    public static void setNeighType(String value) {
        switch (value) {

            // case where value is valid.
            case "VN", "Moore" -> {
                neighType = value;
                System.out.println("neighType = "+neighType);
            }

            // case where value is invalid
            default -> {
                System.out.println("invalid neighType");
                exit(1);
            }
        }
    }

    public static void setPN1(String value) {
        switch (PP) {
            case "noisy" -> {
                try {
                    PN1 = Double.parseDouble(value);
                    System.out.println("PN1="+PN1);
                } catch (NumberFormatException e) {
                    System.out.println("invalid PN1: must be a double");
                    exit(1);
                }
                if (PN1 < 0 || PN1 > 1) {
                    System.out.println("invalid PN1: must be within the interval [0, 1].");
                    exit(1);
                }
            }
        }
    }

    public static void setPN2(String value) {
        if (punish) {
            try {
                PN2 = Double.parseDouble(value);
                System.out.println("PN2 = "+PN2);
            } catch (NumberFormatException e) {
                System.out.println("invalid PN2: must be a double");
                exit(1);
            }
            if (PN2 < 0 || PN2 > 1) {
                System.out.println("invalid PN2: must be within the interval [0, 1].");
                exit(1);
            }
        }
    }

    public static void setM(String value) {
        switch (game) {
            case "UG", "DG" -> {
                try {
                    M = Double.parseDouble(value);
                    System.out.println("M = "+M);
                } catch (NumberFormatException e) {
                    System.out.println("invalid M: must be a double");
                    exit(1);
                }
                if (M < 0) {
                    System.out.println("invalid M: must be greater than 0");
                }
            }
        }
    }

    public static void setEF(String value) {
        boolean set = false;
        switch (PS) {
            case "EF", "EF+ER" -> set = true;
        }
        if (set) {
            try {
                EF = Double.parseDouble(value);
                System.out.println("EF = " + EF);
            } catch (NumberFormatException e) {
                System.out.println("invalid EF: must be a double");
                exit(1);
            }
            if (EF < 0) {
                System.out.println("invalid EF: must be greater than or equal to 0");
            }
        }
    }

//    public double calculatePunishProb(double w_ab, double u_a, double u_b, double v_a) {
    public double calculatePunishProb(double w_ab, double u_a, double u_b, double v_a, double p_a, double p_b) {
        double punish_prob = 0.0;
        switch (PP) {
            case "linear" -> punish_prob = 1 - w_ab;
            case "smoothstep" -> punish_prob = 1 - (3 * Math.pow(w_ab, 2) - 2 * Math.pow(w_ab, 3));
            case "smootherstep" -> punish_prob = 1 - (6 * Math.pow(w_ab, 5) - 15 * Math.pow(w_ab, 4) + 10 * Math.pow(w_ab, 3));
            case "on0" -> punish_prob = w_ab == 0.0? 1.0: 0.0;
            case "noisy" -> punish_prob = (1 - w_ab) * (1 - PN1);
//            case "threshold" -> {
            case "thresholds" -> {
                if (w_ab >= 1 - threshold) {
                    punish_prob = 0.0;
                } else if (w_ab <= threshold) {
                    punish_prob = 1.0;
                } else {
                    punish_prob = 1 - w_ab;
                }
            }
            case "thresholdUpper" -> {
                if (w_ab >= 1 - threshold) {
                    punish_prob = 0;
                } else {
                    punish_prob = 1 - w_ab;
                }
            }
            case "thresholdLower" -> {
                if (w_ab <= threshold) {
                    punish_prob = 1;
                } else {
                    punish_prob = 1 - w_ab;
                }
            }
            case "Uv1" -> {
                if (u_a < u_b) {
                    punish_prob = 1;
                } else {
                    punish_prob = 0;
                }
            }
            case "Uv2" -> {
                if (2 * u_a < u_b) { // if b has more than two times the utility of a, a is guaranteed to punish b.
                    punish_prob = 1;
                } else if (u_a > 2 * u_b) { // if a has more than two times the utility of b, a is guaranteed to not punish b.
                    punish_prob = 0;
                } else { // otherwise, punish prob is linear.
                    punish_prob = 1 - w_ab;
                }
            }
            case "sweetspot" -> { // if the cost is not too high and the fine is not too low, punish prob is linear.
                double x = 2; // i might make this customisable later.
                if (cost > u_a / x || x * fine < u_b) {
                    punish_prob = 0;
                } else {
                    punish_prob = 1 - w_ab;
                }
            }
            case "P" -> {
                if (p_a > p_b) { // a punish b if a fairer than b
                    punish_prob = 1;
                } else {
                    punish_prob = 0;
                }
            }
            case "PD" -> punish_prob = p_a - p_b;
            case "leeway" -> {
                if (p_a > p_b + leeway) { // a punishes b if b is not close enough to being as fair as a.
                    punish_prob = 1;
                } else {
                    punish_prob = 0;
                }
            }
        }
        punish_prob = punish_prob * v_a; // account for vindictiveness
        return punish_prob;
    }
    
    public static void setPS(String value) {
        boolean set = false;
        switch (EWT) {
            case "punish" -> set = true;
        }
        switch (PP) {
            case "P", "PD", "leeway" -> set = true;
        }
        if (set) {
            switch (value) {
                case "normal", "weighted", "utility", "lowNoise", "mediumNoise", "highNoise", "EF", "EF+ER", "percent", "smallerPercent" -> {
                    PS = value;
                    System.out.println("PS = "+PS);
                }
                default -> {
                    System.out.println("invalid PS");
                    exit(1);
                }
            }
        }
    }

    public static void setV(String value) {
        if (punish) {
            switch (value) {
                case "random", "1", "0" -> {
                    V = value;
                    Agent.setStaticV(V);
                    System.out.println("V = "+V);
                }
                default -> {
                    System.out.println("invalid V");
                    exit(1);
                }
            }
        }
    }

    // calculates punishment severity and inflicts costs and fines.
    public void calculatePunishSeverity(Agent a, Agent b, double u_a, double u_b, double w_ab) {
        switch (PS) {
            case "normal" -> {
                a.setU(u_a - cost);
                b.setU(u_b - fine);
            }
            case "weighted" -> { // lower w_ab ==> higher cost and fine.
                a.setU(u_a - cost * (1 - w_ab));
                b.setU(u_b - fine * (1 - w_ab));
            }
            case "utility" -> {
                if (2 * u_a < u_b) { // if b has more than two times the utility of a, double the cost and fine.
                    a.setU(u_a - 2 * cost);
                    b.setU(u_b - 2 * fine);
                } else if (u_a > 2 * u_b) { // if a has more than two times the utility of b, halve the cost and fine.
                    a.setU(u_a - cost / 2);
                    b.setU(u_b - fine / 2);
                } else { // otherwise, revert to PS = normal.
                    a.setU(u_a - cost);
                    b.setU(u_b - fine);
                }
            }
            case "lowNoise" -> {
                a.setU(u_a - cost);
                double random_double3 = ThreadLocalRandom.current().nextDouble(2, 4);
                double noisy_fine = cost * random_double3;
                b.setU(u_b - noisy_fine);
            }
            case "mediumNoise" -> {
                a.setU(u_a - cost);
                double random_double3 = ThreadLocalRandom.current().nextDouble(1, 5);
                double noisy_fine = cost * random_double3;
                b.setU(u_b - noisy_fine);
            }
            case "highNoise" -> {
                a.setU(u_a - cost);
                double random_double3 = ThreadLocalRandom.current().nextDouble(0, 6);
                double noisy_fine = cost * random_double3;
                b.setU(u_b - noisy_fine);
            }
            case "EF" -> { // use the enhancement factor parameter to determine the fine.
                a.setU(u_a - cost);
                b.setU(u_b - cost * EF);
            }
            case "EF+ER" -> { // uses ER and EF to calculate cost and fine
                a.setU(u_a - cost * ER);
                b.setU(u_b - cost * EF * ER);
            }
            case "percent" -> {
                a.setU(u_a - cost);
                b.setU(u_b - u_b * cost);
            }
            case "smallerPercent" -> {
                a.setU(u_a - cost);
                b.setU(u_b - u_b * cost * .1);
            }
        }
    }

    public static void setLeeway(String value) {
        boolean set = false;
        switch (PP) {
            case "leeway" -> set = true;
        }
        if (set) {
            try {
                leeway = Double.parseDouble(value);
                System.out.println("leeway = "+leeway);
            } catch (NumberFormatException e) {
                System.out.println("invalid leeway: must be a double");
                exit(1);
            }
            if (leeway < 0 || leeway > 1.0) {
                System.out.println("invalid leeway: must be within the interval [0, 1].");
                exit(1);
            }
        }
    }
    
    public static void setThreshold(String value){
        boolean set = false;
        switch (PP) {
            case "thresholds", "thresholdUpper", "thresholdLower" -> set = true;
        }
        if (set) {
            try {
                threshold = Double.parseDouble(value);
                System.out.println("threshold = "+threshold);
            } catch (NumberFormatException e) {
                System.out.println("invalid threshold: must be a double");
                exit(1);
            }
            if (PP.equals("thresholds") && (threshold < 0 || threshold >= 0.5)) {
                System.out.println("invalid threshold: must be within the interval [0, 5).");
                exit(1);
            } else if (threshold < 0 || threshold > 1){
                System.out.println("invalid threshold: must be within the interval [0, 1].");
                exit(1);
            }
        }
    }

    public static void setRN1(String value) {
        switch (EWT) {
            case "rewire" -> {
                try {
                    RN1 = Double.parseDouble(value);
                    System.out.println("RN1 = "+RN1);
                } catch (NumberFormatException e) {
                    System.out.println("invalid RN1: must be a double");
                    exit(1);
                }
                if (RN1 < 0 || RN1 > 1) {
                    System.out.println("invalid RN1: must be within the interval [0, 1].");
                    exit(1);
                }
            }
        }
    }

    public static void setRN2(String value) {
        switch (EWT) {
            case "rewire" -> {
                switch (RA) {
                    case "FD", "expo" -> {
                        try {
                            RN2 = Double.parseDouble(value);
                            System.out.println("RN2 = "+RN2);
                        } catch (NumberFormatException e) {
                            System.out.println("invalid RN2: must be a double");
                            exit(1);
                        }
                    }
                }
            }
        }
    }
}