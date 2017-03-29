package com.jukuproject.jukutweet.Adapters;



import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
import com.jukuproject.jukutweet.Models.MenuHeader;
import com.jukuproject.jukutweet.R;

import java.util.ArrayList;

/**
 * Created by JukuProject on 3/23/2017
 * List adapter with group entries and sub lists of child entries that expand
 * when list is clicked
 */
public class MyListExpandableAdapter extends BaseExpandableListAdapter {
    private String TAG = "Menu_Ex_ListAdapter";
    private Context mContext;
    private ArrayList<MenuHeader> mMenuHeader;
    private int mTextsize;

    /* Maximum width that the color blocks can occupy on the screen(in total) */
    private final int mMaxWidthForColorBlocks;


    public MyListExpandableAdapter(Context context
            ,ArrayList<MenuHeader> menuHeader
            ,int maxWidthForColorBlocks
            ,int fontsize
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.expandablelistadapter_listitem, parent, false);
        }
        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.layoutcontainer);

        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        txtListChild.setText(childText);

        ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.favorite_icon);
        imageButton.setFocusable(false);
        imageButton.setClickable(false);

        TextView textViewColorBlock_grey = (TextView) convertView.findViewById(R.id.listitem_colors_1);
        TextView textViewColorBlock_red = (TextView) convertView.findViewById(R.id.listitem_colors_2);
        TextView textViewColorBlock_yellow = (TextView) convertView.findViewById(R.id.listitem_colors_3);
        TextView textViewColorBlock_green = (TextView) convertView.findViewById(R.id.listitem_colors_4);

        TextView lblColorBar = (TextView) convertView.findViewById(R.id.lblcolorbar);
        lblColorBar.setVisibility(TextView.GONE);

        if(BuildConfig.DEBUG) {
            Log.d(TAG, "childText: " + childText);
        }

        /* For the child entries of the adapter, only show the "colorblocks" in the first row of a mylist. This is differentiated
        * here by the string "Browse/Edit" which only appears in the "MyListFragment" fragment */
        if(mMenuHeader.get(groupPosition).getColorBlockMeasurables() != null && childText.equalsIgnoreCase(mContext.getString(R.string.menuchildbrowse))) {

            setColorBlocks(mMenuHeader.get(groupPosition).getColorBlockMeasurables()
                    ,availableWidth
                    ,textViewColorBlock_grey
                    ,textViewColorBlock_red
                    ,textViewColorBlock_yellow
                    ,textViewColorBlock_green);

        } else {
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

        int availableWidth = mMaxWidthForColorBlocks;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandablelistadapter_groupitem, null);
        }



        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setAlpha(1.0f);
        if(BuildConfig.DEBUG){Log.d(TAG,"headerTitle: --" + mMenuHeader.get(groupPosition).getHeaderTitle() + "--" );}
        if(mTextsize >0){
            lblListHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextsize);
        }

//        LinearLayout background = (LinearLayout) convertView.findViewById(R.id.balls2);
        lblListHeader.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
        lblListHeader.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
//        background.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
        lblListHeader.setGravity(Gravity.START);

        TextView lblColorBar = (TextView) convertView.findViewById(R.id.lblcolorbar);
        lblColorBar.setGravity(Gravity.CENTER);

        /* If the adapter is handling "color bars" -- used in the "quiz by color" fragment where the user can quiz themselves on
        * kanji grouped by COLOR (grey, red, yellow, green) -- the entire row should be taken up by one big color block
        * drawable, with the count of entries for that color in the middle. No title, no label. */


            //Hide the big color bar and color blocks
            ((TextView) convertView.findViewById(R.id.lblcolorbar)).setVisibility(TextView.GONE);
        ((TextView) convertView.findViewById(R.id.listitem_colors_1)).setVisibility(TextView.GONE);
        ((TextView) convertView.findViewById(R.id.listitem_colors_2)).setVisibility(TextView.GONE);
        ((TextView) convertView.findViewById(R.id.listitem_colors_3)).setVisibility(TextView.GONE);
        ((TextView) convertView.findViewById(R.id.listitem_colors_4)).setVisibility(TextView.GONE);

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
                    lblListHeader.setText(mMenuHeader.get(groupPosition).getHeaderTitle());

                    final TextView lblListHeaderCount = (TextView) convertView.findViewById(R.id.lblListHeaderCount);

                    if(mMenuHeader.get(groupPosition).isShowLblHeaderCount()) {
                        lblListHeaderCount.setVisibility(TextView.VISIBLE); /**SET TO VISIBLE TO SEE*/
                    } else {
                        lblListHeaderCount.setVisibility(TextView.GONE);
                    }

                    /* Set the list name to be greyed out for empty lists */
                    if(measurables.getTotalCount()>0) {
                        if(BuildConfig.DEBUG){Log.d(TAG,"setting counts...");}
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


        if(BuildConfig.DEBUG){Log.d(TAG,"dimenscore at end of group: " + availableWidth);}
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



    public void setColorBlocks(ColorBlockMeasurables colorBlockMeasurables
            ,int availableWidth
            , TextView txtGrey
            , TextView txtRed
            , TextView txtYellow
            , TextView txtGreen) {

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

        txtRed.setText(String.valueOf(colorBlockMeasurables.getRedCount()));
        txtYellow.setText(String.valueOf(colorBlockMeasurables.getYellowCount()));
        txtGreen.setText(String.valueOf(colorBlockMeasurables.getGreenCount()));

        if (colorBlockMeasurables.getRedCount()
                + colorBlockMeasurables.getYellowCount()
                + colorBlockMeasurables.getGreenCount() == 0) {
            txtGrey.setText(String.valueOf(colorBlockMeasurables.getTotalCount()));
            txtGrey.setVisibility(View.VISIBLE);
            txtGrey.setMinimumWidth(availableWidth);

        } else {

            int availableWidthRemaining = availableWidth;
            if(colorBlockMeasurables.getGreyCount()>0){
                txtGrey.setText(String.valueOf(colorBlockMeasurables.getGreyCount()));
                int dimenscore = colorBlockMeasurables.getGreyDimenscore(availableWidth);
                if(BuildConfig.DEBUG) {
                    Log.i(TAG,"dimenscoretotal: " + availableWidth);
                    Log.i(TAG,"grey/count: " + colorBlockMeasurables.getGreyCount() + "/" + colorBlockMeasurables.getTotalCount());
                    Log.i(TAG,"((float) grey / (float) count): " + ((float) colorBlockMeasurables.getGreyCount() / (float) colorBlockMeasurables.getTotalCount()));
                    Log.i(TAG,"Rounded score: " + dimenscore);
                }
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
                if(BuildConfig.DEBUG) {
                    Log.i(TAG,"dimenscoretotal: " + availableWidth);
                    Log.i(TAG,"grey/count: " + colorBlockMeasurables.getGreenCount() + "/" + colorBlockMeasurables.getTotalCount());
                    Log.i(TAG,"((float) grey / (float) count): " + ((float) colorBlockMeasurables.getGreenCount() / (float) colorBlockMeasurables.getTotalCount()));
                    Log.i(TAG,"Rounded score: " + dimenscore);

                }
                txtGreen.setMinimumWidth(dimenscore);
                txtGreen.setVisibility(View.VISIBLE);
            }
        }

        if (colorBlockMeasurables.getTotalCount() == 0) {
            txtGrey.setVisibility(View.VISIBLE);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                drawablecolorblock1.setColorFilter(ContextCompat.getColor(mContext, android.R.color.white), PorterDuff.Mode.MULTIPLY);
                txtGrey.setBackground(drawablecolorblock1);
            } else {
                drawablecolorblock1.setColorFilter(ContextCompat.getColor(mContext, android.R.color.white), PorterDuff.Mode.MULTIPLY);
                txtGrey.setBackgroundDrawable(drawablecolorblock1);
            }
            txtGrey.setText(mContext.getString(R.string.empty));
            txtRed.setVisibility(View.GONE);
            txtYellow.setVisibility(View.GONE);
            txtGreen.setVisibility(View.GONE);
        }

    }
}
