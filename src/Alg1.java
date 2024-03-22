import java.io.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
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
    static String game; // what game is being played
    static int rows; // how many rows in the square grid
    static int columns; // how many rows in the square grid
    static int N; // population size.
    static int gens; // how many generations occur per experiment run.
    static int runs; // how many times this experiment will be run.
    static String neighbourhood_type;
    ArrayList<ArrayList<Player>> grid = new ArrayList<>(); // 2D square lattice grid containing the population.
    double avg_p; // the average value of p across the population.
    double p_SD; // the standard deviation of p across the pop
    int gen = 0; // indicates which generation is currently running.

    // decimal formatting objects
    static DecimalFormat DF1 = Player.getDF1(); // formats numbers to 1 decimal place
    static DecimalFormat DF4 = Player.getDF4(); // formats numbers to 4 decimal places

    // I/O objects
    static FileWriter fw;
    static BufferedReader br;
    static Scanner scanner = new Scanner(System.in);

    // parameters for determining whether to use saved pop and/or save pop
    static boolean save_pop = false;
    static boolean use_saved_pop = false;

    /**
     * experiment series parameters:<br>
     *
     * - experiment_series indicates whether to run an experiment or an experiment series. A series should be
     * used when you want to vary a parameter of the algorithm between experiments. Otherwise, a single
     * experiment should suffice.<br>
     *
     * - varying_parameter indicates which parameter will be varied in an experiment series.<br>
     *
     * - variation indicates by how much the parameter will vary between subsequent experiments in the
     * series. the double type here also works for varying integer type params such as gens.<br>
     *
     * - num_experiments indicates the number of experiments to occur in the series.<br>
     *
     * - experiment_number tracks which experiment is taking place at any given time during a series.<br>
     *
     * - possible_varying_parameters stores all the possible parameters that can be varied in a series.
     * Currently, the ArrayList just indicates the indices of the possible varying parameters.<br>
     */
    static boolean experiment_series;
    static String varying_parameter;
    static int varying_parameter_index;
    static double variation;
    static int num_experiments;
    static int experiment_number = 0;
    static List<String> possible_varying_parameters = new ArrayList<>(
            List.of("runs" //0
                    , "gens" //1
                    , "rows" //2
                    , "EPR" //3
                    , "ROC" //4
                    , "leeway" //5
                    , "MLB" // 10
                    , "sel noise" //6
                    , "evo noise" //7
                    , "u" //8
                    , "delta" //9
            ));


    // Prefix for filenames of generic data files.
    static String data_filename_prefix = "csv_data\\"+Thread.currentThread().getStackTrace()[1].getClassName();

    // Prefix for filenames of interaction data files.
    static String interaction_data_filename_prefix="csv_data\\interactions_data\\"+Thread.currentThread().getStackTrace()[1].getClassName();
    static int interaction_data_record_rate;
    static int data_gen; // is used only during single experiments

    // Prefix for filenames of player data files.
    static String player_data_filename_prefix="csv_data\\player_data\\"+Thread.currentThread().getStackTrace()[1].getClassName();

    // config filename
    static String config_filename = "config.csv"; // filename of config file


    // EWL parameters
    static String EWT; // edge weight type
    static String EWAE; // edge weight adjustment equation
    static int EPR = 1; // evolution phase rate: how often evolution phases occur e.g. if 5, then evo phase occurs every 5 gens
    static double ROC = 0.0; // EW rate of change
    static double leeway = 0.0; // leeway affecting EWL
    static double MLB = 0.0; // MLB: my_leeway_bound

    // evolution parameters
    static String evolution_method;
    static double approach_noise; // affected by evo noise

    // selection parameters
    static String selection_method;
    static double w; // affected by sel noise

    // mutation parameters
    static String mutation_method;
    static double u; // represents mutation rate
    static double delta; // represents mutation noise

    // Use this to describe experimentation and focus attention on key settings.
    static String desc;






    // main method for executing the program
    public static void main(String[] args) {

        ArrayList<String> configurations = new ArrayList<>(); // stores configurations
        try{ // load the configs from the file
            br = new BufferedReader(new FileReader(config_filename));
            String line; // initialise String to store rows of data
            line = br.readLine(); // purposely ignore the row of headings
            while((line = br.readLine()) != null){
                configurations.add(line);
            }
        } catch(IOException e){
            e.printStackTrace();
        }


        // display intro and config table headings
        System.out.printf("=========================================%n");
        System.out.printf("   Evolutionary Game Theory Simulator%n");
        System.out.printf("   By Evan O'Riordan%n");
        System.out.printf("===================================================================================================================================================================================================================================================================%n");
        System.out.printf("%-6s |" +//config
                        " %-4s |" +//game
                        " %-6s |" +//runs
                        " %-9s |" +//gens
                        " %-4s |" +//rows
                        " %-3s |" +//EWT
                        " %-4s |" +//EWAE
                        " %-3s |" +//EPR
                        " %-6s |" +//ROC
                        " %-6s |" +//leeway
                        " %-6s |" +//MLB
                        " %-10s |" +//varying
                        " %-9s |" +//variation
                        " %-7s |" +//num exp
                        " %-9s |" +//data gen
                        " %-5s |" +//neigh
                        " %-8s |" +//sel
                        " %-9s |" +//sel noise
                        " %-9s |" +//PPM
                        " %-3s |" +//ASD
                        " %-8s |" +//evo
                        " %-9s |" +//evo noise
                        " %-6s |" +//mut
                        " %-6s |" +//u
                        " %-6s |" +//delta
                        " desc%n" // ensure desc is the last column
                ,"config"
                ,"game"
                ,"runs"
                ,"gens"
                ,"rows"
                ,"EWT"
                ,"EWAE"
                ,"EPR"
                ,"ROC"
                ,"leeway"
                ,"MLB"
                ,"varying"
                ,"variation"
                ,"num exp"
                ,"data gen"
                ,"neigh"
                ,"sel"
                ,"sel noise"
                ,"PPM"
                ,"ASD"
                ,"evo"
                ,"evo noise"
                ,"mut"
                ,"u"
                ,"delta");
        System.out.printf("===================================================================================================================================================================================================================================================================%n");




        // display config table rows
        int config_index;
        for(int i=0;i<configurations.size();i++){
            String[] settings = configurations.get(i).split(",");
            config_index = 0;

            System.out.printf("%-6d ", i); //config
            System.out.printf("| %-4s ", settings[config_index++]); //game
            System.out.printf("| %-6s ", settings[config_index++]); //runs
            System.out.printf("| %-9s ", settings[config_index++]); //gens
            System.out.printf("| %-4s ", settings[config_index++]); //rows
            System.out.printf("| %-3s ", settings[config_index++]); //EWT
            System.out.printf("| %-4s ", settings[config_index++]); //EWAE
            System.out.printf("| %-3s ", settings[config_index++]); //EPR
            System.out.printf("| %-6s ", settings[config_index++]); //ROC
            System.out.printf("| %-6s ", settings[config_index++]); //leeway
            System.out.printf("| %-6s ", settings[config_index++]); //MLB
            System.out.printf("| %-10s ", settings[config_index++]); //varying
            System.out.printf("| %-9s ", settings[config_index++]); //variation
            System.out.printf("| %-7s ", settings[config_index++]); //num exp
            System.out.printf("| %-9s ", settings[config_index++]); //data gen
            System.out.printf("| %-5s ", settings[config_index++]); //neigh
            System.out.printf("| %-8s ", settings[config_index++]); //sel
            System.out.printf("| %-9s ", settings[config_index++]); //sel noise
            System.out.printf("| %-9s ", settings[config_index++]); //PPM
            System.out.printf("| %-3s ", settings[config_index++]); //ASD
            System.out.printf("| %-8s ", settings[config_index++]); //evo
            System.out.printf("| %-9s ", settings[config_index++]); //evo noise
            System.out.printf("| %-6s ", settings[config_index++]); //mut
            System.out.printf("| %-6s ", settings[config_index++]); //u
            System.out.printf("| %-6s ", settings[config_index++]); //delta
            System.out.printf("| %s ", settings[config_index]); //desc


            System.out.println();
        }
        System.out.printf("===================================================================================================================================================================================================================================================================%n");




        // ask user which config they want to use
        System.out.println("Which config would you like to use? (int)");
        boolean config_found = false;
        int config_num;
        do{ // ensure user selects valid config
            config_num = scanner.nextInt();
            if(0 <= config_num && config_num < configurations.size()){
                config_found = true;
            } else{
                System.out.println("ERROR: invalid config number, try again");
            }
        }while(!config_found);

        // apply the config
        String[] settings = configurations.get(config_num).split(",");
        config_index = 0;

        // game
        game = settings[config_index++];
        Player.setGame(game);

        // runs
        runs = Integer.parseInt(settings[config_index++]);

        // gens
        gens = Integer.parseInt(settings[config_index++]);

        // rows
        rows = Integer.parseInt(settings[config_index++]);

        // EWT,EWAE,EPR,ROC,leeway
        EWT = settings[config_index++];
        EWAE = settings[config_index++];
        EPR=Integer.parseInt(settings[config_index++]);
        ROC=Double.parseDouble(settings[config_index++]);
        leeway=Double.parseDouble(settings[config_index++]);
        MLB=Double.parseDouble(settings[config_index++]);

        // varying,variation,num exp
        if(!settings[config_index].equals("")) {
            varying_parameter = settings[config_index];
            switch(varying_parameter){
                case"runs"->varying_parameter_index=0;
                case"gens"->varying_parameter_index=1;
                case"rows"->varying_parameter_index=2;
                case"EPR"->varying_parameter_index=3;
                case"ROC"->varying_parameter_index=4;
                case"leeway"->varying_parameter_index=5;
                case"MLB"->varying_parameter_index=10;
                case"sel noise"->varying_parameter_index=6;
                case"evo noise"->varying_parameter_index=7;
                case"u"->varying_parameter_index=8;
                case"delta"->varying_parameter_index=9;
            }
            experiment_series = true;
            variation = Double.parseDouble(settings[config_index+1]);
            num_experiments = Integer.parseInt(settings[config_index+2]);
        }
        config_index+=3;

        // data gen
        if(!settings[config_index].equals("")){
            data_gen = Integer.parseInt(settings[config_index]);
        } else{
            data_gen = -1; // if no data gen value given, assign -1 to nullify the variable's effect
        }
        config_index++;

        // neigh
        neighbourhood_type = settings[config_index++];

        //sel,sel noise,PPM,ASD
        selection_method=settings[config_index];
        switch(settings[config_index]){
            case"variable"->w=Double.parseDouble(settings[config_index+1]);
        }
        Player.setPPM(settings[config_index+2]);
        switch(Player.getPPM()){
            case"avg score"->Player.setASD(settings[config_index+3]);
        }
        config_index+=4;

        // evo,evo noise
        evolution_method=settings[config_index];
        switch(settings[config_index]){
            case"approach"->approach_noise=Double.parseDouble(settings[config_index+1]);
        }
        config_index+=2;

        // mut,u,delta
        mutation_method = settings[config_index];
        switch(mutation_method){
            case"global","local"->{
                u=Double.parseDouble(settings[config_index+1]);
                switch(mutation_method){
                    case"local"->delta=Double.parseDouble(settings[config_index+2]);
                }
            }
        }
        config_index+=3;

        //desc
        desc = settings[config_index];






        // enable these lines if you want to use these features.
        // ask user if they want to save the initial pop (so that it can be used again)
//        save_pop = binaryQuestion("save initial pop? (0: no, 1: yes)");
        // ask user if they want to use the pop saved in the strategies .csv file
//        use_saved_pop = binaryQuestion("use saved pop? (0: no, 1: yes)");


        Player.setGrossPrize(1.0); // set prize per game to 1.0; makes no diff if bigger or smaller


        // square topology by default
        columns = rows;
        N = rows * columns;


        // you could ask the user how often they want to record interaction data. alternatively,
        // it could be a column of the config file.
        interaction_data_record_rate = gens; // by default, do not collect interaction data



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
        // WARNING: DO NOT TRY TO USE THE SAVED POP IF THE GAME IS NOT DG! This is because the
        // strategies file currently only stores p values.
        if(use_saved_pop){ // user wants to use saved pop, read pop from .csv file
//            try{
//                br = new BufferedReader(new FileReader(data_filename_prefix + "Strategies.csv"));
//                String line;
//                int i=0;
//                while((line = br.readLine()) != null) {
//                    String[] row_contents = line.split(",");
//                    ArrayList<Player> row = new ArrayList<>();
//                    for(int j=0;j<row_contents.length;j++){
//                        row.add(new Player(Double.parseDouble(row_contents[i]), 0));
//                    }
//                    i++;
//                    grid.add(row);
//                }
//            } catch(IOException e){
//                e.printStackTrace();
//            }
        }

        else { // user wants to randomly generate a population
            for (int i = 0; i < rows; i++) {
                ArrayList<Player> row = new ArrayList<>();
                for (int j = 0; j < columns; j++) {
                    switch(game){
//                        case"UG"->row.add(new Player(
//                                ThreadLocalRandom.current().nextDouble(),
//                                ThreadLocalRandom.current().nextDouble());
                        case"DG"->row.add(new Player(
                                ThreadLocalRandom.current().nextDouble(),
                                0.0,
                                MLB));
                    }
                }
                grid.add(row);
            }
        }


        if(save_pop){ // user wants to record initial pop into strategies .csv file
            writeStrategies(data_filename_prefix + "Strategies.csv");
        }





        // initialise neighbourhoods
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                Player player = grid.get(i).get(j);

                switch(neighbourhood_type){
                    case"VN","M"->assignAdjacentNeighbours(player, i, j);
                    case"random"->assignRandomNeighbours(player, 3);
                }

                grid.get(i).get(j).initialiseEdgeWeights();
            }
        }



        // players begin playing the game
        while(gen != gens) { // algorithm stops once this condition is reached
            // playing phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    switch(game){
                        case"UG","DG"->{
                            switch(EWT){
                                case"1"->player.playUG1(); // EW affects probability to interact
                                case"2"->player.playUG2(); // EW affects payoff calculation
                            }
                        }


//                        case"PD"->player.playPD();
//                        case"IG"->player.playIG();
//                        case"TG"->player.playTG();
                    }
                }
            }


            // edge weight learning phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    switch(EWT){
                        case"1","2"->player.EWL(EWAE,ROC,leeway);
                    }
//                    switch(EWAE){
//                        case"ROC","AD","EAD"->player.EWL(EWAE, ROC,leeway);
//                    }
                }
            }



            // selection and evolution occur every EPR gens. each player in the grid tries to evolve.
            if((gen + 1) % EPR == 0) {
                for (ArrayList<Player> row : grid) {
                    for (Player player : row) {

                        // select parent
                        Player parent = null;
                        switch(selection_method){
                            case "WRW" -> parent = player.weightedRouletteWheelSelection();
                            case "best" -> parent = player.bestSelection();
                            case "variable" -> parent = player.variableSelection(w);
                        }

                        // evolve child
                        switch (evolution_method) {
                            case "copy" -> player.copyEvolution(parent);
                            case "approach" -> player.approachEvolution(parent, approach_noise);
                        }

                        // mutate child
                        switch (mutation_method){
                            case "global" -> {
                                if(player.mutationCheck(u)){
                                    player.globalMutation();
                                }
                            }
                            case "local" -> {
                                if(player.mutationCheck(u)){
                                    player.localMutation(delta);
                                }
                            }
                        }
                    }
                }
            }


            // opportunity to collect intricate data
            if(!experiment_series && runs == 1){

                // record the avg p and p SD of the gen that just ended
                calculateStats();
                System.out.println("avg p="+DF4.format(avg_p)+", p SD="+DF4.format(p_SD));
                writePerGenData(data_filename_prefix + "PerGenData.csv");

                // record interaction data according to the interaction data record rate
                if(gen % interaction_data_record_rate == 0){
                    writeInteractionData(interaction_data_filename_prefix + "Gen" + gen);
                }

                if(gen==data_gen){ // at end of data gen, record lots of extra info
                    System.out.println("Recording detailed data for gen "+data_gen+"...");
                    writeStrategies(data_filename_prefix + "Strategies.csv");
                    writeOwnConnections(data_filename_prefix + "OwnConnections.csv");
                    writeAllConnections(data_filename_prefix + "AllConnections.csv");
                }
            }

            reset(); // reset certain player attributes
            gen++; // move on to the next generation
        }

        calculateStats(); // record the avg p and p SD of the pop at the end of the run
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
            Alg1 run = new Alg1(); // one run of the algorithm
            run.start(); // start the run
            mean_avg_p_of_experiment += run.avg_p; // tally the mean avg p of the experiment
            avg_p_values_of_experiment[i] = run.avg_p;

            // display the final avg p of the pop of the run
            System.out.println("avg p of run "+i+" of experiment "+experiment_number+": "+run.avg_p);
        }

        // calculate stats
        mean_avg_p_of_experiment /= runs;
        for(int i=0;i<runs;i++){
            sd_avg_p_of_experiment+=Math.pow(avg_p_values_of_experiment[i]-mean_avg_p_of_experiment,2);
        }
        sd_avg_p_of_experiment = Math.pow(sd_avg_p_of_experiment / runs, 0.5);

        // display stats to user in console
        System.out.println("mean avg p="+DF4.format(mean_avg_p_of_experiment)
                +" avg p SD="+DF4.format(sd_avg_p_of_experiment));



        // write experiment data (results and settings) to a .csv data file.
        try{
            String filename = data_filename_prefix + "Data.csv";
            if(experiment_number == 0){ // write column headings
                fw = new FileWriter(filename, false);
                fw.append("exp num");
                fw.append(",mean avg p");
                fw.append(",avg p SD");
                fw.append(",runs");
                fw.append(",gens");
//                fw.append(",neighbourhood");
//                fw.append(",N");
                fw.append(",EWAE");
                fw.append(",EPR");
                fw.append(",ROC");
                fw.append(",leeway");
                fw.append(",MLB");
//                fw.append(",selection");
//                fw.append(",w");
//                fw.append(",evolution");
//                fw.append(",approach noise");
                fw.append(",mutation");
                fw.append(",u");
                fw.append(",delta");
            } else {
                fw = new FileWriter(filename, true);
            }
            fw.append("\n" + experiment_number); // write row data
            fw.append("," + DF4.format(mean_avg_p_of_experiment));
            fw.append("," + DF4.format(sd_avg_p_of_experiment));
            fw.append("," + runs);
            fw.append("," + gens);
//            fw.append("," + Player.getNeighbourhoodType());
//            fw.append("," + N);
            fw.append("," + EWAE);
            fw.append("," + EPR);
            fw.append("," + ROC);
            fw.append("," + DF4.format(leeway));
            fw.append("," + DF4.format(MLB));
//            fw.append("," + selection_method);
//            fw.append("," + DF4.format(w));
//            fw.append("," + evolution_method);
//            fw.append("," + DF4.format(approach_noise));
            fw.append("," + mutation_method);
            fw.append("," + DF4.format(u));
            fw.append("," + DF4.format(delta));
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }


        experiment_number++; // move on to the next experiment in the series
    }







    /**
     * Allows for the running of multiple experiments, i.e. the running of a series of
     * experiments, i.e. the running of an experiment series.
     */
    public static void experimentSeries(){

        // store initial value of varying parameter
        ArrayList<Double> various_amounts = new ArrayList<>();
        switch(varying_parameter_index){
            case 0->various_amounts.add((double)runs);
            case 1->various_amounts.add((double)gens);
            case 2->various_amounts.add((double)rows);
            case 3->various_amounts.add((double)EPR);
            case 4->various_amounts.add(ROC);
            case 5->various_amounts.add(leeway);
            case 10->various_amounts.add(MLB);
            case 6->various_amounts.add(w);
            case 7->various_amounts.add(approach_noise);
            case 8->various_amounts.add(u);
            case 9->various_amounts.add(delta);
        }

        // run experiment series
        for(int i=0;i<num_experiments;i++){

            // helps user keep track of the current value of the varying parameter
            System.out.println("\n===================\n" +
                    "NOTE: Start of experiment "+i+": "+varying_parameter+"="+DF4.format(various_amounts.get(i))+".");

            experiment(); // run an experiment
            switch(varying_parameter_index){ // change the value of the varying parameter
                case 0->{
                    runs+=(int)variation;
                    various_amounts.add((double)runs);
                }
                case 1->{
                    gens+=(int)variation;
                    various_amounts.add((double)gens);
                }
                case 2->{
                    rows+=(int)variation;
                    various_amounts.add((double)rows);
                }
                case 3->{
                    EPR+=(int)variation;
                    various_amounts.add((double)EPR);
                }
                case 4->{
                    ROC+=variation;
                    various_amounts.add(ROC);
                }
                case 5->{
                    leeway+=variation;
                    various_amounts.add(leeway);
                }
                case 10->{
                    MLB+=variation;
                    various_amounts.add(MLB);
                }
                case 6->{
                    w+=variation;
                    various_amounts.add(w);
                }
                case 7->{
                    approach_noise+=variation;
                    various_amounts.add(approach_noise);
                }
                case 8->{
                    u+=variation;
                    various_amounts.add(u);
                }
                case 9->{
                    delta+=variation;
                    various_amounts.add(delta);
                }
            }

            // informs user what the varying parameter's value was
            System.out.println("NOTE: End of "+varying_parameter+"="+DF4.format(various_amounts.get(i))+"."
                    +"\n===================");
        }

        // display a summary of the series in the console
        String summary = "";
        try{
            br = new BufferedReader(new FileReader(data_filename_prefix+"Data.csv"));
            String line;
            line = br.readLine(); // purposely ignore the row of headings
            int i=0; // indicates which index of the row we are currently on
            while((line=br.readLine()) != null){
                String[] row_contents = line.split(",");
                summary += "experiment="+row_contents[0];
                summary += "\tmean avg p="+DF4.format(Double.parseDouble(row_contents[1]));
                summary += "\tp SD="+DF4.format(Double.parseDouble(row_contents[2]));
                summary += "\t";
                switch(varying_parameter_index){
                    case 0->summary+="runs=";
                    case 1->summary+="gens=";
                    case 2->summary+="rows=";
                    case 3->summary+="EPR=";
                    case 4->summary+="ROC=";
                    case 5->summary+="leeway=";
                    case 10->summary+="MLB=";
                    case 6->summary+="sel noise=";
                    case 7->summary+="evo noise=";
                    case 8->summary+="u=";
                    case 9->summary+="delta=";
                }
                summary += DF4.format(various_amounts.get(i));
                i++;
                summary += "\n";
            }
            br.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println(summary);
    }






//    public void findNeighbours2D(Player player, int row_position, int col_position){
//        int a=row_position;
//        int b=col_position;
//        int c=grid.size();
//        int d=grid.get(0).size();
//        int up=((a-1)%c+c)%c; // go up one node (on the square grid)
//        int down=((a+1)%c+c)%c; // down
//        int left=((b-1)%d+d)%d; // left
//        int right=((b+1)%d+d)%d; // right
//
//        player.getNeighbourhood().add(grid.get(up).get((b%d+d)%d));
//        player.getNeighbourhood().add(grid.get(down).get((b%d+d)%d));
//        player.getNeighbourhood().add(grid.get((a%c+c)%c).get(left));
//        player.getNeighbourhood().add(grid.get((a%c+c)%c).get(right));
//
//        if(neighbourhood_type.equals("M")){
//            player.getNeighbourhood().add(grid.get(up).get(left)); // up-left
//            player.getNeighbourhood().add(grid.get(up).get(right)); // up-right
//            player.getNeighbourhood().add(grid.get(down).get(left)); // down-left
//            player.getNeighbourhood().add(grid.get(down).get(right)); // down-right
//        }
//    }


    /**
     * Assigns adjacent neighbours to the player in a 2D square lattice grid with respect
     * to the von Neumann or Moore neighbourhood type.
     * @param player is the player having their neighbourhood assigned.
     * @param y is the y coordinate of the player which represents the row they are in.
     * @param x is the x coordinate of the player which represents the column they are in.
     */
    public void assignAdjacentNeighbours(Player player, int y, int x){
        ArrayList<Player> neighbourhood = player.getNeighbourhood();

        int row_col_length = grid.size();

        int x_plus_one = (((x + 1) % row_col_length) + row_col_length) % row_col_length;
        int x_minus_one = (((x - 1) % row_col_length) + row_col_length) % row_col_length;
        int y_plus_one = (((y + 1) % row_col_length) + row_col_length) % row_col_length;
        int y_minus_one = (((y - 1) % row_col_length) + row_col_length) % row_col_length;

        neighbourhood.add(grid.get(y).get(x_plus_one)); // neighbour at x+1 i.e. to the right
        neighbourhood.add(grid.get(y).get((x_minus_one))); // neighbour at x-1 i.e. to the left
        neighbourhood.add(grid.get(y_plus_one).get(x)); // neighbour at y+1 i.e. above
        neighbourhood.add(grid.get(y_minus_one).get(x)); // neighbour at y-1 i.e. below

        if(neighbourhood_type.equals("M")){
            neighbourhood.add(grid.get(y_plus_one).get(x_plus_one)); // neighbour at (x+1,y+1)
            neighbourhood.add(grid.get(y_minus_one).get(x_plus_one)); // neighbour at (x+1,y-1)
            neighbourhood.add(grid.get(y_minus_one).get(x_minus_one)); // neighbour at (x-1,y-1)
            neighbourhood.add(grid.get(y_plus_one).get(x_minus_one)); // neighbour at (x-1,y+1)
        }
    }




    public void assignRandomNeighbours(Player player, int size){
        ArrayList<Player> neighbourhood = player.getNeighbourhood();


//        while(neighbourhood.size() != size){
//            int a = ThreadLocalRandom.current().nextInt(0,rows);
//            int b = ThreadLocalRandom.current().nextInt(0,rows);
//            Player neighbour = grid.get(a).get(b);
//            int id = neighbour.getId();
//            boolean add_neighbour = true;
//            if(id != player.getId()){
//                for(int i=0;i<player.getNeighbourhood().size();i++){
//                    if()
//                }
//            }
//            if(add_neighbour){
//                player.getNeighbourhood().add(neighbour);
//            }
//        }




//        int row_positions[] = new int[4];
//        ArrayList<Integer> row_positions = new ArrayList<>();
//        int i = 0;
//        row_positions.add(ThreadLocalRandom.current().nextInt(0,rows));
//        while(i < 4){
//            boolean bool = true;
//            for(int j=1;j<row_positions.size();j++){
//                if(row_positions.get(i) == row_positions.get(j)){
////                    row_positions.get(i) = ThreadLocalRandom.current().nextInt(0,rows);
//                    int x = 1;
//                    row_positions.get(i) = x;
//                    bool = false;
//                    break;
//                }
//            }
//            if(bool){
//                i++;
//            }
//        }
//
//
//
//
//        int col_positions[] = new int[4];







//        int row_positions[] = new int[4];
//        int col_positions[] = new int[4];
//        for(int i=0;i<row_positions.length;i++){
//            row_positions[i] = ThreadLocalRandom.current().nextInt(0,rows);
//            col_positions[i] = ThreadLocalRandom.current().nextInt(0,rows);
//        }
//        for(int i=0;i<size;i++){
//            while(true){
//                Player new_neighbour = grid.get(row_positions[i]).get(col_positions[i]);
//                int new_neighbour_id = new_neighbour.getId();
//                if(new_neighbour_id != player.getId()){
//                    for(int j=0;j<size;j++){
//                        if(new_neighbour_id != neighbourhood.get(j).getId()){
//                            neighbourhood.add(new_neighbour);
//                        }
//                    }
//                }
//            }
//        }



//        Set<Integer> rand_ints1 = new HashSet<>();
//        Set<Integer> rand_ints2 = new HashSet<>();
//        while(rand_ints1.size() < size){
//            rand_ints1.add(ThreadLocalRandom.current().nextInt(0, rows));
//        }
//        while(rand_ints2.size() < size){
//            rand_ints2.add(ThreadLocalRandom.current().nextInt(0, rows));
//        }
//        List<Integer> row_positions = new ArrayList<>(rand_ints1);
//        List<Integer> col_positions = new ArrayList<>(rand_ints2);
//        for(int i=0;i<size;i++){
//            neighbourhood.add(grid.get(col_positions.get(i)).get(row_positions.get(i)));
//        }





        Set<Integer> rand_ints = new HashSet<>();
        while(rand_ints.size() < size){
            rand_ints.add(ThreadLocalRandom.current().nextInt(N));
        }
        List<Integer> positions = new ArrayList<>(rand_ints); // position or id of new neighbours
        for(int position: positions){
            int col = position / rows;
            int row = position % rows;
            neighbourhood.add(grid.get(col).get(row));
//            grid.get(col).get(row).getNeighbourhood().add(player);
        }






    }






    /**
     * Calculate the average value of p and the standard deviation of p across the population
     * at the current generation.
     */
    public void calculateStats(){
        // calculate avg p
        avg_p = 0.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                avg_p+=player.getP();
            }
        }
        avg_p /= N;

        // calculate p SD
        p_SD = 0.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                p_SD += Math.pow(player.getP() - avg_p, 2);
            }
        }
        p_SD = Math.pow(p_SD / N, 0.5);
    }



    /**
     * For each player in the population, some attributes are reset in preparation
     * for the upcoming generation.
     */
    public void reset(){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setNI(0);
                player.setNSI(0);
                player.setNSP(0);
                player.setNSR(0);
                switch(game){
                    case"UG","DG"->{
                        player.setOldP(player.getP());
                        switch(game){
                            case"UG"->player.setOldQ(player.getQ());
                            case"DG"->player.setQ(0);
                        }
                    }
                }
            }
        }
    }




    public static void displaySettings(){
        String s = "";
        if(experiment_series && experiment_number == 0){ // if at start of series
            s += "Experiment series ("+desc+") varying "+varying_parameter+" by "+variation+ " between " +
                    num_experiments+" experiments: ";
        } else if(!experiment_series){
            s += "Experiment: ";
        }
        s+=game;
        s+=", "+runs+" runs";
        s+=", "+gens+" gens";
        s+=", "+rows+" rows";
        s+=", EWAE="+EWAE;
        s+=", EPR="+EPR;
        s+=", ROC="+ROC;
        s+=", leeway="+leeway;
        s+=", MLB="+MLB;
        s+=", "+neighbourhood_type+" neigh";
//        s+=", "+selection_method+" sel";
//        switch(selection_method){
//            case"variable"->s+=", w="+w;
//        }
        s+=", PPM="+Player.getPPM();
        switch(Player.getPPM()){
            case"avg score"->s+=", ASD="+Player.getASD();
        }
//        s+=", "+evolution_method+" evo";
//        switch(evolution_method){
//            case"approach"->s+=", approach noise="+approach_noise;
//        }
        switch(mutation_method){
            case"local","global"->{
                s+=", "+mutation_method+" mut";
                s+=", u="+u;
                switch(mutation_method){
                    case"local"->s+=", delta="+delta;
                }
            }
        }
        s+=":"; // signals that there are no more settings left
        System.out.println(s);
    }








    /**
     * Method for asking the user a binary question to receive a boolean answer.
     */
    public static boolean binaryQuestion(String question){
        boolean answer = false;
        boolean keep_looping = true;
        do{
            System.out.printf(question);
            switch(scanner.nextInt()){
                case 0 ->keep_looping = false; // 0 => no/false
                case 1 -> { // 1 => yes/true
                    answer = true;
                    keep_looping = false;
                }
                default ->System.out.println("ERROR: select a valid option");
            }
        } while(keep_looping);
        return answer;
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
        try{
            if(gen == 0){ // apply headings to file before writing data
                fw = new FileWriter(filename, false); // append set to false means writing mode.
                fw.append("gen"
                        + ",avg p"
                        + ",p SD"
                        + "\n"
                );
                fw.close();
            }

            // write the data
            fw = new FileWriter(filename, true); // append set to true means append mode.
            fw.append(gen + "");
            fw.append("," + DF4.format(avg_p));
            fw.append("," + DF4.format(p_SD));
            fw.append("\n");
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
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
        try{
            fw = new FileWriter(filename, false);
            for(ArrayList<Player> row:grid){
                for(int j=0;j<row.size();j++){
                    Player x = row.get(j);

                    // write number of successful interactions this gen.
                    fw.append(Integer.toString(x.getNSI()));


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


}
