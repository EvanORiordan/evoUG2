import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Evan O'Riordan (e.oriordan3@universityofgalway.ie)<br>
 * 02/10/2023<br>
 * School of Computer Science<br>
 * University of Galway<br>
 *
 *
 * DG Algorithm.<br>
 */
public class Alg1 extends Thread{
    static int rows; // how many rows in the square grid.
    static int columns; // how many rows in the square grid.
    static int N; // population size.
    static int gens; // how many generations occur per experiment run.
    static int runs; // how many times this experiment will be run.
    static int EPR; // how often evolutionary phases occur e.g if 5, then evo occurs every 5 gens
    static double ROC; // rate of change wrt EWL1
    ArrayList<ArrayList<Player>> grid = new ArrayList<>(); // contains the population.
    double avg_p; // the average value of p across the population.
    static DecimalFormat DF1 = Player.getDF1(); // formats numbers to 1 decimal place
    static DecimalFormat DF4 = Player.getDF4(); // formats numbers to 4 decimal places
    int gen = 0; // indicates which generation is currently running.

    /**
     * Indicate whether to run an experiment or an experiment series.<br>
     * A series should be used when you want to vary a parameter of the algorithm between experiments.
     * Otherwise, a single experiment should suffice.
     */
    static boolean experiment_series;

    /**
     * varying_parameter indicates which parameter will be varied in an experiment series.<br>
     *
     * variation indicates by how much the parameter will vary between subsequent experiments in the
     * series. the double type here also works for varying integer type params such as gens.<br>
     *
     * num_experiments indicates the number of experiments to occur in the series.<br>
     *
     * experiment_number tracks which experiment is taking place at any given time during a series.
     */
    static String varying_parameter;
    static double variation;
    static int num_experiments;
    static int experiment_number = 0;



    // Prefix for filenames of generic data files.
    static String data_filename_prefix = "csv_data\\" +
            Thread.currentThread().getStackTrace()[1].getClassName();

    // Prefix for filenames of interaction data files.
    static String interaction_data_filename_prefix = "csv_data\\interactions_data\\" +
            Thread.currentThread().getStackTrace()[1].getClassName();

    static int interaction_data_record_rate;
    static int data_gen;

    // Prefix for filenames of player data files.
    static String player_data_filename_prefix = "csv_data\\player_data\\" +
            Thread.currentThread().getStackTrace()[1].getClassName();

    static String config_filename = "config.csv"; // filename of config file

    static String selection_method;
    static String evolution_method;
    static String mutation_method;
    static String edge_weight_learning_method;







    // main method for executing the program
    public static void main(String[] args) {

        // Scanner object for receiving input
        Scanner scanner = new Scanner(System.in);

        // does user want to load a pre-made configuration from the config file or manually set up?
        boolean config;
        System.out.println("config file or manual set up? (1: config, 2: manual)");
        switch(scanner.nextInt()){
            case 1 -> {
                config = true;
            }
            case 2 -> {
                config = false;
            }
            default -> {
                System.out.println("ERROR: select a valid option");
                return;
            }
        }

        if(config){ // if user wants to use config file
            ArrayList<String> configurations = new ArrayList<>(); // stores configurations
            try{  // read in the configs from the file
                BufferedReader br = new BufferedReader(new FileReader(config_filename));
                String line;
                line = br.readLine(); // ignore the row of headings
                while((line = br.readLine()) != null){
                    configurations.add(line);
                }
            } catch(IOException e){
                e.printStackTrace();
            }

            // display configs to user
            for(int i=0;i<configurations.size();i++){
                String[] settings = configurations.get(i).split(",");
                System.out.print("config "+i+": ");
                System.out.print("runs = "+settings[0]);
                System.out.print(", gens = "+settings[1]);
                System.out.print(", rows = "+settings[2]);
                System.out.print(", EPR = "+settings[3]);
                if(Double.parseDouble(settings[4]) > 0.0){
                    System.out.print(", EWL1 with ROC = " + settings[4]);
                } else{
                    System.out.print(", EWL2");
                }
                System.out.print(", varying parameter = "+settings[5]);
                if(!settings[5].equals("none")){
                    System.out.print(", variation = "+settings[6]+
                            ", num experiments = "+settings[7]);
                }
                System.out.print(", description = "+settings[8]);
                System.out.println();
            }

            // ask user which config they want to use
            System.out.println("which config would you like to use? (int)");
            int config_num = scanner.nextInt();
            for(int i=0;i<configurations.size();i++){
                if(config_num == i){
                    String[] settings = configurations.get(i).split(",");
                    runs = Integer.parseInt(settings[0]);
                    gens = Integer.parseInt(settings[1]);
                    rows = Integer.parseInt(settings[2]);
                    EPR = Integer.parseInt(settings[3]);
                    ROC = Double.parseDouble(settings[4]);
                    if(ROC > 0.0){
                        edge_weight_learning_method = "1";
                    } else {
                        edge_weight_learning_method = "2";
                    }
                    varying_parameter = settings[5];
                    if(!varying_parameter.equals("none")) {
                        experiment_series = true;
                        variation = Double.parseDouble(settings[6]);
                        num_experiments = Integer.parseInt(settings[7]);
                    }
                    break;
                }
            }


            // default settings:
            // automatically assign von neumann as neighbourhood
            Player.setNeighbourhoodType("VN");
            // automatically assign selection method
            selection_method = "WRW";
            // automatically assign evolution method
            evolution_method = "copy";
            // automatically assign mutation method
            mutation_method = "none";
            // automatically assign data_gen and interaction_data_record_rate
            data_gen = gens;
            interaction_data_record_rate = gens;

        } // end of config file code


        else {  // if user wants to define algorithm parameters at runtime

            System.out.println("series or experiment? (1: series; 2: experiment)");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> experiment_series = true;
                default -> experiment_series = false;
            }

            System.out.println("runs? (int)");
            runs = scanner.nextInt();

            System.out.println("rows and columns? (int)");
            rows = scanner.nextInt();

            System.out.println("gens? (int)");
            gens = scanner.nextInt();

            System.out.println("neighbourhood type? (VN, M)");
            Player.setNeighbourhoodType(scanner.next());
            String neighbourhood_type = Player.getNeighbourhoodType();
            switch (neighbourhood_type) {
                case "VN", "M" -> {}
                default -> {
                    System.out.println("ERROR: invalid neighbourhood type");
                    return; // terminate the program
                }
            }

            System.out.println("selection method? (WRW, best, variable)");
            selection_method=scanner.next();
            switch (selection_method) {
                case "WRW", "best" -> {}
                case "variable" -> {
                    System.out.println("intensity of selection (w)? (double)");
                    Player.setW(scanner.nextDouble());
                }
                default -> {
                    System.out.println("ERROR: invalid selection method");
                    return; // terminate the program
                }
            }

            System.out.println("evolution method? (copy, imitation, approach)");
            evolution_method = scanner.next();
            switch (evolution_method) {
                case "copy" -> {}
                case "imitation" -> {
                    System.out.println("imitation noise? (double)");
                    Player.setImitationNoise(scanner.nextDouble());
                }
                case "approach" -> {
                    System.out.println("approach noise? (double)");
                    Player.setApproachNoise(scanner.nextDouble());
                }
                default -> {
                    System.out.println("ERROR: invalid evolution method");
                    return; // terminate the program
                }
            }

            System.out.println("mutation? (none, new)");
            mutation_method = scanner.next();
            switch(mutation_method){
                case "new" -> {
                    System.out.println("mutation rate? (double)");
                    Player.setMutationRate(scanner.nextDouble());
                }
                default -> System.out.println("NOTE: no mutation");
            }

            System.out.println("EPR? (int)");
            EPR = scanner.nextInt();

            System.out.println("EWL? (1, 2)");
            switch (edge_weight_learning_method) {
                case "1" -> {
                    System.out.println("ROC? (double)");
                    ROC = scanner.nextDouble();
                }
                case "2" -> {
                }
                default -> {
                    System.out.println("ERROR: no EWL");
                    return;
                }
            }

            if (experiment_series) { // user configures series parameters
            System.out.println("varying parameter? (runs, gens, rows, EPR, ROC, " +
                    "imitation_noise, approach_noise, w, mutation_rate)");
                varying_parameter = scanner.next();

                System.out.println("variation amount?");
                variation = scanner.nextDouble();

                System.out.println("number of experiments? (int)");
                num_experiments = scanner.nextInt();
            }
            else {

                // manually assign data_gen and interaction_data_record_rate at runtime
                System.out.println("data gen? (int)");
                data_gen = scanner.nextInt();
                System.out.println("interaction data record rate? (int)");
                interaction_data_record_rate = scanner.nextInt();

            }
        }




        Player.setPrize(1.0);
        columns = rows;
        N = rows * columns;


        // mark the beginning of the algorithm's runtime
        Instant start = Instant.now();
        System.out.println("Timestamp: " + java.time.Clock.systemUTC().instant());

        if(experiment_series){
            experimentSeries(); // run an experiment series
        } else {
            experiment(); // run a single experiment
        }

        // mark the end of the algorithm's runtime
        System.out.println("Timestamp: " + java.time.Clock.systemUTC().instant());
        Instant finish = Instant.now();
        long secondsElapsed = Duration.between(start, finish).toSeconds();
        long minutesElapsed = Duration.between(start, finish).toMinutes();
        System.out.println("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
    }







    /**
     * Method for running the core algorithm at the heart of the program.
     */
    public void start(){

        // initialise the population
        for(int i=0;i<rows;i++){
            ArrayList<Player> row = new ArrayList<>();
            for(int j=0;j<columns;j++){
                row.add(new Player(ThreadLocalRandom.current().nextDouble(), 0.0));
            }
            grid.add(row);
        }
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);
                grid.get(i).get(j).initialiseEdgeWeights();
            }
        }


        // players begin playing the game
        while(gen != gens) { // algorithm stops once this condition is reached
            // playing phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.play();
                }
            }


            // edge weight learning phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    switch(edge_weight_learning_method){
                        case "1" ->{
                            player.edgeWeightLearning1(ROC);
                        }
                        case "2" ->{
                            player.edgeWeightLearning2();
                        }
                    }
                }
            }


            /**
             * Selection and evolution occur every EPR gens.
             * Each player in the grid tries to evolve.
             */
            if((gen + 1) % EPR == 0) {
                for (ArrayList<Player> row : grid) {
                    for (Player player : row) {

                        // select parent
                        Player parent = null;
                        switch(selection_method){
                            case "WRW" -> parent = player.weightedRouletteWheelSelection();
                            case "best" -> parent = player.bestSelection();
                            case "variable" -> parent = player.variableSelection();
                        }

                        // evolve child
                        switch (evolution_method) {
                            case "copy" -> player.copyEvolution(parent);
                            case "imitation" -> player.imitationEvolution(parent);
                            case "approach" -> player.approachEvolution(parent);
                        }

                        // mutate child
                        switch (mutation_method){
                            case "new" -> {
                                if(player.mutationCheck()){
                                    player.newMutation();
                                }
                            }
                        }
                    }
                }
            }

            /**
             * Only collect this data if interested in the results (of this individual
             * run) of this individual experiment.
             */
            if(!experiment_series && runs == 1){
                getStats();
                writePerGenData(data_filename_prefix + "PerGenData.csv");

                // write interaction data every once in a while
                if(gen % interaction_data_record_rate == 0){
                    writeInteractionData(interaction_data_filename_prefix + "Gen" + gen);
                }


                if(gen==data_gen){
                    System.out.println("Recording detailed data for gen "+data_gen+"...");
                    writeStrategies(data_filename_prefix + "Strategies.csv");
                    writeOwnConnections(data_filename_prefix + "OwnConnections.csv");
                    writeAllConnections(data_filename_prefix + "AllConnections.csv");
                    writeFairRelationships(data_filename_prefix + "FairRelationships.csv");

                    Player x = grid.get(1).get(1);
                    writeDetailedPlayer(
                            player_data_filename_prefix + "Player" + x.getId() + ".csv"
                            , x);
                }
            }

            reset(); // reset certain player attributes.
            gen++; // move on to the next generation
        }

        getStats(); // get stats at the end of the run
    }












    /**
     * Allows for the running of an experiment. Collects data after each experiment into .csv file.
     */
    public static void experiment(){
        displaySettings(); // display settings of experiment

        // stats to be tracked
        double mean_avg_p_of_experiment = 0;
        double[] avg_p_values_of_experiment = new double[runs];
        double sd_avg_p_of_experiment = 0;

        // perform the experiment multiple times
        for(int i=0;i<runs;i++){
            Alg1 run = new Alg1();
            run.start();
            mean_avg_p_of_experiment += run.avg_p;
            avg_p_values_of_experiment[i] = run.avg_p;

            // display the final avg p of the pop of the run that just concluded.
            // this is a different print statement than the one in a similar position in DG18.java!
            System.out.println("final avg p of run "+i+" of experiment "+experiment_number+
                    ": "+run.avg_p);
        }

        // calculate stats
        mean_avg_p_of_experiment /= runs;
        for(int i=0;i<runs;i++){
            sd_avg_p_of_experiment +=
                    Math.pow(avg_p_values_of_experiment[i] - mean_avg_p_of_experiment, 2);
        }
        sd_avg_p_of_experiment = Math.pow(sd_avg_p_of_experiment / runs, 0.5);

        // display stats in console
        System.out.println("mean avg p="+DF4.format(mean_avg_p_of_experiment)
                + ", avg p SD="+DF4.format(sd_avg_p_of_experiment)
        );




        // write results and settings to a .csv data file.
        try{
            FileWriter fw;
            String filename = data_filename_prefix + "Data.csv";

            // write headings
            if(experiment_number == 0){
                fw = new FileWriter(filename, false);
                fw.append("experiment");
                fw.append(",mean avg p");
                fw.append(",avg p SD");
                fw.append(",runs");
                fw.append(",gens");
                fw.append(",neighbourhood");
                fw.append(",N");
                if(edge_weight_learning_method.equals("1")) {
                    fw.append(",EWL1 ROC");
                }
                fw.append(",EPR");
                fw.append(",selection");
                fw.append(",evolution");
                fw.append(",mutation");
            } else {
                fw = new FileWriter(filename, true);
            }

            // write data
            fw.append("\n" + experiment_number);
            fw.append("," + DF4.format(mean_avg_p_of_experiment));
            fw.append("," + DF4.format(sd_avg_p_of_experiment));
            fw.append("," + runs);
            fw.append("," + gens);
            fw.append("," + Player.getNeighbourhoodType());
            fw.append("," + N);
            if(edge_weight_learning_method.equals("1")){
                fw.append("," + ROC);
            }
            fw.append("," + EPR);

            switch(selection_method){ // write selection method
                case "WRW" -> fw.append(",WRW");
                case "best" -> fw.append(",best");
                case "variable" -> fw.append(",variable selection with w="+DF4.format(Player.getW()));
            }

            switch (evolution_method) { // write evolution method
                case "copy" -> fw.append(",copy");
                case "imitation" -> fw.append(",imitation noise=" + DF4.format(Player.getImitationNoise()));
                case "approach" -> fw.append(",approach noise=" + DF4.format(Player.getApproachNoise()));
            }

            switch (mutation_method){ // write mutation method
                case "new" -> fw.append(",mutation rate=" + DF4.format(Player.getMutationRate()));
                default -> fw.append(",no mutation");
            }

            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }

        experiment_number++; // indicates that we are moving onto the next experiment in series
    }







    /**
     * Allows for the running of multiple experiments, i.e. the running of a series of
     * experiments, i.e. the running of an experiment series.
     */
    public static void experimentSeries(){

        // run the experiment series.
        for(int i=0;i<num_experiments;i++){
            experiment(); // run the experiment and store its final data

            // change the value of the assigned varying parameter
            switch (varying_parameter) {
                case "runs" -> runs += (int) variation;
                case "ROC" -> ROC += variation;
                case "EPR" -> EPR += (int) variation;
                case "gens" -> gens += (int) variation;
                case "rows" -> {
                    rows += (int) variation;
                    columns += (int) variation;
                    N = rows * columns;
                }
                case "imitation_noise" -> Player.setImitationNoise(Player.getImitationNoise() + variation);
                case "approach_noise" -> Player.setApproachNoise(Player.getApproachNoise() + variation);
                case "w" -> Player.setW(Player.getW() + variation);
                case "mutation_rate" -> Player.setMutationRate(Player.getMutationRate() + variation);
            }
        }


        // storages for experiment data
        String summary = "";
        ArrayList<String> experiment_number = new ArrayList<>();
        ArrayList<Double> mean_avg_p = new ArrayList<>();
        ArrayList<Double> avg_p_SD = new ArrayList<>();
        ArrayList<Integer> gens = new ArrayList<>();
        ArrayList<Integer> N = new ArrayList<>();
        ArrayList<Double> ROC_list = new ArrayList<>();
        ArrayList<Integer> EPR_list = new ArrayList<>();
        ArrayList<Integer> runs = new ArrayList<>();
        ArrayList<String> imitation_noise = new ArrayList<>();
        ArrayList<String> approach_noise = new ArrayList<>();
        ArrayList<String> w = new ArrayList<>();
        ArrayList<String> mutation_rate = new ArrayList<>();

        // gather experiment data into storages
        int row_count = 0;
        try {
            BufferedReader br =
                    new BufferedReader(
                            new FileReader(
                                    data_filename_prefix + "Data.csv"));
            String line;
            while((line = br.readLine()) != null){
                String[] row_contents = line.split(",");
                if(row_count != 0){
                    experiment_number.add(row_contents[0]);
                    mean_avg_p.add(Double.valueOf(row_contents[1]));
                    avg_p_SD.add(Double.valueOf(row_contents[2]));


                    switch (varying_parameter) {
                        case "runs" -> runs.add(Integer.valueOf(row_contents[3]));
                        case "gens" -> gens.add(Integer.valueOf(row_contents[4]));
                        case "rows" -> N.add(Integer.valueOf(row_contents[6]));
                        case "ROC" -> ROC_list.add(Double.valueOf(row_contents[7]));
                        case "EPR" -> {
                            if(edge_weight_learning_method.equals("1")){
                                EPR_list.add(Integer.valueOf(row_contents[8]));
                            } else {
                                EPR_list.add(Integer.valueOf(row_contents[7]));
                            }
                        }

                        case "w" -> w.add(row_contents[9]);
                        case "imitation_noise" -> imitation_noise.add(row_contents[10]);
                        case "approach_noise" -> approach_noise.add(row_contents[10]);
                        case "mutation_rate" -> mutation_rate.add(row_contents[11]);
                    }
                }
                row_count++;
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        for(int i=0;i<row_count-1;i++){
            summary += "experiment="+experiment_number.get(i)
                    + "\tmean avg p="+DF4.format(mean_avg_p.get(i))
                    + "\tavg p SD="+DF4.format(avg_p_SD.get(i))
            ;

            switch (varying_parameter) {
                case "runs" -> summary += "\truns=" + runs.get(i);
                case "gens" -> summary += "\tgens=" + gens.get(i);
                case "rows" -> summary += "\tN=" + N.get(i);
                case "ROC" -> summary += "\tROC=" + DF4.format(ROC_list.get(i));
                case "EPR" -> summary += "\tEPR=" + EPR_list.get(i);
                case "imitation_noise" -> summary += "\t" + imitation_noise.get(i);
                case "approach_noise" -> summary += "\t" + approach_noise.get(i);
                case "w" -> summary += "\t" + w.get(i);
                case "mutation_rate" -> summary += "\t" + mutation_rate.get(i);
            }


            summary += "\n";
        }
        System.out.println(summary);
    }





    /**
     * Calculate the average value of p across the population at the current gen.<br>
     * The most important avg p is that of the final gen. That particular value is what is being
     * used to calculate the avg p of the experiment as a whole.<br>
     */
    public void getStats(){
        avg_p = 0.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                avg_p+=player.getP();
            }
        }
        avg_p /= N;
    }


    /**
     * For each player in the population, some attributes are reset in preparation
     * for the upcoming generation.
     */
    public void reset(){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setOldP(player.getP());
                player.setNumInteractions(0);
                player.setNumSuccessfulInteractions(0);
                player.setNumSuccessfulDictations(0);
                player.setNumSuccessfulReceptions(0);
            }
        }
    }



    /**
     * Displays experiment settings.
     */
    public static void displaySettings(){
        String s = "";

        if(experiment_series && experiment_number == 0){ // if at start of series
            s += "Experiment series: \nVarying "+varying_parameter+" by "+variation+ " between " +
                    num_experiments+" experiments with settings: ";
        } else {
            s += "Experiment with settings: ";
        }

        s+="\n";

        s += "runs="+runs;
        s += ", gens="+gens;
        s += ", neighbourhood="+Player.getNeighbourhoodType();
        s += ", N="+N;
//        if(Player.getEdgeWeightLearningMethod().equals("1")){
        if(edge_weight_learning_method.equals("1")){
//            s += ", EWL1 with ROC="+DF4.format(Player.getROC());
            s += ", EWL1 with ROC="+DF4.format(ROC);
        } else {
            s += ", EWL2";
        }
        s += ", EPR="+EPR;

        // state the selection method used
//        String selection_method = Player.getSelectionMethod();
        switch(selection_method){
            case "WRW" -> s += ", WRW selection";
            case "best" -> s += ", best selection";
            case "variable" -> s += ", variable selection with w=" + DF4.format(Player.getW());
        }

        // state the evolution method used
//        String evolution_method = Player.getEvolutionMethod();
        switch (evolution_method) {
            case "copy" -> s += ", copy evolution";
            case "imitation" -> s += ", imitation evolution with noise="
                    + DF4.format(Player.getImitationNoise());
            case "approach" -> s += ", approach evolution with noise="
                    + DF4.format(Player.getApproachNoise());
        }

        // state the mutation method used
//        String mutation_method = Player.getMutationMethod();
        switch (mutation_method){
            case "new" -> s += ", new mutation with mutation rate="
                    + DF4.format(Player.getMutationRate());
            default -> s += ", no mutation";
        }

        s += ":";
        System.out.println(s);
    }








    /**
     * Writes the population to a .csv file in the form of a square grid of values in [0.0, 4.0].
     * Each value in the grid represents the sum of connections (edge weights) belonging to the
     * player in that position.<br>
     * This value represents how trusting the player is of their neighbours. If a player x has a 0 on
     * this grid, x must have a higher p value than their neighbours and depending on the ROC, they
     * have had a greater value of p for some time. If x has a 4, x must have the lowest p value in
     * their neighbourhood.<br>
     * This method also calculates the average sum of a player's own weights.
     */
    public void writeOwnConnections(String filename){
        double avg_own_connections = 0;
        FileWriter fw;
        try{
            fw = new FileWriter(filename, false);
            for(ArrayList<Player> row:grid){
                for(int j=0;j<row.size();j++){
                    Player x = row.get(j);
                    double sum = x.calculateOwnConnections();
                    avg_own_connections += sum;
                    fw.append(DF4.format(sum));
                    if(j+1<row.size()){
                        fw.append(",");
                    }
                }
                fw.append("\n");
            }
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }


        // print avg own connections
        avg_own_connections /= N;
        System.out.println("avg own connections="+DF4.format(avg_own_connections));
    }


    /**
     * Write sum of connections of a player and the weights associated with them.<br>
     *
     * This method also calculates the average sum of a player's associated weights.<br>
     */
    public void writeAllConnections(String filename){
        double avg_all_connections = 0;
        FileWriter fw;
        try{
            fw = new FileWriter(filename, false);
            for(ArrayList<Player> row:grid){
                for(int j=0;j<row.size();j++){
                    Player x = row.get(j);
                    double sum = x.calculateAllConnections();
                    avg_all_connections += sum;
                    fw.append(DF4.format(sum));
                    if(j+1<row.size()){
                        fw.append(",");
                    }
                }
                fw.append("\n");
            }
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }


        // print avg all connections
        avg_all_connections /= N;
        System.out.println("avg all connections="+DF4.format(avg_all_connections));
    }


    /**
     * Writes a grid of strategies, i.e. the p values, of the pop to a given .csv file.
     */
    public void writeStrategies(String filename){
        FileWriter fw;
        try{
            fw = new FileWriter(filename, false);
            for(ArrayList<Player> row:grid){
                for(int j=0;j<row.size();j++){
                    Player player = row.get(j);

                    // write strategy.
                    fw.append(DF4.format(player.getP()));


                    if(j+1<row.size()){
                        fw.append(",");
                    }
                }
                fw.append("\n");
            }
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Tracks how many successful interactions the players had (this gen).
     */
    public void writeInteractionData(String filename){
        FileWriter fw;
        try{
            fw = new FileWriter(filename, false);
            for(ArrayList<Player> row:grid){
                for(int j=0;j<row.size();j++){
                    Player x = row.get(j);

                    // write number of successful interactions this gen.
                    fw.append(Integer.toString(x.getNumSuccessfulInteractions()));


                    if(j+1<row.size()){
                        fw.append(",");
                    }
                }
                fw.append("\n");
            }
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }





    /**
     * Allows for the visualisation of the avg p of a run with respect to gens, with gens on x-axis
     * and avg p on y-axis. Now also collects standard deviation (SD) data.<br>
     * <br>Steps:<br>
     * - Export the data of a single run to a .csv file<br>
     * - Import the .csv data into an Excel sheet<br>
     * - Separate the data into columns: gen number, avg p and SD for that gen<br>
     * - Create a line chart with the data.<br>
     */
    public void writePerGenData(String filename){
        FileWriter fw;
        double SD = calculateSD();

        try{
            // reset the .csv file for this run.
            if(gen == 0){
                fw = new FileWriter(filename, false); // append set to false means writing mode.
                fw.append("gen"
                        + ",avg p"
                        + ",p SD"
                        + ",gens"
                        + ",neighbourhood"
                        + ",N"
                        + ",ROC"
                        + ",EPR"
                        + "\n"
                );
                fw.close();
            }

            // add the data to the .csv file.
            fw = new FileWriter(filename, true); // append set to true means append mode.
            fw.append(gen + "");
            fw.append("," + DF4.format(avg_p));
            fw.append("," + DF4.format(SD));
            fw.append("," + gens);
            fw.append("," + Player.getNeighbourhoodType());
            fw.append("," + N);
//                    + "," + Player.getROC()
            fw.append("," + ROC);
            fw.append("," + EPR);
            fw.append("\n");
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Calculates SD of the pop wrt p.
     */
    public double calculateSD(){
        double SD = 0.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                SD += Math.pow(player.getP() - avg_p, 2);
            }
        }
        SD = Math.pow(SD / N, 0.5);

        return SD;
    }







    /**
     * Writes the number of "fair" relationships per node.
     */
    public void writeFairRelationships(String filename){
        FileWriter fw;
        try{
            fw = new FileWriter(filename, false);
            for(ArrayList<Player> row:grid){
                for(int j=0;j<row.size();j++){
                    Player x = row.get(j);

                    // write number of fair relationships of x this gen.
                    fw.append(Integer.toString(x.getNumFairRelationships()));

                    if(j+1<row.size()){
                        fw.append(",");
                    }
                }
                fw.append("\n");
            }
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Writes a detailed account of player x to a .csv file.
     */
    public void writeDetailedPlayer(String filename, Player x){
        FileWriter fw;
        try{
            fw = new FileWriter(filename, false);

            // neighbours of x
            Player up = x.getNeighbourhood().get(0);
            Player down = x.getNeighbourhood().get(0);
            Player left = x.getNeighbourhood().get(0);
            Player right = x.getNeighbourhood().get(0);

            // weights of edges pointing from neighbours to x
            double neighbour_ew_up = up.getEdgeWeights()[x.findXInNeighboursNeighbourhood(up)]; // ew of neighbour above x
            double neighbour_ew_down = down.getEdgeWeights()[x.findXInNeighboursNeighbourhood(down)]; // ew of neighbour below x
            double neighbour_ew_left = left.getEdgeWeights()[x.findXInNeighboursNeighbourhood(left)]; // ew of neighbour to x's left
            double neighbour_ew_right = right.getEdgeWeights()[x.findXInNeighboursNeighbourhood(right)]; // ew of neighbour to x's right

            // weights of edges from x to x's neighbours
            double ew_up = x.getEdgeWeights()[0];
            double ew_down = x.getEdgeWeights()[1];
            double ew_left = x.getEdgeWeights()[2];
            double ew_right = x.getEdgeWeights()[3];

            // general statistics relating to x
            double strategy = x.getP();
            int num_fair_relationships = x.getNumFairRelationships();
            double own_connections = x.calculateOwnConnections();
            double all_connections = x.calculateAllConnections();

//            DF4.format()
            // write data in the form of a 4x4 grid
            fw.append(","+DF4.format(ew_up)+","+DF4.format(neighbour_ew_up)+"\n"
                    +DF4.format(neighbour_ew_left)+","+DF4.format(strategy)+","+DF4.format(num_fair_relationships)+","+DF4.format(ew_right)+"\n"
                    +DF4.format(ew_left)+","+DF4.format(own_connections)+","+DF4.format(all_connections)+","+DF4.format(neighbour_ew_right)+"\n"
                    +","+DF4.format(neighbour_ew_down)+","+DF4.format(ew_down)+"\n"
            );
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
