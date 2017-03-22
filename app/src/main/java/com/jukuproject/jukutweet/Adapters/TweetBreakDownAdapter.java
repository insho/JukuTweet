package com.jukuproject.jukutweet.Adapters;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.InternalDB;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class TweetBreakDownAdapter extends RecyclerView.Adapter<TweetBreakDownAdapter.ViewHolder>  {
    String TAG = "TweetBreakDownAdapter";
    private static final boolean debug = false;

    private Context mContext;
    private ArrayList<WordEntry> mWords;
    private  int mGreyThreshold;
    private  float mRedThreshold;
    private  float mYellowThreshold;


    public class ViewHolder extends RecyclerView.ViewHolder {


        public TextView txtColorBar;
        public TextView txtKanji;
        public TextView txtFurigana;
        public TextView lstDefinitions;
        public ImageButton imgStar;
        public FrameLayout imgStarLayout;
        public TextView txtimgStarNumber;
        public LinearLayout layout;

        public ViewHolder(View v) {
            super(v);
            txtColorBar = (TextView) v.findViewById(R.id.colorbar);
            txtKanji = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Kanji);
            txtFurigana  = (TextView) v.findViewById(R.id.textViewBrowseAdapter_Furigana);
            lstDefinitions = (TextView) v.findViewById(R.id.textViewlstDefinitions);
            imgStar = (ImageButton) v.findViewById(R.id.favorite);
            txtimgStarNumber = (TextView) v.findViewById(R.id.favoritenumber);
            layout = (LinearLayout) v.findViewById(R.id.browseitems_layout);
            imgStarLayout = (FrameLayout) v.findViewById(R.id.browseitems_frameLayout);
        }
    }


    public TweetBreakDownAdapter(Context context, ArrayList<WordEntry> words, int greythreshold, float redthreshold, float yellowthreshold) {
        mContext = context;
        mWords = words;
        mGreyThreshold = greythreshold;
        mRedThreshold = redthreshold;
        mYellowThreshold = yellowthreshold;

    }


    @Override
    public TweetBreakDownAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweetbreakdown_recycler_row, parent, false);
        return new ViewHolder(v);
    }

    public WordEntry getItem(int position) {
        return mWords.get(position);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position ) {

        if(debug){
            Log.d(TAG, "position: " + holder.getAdapterPosition());}

        final WordEntry wordEntry = mWords.get(holder.getAdapterPosition());

            holder.txtimgStarNumber.setVisibility(View.GONE);
            holder.layout.setSelected(false);
            holder.imgStar.setVisibility(View.VISIBLE);
            holder.imgStarLayout.setClickable(true);
            holder.imgStarLayout.setLongClickable(true);


        ArrayList<String> tmpDefinitionArray = new ArrayList<>();

        holder.txtKanji.setText(wordEntry.getKanji());
        holder.txtFurigana.setText(wordEntry.getFurigana());


        /**
         * Parse the definition into an array of multiple lines, if there are multiple sub-definitions in the string
         */



        if(wordEntry.getTotal()< mGreyThreshold) {
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuGrey));
        } else if(wordEntry.getPercentage()< mRedThreshold){
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuRed));
        } else if (wordEntry.getPercentage()< mYellowThreshold){
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuYellow));
        } else {
            holder.txtColorBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorJukuGreen));
        }


        holder.lstDefinitions.setText(wordEntry.getDefinitionMultiLineString(10));
        holder.lstDefinitions.setTypeface(null, Typeface.ITALIC);
        holder.lstDefinitions.setTag(wordEntry.getId());
        holder.lstDefinitions.setFocusable(false);
        holder.lstDefinitions.setClickable(false);


        holder.imgStarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO favorite words
                Toast.makeText(mContext, "Clicked", Toast.LENGTH_SHORT).show();
            }
        });


        holder.imgStarLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
            //TODO multiselect words
                return true;
            }
        });




//        holder.layout.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                /**  The external (BrowseBlocks) onclick listener happens first, apparently. So the hashmap operations should already be done. Just update the visuals*/
//            }
//        });



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mWords.size();
    }



}

