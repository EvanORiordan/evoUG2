import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Arrays;


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
    static int neighSize; // size of neighbours with random neighbourhood type.
    static int N; // population size
    static int run; // current run
    static int runs; // number of experiment runs to occur
    Player[] pop; // array of players; assumes 2D space
    double mean_p; // mean proposal value; informally referred to as the performance of an experiment
    double mean_q; // mean acceptance threshold
    double mean_u; // mean utility
    double mean_deg; // mean degree of population
    double sigma_p; // standard deviation of p
    double sigma_q; // standard deviation of q
    double sigma_u; // standard deviation of utility
    double sigma_deg; // standard deviation of degree
    double max_p; // highest p in population at a time
    int gen; // current generation
    static int gens; // number of generations to occur per experiment run
    static int rounds; // number of rounds of play and EWL
    static String UF; // utility function: indicates how utility is calculated
    static double T; // PD: temptation to defect
    static double R; // PD: reward for mutual coopeation
    static double P; // PD: punishment for mutual defection
    static double S; // PD: sucker's payoff for cooperating with a defector
    static double l; // loner's payoff
    static String varying = ""; // indicates which parameter will be varied in experiment series
    static ArrayList<String> variations = new ArrayList<>();
    static int exp; // indicates how far along we are through the experiment series
    static int exps = 1; // number of experiments in series
    static FileWriter fw;
    static BufferedReader br;
    static Scanner scanner = new Scanner(System.in);
    static String config_filename = "config.csv";
    static DecimalFormat DF1 = Player.getDF1(); // formats numbers to 1 decimal place
    static DecimalFormat DF2 = Player.getDF2(); // formats numbers to 2 decimal place
    static DecimalFormat DF4 = Player.getDF4(); // formats numbers to 4 decimal places
    static LocalDateTime old_timestamp; // timestamp of end of previous experiment in series
    static String project_path = Paths.get("").toAbsolutePath().toString();
    static String general_path = project_path + "\\csv_data"; // address where all data is recorded
    static String this_path; // address where stats for current experimentation is recorded
    static String exp_path; // address where stats for current experiment are stored
    static String run_path; // address where stats for current run are stored
    static boolean writePGenStats;
    static boolean writeUGenStats;
    static boolean writeDegGenStats;
    static boolean writePRunStats;
    static boolean writeURunStats;
    static boolean writeDegRunStats;
    static boolean writePosData;
    static int first_gen_recorded = 1;
    static int writeRate = 0; // write data every x gens
    static String pos_data_filename;
    static String EWT; // EW type
    static String EWL = ""; // EWL function
    static double ROC = 0; // rate of change: fixed learning amount to EW
    static double alpha = 0; // used in alpha-beta rating
    static double beta = 0; // used in alpha-beta rating
//    static String evo; // indicates which evolution function to call
    static String evo = "copy"; // indicates which evolution function to call
    static double evoNoise = 0; // noise affecting evolution
    static String sel; // indicates which selection function to call
    static double selNoise = 0.0; // noise affecting selection
    static String mut; // indicates which mutation function to call
    static double mutRate = 0.0; // probability of mutation
    static double mutBound = 0.0; // denotes max mutation possible
    static boolean selfMut; // indicates whether self mutation can occur. when enabled, mut always occurs. when disabled, mut does not occur if child is parent.
//    static String EM; // evolution mechanism: the mechanism by which evolution occurs.
    static String EM = "newER"; // evolution mechanism: the mechanism by which evolution occurs.
    static int ER = 0; // evolution rate: used in various ways to denote how often generations occur
    static int NIS = 0; // num inner steps: number of inner steps per generation using the monte carlo method; usually is set to value of N
    static String RWT = ""; // roulette wheel type
    static String RA = ""; // rewire away
    static String RT = ""; // rewire to
    static double RP = 0.0; // rewire probability
    static int injIter; // injection iteration: indicates when strategy injection will occur. 0 ==> no injection.
    static double injP = 0.0; // injection p: indicates p value to be injected
    static int injSize = 0; // injection cluster size: indicates size of cluster to be injected







    /**
     * Main method of Java program.
      */
    public static void main(String[] args) {
        configureEnvironment();
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
        System.out.println("Starting timestamp: "+start_timestamp);
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
     * Runs an experiment series. If varying parameter defined, vary it after each
     * subsequent experiment in the series.
     */
    public static void experimentSeries(){
        for(exp = 1; exp <= exps; exp++){
            System.out.println("start experiment " + exp);
            exp_path = this_path + "\\exp" + exp;
            createDataFolders();
            experiment(); // run an experiment of the series
            if(exp <= variations.size()){ // do not try to vary after the last experiment has ended
                switch(varying){
                    case "EWL" -> EWL = variations.get(exp - 1);
                    case "RA" -> RA = variations.get(exp - 1);
                    case "RT" -> RT = variations.get(exp - 1);
                    case "sel" -> sel = variations.get(exp - 1);
                    case "evo" -> evo = variations.get(exp - 1);
                    case "EWT" -> EWT = variations.get(exp - 1);
                    case "gens" -> gens = Integer.parseInt(variations.get(exp - 1));
                    case "length" -> {
                        length = Integer.parseInt(variations.get(exp - 1));
                        N = length * width;
                    }
                    case "ER" -> ER = Integer.parseInt(variations.get(exp - 1));
                    case "NIS" -> NIS = Integer.parseInt(variations.get(exp - 1));
                    case "ROC" -> ROC = Double.parseDouble(variations.get(exp - 1));
                    case "RP" -> RP = Double.parseDouble(variations.get(exp - 1));
                    case "M" -> M = Double.parseDouble(variations.get(exp - 1));
                    case "selNoise" -> selNoise = Double.parseDouble(variations.get(exp - 1));
                    case "mutRate" -> mutRate = Double.parseDouble(variations.get(exp - 1));
                    case "mutBound" -> mutBound = Double.parseDouble(variations.get(exp - 1));
                    case "UF" -> UF = variations.get(exp - 1);
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
//            System.out.println("start run " + run);
            run_path = exp_path + "\\run" + run;
            Env pop = new Env();
            pop.start();
        }
        writeSettings();
        writeSeriesStats();
    }



    /**
     * Core algorithm of the program:
     * (1) Initialise population.
     * (2) Initialise neighbourhoods and weights.
     * (3) Generations of population pass.
     */
    @Override
    public void start(){
        initRandomPop();
        for(int i=0;i<N;i++){
            switch(neighType){
                case"VN","Moore","dia"->assignAdjacentNeighbours(pop[i]);
                case"random"->assignRandomNeighbours(pop[i], neighSize);
                case"all"->assignAllNeighbours(pop[i]);
                default -> {
                    System.out.println("[ERROR] Invalid neighbourhood type configured. Exiting...");
                    Runtime.getRuntime().exit(0);
                }
            }
        }
        if(writePosData && run == 1) writePosData();
        for(int i=0;i<N;i++){
            initialiseEdgeWeights(pop[i]);
        }


        // get stats for gen 0
        calculateStats();
        writeGenAndRunStats();


        switch(EM){
//            case "oldER" -> {
////                gen = 1;
////                gens = 1;
//                gen = 0;
//                gens = 0;
//                // oldER alg iterates "rounds" times; 1 iteration of this loop ==> 1 "round"
////                for(int round = 1; round < rounds; round++){
//                for(int round = 1; round <= rounds; round++){
//                    for(int i=0;i<N;i++) play(pop[i]);
//                    for(int i=0;i<N;i++) updateUtility(pop[i]);
//                    for(int i=0;i<N;i++) EWL(pop[i]);
//                    if(round % ER == 0){ // if true, a generation is going to pass
//                        for(int i=0;i<N;i++) if(EWT.equals("rewire")) rewire(pop[i]); // rewire if applicable
//                        for(int i=0;i<N;i++) {
//                            Player child = pop[i];
//                            Player parent = sel(child);
//                            if(evo(child, parent))
//                                mut(child);
//                        }
//                        // calculate and write stats at end of gen
//                        calculateStats();
//                        gen++; // moving onto to the next gen
//                        gens++; // for recording the total number of gens that occurred
//                        writeGenAndRunStats();
//                    }
//                    prepare(); // reset certain attributes at end of round
//                }
//            }
            case "newER" -> {
                rounds = 0;
                for(gen = 1; gen <= gens; gen++){ // gens
                    for(int j = 0; j < ER; j++){ // rounds
                        for(int i=0;i<N;i++) play(pop[i]);
                        for(int i=0;i<N;i++) updateUtility(pop[i]);
//                        for(int i=0;i<N;i++) EWL(pop[i]);
                        if(!EWL.equals("")) for(int i=0;i<N;i++) EWL(pop[i]); // saves runtime when EWL disabled.
                        rounds++;
                    }
                    if(EWT.equals("rewire")) for(int i=0;i<N;i++) rewire(pop[i]); // rewire if applicable. saves runtime when rewiring disabled.
                    for(int i=0;i<N;i++) {
                        Player child = pop[i];
                        Player parent = sel(child);

//                        if(evo(child,parent))
//                            mut(child);

//                        evo(child,parent);
//                        mut(child);

//                        evo(child,parent);
//                        if(!child.equals(parent) || selfMut)
//                            mut(child);

                        if(!child.equals(parent)) // saves runtime when child is parent.
                            evo(child, parent);
                        if(selfMut || !child.equals(parent)) // if selfMut enabled, child always mutates. else, child only mutates if child is not parent.
                            mut(child);

                    }
                    calculateStats(); // calculate stats at end of gen
                    writeGenAndRunStats(); // write gen and run stats at end of gen
                    prepare(); // reset certain attributes at end of gen
                }
            }
            // my old version of MC simulation
//            case "MC" -> { // Monte Carlo (MC)
//                for(gen = 1; gen <= gens; gen++){ // MC outer loop
//                    for(int i = 0; i < N; i++) play(pop[i]);
//                    for(int i=0;i<N;i++) updateUtility(pop[i]);
//                    for(int i = 0; i < NIS; i++){ // MC inner loop
//                        Player child = selRandomPop();
//                        EWL(child); // EWL inside or outside inner step loop?
//                        if(EWT.equals("rewire")) rewire(child); // rewire if applicable
//                        Player parent = sel(child);
//                        if(evo(child, parent))
//                            mut(child);
//                    }
//                    calculateStats();
//                    writeGenAndRunStats();
//                    prepare(); // reset certain attributes at end of gen
//                }
//            }
            // my new version of MC simulation
            case "MCv2" -> {
                for(gen=1;gen<=gens;gens++){
                    for(int i=0;i<NIS;i++){
                        int random_int = ThreadLocalRandom.current().nextInt(N);
                        Player player = findPlayerByID(random_int);
                        play(player);
                        updateUtility(player);
                        // problem: neighbours have no utility yet therefore evo will not work as intended. to fix this, i could call updateUtility() in updateStats().
                        Player parent = selRandomNeigh(player);
                        evo(player, parent);
                    }
                }
            }
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

    public void play(Player a){
        ArrayList<Player> omega_a = a.getNeighbourhood(); // neighbourhood of a
        for(int i = 0; i < omega_a.size(); i++){
            Player b = omega_a.get(i); // neighbour of a
            switch(EWT) {
                default -> UG(a, b);
                case "proposalProb"-> {
                    ArrayList <Player> omega_b = b.getNeighbourhood(); // neighbourhood of b
                    for (int j = 0; j < omega_b.size(); j++) {
                        Player c = omega_b.get(j); // neighbour of b
                        if (a.equals(c)) {
                            double w_ba = b.getEdgeWeights().get(j); // weight of edge from b to a
                            double d = ThreadLocalRandom.current().nextDouble();
                            if(w_ba > d){
                                UG(a, b);
                            } else{
                                updateStats(a,0);
                                updateStats(b,0);
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
        updateStatsUG(a, pi_a);
        updateStatsUG(b, pi_b);
    }

    /**
     * Proposer initiates ultimatum game interaction with responder.<br>
     * If p_a >= q_b, b accepts the proposal made by a, otherwise b rejects it.<br>
     * Uses w_ba to calculate payoffs of players.
     * Prize is effectively cut by w_ba percent.<br>
     * @param a proposer
     * @param b responder
     * @param w_ba weight of edge from b to a
     */
    public void UG(Player a, Player b, double w_ba){
        double p_a = a.getP();
        double q_b = b.getQ();
        double pi_a = 0.0;
        double pi_b = 0.0;
        double modified_M = M * w_ba; // denote formally by M'
        if(p_a >= q_b) {
            pi_b = modified_M * p_a;
            pi_a = modified_M - pi_b;
        }
        updateStatsUG(a, pi_a);
        updateStatsUG(b, pi_b);
    }




    public void updateStatsUG(Player player, double payoff){
        player.setPi(player.getPi() + payoff);
        player.setMNI(player.getMNI() + 1);
    }





    /**
     * Prisoner's dilemma function.<br>
     * @param a player
     * @param b player
     */
    public void PD(Player a, Player b){
        String s_a = a.getStrategyPD();
        String s_b = b.getStrategyPD();
        double pi_a = 0.0;
        double pi_b = 0.0;
        if(s_a.equals("C") && s_b.equals("C")){
            pi_a = R;
            pi_b = R;
        } else if(s_a.equals("C") && s_b.equals("D")){
            pi_a = S;
            pi_b = T;
        }else if(s_a.equals("D") && s_b.equals("C")){
            pi_a = T;
            pi_b = S;
        }else if(s_a.equals("D") && s_b.equals("D")){
            pi_a = P;
            pi_b = P;
        }else if(s_a.equals("A") || s_b.equals("A")){
            pi_a = l;
            pi_b = l;
        }
        updateStatsPD(a, pi_a);
        updateStatsPD(b, pi_b);
    }
    /**
     * Prisoner's dilemma function.<br>
     * Uses w_ba to calculate payoffs of players.<br>
     * @param a player
     * @param b player
     */
    public void PD(Player a, Player b, double w_ba){
        String s_a = a.getStrategyPD();
        String s_b = b.getStrategyPD();
        double pi_a = 0.0;
        double pi_b = 0.0;
        if(s_a.equals("C") && s_b.equals("C")){
            pi_a = R * w_ba;
            pi_b = R * w_ba;
        } else if(s_a.equals("C") && s_b.equals("D")){
            pi_a = S * w_ba;
            pi_b = T * w_ba;
        }else if(s_a.equals("D") && s_b.equals("C")){
            pi_a = T * w_ba;
            pi_b = S * w_ba;
        }else if(s_a.equals("D") && s_b.equals("D")){
            pi_a = P * w_ba;
            pi_b = P * w_ba;
        }else if(s_a.equals("A") || s_b.equals("A")){
            pi_a = l * w_ba;
            pi_b = l * w_ba;
        }
        updateStatsPD(a, pi_a);
        updateStatsPD(b, pi_b);
    }



    public void updateStatsPD(Player player, double payoff){
        player.setPi(player.getPi() + payoff);
        player.setMNI(player.getMNI() + 1);
    }



    public void initialiseEdgeWeights(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        ArrayList <Double> edge_weights = new ArrayList<>();
        int size = neighbourhood.size();
        for(int i=0;i<size;i++){
            edge_weights.add(1.0);
        }
        player.setEdgeWeights(edge_weights);
    }



    /**
     * Edge Weight Learning (EWL)
     */
    public void EWL(Player a){
        ArrayList<Double> weights = a.getEdgeWeights();
        ArrayList<Player> omega_a = a.getNeighbourhood();
        for(int i = 0; i < omega_a.size(); i++){
            Player b = omega_a.get(i);
            double w_ab = weights.get(i); // weight from a to b
            w_ab += calculateLearning(a, b);
            if(w_ab > 1.0) w_ab = 1.0;
            else if(w_ab < 0.0) w_ab = 0.0;
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
        }
        return learning;
    }





    /**
     * Roulette wheel (RW) selection function where the fitter a candidate, the more likely
     * it is to be selected as parent. Candidates including child and neighbours.
     * @param child player undergoing roulette wheel selection
     */
    public Player selRW(Player child){
//        Player parent = null;
        // why not set parent to child by default?
        Player parent = child;
        ArrayList <Player> pool = new ArrayList<>(child.getNeighbourhood()); // pool of candidates for parent
        pool.add(child);
        int size = pool.size();
        double[] pockets = new double[size];
        double roulette_total = 0;
        for(int i = 0; i < size; i++){
            switch(RWT){
                case "normal" -> pockets[i] = pool.get(i).getU();
                case "exponential" -> pockets[i] = Math.exp(pool.get(i).getU() * selNoise);
            }
            roulette_total += pockets[i];
        }
        double random_double = ThreadLocalRandom.current().nextDouble();
        double tally = 0;
//        for(int i = 0; i < size; i++){
        for(int i = 0; i < size - 1; i++){ // no need to check for child: if noone is selected, child is parent by default
            tally += pockets[i];
            double percent_taken = tally / roulette_total; // how much space in the wheel has been taken up so far
            if(random_double < percent_taken){ // if true, the ball landed in the candidate's slot
                parent = pool.get(i); // select candidate as parent
                break;
            }
        }

        // by default, if parent is null, set parent to child.
//        if(parent == null)
//            System.out.println("BP");
//            parent = child;

        return parent;
    }



    /**
     * child selects fittest player in neighbourhood as parent unless there does not
     * exist fitter neighbour than child.
     * @param a child
     * @return parent
      */
    public Player selFittest(Player a){
        ArrayList<Player> omega_a = a.getNeighbourhood(); // denotes neighbourhood of child
        Player b = a; // denotes parent
        for(int i = 0; i < omega_a.size(); i++){
            Player c = omega_a.get(i); // denotes neighbour of child; candidate for parent
            if(c.getU() > b.getU()){ // if candidate fitter than parent, parent is set to candidate
                b = c;
            }
        }
        return b;
    }



    /**
     * sel and evo effectively occur at once in one function.
     * crossover where one child adopts midway point between two parent strategies.
     *
     * @param child
     */
    public void crossover(Player child){

        // how to select parents?

        // select two fittest neighbours?
        ArrayList <Player> neighbourhood = child.getNeighbourhood();
        Player parent1 = child; // fittest neighbour
        Player parent2 = child; // second-fittest neighbour
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            double neighbour_u = neighbour.getU();
            double parent2_u = parent2.getU();
            if(neighbour_u > parent2_u){
                parent2 = neighbourhood.get(i);
                parent2_u = parent2.getU();
                double parent1_mean_score = parent1.getU();
                if(parent2_u > parent1_mean_score){
                    Player temp = parent1;
                    parent1 = parent2;
                    parent2 = temp;
                }
            }
        }

        double p1 = parent1.getP();
        double p2 = parent2.getP();
        double new_p = (p1 + p2) / 2;
        child.setP(new_p);
    }




    /**
     * Child wholly copies parent's strategy.
     */
    public void evoCopy(Player child, Player parent){
        child.setP(parent.getOldP());
    }



    /**
     * Use evoNoise to move child strategy in direction of parent strategy.
     * @param child
     * @param parent
     */
    public void evoApproach(Player child, Player parent){
        int ID = child.getID();
        int parent_ID = parent.getID();
        double p = child.getP();
        double q = child.getQ();
        double parent_old_p = parent.getOldP();
        double parent_old_q = parent.getOldQ();

        // do not approach evolve if parent is child
        if(parent_ID != ID){

            // for attribute, if parent is lower, reduce child; else, increase.
            double approach = ThreadLocalRandom.current().nextDouble(evoNoise);
            if(parent_old_p < p){
                approach *= -1;
            }
            double new_p = p + approach;
            if(parent_old_q < q){
                approach *= -1;
            }
            double new_q = q + approach;

//            setStrategy(child, new_p, new_q);
            child.setP(new_p);
            child.setQ(new_q);
        }
    }



    /**
     * Mutation rate parameter determines the probability for mutation to occur.
     * @return boolean indicating whether mutation will occur
     */
    public boolean mutationCheck(){
        double random_double = ThreadLocalRandom.current().nextDouble();
        boolean mutation = random_double < mutRate;

        return mutation;
    }



    /**
     * Child's attributes are randomly and independently generated.
     */
    public void mutGlobal(Player child){
        double new_p = ThreadLocalRandom.current().nextDouble();
        double new_q = ThreadLocalRandom.current().nextDouble();
        child.setP(new_p);
        child.setQ(new_q);
    }



    /**
     * Slight mutations are independently applied to child's attributes.
     */
    public void mutLocal(Player child){
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



    public void calculateMeanEdgeWeights(){
        for(int i=0;i<N;i++){
            Player player = pop[i];
            calculateMeanSelfEdgeWeight(player);
            calculateMeanNeighbourEdgeWeight(player);
        }
    }



    public void calculateMeanSelfEdgeWeight(Player player){
        ArrayList <Double> weights = player.getEdgeWeights();
        int size = weights.size();
        double mean_self_edge_weight = 0;

        // calculate mean of edge weights from player to its neighbours
        for(int i = 0; i < size; i++){
            mean_self_edge_weight += weights.get(i);
        }
        mean_self_edge_weight /= size;

        player.setMeanSelfEdgeWeight(mean_self_edge_weight);
    }



    public void calculateMeanNeighbourEdgeWeight(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        int size = neighbourhood.size();
        double mean_neighbour_edge_weight = 0;

        // calculate mean of edge weights directed at player
        for(int i = 0; i < size; i++) {
            Player neighbour = neighbourhood.get(i);
            ArrayList <Double> weights = neighbour.getEdgeWeights();
            int index = findPlayerIndex(player, neighbour);
            mean_neighbour_edge_weight += weights.get(index);
        }
        mean_neighbour_edge_weight /= size;

        player.setMeanNeighbourEdgeWeight(mean_neighbour_edge_weight);
    }



    /**
     * Assume num player neighbours = num neighbour neighbours.<br>
     * Return index corresponding to player within neighbours neighbourhood.
     */
    public int findPlayerIndex(Player player, Player neighbour){
        int index = 0;
        int ID = player.getID();
        ArrayList <Player> neighbourhood = neighbour.getNeighbourhood();
        int size = neighbourhood.size();

        for (int i = 0; i < size; i++) {
            Player neighbour2 = neighbourhood.get(i);
            int ID2 = neighbour2.getID();
            if (ID == ID2) {
                index = i;
                break;
            }
        }

        return index;
    }



    /**
     * Loads in a configuration of settings from the config file, allowing the user to choose the values of the environmental parameters.
     */
    public static void configureEnvironment(){
        // load configurations
        ArrayList<String> configurations = new ArrayList<>(); // stores configs
        try{
            br = new BufferedReader(new FileReader(config_filename));
            String line; // initialise String to store rows of data
            br.readLine(); // purposely ignore the row of headings
            while((line = br.readLine()) != null){
                configurations.add(line);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        // display intro and config table headings
        System.out.printf("=========================================%n");
        System.out.printf("   Artificial Life Simulator%n");
        System.out.printf("   By Evan O'Riordan%n");
        printTableLine();
        System.out.printf("%-10s |"+//config
                " %-5s |"+//runs
                " %-10s |"+//length
                " %-5s |"+//ER
                " %-10s |"+//gens
                " %-15s |"+//EWT
                " %-5s |"+//RP
                " %-15s |"+//RA
                " %-5s |"+//RT
                " %-5s |"+//EWL
                " %-5s |"+//ROC
                " %-15s |"+//sel
                " %-15s |"+//RWT
                " %-10s |"+//selNoise
                " %-10s |"+//mut
                " %-10s |"+//mutRate
                " %-10s |"+//mutBound
                " %-10s |"+//selfMut
                " %-10s |"+//UF
                " %-5s |"+//WPGS
                " %-5s |"+//WUGS
                " %-5s |"+//WDGS
                " %-5s |"+//WPRS
                " %-5s |"+//WURS
                " %-5s |"+//WDRS
                " %-10s |"+//writeRate
                " %-10s |"+//varying
                " %s%n"//variations
                ,"config","runs","length","ER","gens","EWT","RP","RA","RT","EWL","ROC","sel","RWT","selNoise","mut","mutRate","mutBound","selfMut","UF","WPGS","WUGS","WDGS","WPRS","WURS","WDRS","writeRate","varying","variations"

        );
        printTableLine();

        // display config table rows
        int CI; // configuration index
        String[] settings;
        for(int i=0;i<configurations.size();i++){
            settings = configurations.get(i).split(",");
            CI = 0; // reset to 0 for each config
            System.out.printf("%-10d ", i); //config
            System.out.printf("| %-5s ", settings[CI++]); //runs
            System.out.printf("| %-10s ", settings[CI++]); //length
            System.out.printf("| %-5s ", settings[CI++]); //ER
            System.out.printf("| %-10s ", settings[CI++]); //gens
            System.out.printf("| %-15s ", settings[CI++]); //EWT
            System.out.printf("| %-5s ", settings[CI++]); //RP
            System.out.printf("| %-15s ", settings[CI++]); //RA
            System.out.printf("| %-5s ", settings[CI++]); //RT
            System.out.printf("| %-5s ", settings[CI++]); //EWL
            System.out.printf("| %-5s ", settings[CI++]); //ROC
            System.out.printf("| %-15s ", settings[CI++]); //sel
            System.out.printf("| %-15s ", settings[CI++]); //RWT
            System.out.printf("| %-10s ", settings[CI++]); //selNoise
            System.out.printf("| %-10s ", settings[CI++]); //mut
            System.out.printf("| %-10s ", settings[CI++]); //mutRate
            System.out.printf("| %-10s ", settings[CI++]); //mutBound
            System.out.printf("| %-10s ", settings[CI++]); //selfMut
            System.out.printf("| %-10s ", settings[CI++]); //UF
            System.out.printf("| %-5s ", settings[CI++]); //WPGS
            System.out.printf("| %-5s ", settings[CI++]); //WUGS
            System.out.printf("| %-5s ", settings[CI++]); //WDGS
            System.out.printf("| %-5s ", settings[CI++]); //WPRS
            System.out.printf("| %-5s ", settings[CI++]); //WURS
            System.out.printf("| %-5s ", settings[CI++]); //WDRS
            System.out.printf("| %-10s ", settings[CI++]); //writeRate
//            System.out.printf("| %-10s ", settings[CI++]); //varying
//            System.out.printf("| %s ", settings[CI++]); //variations
//            String x;
//            x = settings[CI++];
//            System.out.printf("| %-10s ", x.equals("")? x: ""); //varying
////            !x.equals("")? System.out.printf("| %-10s ", x):
//            x = settings[CI++];
//            System.out.printf("| %s ", x.equals("")? x: ""); //variations
            System.out.printf("| %-10s ", CI!=settings.length? settings[CI++]: ""); //varying
            System.out.printf("| %s ", CI!=settings.length? settings[CI++]: ""); //variations
            System.out.println();
        }
        printTableLine();

        // ask user which config they wish to use
        System.out.println("Which config would you like to use? (int)");
        boolean config_selected = false;
        int config_num;
        do{ // ensure user selects valid config
            config_num = scanner.nextInt();
            if(0 <= config_num && config_num < configurations.size()){
                config_selected = true;
            } else{
                System.out.println("[ERROR] Invalid config number, try again");
            }
        }while(!config_selected);


        // apply config
        settings = configurations.get(config_num).split(",");
        CI = 0;
        int CI2;
        int CI3;


        Player.setGame(game);
        runs = Integer.parseInt(settings[CI++]);
        if(runs < 1){
            System.out.println("[ERROR] Invalid runs passed");
            exit();
        }
        length = Integer.parseInt(settings[CI++]);
        if(length < 3){
            System.out.println("[ERROR] Invalid length passed");
            exit();
        }
        width = length;
        N = length * width;
        ER = Integer.parseInt(settings[CI++]);
        if(ER < 1){
            System.out.println("[ERROR] Invalid ER passed");
            exit();
        }
        gens = Integer.parseInt(settings[CI++]);
        if(gens < 1){
            System.out.println("[ERROR] Invalid gens passed");
            exit();
        }
        EWT = settings[CI++];
        switch(EWT){
            case "rewire" -> {
                try {
                    RP = Double.parseDouble(settings[CI++]); // numerical data types get an exception when input = ""
                    if(RP < 0.0 || RP > 1.0){
                        System.out.println("[ERROR] Invalid RP passed");
                        exit();
                    }
                }catch(NumberFormatException e){}
                RA = settings[CI++]; // strings dont get an exception when input = ""
                if(!(RA.equals("smoothstep") || RA.equals("smootherstep") || RA.equals("linear") || RA.equals("0Many"))){
                    System.out.println("[ERROR] Invalid RA passed");
                    exit();
                }
                RT = settings[CI++];
                if(!(RT.equals("local") || RT.equals("pop"))){
                    System.out.println("[ERROR] Invalid RT passed");
                    exit();
                }
            }
            case "proposalProb", "none" -> CI += 3; // max extra EWT params: 3 ==> skip CI to that index.
            default -> {
                System.out.println("[ERROR] Invalid EWT passed");
                exit();
            }
        }
        EWL = settings[CI++];
        switch(EWL){
            case "PROC", "UROC" -> {
                try {
                    ROC = Double.parseDouble(settings[CI++]);
                }catch(NumberFormatException e){}
                if(ROC < 0.0 || ROC > 1.0){
                    System.out.println("[ERROR] Invalid ROC passed");
                    exit();
                }
            }
            case "PD", "UD", "none" -> CI++; // max extra EWL params: 1 ==> skip CI that many indices.
            default -> {
                System.out.println("[ERROR] Invalid EWL passed");
                exit();
            }
        }
        sel = settings[CI++];
        switch(sel){
            case "RW" -> {
                RWT = settings[CI++];
                switch(RWT){
                    case "exponential" -> {
                        try {
                            selNoise = Double.parseDouble(settings[CI++]);
                        }catch(NumberFormatException e){}
                    }
                    case "normal" -> CI++;
                    default -> {
                        System.out.println("[ERROR] Invalid RWT passed");
                        exit();
                    }
                }
            }
            case "fittest", "randomNeigh", "randomPop", "rankBasedNeigh" -> CI += 2;
            default -> {
                System.out.println("[ERROR] Invalid sel passed");
                exit();
            }
        }
        mut = settings[CI++];
        switch(mut){
            case "global" -> {
                try {
                    mutRate = Double.parseDouble(settings[CI++]);
                }catch(NumberFormatException e){}
                if(mutRate < 0.0 || mutRate > 1.0){
                    System.out.println("[ERROR] Invalid mutRate passed");
                    exit();
                }
                CI++;
            }
            case "local" -> {
                try {
                    mutRate = Double.parseDouble(settings[CI++]);
                }catch(NumberFormatException e){}
                if(mutRate < 0.0 || mutRate > 1.0){
                    System.out.println("[ERROR] Invalid mutRate passed");
                    exit();
                }
                try {
                    mutBound = Double.parseDouble(settings[CI++]);
                }catch(NumberFormatException e){}
                if(mutBound < 0.0 || mutBound > 1.0){
                    System.out.println("[ERROR] Invalid mutBound passed");
                    exit();
                }
                switch(settings[CI++]){
                    case "0" -> selfMut = false;
                    case "1" -> selfMut = true;
                    case "" -> {}
                    default -> {
                        System.out.println("[ERROR] Invalid selfMut passed");
                        exit();
                    }
                }
            }
            default -> {
                System.out.println("[INFO] No mutation");
//                CI += 2;
                CI += 3; // increased from 2 to 3 due to the introduction of the selfmut param.
            }
        }
        UF = settings[CI++];
        switch(UF){
            case "cumulative", "normalised" -> {}
            default -> {
                System.out.println("[ERROR] Invalid UF passed");
                exit();
            }
        }
        switch(settings[CI++]){
            case "0" -> writePGenStats = false;
            case "1" -> writePGenStats = true; // its easier for user to pass a single digit than "true" or "false" for every writing boolean
            case "" -> {}
            default -> {
                System.out.println("[ERROR] Invalid WPGS passed");
                exit();
            }
        }
        switch(settings[CI++]){
            case "0" -> writeUGenStats = false;
            case "1" -> writeUGenStats = true;
            case "" -> {}
            default -> {
                System.out.println("[ERROR] Invalid WUGS passed");
                exit();
            }
        }
        switch(settings[CI++]){
            case "0" -> writeDegGenStats = false;
            case "1" -> writeDegGenStats = true;
            case "" -> {}
            default -> {
                System.out.println("[ERROR] Invalid WDGS passed");
                exit();
            }
        }
        switch(settings[CI++]){
            case "0" -> writePRunStats = false;
            case "1" -> writePRunStats = true;
            case "" -> {}
            default -> {
                System.out.println("[ERROR] Invalid WPRS passed");
                exit();
            }
        }
        switch(settings[CI++]){
            case "0" -> writeURunStats = false;
            case "1" -> writeURunStats = true;
            case "" -> {}
            default -> {
                System.out.println("[ERROR] Invalid WDegRS passed");
                exit();
            }
        }
        switch(settings[CI++]){
            case "0" -> writeDegRunStats = false;
            case "1" -> writeDegRunStats = true;
            case "" -> {}
            default -> {
                System.out.println("[ERROR] Invalid WDRS passed");
                exit();
            }
        }
        if(writePGenStats || writeUGenStats || writeDegGenStats || writePRunStats || writeURunStats || writeDegRunStats){
            writeRate = Integer.parseInt(settings[CI++]);
            if(writeRate < 1 || writeRate > gens){
                System.out.println("[ERROR] Invalid writeRate passed");
                exit();
            }
        }else{
            CI++;
        }
        if(CI!=settings.length){
            varying = settings[CI++];
            switch(varying){
                case "ER",
                        "ROC",
                        "length",
                        "RP",
                        "gens",
                        "EWL",
                        "RA",
                        "RT",
                        "sel",
                        "selNoise",
                        "mutRate",
                        "mutBound",
                        "UF" -> {
                    for(String variation: settings[CI].split(";")){
                        variations.add(variation);
                    }
                    exps = variations.size() + 1;
                }
                default -> {}
            }
        }

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
                        "%n");
    }



    public static void createDataFolders(){
        try{
            if(writeRate > 0) Files.createDirectories(Paths.get(exp_path));
            for(int i=1;i<=runs;i++){
                if(writePGenStats || writeUGenStats || writeDegGenStats || writePRunStats || writeURunStats || writeDegRunStats) // add run stat writing params to this check
                    Files.createDirectories(Paths.get(exp_path + "\\run" + i));
                if(writePGenStats || writeUGenStats || writeDegGenStats)
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
    public void assignAdjacentNeighbours(Player player){
        ArrayList<Player> neighbourhood = new ArrayList<>();
        double y = player.getY();
        double x = player.getX();
        for(int i=1;i<=neighRadius;i++){
            double x_plus = adjustPosition(x, i, width);
            double x_minus = adjustPosition(x, -i, width);
            double y_plus = adjustPosition(y, i, length);
            double y_minus = adjustPosition(y, -i, length);
            neighbourhood.add(findPlayerByPos(y,x_plus));
            neighbourhood.add(findPlayerByPos(y,x_minus));
            neighbourhood.add(findPlayerByPos(y_plus,x));
            neighbourhood.add(findPlayerByPos(y_minus,x));
            if(neighType.equals("dia")) {
                if(i > 1) {
                    double x_plus_minus = adjustPosition(x_plus, -1.0, width);
                    double x_minus_plus = adjustPosition(x_minus, 1.0, width);
                    double y_plus_minus = adjustPosition(y_plus, -1.0, length);
                    double y_minus_plus = adjustPosition(y_minus, 1.0, length);
                    neighbourhood.add(findPlayerByPos(y_plus_minus,(x_plus_minus)));
                    neighbourhood.add(findPlayerByPos(y_minus_plus,(x_plus_minus)));
                    neighbourhood.add(findPlayerByPos(y_minus_plus,(x_minus_plus)));
                    neighbourhood.add(findPlayerByPos(y_plus_minus,(x_minus_plus)));
                }
            }
            if(neighType.equals("Moore")){
                neighbourhood.add(findPlayerByPos(y_plus,x_plus));
                neighbourhood.add(findPlayerByPos(y_minus,x_plus));
                neighbourhood.add(findPlayerByPos(y_minus,x_minus));
                neighbourhood.add(findPlayerByPos(y_plus,x_minus));
            }
        }
        player.setNeighbourhood(neighbourhood);
    }



    /**
     * Randomly assigns either uni-directional or bi-directional edges to player.<br>
     * Assumes 2D square lattice grid population structure.
     * @param player
     * @param size
     */
    public void assignRandomNeighbours(Player player, int size){
        ArrayList<Player> neighbourhood = player.getNeighbourhood();
        Set<Integer> IDs = new HashSet<>();
        while(IDs.size() < size){
            int ID = ThreadLocalRandom.current().nextInt(N);
            IDs.add(ID);
        }
        for(int ID: IDs){
            neighbourhood.add(findPlayerByID(ID));
        }
    }



    // assign all other players to neighbourhood
    public void assignAllNeighbours(Player player){
        ArrayList <Player> neighbourhood = player.getNeighbourhood();
        int ID = player.getID();
        for(int i=0;i<N;i++){
            Player player2 = pop[i];
            int ID2 = player2.getID();
            if(ID != ID2){
                neighbourhood.add(player2);
            }
        }
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

    public void calculateMeanDegree(){
        mean_deg = 0;
        for(int i = 0; i < N; i++){
            mean_deg += pop[i].getDegree();
        }
        mean_deg /= N;
    }

    /**
     * IMPORTANT NOTE: mean deg has been substituted by 4.
     * You can get away with this because mean deg is always 4!
     * If program changes such that mean deg is not always 4, use mean_deg instead of 4!
     */
    public void calculateSigmaDeg(){
        sigma_deg = 0;
        for(int i = 0; i < N; i++){
//            sigma_deg += Math.pow(pop[i].getDegree() - mean_deg, 2);
            double deg = pop[i].getDegree();
            double deg_minus_mean_deg = deg - 4;
            sigma_deg += Math.pow(deg_minus_mean_deg, 2);
        }
//        sigma_deg = Math.pow(sigma_deg / N, 0.5);
        sigma_deg = Math.sqrt(sigma_deg / N);
    }



    public void prepare(){
        for(int i=0;i<N;i++){
            Player player = pop[i];
            player.setPi(0);
            player.setMNI(0);
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
        ArrayList<Player> omega_a = a.getNeighbourhood(); // omega_a denotes neighbourhood of rewirer.
        for(Player b: omega_a){ // b denotes neighbour of rewirer
            ArrayList<Player> omega_b = b.getNeighbourhood(); // omega_b denotes neighbourhood of neighbour of rewirer.
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
        if(pool.size() == 0) RTPop(a, num_rewires); // if pool empty, default to rewiring to a random player in the pop.
        else{ // connect to local player.
            for(int rewires_done = 0; rewires_done < num_rewires; rewires_done++){
                Player f = pool.get(ThreadLocalRandom.current().nextInt(pool.size())); // f denotes new neighbour of a.
                omega_a.add(f); // connect a to f.
                a.getEdgeWeights().add(1.0);
                f.getNeighbourhood().add(a); // connect f to a.
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
            ArrayList<Player> omega_a = a.getNeighbourhood(); // denotes neighbourhood of a.
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
            d.getNeighbourhood().add(a); // connect d to a.
            d.getEdgeWeights().add(1.0);
        }
    }



    /**
     * Generic function to update stats
     */
    public void updateStats(Player player, double payoff){
        player.setPi(player.getPi() + payoff);
        player.setMNI(player.getMNI() + 1);
    }




    /**
     * use the varying field to help determine whether a field should be
     * included in the settings String. if a field equals 0.0 and is not being
     * varied, it does not need to be added.<br>
     */
    public static void writeSettings(){
        if(writeRate > 0){
            String settings_filename = this_path + "\\" + "settings.csv";
            String settings = "";
            if(exp == 1){

//                settings += "game";
//                settings += ",runs";

                settings += "runs";

                settings += ",space";
                settings += ",length";
                settings += ",width";
                settings += ",N";
//                settings += ",neighType";
//                settings += neighRadius == 0 && !varying.equals("neighRadius")? "": ",neighRadius";
//                settings += neighSize == 0 && !varying.equals("neighSize")? "": ",neighSize";
                settings += ",EM";
                settings += ER != 0? ",ER": "";
                settings += NIS != 0? ",NIS": "";
                settings += ",gens";
                settings += rounds != 0? ",rounds": "";
                settings += ",EWT";
                settings += RP == 0.0 && !varying.equals("RP")? "": ",RP";
                settings += RA.equals("")? "": ",RA";
                settings += RT.equals("")? "": ",RT";
                settings += EWL.equals("")? "": ",EWL";
                settings += ROC == 0.0 && !varying.equals("ROC")? "": ",ROC";
//                settings += alpha == 0.0 && !varying.equals("alpha")? "": ",alpha";
//                settings += beta == 0.0 && !varying.equals("beta")? "": ",beta";
                settings += ",sel";
                settings += sel.equals("RW")? ",RWT": "";
                settings += RWT.equals("exponential")? ",selNoise": "";
//                settings += ",evo";
//                settings += evoNoise == 0.0 && !varying.equals("evoNoise")? "": ",evoNoise";
                settings += !mut.equals("")? ",mut": "";
//                settings += mut.equals("local") || mut.equals("global") || mut.equals("localnoself")? ",mutRate": "";
                settings += mut.equals("local") || mut.equals("global")? ",mutRate": "";
//                settings += mut.equals("local") || mut.equals("localnoself")? ",mutBound": "";
                settings += mut.equals("local")? ",mutBound": "";
                settings += !mut.equals("")? ",selfMut": "";
                settings += ",UF";
//                settings += injIter == 0? "": ",injIter";
//                settings += injP == 0.0? "": ",injP";
//                settings += injSize == 0? "": ",injSize";
            }
            settings += "\n";

//            settings += game;
//            settings += "," + runs;

            settings += runs;

            settings += "," + space;
            settings += "," + length;
            settings += "," + width;
            settings += "," + N;
//            settings += "," + neighType;
//            settings += neighRadius == 0 && !varying.equals("neighRadius")? "": "," + neighRadius;
//            settings += neighSize == 0 && !varying.equals("neighSize")? "": "," + neighSize;
            settings += "," + EM;
            settings += ER != 0? "," + ER: "";
            settings += NIS != 0? "," + NIS: "";
            settings += "," + gens;
            settings += rounds != 0? "," + rounds: "";
            settings += "," + EWT;
            settings += RP == 0.0 && !varying.equals("RP")? "": "," + RP;
            settings += RA.equals("")? "": "," + RA;
            settings += RT.equals("")? "": "," + RT;
            settings += EWL.equals("")? "": "," + EWL;
            settings += ROC == 0.0 && !varying.equals("ROC")? "": "," + ROC;
//            settings += alpha == 0.0 && !varying.equals("alpha")? "": "," + alpha;
//            settings += beta == 0.0 && !varying.equals("beta")? "": "," + beta;
            settings += "," + sel;
            settings += sel.equals("RW")? "," + RWT: "";
            settings += RWT.equals("exponential")? "," + selNoise: "";
//            settings += "," + evo;
//            settings += evoNoise == 0.0 && !varying.equals("evoNoise")? "": "," + evoNoise;
            settings += !mut.equals("")? "," + mut: "";
            settings += mut.equals("local") || mut.equals("global")? "," + mutRate: "";
            settings += mut.equals("local")? "," + mutBound: "";
            settings += !mut.equals("")? "," + selfMut: "";
            settings += "," + UF;
//            settings += injIter == 0? "": "," + injIter;
//            settings += injP == 0.0? "": "," + injP;
//            settings += injSize == 0? "": "," + injSize;
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
        if(writeRate > 0 && (writePRunStats || writeURunStats || writeDegRunStats)){
            String series_stats_filename = this_path + "\\series_stats.csv";
            String exp_stats_filename = exp_path + "\\exp_stats.csv";
            String output = "";
            if(exp == 1) {
//                if(writePRunStats)output+="mean mean p,sigma mean p,";
//                if(writeURunStats)output+="mean mean u,";
                if(writePRunStats)output+="mean avg p,sigma mean p,";
                if(writeURunStats)output+="mean avg u,";
//                if(writeDegRunStats && EWT.equals("rewire"))output+="mean sigma deg,";
                if(writeDegRunStats)output+="mean sigma deg,";
                if(!varying.equals(""))output+=varying+",";
                output = removeTrailingComma(output);
            }
//            double mean_mean_p = 0.0;
            double mean_avg_p = 0.0;
            double sigma_mean_p = 0.0;
//            double mean_mean_u = 0.0;
            double mean_avg_u = 0.0;
            double mean_sigma_deg = 0.0;
            double[] mean_p_values = new double[runs];
            double[] mean_u_values = new double[runs];
            double[] sigma_deg_values = new double[runs];
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
//                    if(writeDegRunStats && EWT.equals("rewire")) {
                    if(writeDegRunStats) {
                        sigma_deg_values[i] = Double.parseDouble(row_contents[j]);
                    }
                }
                for(int i=0;i<runs;i++){
                    if(writePRunStats)mean_avg_p += mean_p_values[i];
                    if(writeURunStats)mean_avg_u += mean_u_values[i];
//                    if(writeDegRunStats && EWT.equals("rewire")) mean_sigma_deg += sigma_deg_values[i];
                    if(writeDegRunStats) mean_sigma_deg += sigma_deg_values[i];
                }
                mean_avg_p /= runs;
                mean_avg_u /= runs;
//                mean_sigma_deg /= runs;
                if(writeDegRunStats) mean_sigma_deg /= runs;
                for(int i=0;i<runs;i++){
                    sigma_mean_p += Math.pow(mean_p_values[i] - mean_avg_p, 2);
                }
                sigma_mean_p = Math.pow(sigma_mean_p / runs, 0.5);


//                output += "\n" + DF4.format(mean_mean_p) + "," + DF4.format(sigma_mean_p) + "," + DF4.format(mean_mean_u) +"," + DF4.format(mean_sigma_deg);


                output += "\n";
                if(writePRunStats)output+=DF4.format(mean_avg_p) + "," + DF4.format(sigma_mean_p) + ",";
                if(writeURunStats)output+=DF4.format(mean_avg_u) + ",";
//                if(writeDegRunStats && EWT.equals("rewire"))output+=DF4.format(mean_sigma_deg) + ",";
                if(writeDegRunStats)output+=DF4.format(mean_sigma_deg) + ",";
                output = removeTrailingComma(output);


                switch(varying){
                    case "ER" -> output += "," + ER;
                    case "NIS" -> output += "," + NIS;
                    case "ROC" -> output += "," + ROC;
                    case "length" -> output += "," + length;
                    case "RP" -> output += "," + RP;
                    case "gens" -> output += "," + gens;
                    case "EWL" -> output += "," + EWL;
                    case "EWT" -> output += "," + EWT;
                    case "RA" -> output += "," + RA;
                    case "RT" -> output += "," + RT;
                    case "sel" -> output += "," + sel;
                    case "evo" -> output += "," + evo;
                    case "selNoise" -> output += "," + selNoise;
                    case "mutRate" -> output += "," + mutRate;
                    case "mutBound" -> output += "," + mutBound;
                    case "UF" -> output += "," + UF;
                }
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





    /**
     * Calculate utility of player.<br>
     * With MNI UF, divide by minimum number of interactions player could have had (this gen/round); functionally equivalent to the old average score metric. Indicates what the player earned from its average interaction.<br>
     * With normalised UF, divide by degree. Indicates what the player earned from interacting with its average neighbour.
     */
    public void updateUtility(Player player){
        switch(UF){
            case "MNI" -> { // minimum number of interactions; with neighType="VN", neighRadius=1, no rewiring, MNI of all players is always 8.
                if(player.getNeighbourhood().size() == 0)
                    player.setU(0.0);
                else
                    player.setU(player.getPi() / player.getMNI());
            }
            case "cumulative" -> player.setU(player.getPi()); // cumulative payoff
            case "normalised" -> { // normalised payoff; with neighType="VN", neighRadius=1, no rewiring, denominator is always 4.
                if(player.getNeighbourhood().size() == 0){
                    player.setU(0.0);
                }else{
                    player.setU(player.getPi() / player.getNeighbourhood().size());
                }
            }
        }
    }






    public Player selRandomNeigh(Player a){
        ArrayList<Player> omega_a = a.getNeighbourhood();
        Player parent = null;
        try{
            parent = omega_a.get(ThreadLocalRandom.current().nextInt(omega_a.size()));
        }catch(IllegalArgumentException e){ // if a has no neighbours, it is set as its own parent.
            parent = a;
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
        double random_number = ThreadLocalRandom.current().nextDouble();
        if(RP > random_number){
            int num_rewires = 0;
            ArrayList<Player> omega_a = a.getNeighbourhood();
            ArrayList<Double> weights = a.getEdgeWeights();
            ArrayList<Integer> indices_of_edges_to_be_rewired = new ArrayList<>();
            for(int i = 0; i < omega_a.size(); i++){
                double w_ab = weights.get(i); // w_ab denotes weighted edge from a to neighbour b
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
                ArrayList<Player> omega_e = e.getNeighbourhood();
                for(int j = 0; j < omega_e.size(); j++){
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
    }



    /**
     * evoUDN: evolution based on normalised utility difference.<br>
     * Probability to evolve equals utility difference divided by degree.<br>
     * Child fitter than parent mean no chance.<br>
     * Fitter parent means greater probability.<br>
     * Greater child degree means lesser probability.<br>
     * Inspired by cardinot2016optional.<br>
     * @param child
     * @param parent
     */
//    public void evoUD(Player child, Player parent){
    public void evoUDN(Player child, Player parent){
        double random_number = ThreadLocalRandom.current().nextDouble();
        double prob_evolve = (parent.getU() - child.getU()) / child.getDegree();
        if(random_number < prob_evolve)
            evoCopy(child, parent);
    }



    /**
     * Write stats of experiment.
     * 1 file per exp.
     */
    public void writeExpStats() {
        if(writeRate > 0 && (writePRunStats || writeURunStats || writeDegRunStats)){
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
            case "fittest" -> parent = selFittest(child);
            case "randomNeigh" -> parent = selRandomNeigh(child);
            case "randomPop" -> parent = selRandomPop();
//            case "crossover" -> crossover(child);
//            case "RW2" -> parent = selRW2(child);
            case "rankBasedNeigh" -> parent = selRankBasedNeigh(child);
        }
        return parent;
    }



////    public void evo(Player child, Player parent){
//    public boolean evo(Player child, Player parent){
//        boolean occurred = false;
////        if(parent != null){
//        if(!parent.equals(child)){
//            switch (evo) {
//                case "copy" -> evoCopy(child, parent);
//                case "approach" -> evoApproach(child, parent);
//                case "copyFitter" -> evoCopyFitter(child, parent);
//                case "UD" -> evoUD(child, parent);
//                case "UDN" -> evoUDN(child, parent);
//            }
//
//            occurred = true;
//
//        }
//
//        return occurred;
//
//    }


    public void evo(Player child, Player parent){
        switch (evo) {
            case "copy" -> evoCopy(child, parent);
            case "approach" -> evoApproach(child, parent);
            case "copyFitter" -> evoCopyFitter(child, parent);
            case "UD" -> evoUD(child, parent);
            case "UDN" -> evoUDN(child, parent);
        }
    }



    public void mut(Player child){
        switch (mut){
            case "global" -> {
                if(mutationCheck())
                    mutGlobal(child);
            }
            case "local" -> {
                if(mutationCheck())
                    mutLocal(child);
            }
        }
    }

//    public void mut2(Player child, Player parent){
//        switch (mut) {
//            case "global" -> {
//                if (mutationCheck())
//                    mutGlobal(child);
//            }
//            case "local" -> {
//                if (mutationCheck())
//                    mutLocal(child);
//            }
//            case "localnoself" -> {
//                if(parent != null){
//                    if (!parent.equals(child)) {
//                        if (mutationCheck()) {
//                            mutLocal(child);
//                        }
//                    }
//                }
//            }
//        }
//    }



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
        for(Player player: pop){
            player.calculateDegree();
        }
        if(writePRunStats) {
            calculateMeanP();
            calculateSigmaP();
            calculateMaxP();
        }
        if(writeURunStats) {
            calculateMeanU();
            calculateSigmaU();
        }
        if(writeDegRunStats) {
            // no need to calculate mean deg as it is always 4. if this changes, be sure to calculate it here!
            calculateSigmaDeg();
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
//        if(writeRate != 0 && gen % writeRate == 0) {
//        if(gen == 0 || (writeRate != 0 && gen % writeRate == 0)) {
        if(writeRate != 0 && (gen == 0 || gen % writeRate == 0)) {
            if(writePGenStats || writeUGenStats || writeDegGenStats) writeGenStats();
            if(writePRunStats || writeURunStats || writeDegRunStats) writeRunStats();
        }
    }



    /**
     * Child copies parent if parent fitter.
     * @param child
     * @param parent
     */
    public void evoCopyFitter(Player child, Player parent){
        if(parent.getU() > child.getU()) evoCopy(child, parent);
    }



    /**
     * evoUDN: evolution based on utility difference.<br>
     */
    public void evoUD(Player child, Player parent){
        double random_number = ThreadLocalRandom.current().nextDouble();
        double prob_evolve = (parent.getU() - child.getU());
        if(random_number < prob_evolve)
            evoCopy(child, parent);
    }




    /**
     * Writes the attributes (p, u, deg) of all players in the pop at gen t.
     * 1 file per gen.
     */
    public void writeGenStats(){
        String filename = run_path + "\\gen_stats\\gen" + gen + ".csv";
        String s = "";
//        s += "gen,";
        if(writePGenStats) s += "p,mean p omega,";
        if(writeUGenStats) s += "u,";
        if(writeDegGenStats) s += "deg,";
        s = removeTrailingComma(s);
        for(Player player: pop){
            s += "\n";
//            s += gen + ",";
            if(writePGenStats) s += DF4.format(player.getP()) + "," + DF4.format(player.getMeanPOmega()) + ",";
            if(writeUGenStats) s += DF4.format(player.getU()) + ",";
            if(writeDegGenStats) s += DF4.format(player.getDegree()) + ",";
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
            if (writeDegRunStats) s += "sigma deg,";
            s = removeTrailingComma(s);
            s += "\n";
        }
        s += gen + ",";
        if(writePRunStats) s += DF4.format(mean_p) + "," + DF4.format(sigma_p) + "," + DF4.format(max_p) + ",";
        if(writeURunStats) s += DF4.format(mean_u) + "," + DF4.format(sigma_u) + ",";
//        if(writeDegRunStats && EWT.equals("rewire")) s += DF4.format(sigma_deg) + ",";
        if(writeDegRunStats) s += DF4.format(sigma_deg) + ",";
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
    public Player selRankBasedNeigh(Player child){
        // get candidates
        Player parent = child; // if no neighbour is selected, child is parent by default
        ArrayList <Player> pool = new ArrayList<>(child.getNeighbourhood()); // pool of candidates for parent
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

        // probability of selection for candidate = rank / T_n (where T_n denotes nth triangular number and n denotes candidate pool size)
        // lowest rank: 1
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
}
