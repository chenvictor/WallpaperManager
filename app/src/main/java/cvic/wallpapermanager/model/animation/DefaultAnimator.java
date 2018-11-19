package cvic.wallpapermanager.model.animation;

/**
 * A default animator fallback.
 *      Provides no animation, transitioning
 *     immediately to the second image.
 */
public class DefaultAnimator extends TransitionAnimator {

    @Override
    protected void init() {

    }

    @Override
    protected boolean animate() {
        return false;
    }

}
