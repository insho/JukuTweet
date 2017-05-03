package com.jukuproject.jukutweet;

/**
 * Created by JClassic on 4/23/2017.
 */

import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Clickable span function used in {@link LongClickLinkMovementMethod}
 */
public abstract class LongClickableSpan extends ClickableSpan {

    abstract public void onLongClick(View view);

}