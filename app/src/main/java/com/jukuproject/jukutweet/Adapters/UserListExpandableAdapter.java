package com.jukuproject.jukutweet.Adapters;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MenuHeader;
import com.jukuproject.jukutweet.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Adapter for saved Users in the {@link com.jukuproject.jukutweet.Fragments.UserListFragment}, with a parent group for each
 * User list, and two expandable child entries: "Show Timeline" and "Saved Tweets" (and saved tweets has colorblocks showing saved tweets).
 */
public class UserListExpandableAdapter extends BaseExpandableListAdapter {
    private String TAG = "Menu_Ex_ListAdapter";
    private Context mContext;
    private ArrayList<MenuHeader> mMenuHeader;
    private RxBus _rxbus;

    /* Maximum width that the color blocks can occupy on the screen(in total) */
//    private final int mMaxWidthForColorBlocks;


    public UserListExpandableAdapter(Context context
            , ArrayList<MenuHeader> menuHeader
//            , int maxWidthForColorBlocks
            , RxBus rxBus
    ) {
        this.mContext = context;
        this.mMenuHeader = menuHeader;
//        this.mMaxWidthForColorBlocks = maxWidthForColorBlocks;
        this._rxbus = rxBus;
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

//        int availableWidth = mMaxWidthForColorBlocks;
        final String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.expandablelistadapter_listitem_userlistchild, parent, false);
        }
        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.layoutcontainer);

        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        txtListChild.setText(childText);

        ImageButton tweetIcon = (ImageButton) convertView.findViewById(R.id.tweet_icon);
        TextView tweetIconText = (TextView) convertView.findViewById(R.id.tweet_title);
//        tweetIcon.setFocusable(false);
//        tweetIcon.setClickable(false);
//
//        tweetIconText.setFocusable(false);
//        tweetIconText.setClickable(false);

//        TextView textViewColorBlock_empty = (TextView) convertView.findViewById(R.id.listitem_colors_0);
//        TextView textViewColorBlock_grey = (TextView) convertView.findViewById(R.id.listitem_colors_1);
//        TextView textViewColorBlock_red = (TextView) convertView.findViewById(R.id.listitem_colors_2);
//        TextView textViewColorBlock_yellow = (TextView) convertView.findViewById(R.id.listitem_colors_3);
//        TextView textViewColorBlock_green = (TextView) convertView.findViewById(R.id.listitem_colors_4);

        /* For the child entries of the adapter, only show the "colorblocks" in the first row of a mylist. This is differentiated
        * here by the string "Browse/Edit" which only appears in the "WordListFragment" fragment */
        if(mMenuHeader.get(groupPosition).getColorBlockMeasurables() != null && childText.equalsIgnoreCase(mContext.getString(R.string.menuchildviewsavedtweets))) {

            tweetIcon.setVisibility(View.VISIBLE);
            tweetIconText.setVisibility(View.VISIBLE);

            /* Add the count of saved tweets contained within the list */
            try {
                tweetIconText.setText("x"+String.valueOf(mMenuHeader.get(groupPosition).getColorBlockMeasurables().getTweetCount()));
            } catch (Exception e) {
                tweetIcon.setVisibility(View.GONE);
                tweetIconText.setVisibility(View.GONE);
            }

//            setColorBlocks(mMenuHeader.get(groupPosition).getColorBlockMeasurables()
//                    ,availableWidth
//                    ,textViewColorBlock_grey
//                    ,textViewColorBlock_red
//                    ,textViewColorBlock_yellow
//                    ,textViewColorBlock_green
//                    ,textViewColorBlock_empty);


        } else {
            tweetIcon.setVisibility(View.GONE);
            tweetIconText.setVisibility(View.GONE);
//            textViewColorBlock_grey.setVisibility(View.GONE);
//            textViewColorBlock_red.setVisibility(View.GONE);
//            textViewColorBlock_yellow.setVisibility(View.GONE);
//            textViewColorBlock_green.setVisibility(View.GONE);
        }

//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                /*If it's a short click, send the UserInfo object back to UserInfoFragment,
//                * to trigger opening the UserTimeLine Fragment */
//                _rxbus.send(mMenuHeader.get(groupPosition));
//            }
//        });
//
//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                /*If it's a long click, send the UserInfo object back to UserInfoFragment,
//                * to trigger opening the RemoveUser dialog */
//                _rxbus.send(childText);
//            }
//        });

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
    public View getGroupView(final int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

//        int availableWidth = mMaxWidthForColorBlocks;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.userlist_recycler_row, null);
        }

        TextView txtUserName = (TextView) convertView.findViewById(R.id.name);
        final ImageView imageIcon = (ImageView) convertView.findViewById(R.id.image);
        TextView txtUserDescription = (TextView) convertView.findViewById(R.id.description);
        TextView txtUserScreenName = (TextView) convertView.findViewById(R.id.screenName);

        /* If the user only contains the username, and everything else is null, it is because the device
        * was offline while saving the user, and this row should be greyed out, and only display the username... */
        if(mMenuHeader.get(groupPosition).getUserInfo() != null
                && mMenuHeader.get(groupPosition).getUserInfo().getUserId() == null) {
            txtUserName.setText(mMenuHeader.get(groupPosition).getUserInfo().getDisplayScreenName());
            txtUserName.setAlpha(.7f);
            txtUserName.setPadding(0,10,0,10);
            txtUserScreenName.setVisibility(View.GONE);
            txtUserDescription.setVisibility(View.GONE);
            imageIcon.setVisibility(View.INVISIBLE);

        } else {
            imageIcon.setVisibility(View.VISIBLE);
            txtUserName.setAlpha(1.0f);
            txtUserName.setPadding(0,0,0,0);
            txtUserScreenName.setVisibility(View.VISIBLE);
            txtUserDescription.setVisibility(View.VISIBLE);
            txtUserName.setText(mMenuHeader.get(groupPosition).getUserInfo().getName());
            txtUserScreenName.setText(mMenuHeader.get(groupPosition).getUserInfo().getDisplayScreenName());
            txtUserDescription.setText(mMenuHeader.get(groupPosition).getUserInfo().getDescription());
            imageIcon.setVisibility(View.VISIBLE);
            Picasso picasso = new Picasso.Builder(mContext)
                    .listener(new Picasso.Listener() {
                        @Override
                        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                            //Here your log
                            imageIcon.setVisibility(View.INVISIBLE);
                        }
                    })
                    .build();
            picasso.load(mMenuHeader.get(groupPosition).getUserInfo().getProfileImageUrlBig())
                    .into(imageIcon);
            imageIcon.setAdjustViewBounds(true);
        }

//        convertView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                /*If it's a long click, send the UserInfo object back to UserInfoFragment,
//                * to trigger opening the RemoveUser dialog */
//                _rxbus.sendLongClick(mMenuHeader.get(groupPosition).getUserInfo());
//                return false;
//            }
//        });

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

}
