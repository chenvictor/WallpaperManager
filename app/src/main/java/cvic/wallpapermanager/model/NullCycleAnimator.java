package cvic.wallpapermanager.model;

public class NullCycleAnimator extends CycleAnimator {

    @Override
    protected void init() {

    }

    @Override
    protected boolean animate() {
        return false;
    }

}
