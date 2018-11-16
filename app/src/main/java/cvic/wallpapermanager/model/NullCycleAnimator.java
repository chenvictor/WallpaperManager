package cvic.wallpapermanager.model;

/**
 * A default 'null' animator fallback.
 *      Provides no animation, transitioning
 *     immediately to the second image.
 */
public class NullCycleAnimator extends CycleAnimator {

    @Override
    protected void init() {

    }

    @Override
    protected boolean animate() {
        return false;
    }

}
