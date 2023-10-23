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

    private static int count = 0; // class-wide attribute that helps assign player id
    private final int id; // each player has a unique id
    private double score; // amount of reward player has received from playing; i.e. this player's fitness
    private double p; // proposal value; real num within [0,1]
    private double q; // acceptance threshold value; real num within [0,1]
    private static String neighbourhood_type; // neighbourhood type of this player
    private ArrayList<Player> neighbourhood; // this player's neighbourhood
    private static double prize; // the prize amount being split in an interaction
    private double old_p; // the p value held at the beginning of the gen; will be copied by imitators
    private double average_score; // average score of this player this gen
    private static final DecimalFormat DF1 = new DecimalFormat("0.0");
    private static final DecimalFormat DF4 = new DecimalFormat("0.0000");

    /**
     * Stores edge weights belonging to the player.
     * For each neighbour y of player x, e_xy denotes the edge from x to y.
     * w_e_xy denotes the weight of e_xy probability of y dictating to x.
     * w_e_xy lies within the interval [0,1].
     * x controls w_e_xy i.e. the probability of x's neighbours getting to dictate to x.
     */
    private double[] edge_weights;

    private static double rate_of_change; // fixed amount by which edge weight is modified.

    /**
     * NI: how many interactions the player had (within some timeframe e.g. within a gen).<br>
     * NSI: how many successful interactions the player had. A successful interaction is defined as an
     * interaction where payoff was received.<br>
     * NSD: how many successful interaction the player had as dictator.<br>
     * NSR: how many successful interaction the player had as recipient.<br>
     */
    private int num_interactions = 0;               // NI
    private int num_successful_interactions = 0;    // NSI
    private int num_successful_dictations;          // NSD
    private int num_successful_receptions;          // NSR

    private static double fairness_interval; // the leeway given when determining fair relationships
    private static double imitation_noise; // the amount of noise that may affect imitation evolution
    private static double approach_noise; // states by how much a player can change via approach evolution
    private static String selection_method; // indicates the selection method to be used
    private static String evolution_method; // indicates the evolution method to be used




    /**
     * Constructor method for instantiating a Player object.<br>
     * Since this is the DG, make sure to pass 0.0 to q.
     */
    public Player(double p, double q){
        id=count++; // assign this player's id
        this.p=p; // assign p value
        this.q=q; // assign q value
        old_p=p;
    }


    /**
     * Play the UG with respect to space and edge weights.<br>
     * For each neighbour y in x's neighbourhood, x proposes/dictates to them if the weight of y's
     * edge to x, which represents y's likelihood of receiving from x, is greater than a
     * randomly generated double between 0 and 1.<br>
     */
    public void playEWSpatialUG(){
        for(int i=0;i<neighbourhood.size();i++){ // loop through x's neighbourhood
            Player neighbour = neighbourhood.get(i);
            for(int j=0;j<neighbour.neighbourhood.size();j++){ // loop through y's neighbourhood
                Player neighbours_neighbour = neighbour.neighbourhood.get(j);
                if(neighbours_neighbour.id == id){ // find EW of y associated with x
                    double edge_weight = neighbour.edge_weights[j];
                    double random_double = ThreadLocalRandom.current().nextDouble();
                    if(edge_weight > random_double){ // x has EW% probability of success
                        if(p >= neighbour.q){ // accept offer
                            updateStats(prize * (1 - p), true);
                            neighbour.updateStats(prize * p, false);
                        } else { // reject offer
                            updateStats(0, true);
                            neighbour.updateStats(0, false);
                        }
                    }
                    break;
                }
            }
        }
    }




    public void setScore(double score){
        this.score=score;
    }

    public double getP(){
        return p;
    }

    // recall that p must lie within the range [0,1]
    public void setP(double p){
        this.p=p;
        if(this.p>1){
            this.p=1;
        } else if(this.p<0){
            this.p=0;
        }
    }


    /**
     * Update the status of the player after having played, including score and average score.<br>
     */
    public void updateStats(double payoff, boolean dictator){
        score+=payoff;
        num_interactions++;
        if(payoff != 0){ // check if the interaction was successful i.e. if any payoff was received.
            num_successful_interactions++;
            if(dictator){
                num_successful_dictations++;
            } else{
                num_successful_receptions++;
            }
        }

//        average_score = score / num_interactions;
        average_score = score / num_successful_interactions;
    }


    public int getId(){
        return id;
    }

    public static String getNeighbourhoodType(){
        return neighbourhood_type;
    }

    public static void setNeighbourhoodType(String s){
        neighbourhood_type=s;
    }




    /**
     * Method for assigning a position of the player x on a 2D space and finding x's neighbours in
     * the space.<br>
     *
     * Possible neighbourhood_type values: VN, M.<br>
     *
     * Neighbourhood order (with VN): up, down, left, right.<br>
     */
    public void findNeighbours2D(ArrayList<ArrayList<Player>> grid, int row_position, int column_position){
        neighbourhood = new ArrayList<>();
        int a=row_position;
        int b=column_position;
        int c=grid.size();
        int d=grid.get(0).size();
        int up=((a-1)%c+c)%c; // go up one node (on the square grid)
        int down=((a+1)%c+c)%c; // down
        int left=((b-1)%d+d)%d; // left
        int right=((b+1)%d+d)%d; // right
        neighbourhood.add(grid.get(up).get((b%d+d)%d));
        neighbourhood.add(grid.get(down).get((b%d+d)%d));
        neighbourhood.add(grid.get((a%c+c)%c).get(left));
        neighbourhood.add(grid.get((a%c+c)%c).get(right));
        if(neighbourhood_type.equals("M")){
            neighbourhood.add(grid.get(up).get(left)); // up-left
            neighbourhood.add(grid.get(up).get(right)); // up-right
            neighbourhood.add(grid.get(down).get(left)); // down-left
            neighbourhood.add(grid.get(down).get(right)); // down-right
        }
    }



    /**
     * Initialise edge_weights with respect to neighbourhood size.
     */
    public void initialiseEdgeWeights() {
        edge_weights = new double[neighbourhood.size()];
        for(int i=0;i<neighbourhood.size();i++){
            edge_weights[i] = 1.0; // initialise edge weight at 1.0
        }
    }



    /**
     * Method that allows players to perform a form of edge weight learning.
     * Here, a player x's edge weights is supposed to represent x's neighbours' relationship towards x.
     * If a neighbour y has a higher value of p than x, x raises the weight of their edge to y.
     * If y has a lower value of p than x, x reduces the weight of their edge to y.
     * The amount by which an edge is modified is determined by the rate_of_change parameter.
     */
    public void edgeWeightLearning(){
        for (int i = 0; i < neighbourhood.size(); i++) {
            Player neighbour = neighbourhood.get(i);
            if (neighbour.p > p) { // if neighbour is more generous than you, increase EW
                edge_weights[i] += rate_of_change;
                if(edge_weights[i] > 1.0){
                    edge_weights[i] = 1.0;
                }
            } else if(neighbour.p < p){ // if neighbour is less generous, decrease EW
                edge_weights[i] -= rate_of_change;
                if(edge_weights[i] < 0.0){
                    edge_weights[i] = 0.0;
                }
            }
        }
    }


    /**
     *  Identifies if the player and the given neighbour have a fair relationship under the given
     *  fairness_interval.<br>
     *
     *  A fair relationship here is loosely defined as one where the p values of the two players
     *  are within the fairness interval from each other. The "fairness interval" indicates how
     *  much leeway is being given.<br>
     *
     *  Nodes x and y have a fair relationship if p_y lies within [p_x - fairness_interval, p_y +
     *  fairness_interval].<br>
     *
     *  E.g. p_1=0.4, p_2=0.37 and fairness_interval=0.05 is a fair relationship because p_2 lies within
     *  the interval [p_1 - fairness_interval, p_1 + fairness_interval] = [0.4 - 0.05, 0.4 + 0.05] =
     *  [0.35, 0.45] so here node 1 and 2 have a fair relationship.<br>
     *
     *  E.g. With p_3=0.2, p_4=0.05 and fairness_interval=0.1, node 3 and 4 do not have a fair
     *  relationship.<br>
     */
    public boolean identifyFairRelationship(Player neighbour){
        double p_y = neighbour.getP();
        double lower_bound = p - fairness_interval;
        double upper_bound = p + fairness_interval;

        return lower_bound <= p_y && upper_bound >= p_y;
    }




    public static void setPrize(double d){
        prize=d;
    }

    public ArrayList<Player> getNeighbourhood() {
        return neighbourhood;
    }

    public void setOldP(double old_p){
        this.old_p=old_p;
    }

    public static DecimalFormat getDF1() { return DF1; }

    public static DecimalFormat getDF4(){
        return DF4;
    }

    public double[] getEdge_weights(){
        return edge_weights;
    }

    public static double getRate_of_change(){
        return rate_of_change;
    }

    public static void setRateOfChange(double d){
        rate_of_change=d;
    }

    public int getNum_successful_interactions(){
        return num_successful_interactions;
    }



    /**
     * Identifies the number of fair relationships the player has with their neighbours with
     * respect to the given fairness interval.
     */
    public int getNumFairRelationships(){
        int num_fair_relationships = 0;
        for(Player neighbour: neighbourhood){
            if(identifyFairRelationship(neighbour)){
                num_fair_relationships++;
            }
        }
        return num_fair_relationships;
    }



    @Override
    public String toString(){ // document details relating to the player
        String description = "";
        description += "id="+id;
        description += " p=" + DF4.format(p) + " ("+DF4.format(old_p) + ")"; // document p and old p
        description += " score="+DF4.format(score)+" ("+DF4.format(average_score)+")"; // score and avg score

        // document neighbourhood and EWs
        description += " neighbourhood=[";
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            description += neighbour.id + " (" + DF4.format(edge_weights[i]) + ")";
            if((i+1) < neighbourhood.size()){ // check if there are any neighbours left to document
                description +=", ";
            }
        }
        description +="]";

        description += " NI="+ num_interactions;
        description += " NSI="+ num_successful_interactions;
        description += " NSD="+ num_successful_dictations;
        description += " NSR="+ num_successful_receptions;
        return description;
    }


    /**
     * Finds the index of the player x in the neighbourhood of a neighbour.
     *
     * @return index
     */
    public int findXInNeighboursNeighbourhood(Player neighbour){
        int index = 0; // by default, assign index to 0.
        for (int l = 0; l < neighbour.neighbourhood.size(); l++) {
            Player y = neighbour.neighbourhood.get(l);
            if (id == y.getId()) {
                index = l;
            }
        }
        return index;
    }



    public static void setFairnessInterval(double d){
        fairness_interval=d;
    }



    // method for calculating sum of own connections of the player
    public double calculateOwnConnections(){
        double sum = 0.0;
        for(int i=0;i<edge_weights.length;i++){
            sum += edge_weights[i];
        }
        return sum;
    }



    // method for calculating sum of all associated connections of the player
    public double calculateAllConnections(){
        double sum = calculateOwnConnections();

        /**
         * How to find weights of neighbour edges associated with player x:
         * Loop through x's neighbourhood;
         * For each neighbour y of x, find x's index within y's neighbourhood;
         * Use the index to find the weight of the edge from y to x;
         * Add the weight to the sum.
         */
        for(int k=0;k<neighbourhood.size();k++) {
            Player y = neighbourhood.get(k);
            sum += y.edge_weights[findXInNeighboursNeighbourhood(y)];
        }

        return sum;
    }





    public static String getSelectionMethod(){
        return selection_method;
    }
    public static void setSelectionMethod(String s){
        selection_method=s;
    }
    public static String getEvolutionMethod(){
        return evolution_method;
    }
    public static void setEvolutionMethod(String s){
        evolution_method=s;
    }



    /**
     * Evolution method where child wholly copies parent's strategy.<br>
     * Evolution does not take place if the parent and the child are the same player.<br>
     */
    public void copyEvolution(Player parent){
        if(parent.id != id){
            setP(parent.old_p);
        }
    }



    public static double getImitationNoise(){
        return imitation_noise;
    }
    public static void setImitationNoise(double d){
        imitation_noise = d;
    }

    /**
     * Evolution method where evolver imitates parent's strategy with respect to noise i.e.
     * new strategy lies within interval centred around parent's strategy.<br>
     * Evolution does not take place if the parent and the child are the same player.<br>
     * The lower the noise, the more accurate the imitations will generally be.<br>
     * If noise is set to 0, the result of the evolution is identical to copy evolution.<br>
     */
    public void imitationEvolution(Player parent){
        if(parent.id != id){
            double new_p = ThreadLocalRandom.current().nextDouble(
                    parent.old_p - imitation_noise, parent.old_p + imitation_noise);
            setP(new_p);
        }
    }


    public double getAverageScore(){
        return average_score;
    }



    /**
     * Evolution method where child's strategy gets closer to, i.e. approaches, parent's strategy.
     * The amount by which the child's strategy approaches the parent's is a randomly generated
     * double between 0.0 and the approach limit.<br>
     * Evolution does not take place if the parent and the child are the same player.<br>
     * The greater the noise, the greater the approach is.<br>
     */
    public void approachEvolution(Player parent){
        if(parent.id != id){
            double approach = ThreadLocalRandom.current().nextDouble(approach_noise);
            if(parent.old_p < p){ // if parent's p is lower, reduce p; else, increase p
                approach *= -1;
            }
            double new_p = p + approach;
            setP(new_p);
        }
    }

    public static double getApproachNoise(){
        return approach_noise;
    }
    public static void setApproachNoise(double d){
        approach_noise=d;
    }





    public void setNumInteractions(int i){
        num_interactions=i;
    }
    public void setNumSuccessfulInteractions(int i){
        num_successful_interactions=i;
    }
    public void setNumSuccessfulDictations(int i){
        num_successful_dictations=i;
    }
    public void setNumSuccessfulReceptions(int i){
        num_successful_receptions=i;
    }
}
