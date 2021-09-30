package Model.State;

import Model.CarModel;

public class Init extends State {
    private final static State state = new Init();

    public static State getInstance() {
        return state;
    }

    @Override
    public void doAction(CarModel model) {
        System.out.println(this);
        model.setState(Run.getInstance());
    }

    @Override
    public String toString() {
        return "INIT";
    }
}
