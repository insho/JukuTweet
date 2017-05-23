package com.jukuproject.jukutweet.Adapters;


import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.MenuHeader;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Adapter for TweetList {@link com.jukuproject.jukutweet.Fragments.TweetListFragment}, with a parent group for each
 * Tweet list, and expandable child entries for the quiz options. The first row of each
 * child group has the title "Browse/Edit", as well as a colorblock set breaking down the words contained in the list by color.
 *
 * @see com.jukuproject.jukutweet.Fragments.TweetListFragment
 * @see com.jukuproject.jukutweet.Fragments.TweetListSingleUserFragment
 */
public class TweetListExpandableAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private ArrayList<MenuHeader> mMenuHeader;
    private int mTextsize;

    /* Maximum width that the color blocks can occupy on the screen(in total) */
    private final int mMaxWidthForColorBlocks;


    public TweetListExpandableAdapter(Context context
            , ArrayList<MenuHeader> menuHeader
            , int maxWidthForColorBlocks
            , int fontsize
    ) {
        this.mContext = context;
        this.mMenuHeader = menuHeader;
        this.mMaxWidthForColorBlocks = maxWidthForColorBlocks;
        this.mTextsize = fontsize;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {

        return mMenuHeader.get(groupPosition).getChildOptions().get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        int availableWidth = mMaxWidthForColorBlocks;
        final String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.expandablelistadapter_listitem_tweet, parent, false);
        }
        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.layoutcontainer);

        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        txtListChild.setText(childText);

        ImageButton tweetIcon = (ImageButton) convertView.findViewById(R.id.tweet_icon);
        TextView tweetIconText = (TextView) convertView.findViewById(R.id.tweet_title);
        tweetIcon.setFocusable(false);
        tweetIcon.setClickable(false);

        tweetIconText.setFocusable(false);
        tweetIconText.setClickable(false);

        TextView textViewColorBlock_empty = (TextView) convertView.findViewById(R.id.listitem_colors_0);
        TextView textViewColorBlock_grey = (TextView) convertView.findViewById(R.id.listitem_colors_1);
        TextView textViewColorBlock_red = (TextView) convertView.findViewById(R.id.listitem_colors_2);
        TextView textViewColorBlock_yellow = (TextView) convertView.findViewById(R.id.listitem_colors_3);
        TextView textViewColorBlock_green = (TextView) convertView.findViewById(R.id.listitem_colors_4);

        /* For the child entries of the adapter, only show the "colorblocks" in the first row of a mylist. This is differentiated
        * here by the string "Browse/Edit" which only appears in the "WordListFragment" fragment */
        if(mMenuHeader.get(groupPosition).getColorBlockMeasurables() != null && childText.equalsIgnoreCase(mContext.getString(R.string.menuchildbrowse))) {

            tweetIcon.setVisibility(View.VISIBLE);
            tweetIconText.setVisibility(View.VISIBLE);

            /* Add the count of saved tweets contained within the list */
            try {
                tweetIconText.setText(mContext.getString(R.string.tweetcount,mMenuHeader.get(groupPosition).getColorBlockMeasurables().getTweetCount()));
            } catch (Exception e) {
                tweetIcon.setVisibility(View.GONE);
                tweetIconText.setVisibility(View.GONE);
            }

            setColorBlocks(mMenuHeader.get(groupPosition).getColorBlockMeasurables()
                    ,availableWidth
                    ,textViewColorBlock_grey
                    ,textViewColorBlock_red
                    ,textViewColorBlock_yellow
                    ,textViewColorBlock_green
                    ,textViewColorBlock_empty);


        } else {
            tweetIcon.setVisibility(View.GONE);
            tweetIconText.setVisibility(View.GONE);
            textViewColorBlock_grey.setVisibility(View.GONE);
            textViewColorBlock_red.setVisibility(View.GONE);
            textViewColorBlock_yellow.setVisibility(View.GONE);
            textViewColorBlock_green.setVisibility(View.GONE);
        }


        return layout;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mMenuHeader.get(groupPosition).getChildOptions().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mMenuHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mMenuHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandablelistadapter_groupitem, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setAlpha(1.0f);

        if(mTextsize >0){
            lblListHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextsize);
        }

        lblListHeader.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
        lblListHeader.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
        lblListHeader.setGravity(Gravity.START);

        TextView lblColorBar = (TextView) convertView.findViewById(R.id.lblcolorbar);
        lblColorBar.setGravity(Gravity.CENTER);

        /* If the adapter is handling "color bars" -- used in the "quiz by color" fragment where the user can quiz themselves on
        * kanji grouped by COLOR (grey, red, yellow, green) -- the entire row should be taken up by one big color block
        * drawable, with the count of entries for that color in the middle. No title, no label. */

        //Hide the big color bar and color blocks
        convertView.findViewById(R.id.lblcolorbar).setVisibility(TextView.GONE);
        convertView.findViewById(R.id.listitem_colors_1).setVisibility(TextView.GONE);
        convertView.findViewById(R.id.listitem_colors_2).setVisibility(TextView.GONE);
        convertView.findViewById(R.id.listitem_colors_3).setVisibility(TextView.GONE);
        convertView.findViewById(R.id.listitem_colors_4).setVisibility(TextView.GONE);

            /* Handle MyList rows -- either systemlists (with colored favorites stars visible), or
             * user-created lists with no colored star  */

        ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.favorite_icon);
        imageButton.setFocusable(false);
        imageButton.setClickable(false);

        ColorBlockMeasurables measurables = mMenuHeader.get(groupPosition).getColorBlockMeasurables();
                /* Handle System lists*/
        if (mMenuHeader.get(groupPosition).isSystemList()) {

            imageButton.setImageResource(R.drawable.ic_star_black);
            imageButton.setVisibility(ImageButton.VISIBLE);
            lblListHeader.setText(mContext.getString(R.string.tweetadapter_favoritetweets));
            final TextView lblListHeaderCount = (TextView) convertView.findViewById(R.id.lblListHeaderCount);

            if(mMenuHeader.get(groupPosition).isShowLblHeaderCount()) {
                lblListHeaderCount.setVisibility(TextView.VISIBLE); /**SET TO VISIBLE TO SEE*/
            } else {
                lblListHeaderCount.setVisibility(TextView.GONE);
            }

            /* Set the list name to be greyed out for empty lists */
            if(measurables.getTweetCount()>0) {
                lblListHeaderCount.setText("(" + measurables.getTotalCount() + ")");
                lblListHeader.setAlpha(1.0f);
                lblListHeaderCount.setAlpha(.8f);
            } else {
                lblListHeaderCount.setText("");
                lblListHeader.setAlpha(.5f);
                lblListHeaderCount.setAlpha(.5f);
            }

            //Set star color
            if(mMenuHeader.get(groupPosition).getStarColor() != null) {
                imageButton.setColorFilter(ContextCompat.getColor(mContext,mMenuHeader.get(groupPosition).getStarColor()));
            }

        } else {
            /* It is a user-created list, so do not show the colored star */
            imageButton.setVisibility(ImageButton.GONE);
            lblListHeader.setText(mMenuHeader.get(groupPosition).getHeaderTitle());
            final TextView lblListHeaderCount = (TextView) convertView.findViewById(R.id.lblListHeaderCount);

            if(mMenuHeader.get(groupPosition).isShowLblHeaderCount()) {
                lblListHeaderCount.setVisibility(TextView.VISIBLE);
            } else {
                lblListHeaderCount.setVisibility(TextView.GONE);
            }

            float alpha = .5f;
            lblListHeaderCount.setAlpha(alpha);

            if( measurables.getTotalCount() == 0) {
                lblListHeaderCount.setText("");
                lblListHeader.setAlpha(.5f);
                lblListHeaderCount.setAlpha(.5f);
            } else {
                lblListHeaderCount.setText(mContext.getString(R.string.mylistcount,measurables.getTotalCount()));
                lblListHeader.setAlpha(1.0f);
                lblListHeaderCount.setAlpha(.7f);
            }

        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        return true;
    }



    private void setColorBlocks(ColorBlockMeasurables colorBlockMeasurables
            ,int availableWidth
            , TextView txtGrey
            , TextView txtRed
            , TextView txtYellow
            , TextView txtGreen
            , TextView txtEmpty) {

        Drawable drawablecolorblock1 = ContextCompat.getDrawable(mContext, R.drawable.colorblock);
        Drawable drawablecolorblock2 = ContextCompat.getDrawable(mContext, R.drawable.colorblock);
        Drawable drawablecolorblock3 = ContextCompat.getDrawable(mContext, R.drawable.colorblock);
        Drawable drawablecolorblock4 = ContextCompat.getDrawable(mContext, R.drawable.colorblock);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            drawablecolorblock1.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
            txtGrey.setBackground(drawablecolorblock1);
            drawablecolorblock2.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
            txtRed.setBackground(drawablecolorblock2);
            drawablecolorblock3.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
            txtYellow.setBackground(drawablecolorblock3);
            drawablecolorblock4.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
            txtGreen.setBackground(drawablecolorblock4);

        } else {
            drawablecolorblock1.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
            txtGrey.setBackgroundDrawable(drawablecolorblock1);
            drawablecolorblock2.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
            txtRed.setBackgroundDrawable(drawablecolorblock2);
            drawablecolorblock3.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
            txtYellow.setBackgroundDrawable(drawablecolorblock3);
            drawablecolorblock4.setColorFilter(ContextCompat.getColor(mContext, R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
            txtGreen.setBackgroundDrawable(drawablecolorblock4);
        }

        txtGrey.setVisibility(View.GONE);
        txtRed.setVisibility(View.GONE);
        txtYellow.setVisibility(View.GONE);
        txtGreen.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);

        txtRed.setText(String.valueOf(colorBlockMeasurables.getRedCount()));
        txtYellow.setText(String.valueOf(colorBlockMeasurables.getYellowCount()));
        txtGreen.setText(String.valueOf(colorBlockMeasurables.getGreenCount()));
        txtEmpty.setText(String.valueOf(colorBlockMeasurables.getEmptyCount()));

        if (colorBlockMeasurables.getRedCount()
                + colorBlockMeasurables.getYellowCount()
                + colorBlockMeasurables.getGreenCount()
                + colorBlockMeasurables.getTotalCount()
                + colorBlockMeasurables.getEmptyCount() == 0) {
            txtGrey.setText(String.valueOf(colorBlockMeasurables.getTotalCount()));
            txtGrey.setVisibility(View.VISIBLE);
            txtGrey.setMinimumWidth(availableWidth);

        } else {

            int availableWidthRemaining = availableWidth;
            if(colorBlockMeasurables.getGreyCount()>0){
                txtGrey.setText(String.valueOf(colorBlockMeasurables.getGreyCount()));
                int dimenscore = colorBlockMeasurables.getGreyDimenscore(availableWidth);
                availableWidthRemaining = availableWidth-dimenscore;
                txtGrey.setMinimumWidth(dimenscore);
                txtGrey.setVisibility(View.VISIBLE);
            }

            if(colorBlockMeasurables.getRedCount()>0){
                txtRed.setText(String.valueOf(colorBlockMeasurables.getRedCount()));
                int dimenscore = colorBlockMeasurables.getRedDimenscore(availableWidth,availableWidthRemaining);

                availableWidthRemaining = availableWidthRemaining -dimenscore;
                txtRed.setMinimumWidth(dimenscore);
                txtRed.setVisibility(View.VISIBLE);

            }

            if(colorBlockMeasurables.getYellowCount()>0){
                txtYellow.setText(String.valueOf(colorBlockMeasurables.getYellowCount()));
                int dimenscore = colorBlockMeasurables.getYellowDimenscore(availableWidth,availableWidthRemaining);
                availableWidthRemaining = availableWidthRemaining -dimenscore;
                txtYellow.setMinimumWidth(dimenscore);
                txtYellow.setVisibility(View.VISIBLE);

            }

            if(colorBlockMeasurables.getGreenCount()>0){
                txtGreen.setText(String.valueOf(colorBlockMeasurables.getGreenCount()));
                int dimenscore = colorBlockMeasurables.getGreenDimenscore(availableWidth,availableWidthRemaining);
                availableWidthRemaining = availableWidthRemaining -dimenscore;
                txtGreen.setMinimumWidth(dimenscore);
                txtGreen.setVisibility(View.VISIBLE);
            }

            if(colorBlockMeasurables.getEmptyCount()>0){
                txtEmpty.setText(String.valueOf(colorBlockMeasurables.getEmptyCount()));
                int dimenscore = colorBlockMeasurables.getEmptyDimenscore(availableWidth,availableWidthRemaining);
                txtEmpty.setMinimumWidth(dimenscore);
                txtEmpty.setVisibility(View.VISIBLE);

            }

        }

        /* If the list is entirely empty, and yet is open, show the
        * colorblock with a white background and "empty" as text inside.
        *
        * However, if it is a tweet list with some tweets in it that haven't yet been parsed
        * the white background will say "..." instead, enticing the user to browse the list and parse
        * some tweets */
        if (colorBlockMeasurables.getTotalCount() == 0) {
            txtGrey.setVisibility(View.VISIBLE);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                drawablecolorblock1.setColorFilter(ContextCompat.getColor(mContext, android.R.color.white), PorterDuff.Mode.MULTIPLY);
                txtGrey.setBackground(drawablecolorblock1);
            } else {
                drawablecolorblock1.setColorFilter(ContextCompat.getColor(mContext, android.R.color.white), PorterDuff.Mode.MULTIPLY);
                txtGrey.setBackgroundDrawable(drawablecolorblock1);
            }
            if(colorBlockMeasurables.getTweetCount()>0) {
                txtGrey.setText(mContext.getString(R.string.dotdotdot));
            } else {
                txtGrey.setText(mContext.getString(R.string.empty));
            }
            txtRed.setVisibility(View.GONE);
            txtYellow.setVisibility(View.GONE);
            txtGreen.setVisibility(View.GONE);
            txtEmpty.setVisibility(View.GONE);
        }

    }
}
