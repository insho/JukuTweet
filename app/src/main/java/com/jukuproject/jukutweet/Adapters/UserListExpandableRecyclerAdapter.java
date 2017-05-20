package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MenuChild;
import com.jukuproject.jukutweet.Models.MenuHeader;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Shows twitter users saved to the db (name, icon etc) as "parent rows", that expand when clicked to show
 * child entries ("Timeline","Saved Tweets")
 * @see com.jukuproject.jukutweet.Fragments.UserListFragment
 */
public class UserListExpandableRecyclerAdapter extends ExpandableRecyclerAdapter<MenuHeader, MenuChild, UserListExpandableRecyclerAdapter.ParentOptionViewHolder, UserListExpandableRecyclerAdapter.ChildOptionViewHolder> {

    private LayoutInflater mInflater;
    private Context mContext;
    private RxBus mRxBus;
    private String TAG = "TEST-exprecyadp";

    public UserListExpandableRecyclerAdapter(Context context, @NonNull ArrayList<MenuHeader> menuHeaderList, RxBus rxBus) {
        super(menuHeaderList);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mRxBus = rxBus;
    }

    @Override
    public ParentOptionViewHolder  onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {

        View recipeView = mInflater.inflate(R.layout.userlist_recycler_row, parentViewGroup, false);
        return new ParentOptionViewHolder (recipeView);
    }

    @Override
    public ChildOptionViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View ingredientView = mInflater.inflate(R.layout.expandablelistadapter_listitem_userlistchild, childViewGroup, false);
        return new ChildOptionViewHolder(ingredientView);
    }

    // onBind ...
    @Override
    public void onBindParentViewHolder(@NonNull ParentOptionViewHolder  parentViewHolder, int parentPosition, @NonNull MenuHeader menuHeader) {
        parentViewHolder.bind(menuHeader);
    }



    @Override
    public void onBindChildViewHolder(@NonNull ChildOptionViewHolder childOptionViewHolder, int parentPosition, int childPosition, @NonNull MenuChild menuChild) {
        childOptionViewHolder.bind(menuChild);
    }




    public class ParentOptionViewHolder extends ParentViewHolder {

        private TextView mTxtUserName;
        private TextView mTxtUserDescription;
        private TextView mTxtUserScreenName;
        private ImageView mImageIcon;

        public ParentOptionViewHolder(View itemView) {
            super(itemView);

            mTxtUserName = (TextView) itemView.findViewById(R.id.name);
            mImageIcon = (ImageView) itemView.findViewById(R.id.image);
            mTxtUserDescription = (TextView) itemView.findViewById(R.id.description);
            mTxtUserScreenName = (TextView) itemView.findViewById(R.id.screenName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isExpanded()) {
                        collapseView();
                    } else {
                        expandView();
                    }
                }
            });
        }

        public void bind(final MenuHeader menuHeader) {

           final  UserInfo userInfo = menuHeader.getUserInfo();
            /* If the user only contains the username, and everything else is null, it is because the device
            * was offline while saving the user, and this row should be greyed out, and only display the username... */
            if(userInfo.getUserId() == null) {
                mTxtUserName.setText(userInfo.getDisplayScreenName());
                mTxtUserName.setAlpha(.7f);
                mTxtUserName.setPadding(0,10,0,10);
                mTxtUserScreenName.setVisibility(View.GONE);
                mTxtUserDescription.setVisibility(View.GONE);
                mImageIcon.setVisibility(View.INVISIBLE);

            } else {
                mImageIcon.setVisibility(View.VISIBLE);
                mTxtUserName.setAlpha(1.0f);
                mTxtUserName.setPadding(0,0,0,0);
                mTxtUserScreenName.setVisibility(View.VISIBLE);
                mTxtUserName.setText(userInfo.getName());
                mTxtUserScreenName.setText(userInfo.getDisplayScreenName());

                if(userInfo.getDescription()!=null && userInfo.getDescription().length()>0) {
                    mTxtUserDescription.setText(userInfo.getDescription());
                    mTxtUserDescription.setVisibility(View.VISIBLE);
                } else {
                    mTxtUserDescription.setVisibility(View.GONE);
                }
                mImageIcon.setVisibility(View.VISIBLE);

                String path;
                if(userInfo.getProfileImageFilePath()!=null) {
                    path = userInfo.getProfileImageFilePath();
                } else {
                    path = userInfo.getProfileImageUrlBig();
                }

                Picasso picasso = new Picasso.Builder(mContext)
                        .listener(new Picasso.Listener() {
                            @Override
                            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                                //Here your log
                                Log.e(TAG,"SET Image FAILED: " + userInfo.getScreenName());
                                mImageIcon.setVisibility(View.INVISIBLE);
                            }


                        })
                        .build();
                picasso.load(path)
                        .into(mImageIcon);
                mImageIcon.setAdjustViewBounds(true);
            }


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                /*If it's a long click, send the UserInfo object back to UserInfoFragment,
                * to trigger opening the RemoveUser dialog */
                mRxBus.sendLongClick(userInfo);
                return false;
            }
        });

        }
    }

    public class ChildOptionViewHolder extends ChildViewHolder {

//        private LinearLayout mLayout;
        private TextView mTxtListChild;
        private ImageButton mTweetIcon;
        private TextView mTweetIconText;

        public ChildOptionViewHolder(View itemView) {
            super(itemView);
//            mLayout = (LinearLayout) itemView.findViewById(R.id.layoutcontainer);
            mTxtListChild = (TextView) itemView.findViewById(R.id.lblListItem);
            mTweetIcon = (ImageButton) itemView.findViewById(R.id.tweet_icon);
            mTweetIconText = (TextView) itemView.findViewById(R.id.tweet_title);
        }

        public void bind(final MenuChild menuChild) {

            /* For the child entries of the adapter, only show the "colorblocks" in the first row of a mylist. This is differentiated
        * here by the string "Browse/Edit" which only appears in the "WordListFragment" fragment */

            mTxtListChild.setText(menuChild.getChildTitle());

            if(menuChild.getChildTitle().equalsIgnoreCase(mContext.getString(R.string.menuchildviewsavedtweets))) {
                mTweetIconText.setVisibility(View.VISIBLE);
                mTxtListChild.setAlpha(1.0f);
                mTweetIconText.setAlpha(1.0f);

                if(menuChild.getColorBlockMeasurables().getTweetCount()>0) {
                    mTweetIcon.setVisibility(View.VISIBLE);

            /* Add the count of saved tweets contained within the list */
                    try {
                        mTweetIconText.setText(mContext.getString(R.string.tweetcount,menuChild.getColorBlockMeasurables().getTweetCount()));
                    } catch (Exception e) {
                        mTweetIcon.setVisibility(View.GONE);
                        mTweetIconText.setVisibility(View.GONE);
                    }
                } else {
                    mTweetIcon.setVisibility(View.GONE);
                    mTweetIconText.setVisibility(View.INVISIBLE);
                    mTweetIconText.setText(mContext.getString(R.string.empty_parenthesis_space));
                    mTxtListChild.setAlpha(.7f);
                    mTweetIconText.setAlpha(.7f);

                }

            } else {
                mTweetIcon.setVisibility(View.GONE);
                mTweetIconText.setVisibility(View.GONE);
            }



        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuChild.getChildTitle().equalsIgnoreCase(mContext.getString(R.string.menuchildviewsavedtweets))
                        && menuChild.getColorBlockMeasurables().getTweetCount()==0){
                    mTweetIconText.setVisibility(View.VISIBLE);
                } else {
                /*If it's a short click, send the UserInfo object back to UserInfoFragment,
                * to trigger opening the UserTimeLine Fragment */
                    mRxBus.send(menuChild);
                }
            }
        });


        }
    }





}