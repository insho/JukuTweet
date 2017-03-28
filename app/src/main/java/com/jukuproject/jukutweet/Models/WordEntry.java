package com.jukuproject.jukutweet.Models;

import android.os.Parcel;
import android.os.Parcelable;


public class WordEntry implements Parcelable {

    private Integer id;
    private String kanji;
    private String furigana;
    private String definition;
    private float percentage;
    private Integer total;

    public ItemFavorites getItemFavorites() {
        return itemFavorites;
    }

    public void setItemFavorites(ItemFavorites itemFavorites) {
        this.itemFavorites = itemFavorites;
    }

    private ItemFavorites itemFavorites;

    public WordEntry(Integer id, String kanji, String furigana, String definition,Integer total, float percentage ) {
        this.id = id;
        this.kanji = kanji;
        this.furigana = furigana;
        this.definition = definition;
        this.total = total;
        this.percentage = percentage;
        this.itemFavorites = new ItemFavorites();
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
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public WordEntry() {}

//    public ArrayList<String> getDefinitionArray() {
//        ArrayList<String> definitionArray = new ArrayList<>();
//
//        if(definition != null) {
//            for (int i=1; i<=20; i++) {
//                String s = "(" + String.valueOf(i) + ")";
//                String sNext = "(" + String.valueOf(i+1) + ")";
//                int slength = s.length();
//                if(definition.contains(s)){
//                    int endIndex = definition.length();
//                    if(definition.contains(sNext)){ //If we can find the next "(#)" in the string, we'll use it as this definition's end point
//                        endIndex =  definition.indexOf(sNext);
//                    }
//
//                    String sentence = definition.substring(definition.indexOf(s)+slength, endIndex);
//                    //Capitalize it
//                    if(sentence.length()>1) {
//                        sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1).toLowerCase();
//                    }
//                    definitionArray.add(sentence);
//                } else if (i==1) { //if the thing doesn't contain a "(1)", just print the whole definition in line 1 of the array.
//                    String sentence = definition;
//                    if(sentence.length()>1) {
//                        sentence = definition.substring(0, 1).toUpperCase() + definition.substring(1).toLowerCase();
//                    }
//                    definitionArray.add(sentence);
//                }
//
//            }
//        }
//
//        return definitionArray;
//    }

  //TODO comment this out, and put try catch
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
                    if(i>1) {
                        stringBuilder.append(System.getProperty("line.separator"));
                    }
                    stringBuilder.append("\u2022 ");
                    stringBuilder.append(sentence);
                }

            }
        }

        return stringBuilder.toString();
    }

    private WordEntry(Parcel in) {
        id = in.readInt();
        kanji = in.readString();
        furigana = in.readString();
        definition = in.readString();
        percentage = in.readFloat();
        total = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(kanji);
        out.writeString(furigana);
        out.writeString(definition);
        out.writeFloat(percentage);
        out.writeInt(total);
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
