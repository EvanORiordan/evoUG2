import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Evan O'Riordan (e.oriordan3@universityofgalway.ie)<br>
 * School of Computer Science<br>
 * University of Galway<br>
 */
public class Agent {

    // ===================================== Attributes =====================================
    private static int count; // class-wide attribute that helps assign agent ID
    private final int ID; // each agent has a unique ID i.e. position
    private static final DecimalFormat DF1 = new DecimalFormat("0.0");
    private static final DecimalFormat DF2 = new DecimalFormat("0.00");
    private static final DecimalFormat DF4 = new DecimalFormat("0.0000");
    private static String game; // indicates what game agent is playing
    private double p; // proposal value residing within [0,1]
    private double oldP; // p value held at beginning of gen to be inherited by children
    private double mean_p_omega;
    private double q; // acceptance threshold value residing within [0,1]
    private double oldQ; // q value held at beginning of gen to be inherited by children
    private ArrayList<Agent> omega; // neighbourhood
    private ArrayList <Double> edgeWeights; // using Double arraylist rather than double array allows us to use ArrayList.add() to add things to the collection without an index.
    private int NSP = 0; // num successful proposals (NSP)
    private int NSR = 0; // num successful receptions (NSR)
    private double mean_self_edge_weight; // mean of edge weights from agent to its neighbours
    private double mean_neighbour_edge_weight; // mean of edge weights directed at agent
    private double x; // x position in the topology
    private double y; // y position in the topology
    private double u; // utility
    private int k; // degree
    private int sel_rank;
    private static boolean NU = true; // by default, allow individuals to have negative utility.
    private static double v; // vindictiveness of the agent




    /**
     * Constructor method for instantiating a UG/DG Player object.
     * @param p is the proposal value of the agent
     * @param q is the acceptance threshold value of the agent
     */
    public Agent(double x, double y, double p, double q){
        ID=count++;
        this.x=x;
        this.y=y;
        this.p=p;
        this.q=q;
        oldP=p;
        oldQ=q;
    }



    /**
     * Augmented setter.
     * p must reside within [0,1].
      */
    public void setP(double d){
        p=d;
        if(p>1){
            p=1;
        } else if(p<0){
            p=0;
        }
    }



//    /**
//     * Augmented setter.
//     * q must reside within [0,1].
//     */
//    public void setQ(double q){
//        this.q=q;
//        if(this.q>1){
//            this.q=1;
//        } else if(this.q<0){
//            this.q=0;
//        }
//    }



    @Override
    public String toString(){ // document details relating to the agent
        String agent_desc = "";
        agent_desc += "ID="+ID;
//        agent_desc += " pos=("+x+","+y+")";
        switch(game){
            case"UG","DG"->{
                agent_desc += " p=" + DF4.format(p) // p
                        + " ("+DF4.format(oldP) + ")"; // old p
                switch(game){
                    case"UG"-> {
                        agent_desc += " q=" + DF4.format(q) // q
                                + " (" + DF4.format(oldQ) + ")"; // old q
                    }
                }
            }
        }
//        agent_desc+=" pi="+DF4.format(pi); // score
        agent_desc += " u=" + DF4.format(u); // utility

        agent_desc += " omega=[";
        for(int i=0;i<omega.size();i++){
            agent_desc += omega.get(i).ID;
            if((i + 1) < omega.size()){
                agent_desc += ", ";
            }
        }
        agent_desc += "]";

        agent_desc += " weights=[";
        for(int i=0;i<edgeWeights.size();i++){
            agent_desc += DF2.format(edgeWeights.get(i));
            if((i + 1) < edgeWeights.size()){
                agent_desc += ", ";
            }
        }
        agent_desc += "]";

        // interaction stats
//        agent_desc += " NI="+ NI;
//        agent_desc += " MNI=" + MNI;
//        agent_desc += " NSI="+ NSI;
//        agent_desc += " NSP="+ NSP;
//        agent_desc += " NSR="+ NSR;

        return agent_desc;
    }


    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(!(obj instanceof Agent)) return false;
        Agent obj2 = (Agent) obj;
        return ID == obj2.ID;
    }


//    // degree is equal to number of edges i.e. number of neighbours i.e. size of neighbourhood
//    public void calculateDegree(){
//        k = neighbourhood.size();
//    }


    // calculate mean p of omega
    public void calculateMeanPOmega(){
        mean_p_omega = 0.0;
        int size = omega.size();
        for(int i = 0; i < size; i++){
            mean_p_omega += omega.get(i).getP();
        }
        mean_p_omega /= size;
    }


    public void setU(double d){
        u=d;

        // ensures that u >= 0 if NU is disabled.
        if(!NU){
            if(u<0){
                u=0;
            }
        }
    }





    // generic functions
    public int getID(){
        return ID;
    }
    public static DecimalFormat getDF1() { return DF1; }
    public static DecimalFormat getDF2(){return DF2;}
    public static DecimalFormat getDF4(){
        return DF4;
    }
    public static void setGame(String s){game=s;}
    public double getP(){
        return p;
    }
    public double getOldP(){return oldP;}
    public void setOldP(double oldP){
        this.oldP=oldP;
    }
    public double getQ(){return q;}
    public void setQ(double d){q=d;}
    public double getOldQ(){return oldQ;}
    public void setOldQ(double oldQ){
        this.oldQ=oldQ;
    }
    public ArrayList<Agent> getOmega() {
        return omega;
    }
    public void setOmega(ArrayList <Agent> x){omega=x;}
    public ArrayList <Double> getEdgeWeights(){return edgeWeights;}
    public void setEdgeWeights(ArrayList <Double> x){edgeWeights=x;}
    public double getY(){return y;}
    public double getX(){return x;}
    public static int getCount(){return count;}
    public static void setCount(int i){count=i;}
    public double getU(){return u;}
    public int getK(){return k;}
    public void setK(int i){k=i;}
    public double getMeanPOmega(){return mean_p_omega;}
    public void setMeanPOmega(double d){mean_p_omega=d;}
    public int getSelRank(){return sel_rank;}
    public void setSelRank(int i){sel_rank=i;}
    public static void setNU(boolean b){
        NU=b;
    }
}
