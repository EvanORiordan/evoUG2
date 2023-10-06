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
    private int ID; // each player has a unique ID
    private double score; // amount of reward player has received from playing; i.e. this player's fitness
    private double p; // proposal value; real num within [0,1]
    private double q; // acceptance threshold value; real num within [0,1]
    private int games_played_in_total; // keep track of the total number of games this player has played
    private static String neighbourhood_type; // neighbourhood type of this player
    private ArrayList<Player> neighbourhood; // this player's neighbourhood
    private int games_played_this_gen;
    private static double prize; // the prize amount being split in an interaction
    private double old_p; // the p value held at the beginning of the gen; will be copied by imitators
    private double old_q; // the q value held at the beginning of the gen; will be copied by imitators
    private int role1_games; // how many games this player has played as role1
    private int role2_games; // how many games this player has played as role2
    private double average_score; // average score of this player this gen
    private static DecimalFormat DF1 = new DecimalFormat("0.0"); // 1 decimal point DecimalFormat
    private static DecimalFormat DF4 = new DecimalFormat("0.0000"); // 4 decimal point DecimalFormat

    /**
     * Stores edge weights belonging to the player.
     * For each neighbour y of player x, e_xy denotes the edge from x to y.
     * w_e_xy denotes the weight of e_xy probability of y dictating to x.
     * w_e_xy lies within the interval [0,1].
     * x controls w_e_xy i.e. the probability of x's neighbours getting to dictate to x.
     */
    private double[] edge_weights;

    private static double rate_of_change; // fixed amount by which edge weight is modified.

    // Tracks how many successful interactions the player had within some timeframe e.g. within a gen.
    private int num_successful_interactions = 0;

    // Tracks how many fair relationships the player has with their neighbours.
    private int num_fair_relationships = 0;








    /**
     * constructor for instantiating a player.
     * if DG, make sure to pass 0.0 double as q argument.
     */
    public Player(double p, double q){
        ID=count++; // assign this player's ID
        this.p=p; // assign p value
        this.q=q; // assign q value
        old_p=p;
    }


    /**
     * method for playing the UG.
     * receives a Player argument to play with.
     * if DG, the offer is always accepted since the responder/recipient/role2 player has q=0.0.
     *
     */
    public void playUG(Player responder) {
        if(p >= responder.q){ // accept offer
            updateStats(prize*(1-p), true);
            responder.updateStats(prize*p, false);
        } else { // reject offer
            updateStats(0, true);
            responder.updateStats(0, false);
        }
    }




    /**
     * Play the game with respect to space and edge weights.<br>
     * For each neighbour in your neighbourhood, propose to them if the weight of their edge
     * to you, which represents their likelihood of receiving from you, is greater than a
     * randomly generated double between 0 and 1.<br>
     * If the game is DG, you can mentally replace the word "propose" with "dictate".
     */
    public void playEWSpatialUG(){
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            double random_double = ThreadLocalRandom.current().nextDouble();
            double edge_weight = 0.0;
            for(int j=0;j<neighbour.neighbourhood.size();j++){ // find the edge weight.
                Player neighbours_neighbour = neighbour.getNeighbourhood().get(j);
                if(neighbours_neighbour.getId() == ID){
                    edge_weight = neighbour.getEdge_weights()[j];
                    break;
                }
            }
            if(edge_weight > random_double){
                playUG(neighbour);
            }
//            else {
//                System.out.println("(place BP here) EW too low");
//            }
        }
    }





    public double getScore(){
        return score;
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

    public double getQ(){
        return q;
    }

    // recall that q must lie within the range [0,1]
    public void setQ(double q){
        this.q=q;
        if(this.q>1){
            this.q=1;
        } else if(this.q<0){
            this.q=0;
        }
    }

    /**
     * Update the status of this player after playing, including score and average score.
     * The average score calculation is usual for seeing what score a player accrued over
     * the gen.<br>
     * 6/3/23: I have changed this to dividing by games_played_this_gen to games_played_in_total.
     */
    public void updateStats(double payoff, boolean role1){
        score+=payoff;
        games_played_in_total++;
        games_played_this_gen++;
        if(role1){
            role1_games++;
        } else{
            role2_games++;
        }
        average_score = score / games_played_this_gen;

        // check if the interaction was successful i.e. if any payoff was received.
        if(payoff != 0){
            num_successful_interactions++;
        }
    }

    // copy another player's strategy.
    public void copyStrategy(Player model){
        setP(model.old_p);
        setQ(model.old_q);
    }

    public int getId(){
        return ID;
    }

    public static String getNeighbourhoodType(){
        return neighbourhood_type;
    }

    public static void setNeighbourhoodType(String s){
        neighbourhood_type=s;
    }

    public void setGamesPlayedThisGen(int games_played_this_gen){
        this.games_played_this_gen = games_played_this_gen;
    }



    /**
     * Method for assigning the position of a player on a 2D space and finding the neighbours when a
     * player resides on a 2D space.<br>
     * Possible neighbourhood_type values: VN, M.
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
//            associated_edge_weights[i] = 1.0;
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
     *  fairness_interval.
     *
     *  A fair relationship here is loosely defined as one where the p values of the two players
     *  are within the fairness interval from each other. The "fairness interval" indicates how
     *  much leeway is being given.<br>
     *
     *  Nodes x and y have a fair relationship if p_y lies within [p_x - fairness_interval, p_y +
     *  fairness_interval].
     *
     *  E.g. p_1=0.4, p_2=0.37 and fairness_interval=0.05 is a fair relationship because p_2 lies within
     *  the interval [p_1 - fairness_interval, p_1 + fairness_interval] = [0.4 - 0.05, 0.4 + 0.05] =
     *  [0.35, 0.45] so here node 1 and 2 have a fair relationship.<br>
     *
     *  E.g. With p_3=0.2, p_4=0.05 and fairness_interval=0.1, node 3 and 4 do not have a fair
     *  relationship.<br>
     */
    public boolean identifyFairRelationship(Player neighbour, double fairness_interval){
        double p_y = neighbour.getP();
        double lower_bound = p - fairness_interval;
        double upper_bound = p + fairness_interval;
        if(lower_bound <= p_y && upper_bound >= p_y){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Identifies the number of fair relationships the player has with their neighbours.
     */
    public void identifyFairRelationships(double fairness_interval){
        for(Player neighbour: neighbourhood){
            if(identifyFairRelationship(neighbour, fairness_interval)){
                num_fair_relationships++;
            }
        }
    }



    public static double getPrize(){
        return prize;
    }

    public static void setPrize(double d){
        prize=d;
    }

    public ArrayList<Player> getNeighbourhood() {
        return neighbourhood;
    }

    public double getAverage_score(){
        return average_score;
    }

    public double getOld_p(){
        return old_p;
    }

    public void setOld_p(double old_p){
        this.old_p=old_p;
    }

    public double getOld_q(){
        return old_q;
    }

    public void setOld_q(double old_q){
        this.old_q=old_q;
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

    public static void setRate_of_change(double d){
        rate_of_change=d;
    }

    public int getNum_successful_interactions(){
        return num_successful_interactions;
    }

    public void setNum_successful_interactions(int i){
        num_successful_interactions=i;
    }

    public int getNum_fair_relationships(){
        return num_fair_relationships;
    }

    public void setNum_fair_relationships(int i){
        num_fair_relationships=i;
    }










    @Override
    public String toString(){
        // comment out a variable if you don't want it to appear in the player description when debugging
        String description = "";
        description += "ID="+ID;
        description += " p="+DF4.format(p);
//        description += " oldp="+DF4.format(old_p);
        if(q != 0){
            description += " q="+DF4.format(q);
        }
        description += " score="+DF4.format(score);
        description += " avgscore="+DF4.format(average_score);
        if(neighbourhood.size() != 0){
            description += " neighbours=[";
            for(int i=0;i<neighbourhood.size();i++){
                description += neighbourhood.get(i).getId();
                if((i+1) < neighbourhood.size()){ // are there more neighbours?
                    description +=", ";
                }
            }
            description +="]";
        }
        if(edge_weights.length != 0){
            description += " EW=[";
            for(int i=0;i<edge_weights.length;i++){
                description += DF4.format(edge_weights[i]);
                if((i+1) < neighbourhood.size()){ // are there more neighbours?
                    description +=", ";
                }
            }
            description +="]";
        }
        description += " GPTG="+ games_played_this_gen;
        description += " GPIT="+games_played_in_total;
        description += " R1G="+role1_games;
        description += " R2G="+role2_games;
        return description;
    }
}
