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
//    static String game; // indicates what game is being played
    static String game = "DG"; // indicates what game is being played
    static double M = 1.0; // default prize amount during a UG/DG
//    static String space; // indicates what kind of space the population will reside within
    static String space = "grid"; // indicates what kind of space the population will reside within
    static int length; // length of space; in 2D grid, len = num rows
    static int width; // width of space; in 2D grid, wid = num cols i.e. num players per row
//    static String neighType; // indicates type of neighbourhood players will have.
    static String neighType = "VN"; // indicates type of neighbourhood players will have.
//    static int neighRadius; // radius of neighbourhood
    static int neighRadius = 1; // radius of neighbourhood
    static int neighSize; // size of neighbourhood (assuming randomNeigh neighType) i.e. num random neighbours
    static int N; // population size
    static int run; // current run
    static int runs; // number of experiment runs to occur
    Player[] pop; // array of players; assumes 2D space
    double mean_p; // mean proposal value; informally referred to as the performance of an experiment
//    double mean_q; // mean acceptance threshold
    double mean_u; // mean utility
    double mean_k; // mean k of population
    double sigma_p; // standard deviation of p
//    double sigma_q; // standard deviation of q
    double sigma_u; // standard deviation of utility
    double sigma_k; // standard deviation of k
    double max_p; // highest p in population at a time
    int gen; // current generation
    static int gens; // number of generations of evolution to occur per experiment run
    static int rounds; // number of rounds of play and EWL
    static String UF; // utility function: indicates how utility is calculated
//    static double T; // PD: temptation to defect
//    static double R; // PD: reward for mutual coopeation
//    static double P; // PD: punishment for mutual defection
//    static double S; // PD: sucker's payoff for cooperating with a defector
//    static double l; // loner's payoff
    static String VP = ""; // variable parameter: indicates which parameter will be varied in experiment series.
//    static ArrayList<String> variations = new ArrayList<>();
    static String[] variations = new String[]{};
    static int exp; // indicates how far along we are through the experiment series
    static int exps = 1; // number of experiments in series
    static FileWriter fw;
    static BufferedReader br;
    static Scanner scanner = new Scanner(System.in);
    static String config_filename = "config.csv";
//    static DecimalFormat DF1 = Player.getDF1(); // formats numbers to 1 decimal place
//    static DecimalFormat DF2 = Player.getDF2(); // formats numbers to 2 decimal place
    static DecimalFormat DF4 = Player.getDF4(); // formats numbers to 4 decimal places
    static LocalDateTime old_timestamp; // timestamp of end of previous experiment in series
    static String project_path = Paths.get("").toAbsolutePath().toString();
    static String general_path = project_path + "\\csv_data"; // address where all data is recorded
    static String this_path; // address where stats for current experimentation is recorded
    static String exp_path; // address where stats for current experiment are stored
    static String run_path; // address where stats for current run are stored
    static boolean writePGenStats;
    static boolean writeUGenStats;
    static boolean writeKGenStats;
    static boolean writePRunStats;
    static boolean writeURunStats;
    static boolean writeKRunStats;
    static boolean writePosData;
    static int writeRate = 0; // write data every x gens
    static String EWT; // EW type
    static String EWL = ""; // EWL function
    static double ROC = 0; // rate of change: fixed learning amount to EW
//    static double alpha = 0; // used in alpha-beta rating
//    static double beta = 0; // used in alpha-beta rating
    static String evo; // indicates which evolution function to call
    static String sel; // indicates which selection function to call
    static double noise = 0.0; // noise affecting the evolutionary process.
    static String mut = ""; // indicates which mutation function to call
    static double mutRate = 0.0; // probability of mutation
    static double mutBound = 0.0; // denotes max mutation possible
//    static String EM;
//    static String EM = "ERv2"; // evolution mechanism / evolutionary dynamics / strategy update process: the mechanism by which evolution occurs.
    static String genType; // indicates the type of generation the algorithm will employ.
    static int ER = 0; // evolution rate: used in various ways to denote how often generations occur
    static int NIS = 0; // num inner steps: number of inner steps per generation using the monte carlo method; usually is set to value of N
    static String RWT = ""; // roulette wheel type
    static String RA = ""; // rewire away
    static String RT = ""; // rewire to
    static double RP = 0.0; // rewire probability
//    static int injRound; // injection round: indicates at which round strategy injection will occur. 0 ==> no injection.
    static double injP = 0.0; // injection p: indicates p value to be injected
    static int injSize = 0; // injection cluster size: indicates size of cluster to be injected
    static double
            //punisherCost;
            punishCost; // cost incurred by punisher
    static double
//            punisheeCost;
            punishFine; // fine received by punishee
    static String punishFunc = "";
    static double punishRatio;
    static int CI; // configuration index
//    static String[] settings; // configuration settings
    static boolean NU; // negative utility param: if true, agents can have negative utility.




    /**
     * Main method of Java program.
      */
    public static void main(String[] args) {
        configEnv();
        LocalDateTime start_timestamp = LocalDateTime.now(); // timestamp of start of experimentation
        old_timestamp = start_timestamp;
        String start_timestamp_string = start_timestamp.getYear()
                +"-"+start_timestamp.getMonthValue()
                +"-"+start_timestamp.getDayOfMonth()
                +"_"+start_timestamp.getHour()
                +"-"+start_timestamp.getMinute()
                +"-"+start_timestamp.getSecond();
        this_path = general_path+"\\"+start_timestamp_string;
        if (writeRate > 0) {
            try {
                Files.createDirectories(Paths.get(this_path)); // create stats storage folder
            }catch(IOException e){
                e.printStackTrace();
            }
            printPath();
        }
        System.out.println("Start experimentation...\nStarting timestamp: "+start_timestamp);
        experimentSeries();
        LocalDateTime finish_timestamp = LocalDateTime.now(); // marks the end of the main algorithm's runtime
        System.out.println("Finishing timestamp: "+finish_timestamp);
        Duration duration = Duration.between(start_timestamp, finish_timestamp);
        long secondsElapsed = duration.toSeconds();
        long minutesElapsed = duration.toMinutes();
        long hoursElapsed = duration.toHours();
        System.out.println("Time elapsed: "+hoursElapsed+" hours, "+minutesElapsed%60+" minutes, "+secondsElapsed%60+" seconds");
        if (writeRate > 0) printPath();
    }



    /**
     * Runs an experiment series.
     */
    public static void experimentSeries(){
        for(exp = 1; exp <= exps; exp++){
            System.out.println("Start experiment " + exp);
            exp_path = this_path + "\\exp" + exp;
            createDataFolders();
            experiment(); // run an experiment of the series
            System.out.println("End experiment " + exp);
            if(exp <= variations.length){ // do not try to vary after the last experiment has ended
                System.out.println("Varying "+VP+"...");
                switch(VP){
                    case "EWL" -> assignEWL(variations[exp - 1]);
                    case "RA" -> assignRA(variations[exp - 1]);
                    case "RT" -> assignRT(variations[exp - 1]);
                    case "sel" -> assignSel(variations[exp - 1]);
                    case "evo" -> assignEvo(variations[exp - 1]);
                    case "noise" -> assignNoise(variations[exp - 1]);
                    case "EWT" -> assignEWT(variations[exp - 1]);
                    case "gens" -> assignGens(variations[exp - 1]);
                    case "length" -> {
                        assignLength(variations[exp - 1]);
                        assignWidth();
                        assignN();
                    }
                    case "ER" -> assignER(variations[exp - 1]);
                    case "ROC" -> assignROC(variations[exp - 1]);
                    case "RP" -> assignRP(variations[exp - 1]);
                    case "mutRate" -> assignMutRate(variations[exp - 1]);
                    case "mutBound" -> assignMutBound(variations[exp - 1]);
                    case "UF" -> assignUF(variations[exp - 1]);
                    case "punishFunc" -> assignPunishFunc(variations[exp - 1]);
                    case "punishCost" -> assignPunishCost(variations[exp - 1]);
                    case "punishFine" -> assignPunishFine(variations[exp - 1]);
                    case "punishRatio" -> assignPunishRatio(variations[exp - 1]);
                    case "NU" -> assignNU(variations[exp - 1]);
                }
            }
        }
    }



    /**
     * Run an experiment.
     * An experiment consists of runs.
     * Data is collected after each experiment.
     */
    public static void experiment(){
        for(run = 1; run <= runs; run++){
            System.out.println("Start run " + run);
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
    public void start(){
        // initialise population and edge network.
        initRandomPop();
        for(int i=0;i<N;i++){
            getNeighbours(pop[i]);
        }
        if(writePosData && run == 1) {
            writePosData();
        }
        for(int i=0;i<N;i++){
            initialiseEdgeWeights(pop[i]);
        }

        // get stats for gen 0.
        calculateStats();
        writeGenAndRunStats();

        // activate population.
        switch(genType){
            case "ER" -> ER();
            case "MCv1" -> MCv1();
            case "MCv2" -> MCv2();
            case "oneGuyEvo" -> oneGuyEvo();
        }
        writeExpStats();
    }



//    /**
//     * a initiates games with neighbours.
//     * @param a player
//     */
//    public void play(Player a) {
//        ArrayList<Player> omega_a = a.getNeighbourhood(); // neighbourhood of a
//        for(int i = 0; i < omega_a.size(); i++){
//            Player b = omega_a.get(i); // neighbour of a
//            ArrayList <Player> omega_b = b.getNeighbourhood(); // neighbourhood of b
//            for (int j = 0; j < omega_b.size(); j++) {
//                Player c = omega_b.get(j); // neighbour of b
//                if (a.equals(c)) {
//                    ArrayList <Double> weights_b = b.getEdgeWeights(); // weights of b
//                    double w_ba = weights_b.get(j); // weight of edge from b to a
//                    switch(EWT){
//                        case "proposalProb" -> {
//                            double d = ThreadLocalRandom.current().nextDouble();
//                            if(w_ba > d){
//                                switch(game){
//                                    case "UG", "DG" -> UG(a, b);
//                                    case "PD" -> PD(a, b);
//                                }
//                            } else{
//                                updateStats(a,0);
//                                updateStats(b,0);
//                            }
//                        }
//                        case "payoffPercent" -> {
//                            switch(game){
//                                case "UG", "DG" -> UG(a, b, w_ba);
//                                case "PD" -> PD(a, b, w_ba);
//                            }
//                        }
//                        default -> {
//                            switch(game){
//                                case "UG", "DG" -> UG(a, b);
//                                case "PD" -> PD(a, b);
//                            }
//                        }
//                    }
//                    break;
//                }
//            }
//        }
//    }

    /**
     * play Dictator Games
     * @param a focal player
     */
    public void play(Player a){
        ArrayList<Player> omega_a = a.getOmega(); // neighbourhood of a
        for(int i = 0; i < a.getK(); i++){
            Player b = omega_a.get(i); // neighbour of a
            switch(EWT) {
                default -> UG(a, b);
                case "proposalProb"-> {
                    ArrayList <Player> omega_b = b.getOmega(); // neighbourhood of b
                    for (int j = 0; j < b.getK(); j++) {
                        Player c = omega_b.get(j); // neighbour of b
                        if (a.equals(c)) {
                            double w_ba = b.getEdgeWeights().get(j); // weight of edge from b to a
                            double random_double = ThreadLocalRandom.current().nextDouble();
                            if(w_ba > random_double){
                                UG(a, b);
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
    public void UG(Player a, Player b){
        double p_a = a.getP();
        double q_b = b.getQ();
        double pi_a = 0.0;
        double pi_b = 0.0;
        if(p_a >= q_b){
            pi_b = M * p_a;
            pi_a = M - pi_b;
        }
        updateUtility(a, pi_a);
        updateUtility(b, pi_b);
    }


//    /**
//     * Prisoner's dilemma function.<br>
//     * @param a player
//     * @param b player
//     */
//    public void PD(Player a, Player b){
//        String s_a = a.getStrategyPD();
//        String s_b = b.getStrategyPD();
//        double pi_a = 0.0;
//        double pi_b = 0.0;
//        if(s_a.equals("C") && s_b.equals("C")){
//            pi_a = R;
//            pi_b = R;
//        } else if(s_a.equals("C") && s_b.equals("D")){
//            pi_a = S;
//            pi_b = T;
//        }else if(s_a.equals("D") && s_b.equals("C")){
//            pi_a = T;
//            pi_b = S;
//        }else if(s_a.equals("D") && s_b.equals("D")){
//            pi_a = P;
//            pi_b = P;
//        }else if(s_a.equals("A") || s_b.equals("A")){
//            pi_a = l;
//            pi_b = l;
//        }
//        updateStatsPD(a, pi_a);
//        updateStatsPD(b, pi_b);
//    }


//    /**
//     * Prisoner's dilemma function.<br>
//     * Uses w_ba to calculate payoffs of players.<br>
//     * @param a player
//     * @param b player
//     */
//    public void PD(Player a, Player b, double w_ba){
//        String s_a = a.getStrategyPD();
//        String s_b = b.getStrategyPD();
//        double pi_a = 0.0;
//        double pi_b = 0.0;
//        if(s_a.equals("C") && s_b.equals("C")){
//            pi_a = R * w_ba;
//            pi_b = R * w_ba;
//        } else if(s_a.equals("C") && s_b.equals("D")){
//            pi_a = S * w_ba;
//            pi_b = T * w_ba;
//        }else if(s_a.equals("D") && s_b.equals("C")){
//            pi_a = T * w_ba;
//            pi_b = S * w_ba;
//        }else if(s_a.equals("D") && s_b.equals("D")){
//            pi_a = P * w_ba;
//            pi_b = P * w_ba;
//        }else if(s_a.equals("A") || s_b.equals("A")){
//            pi_a = l * w_ba;
//            pi_b = l * w_ba;
//        }
//        updateStatsPD(a, pi_a);
//        updateStatsPD(b, pi_b);
//    }



//    public void updateStatsPD(Player player, double payoff){
//        player.setPi(player.getPi() + payoff);
//        player.setMNI(player.getMNI() + 1);
//    }



    public void initialiseEdgeWeights(Player player){
        ArrayList <Double> edge_weights = new ArrayList<>();
        for(int i=0;i<player.getK();i++){
            edge_weights.add(1.0);
        }
        player.setEdgeWeights(edge_weights);
    }



    /**
     * Edge Weight Learning (EWL)
     */
    public void EWL(Player a){
        ArrayList<Double> weights = a.getEdgeWeights();
        ArrayList<Player> omega_a = a.getOmega();
        for(int i = 0; i < a.getK(); i++){
            Player b = omega_a.get(i);
            double w_ab = weights.get(i); // weight from a to b
            w_ab += calculateLearning(a, b);
            if(w_ab > 1.0) {
                w_ab = 1.0;
            } else if(w_ab < 0.0) {
                w_ab = 0.0;
            }
            weights.set(i, w_ab); // replace old weight value
        }
    }



    public double calculateLearning(Player a, Player b){
        double learning = 0.0;
        switch(EWL){
            case "PROC" -> {
                double pa = a.getP();
                double pb = b.getP();
                if(pa < pb) {// if a unfairer than b, increase weight
                    learning = ROC;
                }else if(pa > pb){ // else if a fairer than b, decrease weight
                    learning = -ROC;
                } // else no change
            }
            case "PD" -> learning = b.getP() - a.getP();
            case "PED" -> learning = Math.exp(b.getP() - a.getP());
            case "UD" -> learning = b.getU() - a.getU();
            case "UED" -> learning = Math.exp(b.getU() - a.getU());
            case "PDR" -> {
                double p_a = a.getP();
                double p_b = b.getP();
                double diff = p_b - p_a;
                if(p_a < p_b){
                    learning = ThreadLocalRandom.current().nextDouble(diff);
                } else if(p_a > p_b){
                    learning = -ThreadLocalRandom.current().nextDouble(-(diff)); // the minuses work around exceptions. ultimately, learning will be assigned a negative value.
                }
            }
            case "PDRv2" -> {
                double p_a = a.getP();
                double p_b = b.getP();
                if(p_a < p_b){
                    learning = ThreadLocalRandom.current().nextDouble(p_b - p_a);
                } else if(p_a > p_b){
                    learning = -ThreadLocalRandom.current().nextDouble(p_a - p_b);
                }
            }
            case "PEDv2" -> {
                double p_a = a.getP();
                double p_b = b.getP();
                if(p_a < p_b){ // if a unfairer than b, raise weight
                    learning = Math.pow((p_b - p_a), Math.exp(1));
                } else { // if a fairer than b, reduce weight
                    learning = - Math.pow((p_a - p_b), Math.exp(1));
                }
            }
            case "PEDv4" -> {
                double p_a = a.getP();
                double p_b = b.getP();
                double exp = Math.exp(Math.abs(p_b - p_a));
                if(p_a < p_b){
                    learning = exp;
                } else if(p_a > p_b){
                    learning = -exp;
                } else{
                    learning = 0.0;
                }
            }
            case "PStepwise" -> {
                double pa = a.getP();
                double pb = b.getP();
                if(pa<pb){
                    learning = 1.0;
                } else if(pa>pb){
                    learning = -1.0;
                }
            }
            case "UROC" ->{
                double ua = a.getU();
                double ub = b.getU();
                if(ua>ub){
                    learning = ROC;
                }else if(ua<ub){
                    learning = -ROC;
                }
            }
            case "PEAD" -> learning = Math.exp(Math.abs(b.getP() - a.getP()));
            case "PPEAD" -> {
                double pa=a.getP();
                double pb=b.getP();
                if(pa<pb){
                    learning = Math.exp(Math.abs(b.getP() - a.getP()));
                } else if(pa>pb){
                    learning = -Math.exp(Math.abs(b.getP() - a.getP()));
                }
            }
            case "PPED"->{
                double pa=a.getP();
                double pb=b.getP();
                if(pa<pb){
                    learning = Math.exp(b.getP() - a.getP());
                } else if(pa>pb){
                    learning = -Math.exp(b.getP() - a.getP());
                }
            }
            case "UUEAD" ->{
                double ua=a.getU();
                double ub=b.getU();
                if(ua>ub){
                    learning = Math.exp(Math.abs(b.getU() - a.getU()));
                }else if(ua<ub){
                    learning = -Math.exp(Math.abs(b.getU() - a.getU()));
                }
            }
            case "UStepwise" ->{
                double ua=a.getU();
                double ub=b.getU();
                if(ua>ub){
                    learning = 1.0;
                }else if(ua<ub){
                    learning = -1.0;
                }
            }
            case "PDhalf" -> learning = (b.getP() - a.getP()) / 2;
        }
        return learning;
    }





    /**
     * Roulette wheel (RW) selection function.
     * canditates are child and its neighbours.
     * probability of selection depends directly on fitness in
     * comparison to other candidates in the pool.
     * fitter ==> greater probability.
     * @param child player undergoing roulette wheel selection
     * @return parent
     */
    public Player selRW(Player child){
        Player parent = child; // if no neighbour is selected, child is parent by default.
        ArrayList <Player> pool = new ArrayList<>(child.getOmega()); // pool of candidates for parent
        pool.add(child);
        int size = pool.size();
        double[] pockets = new double[size];
        double roulette_total = 0;
        for(int i = 0; i < size; i++){
            switch(RWT){
                case "normal" -> pockets[i] = pool.get(i).getU();
                case "expo" -> pockets[i] = Math.exp(pool.get(i).getU() * noise);
            }
            roulette_total += pockets[i];
        }
        double random_double = ThreadLocalRandom.current().nextDouble();
        double tally = 0;
        for(int i = 0; i < size - 1; i++){ // if no neighbour candidate is selected, child is selected.
            tally += pockets[i];
            double percent_taken = tally / roulette_total; // how much space in the wheel has been taken up so far
            if(random_double < percent_taken){ // if true, the ball landed in the neighbour candidate's slot
                parent = pool.get(i); // select neighbour candidate as parent
                break;
            }
        }
        return parent;
    }



    /**
     * child selects fittest neighbour that is fitter than them.
     * if no such neighbours exist, child is parent by default.
     * @param child
     * @return parent
      */
    public Player selElitist(Player child){
        ArrayList<Player> omega = child.getOmega();
        Player parent = child;
        for(int i = 0; i < child.getK(); i++){
            Player neighbour = omega.get(i);
            if(neighbour.getU() > parent.getU()){ // if candidate fitter than parent, parent is set to candidate
                parent = neighbour;
            }
        }
        return parent;
    }



    /**
     * sel and evo effectively occur at once in one function.
     * crossover where one child adopts midway point between two parent strategies.
     *
     * @param child child produced as a result of crossover
     */
    public void crossover(Player child){

//        // how to select parents?
//
//        // select two fittest neighbours?
//        ArrayList <Player> omega = child.getOmega();
//        Player parent1 = child; // fittest neighbour
//        Player parent2 = child; // second-fittest neighbour
//        for(int i=0;i<omega.size();i++){
//            Player neighbour = omega.get(i);
//            double neighbour_u = neighbour.getU();
//            double parent2_u = parent2.getU();
//            if(neighbour_u > parent2_u){
//                parent2 = omega.get(i);
//                parent2_u = parent2.getU();
//                double parent1_mean_score = parent1.getU();
//                if(parent2_u > parent1_mean_score){
//                    Player temp = parent1;
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
    public void evoCopy(Player child, Player parent){
        child.setP(parent.getOldP());
    }



    /**
     * Use noise to move child strategy in direction of parent strategy.
     * @param child
     * @param parent
     */
    public void evoApproach(Player child, Player parent){
//        int ID = child.getID();
//        int parent_ID = parent.getID();
//        double p = child.getP();
//        double q = child.getQ();
//        double parent_old_p = parent.getOldP();
//        double parent_old_q = parent.getOldQ();
//
//        // do not approach evolve if parent is child
//        if(parent_ID != ID){
//
//            // for attribute, if parent is lower, reduce child; else, increase.
//            double approach = ThreadLocalRandom.current().nextDouble(noise);
//            if(parent_old_p < p){
//                approach *= -1;
//            }
//            double new_p = p + approach;
//            if(parent_old_q < q){
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
    public boolean mutationCheck(){
        double random_double = ThreadLocalRandom.current().nextDouble();
        return random_double < mutRate;
    }



    /**
     * Child's attributes are randomly and independently generated.
     */
    public void mutGlobal(Player child){
        if(mutationCheck()){
            switch(game){
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
    public void mutLocal(Player child){
        if(mutationCheck()){
            switch(game){
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



    /**
     * Loads in a configuration of settings from the config file, allowing the user to choose the values of the environmental parameters.
     */
    public static void configEnv(){
        // load configurations
        ArrayList<String> configurations = new ArrayList<>(); // stores configs
        try{
            br = new BufferedReader(new FileReader(config_filename));
            String line; // initialises String to store rows of data
            br.readLine(); // ignores the row of headings
            while((line = br.readLine()) != null){
                configurations.add(line);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        // displays intro and config table headings
        System.out.printf("=========================================%n");
        System.out.printf("|   Dictator Game Simulator             |%n");
        System.out.printf("|   By Evan O'Riordan                   |%n");
        printTableLine();
        System.out.printf("|" +
                " %-6s |"+//config
                " %-4s |"+//runs
                " %-6s |"+//length
                " %-9s |"+//genType
                " %-4s |"+//ER
                " %-8s |"+//gens
                " %-12s |"+//EWT
                " %-7s |"+//RP
                " %-15s |"+//RA
                " %-5s |"+//RT
                " %-10s |"+//punishFunc
                " %-10s |"+//punishCost
                " %-10s |"+//punishFine
                " %-11s |"+//punishRatio
                " %-2s |"+//NU
                " %-6s |"+//EWL
                " %-5s |"+//ROC
                " %-10s |"+//evo
                " %-11s |"+//sel
                " %-6s |"+//RWT
                " %-5s |"+//noise
                " %-6s |"+//mut
                " %-7s |"+//mutRate
                " %-8s |"+//mutBound
                " %-10s |"+//UF
                " %-4s |"+//WPGS
                " %-4s |"+//WUGS
                " %-4s |"+//WKGS
                " %-4s |"+//WPRS
                " %-4s |"+//WURS
                " %-4s |"+//WKRS
                " %-9s |"+//writeRate
                " %-11s |"+//VP
                " %s%n"//variations
                ,"config"
                ,"runs"
                ,"length"
                ,"genType"
                ,"ER"
                ,"gens"
                ,"EWT"
                ,"RP"
                ,"RA"
                ,"RT"
                ,"punishFunc"
                ,"punishCost"
                ,"punishFine"
                ,"punishRatio"
                ,"NU"
                ,"EWL"
                ,"ROC"
                ,"evo"
                ,"sel"
                ,"RWT"
                ,"noise"
                ,"mut"
                ,"mutRate"
                ,"mutBound"
                ,"UF"
                ,"WPGS"
                ,"WUGS"
                ,"WKGS"
                ,"WPRS"
                ,"WURS"
                ,"WKRS"
                ,"writeRate"
                ,"VP"
                ,"variations"
        );
        printTableLine();

        // displays config table rows
        String[] settings; // configuration settings
        for(int i=0;i<configurations.size();i++){
            settings = configurations.get(i).split(",");
            CI = 0;
            System.out.printf("| %-6d ", i); //config
            System.out.printf("| %-4s ", settings[CI++]); //runs
            System.out.printf("| %-6s ", settings[CI++]); //length
            System.out.printf("| %-9s ", settings[CI++]); // genType
            System.out.printf("| %-4s ", CI!=settings.length? settings[CI++]: ""); //ER
            System.out.printf("| %-8s ", settings[CI++]); //gens
            System.out.printf("| %-12s ", settings[CI++]); //EWT
            System.out.printf("| %-7s ", CI!=settings.length? settings[CI++]: ""); //RP
            System.out.printf("| %-15s ", CI!=settings.length? settings[CI++]: ""); //RA
            System.out.printf("| %-5s ", CI!=settings.length? settings[CI++]: ""); //RT
            System.out.printf("| %-10s ", CI!=settings.length? settings[CI++]: ""); //punishFunc
            System.out.printf("| %-10s ", CI!=settings.length? settings[CI++]: ""); //punishCost
            System.out.printf("| %-10s ", CI!=settings.length? settings[CI++]: ""); //punishFine
            System.out.printf("| %-11s ", CI!=settings.length? settings[CI++]: ""); //punishRatio
            System.out.printf("| %-2s ", CI!=settings.length? settings[CI++]: ""); //NU
            System.out.printf("| %-6s ", CI!=settings.length? settings[CI++]: ""); //EWL
            System.out.printf("| %-5s ", CI!=settings.length? settings[CI++]: ""); //ROC
            System.out.printf("| %-10s ", settings[CI++]); //evo
            System.out.printf("| %-11s ", settings[CI++]); //sel
            System.out.printf("| %-6s ", CI!=settings.length? settings[CI++]: ""); //RWT
            System.out.printf("| %-5s ", CI!=settings.length? settings[CI++]: ""); //noise
            System.out.printf("| %-6s ", CI!=settings.length? settings[CI++]: ""); //mut
            System.out.printf("| %-7s ", CI!=settings.length? settings[CI++]: ""); //mutRate
            System.out.printf("| %-8s ", CI!=settings.length? settings[CI++]: ""); //mutBound
            System.out.printf("| %-10s ", settings[CI++]); //UF
            System.out.printf("| %-4s ", CI!=settings.length? settings[CI++]: ""); //WPGS
            System.out.printf("| %-4s ", CI!=settings.length? settings[CI++]: ""); //WUGS
            System.out.printf("| %-4s ", CI!=settings.length? settings[CI++]: ""); //WKGS
            System.out.printf("| %-4s ", CI!=settings.length? settings[CI++]: ""); //WPRS
            System.out.printf("| %-4s ", CI!=settings.length? settings[CI++]: ""); //WURS
            System.out.printf("| %-4s ", CI!=settings.length? settings[CI++]: ""); //WKRS
            System.out.printf("| %-9s ", CI!=settings.length? settings[CI++]: ""); //writeRate
            System.out.printf("| %-11s ", CI!=settings.length? settings[CI++]: ""); //VP
            System.out.printf("| %s ", CI!=settings.length? settings[CI++]: ""); //variations
            System.out.println();
        }
        printTableLine();

        // asks user which configuration of settings they wish to use.
        System.out.print("Beware: if you want to vary a parameter to some value that requires secondary parameters to be initialised, make sure the configuration includes values for those secondary parameters!" +
                "\nWhich config would you like to use? (int) ");
        boolean config_selected = false;
        int config_num;
        do{ // ensures user selects valid config
            config_num = scanner.nextInt();
            if(0 <= config_num && config_num < configurations.size()){
                config_selected = true;
            } else{
                System.out.println("[ERROR] Invalid config number, try again");
            }
        }while(!config_selected);

        // assigns values from user-selected configuration to environmental parameters.
        settings = configurations.get(config_num).split(",");
        CI = 0;
        System.out.println("Start assigning settings...");
        assignGame();
        assignRuns(settings[CI++]);
        assignLength(settings[CI++]);
        assignWidth();
        assignN();
        assignGenType(settings[CI++]);
        assignER(settings[CI++]);
        assignGens(settings[CI++]);
        assignEWT(CI!=settings.length? settings[CI++]: "");
        assignRP(CI!=settings.length? settings[CI++]: "");
        assignRA(CI!=settings.length? settings[CI++]: "");
        assignRT(CI!=settings.length? settings[CI++]: "");
        assignPunishFunc(CI!=settings.length? settings[CI++]: "");
        assignPunishCost(CI!=settings.length? settings[CI++]: "");
        assignPunishFine(CI!=settings.length? settings[CI++]: "");
        assignPunishRatio(CI!=settings.length? settings[CI++]: "");
        assignNU(CI!=settings.length? settings[CI++]: "");
        assignEWL(CI!=settings.length? settings[CI++]: "");
        assignROC(CI!=settings.length? settings[CI++]: "");
        assignEvo(settings[CI++]);
        assignSel(settings[CI++]);
        assignRWT(CI!=settings.length? settings[CI++]: "");
        assignNoise(CI!=settings.length? settings[CI++]: "");
        assignMut(CI!=settings.length? settings[CI++]: "");
        assignMutRate(CI!=settings.length? settings[CI++]: "");
        assignMutBound(CI!=settings.length? settings[CI++]: "");
        assignUF(settings[CI++]);
        assignWritePGenStats(CI!=settings.length? settings[CI++]: "");
        assignWriteUGenStats(CI!=settings.length? settings[CI++]: "");
        assignWriteKGenStats(CI!=settings.length? settings[CI++]: "");
        assignWritePRunStats(CI!=settings.length? settings[CI++]: "");
        assignWriteURunStats(CI!=settings.length? settings[CI++]: "");
        assignWriteKRunStats(CI!=settings.length? settings[CI++]: "");
        assignWriteRate(CI!=settings.length? settings[CI++]: "");
        assignVP(CI!=settings.length? settings[CI++]: "");
        assignVariations(CI!=settings.length? settings[CI++]: "");
    }



    /**
     * Initialises a lattice grid population of players with randomly generated strategies.
     */
    public void initRandomPop(){
        pop = new Player[N];
        Player.setCount(0);
        int index = 0;
        switch(space){
            case "grid" -> {
                for(int y=0;y<length;y++){
                    for(int x=0;x<width;x++){
                        Player new_player = null;
                        switch(game){
                            case "UG" -> {
                                double p = ThreadLocalRandom.current().nextDouble();
                                double q = ThreadLocalRandom.current().nextDouble();
                                new_player = new Player(x, y, p, q);
                            }
                            case "DG" -> {
                                double p = ThreadLocalRandom.current().nextDouble();
                                new_player = new Player(x, y, p, 0.0);
                            }
                            case "PD" -> {}
                        }
                        pop[index] = new_player;
                        index++;
                    }
                }
            }
            case "hex" -> {}
        }
    }



    /**
     * Prints a line in the table of configurations.
     */
    public static void printTableLine(){
        System.out.printf(
                "=======================================================" +
                        "=======================================================" +
                        "=======================================================" +
                        "=======================================================" +
                        "=======================================================" +
                        "=======================================================" +
                        "=======================================================" +
                        "=======================================================" +
                        "%n");
    }



    public static void createDataFolders(){
        try{
            if(writeRate > 0) Files.createDirectories(Paths.get(exp_path));
            for(int i=1;i<=runs;i++){
                if(writePGenStats || writeUGenStats || writeKGenStats || writePRunStats || writeURunStats || writeKRunStats) // add run stat writing params to this check
                    Files.createDirectories(Paths.get(exp_path + "\\run" + i));
                if(writePGenStats || writeUGenStats || writeKGenStats)
                    Files.createDirectories(Paths.get(exp_path + "\\run" + i + "\\gen_stats"));
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }



    /**
     * Prints path of experiment stats folder.
     */
    public static void printPath(){
        System.out.println("Address of experimentation data: \n" + this_path);
    }



    /**
     * Assigns adjacent neighbour to player's neighbourhood.<br>
     * d denotes Manhattan distance for von Neumann neighbourhood or Chebyshev distance
     * for Moore neighbourhood.
    */
    public void adjacentNeigh(Player player){
        ArrayList<Player> omega = new ArrayList<>();
        double y = player.getY();
        double x = player.getX();
        for(int i=1;i<=neighRadius;i++){
            double x_plus = adjustPosition(x, i, width);
            double x_minus = adjustPosition(x, -i, width);
            double y_plus = adjustPosition(y, i, length);
            double y_minus = adjustPosition(y, -i, length);
            omega.add(findPlayerByPos(y, x_plus));
            omega.add(findPlayerByPos(y, x_minus));
            omega.add(findPlayerByPos(y_plus, x));
            omega.add(findPlayerByPos(y_minus, x));
            if(neighType.equals("dia")) {
                if(i > 1) {
                    double x_plus_minus = adjustPosition(x_plus, -1.0, width);
                    double x_minus_plus = adjustPosition(x_minus, 1.0, width);
                    double y_plus_minus = adjustPosition(y_plus, -1.0, length);
                    double y_minus_plus = adjustPosition(y_minus, 1.0, length);
                    omega.add(findPlayerByPos(y_plus_minus, x_plus_minus));
                    omega.add(findPlayerByPos(y_minus_plus, x_plus_minus));
                    omega.add(findPlayerByPos(y_minus_plus, x_minus_plus));
                    omega.add(findPlayerByPos(y_plus_minus, x_minus_plus));
                }
            }
            if(neighType.equals("Moore")){
                omega.add(findPlayerByPos(y_plus, x_plus));
                omega.add(findPlayerByPos(y_minus, x_plus));
                omega.add(findPlayerByPos(y_minus, x_minus));
                omega.add(findPlayerByPos(y_plus, x_minus));
            }
        }
        player.setOmega(omega);
        player.setK(player.getOmega().size());
    }



    /**
     * Randomly assigns either uni-directional or bi-directional edges to player.<br>
     * Assumes 2D square lattice grid population structure.
     * @param player
     * @param size
     */
    public void randomNeigh(Player player, int size){
        ArrayList<Player> omega = player.getOmega();
        Set<Integer> IDs = new HashSet<>();
        while(IDs.size() < size){
            int ID = ThreadLocalRandom.current().nextInt(N);
            IDs.add(ID);
        }
        for(int ID: IDs){
            omega.add(findPlayerByID(ID));
        }
        player.setK(player.getOmega().size());
    }



    // assign all other players to neighbourhood
    public void allPopNeigh(Player player){
        ArrayList <Player> omega = player.getOmega();
        int ID = player.getID();
        for(int i=0;i<N;i++){
            Player player2 = pop[i];
            int ID2 = player2.getID();
            if(ID != ID2){
                omega.add(player2);
            }
        }
        player.setK(player.getOmega().size());
    }



    public void calculateMeanP(){
        mean_p = 0;
        for(int i = 0; i < N; i++){
            mean_p += pop[i].getP();
        }
        mean_p /= N;
    }

    public void calculateSigmaP(){
        sigma_p = 0;
        for(int i = 0; i < N; i++){
            sigma_p += Math.pow(pop[i].getP() - mean_p, 2);
        }
        sigma_p = Math.pow(sigma_p / N, 0.5);
    }

    public void calculateMeanU(){
        mean_u = 0;
        for(int i = 0; i < N; i++){
            mean_u += pop[i].getU();
        }
        mean_u /= N;
    }

    public void calculateSigmaU(){
        sigma_u = 0;
        for(int i = 0; i < N; i++){
            sigma_u += Math.pow(pop[i].getU() - mean_u, 2);
        }
        sigma_u = Math.pow(sigma_u / N, 0.5);
    }

    public void calculateMeanK(){
        mean_k = 0;
        for(int i = 0; i < N; i++){
            mean_k += pop[i].getK();
        }
        mean_k /= N;
    }

    public void calculateSigmaK(){
        sigma_k = 0;
        for(int i = 0; i < N; i++){
            sigma_k += Math.pow(pop[i].getK() - mean_k, 2);
        }
        sigma_k = Math.sqrt(sigma_k / N);
    }



    public void prepare(){
        for(int i=0;i<N;i++){
            Player player = pop[i];
            player.setU(0);
            player.setOldP(player.getP());
            max_p = 0.0;
        }
    }



    // writes IDs and positions of players
    public void writePosData(){
        try{
            String filename = exp_path + "\\pos_data.csv";
            fw = new FileWriter(filename, false);
            String s = "";
            for(int y=length-1;y>=0;y--){
                for(int x=0;x<width;x++){
                    Player player = findPlayerByPos(y,x);
                    int ID = player.getID();
                    s += ID;
                    if (x + 1 < width)
                        s += ",";
                }
                s += "\n";
            }
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



    /**
     * Finds a player given the integer ID parameter.<br>
     * Currently not used.
     * @param ID of the player to find
     * @return player object with the given ID
     */
    public Player findPlayerByID(int ID){
        Player player = null;
        for(int i=0;i<N;i++){
            Player player2 = pop[i];
            int ID2 = player2.getID();
            if(ID == ID2){
                player = player2;
                break;
            }
        }

        return player;
    }



    /**
     * find player by position in the grid.<br>
     * e.g. if you call findPlayerByPos(5, 2), it returns the player at position (2, 5).
     * @param y y co-ordinate of the player
     * @param x x co-ordinate of the player
     * @return Player object at position (x, y) in the grid.
     */
    public Player findPlayerByPos(double y, double x){
        Player player = null;
        boolean found = false;
        int i=0;
        do{
            Player player2=pop[i];
            double y2=player2.getY();
            double x2=player2.getX();
            if(y2==y && x2==x){
                player=player2;
                found=true;
            }
            i++;
        }
        while(!found);
        return player;
    }



    /**
     * Assign same strategy to cluster of players within the grid.
     */
    public void injectStrategyCluster(){
        for(int i = 0; i < injSize; i++){
            for(int j = 0; j < injSize; j++){
                Player player = findPlayerByPos(j, i);
                player.setP(injP);
            }
        }
    }



    // adjust position with respect to periodic boundaries
    public double adjustPosition(double position, double adjustment, int max){
        double new_position = (((position + adjustment) % max) + max) % max;
        return new_position;
    }



    /**
     * Find new neighbour by randomly choosing a neighbour of a neighbour.<br>
     * New neighbour cannot be rewirer or already a neighbour.<br>
     */
    public void RTLocal(Player a, int num_rewires){
        ArrayList<Player> pool = new ArrayList<>(); // pool of candidates the rewirer might rewire to
        ArrayList<Player> omega_a = a.getOmega(); // omega_a denotes neighbourhood of rewirer.
        for(Player b: omega_a){ // b denotes neighbour of rewirer
            ArrayList<Player> omega_b = b.getOmega(); // omega_b denotes neighbourhood of neighbour of rewirer.
            for(Player c: omega_b){ // c denotes neighbour of the neighbour of rewirer.
                if(!c.equals(a)){ // do not add c to pool if c = a
                    boolean add_to_pool = true; // boolean tracking whether c should be added to pool or not.
                    for (Player d : pool) { // d denotes candidate in pool
                        if(c.equals(d)){ // if c = d, c must already be in the pool, therefore do not add c to the pool.
                            add_to_pool = false;
                            break;
                        }
                    }
                    if(!add_to_pool) continue; // move on to next c if this c has already been ruled out of contention.
                    for (Player e : omega_a) { // e denotes neighbour of rewirer.
                        if(c.equals(e)){ // if c = e, c must already be in omega_a, so you do not want to add c to pool.
                            add_to_pool = false;
                            break;
                        }
                    }
                    if(add_to_pool) pool.add(c); // if deemed valid, add c to pool.
                }
            }
        }
        if(pool.isEmpty()) {
            RTPop(a, num_rewires); // if pool empty, default to rewiring to a random player in the pop.
        } else{ // connect to local player.
            for(int rewires_done = 0; rewires_done < num_rewires; rewires_done++){
                Player f = pool.get(ThreadLocalRandom.current().nextInt(pool.size())); // f denotes new neighbour of a.
                omega_a.add(f); // connect a to f.
                a.getEdgeWeights().add(1.0);
                f.getOmega().add(a); // connect f to a.
                f.getEdgeWeights().add(1.0);
            }
        }
    }



    /**
     * Find new neighbour by randomly choosing a player from the population.<br>
     * New neighbour cannot be rewirer or already a neighbour.<br>
     * @param a rewirer
     * @param b number of rewires to do
     */
    public void RTPop(Player a, int b){
        for(int c=0;c<b;c++){ // c denotes number of rewires done so far.
            ArrayList<Player> omega_a = a.getOmega(); // denotes neighbourhood of a.
            Player d = null; // d denotes new neighbour.
            boolean found_new_neighbour = false;
            while(!found_new_neighbour){ // keep searching until you find a valid new neighbour
                d = pop[ThreadLocalRandom.current().nextInt(pop.length)]; // randomly choose player from pop.
                if(!d.equals(a)){ // do not connect a to d if d = a.
//                    boolean f = true; // f indicates whether there does not exist g in omega_a such that g = d.
                    boolean already_neighbours = false; // indicates whether a and d are already neighbours
                    for(Player g: omega_a){ // g denotes neighbour of a.
                        if(d.equals(g)){
//                            f = false;
                            already_neighbours = true;
                            break;
                        }
                    }
//                    found_new_neighbour = f;
                    found_new_neighbour = !already_neighbours;
                }
            }
            omega_a.add(d); // connect a to d.
            a.getEdgeWeights().add(1.0);
            d.getOmega().add(a); // connect d to a.
            d.getEdgeWeights().add(1.0);
        }
    }



    public static void writeSettings(){
        if(writeRate > 0){
            String settings_filename = this_path + "\\" + "settings.csv";
            String settings = "";
            if(exp == 1){
                settings += "runs";
                settings += ",space";
                settings += ",length";
                settings += ",width";
                settings += ",N";
                settings += ",genType";
                settings += ER != 0? ",ER": "";
                settings += NIS != 0? ",NIS": "";
                settings += ",gens";
                settings += rounds != 0? ",rounds": "";
                settings += EWT.isEmpty()? "": ",EWT";
                settings += RP == 0.0? "": ",RP";
                settings += RA.isEmpty() ? "": ",RA";
                settings += RT.isEmpty() ? "": ",RT";
                settings += punishFunc.isEmpty()? "": ",punishFunc";
                settings += punishCost != 0.0? ",punishCost": "";
                settings += punishFine != 0.0? ",punishFine": "";
                settings += punishRatio != 0.0? ",punishRatio": "";
                settings += EWT.equals("punish")? ",NU": ""; // NU
                settings += EWL.isEmpty()? "": ",EWL";
                settings += ROC == 0.0? "": ",ROC";
                settings += ",evo";
                settings += ",sel";
                settings += !RWT.isEmpty()? ",RWT": "";
                settings += noise != 0.0? ",noise": "";
                settings += mut.isEmpty()? "": ",mut";
//                settings += mutRate != 0.0? ",mutRate": "";
                settings += mut.isEmpty()? "": ",mutRate"; // mutRate
                settings += mutBound != 0.0? ",mutBound": "";
                settings += ",UF";
            }
            settings += "\n";
            settings += runs;
            settings += "," + space;
            settings += "," + length;
            settings += "," + width;
            settings += "," + N;
            settings += "," + genType;
            settings += ER != 0? "," + ER: "";
            settings += NIS != 0? "," + NIS: "";
            settings += "," + gens;
            settings += rounds != 0? "," + rounds: "";
            settings += EWT.isEmpty()? "": "," + EWT;
            settings += RP == 0.0? "": "," + RP;
            settings += RA.isEmpty() ? "": "," + RA;
            settings += RT.isEmpty() ? "": "," + RT;
            settings += punishFunc.isEmpty()? "": "," + punishFunc;
            settings += punishCost != 0.0? "," + punishCost: "";
            settings += punishFine != 0.0? "," + punishFine: "";
            settings += punishRatio != 0.0? "," + punishRatio: "";
            settings += EWT.equals("punish")? "," + NU: ""; // NU
            settings += EWL.isEmpty()? "": "," + EWL;
            settings += ROC == 0.0? "": "," + ROC;
            settings += "," + evo;
            settings += "," + sel;
            settings += !RWT.isEmpty()? "," + RWT: "";
            settings += noise != 0.0? "," + noise: "";
            settings += mut.isEmpty()? "": "," + mut;
//            settings += mutRate != 0.0? "," + mutRate: "";
            settings += mut.isEmpty()? "": "," + mutRate; // mutRate
            settings += mutBound != 0.0? "," + mutBound: "";
            settings += "," + UF;
            try{
                fw = new FileWriter(settings_filename, true);
                fw.append(settings);
                fw.close();
            } catch(IOException e){
                e.printStackTrace();
                System.exit(0);
            }
        }
    }



    /**
     * Write and calculate stats of series. Documents how the series went.
     * Reads and writes local data.
     */
    public static void writeSeriesStats(){
        if(writeRate > 0 && (writePRunStats || writeURunStats || writeKRunStats)){
            String series_stats_filename = this_path + "\\series_stats.csv";
            String exp_stats_filename = exp_path + "\\exp_stats.csv";
            String output = "";
            if(exp == 1) {
                if(writePRunStats){
                    output+="mean avg p,sigma mean p,";
                }
                if(writeURunStats){
                    output+="mean avg u,";
                }
                if(writeKRunStats){
                    output+="mean sigma k,";
                }

//                // records the name of the varying param in series_stats.csv.
//                if(!varying.equals("")){
//                    output+=varying+",";
//                }

                output = removeTrailingComma(output);
            }
            double mean_avg_p = 0.0;
            double sigma_mean_p = 0.0;
            double mean_avg_u = 0.0;
            double mean_sigma_k = 0.0;
            double[] mean_p_values = new double[runs];
            double[] mean_u_values = new double[runs];
            double[] sigma_k_values = new double[runs];
            try{
                fw = new FileWriter(series_stats_filename, true);
                br = new BufferedReader(new FileReader(exp_stats_filename));
                br.readLine();
                for(int i=0;i<runs;i++){
                    String row = br.readLine();
                    String[] row_contents = row.split(",");
                    int j = 0;
                    if(writePRunStats){
                        mean_p_values[i] = Double.parseDouble(row_contents[j]);
                        j++; // move past mean p
                        j++; // move past sigma p
                        j++; // move past max p
                    }
                    if(writeURunStats) {
                        mean_u_values[i] = Double.parseDouble(row_contents[j]);
                        j++; // move past mean u
                        j++; // move past sigma u
                    }
                    if(writeKRunStats) {
                        sigma_k_values[i] = Double.parseDouble(row_contents[j]);
                    }
                }
                for(int i=0;i<runs;i++){
                    if(writePRunStats){
                        mean_avg_p += mean_p_values[i];
                    }
                    if(writeURunStats){
                        mean_avg_u += mean_u_values[i];
                    }
                    if(writeKRunStats){
                        mean_sigma_k += sigma_k_values[i];
                    }
                }
                mean_avg_p /= runs;
                mean_avg_u /= runs;
                if(writeKRunStats) {
                    mean_sigma_k /= runs;
                }
                for(int i=0;i<runs;i++){
                    sigma_mean_p += Math.pow(mean_p_values[i] - mean_avg_p, 2);
                }
                sigma_mean_p = Math.pow(sigma_mean_p / runs, 0.5);
                output += "\n";
                if(writePRunStats){
                    output+=DF4.format(mean_avg_p) + "," + DF4.format(sigma_mean_p) + ",";
                }
                if(writeURunStats){
                    output+=DF4.format(mean_avg_u) + ",";
                }if(writeKRunStats){
                    output+=DF4.format(mean_sigma_k) + ",";
                }
                output = removeTrailingComma(output);
                fw.append(output);
                fw.close();
            }catch(Exception e){
                e.printStackTrace();
            }

            // display info in console
            if(writePRunStats){
                System.out.println("exp: "+exp+"; mean avg p: "+DF4.format(mean_avg_p));
            }
        }
    }


    public void updateUtility(Player player, double payoff){
        switch(UF){
            case "cumulative" -> player.setU(player.getU() + payoff);
            case "normalised" -> player.setU(player.getU() + (payoff / player.getK()));
        }
    }



    public Player selRandomNeigh(Player child){
        Player parent = child;
        int k = child.getK();
        if(k > 0){
            int random_int = ThreadLocalRandom.current().nextInt(k);
            parent = child.getOmega().get(random_int);
        }
        return parent;
    }



    public Player selRandomPop(){
        return findPlayerByID(ThreadLocalRandom.current().nextInt(N));
    }





    /**
     * Player may rewire their edges.<br>
     * Rewiring is guaranteed to occur once the edge is referenced in the
     * indices_of_edges_to_be_rewired ArrayList.<br>
     * @param a player who may rewire
      */
    public void rewire(Player a){
        double random_double = ThreadLocalRandom.current().nextDouble();
        if(RP > random_double){
            int num_rewires = 0;
            ArrayList<Player> omega_a = a.getOmega();
            ArrayList<Double> weights = a.getEdgeWeights();
            ArrayList<Integer> indices_of_edges_to_be_rewired = new ArrayList<>();
            for(int i = 0; i < a.getK(); i++){
//                double w_ab = weights.get(i); // w_ab denotes weighted edge from a to neighbour b

                // TODO: testing why the above commented out line doesnt sometimes work...
                double w_ab=0;
                try {
                    w_ab = weights.get(i); // w_ab denotes weighted edge from a to neighbour b
                }catch(IndexOutOfBoundsException e){
                    System.out.println("BP");
                }

                double prob_rewire = 0;
                switch(RA){ // I decided to bunch the rewire away functions into one switch because they are very similar functionally.
                    case "smoothstep" -> prob_rewire = 1 - (3 * Math.pow(w_ab, 2) - 2 * Math.pow(w_ab, 3));
                    case "smootherstep" -> prob_rewire = 1 - (6 * Math.pow(w_ab, 5) - 15 * Math.pow(w_ab, 4) + 10 * Math.pow(w_ab, 3));
                    case "0Many" -> prob_rewire = w_ab == 0.0? 1.0: 0.0;
                    case "linear" -> prob_rewire = 1 - w_ab;
                    case "exponential" -> {
                        double k = 0.1; // manually set noise
                        prob_rewire = Math.exp(-k * w_ab);
                    }
                    case "FD" -> {
                        Player b = omega_a.get(i);
                        double k = 0.1; // manually set noise
                        prob_rewire = 1 / (1 + Math.exp((a.getU() - b.getU()) / k));
                    }
                    case "linearR" -> prob_rewire = w_ab < 1.0? ThreadLocalRandom.current().nextDouble(1 - w_ab): 0.0;
                }
                double c = ThreadLocalRandom.current().nextDouble();
                if(prob_rewire > c){
                    indices_of_edges_to_be_rewired.add(i);
                }
            }
            for(int i = indices_of_edges_to_be_rewired.size() - 1; i >= 0; i--){
                int d = indices_of_edges_to_be_rewired.get(i);
                Player e = omega_a.get(d);
                ArrayList<Player> omega_e = e.getOmega();
                for(int j = 0; j < e.getK(); j++){
                    Player f = omega_e.get(j);
                    if(f.equals(a)){
                        omega_a.remove(d);
                        weights.remove(d);
                        omega_e.remove(j);
                        e.getEdgeWeights().remove(j);
                        num_rewires++;
                        break;
                    }
                }
            }


            if(num_rewires > 0){
                switch (RT){
                    case"local"->RTLocal(a, num_rewires);
                    case"pop"->RTPop(a, num_rewires);
                }
            }
        }
        a.setK(a.getOmega().size());
    }



    /**
     * evoUDN: evolution based on normalised utility difference.<br>
     * Probability to evolve equals utility difference divided by k.<br>
     * Child fitter than parent mean no chance.<br>
     * Fitter parent means greater probability.<br>
     * Greater child k means lesser probability.<br>
     * Inspired by cardinot2016optional.<br>
     * @param child
     * @param parent
     */
//    public void evoUD(Player child, Player parent){
    public void evoUDN(Player child, Player parent){
        double random_number = ThreadLocalRandom.current().nextDouble();
        double prob_evolve = (parent.getU() - child.getU()) / child.getK();
        if(random_number < prob_evolve)
            evoCopy(child, parent);
    }



    /**
     * Write stats of experiment.
     * 1 file per exp.
     */
    public void writeExpStats() {
        if(writeRate > 0 && (writePRunStats || writeURunStats || writeKRunStats)){
            String exp_stats_filename = exp_path + "\\exp_stats.csv";
            String run_stats_filename = run_path + "\\run_stats.csv";
            String output = "";
            try {
                fw = new FileWriter(exp_stats_filename, true);
                br = new BufferedReader(new FileReader(run_stats_filename));
                String line = br.readLine();
                if(run == 1) {


//                    output += line; // write headings


                    // remove gen column
                    String[] row_contents = line.split(",");
                    String no_gen_str = "";
                    for(int i=1;i<row_contents.length;i++){
                        no_gen_str += row_contents[i] + ",";
                    }
                    no_gen_str = removeTrailingComma(no_gen_str);
                    output += no_gen_str;


                }

                // skip to the last row of run stats
//                for(int i = 0; i < gens / writeRate; i++){
                for(int i = 0; i <= gens / writeRate; i++){
                    line = br.readLine();
                }


//                output += "\n" + line; // write stats of last gen of run


                // remove gen column
                String[] row_contents = line.split(",");
                String no_gen_str = "";
                for(int i=1;i<row_contents.length;i++){
                    no_gen_str += row_contents[i] + ",";
                }
                no_gen_str = removeTrailingComma(no_gen_str);
                output += "\n" + no_gen_str;


                fw.append(output);
                fw.close();
            }catch(Exception e){
                e.printStackTrace();
            }


            // display info in console
            if(writePRunStats){
                System.out.println("run: "+run+"; mean p: "+DF4.format(mean_p));
            }


        }
    }



    public void calculateMaxP(){
        for(int i=0;i<N;i++){
            double p = pop[i].getP();
            if(p > max_p)
                max_p = p;
        }
    }



    public Player sel(Player child){
        Player parent = null;
        switch(sel){
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


    public void evo(Player child, Player parent){
        switch (evo) {
            case "copy" -> evoCopy(child, parent);
//            case "approach" -> evoApproach(child, parent);
            case "copyFitter" -> evoCopyFitter(child, parent);
            case "UD" -> evoUD(child, parent);
            case "UDN" -> evoUDN(child, parent);
            case "FD" -> evoFD(child, parent);
        }
    }



    public void mut(Player child){
        switch (mut){
            case "global" -> mutGlobal(child);
            case "local" -> mutLocal(child);
        }
    }



    /**
     * Calculate stats at the end of a generation.
     * E.g. Calculate mean p of the pop at gen t.
     * calculateStats() will be called every generation.
     */
    public void calculateStats(){
        if(writePGenStats){
            for(Player player: pop){
                player.calculateMeanPOmega();
            }
        }
//        if(writeDegGenStats){
//            for(Player player: pop){
//                player.calculateDegree();
//            }
//        }

        // the only reason to use this loop is if degree is not being updated whenever a player's degree changes.
//        for(Player player: pop){
//            player.calculateDegree();
//        }

        if(writePRunStats) {
            calculateMeanP();
            calculateSigmaP();
            calculateMaxP();
        }
        if(writeURunStats) {
            calculateMeanU();
            calculateSigmaU();
        }
        if(writeKRunStats) {
            calculateMeanK();
            calculateSigmaK();
        }
    }



    /**
     * Calls the gen and run stat writing functions every writeRate gens.<br><br>
     * This function is typically called at the end of a gen.<br><br>
     * Writes gen stats e.g. write p_x for all players x in the pop at gen t.<br><br>
     * Writes run stats e.g. write mean(p) of the pop at gen t.<br><br>
     * Whether a stat is recorded depends on the writing params.<br>
     */
    public void writeGenAndRunStats(){
        if(writeRate != 0 && (gen == 0 || gen % writeRate == 0)) {
            if(writePGenStats || writeUGenStats || writeKGenStats) writeGenStats();
            if(writePRunStats || writeURunStats || writeKRunStats) writeRunStats();
        }
    }



    /**
     * Child copies parent if parent fitter.
     * @param child
     * @param parent
     */
    public void evoCopyFitter(Player child, Player parent){
        if(parent.getU() > child.getU()) {
            evoCopy(child, parent);
        }
    }



    /**
     * evoUDN: evolution based on utility difference.<br>
     */
    public void evoUD(Player child, Player parent){
        double random_number = ThreadLocalRandom.current().nextDouble();
        double prob_evolve = parent.getU() - child.getU();
        if(random_number < prob_evolve)
            evoCopy(child, parent);
    }




    /**
     * Writes the attributes (p, u, k) of all players in the pop at gen t.
     * 1 file per gen.
     */
    public void writeGenStats(){
        String filename = run_path + "\\gen_stats\\gen" + gen + ".csv";
        String s = "";
//        s += "gen,";
        if(writePGenStats) s += "p,mean p omega,";
        if(writeUGenStats) s += "u,";
        if(writeKGenStats) s += "k,";
        s = removeTrailingComma(s);
        for(Player player: pop){
            s += "\n";
//            s += gen + ",";
            if(writePGenStats) s += DF4.format(player.getP()) + "," + DF4.format(player.getMeanPOmega()) + ",";
            if(writeUGenStats) s += DF4.format(player.getU()) + ",";
            if(writeKGenStats) s += DF4.format(player.getK()) + ",";
            s = removeTrailingComma(s);
        }
        try{
            fw = new FileWriter(filename);
            fw.append(s);
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }


    /**
     * Write aggregate stats of a run at gen t.
     * 1 file per run.
     */
    public void writeRunStats() {
        String filename = run_path + "\\run_stats.csv";
        String s = "";
//        if(gen / writeRate == 1){ // apply headings to file before writing data
        if(gen == 0){ // apply headings to file before writing data // stop extra headings from printing...


//            s += "gens,";
            s += "gen,";


            if (writePRunStats) s += "mean p,sigma p,max p,";
            if (writeURunStats) s += "mean u,sigma u,";
//            if (writeDegRunStats && EWT.equals("rewire")) s += "sigma deg,";
            if (writeKRunStats) s += "sigma k,";
            s = removeTrailingComma(s);
            s += "\n";
        }
        s += gen + ",";
        if(writePRunStats) s += DF4.format(mean_p) + "," + DF4.format(sigma_p) + "," + DF4.format(max_p) + ",";
        if(writeURunStats) s += DF4.format(mean_u) + "," + DF4.format(sigma_u) + ",";
//        if(writeDegRunStats && EWT.equals("rewire")) s += DF4.format(sigma_deg) + ",";
        if(writeKRunStats) s += DF4.format(sigma_k) + ",";
        s = removeTrailingComma(s);
        s+="\n";
        try{
            fw = new FileWriter(filename, true);
            fw.append(s);
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }



    /**
     * If last char of string is a comma, get rid of it.
      */
    public static String removeTrailingComma(String s){
        if(s.length() > 0 && s.charAt(s.length() - 1) == ','){
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }



    public static void exit(){
        System.out.println("[INFO] Exiting...");
        Runtime.getRuntime().exit(0);
    }


    /**
     * probability of selection does not depend directly on the fitness
     * of an individual (like with RW sel)
     * but rather their fitness rank within the population/neighbourhood.
     * compare fitness of candidates.
     * higher rank ==> higher probability of being selected.
     * candidates for selection are child and its neighbours.
      */
    public Player selRank(Player child){
        // get candidates
        Player parent = child; // if no neighbour is selected, child is parent by default
        ArrayList <Player> pool = new ArrayList<>(child.getOmega()); // pool of candidates for parent
        pool.add(child);

        // get utilities of candidates
        int size = pool.size();
        double[] utilities = new double[size];
        for(int i=0;i<size;i++){
            utilities[i] = pool.get(i).getU();
        }

        // determine ranks
        int ranks[] = new int[size];
        for(int i=0;i<size;i++){
            ranks[i] = 1;
        }
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                if(i != j){
                    if(utilities[i] > utilities[j]){
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
        for(int i = 0; i < size; i++){
            total += ranks[i];
        }
        double random_double = ThreadLocalRandom.current().nextDouble(total);
        double tally = 0;
        for(int i = 0; i < size - 1; i++){
            tally += ranks[i];
            if(random_double < tally){
                parent = pool.get(i); // select candidate as parent
                break;
            }
        }
        return parent;
    }

    /**
     * punisher tries to punish all neighbours.
     * a denotes punisher.
     * b denotes punishee neighbour of a.
     * probability of punishing = 1 - w_ab.
     * higher w_ab ==> lower probability of a punishing b.
     * w_ab = 1.0 ==> guaranteed not to punish.
     * w_ab = 0.0 ==> guaranteed to punish.
     * @param a
     */
    public void punishAllProb(Player a){
        ArrayList<Double> weights = a.getEdgeWeights();
        ArrayList<Player> omega_a = a.getOmega();
        for(int i=0;i<a.getK();i++){
            Player b = omega_a.get(i);
            double random_double = ThreadLocalRandom.current().nextDouble();
            double w_ab = weights.get(i);
            double punish_prob = 1 - w_ab;
//            if(punish_prob > random_double){
//            if(punish_prob > random_double && a.getU() - punishCost >= 0 && b.getU() - punishFine >= 0){
            if(punish_prob > random_double && ((a.getU() - punishCost >= 0 && b.getU() - punishFine >= 0) || NU == true)){
                a.setU(a.getU() - punishCost);
                b.setU(b.getU() - punishFine);
            }
        }
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
    public void evoFD(Player child, Player parent){
        double random_double = ThreadLocalRandom.current().nextDouble();
        double K = noise;
        double u_x = child.getU();
        double u_y = parent.getU();
        double u_diff = u_x - u_y;
        double divided_by_K = u_diff / K;
        double exponential = Math.exp(divided_by_K);
        double denominator = 1 + exponential;
        double evolve_probability = 1 / denominator;
        if (random_double < evolve_probability){
            evoCopy(child, parent);
        }
    }


    public void punish(Player player){
        switch(punishFunc){
            case "allProb" -> {
                punishAllProb(player);
            }
            case "oneProb" -> {
                punishOneProb(player);
            }
            case "allAmount" -> {
                punishAllAmount(player);
            }
            case "oneAmount" -> {
                punishOneAmount(player);
            }
        }
    }


    /**
     * punisher tries to punish one neighbours.
     * a denotes punisher.
     * b denotes punishee i.e. the neighbour that may be punished by a.
     * probability of punishing = 1 - w_ab.
     * higher w_ab ==> lower probability of a punishing b.
     * w_ab = 1.0 ==> guaranteed not to punish.
     * w_ab = 0.0 ==> guaranteed to punish.
     * @param a
     */
    public void punishOneProb(Player a){
//        ArrayList<Double> weights = a.getEdgeWeights();
//        ArrayList<Player> omega_a = a.getOmega();
//        int random_int = ThreadLocalRandom.current().nextInt(a.getK());
//        Player b = omega_a.get(random_int);
//        double w_ab = weights.get(random_int);
//        double punish_prob = 1 - w_ab;
//        double random_double = ThreadLocalRandom.current().nextDouble();
//        if(punish_prob > random_double){
//            a.setU(a.getU() - punishCost);
//            b.setU(b.getU() - punishFine);
//        }
        System.out.println("decommissioned since func does not consider negative utility...");
    }

    // for every 1 unit spent by punisher to punish, victim loses punishRatio units. inspired by fehr2002altrustic.
    public void punishAllAmount(Player a){
        ArrayList<Double> weights = a.getEdgeWeights();
        ArrayList<Player> omega_a = a.getOmega();
        for(int i=0;i<a.getK();i++){
            Player b = omega_a.get(i);
            double w_ab = weights.get(i);
            double cost = 1 - w_ab; // cost of punishing for punisher.
            double fine = punishRatio * (1 - w_ab); // fine applied to punishee.
            a.setU(a.getU() - cost);
            b.setU(b.getU() - fine);
        }
    }
    public void punishOneAmount(Player a){
        // TODO: implement. use punishAllAmount() as a blueprint for how to implement the "amount" part.
    }


    public static void assignPunishCost(String value){
        switch(punishFunc){

            // punishCost must be valid if punishFunc is oneProb or allProb.
            case "oneProb", "allProb" -> {
                try{
                    punishCost = Double.parseDouble(value);
                    System.out.println("punishCost="+punishCost);
                }catch(NumberFormatException e){
                    System.out.println("invalid punishCost: must be a double");
                    exit();
                }
            }
        }
    }

    public static void assignPunishFine(String value){
        switch(punishFunc){

            // punishFine must be valid if punishFunc is oneProb or allProb.
            case "oneProb", "allProb" -> {
                try{
                    punishFine = Double.parseDouble(value);
                    System.out.println("punishFine="+punishFine);
                }catch(NumberFormatException e){
                    System.out.println("invalid punishFine: must be a double");
                    exit();
                }
            }
        }
    }

    public static void assignPunishFunc(String value){
        switch(EWT){

            // punishFunc must be valid if EWT = punish.
            case "punish" -> {
                switch(value){

                    // case where value is valid.
                    case "allProb", "oneProb", "allAmount", "oneAmount" -> {
                        punishFunc = value;
                        System.out.println("punishFunc="+punishFunc);
                    }

                    // case where value is invalid.
                    default -> {
                        System.out.println("invalid punishFunc");
                        exit();
                    }
                }
            }
        }
    }

    public static void assignPunishRatio(String value){
        switch(punishFunc){

            // punishRatio must be valid if punishFunc is oneAmount or allAmount.
            case "oneAmount", "allAmount" -> {
                try{
                    punishRatio = Double.parseDouble(value);
                    System.out.println("punishRatio="+punishRatio);
                }catch(NumberFormatException e){
                    System.out.println("invalid punishRatio: must be a double");
                    exit();
                }
            }
        }
    }

    public static void assignMutRate(String value){
        switch(mut){
            case "global", "local" -> {
                try{
                    mutRate = Double.parseDouble(value);
                    System.out.println("mutRate="+mutRate);
                }catch(NumberFormatException e){
                    System.out.println("invalid mutRate");
                    exit();
                }
                if(mutRate < 0 || mutRate > 1){
                    System.out.println("invalid mutRate: must be within the interval [0, 1].");
                    exit();
                }
            }
        }
    }

    // if EWL is not PROC or UROC, nothing happens.
    public static void assignROC(String value){
        switch(EWL){
            case "PROC", "UROC" -> { // value must be valid if EWL is PROC or UROC.
                try{
                    ROC = Double.parseDouble(value);
                    System.out.println("ROC="+ROC);
                }catch(NumberFormatException e){
                    System.out.println("invalid ROC: must be a double");
                    exit();
                }
                if(ROC <= 0 || ROC > 1){
                    System.out.println("invalid ROC: must be within the interval (0, 1].");
                    exit();
                }
            }
        }
    }

    public static void assignGens(String value){
        try{
            gens = Integer.parseInt(value);
            System.out.println("gens="+gens);
        }catch(NumberFormatException e){
            System.out.println("invalid gens: must be an integer");
            exit();
        }
        if(gens < 1){
            System.out.println("invalid gens: must be >= 1.");
            exit();
        }
    }

    public static void assignER(String value){
        switch(genType) {
            case "ER" -> {
                try{
                    ER = Integer.parseInt(value);
                    System.out.println("ER="+ER);
                }catch(NumberFormatException e){
                    System.out.println("invalid ER: must be an integer");
                    exit();
                }
                if(ER < 1){
                    System.out.println("invalid ER: must be >= 1.");
                    exit();
                }
            }
        }
    }

    public static void assignLength(String value){
        try{
            length = Integer.parseInt(value);
            System.out.println("length="+length);
        }catch(NumberFormatException e){
            System.out.println("invalid length: must be an integer");
            exit();
        }
        if(length < 3){
            System.out.println("invalid length: must be >= 3.");
            exit();
        }
    }

    public static void assignRuns(String value){
        try{
            runs = Integer.parseInt(value);
            System.out.println("runs="+runs);
        }catch(NumberFormatException e){
            System.out.println("invalid runs: must be an integer");
            exit();
        }
        if(runs < 1){
            System.out.println("invalid runs: must be >= 1.");
            exit();
        }
    }

    public static void assignMutBound(String value){
        switch(mut){
            case "local" -> {
                try{
                    mutBound = Double.parseDouble(value);
                    System.out.println("mutBound="+mutBound);
                }catch(NumberFormatException e){
                    System.out.println("invalid mutBound");
                    exit();
                }
                if(mutBound <= 0 || mutBound > 1){
                    System.out.println("invalid mutBound: must be within the interval (0, 1].");
                    exit();
                }
            }
        }
    }

    public static void assignMut(String value){
        switch(value){
            case "global", "local" -> {
                mut = value;
                System.out.println("mut="+mut);
            }
            default -> System.out.println("no mut");
        }
    }

    public static void assignEWT(String value){
        switch(value){

            // case where value is valid.
            case "proposalProb", "punish", "rewire" -> {
                EWT = value;
                System.out.println("EWT="+EWT);
            }

            // case where value is invalid.
            // EWT can be unassigned.
            default -> System.out.println("no EWT");
        }
    }

    public static void assignEWL(String value){
        switch(value){

            // case where value is valid.
            case "PROC", "UROC", "PD", "UD", "PDhalf" -> {
                EWL = value;
                System.out.println("EWL="+EWL);
            }

            // case where value is invalid.
            // EWL can be unassigned.
            default -> System.out.println("no EWL");
        }
    }

    public static void assignSel(String value){
        switch(value){

            // case where value is valid.
            case "RW", "elitist", "randomNeigh", "randomPop", "rank" -> {
                sel = value;
                System.out.println("sel="+sel);
            }

            // case where value is invalid.
            default -> {
                System.out.println("invalid sel");
                exit();
            }
        }
    }

    public static void assignRWT(String value){
        switch(sel){
            case "RW" -> {
                switch(value){
                    case "expo", "normal" -> {
                        RWT = value;
                        System.out.println("RWT="+RWT);
                    }
                    default -> {
                        System.out.println("invalid RWT");
                        exit();
                    }
                }
            }
        }
    }

    // the func leaves room for more sel funcs utilising noise, including one's not based on RW sel.
    public static void assignNoise(String value){
        boolean assign = false;
        switch(sel){
            case "RW" -> {
                switch(RWT) {
                    case "expo" -> assign = true;
                }
            }
        }
        switch(evo){
            case "FD" -> assign = true;
        }
        if(assign){
            try{
                noise = Double.parseDouble(value);
                System.out.println("noise="+noise);
            }catch(NumberFormatException e){
                System.out.println("invalid noise: must be a double");
                exit();
            }
        }
    }

    public static void assignUF(String value){
        switch(value){
            case "cumulative", "normalised" -> {
                UF = value;
                System.out.println("UF="+UF);
            }
            default -> {
                System.out.println("invalid UF");
                exit();
            }
        }

    }

    // accumulates VP (var param (variable parameter)) info inside and writes the output String to a file.
    public static void writeVP(){
        if(writeRate > 0){
            String filename = this_path + "\\" + "VP.csv";
            String output = "";

            // includes the column header for the variable parameter.
            if(exp == 1) {
                output += VP;
            }

            // includes the current value of the variable parameter.
            switch(VP){
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
                case "noise" -> output += "\n" + noise;
                case "mutRate" -> output += "\n" + mutRate;
                case "mutBound" -> output += "\n" + mutBound;
                case "UF" -> output += "\n" + UF;
                case "punishFunc" -> output += "\n" + punishFunc;
                case "punishCost" -> output += "\n" + punishCost;
                case "punishFine" -> output += "\n" + punishFine;
                case "punishRatio" -> output += "\n" + punishRatio;
                case "NU" -> output += "\n" + NU;
            }

            // create the file and write the data.
            try{
                fw = new FileWriter(filename, true);
                fw.append(output);
                fw.close();
            } catch(IOException e){
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    public static void assignVP(String value){
        switch(value){
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
                    "noise",
                    "mut",
                    "mutRate",
                    "mutBound",
                    "UF",
                    "punishFunc",
                    "punishCost",
                    "punishFine",
                    "punishRatio",
                    "NU" -> {
                VP = value;
                System.out.println("VP="+VP);
            }
            default -> System.out.println("[INFO] No follow-up experimentation involving parameter variation scheduled.");
        }
    }

    public void getNeighbours(Player player){
        switch(neighType){
            case"VN","Moore","dia"->adjacentNeigh(player);
            case"random"->randomNeigh(player, neighSize);
            case"all"->allPopNeigh(player);
        }
    }

    public void ER(){
        rounds = 0;
        for(gen = 1; gen <= gens; gen++){ // gens
            for(int round = 0; round < ER; round++){ // rounds
                for(int i=0;i<N;i++) {
                    play(pop[i]); // play DG
                }
                if(!EWL.isEmpty()){
                    for(int i=0;i<N;i++){
                        EWL(pop[i]); // edge weight learning
                    }
                }
                rounds++;
            }
            switch(EWT){
                case "rewire" -> {
                    for(int i=0;i<N;i++){
                        rewire(pop[i]);
                    }
                }
                case "punish" -> {
                    for(int i=0;i<N;i++){
                        punish(pop[i]);
                    }
                }
            }
            for(int i=0;i<N;i++) {
                Player child = pop[i];
                Player parent = sel(child);
                if(!child.equals(parent)) {
                    evo(child, parent);
                    mut(child);
                }
            }
            calculateStats(); // calculate stats at end of gen
            writeGenAndRunStats(); // write gen and run stats at end of gen
            prepare(); // reset certain attributes at end of gen
        }
    }

    public void MCv1(){
//        for(gen = 1; gen <= gens; gen++){ // MC outer loop
//            for(int i = 0; i < N; i++) play(pop[i]);
//            for(int i=0;i<N;i++) updateUtility(pop[i]);
//            for(int i = 0; i < NIS; i++){ // MC inner loop
//                Player child = selRandomPop();
//                EWL(child); // EWL inside or outside inner step loop?
//                if(EWT.equals("rewire")) rewire(child); // rewire if applicable
//                Player parent = sel(child);
//                if(evo(child, parent))
//                    mut(child);
//            }
//            calculateStats();
//            writeGenAndRunStats();
//            prepare(); // reset certain attributes at end of gen
//        }

        System.out.println("[INFO] MCv1() has been decommissioned for now...");
    }

    public void MCv2(){
//        for(gen=1;gen<=gens;gen++){
//            for(int i=0;i<NIS;i++){
//                int random_int = ThreadLocalRandom.current().nextInt(N);
//                Player player = findPlayerByID(random_int);
//                play(player);
//                Player parent = selRandomNeigh(player);
//                evo(player, parent);
//            }
//        }

        System.out.println("[INFO] MCv2() has been decommissioned for now...");
    }

    // one agent may evolve per gen.
    public void oneGuyEvo(){
        for(gen=1;gen<=gens;gen++){
            for(int i=0;i<N;i++) {
                play(pop[i]); // play DG
            }
            if(!EWL.isEmpty()){
                for(int i=0;i<N;i++) {
                    EWL(pop[i]);
                }
            }
            switch(EWT){
                case "rewire" -> {
                    for(int i=0;i<N;i++){
                        rewire(pop[i]);
                    }
                }
                case "punish" -> {
                    for(int i=0;i<N;i++){
                        punish(pop[i]);
                    }
                }
            }
            int random_int = ThreadLocalRandom.current().nextInt(N);
            Player child = findPlayerByID(random_int);
            Player parent = sel(child);
            if(!child.equals(parent)) {
                evo(child, parent);
                mut(child);
            }
            calculateStats(); // calculate stats at end of gen
            writeGenAndRunStats(); // write gen and run stats at end of gen
            prepare(); // reset certain attributes at end of gen
        }
    }

    public static void assignVariations(String value){
        if(!value.isEmpty()){

//            for (String variation : value.split(";")) {
//                variations.add(variation);
//                System.out.println("variation="+variation);
//            }

//            String[] split = value.split(";");
//            for(int i=0;i<split.length;i++){
//                String variation = split[i];
//                variations.add(variation);
//                System.out.println("variation"+(i+1)+"="+variation);
//            }
//            exps = variations.size() + 1;

            variations = value.split(";");
            for(int i=0;i<variations.length;i++){
                System.out.println("variation"+(i+1)+"="+variations[i]);
            }
            exps = variations.length + 1;

        }
    }

    public static void assignWritePGenStats(String value){
        try{
            if(value.equals("1")){
                writePGenStats = true;
                System.out.println("writePGenStats="+writePGenStats);
            }
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("[INFO] Will not record p gen stats.");
        }
    }

    public static void assignWriteUGenStats(String value){
        try{
            if(value.equals("1")){
                writeUGenStats = true;
                System.out.println("writeUGenStats="+writeUGenStats);
            }
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("[INFO] Will not record u gen stats.");
        }
    }

    public static void assignWriteKGenStats(String value){
        try{
            if(value.equals("1")){
                writeKGenStats = true;
                System.out.println("writeKGenStats="+writeKGenStats);
            }
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("[INFO] Will not record k gen stats.");
        }
    }

    public static void assignWritePRunStats(String value){
        try{
            if(value.equals("1")){
                writePRunStats = true;
                System.out.println("writePRunStats="+writePRunStats);
            }
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("[INFO] Will not record p run stats.");
        }
    }

    public static void assignWriteURunStats(String value){
        try{
            if(value.equals("1")){
                writeURunStats = true;
                System.out.println("writeURunStats="+writeURunStats);
            }
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("[INFO] Will not record u run stats.");
        }
    }

    public static void assignWriteKRunStats(String value){
        try{
            if(value.equals("1")){
                writeKRunStats = true;
                System.out.println("writeKRunStats="+writeKRunStats);
            }
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("[INFO] Will not record k run stats.");
        }
    }

    public static void assignWriteRate(String value){
        if(writePGenStats || writeUGenStats || writeKGenStats || writePRunStats || writeURunStats || writeKRunStats){
            try{
                writeRate = Integer.parseInt(value);
                System.out.println("writeRate="+writeRate);
            }catch(NumberFormatException e){
                System.out.println("invalid writeRate");
                exit();
            }
        }
    }

    public static void assignWidth(){
        width = length;
        System.out.println("width="+width);
    }

    public static void assignN(){
        N = length * width;
        System.out.println("N="+N);
    }

    public static void assignGame(){
        Player.setGame(game);
        System.out.println("game="+game);
    }

    public static void assignRP(String value){
        switch(EWT){
            case "rewire" -> {
                try{
                    RP = Double.parseDouble(value);
                    System.out.println("RP="+RP);
                }catch(NumberFormatException e){
                    System.out.println("invalid RP: must be a double");
                    exit();
                }
                if(RP <= 0 || RP > 1){
                    System.out.println("invalid RP: must be within the interval (0, 1].");
                    exit();
                }
            }
        }
    }

    public static void assignRA(String value){
        switch(EWT){ // value must be valid if EWT = rewire.
            case "rewire" -> {
                switch(value){

                    // case where value is valid.
                    case "smoothstep", "smootherstep", "linear", "0Many" -> {
                        RA = value;
                        System.out.println("RA="+RA);
                    }

                    // case where value is invalid.
                    default -> {
                        System.out.println("invalid RA");
                        exit();
                    }
                }
            }
        }
    }

    public static void assignRT(String value){
        switch(EWT){ // value must be valid if EWT = rewire.
            case "rewire" -> {
                switch(value){

                    // case where value is valid.
                    case "local", "pop" -> {
                        RT = value;
                        System.out.println("RT="+RT);
                    }

                    // case where value is invalid.
                    default -> {
                        System.out.println("invalid RT");
                        exit();
                    }
                }
            }
        }
    }

    public static void assignEvo(String value){
        switch(value){

            // case where value is valid.
            case "copy",
                    "copyFitter",
                    "FD",
//                    "approach",
                    "UD",
                    "UDN" -> {
                evo = value;
                System.out.println("evo="+evo);
            }

            // case where value is invalid.
            default -> {
                System.out.println("invalid evo");
                exit();
            }
        }
    }

    public static void assignGenType(String value){
        switch(value){

            // case where value is valid.
            case "ER", "MCv1", "MCv2", "oneGuyEvo"-> {
                genType = value;
                System.out.println("genType="+genType);
            }

            // case where value is invalid.
            default -> {
                System.out.println("invalid genType");
                exit();
            }
        }
    }

    public static void assignNU(String value){
        switch(EWT){
            case "punish" -> {
                switch(value){
                    case "1" -> {
                        NU = true;
                        System.out.println("NU="+NU);
                    }
                    case "0" -> {
                        NU = false;
                        System.out.println("NU="+NU);
                    }
                    default -> {
                        System.out.println("invalid NU");
                        exit();
                    }
                }
            }
        }

//        try{
//            if(value.equals("1")){
//                NU = true;
//                System.out.println("NU="+NU);
//            }
//        }catch(ArrayIndexOutOfBoundsException e){
//            System.out.println("[INFO] Will not record p gen stats.");
//        }
    }
}