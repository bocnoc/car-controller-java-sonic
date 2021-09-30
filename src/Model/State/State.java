package Model.State;

import Model.CarModel;

public abstract class State {
    public abstract void doAction(final CarModel model);
}
