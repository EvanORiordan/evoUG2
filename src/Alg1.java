import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
    static int evo_phase_rate; // how often the evolutionary phase occurs.
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

    // here, manually set the rate at which interaction data will be recorded
    static int interaction_data_record_rate;

    static int data_gen;

    // Prefix for filenames of player data files.
    static String player_data_filename_prefix = "csv_data\\player_data\\" +
            Thread.currentThread().getStackTrace()[1].getClassName();

    // add scanner to receive input. then receive input for experiment or series, which varying param,
    // what is the variation amount, etc.








    /**
     * Method for starting a run of an experiment.
     * This is the core algorithm at the heart of the program.
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
                    player.playEWSpatialUG();
                }
            }


            // edge weight learning phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.edgeWeightLearning();
                }
            }


            /**
             * Selection and evolution occur every evo_phase_rate gens.
             * Each player in the grid tries to evolve.
             */
            if((gen + 1) % evo_phase_rate == 0) {
                for (ArrayList<Player> row : grid) {
                    for (Player player : row) {

                        // select parent
                        Player parent = null;
                        String selection_method = Player.getSelectionMethod();
                        if(selection_method.equals("WRW")){
                            parent = weightedRouletteWheelSelection(player);
                        } else if(selection_method.equals("best")){
                            parent = bestSelection(player);
                        }

                        // evolve child
                        String evolution_method = Player.getEvolutionMethod();
                        switch (evolution_method) {
                            case "copy" -> player.copyEvolution(parent);
                            case "imitation" -> player.imitationEvolution(parent);
                            case "approach" -> player.approachEvolution(parent);
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






    // main method for executing the program/algorithm
    public static void main(String[] args) {

        // marks the beginning of the program's runtime
        Instant start = Instant.now();
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
        System.out.println("Timestamp: " + java.time.Clock.systemUTC().instant());



        // define initial parameter values.
        runs = 100;
        Player.setRate_of_change(0.02);
        rows = 20;
        gens = 10000;
        evo_phase_rate = 5;
        Player.setNeighbourhoodType("VN"); // von neumann neighbourhood
//        Player.setNeighbourhoodType("M"); // moore neighbourhood
        Player.setFairnessInterval(0.05); // set the fairness interval
        Player.setImitationNoise(0.05); // set the imitation noise
        Player.setApproachNoise(0.05); // set the approach noise




        // select a selection method
        Player.setSelectionMethod("WRW"); // weighted roulette wheel
//        Player.setSelectionMethod("best");

        // select an evolution method
//        Player.setEvolutionMethod("copy");
        Player.setEvolutionMethod("imitation");
//        Player.setEvolutionMethod("approach");





        // after which gen of the experiment do you wish to collect at?
        data_gen = gens - 1; // collect data at end of final gen of run
//        data_gen = 50;


        // define how often interaction data is recorded
        interaction_data_record_rate = gens - 1; // collect data at end of final gen of run
//        interaction_data_record_rate = 10;






        Player.setPrize(1.0);
        columns = rows;
        N = rows * columns;


//        experiment_series = true; // to run a single experiment
        experiment_series = false; // to run an experiment series

        if(experiment_series){

            // assign varying parameter
//            varying_parameter = "ROC"; // vary the edge weight rate of change per EWL phase.
//            varying_parameter = "EPR"; // vary the evolutionary phase rate.
//            varying_parameter = "gens"; // vary the number of generations.
            varying_parameter = "rows_columns"; // vary the number of rows and columns.
//            varying_parameter = "imitation_noise"; // vary amount of imitation noise affecting evolution
//            varying_parameter = "approach_noise"; // vary amount of approach noise affecting evolution

            variation = 4; // assign variation
            num_experiments = 6; // assign number of experiments


            experimentSeries(); // run an experiment series
        }

        else {
            experiment(); // run a single experiment
        }




        // marks the end of the program's runtime
        System.out.println("Timestamp: " + java.time.Clock.systemUTC().instant());
        Instant finish = Instant.now();
        long secondsElapsed = Duration.between(start, finish).toSeconds();
        long minutesElapsed = Duration.between(start, finish).toMinutes();
        System.out.println("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
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
            if(experiment_number == 0){
                fw = new FileWriter(filename, false);
                fw.append("experiment"
                        + ",mean avg p"
                        + ",avg p SD"
                        + ",runs"
                        + ",gens"
                        + ",neighbourhood"
                        + ",N"
                        + ",ROC"
                        + ",EPR"
                        + ",selection" // include heading for selection method
                        + ",evolution" // include heading for evolution method
                );
            } else {
                fw = new FileWriter(filename, true);
            }
            fw.append("\n" + experiment_number
                    + "," + DF4.format(mean_avg_p_of_experiment)
                    + "," + DF4.format(sd_avg_p_of_experiment)
                    + "," + runs
                    + "," + gens
                    + "," + Player.getNeighbourhoodType()
                    + "," + N
                    + "," + Player.getRate_of_change()
                    + "," + evo_phase_rate
            );

            // write selection method
            String selection_method = Player.getSelectionMethod();
            if(selection_method.equals("WRW")){
                fw.append(",WRW");
            } else if(selection_method.equals("best")){
                fw.append(",best");
            }

            // write evolution method
            String evolution_method = Player.getEvolutionMethod();
            switch (evolution_method) {
                case "copy" -> fw.append(",copy");
                case "imitation" -> fw.append(",imitation noise=" + DF4.format(Player.getImitationNoise()));
                case "approach" -> fw.append(",approach noise=" + DF4.format(Player.getApproachNoise()));
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
                case "ROC" -> Player.setRate_of_change(Player.getRate_of_change() + variation);
                case "EPR" -> evo_phase_rate += (int) variation;
                case "gens" -> gens += (int) variation;
                case "rows_columns" -> {
                    rows += (int) variation;
                    columns += (int) variation;
                    N = rows * columns;
                }
                case "imitation_noise" -> Player.setImitationNoise(Player.getImitationNoise() + variation);
                case "approach_noise" -> Player.setApproachNoise(Player.getApproachNoise() + variation);
            }
        }


        // display a summary of the experiment series on the console.
        String summary = "";
        ArrayList<String> experiment_number = new ArrayList<>();
        ArrayList<Double> mean_avg_p = new ArrayList<>();
        ArrayList<Double> avg_p_SD = new ArrayList<>();
        ArrayList<Integer> gens = new ArrayList<>();
        ArrayList<Integer> N = new ArrayList<>();
        ArrayList<Double> ROC = new ArrayList<>();
        ArrayList<Integer> EPR = new ArrayList<>();
//        ArrayList<Integer> runs = new ArrayList<>();
//        ArrayList<String> neighbourhood = new ArrayList<>();
        ArrayList<String> imitation_noise = new ArrayList<>();
        ArrayList<String> approach_noise = new ArrayList<>();



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

//                    runs.add(Integer.valueOf(row_contents[3]));
//                    neighbourhood.add(String.valueOf(row_contents[5]));

                    switch (varying_parameter) {
                        case "gens" -> gens.add(Integer.valueOf(row_contents[4]));
                        case "rows_columns" -> N.add(Integer.valueOf(row_contents[6]));
                        case "ROC" -> ROC.add(Double.valueOf(row_contents[7]));
                        case "EPR" -> EPR.add(Integer.valueOf(row_contents[8]));
                        case "imitation_noise" -> imitation_noise.add(row_contents[10]);
                        case "approach_noise" -> approach_noise.add(row_contents[10]);
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
                case "gens" -> summary += "\tgens=" + gens.get(i);
                case "rows_columns" -> summary += "\tN=" + N.get(i);
                case "ROC" -> summary += "\tROC=" + DF4.format(ROC.get(i));
                case "EPR" -> summary += "\tEPR=" + EPR.get(i);
                case "imitation_noise" -> summary += "\t" + imitation_noise.get(i);
                case "approach_noise" -> summary += "\t" + approach_noise.get(i);
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
     * Some player values are reset in preparation for the upcoming generation.
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

        s += "runs="+runs
                + ", gens="+gens
                + ", neighbourhood="+Player.getNeighbourhoodType()
                + ", N="+N
                + ", ROC="+DF4.format(Player.getRate_of_change())
                + ", EPR="+evo_phase_rate
        ;

        // state the selection method used
        String selection_method = Player.getSelectionMethod();
        if(selection_method.equals("WRW")){
            s += ", WRW selection";
        }else if(selection_method.equals("best")){
            s += ", best selection";
        }

        // state the evolution method used
        String evolution_method = Player.getEvolutionMethod();
        switch (evolution_method) {
            case "copy" -> s += ", copy evolution";
            case "imitation" -> s += ", imitation evolution with noise=" + Player.getImitationNoise();
            case "approach" -> s += ", approach evolution with noise=" + Player.getApproachNoise();
        }

        s += ":";
        System.out.println(s);
    }


    /**
     * Writes the population to a .csv file in the form of a square grid of values in [0.0, 4.0].
     * Each value in the grid represents the sum of connections (edge weights) belonging to the
     * player in that position.<br>
     *
     * This value represents how trusting the player is of their neighbours. If a player x has a 0 on
     * this grid, x must have a higher p value than their neighbours and depending on the ROC, they
     * have had a greater value of p for some time. If x has a 4, x must have the lowest p value in
     * their neighbourhood.<br>
     *
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
                    fw.append(Integer.toString(x.getNum_successful_interactions()));


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
     * Export the data of a single run to a .csv file<br>
     * Import the .csv data into an Excel sheet<br>
     * Separate the data into columns: gen number, avg p and SD for that gen<br>
     * Create a line chart with the data.<br>
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
            fw.append(gen
                    + "," + DF4.format(avg_p)
                    + "," + DF4.format(SD)
                    + "," + gens
                    + "," + Player.getNeighbourhoodType()
                    + "," + N
                    + "," + Player.getRate_of_change()
                    + "," + evo_phase_rate
                    + "\n");
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
            double neighbour_ew_up = up.getEdge_weights()[x.findXInNeighboursNeighbourhood(up)]; // ew of neighbour above x
            double neighbour_ew_down = down.getEdge_weights()[x.findXInNeighboursNeighbourhood(down)]; // ew of neighbour below x
            double neighbour_ew_left = left.getEdge_weights()[x.findXInNeighboursNeighbourhood(left)]; // ew of neighbour to x's left
            double neighbour_ew_right = right.getEdge_weights()[x.findXInNeighboursNeighbourhood(right)]; // ew of neighbour to x's right

            // weights of edges from x to x's neighbours
            double ew_up = x.getEdge_weights()[0];
            double ew_down = x.getEdge_weights()[1];
            double ew_left = x.getEdge_weights()[2];
            double ew_right = x.getEdge_weights()[3];

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





    /**
     * Selection method where the child compares their score with their neighbours'. The
     * greater the difference in score, the neighbour's probability of being selected as child's
     * parent is exponentially affected. If child selects itself as parent, no evolution occurs.<br>
     * After parent has been selected, evolution takes place.<br>
     */
    public Player weightedRouletteWheelSelection(Player child){

        /**
         * If a parent does not yet been selected, the child selects itself as parent by default.
         * Therefore, the child will not undergo any evolutionary change.
         */
        Player parent = child;

        ArrayList<Player> neighbourhood = child.getNeighbourhood();
        double[] imitation_scores = new double[neighbourhood.size()];
        double total_imitation_score = 0;
        double player_avg_score = child.getAverageScore();
        for (int i = 0; i < neighbourhood.size(); i++) {
            imitation_scores[i] =
                    Math.exp(neighbourhood.get(i).getAverageScore() - player_avg_score);
            total_imitation_score += imitation_scores[i];
        }
        total_imitation_score += 1.0;
        double imitation_score_tally = 0;
        double random_double_to_beat = ThreadLocalRandom.current().nextDouble();
        for (int j = 0; j < neighbourhood.size(); j++) {
            imitation_score_tally += imitation_scores[j];
            double percentage = imitation_score_tally / total_imitation_score;
            if (random_double_to_beat < percentage) {
                parent = neighbourhood.get(j);
                break;
            }
        }

        return parent;
    }


    /**
     * Selection method where child selects the highest scoring neighbour this gen as parent if
     * that neighbour scored higher than the child.<br>
     * Should the score comparison be between avg scores or just scores?<br>
     */
    public Player bestSelection(Player child) {
        Player parent;
        int index = 0;
        ArrayList<Player> neighbourhood = child.getNeighbourhood();
        double best_avg_score;
        for(int i=1;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            Player best = neighbourhood.get(index);
            double neighbour_avg_score = neighbour.getAverageScore();
            best_avg_score = best.getAverageScore();
            if(neighbour_avg_score > best_avg_score){
                index = i;
            }
        }
        parent = neighbourhood.get(index);
        best_avg_score = parent.getAverageScore();
        double child_avg_score = child.getAverageScore();
        if(best_avg_score <= child_avg_score){
            parent = child;
        }

        return parent;
    }











}
