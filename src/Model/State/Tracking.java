package Model.State;

import Model.CarModel;

public class Tracking extends State {
    private final static State state = new Tracking();

    public static State getInstance() {
        return state;
    }

    @Override
    public void doAction(CarModel model) {
        System.out.println(this);
        model.setState(Halt.getInstance());
    }

    @Override
    public String toString() {
        return "TRACKING";
    }
}
