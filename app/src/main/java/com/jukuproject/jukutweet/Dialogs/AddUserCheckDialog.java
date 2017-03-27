package com.jukuproject.jukutweet.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

//import android.app.DialogFragment;

/**
 * Dialog for "following" a new twitter user. New user name is entered into edittext
 * and then input into the database
 */
public class AddUserCheckDialog extends DialogFragment {

    private AlertDialog.Builder builder;
    public DialogInteractionListener mAddUserDialogListener;
    private ImageView imgBanner;
    String TAG = "TEST-AddUser";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAddUserDialogListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement mAddUserDialogListener");
        }


        Log.d(TAG,"ON ATTACH");

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
            imgBanner = (ImageView) dialogView.findViewById(R.id.imgBanner);
            try {
                loadBestFitBanner(userInfo.getScreenName(),imgBanner);
                //TODO set onclick listener for the banner

            } catch (NullPointerException e) {
                //TODO HIDE BANNER
                Log.e(TAG,"loadbestfit Nullpointer: " + e);
            } catch (Exception e) {
                //TODO HIDE BANNER
                Log.e(TAG,"loadbestfit failed: " + e);
            }
        }





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

//            txtUrl.setText(userInfo.getUrl());


        } else {

        }

//        Log.d(TAG,"followers: " + userInfo.getFollowerCount());
//        Log.d(TAG,"followers string: " + userInfo.getFollowerCountString());


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
                Log.d(TAG,"mAddUserDialogListener: " + mAddUserDialogListener);

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
//        Dialog dialog = builder.create();
//        dialog.getWindow().setLayout(300,500);

        Log.d(TAG,"BUilder create");
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mAddUserDialogListener.onDialogDismiss();
    }

//    @Override
//    public void onStart()
//    {
//        super.onStart();
//
//        // safety check
//        if (getDialog() == null)
//            return;
//
////        int dialogWidth = getDialog().getWindow().getWindowManager().getw // specify a value here
////        int dialogHeight = ... // specify a value here
//
//        int width = (int) (400 * Resources.getSystem().getDisplayMetrics().density);
//        int height = (int) (600 * Resources.getSystem().getDisplayMetrics().density);
//
//        getDialog().getWindow().setLayout(width,height);
////        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
//
//        // ... other stuff you want to do in your onStart() method
//    }

    private void loadBestFitBanner(final String screenName, final ImageView imgBanner) {

        Log.d(TAG,"INSIDE loading best fit banner");
        String token = getResources().getString(R.string.access_token);
        String tokenSecret = getResources().getString(R.string.access_token_secret);

//        final String bestFitUrl;
        TwitterUserClient.getInstance(token,tokenSecret)
                .getProfileBanner(screenName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserProfileBanner>() {
                    UserProfileBanner bannerInstance;


                    @Override public void onCompleted() {
                        if(BuildConfig.DEBUG){
                            Log.d(TAG, "In onCompleted()");}

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
                                                    //TODO load with banner
//                                                    showBanner(true);
//                                                    mAddUserDialogListener.showAddUserCheckDialog();
                                                }

                                                @Override
                                                public void onError() {
                                                    //TODO load without banner
//                                                    showBanner(false);
//                                                    mAddUserDialogListener.showAddUserCheckDialog();
                                                    onCreateDialog(null);
                                                }
                                            });




                                }
                            } catch (NullPointerException e) {
                                Log.e(TAG,"Banner url pull Nullpointed: " + e);
                            } catch (Exception e) {
                                Log.e(TAG,"Banner url pull Other Exception: " + e);
                            }



                        } else {
//                            Toast.makeText(MainActivity.this, "Unable to add user " + screenName, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        if(BuildConfig.DEBUG){Log.d(TAG, "BestFitBannerURL In onError()");}
//                        Toast.makeText(getBaseContext(), "Unable to connect to Twitter API", Toast.LENGTH_SHORT).show();
//                        showBanner(false);
                        Log.d(TAG,"ERROR CAUSE: " + e.getCause());
                    }

                    @Override public void onNext(UserProfileBanner userProfileBanner) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "BestFitBannerURL In onNext()");
//                            Log.d(TAG, "userInfo: " + userInfo.getUserId() + ", " + userInfo.getDescription());
                        }

                        /***TMP**/
                        if(bannerInstance == null) {
                            bannerInstance = userProfileBanner;
                        }


                    }
                });

    }






//    public void showBanner(boolean show) {
//
//    }


}