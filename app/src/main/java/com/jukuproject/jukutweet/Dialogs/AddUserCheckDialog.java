package com.jukuproject.jukutweet.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.Models.UserProfileBanner;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.TwitterUserClient;
import com.squareup.picasso.Picasso;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Dialog for "following" a new twitter user. New user name is entered into edittext
 * and then input into the database
 */
public class AddUserCheckDialog extends DialogFragment {

    public DialogInteractionListener mAddUserDialogListener;
    private SmoothProgressBar progressBar;
    String TAG = "TEST-AddUser";


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mAddUserDialogListener = (DialogInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement mAddUserDialogListener");
        }
    }

    public static AddUserCheckDialog newInstance(UserInfo userInfo) {
        AddUserCheckDialog frag = new AddUserCheckDialog();
        Bundle args = new Bundle();
        args.putParcelable("userInfo", userInfo);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final UserInfo userInfo = getArguments().getParcelable("userInfo");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //If there is no banner, show the simple different layout
        View dialogView;
        if(userInfo.getBannerUrl() == null) {
            dialogView  = inflater.inflate(R.layout.fragment_dialog_addusercheck_simple, null);
        } else {
            dialogView  = inflater.inflate(R.layout.fragment_dialog_addusercheck, null);
            ImageView imgBanner = (ImageView) dialogView.findViewById(R.id.imgBanner);
            progressBar = (SmoothProgressBar) dialogView.findViewById(R.id.progressbar);
            try {
                loadBestFitBanner(userInfo.getScreenName(),imgBanner);
            } catch (NullPointerException e) {
                Log.e(TAG,"loadbestfit Nullpointer: " + e);
            } catch (Exception e) {
                Log.e(TAG,"loadbestfit failed: " + e);
            }
        }

        /* Load profile image */
        final ImageView imgProfile = (ImageView) dialogView.findViewById(R.id.imgUser);
        if(userInfo.getProfileImageUrl()!=null) {

            /* Change the last bit of the image profile url from _normal to _bigger,
            * and try to get a better url image... if that doesn't work, do the normal url */
            Picasso picasso = new Picasso.Builder(getContext())
                    .listener(new Picasso.Listener() {
                        @Override
                        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                            //Here your log
                            Picasso.with(getContext()).load(userInfo.getProfileImageUrl())
                                    .into(imgProfile);

                        }
                    })
                    .build();
            picasso.load(userInfo.getProfileImageUrlBig())
                    .into(imgProfile);
            imgProfile.bringToFront();

        }


        TextView txtDisplayName = (TextView) dialogView.findViewById(R.id.txtName);
        txtDisplayName.setText(userInfo.getName());

        TextView txtScreenName = (TextView) dialogView.findViewById(R.id.txtScreenName);
        txtScreenName.setText(userInfo.getDisplayScreenName());

        if(userInfo.getUrl() != null) {
            TextView txtUrl = (TextView) dialogView.findViewById(R.id.txtUrl);
            txtUrl.setVisibility(View.VISIBLE);

            SpannableString text = new SpannableString(userInfo.getUrl());
            text.setSpan(new URLSpan(userInfo.getUrl()), 0, userInfo.getUrl().length(), 0);
            txtUrl.setMovementMethod(LinkMovementMethod.getInstance());
            txtUrl.setText(text, TextView.BufferType.SPANNABLE);
        }

        TextView txtFollowing = (TextView) dialogView.findViewById(R.id.txtFollowing);
        txtFollowing.setText(userInfo.getFriendCountString());

        TextView txtFollowers = (TextView) dialogView.findViewById(R.id.txtFollowers);
        txtFollowers.setText(userInfo.getFollowerCountString());

      builder.setView(dialogView);


        /* Checks for a valid input, and if one exists, passes click event through DialogInterface
           to MainActivity */
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAddUserDialogListener.saveAndUpdateUserInfoList(userInfo);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(true);
        return builder.create();
    }

    /**
     * Tries to load the user's profile banner into the top portion of the dialog
     * @param screenName users screen name
     * @param imgBanner user profile banner
     */
    private void loadBestFitBanner(final String screenName, final ImageView imgBanner) {
        if(progressBar!=null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        String token = getResources().getString(R.string.access_token);
        String tokenSecret = getResources().getString(R.string.access_token_secret);

        TwitterUserClient.getInstance(token,tokenSecret)
                .getProfileBanner(screenName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserProfileBanner>() {
                    UserProfileBanner bannerInstance;


                    @Override public void onCompleted() {
                        if(BuildConfig.DEBUG){Log.d(TAG, "In onCompleted()");}
                        if(progressBar!=null) {
                            progressBar.setVisibility(View.GONE);
                        }

                            /* If the user exists and a UserInfo object has been populated,
                            * save it to the database and update the UserInfoFragment adapter */
                        if(bannerInstance != null) {

                            //Set the banner that best fits phone size
                            try {
                                DisplayMetrics metrics = new DisplayMetrics();
                                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                String bannerUrl = bannerInstance.getBestFittingBannerUrl(metrics.widthPixels);
                                if(bannerUrl.length()>0 ) {
                                    Picasso.with(getActivity()).load(bannerUrl).fit() // resizes the image to these dimensions (in pixel). does not respect aspect ratio
                                            .into(imgBanner, new com.squareup.picasso.Callback() {
                                                @Override
                                                public void onSuccess() {
                                                }

                                                @Override
                                                public void onError() {
                                                    onCreateDialog(null);
                                                }
                                            });
                                }
                            } catch (NullPointerException e) {
                                Log.e(TAG,"Banner url pull Nullpointed: " + e);
                            } catch (Exception e) {
                                Log.e(TAG,"Banner url pull Other Exception: " + e);
                            }

                        }
                    }

                    @Override public void onError(Throwable e) {
                        if(progressBar!=null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        e.printStackTrace();
                        if(BuildConfig.DEBUG){Log.d(TAG, "BestFitBannerURL In onError()");}
                    }

                    @Override public void onNext(UserProfileBanner userProfileBanner) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "BestFitBannerURL In onNext()");
                        }
                            if(bannerInstance == null) {
                                bannerInstance = userProfileBanner;
                            }
                    }
                });

    }


}