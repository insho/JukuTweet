//package com.jukuproject.jukutweet.Adapters;
//
///**
// * Created by JClassic on 3/23/2017.
// */
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.graphics.PorterDuff;
//import android.graphics.drawable.Drawable;
//import android.os.Build;
//import android.preference.PreferenceManager;
//import android.support.v4.content.ContextCompat;
//import android.text.SpannableString;
//import android.text.style.UnderlineSpan;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseExpandableListAdapter;
//import android.widget.ImageButton;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.jukuproject.jukutweet.BuildConfig;
//import com.jukuproject.jukutweet.Models.ColorBlockMeasurables;
//import com.jukuproject.jukutweet.Models.MenuHeader;
//import com.jukuproject.jukutweet.R;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class ColorMenuExpandableListAdapter extends BaseExpandableListAdapter {
//    String TAG = "Menu_Ex_ListAdapter";
//
//    private Context _context;
//    private ArrayList<MenuHeader> mMenuHeader;
//
//    //    private List<String> _listDataHeader; // header titles
////    private HashMap<String, List<String>> _listDataChild;
////    private HashMap<String, List<Integer>> _listDataExtra; //This is for the "quiz by colors" expandable list. Holds data on color groups. (otherwise use a dummy hashmap)
//    private boolean _usecolorbar;  // Same goes for this boolean. used for colorbars in the "quiz by colors" list.
//    private List<Integer> _addStarList; // This one only used for myfavorites list, to keep track of star buttons for system favorites.
//    private int _textsize;
//    private ArrayList<String> _colors;
//    private boolean _showlblheadercount;
//    private boolean _mylists;
//
//    /**This correspond to textsize values of color blocks, so we have a minimum value*/
//
//    private TextView textViewColorBlock_grey;
//    private TextView textViewColorBlock_red;
//    private TextView textViewColorBlock_yellow;
//    private TextView textViewColorBlock_green;
//    private TextView lblColorBar;
//    private int dimenscore_supertotal;
//
//
//    public ColorMenuExpandableListAdapter(Context context
//            , ArrayList<MenuHeader> menuHeader;
////                                    , List<String> listDataHeader
//            , HashMap<String, List<String>> listChildData,
//                                     HashMap<String, List<Integer>> listDataExtra,
//                                     Boolean usecolorbar
//            , List<Integer> addStarList,
//                                     int dimenscore_total,
//                                     int fontsize,
//                                     ArrayList<String> colors,
//                                     Boolean showlblheadercount,
//                                     Boolean mylists
//    ) {
//        this._context = context;
////        this._listDataHeader = listDataHeader;
////        this._listDataChild = listChildData;
//        this._usecolorbar = usecolorbar;
//        this._listDataExtra = listDataExtra;
//        this._addStarList = addStarList;
//        this.dimenscore_supertotal = dimenscore_total;
//        this._textsize = fontsize;
//        this._colors = colors;
//        this._showlblheadercount = showlblheadercount;
//        this._mylists = mylists;
//    }
//
//    @Override
//    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
//    }
//
//    @Override
//    public void notifyDataSetInvalidated() {
//        super.notifyDataSetInvalidated();
//    }
//
//    @Override
//    public Object getChild(int groupPosition, int childPosititon) {
//
//        return mMenuHeader.get(groupPosition).getChildOptions().get(childPosititon);
////        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
////                .get(childPosititon);
//    }
//
//    @Override
//    public long getChildId(int groupPosition, int childPosition) {
//        return childPosition;
//    }
//
//    @Override
//    public View getChildView(int groupPosition, final int childPosition,
//                             boolean isLastChild, View convertView, ViewGroup parent) {
//
//        int dimenscore_total = dimenscore_supertotal;
//        final String childText = (String) getChild(groupPosition, childPosition);
//        final String HeaderText = getGroup(groupPosition).toString();
//        if(BuildConfig.DEBUG){
//            Log.d(TAG,"HeaderText: " + HeaderText);}
//        if (convertView == null) {
//            convertView = LayoutInflater.from(_context).inflate(R.layout.expandablelistadapter_listitem, parent, false);
//        }
//        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.layoutcontainer);
//
//        TextView txtListChild = (TextView) convertView
//                .findViewById(R.id.lblListItem);
//        txtListChild.setText(childText);
//
//        ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.favorite_icon);
//        imageButton.setFocusable(false);
//        imageButton.setClickable(false);
//
//        textViewColorBlock_grey = (TextView) convertView.findViewById(R.id.listitem_colors_1);
//        textViewColorBlock_red = (TextView) convertView.findViewById(R.id.listitem_colors_2);
//        textViewColorBlock_yellow = (TextView) convertView.findViewById(R.id.listitem_colors_3);
//        textViewColorBlock_green = (TextView) convertView.findViewById(R.id.listitem_colors_4);
//
//        lblColorBar = (TextView) convertView.findViewById(R.id.lblcolorbar);
//        lblColorBar.setVisibility(TextView.GONE);
////        if(BuildConfig.DEBUG){Log.d(TAG, "listdataextra size: " + mMenuHeader.get(groupPosition).getColorBlockMeasurables().size());}
//
//        String newHeaderTitle = HeaderText;
//        if(_addStarList.size() >0 && _addStarList.size() >groupPosition) {
//            newHeaderTitle = HeaderText + "1";
//        }else if(_mylists ){
//            newHeaderTitle = HeaderText + "0";
//        }
//        if(BuildConfig.DEBUG) {
//            Log.d(TAG, "newheadertext: " + newHeaderTitle);
//            Log.d(TAG, "childText: " + childText);
//        }
//
//        if(mMenuHeader.get(groupPosition).getColorBlockMeasurables() != null && childText.equalsIgnoreCase("Browse/Edit")) {
////            int count = _listDataExtra.get(newHeaderTitle).get(0);
////            int grey = _listDataExtra.get(newHeaderTitle).get(1);
////            int red = _listDataExtra.get(newHeaderTitle).get(2);
////            int yellow = _listDataExtra.get(newHeaderTitle).get(3);
////            int green = _listDataExtra.get(newHeaderTitle).get(4);
//
////            int greyminwidth = 0;
////            int redminwidth = 0;
////            int yellowminwidth = 0;
////            int greenminwidth = 0;
//
////            if(_listDataExtra.get(newHeaderTitle).size()>8){
////                greyminwidth = _listDataExtra.get(newHeaderTitle).get(5);
////                redminwidth = _listDataExtra.get(newHeaderTitle).get(6);
////                yellowminwidth = _listDataExtra.get(newHeaderTitle).get(7);
////                greenminwidth = _listDataExtra.get(newHeaderTitle).get(8);
////
////            }
//            final ColorBlockMeasurables colorBlockMeasurables = mMenuHeader.get(groupPosition).getColorBlockMeasurables();
//
//            Drawable drawablecolorblock1 = ContextCompat.getDrawable(_context, R.drawable.colorblock);
//            Drawable drawablecolorblock2 = ContextCompat.getDrawable(_context, R.drawable.colorblock);
//            Drawable drawablecolorblock3 = ContextCompat.getDrawable(_context, R.drawable.colorblock);
//            Drawable drawablecolorblock4 = ContextCompat.getDrawable(_context, R.drawable.colorblock);
//
//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                drawablecolorblock1.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
//                textViewColorBlock_grey.setBackground(drawablecolorblock1);
//                drawablecolorblock2.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
//                textViewColorBlock_red.setBackground(drawablecolorblock2);
//                drawablecolorblock3.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
//                textViewColorBlock_yellow.setBackground(drawablecolorblock3);
//                drawablecolorblock4.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
//                textViewColorBlock_green.setBackground(drawablecolorblock4);
//
//            } else {
//                drawablecolorblock1.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
//                textViewColorBlock_grey.setBackgroundDrawable(drawablecolorblock1);
//                drawablecolorblock2.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
//                textViewColorBlock_red.setBackgroundDrawable(drawablecolorblock2);
//                drawablecolorblock3.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
//                textViewColorBlock_yellow.setBackgroundDrawable(drawablecolorblock3);
//                drawablecolorblock4.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
//                textViewColorBlock_green.setBackgroundDrawable(drawablecolorblock4);
//            }
//
//
//
//            textViewColorBlock_grey.setVisibility(View.GONE);
//            textViewColorBlock_red.setVisibility(View.GONE);
//            textViewColorBlock_yellow.setVisibility(View.GONE);
//            textViewColorBlock_green.setVisibility(View.GONE);
//
//
//            textViewColorBlock_red.setText(String.valueOf(colorBlockMeasurables.getRedCount()));
//            textViewColorBlock_yellow.setText(String.valueOf(colorBlockMeasurables.getYellowCount()));
//            textViewColorBlock_green.setText(String.valueOf(colorBlockMeasurables.getGreenCount()));
//
//
//
//            if (colorBlockMeasurables.getRedCount()
//                    + colorBlockMeasurables.getYellowCount()
//                    + colorBlockMeasurables.getGreenCount() == 0) {
//                textViewColorBlock_grey.setText(String.valueOf(colorBlockMeasurables.getTotalCount()));
//                textViewColorBlock_grey.setVisibility(View.VISIBLE);
//                textViewColorBlock_grey.setMinimumWidth(dimenscore_total);
//                if(BuildConfig.DEBUG){Log.d(TAG, newHeaderTitle+ " - " + "grey (total) dimenscore: " + dimenscore_total);}
//
//            } else {
//
//                int dimenscoreremaining = dimenscore_total;
//                if(colorBlockMeasurables.getGreyCount()>0){
//                    textViewColorBlock_grey.setText(String.valueOf(colorBlockMeasurables.getGreyCount()));
//                    int dimenscore = colorBlockMeasurables.getGreyDimenscore(dimenscore_total);
//
//                    if(BuildConfig.DEBUG) {
//                        Log.i("MINMIN","dimenscoretotal: " + dimenscore_total);
//                        Log.i("MINMIN","grey/count: " + colorBlockMeasurables.getGreyCount() + "/" + colorBlockMeasurables.getTotalCount());
//                        Log.i("MINMIN","((float) grey / (float) count): " + ((float) colorBlockMeasurables.getGreyCount() / (float) colorBlockMeasurables.getTotalCount()));
//                        Log.i("MINMIN","Rounded score: " + dimenscore);
//                    }
//
////                    if((dimenscore_total-dimenscore)<(redminwidth+yellowminwidth+greenminwidth)){
////                        dimenscore =dimenscore_total-(redminwidth+yellowminwidth+greenminwidth);
////                    } else if(dimenscore<greyminwidth) {
////                        dimenscore = greyminwidth;
////                    }
//
//
//                    dimenscoreremaining = dimenscore_total-dimenscore;
//
//                    if(BuildConfig.DEBUG){
//                        Log.e("MINMIN",newHeaderTitle+ " - " + "dimenscoregrey: " + dimenscore);
//                        Log.e("MINMIN", newHeaderTitle+ " - " + "dimenscoreremaining: " + dimenscoreremaining +"/"+dimenscore_total);
//                    }
//
//
//                    textViewColorBlock_grey.setMinimumWidth(dimenscore);
//                    textViewColorBlock_grey.setVisibility(View.VISIBLE);
//                }
//
//                if(colorBlockMeasurables.getRedCount()>0){
//                    textViewColorBlock_red.setText(String.valueOf(colorBlockMeasurables.getRedCount()));
//                    int dimenscore = colorBlockMeasurables.getRedDimenscore(dimenscore_total,dimenscoreremaining);
//
////                    int dimenscore = Math.round((dimenscore_total * ((float) red / (float) count)));
////                    if((dimenscoreremaining-dimenscore)<(yellowminwidth+greenminwidth)){
////                        dimenscore =dimenscoreremaining-(yellowminwidth+greenminwidth);
////                    } else if(dimenscore<redminwidth) {
////                        dimenscore = redminwidth;
////                    }
//                    dimenscoreremaining = dimenscoreremaining -dimenscore;
//                    textViewColorBlock_red.setMinimumWidth(dimenscore);
//                    textViewColorBlock_red.setVisibility(View.VISIBLE);
//
//                    if(BuildConfig.DEBUG){
//                        Log.e("MINMIN", newHeaderTitle+ " - " + "dimenscorered: " + dimenscore);
//                        Log.e("MINMIN",newHeaderTitle+ " - " +  "dimenscoreremaining: " + dimenscoreremaining + "/" + dimenscore_total);
//                    }
//
//
//                }
//
//                if(colorBlockMeasurables.getYellowCount()>0){
//                    textViewColorBlock_yellow.setText(String.valueOf(colorBlockMeasurables.getYellowCount()));
//                    int dimenscore = colorBlockMeasurables.getYellowDimenscore(dimenscore_total,dimenscoreremaining);
////                    int dimenscore = Math.round((dimenscore_total * ((float) yellow / (float) count)));
////                    if((dimenscoreremaining-dimenscore)<(greenminwidth)){
////                        dimenscore =dimenscoreremaining-(greenminwidth);
////                    } else if(dimenscore<yellowminwidth) {
////                        dimenscore = yellowminwidth;
////                    }
//                    dimenscoreremaining = dimenscoreremaining -dimenscore;
//
//                    if(BuildConfig.DEBUG){
//
//                        Log.e("MINMIN", newHeaderTitle+ " - " + "dimenscoreyellow: " + dimenscore);
//                        Log.e("MINMIN", newHeaderTitle+ " - " + "dimenscoreremaining: " + dimenscoreremaining +"/"+dimenscore_total);
//
//                    }
//
//
//                    textViewColorBlock_yellow.setMinimumWidth(dimenscore);
//                    textViewColorBlock_yellow.setVisibility(View.VISIBLE);
//
//                }
//
//                if(colorBlockMeasurables.getGreenCount()>0){
//                    textViewColorBlock_green.setText(String.valueOf(colorBlockMeasurables.getGreenCount()));
//                    int dimenscore = colorBlockMeasurables.getGreenDimenscore(dimenscore_total,dimenscoreremaining);
////                    int dimenscore = Math.round((dimenscore_total * ((float) green / (float) count)));
//
//                    if(BuildConfig.DEBUG) {
//                        Log.i("MINMIN","dimenscoretotal: " + dimenscore_total);
//                        Log.i("MINMIN","grey/count: " + colorBlockMeasurables.getGreenCount() + "/" + colorBlockMeasurables.getTotalCount());
//                        Log.i("MINMIN","((float) grey / (float) count): " + ((float) colorBlockMeasurables.getGreenCount() / (float) colorBlockMeasurables.getTotalCount()));
//                        Log.i("MINMIN","Rounded score: " + dimenscore);
//
//                    }
//
////                    if((dimenscoreremaining- dimenscore )<0 ){
////                        dimenscore =dimenscoreremaining;
////                    } else if(dimenscore<greenminwidth) {
////                        dimenscore = greenminwidth;
////                    }
//
//                    textViewColorBlock_green.setMinimumWidth(dimenscore);
//                    textViewColorBlock_green.setVisibility(View.VISIBLE);
//
//                    if(BuildConfig.DEBUG){
//                        Log.e("MINMIN", newHeaderTitle + " - " + "dimenscoregreen: " + dimenscore);
//                        Log.e("MINMIN",newHeaderTitle+ " - " +  "dimenscoreremaining: " + dimenscoreremaining + "/" + dimenscore_total);
//
//                    }
//
//                }
//
//
//            }
//
//
//            if (colorBlockMeasurables.getTotalCount() == 0) {
//                textViewColorBlock_grey.setVisibility(View.VISIBLE);
//                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    drawablecolorblock1.setColorFilter(ContextCompat.getColor(_context, android.R.color.white), PorterDuff.Mode.MULTIPLY);
//                    textViewColorBlock_grey.setBackground(drawablecolorblock1);
//                } else {
//                    drawablecolorblock1.setColorFilter(ContextCompat.getColor(_context, android.R.color.white), PorterDuff.Mode.MULTIPLY);
//                    textViewColorBlock_grey.setBackgroundDrawable(drawablecolorblock1);
//                }
//                textViewColorBlock_grey.setText(_context.getString(R.string.empty));
//                textViewColorBlock_red.setVisibility(View.GONE);
//                textViewColorBlock_yellow.setVisibility(View.GONE);
//                textViewColorBlock_green.setVisibility(View.GONE);
//            }
//
//        } else {
//            textViewColorBlock_grey.setVisibility(View.GONE);
//            textViewColorBlock_red.setVisibility(View.GONE);
//            textViewColorBlock_yellow.setVisibility(View.GONE);
//            textViewColorBlock_green.setVisibility(View.GONE);
//        }
//
//
//        return layout;
//    }
//
//    @Override
//    public int getChildrenCount(int groupPosition) {
//        return this.mMenuHeader.get(groupPosition).getChildOptions().size();
////        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
////                .size();
//    }
//
//    @Override
//    public Object getGroup(int groupPosition) {
//        return this.mMenuHeader.get(groupPosition);
//    }
//
//    @Override
//    public int getGroupCount() {
//        return this.mMenuHeader.size();
//    }
//
//    @Override
//    public long getGroupId(int groupPosition) {
//        return groupPosition;
//    }
//
//    @Override
//    public View getGroupView(int groupPosition, boolean isExpanded,
//                             View convertView, ViewGroup parent) {
//        String headerTitle = (String) getGroup(groupPosition);
//        int dimenscore_total = dimenscore_supertotal;
//        if (convertView == null) {
//            LayoutInflater infalInflater = (LayoutInflater) this._context
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = infalInflater.inflate(R.layout.expandablelistadapter_groupitem, null);
//        }
//
//
//        final ColorBlockMeasurables colorBlockMeasurables = mMenuHeader.get(groupPosition).getColorBlockMeasurables();
//
//        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
//        lblListHeader.setAlpha(1.0f);
//        if(BuildConfig.DEBUG){Log.d(TAG,"headerTitle: --" + headerTitle + "--" );}
//        if(_textsize>0){
//            lblListHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, _textsize);
//        }
//
//        LinearLayout background = (LinearLayout) convertView.findViewById(R.id.balls2);
//        lblListHeader.setBackgroundColor(ContextCompat.getColor(_context, android.R.color.white));
//        lblListHeader.setTextColor(ContextCompat.getColor(_context, android.R.color.black));
//        background.setBackgroundColor(ContextCompat.getColor(_context, android.R.color.white));
//        lblListHeader.setGravity(Gravity.START);
//
//        lblColorBar = (TextView) convertView.findViewById(R.id.lblcolorbar);
//        lblColorBar.setGravity(Gravity.CENTER);
////        if(_usecolorbar) {
//        if(mMenuHeader.get(groupPosition).getType().equals("colorbar")) {
//            if(BuildConfig.DEBUG){Log.d(TAG,"USING COLORBAR"  );}
//
////            Integer colorcount;
////            Integer colorcount = _listDataExtra.get(headerTitle).get(1);
//
//
//            final Drawable drawable = ContextCompat.getDrawable(_context, R.drawable.colorblock);
//
//            switch(headerTitle) {
//                case "Grey":
//                    drawable.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
//                    lblColorBar.setText(String.valueOf(colorBlockMeasurables.getGreyCount()));
//                    break;
//                case "Red":
//                    drawable.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
//                    lblColorBar.setText(String.valueOf(colorBlockMeasurables.getRedCount()));
//                    break;
//                case "Yellow":
//                    drawable.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
//                    lblColorBar.setText(String.valueOf(colorBlockMeasurables.getYellowCount()));
//                    break;
//                case "Green":
//                    drawable.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
//                    lblColorBar.setText(String.valueOf(colorBlockMeasurables.getGreenCount()));
//                    break;
//
//            }
//
//
//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                lblColorBar.setBackground(drawable);
//            } else {
//                lblColorBar.setBackgroundDrawable(drawable);
//            }
//
//        } else if (mMenuHeader.get(groupPosition).getType().equals("userlist")){
//
//
//
//        } else {
//
//            if (_mylists) {  //.i.e. we're expanding the favorites lists in myfavorites, and there are star pictures to assign to the headers
//
//                if(_colors == null){
//                    _colors = new ArrayList<>();
//                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
//                    Set<String> prefs_stars_hash = prefs.getStringSet("list_favoriteslistcount", new HashSet<String>());
//                    for (String s : prefs_stars_hash) {
//                        if (s.length() > 0) {
//                            String upperS = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
//                            _colors.add(upperS);
//                        }
//                    }
//                }
//
//                ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.favorite_icon);
//                imageButton.setFocusable(false);
//                imageButton.setClickable(false);
//
//
//                if (_addStarList.size() > groupPosition && _colors.contains(headerTitle)) {
//
//                    String sys = "1";
//                    imageButton.setImageResource(R.drawable.ic_star_black);
//                    imageButton.setVisibility(ImageButton.VISIBLE);
//
//                    lblListHeader.setText(headerTitle);
//
//                    int count = _listDataExtra.get(headerTitle+sys).get(0);
//
//                    final TextView lblListHeaderCount = (TextView) convertView.findViewById(R.id.lblListHeaderCount);
//
//                    if(_showlblheadercount) {
//                        lblListHeaderCount.setVisibility(TextView.VISIBLE); /**SET TO VISIBLE TO SEE*/
//                    } else {
//                        lblListHeaderCount.setVisibility(TextView.GONE);
//                    }
//                    lblListHeader.setAlpha(1.0f);
//                    if(headerTitle.equalsIgnoreCase("Create New List") || groupPosition == mMenuHeader.size() -1) {
//                        if(BuildConfig.DEBUG){Log.d(TAG, "doing the correct thing");}
//
//                        lblListHeaderCount.setVisibility(TextView.GONE);
//                    } else if(count>0) {
//                        if(BuildConfig.DEBUG){Log.d(TAG,"setting counts...");}
//                        lblListHeaderCount.setText("(" + count + ")");
//                        lblListHeader.setAlpha(1.0f);
//                        lblListHeaderCount.setAlpha(.7f);
//                    } else {
//                        lblListHeaderCount.setText("");
//                        lblListHeader.setAlpha(.5f);
//                        lblListHeaderCount.setAlpha(.5f);
//                    }
//
//
//
//                    if(headerTitle.equals("Yellow")) {
//                        imageButton.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuYellow));
//                    } else if (headerTitle.equals("Blue")) {
//                        imageButton.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuBlue));
//                    } else if (headerTitle.equals("Red")) {
//                        imageButton.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuRed));
//                    } else if (headerTitle.equals("Green")) {
//                        imageButton.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuGreen));
//                    }
//                } else {
//                    /** if it is not a system list, but is instead a user-created MyList, data pulled from the mylist_extra map
//                     * must use a title + 0 concatenation instead of just the title...*/
//                    String newHeaderTitle = headerTitle;
//                    if(_mylists) {
//                        newHeaderTitle = headerTitle + "0";
//                    }
//                    imageButton.setVisibility(ImageButton.GONE);
//                    lblListHeader.setText(headerTitle);
//                    final TextView lblListHeaderCount = (TextView) convertView.findViewById(R.id.lblListHeaderCount);
//
//                    if(_showlblheadercount) {
//                        lblListHeaderCount.setVisibility(TextView.VISIBLE); /**SET TO VISIBLE TO SEE*/
//                    } else {
//                        lblListHeaderCount.setVisibility(TextView.GONE);
//                    }
//
//                    float alpha = .5f;
//                    lblListHeaderCount.setAlpha(alpha);
//
//                    if(!_listDataExtra.containsKey(newHeaderTitle) && headerTitle.equalsIgnoreCase("Create New List")) {
//                        lblListHeaderCount.setVisibility(TextView.GONE);
//                        String string = "Create New List";
//                        SpannableString content = new SpannableString(string);
//                        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//                        lblListHeader.setText(content);
//
//                        if(BuildConfig.DEBUG){Log.d(TAG, "doing the correct thing 2");}
//
//                    } else if(!_listDataExtra.containsKey(newHeaderTitle) || _listDataExtra.get(newHeaderTitle).size() == 0 || _listDataExtra.get(newHeaderTitle).get(0) == 0) {
//                        lblListHeaderCount.setText("");
//                        lblListHeader.setAlpha(.5f);
//                        lblListHeaderCount.setAlpha(.5f);
//                    } else {
//                        int count = _listDataExtra.get(newHeaderTitle).get(0);
//                        String headercounttmp = "(" + count + ")";
//                        lblListHeaderCount.setText(headercounttmp);
//                        lblListHeader.setAlpha(1.0f);
//                        lblListHeaderCount.setAlpha(.7f);
//                        if(BuildConfig.DEBUG){Log.d(TAG, "?? 4  -- " + headerTitle.toString());}
//
//                    }
//
//                }
//
//
//
//            } else {
//                lblListHeader.setText(headerTitle);
//
//
//            }
//
//            textViewColorBlock_grey = (TextView) convertView.findViewById(R.id.listitem_colors_1);
//            textViewColorBlock_red = (TextView) convertView.findViewById(R.id.listitem_colors_2);
//            textViewColorBlock_yellow = (TextView) convertView.findViewById(R.id.listitem_colors_3);
//            textViewColorBlock_green = (TextView) convertView.findViewById(R.id.listitem_colors_4);
//
//            lblColorBar = (TextView) convertView.findViewById(R.id.lblcolorbar);
//            lblColorBar.setVisibility(TextView.GONE);
//            if(BuildConfig.DEBUG){Log.d(TAG, "listdataextra size: " + _listDataExtra.size());}
//
//            if(_mylists && !_usecolorbar) {//i.e. if it's the favorites list don't do any of that shit
//                textViewColorBlock_grey.setVisibility(View.GONE);
//                textViewColorBlock_red.setVisibility(View.GONE);
//                textViewColorBlock_yellow.setVisibility(View.GONE);
//                textViewColorBlock_green.setVisibility(View.GONE);
//
//            } else {
//
//                if (_listDataExtra != null && _listDataExtra.size() > 0 && !headerTitle.equalsIgnoreCase("Create New List")) {
//
//                    if(BuildConfig.DEBUG){
//                        Log.d(TAG,"listDataExtra size: " + _listDataExtra.size() + ", " + headerTitle);
//
//                    }
//                    int count = _listDataExtra.get(headerTitle).get(0);
//                    int grey = _listDataExtra.get(headerTitle).get(1);
//                    int red = _listDataExtra.get(headerTitle).get(2);
//                    int yellow = _listDataExtra.get(headerTitle).get(3);
//                    int green = _listDataExtra.get(headerTitle).get(4);
//
//
//                    int greyminwidth = 0;
//                    int redminwidth = 0;
//                    int yellowminwidth = 0;
//                    int greenminwidth = 0;
//
//                    if(_listDataExtra.get(headerTitle).size()>8){
//                        greyminwidth = _listDataExtra.get(headerTitle).get(5);
//                        redminwidth = _listDataExtra.get(headerTitle).get(6);
//                        yellowminwidth = _listDataExtra.get(headerTitle).get(7);
//                        greenminwidth = _listDataExtra.get(headerTitle).get(8);
//
//                    }
//
//
//
//                    if(BuildConfig.DEBUG){
//                        Log.e("MINMIN",headerTitle+ " - " + "_listDataExtra.get(headerTitle).size(): " + _listDataExtra.get(headerTitle).size());
//                        Log.e("MINMIN",headerTitle+ " - " + "greyminwidth: " + greyminwidth);
//                        Log.e("MINMIN",headerTitle+ " - " + "redminwidth: " + redminwidth);
//                        Log.e("MINMIN",headerTitle+ " - " + "yellowminwidth: " + yellowminwidth);
//                        Log.e("MINMIN",headerTitle+ " - " + "greenminwidth: " + greenminwidth);
//                    }
//
//                    Drawable drawablecolorblock1 = ContextCompat.getDrawable(_context, R.drawable.colorblock);
//                    Drawable drawablecolorblock2 = ContextCompat.getDrawable(_context, R.drawable.colorblock);
//                    Drawable drawablecolorblock3 = ContextCompat.getDrawable(_context, R.drawable.colorblock);
//                    Drawable drawablecolorblock4 = ContextCompat.getDrawable(_context, R.drawable.colorblock);
//
//                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                        drawablecolorblock1.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
//                        textViewColorBlock_grey.setBackground(drawablecolorblock1);
//                        drawablecolorblock2.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
//                        textViewColorBlock_red.setBackground(drawablecolorblock2);
//                        drawablecolorblock3.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
//                        textViewColorBlock_yellow.setBackground(drawablecolorblock3);
//                        drawablecolorblock4.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
//                        textViewColorBlock_green.setBackground(drawablecolorblock4);
//
//                    } else {
//                        drawablecolorblock1.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuGrey), PorterDuff.Mode.MULTIPLY);
//                        textViewColorBlock_grey.setBackgroundDrawable(drawablecolorblock1);
//                        drawablecolorblock2.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuRed), PorterDuff.Mode.MULTIPLY);
//                        textViewColorBlock_red.setBackgroundDrawable(drawablecolorblock2);
//                        drawablecolorblock3.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuYellow), PorterDuff.Mode.MULTIPLY);
//                        textViewColorBlock_yellow.setBackgroundDrawable(drawablecolorblock3);
//                        drawablecolorblock4.setColorFilter(ContextCompat.getColor(_context, R.color.colorJukuGreen), PorterDuff.Mode.MULTIPLY);
//                        textViewColorBlock_green.setBackgroundDrawable(drawablecolorblock4);
//                    }
//
//
//                    textViewColorBlock_grey.setVisibility(View.GONE);
//                    textViewColorBlock_red.setVisibility(View.GONE);
//                    textViewColorBlock_yellow.setVisibility(View.GONE);
//                    textViewColorBlock_green.setVisibility(View.GONE);
//
//
//                    textViewColorBlock_red.setText(String.valueOf(red));
//                    textViewColorBlock_yellow.setText(String.valueOf(yellow));
//                    textViewColorBlock_green.setText(String.valueOf(green));
//
//
//                    if(BuildConfig.DEBUG){Log.d(TAG,headerTitle+ " - " + "groupview dimenscore total: " + dimenscore_total);}
//                    if (_mylists) {
//                        dimenscore_total = Math.round((dimenscore_total * ((float) 0.866666667 / (float) dimenscore_total)));
//                        if(BuildConfig.DEBUG){Log.d(TAG,headerTitle+ " - " + "groupview dimenscore decreasing to : " + dimenscore_total);}
//                    }
//
//
//
//
//                    if (red + yellow + green == 0) {
//                        textViewColorBlock_grey.setText(String.valueOf(count));
//                        textViewColorBlock_grey.setVisibility(View.VISIBLE);
//                        textViewColorBlock_grey.setMinimumWidth(dimenscore_total);
//                        if(BuildConfig.DEBUG){Log.d(TAG, headerTitle+ " - " + "grey (total) dimenscore: " + dimenscore_total);}
//
//                    } else {
//
//                        int dimenscoreremaining = dimenscore_total;
//                        if(grey>0){
//                            textViewColorBlock_grey.setText(String.valueOf(grey));
//                            int dimenscore = Math.round((dimenscore_total * ((float) grey / (float) count)));
//                            if(BuildConfig.DEBUG) {
//                                Log.i("MINMIN","dimenscoretotal: " + dimenscore_total);
//                                Log.i("MINMIN","grey/count: " + grey + "/" + count);
//                                Log.i("MINMIN","((float) grey / (float) count): " + ((float) grey / (float) count));
//                                Log.i("MINMIN","Rounded score: " + dimenscore);
//
//                            }
//                            if((dimenscore_total-dimenscore)<(redminwidth+yellowminwidth+greenminwidth)){
//                                dimenscore =dimenscore_total-(redminwidth+yellowminwidth+greenminwidth);
//                            } else if(dimenscore<greyminwidth) {
//                                dimenscore = greyminwidth;
//                            }
//
//
//                            dimenscoreremaining = dimenscore_total-dimenscore;
//
//                            if(BuildConfig.DEBUG){
//                                Log.e("MINMIN",headerTitle+ " - " + "dimenscoregrey: " + dimenscore);
//                                Log.e("MINMIN", headerTitle+ " - " + "dimenscoreremaining: " + dimenscoreremaining +"/"+dimenscore_total);
//                            }
//
//
//                            textViewColorBlock_grey.setMinimumWidth(dimenscore);
//                            textViewColorBlock_grey.setVisibility(View.VISIBLE);
//                        }
//
//                        if(red>0){
//                            textViewColorBlock_red.setText(String.valueOf(red));
//                            int dimenscore = Math.round((dimenscore_total * ((float) red / (float) count)));
//                            if((dimenscoreremaining-dimenscore)<(yellowminwidth+greenminwidth)){
//                                dimenscore =dimenscoreremaining-(yellowminwidth+greenminwidth);
//                            } else if(dimenscore<redminwidth) {
//                                dimenscore = redminwidth;
//                            }
//                            dimenscoreremaining = dimenscoreremaining -dimenscore;
//                            textViewColorBlock_red.setMinimumWidth(dimenscore);
//                            textViewColorBlock_red.setVisibility(View.VISIBLE);
//
//                            if(BuildConfig.DEBUG){
//                                Log.e("MINMIN", headerTitle+ " - " + "dimenscorered: " + dimenscore);
//                                Log.e("MINMIN",headerTitle+ " - " +  "dimenscoreremaining: " + dimenscoreremaining + "/" + dimenscore_total);
//
//                            }
//
//
//                        }
//
//                        if(yellow>0){
//                            textViewColorBlock_yellow.setText(String.valueOf(yellow));
//                            int dimenscore = Math.round((dimenscore_total * ((float) yellow / (float) count)));
//                            if((dimenscoreremaining-dimenscore)<(greenminwidth)){
//                                dimenscore =dimenscoreremaining-(greenminwidth);
//                            } else if(dimenscore<yellowminwidth) {
//                                dimenscore = yellowminwidth;
//                            }
//                            dimenscoreremaining = dimenscoreremaining -dimenscore;
//
//                            if(BuildConfig.DEBUG){
//
//                                Log.e("MINMIN", headerTitle+ " - " + "dimenscoreyellow: " + dimenscore);
//                                Log.e("MINMIN", headerTitle+ " - " + "dimenscoreremaining: " + dimenscoreremaining +"/"+dimenscore_total);
//
//                            }
//
//
//                            textViewColorBlock_yellow.setMinimumWidth(dimenscore);
//                            textViewColorBlock_yellow.setVisibility(View.VISIBLE);
//
//                        }
//
//                        if(green>0){
//                            textViewColorBlock_green.setText(String.valueOf(green));
//                            int dimenscore = Math.round((dimenscore_total * ((float) green / (float) count)));
//
//                            if(BuildConfig.DEBUG) {
//                                Log.i("MINMIN","dimenscoretotal: " + dimenscore_total);
//                                Log.i("MINMIN","grey/count: " + green + "/" + count);
//                                Log.i("MINMIN","((float) grey / (float) count): " + ((float) green / (float) count));
//                                Log.i("MINMIN","Rounded score: " + dimenscore);
//
//                            }
//
//                            if((dimenscoreremaining- dimenscore )<0 ){
//                                dimenscore =dimenscoreremaining;
//                            } else if(dimenscore<greenminwidth) {
//                                dimenscore = greenminwidth;
//                            }
//
//                            textViewColorBlock_green.setMinimumWidth(dimenscore);
//                            textViewColorBlock_green.setVisibility(View.VISIBLE);
//
//                            if(BuildConfig.DEBUG){
//                                Log.e("MINMIN", headerTitle+ " - " + "dimenscoregreen: " + dimenscore);
//                                Log.e("MINMIN",headerTitle+ " - " +  "dimenscoreremaining: " + dimenscoreremaining + "/" + dimenscore_total);
//
//                            }
//
//                        }
//
//
//                    }
//
//                    if (count == 0) {
//                        textViewColorBlock_grey.setVisibility(View.VISIBLE);
//                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                            drawablecolorblock1.setColorFilter(ContextCompat.getColor(_context, android.R.color.white), PorterDuff.Mode.MULTIPLY);
//                            textViewColorBlock_grey.setBackground(drawablecolorblock1);
//                        } else {
//                            drawablecolorblock1.setColorFilter(ContextCompat.getColor(_context, android.R.color.white), PorterDuff.Mode.MULTIPLY);
//                            textViewColorBlock_grey.setBackgroundDrawable(drawablecolorblock1);
//                        }
//                        textViewColorBlock_grey.setText("empty");
//
//                        textViewColorBlock_red.setVisibility(View.GONE);
//                        textViewColorBlock_yellow.setVisibility(View.GONE);
//                        textViewColorBlock_green.setVisibility(View.GONE);
//                    }
//
//
//                }
//
//            }
//
//        }
//
//        if(BuildConfig.DEBUG){Log.d(TAG,"dimenscore at end of group: " + dimenscore_total);}
//        return convertView;
//    }
//
//    @Override
//    public boolean hasStableIds() {
//        return false;
//    }
//
//    @Override
//    public boolean isChildSelectable(int groupPosition, int childPosition) {
//
//        return true;
//    }
//
//
//}
