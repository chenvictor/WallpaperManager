package cvic.wallpapermanager.ui.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import cvic.wallpapermanager.R;

public class CustomCheckBox extends android.support.v7.widget.AppCompatCheckBox {

    public static final int UNCHECKED = 0;
    public static final int PLUS      = 1;
    public static final int MINUS     = 2;
    private int state = UNCHECKED;

    private OnStateChangeListener listener;

    public CustomCheckBox(Context context) {
        super(context);
    }

    public CustomCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public final void toggle() {
        super.toggle();
        state = (state + 1)%3;
    }

    @Override
    public final boolean isChecked() {
        throw new UnsupportedOperationException("getState should be used instead!");
    }

    public int getState() {
        return state;
    }

    @Override
    public final void setChecked(boolean checked) {
        throw new UnsupportedOperationException("setState should be used instead!");
    }

    public void setState(int state) {
        if (state < 0 || state > 2) {
            throw new IllegalArgumentException("State must be one of UNCHECKED, PLUS, or MINUS");
        }
        this.state = state;
        updateDrawable();
        listener.onStateChanged(this, state);
    }

    @Override
    public final void setOnCheckedChangeListener(@Nullable CompoundButton.OnCheckedChangeListener listener) {
        throw new UnsupportedOperationException("setOnStateChangeListener should be used instead!");
    }

    public void setOnStateChangeListener(@Nullable OnStateChangeListener listener) {
        this.listener = listener;
    }

    private void updateDrawable() {
        int drawable = R.drawable.checkbox_blank;
        switch(state) {
            case PLUS:
                drawable = R.drawable.checkbox_plus;
                break;
            case MINUS:
                drawable = R.drawable.checkbox_minus;
                break;
        }
        setButtonDrawable(drawable);
    }

    public interface OnStateChangeListener {
        void onStateChanged(CustomCheckBox checkBox, int state);
    }
}
