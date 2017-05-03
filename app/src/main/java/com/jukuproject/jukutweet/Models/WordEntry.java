package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.jukuproject.jukutweet.Adapters.BrowseWordsAdapter;
import com.jukuproject.jukutweet.R;

/**
 * Container for information on a single Japanese Word contained in the Edict database (including
 * its quiz score contained in JScoreboard table in the database)
 */
public class WordEntry implements Parcelable {

    private Integer id;
    private String kanji;
    private String furigana;
    private String definition;
    private Integer correct;
    private Integer total;
    private ItemFavorites itemFavorites;

    //Used when positioning a word in a broken-up sentence, and coloring it
    private String color;
    private int startIndex;
    private int endIndex;
    private String coreKanjiBlock;
    private double quizWeight;
    private boolean isSpinner;
    private FillinSentencesSpinner fillinSentencesSpinner;


    public WordEntry() {}

    public WordEntry(Integer id, String kanji, String furigana, String definition,Integer total, Integer correct) {
        this.id = id;
        this.kanji = kanji;
        this.furigana = furigana;
        this.definition = definition;
        this.total = total;
        this.correct = correct;
        this.itemFavorites = new ItemFavorites();
        this.isSpinner = false;
    }


    public WordEntry(Integer id
            , String kanji
            , String furigana
            , String definition
            , Integer total
            , Integer correct
            , String color
            , Integer startIndex
            , Integer endIndex) {
        this.id = id;
        this.kanji = kanji;
        this.furigana = furigana;
        this.definition = definition;
        this.total = total;
        this.correct = correct;
        this.itemFavorites = new ItemFavorites();
        this.color = color;
        this.startIndex  = startIndex;
        this.endIndex = endIndex;
        this.isSpinner = false;
    }

    public FillinSentencesSpinner getFillinSentencesSpinner() {
     if(fillinSentencesSpinner == null) {
         fillinSentencesSpinner = new FillinSentencesSpinner();
     }
        return fillinSentencesSpinner;
    }


    public void setSpinner(boolean isSpinner) {
        this.isSpinner = isSpinner;
    }

    public boolean isSpinner() {
        return isSpinner;
    }

    public String getColor() {
        return color;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }


    public ItemFavorites getItemFavorites() {
        if(itemFavorites==null) {
            itemFavorites = new ItemFavorites();
        }
        return itemFavorites;
    }

    public void setItemFavorites(ItemFavorites itemFavorites) {
        this.itemFavorites = itemFavorites;
    }


    public String getCoreKanjiBlock() {
        return coreKanjiBlock;
    }

    public void setCoreKanjiBlock(String coreKanjiBlock) {
        this.coreKanjiBlock = coreKanjiBlock;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKanji() {
        return kanji;
    }

    public void setKanji(String kanji) {
        this.kanji = kanji;
    }

    public String getFurigana() {
        if(furigana == null){
            return "";
        }
        return furigana;
    }
    public void setFurigana(String furigana) {
        this.furigana = furigana;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public float getPercentage() {
        try {
            return (float)correct/(float)total;
        } catch (NullPointerException e) {
            Log.e("Test-WordEntry", "Null pointer in Word Entry percentage calculation: " + e);
            return 0;
        }
    }


    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getCorrect() {
        return correct;
    }

    public void setCorrect(Integer correct) {
        this.correct = correct;
    }

    /**
     * Splits a definition in the Edict database, which comes in one long string punctuated by
     * parenthetic numbered chunks (ex: "(1)to go(2)to come") into a multiline string that can be used
     * in {@link BrowseWordsAdapter} or {@link com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog} definition
     *  etc etc
     * @param limit number of definitions to include
     * @return Multi-line string version of the definition
     */
    public String getDefinitionMultiLineString(int limit) {
        StringBuilder stringBuilder = new StringBuilder();

        if(definition != null) {
            for (int i=1; i<=limit; i++) {
                String s = "(" + String.valueOf(i) + ")";
                String sNext = "(" + String.valueOf(i+1) + ")";
                int slength = s.length();
                if(definition.contains(s)){
                    int endIndex = definition.length();
                    if(definition.contains(sNext)){ //If we can find the next "(#)" in the string, we'll use it as this definition's end point
                        endIndex =  definition.indexOf(sNext);
                    }

                    String sentence = definition.substring(definition.indexOf(s)+slength, endIndex);
                    //Capitalize it
                    if(sentence.length()>1) {
                        sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1).toLowerCase();
                    }
                    if(i>1) {
                        stringBuilder.append(System.getProperty("line.separator"));
                    }
                    stringBuilder.append("\u2022 ");
                    stringBuilder.append(sentence);
                } else if (i==1) { //if the thing doesn't contain a "(1)", just print the whole definition in line 1 of the array.
                    String sentence = definition;
                    if(sentence.length()>1) {
                        sentence = definition.substring(0, 1).toUpperCase() + definition.substring(1).toLowerCase();
                    }
                    stringBuilder.append("\u2022 ");
                    stringBuilder.append(sentence);
                }

            }
        }
        return stringBuilder.toString();
    }


    //TODO comment this out, and put try catch
    public String getFlashCardDefinitionMultiLineString(int limit) {
        StringBuilder stringBuilder = new StringBuilder();

        if(definition != null) {
            for (int i=1; i<=limit; i++) {
                String s = "(" + String.valueOf(i) + ")";
                String sNext = "(" + String.valueOf(i+1) + ")";
                int slength = s.length();
                if(definition.contains(s)){
                    int endIndex = definition.length();
                    if(definition.contains(sNext)){ //If we can find the next "(#)" in the string, we'll use it as this definition's end point
                        endIndex =  definition.indexOf(sNext);
                    }

                    String sentence = definition.substring(definition.indexOf(s)+slength, endIndex);
                    //Capitalize it
                    if(sentence.length()>1) {
                        sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1).toLowerCase();
                    }
                    if(i>1) {
                        stringBuilder.append(System.getProperty("line.separator"));
                        stringBuilder.append("\u2022 ");
                    } else  if(definition.contains("(2)")) {
                        stringBuilder.append("\u2022 ");
                    }

                    stringBuilder.append(sentence);
                } else if (i==1) { //if the thing doesn't contain a "(1)", just print the whole definition in line 1 of the array.
                    String sentence = definition;
                    if(sentence.length()>1) {
                        sentence = definition.substring(0, 1).toUpperCase() + definition.substring(1).toLowerCase();

                    }
                    if(definition.contains("(2)")) {
                        stringBuilder.append("\u2022 ");
                    }
                    stringBuilder.append(sentence);
                }

            }
        }

        return stringBuilder.toString();
    }

    /**
     * Assigns a color to the WordEntry based on the percentage of its quiz scores
     * @param colorThresholds Color Threshold object containing percentage thresholds used to assign the color
     */
    public void createColorForWord(ColorThresholds colorThresholds) {
        if (total != null && correct != null) {
            if (total < colorThresholds.getTweetGreyThreshold()) {
                color = "Grey";
            } else if (getPercentage() < colorThresholds.getRedThreshold()) {
                color = "Red";
            } else if (getPercentage() < colorThresholds.getYellowThreshold()) {
                color = "Yellow";
            } else {
                color = "Green";
            }
        } else {
            color = "Grey";
        }

    }

    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Get the color value for a WordEntry, used to fill in the colorbar in {@link BrowseWordsAdapter} for example
     * @return color value for WordEntry, or black if the word has no color
     */
    public int getColorValue() {
        if(color == null) {
            return android.R.color.white;
        } else if(color.equals("Grey")){
            return R.color.colorJukuGrey;
        }  else if(color.equals("Red")){
            return R.color.colorJukuRed;
        }  else if(color.equals("Yellow")){
            return R.color.colorJukuYellow;
        }  else if(color.equals("Green")){
            return R.color.colorJukuGreen;
        } else {
            return android.R.color.black;
        }

    }

    /**
     * Used in {@link com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment} and {@link com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment}
     * @param quizType Quiz menu option for multiple choice quiz ("Kanji to Definition" etc)
     * @return  Returns different parts of the word entry depending on the type of quiz
     */
    public String getQuizQuestion(String quizType) {
        switch(quizType) {

            case "Kanji to Definition":
                return kanji;
            case "Kanji to Kana":
                return kanji;
            case "Kana to Kanji":
                return furigana;
            case "Definition to Kanji":
                return getDefinitionMultiLineString(10);
            default:
                return kanji;

        }
    };

    /**
     * Used in {@link com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment} and {@link com.jukuproject.jukutweet.Fragments.FillInTheBlankFragment}
     * @param quizType Quiz menu option for multiple choice quiz ("Kanji to Definition" etc)
     * @return  Returns different parts of the word entry depending on the type of quiz
     */
    public String getQuizAnswer(String quizType) {
        switch(quizType) {

            case "Kanji to Definition":
                return definition;
            case "Kanji to Kana":
                return furigana;
            case "Kana to Kanji":
                return kanji;
            case "Definition to Kanji":
                return kanji;
            default:
                return definition;

        }
    };

    public double getQuizWeight() {
        return quizWeight;
    }

    public void setQuizWeight(double quizWeight) {
        this.quizWeight = quizWeight;
    }

    private WordEntry(Parcel in) {
        id = in.readInt();
        kanji = in.readString();
        furigana = in.readString();
        definition = in.readString();
        correct = in.readInt();
        total = in.readInt();
        color = in.readString();
        startIndex = in.readInt();
        endIndex = in.readInt();
        quizWeight = in.readDouble();
        itemFavorites = (ItemFavorites) in.readParcelable(ItemFavorites.class.getClassLoader());
        fillinSentencesSpinner = (FillinSentencesSpinner) in.readParcelable(FillinSentencesSpinner.class.getClassLoader());
        isSpinner = in.readByte() != 0;
        coreKanjiBlock = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(kanji);
        out.writeString(furigana);
        out.writeString(definition);
        out.writeInt(correct);
        out.writeInt(total);
        out.writeString(color);
        out.writeInt(startIndex);
        out.writeInt(endIndex);
        out.writeDouble(quizWeight);
        out.writeParcelable(itemFavorites,flags);
        out.writeParcelable(fillinSentencesSpinner,flags);
        out.writeByte((byte) (this.isSpinner ? 1 : 0));
        out.writeString(coreKanjiBlock);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<WordEntry> CREATOR = new Parcelable.Creator<WordEntry>() {
        public WordEntry createFromParcel(Parcel in) {
            return new WordEntry(in);
        }

        public WordEntry[] newArray(int size) {
            return new WordEntry[size];
        }
    };





}
