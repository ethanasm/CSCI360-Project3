import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.util.Vector;

import jdk.vm.ci.code.site.Mark;

public class project3cs360s2020 {
    private class Action {
        private final int action;

        public Action(String action) {
            if (action.contentEquals("NORTH")) {
                this.action = 0;
            } else if (action.contentEquals("EAST")) {
                this.action = 1;
            } else if (action.contentEquals("SOUTH")) {
                this.action = 2;
            } else if (action.contentEquals("WEST")) {
                this.action = 3;
            } else if (action.contentEquals("STAY")) {
                this.action = 4;
            } else {
                this.action = -1;
            }
        }

        public int getAction() { return this.action; }

    }

    private class State {
        private int index;
        private int row;
        private int col;
        private boolean destination;
        private boolean obstacle;
        private double utility;
        private double reward;
        private Action bestAction;

        public State(int index, int row, int col, double utility, double reward) {
            this.index = index;
            this.row = row;
            this.col = col;
            this.utility = utility;
            this.reward = reward;
            this.destination = false;
            this.obstacle = false;
            this.bestAction = null;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public void setObstacle() {
            obstacle = true;
        }

        public boolean isObstacle() {
            return obstacle;
        }

        public void setDestination() {
            destination = true;
        }

        public boolean isDestination() {
            return destination;
        }

        public double getReward() {
            return reward;
        }

        public void setUtility(double utility) {
            this.utility = utility;
        }

        public double getUtility() {
            return utility;
        }

        public void setBestAction(Action action) {
            this.bestAction = action;
        }

        public Action getBestAction() {
            return bestAction;
        }

    }


    private class Transition {
        private double probability;
        private State newState;

        public Transition(double probability, State state) {
            probability = probability;
            newState = state;
        }

        public double getProbability() {
            return probability;
        }

        public State getNewState() {
            return newState;
        }
    }

    private class Project3FileReader {
        BufferedReader reader;

        public Project3FileReader(String filename) {
            try {
                this.reader = new BufferedReader(new FileReader(filename));
            } catch (FileNotFoundException fnfe) {
                System.out.println("Input file not found");
                System.out.println(fnfe.getStackTrace());
            }
        }

        public MarkovDecisionProcess parseFile() {
            if (reader == null) {
                return null;
            }
            int gridSize = Integer.parseInt(reader.readLine());
            MarkovDecisionProcess mdp = new MarkovDecisionProcess(gridSize);

            int numObstacles = Integer.parseInt(reader.readLine());
            for (int i = 0; i < numObstacles; i++) {
                String line = reader.readLine();
                String[] obstCoord = line.split(",");
                mdp.addObstacle(Integer.parseInt(obstCoord[1]), Integer.parseInt(obstCoord[0]));
            }

            String line = reader.readLine();
            String[] destCoord = line.split(",");
            mdp.setDestination(Integer.parseInt(destCoord[1]), Integer.parseInt(destCoord[0]));

            reader.close();
            
            return mdp;
        }
    }

    private class Project3FileWriter {
        BufferedWriter writer;

        public Project3FileWriter(String filename) {
            this.writer = new BufferedWriter(new FileWriter(filename));
        }

        public void writeFile(String res) {
            if (writer != null) {
                writer.write(res);
                writer.close();
            }
        }
    }

    private class MarkovDecisionProcess {
        private Vector<State> states;
        private Vector<Action> actions;
        private int gridSize;
        private int moveReward;
        private int destReward;
        private int obstReward;
        private double epsilon;
        private double gamma;

        private final int ACTION_NORTH = 0;
        private final int ACTION_EAST = 1;
        private final int ACTION_SOUTH = 2;
        private final int ACTION_WEST = 3;
        private final int ACTION_STAY = 4;

        private final double PROB_INTENDED = 0.7;
        private final double PROB_ELSE = 0.1;

        public MarkovDecisionProcess(int gridSize) {
            states = new Vector();
            State s;
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    states.add(new State(j + (i * gridSize), i, j, 0.0, 0.0));
                }
            }
            actions = new Vector();
            actions.add(new Action("NORTH"));
            actions.add(new Action("EAST"));
            actions.add(new Action("SOUTH"));
            actions.add(new Action("WEST"));
        }

        public void addObstacle(int row, int col) {
            State s = states.get(col + (row * gridSize));
            s.setObstacle();
        }

        public void setDestination(int row, int col) {
            State s = states.get(col + (row * gridSize));
            s.setDestination();
        }

        public void setEpsilon(double epsilon) {
            this.epsilon = epsilon;
        }

        public void setGamma(double gamma) {
            this.gamma = gamma;
        }

        public void setMoveReward(int reward) {
            this.moveReward = reward;
        }

        public void setDestReward(int reward) {
            this.destReward = reward;
        }

        public void setObstReward(int reward) {
            this.obstReward = reward;
        }

        public void setRewards() {
            for (State state : states) {
                if (state.isDestination()) {
                    state.setReward(destReward + moveReward);
                } else if (state.isObstacle()) {
                    state.setReward(obstReward + moveReward);
                } else {
                    state.setReward(moveReward);
                }
            }
        }

        public State getNewState(State state, Action action) {
            int newRow = state.getRow();
            int newCol = state.getCol();

            if (action.getAction() == ACTION_STAY) {
                return state;
            }

            switch (action.getAction()) {
                case ACTION_NORTH:
                    newRow--;
                    if (newRow < 0) {
                        newRow = 0;
                    }
                    break;
                case ACTION_EAST:
                    newCol++;
                    if (newCol >= gridSize) {
                        newCol = gridSize - 1;
                    }
                    break;
                case ACTION_SOUTH:
                    newRow++;
                    if (newRow >= gridSize) {
                        newRow = gridSize - 1;
                    }
                    break;
                case ACTION_WEST:
                    newCol--;
                    if (newCol < 0) {
                        newCol = 0;
                    }
                    break;
            }
            return states.get(newCol + (newRow * gridSize));
        }

        public Vector<Transition> getTransitions(State state, Action action) {
            Vector<Transition> transitions = new Vector();
            if (state.isDestination()) {
                return transitions;
            }

            for (int i = 0; i < actions.size() + 1; i++) {
                double prob;
                if (action.getAction() == actions.get(i).getAction()) {
                    prob = PROB_INTENDED;
                } else {
                    prob = PROB_ELSE;
                }
                transitions.add(new Transition(prob, getNewState(state, actions.get(i))));
            }
            return transitions;
        }

        public String policyToString() {
            String policy = "";
            for (State state : states) {
                if (state.isDestination()) {
                    policy += ".";
                } else if (state.isObstacle()) {
                    policy += "o";
                } else if (state.getBestAction().getAction()
                            == ACTION_NORTH) {
                    policy += "^";
                } else if (state.getBestAction().getAction()
                            == ACTION_EAST) {
                    policy += ">";
                } else if (state.getBestAction().getAction()
                            == ACTION_WEST) {
                    policy += "<";
                } else if (state.getBestAction().getAction()
                            == ACTION_SOUTH) {
                    policy += "v";
                }

                if (state.getCol() == gridSize - 1) {
                    policy += "\n";
                }
            }
            return policy;
        }


        public void solve() {
            double delta;
            if (gamma == 1) {
                delta = epsilon;
            } else {
                delta = epsilon * (1 - gamma) / gamma;
            }
            boolean converged = false;

            while (!converged) {
                double maxError = -0.1;
                for (State state : states) {
                    double maxUtility = -Double.MAX_VALUE;
                    Action maxAction = null;

                    double utility = state.getUtility();
                    double reward = state.getReward();

                    Vector<Transition> transitions;
                    for (Action action : actions) {
                        transitions = getTransitions(state, action);
                        double nextUtility = 0;
                        for (Transition transition: transitions) {
                            State newState = transition.getNewState();
                            nextUtility += (transition.getProbability() * newState.getUtility());
                        }
                        if (nextUtility > maxUtility) {
                            maxUtility = nextUtility;
                            maxAction = action;
                        }
                    }
                    maxUtility = reward + gamma * maxUtility;
                    state.setUtility(maxUtility);
                    state.setBestAction(maxAction);
                    double curError = Math.abs(maxUtility - utility);
                    if (curError > maxError) {
                        maxError = curError;
                    }
                }
                if (delta > maxError) {
                    converged = true;
                }
            }
        }
    }

    public static void main() {
        Project3FileReader fileReader = new Project3FileReader("input.txt");
        MarkovDecisionProcess mdp = fileReader.parseFile();
        mdp.setEpsilon(0.01);
        mdp.setGamma(0.9);
        mdp.setDestReward(100);
        mdp.setObstReward(-100);
        mdp.setMoveReward(-1);
        mdp.setRewards();
        mdp.solve();
        Project3FileWriter fileWriter = new Project3FileWriter("output.txt");
        fileWriter.writeFile(mdp.policyToString());
    }




}