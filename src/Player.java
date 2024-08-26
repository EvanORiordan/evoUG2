import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Evan O'Riordan (e.oriordan3@universityofgalway.ie)<br>
 * 02/10/2023<br>
 * School of Computer Science<br>
 * University of Galway<br>
 *
 *
 * Player class for instantiating player objects.<br>
 */
public class Player {

    private static int count = 0; // class-wide attribute that helps assign player ID
    private final int ID; // each player has a unique ID i.e. position
    public int getID(){
        return ID;
    }

    private static final DecimalFormat DF1 = new DecimalFormat("0.0");
    public static DecimalFormat getDF1() { return DF1; }
    private static final DecimalFormat DF2 = new DecimalFormat("0.00");
    public static DecimalFormat getDF2(){return DF2;}
    private static final DecimalFormat DF4 = new DecimalFormat("0.0000");
    public static DecimalFormat getDF4(){
        return DF4;
    }

    private static String game;
    public static void setGame(String s){game=s;}


    private double p; // proposal value residing within [0,1]
    public double getP(){
        return p;
    }
    public void setP(double p){ // recall that p must lie within the range [0,1]
        this.p=p;
        if(this.p>1){
            this.p=1;
        } else if(this.p<0){
            this.p=0;
        }
    }
    private double old_p; // p value held at beginning of gen to be inherited by children
    public void setOldP(double old_p){
        this.old_p=old_p;
    }
    private double q; // acceptance threshold value residing within [0,1]
    public double getQ(){return q;}
    public void setQ(double q){
        this.q=q;
        if(this.q>1){
            this.q=1;
        } else if(this.q<0){
            this.q=0;
        }
    }
    private double old_q; // q value held at beginning of gen to be inherited by children
    public void setOldQ(double old_q){
        this.old_q=old_q;
    }
    public void setStrategy(double p, double q){
        setP(p);
        setQ(q);
    }
    private double local_leeway; // inherent leeway of the player
    /**
     * Constructor method for instantiating a Player object.<br>
     * Since this is the DG, make sure to pass 0 to q.<br>
     * @param p is the proposal value of the player
     * @param q is the acceptance threshold value of the player
     * @param leeway2 is a factor used to calculate the player's local leeway
     */
    public Player(double p, double q, double leeway2){
        ID=count++; // assign this player's ID
        setP(p); // assign p value
        setQ(q); // assign q value
        old_p=p;
        old_q=q;

        // assign local_leeway value
        if(leeway2 == 0){
            local_leeway = 0;
        } else {
            local_leeway = ThreadLocalRandom.current().nextDouble(-leeway2,leeway2);
        }
    }








    private static double gross_prize; // the gross prize available in an interaction
    public static void setGrossPrize(double d){
        gross_prize=d;
    }

    private ArrayList<Player> neighbourhood = new ArrayList<>(); // contains player's neighbours
    public ArrayList<Player> getNeighbourhood() {
        return neighbourhood;
    }

    private int[] neighbour_IDs; // array of the IDs of the player's neighbour
    public void setNeighbourIDs(int[] arr){
        neighbour_IDs=arr;
    }

    /**
     * Stores edge weights belonging to the player.
     * For each neighbour y of player x, e_xy denotes the edge from x to y.
     * w_e_xy denotes the weight of e_xy probability of y dictating to x.
     * w_e_xy lies within the interval [0,1].
     * x controls w_e_xy i.e. the probability of x's neighbours getting to dictate to x.
     */
    private double[] edge_weights;
    public double[] getEdgeWeights(){
        return edge_weights;
    }

    /**
     * Ultimatum game where edge weights affects probability to interact.<br>
     * The greater the weight, the greater the likelihood of the player being pointed at of interacting
     * with the owner of the edge.<br>
     * If EWL is disabled, this function defaults to the standard ultimatum game.
     */
    public void playUG1(){
        for(int i=0;i<neighbourhood.size();i++){ // loop through x's neighbourhood
            Player neighbour = neighbourhood.get(i);
            for(int j=0;j<neighbour.neighbourhood.size();j++){ // loop through y's neighbourhood
                Player neighbours_neighbour = neighbour.neighbourhood.get(j);
                if(neighbours_neighbour.ID == ID){ // find EW of y associated with x
                    double edge_weight = neighbour.edge_weights[j];
                    double random_double = ThreadLocalRandom.current().nextDouble();
                    if(edge_weight > random_double){ // x has EW% probability of success
                        UG(gross_prize, neighbour);
                    }
                    else{ // if interaction did not successfully occur, avg score decreases.
                        unsuccessfulInteraction(neighbour);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Ultimatum Game where edge weights are applied as a factor in payoff calculation.<br>
     * If EWL is disabled, this function defaults to the standard ultimatum game.
     */
    public void playUG2(){
        for(Player neighbour: neighbourhood){
            for(int j=0;j<neighbour.neighbourhood.size();j++){
                Player neighbours_neighbour = neighbour.neighbourhood.get(j);
                if(neighbours_neighbour.ID == ID) {
                    UG(neighbour.edge_weights[j] * gross_prize, neighbour);
                    break;
                }
            }
        }
    }



    /**
     * Standard ultimatum Game where the player proposes to the responder.
     * @param responder
     */
    public void UG(double net_prize, Player responder){
        if(p >= responder.q){
            successfulInteraction(net_prize, responder); // responder accepts proposal
        } else {
            unsuccessfulInteraction(responder); // responder rejects proposal
        }
    }


    /**
     * Method for updating player statistics after a successful interaction.
     * @param responder
     */
    public void successfulInteraction(double net_prize, Player responder){
        updateStats(net_prize - (net_prize * p), true, responder.ID);
        responder.updateStats(net_prize * p, false, ID);
    }

    /**
     * Method for updating player statistics after an unsuccessful interaction.
     * @param responder
     */
    public void unsuccessfulInteraction(Player responder){
        updateStats(0, true, responder.ID);
        responder.updateStats(0, false, ID);
    }




    private int NI = 0;  // num interactions (NI) player had
    public void setNI(int i){
        NI=i;
    }
    private int NSI = 0; // num successful interactions (NSI) player had i.e. num interactions earned payoff
    public void setNSI(int i){
        NSI=i;
    }
    private int[] NSI_per_neighbour = {0,0,0,0};
    public int[] getNSIPerNeighbour(){return NSI_per_neighbour;}
    public void setNSIPerNeighbour(int[] arr){NSI_per_neighbour=arr;}
    private int NSP = 0; // num successful proposals (NSP)
    public void setNSP(int i){
        NSP=i;
    }
    private int NSR = 0; // num successful receptions (NSR)
    public void setNSR(int i){
        NSR=i;
    }
    private double score; // total accumulated payoff i.e. fitness
    public void setScore(double score){
        this.score=score;
    }
    private double average_score; // $\overline{\Pi}$: average score of this player (this gen)
    public double getAverageScore(){return average_score;}
    private static String ASD = ""; // average score denominator determines how average score is calculated
    public static String getASD(){return ASD;}
    public static void setASD(String s){ASD=s;}

    /**
     * Update the status of the player after having played, including score and average score.
     */
    public void updateStats(double payoff, boolean proposer, int neighbour_ID){
        score+=payoff;
        NI++;
        if(payoff > 0){ // check if the interaction was successful i.e. if any payoff was received.
            NSI++;


            // update NSI with neighbour
            for(int i=0;i<neighbourhood.size();i++){
                if(neighbourhood.get(i).getID() == neighbour_ID){
                    NSI_per_neighbour[i]++;
                    break;
                }
            }


            if(proposer){
                NSP++;
            } else{
                NSR++;
            }
        }
        switch(ASD){
            case"NI"->average_score = score / NI;
            case"NSI"->average_score = score / NSI;
            default -> {
                System.out.println("[ERROR] Invalid average score denominator configured. Exiting...");
                Runtime.getRuntime().exit(0);
            }
        }
    }






    /**
     * Initialise the player's edge weights.
     */
    public void initialiseEdgeWeights() {
        edge_weights = new double[neighbourhood.size()];
        for(int i=0;i<neighbourhood.size();i++){
            edge_weights[i] = 1.0; // initialise edge weight at 1.0
        }
    }





    /**
     * Edge weight learning method.
     * @param EWLC is the condition used to determine whether positive, negative or no edge weight learning will occur.
     * @param EWLF is the formula used to calculate the amount of edge weight learning.
     * @param ROC is the rate of change of edge weights
     * @param leeway1 is the leeway allowed by the player to their neighbours
     * @param leeway3 is a factor used to calculate the player's edge weight leeway with the neighbour
     */
    public void EWL(String EWLC,
                    String EWLF,
                    double ROC,
                    double alpha,
                    double beta,
                    double leeway1,
                    double leeway3,
                    double leeway4,
                    double leeway5,
                    double leeway6,
                    double leeway7)
    {
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            double total_leeway = calculateTotalLeeway(neighbour, i, leeway1, leeway3, leeway4, leeway5, leeway6, leeway7);
            int option = checkEWLC(EWLC, neighbour, alpha, beta, total_leeway);
            if(option == 0){ // positive edge weight learning
                edge_weights[i] += calculateLearning(EWLF, neighbour, ROC, alpha, beta);
                if(edge_weights[i] > 1.0){ // ensure edge weight resides within [0,1.0]
                    edge_weights[i] = 1.0;
                }
            } else if (option == 1){ // negative edge weight learning
                edge_weights[i] -= calculateLearning(EWLF, neighbour, ROC, alpha, beta);
                if(edge_weights[i] < 0){ // ensure edge weight resides within [0,1.0]
                    edge_weights[i] = 0;
                }
            }
            // if option equals 2, no edge weight learning occurs
        }
    }

    /**
     * @param neighbour
     * @param i
     * @param leeway1
     * @param leeway3
     * @param leeway4
     * @param leeway5
     * @param leeway6
     * @param leeway7
     * @return total leeway to be given by the player to the neighbour during edge weight learning
     */
    public double calculateTotalLeeway(Player neighbour,
                                       int i,
                                       double leeway1,
                                       double leeway3,
                                       double leeway4,
                                       double leeway5,
                                       double leeway6,
                                       double leeway7)
    {
        double global_leeway = leeway1;
        double edge_weight_leeway = edge_weights[i] * leeway3;
        double p_comparison_leeway = (neighbour.p - p) * leeway4;
        double p_leeway = neighbour.p * leeway5;
        double random_leeway;
        if(leeway6 == 0){
            random_leeway = 0;
        } else{
            random_leeway = ThreadLocalRandom.current().nextDouble(-leeway6, leeway6);
        }
        double avg_score_comparison_leeway = (average_score - neighbour.average_score) * leeway7;

        double total_leeway = global_leeway
                + local_leeway
                + edge_weight_leeway
                + p_comparison_leeway
                + p_leeway
                + random_leeway
                + avg_score_comparison_leeway;

        return total_leeway;
    }

    /**
     * Checks the edge weight learning condition to determine what kind of edge weight
     * learning should occur, if any.
     *
     * @param EWLC is the edge weight learning condition
     * @param neighbour is the neighbour being pointed at by the edge
     * @param total_leeway is the leeway being given to the neighbour by the edge's owner
     * @return 0 for positive edge weight learning, 1 for negative, 2 for none
     */
    public int checkEWLC(String EWLC, Player neighbour, double alpha, double beta, double total_leeway) {
        int option = 2;
        switch(EWLC){
            case"p"->{ // compare proposal values
                if (neighbour.p + total_leeway > p){
                    option = 0;
                } else if (neighbour.p + total_leeway < p){
                    option = 1;
                }
            }
            case"avgscore"->{ // compare average scores
                if (neighbour.average_score < average_score + total_leeway){
                    option = 0;
                } else if (neighbour.average_score > average_score + total_leeway){
                    option = 1;
                }
            }
            case"AB"->{ // compare alpha-beta rating
                double AB_rating1 = ((alpha * p) + (beta * average_score)) / (alpha + beta);
                double AB_rating2 = ((alpha * neighbour.p) + (beta * neighbour.average_score)) / (alpha + beta);
                if(AB_rating1 < AB_rating2)
                    option = 0;
                else if(AB_rating1 > AB_rating2)
                    option = 1;
            }
            default->System.out.println("[NOTE] No edge weight learning condition configured.");
        }

        return option;
    }

    /**
     * Calculate amount of edge weight learning to be applied to the weight of the edge.
     * @param EWLF
     * @param neighbour
     * @param ROC
     */
    public double calculateLearning(String EWLF, Player neighbour, double ROC, double alpha, double beta){
        double learning = 0;
        switch(EWLF){
            case"ROC"->         learning = ROC;
            case"pAD"->         learning = Math.abs(neighbour.p - p);
            case"pEAD"->        learning = Math.exp(Math.abs(neighbour.p - p));
            case"avgscoreAD"->  learning = Math.abs(neighbour.average_score - average_score);
            case"avgscoreEAD"-> learning = Math.exp(Math.abs(neighbour.average_score - average_score));
            case"pAD2"->        learning=Math.pow(Math.abs(neighbour.p - p), 2);
            case"pAD3"->        learning=Math.pow(Math.abs(neighbour.p - p), 3);
            case"AB"->          learning=Math.abs((((alpha * p) + (beta * average_score)) / (alpha + beta))
                    - (((alpha * neighbour.p) + (beta * neighbour.average_score)) / (alpha + beta)));

            default->System.out.println("[NOTE] No edge weight learning condition configured.");
        }

        return learning;
    }



    /**
     * mean of edge weights belonging to the player
     */
    private double mean_self_edge_weight = 0;
    public double getMeanSelfEdgeWeight(){return mean_self_edge_weight;}
    public void calculateMeanSelfEdgeWeight(){
        mean_self_edge_weight = 0;
        for(int i = 0; i < edge_weights.length; i++){
            mean_self_edge_weight += edge_weights[i];
        }
        mean_self_edge_weight /= edge_weights.length;
    }


    /**
     * mean of edge weights belonging to the player's neighbour that are directed at the player.<br>
     * function assumes all players have same number of edge weights and neighbours. <br>
     * otherwise the function does not signify much.
     */
    private double mean_neighbour_edge_weight = 0;
    public double getMeanNeighbourEdgeWeight(){return mean_neighbour_edge_weight;}
    public void calculateMeanNeighbourEdgeWeight(){
        mean_neighbour_edge_weight = 0;
        for(int i = 0; i < neighbourhood.size(); i++) {
            Player neighbour = neighbourhood.get(i);
            mean_neighbour_edge_weight += neighbour.edge_weights[findMeInMyNeighboursNeighbourhood(neighbour)];
        }
        mean_neighbour_edge_weight /= edge_weights.length;
    }

    /**
     * Returns the index of this player in the neighbourhood of their neighbour.<br>
     * Assumes the player and the neighbour have the same number of neighbours and edge weights.
     */
    public int findMeInMyNeighboursNeighbourhood(Player my_neighbour){
        int my_index_in_neighbours_neighbourhood = 0; // by default, assign index to 0.
        int my_id = ID;
        for (int i = 0; i < my_neighbour.neighbourhood.size(); i++) {
            Player neighbours_neighbour = my_neighbour.neighbourhood.get(i);
            if (my_id == neighbours_neighbour.ID) {
                my_index_in_neighbours_neighbourhood = i;
                break;
            }
        }

        return my_index_in_neighbours_neighbourhood;
    }





    @Override
    public String toString(){ // document details relating to the player

        // document key player attributes
        String player_desc = "";
        player_desc += "ID="+ID;
        switch(game){
            case"UG","DG"->{
                player_desc += " p=" + DF4.format(p) // p
                        + " ("+DF4.format(old_p) + ")"; // old p
                switch(game){
                    case"UG"-> {
                        player_desc += " q=" + DF4.format(q) // q
                                + " (" + DF4.format(old_q) + ")"; // old q
                    }
                }
            }
        }
        player_desc+=" score="+DF4.format(score); // score
        player_desc+=" ("+DF4.format(average_score)+")"; // avg score


        // document EW and NSI per neighbour
        player_desc += " neighbourhood=[";
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            player_desc += "("
                    + neighbour.ID + ", " // neighbour ID
                    + DF2.format(edge_weights[i]) + ", " // EW with neighbour
                    + NSI_per_neighbour[i] + ")"; // NSI with neighbour
            if((i+1) < neighbourhood.size()){ // check if there are any neighbours left to document
                player_desc +=", ";
            }
        }
        player_desc +="]";


        // document interaction stats
        player_desc += " NI="+ NI;
        player_desc += " NSI="+ NSI;
        player_desc += " NSD="+ NSP;
        player_desc += " NSR="+ NSR;

        return player_desc;
    }






    /**
     * Evolution method where child wholly copies parent's strategy.
     * @param parent is the parent the player is copying.
     */
    public void copyEvolution(Player parent){
        setStrategy(parent.old_p, parent.old_q);
    }



    /**
     * Evolution method where child's strategy gets closer to, i.e. approaches, parent's strategy.
     * The amount by which the child's strategy approaches the parent's is a randomly generated
     * double between 0 and the approach limit.<br>
     * Evolution does not take place if the parent and the child are the same player.<br>
     * The greater the noise, the greater the approach is.<br>
     */
    public void approachEvolution(Player parent, double approach_noise){
        if(parent.ID != ID){
            double approach = ThreadLocalRandom.current().nextDouble(approach_noise);
            if(parent.old_p < p){ // if parent's p is lower, reduce p; else, increase p
                approach *= -1;
            }
            double new_p = p + approach;

            if(parent.old_q < q){
                approach *= -1;
            }
            double new_q = q + approach;
            setStrategy(new_p, new_q);
        }
    }





    /**
     * Selection method where the child compares their score with their neighbours'. The
     * greater the difference in score, the neighbour's probability of being selected as child's
     * parent is exponentially affected. If a parent is not selected, the child is selected as parent
     * by default.<br>
     */
    public Player rouletteWheelSelection(){
        Player parent = this; // by default, the child is the parent
        double[] parent_scores = new double[neighbourhood.size()];
        double total_parent_score = 0; // track the sum of the "parent scores" of the neighbourhood
        for(int i=0;i<neighbourhood.size();i++){ // calculate the parent scores of the neighbourhood
            parent_scores[i] = Math.exp(neighbourhood.get(i).average_score-average_score);
            total_parent_score += parent_scores[i];
        }
        total_parent_score += 1.0; // effectively this gives the child a slot on their own roulette
        double parent_score_tally = 0; // helps us count through the parent scores up to the total

        // the first parent score to be greater than this double is the winner.
        // if no neighbour's parent score wins, the child wins.
        double random_double = ThreadLocalRandom.current().nextDouble();
        for(int j=0;j<neighbourhood.size();j++){
            parent_score_tally += parent_scores[j];
            double percentile = parent_score_tally / total_parent_score;
            if (random_double < percentile) {
                parent = neighbourhood.get(j);
                break;
            }
        }

        return parent;
    }



    /**
     * Selection method where child selects the highest scoring neighbour this gen as parent if
     * that neighbour scored higher than the child.
     */
    public Player bestSelection(){
        Player parent;
        int best_index = 0;
        for(int i=1;i<neighbourhood.size();i++){ // find the highest scoring neighbour
            Player neighbour = neighbourhood.get(i);
            Player best = neighbourhood.get(best_index);
            if(neighbour.average_score > best.average_score){
                best_index=i;
            }
        }
        parent = neighbourhood.get(best_index);
        if(parent.average_score <= average_score){
            parent = this;
        }

        return parent;
    }



    /**
     * Inspired by Rand et al. (2013) (rand2013evolution).<br>
     * Calculate effective payoffs of neighbours. Select highest as parent.<br>
     * w represents the intensity of selection.
     */
    public Player variableSelection(double w){
        Player parent;
        double[] effective_payoffs = new double[neighbourhood.size()];
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            effective_payoffs[i] = Math.exp(w * neighbour.average_score);
        }
        double best_effective_payoff = effective_payoffs[0];
        int index = 0;
        for(int j=1;j<effective_payoffs.length;j++){
            if(effective_payoffs[j] > best_effective_payoff){
                index = j;
            }
        }
        parent = neighbourhood.get(index);

        return parent;
    }











    /**
     * Mutation rate parameter determines the probability for mutation to occur.
     * @param mutation_rate is the probability that mutation occurs.
     * @return boolean indicating whether mutation will occur.
     */
    public boolean mutationCheck(double mutation_rate){
        double random_double = ThreadLocalRandom.current().nextDouble();

        return random_double < mutation_rate;
    }



    /**
     * Mutation method where the player's strategy is randomly generated. Mutation is independent.
     * Inspired by Akdeniz and van Veelen (2023).
     */
    public void globalMutation(){
        setStrategy(ThreadLocalRandom.current().nextDouble(), ThreadLocalRandom.current().nextDouble());
    }



    /**
     * Mutation method where attributes are modified by a random double within an interval
     * determined by delta. The mutation is independent as the mutation applied to
     * one attribute is independent of the mutation of another.
     * Inspired by Akdeniz and van Veelen (2023).
     */
    public void localMutation(double delta){
        double new_p = ThreadLocalRandom.current().nextDouble(p - delta, p + delta);
        double new_q = ThreadLocalRandom.current().nextDouble(q - delta, q + delta);
        setStrategy(new_p,new_q);
    }










    // ================= PD =================

    private String[] strategiesPD = {"C","D","A","TFT"};

    private String strategyPD = "";


    public void PD(double T, double R, double P, double S, double l, Player partner){
        if(strategyPD.equals("C") && partner.strategyPD.equals("C")){
            score += R;
            partner.score += R;
        } else if(strategyPD.equals("C") && partner.strategyPD.equals("D")){
            score += S;
            partner.score += T;
        }else if(strategyPD.equals("D") && partner.strategyPD.equals("C")){
            score += T;
            partner.score += S;
        }else if(strategyPD.equals("D") && partner.strategyPD.equals("D")){
            score += P;
            partner.score += P;
        }else if(strategyPD.equals("A") || partner.strategyPD.equals("A")){
            score += l;
            partner.score += l;
        }
        NI++;
        partner.NI++;
        average_score = score / NI;
        partner.average_score = partner.score / partner.NI;
    }






//    public void play() {
//        switch (EWT) {
//            case "1" -> {
//                for (int i = 0; i < neighbourhood.size(); i++) {
//                    Player neighbour = neighbourhood.get(i);
//                    for (int j = 0; j < neighbour.neighbourhood.size(); j++) {
//                        Player neighbours_neighbour = neighbour.neighbourhood.get(j);
//                        if (neighbours_neighbour.ID == ID) {
//                            double edge_weight = neighbour.edge_weights[j];
//                            double random_double = ThreadLocalRandom.current().nextDouble();
//                            if (edge_weight > random_double) {
//                                UG(gross_prize, neighbour);
//                            } else {
//                                unsuccessfulInteraction(neighbour);
//                            }
//                            break;
//                        }
//                    }
//                }
//            }
//            case "2" ->{
////                for(Player neighbour: neighbourhood){
//                for(int i=0;i<neighbourhood.size();i++){
//                    Player neighbour = neighbourhood.get(i);
//                    for(int j=0;j<neighbour.neighbourhood.size();j++){
//                        Player neighbours_neighbour = neighbour.neighbourhood.get(j);
//                        if(neighbours_neighbour.ID == ID) {
//                            switch(game){
//                                case"UG","DG"->{
//                                    UG(neighbour.edge_weights[j] * gross_prize, neighbour);
//                                }
//                                case"PD"->{
//                                    PD(T, R, P, S, l, neighbour);
//                                }
//                            }
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//    }



}
