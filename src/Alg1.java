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
    static String varying_parameter; // indicates which parameter to be varied in an experiment series.
    static boolean experiment_series; // indicate whether to run an experiment or an experiment series.

    // Prefix for filenames of generic data files.
    static String data_filename_prefix = "csv_data\\" +
            Thread.currentThread().getStackTrace()[1].getClassName();

    // Prefix for filenames of interaction data files.
    static String interaction_data_filename_prefix = "csv_data\\interactions_data\\" +
            Thread.currentThread().getStackTrace()[1].getClassName();

    // here, manually set the rate at which interaction data will be recorded
    static int interaction_data_record_rate = 10;

    static int data_gen;

    // Prefix for filenames of player data files.
    static String player_data_filename_prefix = "csv_data\\player_data\\" +
            Thread.currentThread().getStackTrace()[1].getClassName();










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
        while(gen != gens) {
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
                        if(evolution_method.equals("copy")){
                            player.copyEvolution(parent);
                        } else if(evolution_method.equals("imitation")){
                            player.imitationEvolution(parent);
                        } else if(evolution_method.equals("approach")){
                            player.approachEvolution(parent);
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
                    System.out.println("Taking data screenshot at gen "+data_gen+"...");
                    writePopStrategies(data_filename_prefix + "Strategies.csv");
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
        runs = 1;
        Player.setRate_of_change(0.05);
        rows = 10;
        gens = 5000;
        evo_phase_rate = 5;
        Player.setNeighbourhoodType("VN"); // von neumann neighbourhood
//        Player.setNeighbourhoodType("M"); // moore neighbourhood



        // after which gen of the experiment do you wish to collect at?
//         data_gen = gens; // collect data at end of run
        data_gen = 50;


        Player.setFairnessInterval(0.05); // set the fairness interval


        Player.setImitationNoise(0.1); // set the imitation noise


        Player.setApproachNoise(0.1); // set the approach noise


        // select a selection method
        Player.setSelectionMethod("WRW"); // weighted roulette wheel
//        Player.setSelectionMethod("best");

        // select an evolution method
//        Player.setEvolutionMethod("copy");
//        Player.setEvolutionMethod("imitation");
        Player.setEvolutionMethod("approach");









        Player.setPrize(1.0);
        columns = rows;
        N = rows * columns;


//        experiment_series = true; // to run a single experiment
        experiment_series = false; // to run an experiment series

        if(experiment_series){

            // define the parameter to be varied across the experiment series.
            varying_parameter = "ROC"; // vary the edge weight rate of change per EWL phase.
//            varying_parameter = "EPR"; // vary the evolutionary phase rate.
//            varying_parameter = "gens"; // vary the number of generations.
//            varying_parameter = "rows_columns"; // vary the number of rows and columns.
//            varying_parameter = "rows_columns"; // vary the number of rows and columns.

            // define the amount by which the parameter will vary between subsequent experiments.
            // note: the double type here also works for varying integer type params such as gens.
            double variation = 0.05;

            int num_experiments = 8; // define number of experiments to occur here

            // display which parameter is being modified and by how much per experiment.
            System.out.println("Varying "+varying_parameter+" by "+variation+" between "+num_experiments+
                    " experiments with settings: ");

            experimentSeries(variation, num_experiments); // run an experiment series
        }

        else {
            experiment(0); // run a single experiment
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
//    public static void experiment(String filename, int experiment_number){
    public static void experiment(int experiment_number){
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

        // write stats/results and settings to a .csv data file.
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
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }







    /**
     * Allows for the running of multiple experiments, i.e. the running of a series of
     * experiments, i.e. the running of an experiment series.
     */
//    public static void experimentSeries(String filename, double variation, int num_experiments){
    public static void experimentSeries(double variation, int num_experiments){

        // run the experiment series.
        for(int i=0;i<num_experiments;i++){
            experiment(i); // run the experiment and store its final data

            // change the value of the parameter
            if(varying_parameter.equals("ROC")){
                Player.setRate_of_change(Player.getRate_of_change() + variation);
            } else if(varying_parameter.equals("EPR")){
                evo_phase_rate += variation;
            } else if(varying_parameter.equals("gens")){
                gens += variation;
            } else if(varying_parameter.equals("rows_columns")){
                rows += variation;
                columns += variation;
                N = rows * columns;
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

        int row_count = 0;
        try {
            BufferedReader br =
                    new BufferedReader(
                            new FileReader(
                                    data_filename_prefix + "Data.csv"));
            String line = "";
            while((line = br.readLine()) != null){
                String[] row_contents = line.split(",");
                if(row_count != 0){
                    experiment_number.add(row_contents[0]);
                    mean_avg_p.add(Double.valueOf(row_contents[1]));
                    avg_p_SD.add(Double.valueOf(row_contents[2]));

//                    runs.add(Integer.valueOf(row_contents[3]));
//                    neighbourhood.add(String.valueOf(row_contents[5]));

                    if(varying_parameter.equals("gens")){
                        gens.add(Integer.valueOf(row_contents[4]));
                    } else if(varying_parameter.equals("rows_columns")){
                        N.add(Integer.valueOf(row_contents[6]));
                    } else if(varying_parameter.equals("ROC")){
                        ROC.add(Double.valueOf(row_contents[7]));
                    } else if(varying_parameter.equals("EPR")){
                        EPR.add(Integer.valueOf(row_contents[8]));
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

            if(varying_parameter.equals("gens")){
                summary += "\tgens=" + gens.get(i);
            } else if(varying_parameter.equals("rows_columns")){
                summary += "\tN=" + N.get(i);
            } else if(varying_parameter.equals("ROC")){
                summary += "\tROC=" + DF4.format(ROC.get(i));
            } else if(varying_parameter.equals("EPR")){
                summary += "\tEPR=" + EPR.get(i);
            }
            summary += "\n";
        }
        System.out.println(summary);
    }





    /**
     * Calculate the average value of p across the population at the current gen.
     *
     * The most important avg p is that of the final gen. That particular value is what is being
     * used to calculate the avg p of the experiment as a whole.
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
                player.setGamesPlayedThisGen(0);
                player.setOld_p(player.getP());

                // reset number of successful interactions this gen to zero
                player.setNum_successful_interactions(0);
            }
        }
    }



    /**
     * Displays experiment settings.
     */
    public static void displaySettings(){
        String s = "";
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
        if(evolution_method.equals("copy")){
            s += ", copy evolution";
        } else if(evolution_method.equals("imitation")){
            s += ", imitation evolution with noise="+Player.getImitationNoise();
        } else if(evolution_method.equals("approach")){
            s += ", approach evolution with noise="+Player.getApproachNoise();
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

//                    double sum = 0.0;
//                    for(int i=0;i<player.getEdge_weights().length;i++){
//                        double addition = player.getEdge_weights()[i];
//                        sum+=addition;
//                        avg_own_connections+=addition;
//                    }

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

//                    double sum = 0.0;
//
//                    // calculate sum of own edge weights
//                    for(int i=0;i<x.getEdge_weights().length;i++){
//                        double addition = x.getEdge_weights()[i];
//                        sum+=addition;
//                        avg_all_connections+=addition;
//                    }


//                    double sum = x.calculateOwnConnections();
//                    avg_all_connections += sum;



//                    boolean identified_x = false;
//                    do {
//                        for (int k = 0; k < x.getNeighbourhood().size(); k++) {
//                            Player neighbour = x.getNeighbourhood().get(k);
//                            for (int l = 0; l < neighbour.getNeighbourhood().size(); l++) {
//                                Player y = neighbour.getNeighbourhood().get(l);
//                                if (x.getId() == y.getId()) {
//                                    double addition = neighbour.getEdge_weights()[l];
//                                    sum += addition;
//                                    avg_all_connections += addition;
//                                    identified_x = true;
//                                }
//                            }
//                        }
//                    } while(!identified_x);


//                    for (int k = 0; k < x.getNeighbourhood().size(); k++) {
//                            Player y = x.getNeighbourhood().get(k);
//                            double addition = y.getEdge_weights()[x.findXInNeighboursNeighbourhood(y)];
//                            sum += addition;
//                            avg_all_connections += addition;
//                    }






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
    public void writePopStrategies(String filename){
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
     * and avg p on y-axis. Now also collects standard deviation (SD) data.
     *
     * Steps:
     * Export the data of a single run to a .csv file
     * Import the .csv data into an Excel sheet
     * Separate the data into columns: gen number, avg p and SD for that gen
     * Create a line chart with the data.
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
     *
     * @param filename
     * @param x
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
     * @param child
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
            }
        }

        return parent;
    }


    /**
     * Selection method where child selects the highest scoring neighbour this gen as parent if
     * that neighbour scored higher than the child.<br>
     * Should the score comparison be between avg scores or just scores?<br>
     * @param child
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
