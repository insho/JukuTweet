package com.jukuproject.jukutweet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.ItemFavorites;
import com.jukuproject.jukutweet.Models.ParseSentenceMatchCombination;
import com.jukuproject.jukutweet.Models.ParseSentencePossibleKanji;
import com.jukuproject.jukutweet.Models.Tweet;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.Models.WordLoader;
import com.vdurmont.emoji.EmojiManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Takes a sentence (or piece of text), finds the kanji in it and creates an array of those kanji
 * as {@link WordEntry} objects
 *
 * @see com.jukuproject.jukutweet.Fragments.TweetBreakDownFragment
 * @see MainActivity#parseAndSaveTweet(Tweet)
 */

public class TweetParser {

    private static String TAG = "TEST-tweetparse";
    private String entireSentence;
    private WordLoader wordLoader;
    private ColorThresholds mColorThresholds;
    private Context mContext;
    private HashMap<String, String> VerbChunksAndPositions = new HashMap<>();
    private ArrayList<ParseSentencePossibleKanji> possibleKanjiInSentence;
    private final int minKanjiLengthtoSplit = 2; //Smallest # of characters in a kanji combo for the kanji breakup builder to try splitting

    public static TweetParser getInstance() {
        return new TweetParser();
    }

    public ArrayList<WordEntry> parseSentence(Context context
            ,String entireSentence
            , ArrayList<String> spansToExclude
            ,ColorThresholds colorThresholds
    ) {
        this.mContext = context;
        this.entireSentence = entireSentence;
        this.mColorThresholds = colorThresholds;
        this.wordLoader = InternalDB.getInstance(mContext).getWordLists();
        possibleKanjiInSentence = findCoreKanjiBlocksInSentence(entireSentence,wordLoader,spansToExclude);

        if(BuildConfig.DEBUG){
            Log.d(TAG, "whole sentence: " + entireSentence);
            Log.d(TAG, "# of Kanji found: " + possibleKanjiInSentence.size());
            for(ParseSentencePossibleKanji possibleKanji : possibleKanjiInSentence) {
                Log.d(TAG,"(" + possibleKanji.getListIndex() + ") " + possibleKanji.getKanji());
            }
            Log.i(TAG,"LOADING UP THE PREFIX, SUFFIX AND VERB CONJUGATION COMBOS ");
        }
        attachPrefixesandSuffixesToCoreKanji(possibleKanjiInSentence);
        if(BuildConfig.DEBUG){
            Log.i(TAG,"CREATING PREFIX/SUFIX COMBOS, ITERATING THROUGH THEM, IF MATCH FOUND ADDING TO FINAL");
        }
        createBetterMatchesForPossibleKanji(possibleKanjiInSentence);
        if(BuildConfig.DEBUG){Log.i(TAG,"CHECKING/CHOPPING POSSIBLE VERB COMBOS, ADDING TO KANJIFINALARRAY");}
        ArrayList<Integer> cleanKanjiIds = getCleanKanjiIDsFromBetterMatches(possibleKanjiInSentence);
        return compileFinalSentenceMap(cleanKanjiIds);
    }





    /** Creates an array list of core kanji in the sentence and their positions.
     *  The result array is built by adding different combinations of hiragana/katakana/symbols to these core kanji .
     *
     * @param entireSentence Sentence or piece of text to be split
     * @param wordLoader Arrays and Maps of hiragana/katakana/symbols/verbendings. Used to determine whether a character (or possible verb ending) is a Kanji or conjugated verb.
    //     * @param kanjPositionArray Position indexes of the "spinner" kanji for each question (if this is a FillinSentences Activity). These kanji are known initially
     *                          so it is unnecessary to break them down or match them against the dictionary
     * @return An array list of ParseSentencePossibleKanji, representing the core of each possible kanji in the sentence
     */
    private static ArrayList<ParseSentencePossibleKanji> findCoreKanjiBlocksInSentence(String entireSentence, WordLoader wordLoader, ArrayList<String> spansToExclude) {
        ArrayList<Integer> IndexPositionsToExclude = getExcludedSpanIndexes(entireSentence,spansToExclude);
        ArrayList<ParseSentencePossibleKanji> possibleKanjiInSentence = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        StringBuilder builder_katakana = new StringBuilder();
        String char_aPrev = "";

        for (int i = 0; i < entireSentence.length(); i++) {
            String char_a = String.valueOf(entireSentence.charAt(i));
            String char_aNext = "";
            if(entireSentence.length()>(i+1)) {
                char_aNext = String.valueOf(entireSentence.charAt(i+1));
            }

            /* If it is an exclude span item (url, etc), ignore it */
            if (IndexPositionsToExclude != null && IndexPositionsToExclude.contains(i)) {
                addOrReleaseBuilderContents(i,builder,possibleKanjiInSentence);
                addOrReleaseBuilderContents(i,builder_katakana,possibleKanjiInSentence,true);

            } else if (wordLoader.getKatakana().contains(char_a)) {
                /* Determine if the character is a kanji by process of elimination. If it is not Hiragana, Katakana or a Symbol, it must be a kanji */
                builder_katakana.append(entireSentence.charAt(i));
                addOrReleaseBuilderContents(i,builder,possibleKanjiInSentence);

            } else if (!EmojiManager.isEmoji(char_a + char_aNext)
                    && !EmojiManager.isEmoji(char_aPrev + char_a)
                    && !(char_a.equals(System.getProperty("line.separator")))
                    && !isAlphaNumericChar(char_a)
                    && !wordLoader.getHiragana().contains(char_a)
                    && !wordLoader.getSymbols().contains(char_a)) {
                builder.append(entireSentence.charAt(i));

                Log.d(TAG,"builder appending: " + entireSentence.charAt(i));

                addOrReleaseBuilderContents(i,builder_katakana,possibleKanjiInSentence,true);
            }   else {
                Log.d(TAG,"NOT RECORDING : " + entireSentence.charAt(i));

                /*Add the contents of any full builder to the possibleKanji list, and do not record this character */
                addOrReleaseBuilderContents(i,builder,possibleKanjiInSentence);
                addOrReleaseBuilderContents(i,builder_katakana,possibleKanjiInSentence,true);
            }
            char_aPrev = char_a;
        }

        /*If there are any leftover items in the builder, release them at the end
         This would occur if there is a kanji that ends the tweet */

        addOrReleaseBuilderContents(entireSentence.length(),builder,possibleKanjiInSentence);
        addOrReleaseBuilderContents(entireSentence.length(),builder_katakana,possibleKanjiInSentence,true);

        return possibleKanjiInSentence;
    }


    /**
     * Checks whether a given string is alphanumeric
     * @param s string (a single character from the sentence)
     * @return true if it is alphanumeric, false if not
     */
    private static boolean isAlphaNumericChar(String s) {
        return !s.matches("^.*[^\\\\dA-Za-z0-9].*$");
    }


    /**
     * During {@link #findCoreKanjiBlocksInSentence(String, WordLoader, ArrayList)} method, is called to add the contents of the stringBuilder
     * as a new {@link ParseSentencePossibleKanji} item (when it is determined that a possible kanji might have been found)
     * @param index index of possible kanji in sentence
     * @param builder stringbuilder with possible kanji
     * @param possibleKanjiInSentence list of possible kanji in sentence, which the contents of the stringbuilder is added to
     *
     * @see #findCoreKanjiBlocksInSentence(String, WordLoader, ArrayList)
     */
    public static void addOrReleaseBuilderContents(Integer index, StringBuilder builder, ArrayList<ParseSentencePossibleKanji> possibleKanjiInSentence) {
        if (builder.length() > 0) {
            possibleKanjiInSentence.add(new ParseSentencePossibleKanji(index,possibleKanjiInSentence.size(),builder.toString()));
            builder.setLength(0);
        }
    }

    /**
     * During {@link #findCoreKanjiBlocksInSentence(String, WordLoader, ArrayList)} method, is called to add the contents of the stringBuilder
     * as a new {@link ParseSentencePossibleKanji} item (when it is determined that a possible kanji might have been found)
     * @param index index of possible kanji in sentence
     * @param builder stringbuilder with possible kanji
     * @param possibleKanjiInSentence list of possible kanji in sentence, which the contents of the stringbuilder is added to
     * @param isKatakana bool true if the contents of the builder are katakana items (which must be marked when the possible kanji is added)
     * @see #findCoreKanjiBlocksInSentence(String, WordLoader, ArrayList)
     */
    private static void addOrReleaseBuilderContents(Integer index, StringBuilder builder, ArrayList<ParseSentencePossibleKanji> possibleKanjiInSentence, Boolean isKatakana) {
        if (builder.length() > 0) {
            possibleKanjiInSentence.add(new ParseSentencePossibleKanji(index,possibleKanjiInSentence.size(),builder.toString(),isKatakana));
            builder.setLength(0);
        }
    }

    /** Load up lists of possible prefix, suffix and verb conjugations for each kanji
     *
     * @param possibleKanjiInSentence Array of ParseSentencePossibleKanji objects, representing possible kanji within the sentence
     *                                (intial core kanji, furigana, position, better matches for kanji etc)
     */
    private void attachPrefixesandSuffixesToCoreKanji(ArrayList<ParseSentencePossibleKanji> possibleKanjiInSentence) {
        for (int i = 0; i < possibleKanjiInSentence.size(); i++) {

            int prevposition; //Initially set the prevposition to 0 (to avoid out of index errors)
            if (i > 0) {
                prevposition = possibleKanjiInSentence.get(i - 1).getPositionInSentence();
            } else {
                prevposition = 0;
            }
            if(BuildConfig.DEBUG){Log.d(TAG, "CURRENT kanji: " + possibleKanjiInSentence.get(i).getKanji());}

            /* For FillInTheBlanks, if the current Kanji is a designated SpinnerKanji, do not try to attach prefixes or suffixes*/
            ArrayList<String> suffixes = new ArrayList<>();
            ArrayList<String> prefixes = new ArrayList<>();

            int hiraganalength = findTrailingHiraganaLength(i,entireSentence.length(),possibleKanjiInSentence);
            if(BuildConfig.DEBUG){
                Log.d(TAG, possibleKanjiInSentence.get(i).getKanji() + " hiraganalength: " + hiraganalength);
                Log.d(TAG,possibleKanjiInSentence.get(i).getKanji() +  " currentposition: " + possibleKanjiInSentence.get(i).getPositionInSentence());
            }


            if (hiraganalength > possibleKanjiInSentence.get(i).getPositionInSentence()) {
                /* The maximum number of characters for a possible suffix/prefix should be 9. That's enough to catch
                * any long prefix/suffixes or compound characters */
                if(hiraganalength>(possibleKanjiInSentence.get(i).getPositionInSentence()+9)) {
                     hiraganalength =    possibleKanjiInSentence.get(i).getPositionInSentence()+9;
                }
                if(possibleKanjiInSentence.get(i).getPositionInSentence() - prevposition>9) {
                    prevposition = possibleKanjiInSentence.get(i).getPositionInSentence() - 9;
                }
                suffixes = fillTheSuffixLists(possibleKanjiInSentence.get(i),hiraganalength,wordLoader);
                prefixes = fillThePrefixList(prevposition,possibleKanjiInSentence.get(i));
                if(BuildConfig.DEBUG){Log.d(TAG, "Adding kanji chunk to kanjifinal_HashMap (at pos): " + i + " - " + possibleKanjiInSentence.get(i).getKanji());}
            }
            possibleKanjiInSentence.get(i).setPrefixes(prefixes);
            possibleKanjiInSentence.get(i).setSuffixes(suffixes);
        }
    }


    /**
     * Take the position of the next kanji (inclusive of that kanji), and subtract its length to find the position of the hiragana between kanjis (to
     * essentially identify conjugated verbs or compound kanji). If the sentence ends with hiragana, return the entire sentence length as the end position
     *
     * @param indexOfCurrentPossibleKanji indexnumber of possible Kanji in Sentence within the possibleKanjiInSentence array
     * @param entireSentenceLength Length of entire sentence
     * @param possibleKanjiInSentence Array of possible kanji within sentence. Needed to find
     * @return length of hiragana after the current possibleKanji object
     *
     * @see #attachPrefixesandSuffixesToCoreKanji(ArrayList)
     */
    private static int findTrailingHiraganaLength(int indexOfCurrentPossibleKanji, int entireSentenceLength, ArrayList<ParseSentencePossibleKanji> possibleKanjiInSentence) {

        if ((indexOfCurrentPossibleKanji + 1) < possibleKanjiInSentence.size()) {
            String nextkanji = possibleKanjiInSentence.get(indexOfCurrentPossibleKanji + 1).getKanji();
            return possibleKanjiInSentence.get(indexOfCurrentPossibleKanji + 1).getPositionInSentence() - nextkanji.length();
        } else {
            return entireSentenceLength;
        }

    }

    /**
     * Creates a list of possible suffixes, or conjugated verb endings that come after a core kanji. The suffixes are added to the suffixes element of
     * the possibleKanji object. The possible verb endings are added to the VerbChunksAndPositions list.
     *
     * @param possibleKanji ParseSentencePossibleKanji object, representing possible kanji within the sentence
     * @param hiraganalength Length of hiragana characters following the possiblKanji Object in the sentence
     * @param wordLoader Arrays and Maps of hiragana/katakana/symbols/verbendings. Used to determine whether a character (or possible verb ending) is a Kanji or conjugated verb.
     * @return A list of possible suffixes for the possibleKanji object, to be attached to the possibleKanji.suffixes element
     *
     * @see #attachPrefixesandSuffixesToCoreKanji(ArrayList)
     */
    private ArrayList<String> fillTheSuffixLists(ParseSentencePossibleKanji possibleKanji, int hiraganalength, WordLoader wordLoader ) {

        StringBuilder builder_hiragana = new StringBuilder();
        ArrayList<String> suffixes = new ArrayList<>();

        int extendedHiraganaLength = extendedHiraganaLength(hiraganalength,entireSentence.length(),possibleKanji.getPositionInSentence());

        /* This fills the suffixes list, while also filling the possible verb conjugations list */
        for (int j = possibleKanji.getPositionInSentence(); j < extendedHiraganaLength; j++) {

            builder_hiragana.append(entireSentence.charAt(j)); // add to the builder

            /*If the current char is NOT a kanji, but the builder isn't empty either, move the builder contents to the final list and empty builder */
            if (builder_hiragana.length() > 0) {

                String possibleSuffix = builder_hiragana.toString();
                suffixes.add(possibleSuffix);
                if(BuildConfig.DEBUG){Log.d(TAG, "(" + possibleKanji.getKanji() + ") possible suffix: " + possibleSuffix);}

                /* If there is a KanjiChunk + verb suffix match, add it to the possible verb combos map*/
                if(wordLoader.getVerbEndingsConjugation().contains(possibleSuffix)) {

                    for (int k = 0; k < wordLoader.getVerbEndingsRoot().size(); k++) {
                        String root = wordLoader.getVerbEndingsRoot().get(k);
                        String conjugation = wordLoader.getVerbEndingsConjugation().get(k);
                        if (conjugation.equalsIgnoreCase(possibleSuffix)) {
                            possibleKanji.addToVerbCombos(possibleKanji.getKanji() + root);
                            VerbChunksAndPositions.put(possibleKanji.getKanji() + root, possibleKanji.getKanji());
                            if(BuildConfig.DEBUG){
                                Log.d(TAG, "Kanjifinal POSSIBLE Verb match: " + possibleKanji.getKanji() + possibleSuffix);
                                Log.d(TAG, "Adding to kanjifinal_HashMap_Verbs: - " + possibleKanji.getKanji() + root);
                            }
                        }
                    }
                }
            }
        }
        return suffixes;
    }

    /** Hiragana length captures possible suffixes that follow the core kanji, but sometimes there are compound words that
     * have [Core Kanji] + [Suffix] + [Another Core Kanji] + [Suffix]  ... So we extend the hiragana length out another five characters if possible,
     * in order to catch these double compound words
     *
     * @param hiraganalength Length of hiragana characters following the possiblKanji Object in the sentence
     * @param entireSentenceLength The length of the sentence or piece of text to be split
     * @param kanjiPositioninSentence position of the (start of the) kanji block in the sentence
     * @return An int representing the number of trailing characters to be included in the search for possible compound kanji
     *
     * @see #fillTheSuffixLists(ParseSentencePossibleKanji, int, WordLoader)
     */
    private static int extendedHiraganaLength(int hiraganalength, int entireSentenceLength, int kanjiPositioninSentence) {
        if(hiraganalength > kanjiPositioninSentence && (hiraganalength - kanjiPositioninSentence)<4) {
            if(hiraganalength + 5 > entireSentenceLength) {
                return entireSentenceLength;
            } else {
                return hiraganalength + 5;
            }
        } else {
            return hiraganalength;
        }
    }

    /**
     * @param prevposition position of the end of the previous kanji block. The iteration starts from the beginning of the block and movies
     *                     backwards to this prevposition to build the prefix
     * @param possibleKanji ParseSentencePossibleKanji object, representing possible kanji within the sentence
     * @return A list of possible prefixes for the possibleKanji object, to be attached to the possibleKanji.prefixes element
     *
     * @see #attachPrefixesandSuffixesToCoreKanji(ArrayList)
     */
    private ArrayList<String> fillThePrefixList(int prevposition, ParseSentencePossibleKanji possibleKanji) {

        StringBuilder builder_hiragana_prev = new StringBuilder();
        ArrayList<String> prefixes = new ArrayList<>();

        /* This chunk adds prefixes to each kanji, looking for larger matches in the DB */
        if (possibleKanji.getPositionInSentence() - possibleKanji.getKanji().length() > 0) {

            for (int j = possibleKanji.getPositionInSentence() - possibleKanji.getKanji().length() - 1; j > (prevposition - 1); j--) {
                builder_hiragana_prev.insert(0, entireSentence.charAt(j)); // add to the builder

                /*If the current char is NOT a kanji, but the builder isn't empty either, move the builder contents to the final list and empty builder */
                if (builder_hiragana_prev.length() > 0) {
                    String string = builder_hiragana_prev.toString();
                    prefixes.add(string);
                    if(BuildConfig.DEBUG){Log.d(TAG, "(" + possibleKanji.getKanji() + ") possible prefix: " + string);}
                }
            }
        }
        return prefixes;
    }

    /**
     * Search each combination of broken up kanji against the DB, to see if there is a full match for each piece
     * @param possibleKanji ParseSentencePossibleKanji object, representing possible kanji within the sentence
     * @param brokenUpKanjiCombinations Arraylist containing arrays of different combinations (coming from the chopKanjiIntoDifferentCombinations method). The arrays will then be compared against the dictionary.
     * @param isFinalMatching  Boolean value determining whether to match verb combinations. BECAUSE, this method is called twice,
     *                          and while the first time we DO want to look for verb combos, the 2nd time we DO NOT. This boolean is passed onto the
     *                          boolean "lookForVerbCombos" in the setMatchSuffixes child method
     * @param cleanKanjiIDs list of finalized ids for the kanji contained in the sentence
     *
     * @see #addEntrytoFinalKanjiIDs(ParseSentencePossibleKanji, ArrayList)
     * @see #chopandCompare(ParseSentencePossibleKanji)
     */
    private void matchKanjiPiecesAgainstDB(ParseSentencePossibleKanji possibleKanji, ArrayList<ArrayList<String>> brokenUpKanjiCombinations, boolean isFinalMatching, @Nullable ArrayList<Integer> cleanKanjiIDs) {

        boolean shutoff = false;
        for (int m = 0; m < brokenUpKanjiCombinations.size() && !shutoff ; m++) {

            ArrayList<String> possibleCombination = brokenUpKanjiCombinations.get(m);
            ParseSentenceMatchCombination matchCombination = getCountofMatchingPieces(possibleCombination);
            if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(1) breakuparray combos size: " + possibleCombination.size());}
            /* If all the pieces have a DB match move them on to the next step
             * OR, if all but ONE have a match, try looking for suffixes/prefixes for that one. Maybe there's a match */
            if (matchCombination.getMatchCount() == possibleCombination.size() || (matchCombination.getMatchCount()>1 && possibleCombination.size()-matchCombination.getMatchCount()==1 && matchCombination.getNoMatchforFinalKanji())) {
                if(BuildConfig.DEBUG){Log.d(TAG,"BREAKUP(1) -- Success. Matches found for each broken up kanji");}

                /* Shut off the process if all combinations are accounted for */
                if(matchCombination.getMatchCount() == possibleCombination.size()) {
                    shutoff = true;
                }

                if(BuildConfig.DEBUG){Log.d(TAG,"BREAKUP(1) tmpStringArray size: " + matchCombination.getMatches().size());}
                for (int v = 0; v < matchCombination.getMatches().size(); v++) {

                    /* Check for PREFIX matches for the trailing kanji */
                    if(BuildConfig.DEBUG) {
                        Log.d(TAG, "BREAKUP(1) current breakup chunk string: " + matchCombination.getMatches().get(v));
                        Log.d(TAG, "BREAKUP(1) kanjibreakupArray.size() >0: " + (brokenUpKanjiCombinations.size() > 0));
                        Log.d(TAG, "BREAKUP(1) v = " + v);
                    }
                    if(brokenUpKanjiCombinations.size() >0 && v == 0) {
                        setMatchesPrefix(possibleKanji,possibleCombination,matchCombination);

                    }

                    /* Now check for suffix matches for the trailing kanji. If, for instance the sentence string is
                     *  KINOUWATASHTACHI, and we have split up the kanji into KINOU and WATASH, we must try to paste on the "TACHI" onto the end of WATASH, so the final
                     *  product is KINOU and WATASHTACHI... Same goes for prefixes on the first*/

                    if(brokenUpKanjiCombinations.size() >0 && v == (matchCombination.getMatches().size()-1) ) {
                        setMatchesSuffix(!isFinalMatching,possibleKanji,possibleCombination,matchCombination);
                    }

                    if(isFinalMatching && cleanKanjiIDs != null) {
                        cleanKanjiIDs.add(matchCombination.getMatchIDs().get(v));
                    } else {
                        possibleKanji.setBetterKanjiMatches(matchCombination.getMatches());
                    }
                }

            } else {
                matchCombination.getMatches().clear();
                matchCombination.getMatchIDs().clear();
            }

        }
    }

    /**
     *  Iterates through a possible combination of core kanji/prefix/suffix/verb ending for a Kanji in the sentence,
     *  loading up a ParseSentenceMatchCombination object with information for each of the pieces (their edict dictionary ids, etc)
     *
     * @param possibleCombination An array of a possible combination of a chopped up kanji, to be checked against the database
     * @return ParseSentenceMatchCombination object, representing a possible succesful match of a core kanji/prefix/suffix/verb ending
     *
     * @see #matchKanjiPiecesAgainstDB(ParseSentencePossibleKanji, ArrayList, boolean, ArrayList)
     */
    private ParseSentenceMatchCombination getCountofMatchingPieces(ArrayList<String> possibleCombination) {

        ParseSentenceMatchCombination combination = new ParseSentenceMatchCombination();
        int matchingPieces = 0;

        for (int index=0; index<possibleCombination.size();index++) {
            final String kanjiPiece = possibleCombination.get(index);
            Cursor cursorMatchPieceAgainstDB  = cursorMatchStringAgainstDB(kanjiPiece);
            if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(1): SELECT FROM [Edict_FTS] WHERE [Kanji] MATCH " + kanjiPiece + ":count: " + cursorMatchPieceAgainstDB.getCount());}
            if (cursorMatchPieceAgainstDB.getCount() > 0) {
                cursorMatchPieceAgainstDB.moveToFirst();
                combination.addMatches(kanjiPiece);
                combination.addMatchID(cursorMatchPieceAgainstDB.getInt(0));
                combination.addMatchCount();
                if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(1) found breakup match: " + kanjiPiece + ", " + matchingPieces + "/" + possibleCombination.size());}
            } else if (index == possibleCombination.size()-1) {
                /* If it's the last kanji in the group, and there were matches for all the other pieces, and there were no matches for this last one, maybe it's a verb */
                combination.setNoMatchforFinalKanji(true);
                if(possibleCombination.size()-matchingPieces==1) {
                    combination.addMatches(kanjiPiece);
                    if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(1) no match for final entry: " + kanjiPiece + ", Adding it anyway...");}
                }
            }
            cursorMatchPieceAgainstDB.close();
        }
        return combination;

    }

    /** Matches a possible kanji against the dictionary
     *
     //     * @param db Sqlite database connection
     * @param kanji possible Kanji
     * @return Cursor
     */
    private Cursor cursorMatchStringAgainstDB(String kanji) {
        return InternalDB.getInstance(mContext).getWritableDatabase().rawQuery("SELECT [_id],[Kanji] FROM [Edict] WHERE _id in(SELECT docid FROM [Edict_FTS] WHERE [Kanji] MATCH ?)  ORDER BY [Common] LIMIT 1", new String[]{kanji});
    }

    /**
     * Searches dictionary for matches with prefix + core kanji. Ex: "お" + "元気" = "お元気"
     * If match exists, add it to the ParseSentenceMatchCombination
     *
     * @param possibleKanji ParseSentencePossibleKanji object, representing possible kanji within the sentence
     * @param possibleCombination Array of a possible combination of a chopped up kanji, to be checked against the database
     * @param matchCombination input ParseSentenceMatchCombination object, representing a broken up core kanji, to be matched against the dictionary
     * @return  Updated ParseSentenceMatchCombination object, representing one, or even several, correctly matched kanji within the sentence. Returnvalue for testing.
     *
     * @see #matchKanjiPiecesAgainstDB(ParseSentencePossibleKanji, ArrayList, boolean, ArrayList)
     */
    private ParseSentenceMatchCombination setMatchesPrefix(ParseSentencePossibleKanji possibleKanji, ArrayList<String> possibleCombination, ParseSentenceMatchCombination matchCombination) {

        int kanjibreakupArraySize = possibleCombination.size();
        for(int w = 0;w<kanjibreakupArraySize;w++) {

            if(possibleCombination.size()>1) {

                String firstkanji = possibleCombination.get(0);
                if(BuildConfig.DEBUG){Log.d(TAG,"BREAKUP(1) firstkanji = " + possibleCombination.get(0));}

                if (possibleKanji.getPrefixes().size() > 0) {

                    for (int xx = 0; xx < possibleKanji.getPrefixes().size(); xx++) {
                        Cursor dd = InternalDB.getInstance(mContext).getWritableDatabase().rawQuery("SELECT [Kanji],_id FROM [Edict] WHERE _id in(SELECT docid FROM [Edict_FTS] WHERE [Kanji] MATCH ?)  ORDER BY [Common] LIMIT 1", new String[]{possibleKanji.getPrefixes().get(xx) + firstkanji});
                        if(BuildConfig.DEBUG){
                            Log.d(TAG,"BREAKUP(1) (prefix) trying: " + possibleKanji.getPrefixes().get(xx) + firstkanji);
                            Log.d(TAG, "BREAKUP(1) SELECT [Kanji] FROM [Edict_FTS] WHERE [Kanji] MATCH " + possibleKanji.getPrefixes().get(xx) + firstkanji + ":count: " + dd.getCount());
                        }
                        if (dd.getCount() > 0) {
                            dd.moveToFirst();
                            if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(1) found extra match: (" + dd.getString(0) + ") " + possibleKanji.getPrefixes().get(xx) + firstkanji);}
                            if(dd.getString(0).length() > matchCombination.getMatches().get(0).length()) {
                                xx = possibleKanji.getPrefixes().size(); // THIS ENDS THE PROCESS
                                if(BuildConfig.DEBUG){Log.d(TAG,"BREAKUP(1) removing: " + matchCombination.getMatches().get(0) + ", adding: " + dd.getString(0));}

                                ArrayList<String> updatedKanji = new ArrayList<>();
                                ArrayList<Integer> updatedIDs = new ArrayList<>();
                                matchCombination.getMatches().remove(0);

                                updatedKanji.add(dd.getString(0));
                                updatedIDs.add(dd.getInt(1));

                                for(int e = 0; e<matchCombination.getMatches().size();e++) {
                                    updatedKanji.add(matchCombination.getMatches().get(e));
                                    updatedIDs.add(matchCombination.getMatchIDs().get(e));
                                }
                                matchCombination.setMatches(updatedKanji);
                                matchCombination.setMatchIDs(updatedIDs);
                            }
                        }
                        dd.close();
                    }

                }

            }
        }
        return matchCombination;
    }


    /**
     * Searches dictionary for matches with core kanji + suffix OR core kanji + verb ending. Ex: "暖" + "かい" = 暖かい
     *                                                                                       Ex: "失" + "う" = "失う"
     * If match exists, add it to the ParseSentenceMatchCombination
     *
     * @param lookForVerbCombos Boolean value determining whether match verb combinations. BECAUSE, the parent method "matchKanjiPiecesAgainstDB" is called twice,
     *                          and while the first time we DO want to look for verb combos, the 2nd time we DO NOT. So this boolean is the opposite of the
     *                          boolean "isFinalMatching" in the parent
     * @param possibleKanji ParseSentencePossibleKanji object, representing possible kanji within the sentence
     * @param possibleCombination An array of a possible combination of a chopped up kanji, to be checked against the database
     * @param matchCombination input ParseSentenceMatchCombination object, representing a broken up core kanji, to be matched against the dictionary
     * @return Updated ParseSentenceMatchCombination object, representing one, or even several, correctly matched kanji within the sentence. Returnvalue for testing.
     *
     * @see #matchKanjiPiecesAgainstDB(ParseSentencePossibleKanji, ArrayList, boolean, ArrayList)
     */
    private ParseSentenceMatchCombination setMatchesSuffix(boolean lookForVerbCombos, ParseSentencePossibleKanji possibleKanji, ArrayList<String> possibleCombination, ParseSentenceMatchCombination matchCombination) {
        int kanjibreakupArraySize = possibleCombination.size();
        for(int w = 0;w<kanjibreakupArraySize;w++) {

            if(possibleCombination.size()>1) {

                /* Filling in the first x number of entries for the broken up array. Leaving the last one out. We will then input the last entry + suffix into the kanjibreakuparray*/
                String lastkanji = possibleCombination.get(possibleCombination.size()-1);
                if(BuildConfig.DEBUG){Log.d(TAG,"BREAKUP(2) lastkanji  = " + possibleCombination.get(possibleCombination.size()-1));}
                /* Search Edict for matches with Kanji + suffixes, prefix + kanji, and prefix + kanji + suffix. If matche exists, book it and replace old kanji with new entry in final hashmap*/
                if (possibleKanji.getSuffixes().size() > 0) {
                    boolean shutoff = false;
                    for (int x = 0; x < possibleKanji.getSuffixes().size() && !shutoff; x++) {

                        Cursor cursorDBMatch2 = cursorMatchStringAgainstDB(lastkanji + possibleKanji.getSuffixes().get(x));

                        if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(2) () (suffix)SELECT [_id] FROM [Edict_FTS] WHERE [Kanji] MATCH " + lastkanji + possibleKanji.getSuffixes().get(x) + ":count: " + cursorDBMatch2.getCount());}
                        if (cursorDBMatch2.getCount() > 0) {
                            cursorDBMatch2.moveToFirst();
                            if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(2) found extra match: (" + cursorDBMatch2.getString(0) + ") " + lastkanji + possibleKanji.getSuffixes().get(x));}
                            shutoff = true; // THIS ENDS THE PROCESS
                            matchCombination.getMatchIDs().remove(matchCombination.getMatchIDs().size()-1);
                            matchCombination.getMatchIDs().add(cursorDBMatch2.getInt(0));

                            matchCombination.getMatches().remove(matchCombination.getMatches().size() - 1);
                            matchCombination.getMatches().add(cursorDBMatch2.getString(1));

                        } else if (lookForVerbCombos && wordLoader.getVerbEndingsConjugation().contains(possibleKanji.getSuffixes().get(x))) {


                            for (int k = 0; k < wordLoader.getVerbEndingsRoot().size(); k++) {
                                String root = wordLoader.getVerbEndingsRoot().get(k);
                                String conjugation = wordLoader.getVerbEndingsConjugation().get(k);
                                if (conjugation.equalsIgnoreCase(possibleKanji.getSuffixes().get(x))) {
                                    if(BuildConfig.DEBUG){Log.d(TAG, "Kanjifinal POSSIBLE BREAKUP(1) Verb match: " + lastkanji + possibleKanji.getSuffixes().get(x));}
                                    Cursor cursorMatchVerbInDB = InternalDB.getInstance(mContext).getWritableDatabase().rawQuery("SELECT[Kanji] FROM [Edict] WHERE _id in(SELECT docid FROM [Edict_FTS] WHERE [Kanji] MATCH ?)  ORDER BY [Common] LIMIT 1", new String[]{lastkanji + root});
                                    if(cursorMatchVerbInDB.getCount() > 0) {
                                        if((lastkanji + root).length() >= matchCombination.getMatches().get(matchCombination.getMatches().size() - 1).length()) {
                                            if (BuildConfig.DEBUG) {
                                                Log.d(TAG, "BREAKUP(1) removing: " + matchCombination.getLastMatch() + ", adding: " + lastkanji + root);
                                            }
                                            matchCombination.getMatches().remove(matchCombination.getMatches().size() - 1);
                                            matchCombination.getMatches().add(lastkanji + root);
                                            VerbChunksAndPositions.put(lastkanji + root,lastkanji );
                                        }
                                    }
                                    cursorMatchVerbInDB.close();
                                }
                            }

                        }

                        cursorDBMatch2.close();
                    }

                }


            }
        }
        return matchCombination;
    }

    /**
     * Creates various combinations of a kanji block. If it is impossible to create a match for a large kanji  (like + 3 characters), look for matches
     * by breaking up those characters into smaller sets (of at least 1 kanji). And if there is a match for every piece of the broken-up kanji, insert
     * those pieces into the kanjifinal_clean_integer
     *
     * @param coreKanjiBlock Kanji block to chop up
     * @return Arraylist containing arrays of different combinations. The arrays will then be compared against the dictionary.
     *
     * @see #chopandCompare(ParseSentencePossibleKanji)
     * @see #addEntrytoFinalKanjiIDs(ParseSentencePossibleKanji, ArrayList)
     */
    private ArrayList<ArrayList<String>> chopKanjiIntoDifferentCombinations(String coreKanjiBlock) {
        ArrayList<ArrayList<String>> kanjibreakupArray = new ArrayList<>();
        StringBuilder kanjibreakupBuilder = new StringBuilder();
        int divisor = coreKanjiBlock.length() - 1;
        if(BuildConfig.DEBUG){Log.d(TAG,"initial divisor: " + divisor);}
        int innerstart =0;
        int outerstart = 0;
        while (divisor >= 1) {

            ArrayList<String> tmp = new ArrayList<>();
            kanjibreakupBuilder.setLength(0);

            /* Attach the preceding straggling chars (outside of divisor chunks) */
            while ((innerstart + divisor) <= coreKanjiBlock.length() && !(innerstart == divisor && (innerstart + divisor) == coreKanjiBlock.length())) {

                for (int l = 0; l < innerstart; l++) {
                    kanjibreakupBuilder.append(coreKanjiBlock.charAt(l));
                }
                if(kanjibreakupBuilder.length()>0) {
                    if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(1) div(" + divisor + ") added pre-div chunk: " + kanjibreakupBuilder.toString());}
                    tmp.add(kanjibreakupBuilder.toString());
                    kanjibreakupBuilder.setLength(0);
                }

                for (int l = 0; l < divisor; l++) {
                    kanjibreakupBuilder.append(coreKanjiBlock.charAt(innerstart + l));
                }
                if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(1) div(" + divisor + ") added: " + kanjibreakupBuilder.toString());}
                tmp.add(kanjibreakupBuilder.toString());
                innerstart = innerstart + divisor;
                kanjibreakupBuilder.setLength(0);

                if(innerstart + divisor >= coreKanjiBlock.length()) {
                    /* Attach the following (last) straggling chars (outside of divisor chunks) */
                    for (int l = innerstart; l < coreKanjiBlock.length(); l++) {
                        kanjibreakupBuilder.append(coreKanjiBlock.charAt(l));
                    }
                    if(kanjibreakupBuilder.length()>0) {
                        if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(1) div(" + divisor + ") added post-div chunk: " + kanjibreakupBuilder.toString());}
                        tmp.add(kanjibreakupBuilder.toString());
                        kanjibreakupBuilder.setLength(0);
                    }
                    kanjibreakupArray.add(tmp);
                }
            }

            if(outerstart + divisor > coreKanjiBlock.length()) {
                divisor = divisor - 1;
                outerstart = 0;
                innerstart = 0;
                if(BuildConfig.DEBUG){Log.d(TAG," new divisor: " + divisor);}
            } else {
                outerstart = outerstart +1;
                innerstart = outerstart;
            }

        }
        if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(2) kanjibreakupArray size: " + kanjibreakupArray.size());}
        return kanjibreakupArray;
    }


    /**
     * @param possibleKanji ParseSentencePossibleKanji object, representing possible kanji within the sentence
     * @return true if verb infinitive exists in DB, false if it doesn't
     *
     * @see #chopandCompare(ParseSentencePossibleKanji)
     */
    private boolean verbInfinitiveExists( ParseSentencePossibleKanji possibleKanji){
        boolean foundVerbInfinitive = false;
        int x = possibleKanji.getVerbCombos().size() - 1;
        while (x >= 0 && !foundVerbInfinitive) {  // we're cycling backwards because the later matches for the verb ending are longer, and therefore more accurate
            String kanji = possibleKanji.getVerbCombos().get(x);
            if(BuildConfig.DEBUG){Log.d(TAG, "checking kanji: " + possibleKanji.getVerbCombos().get(x));}

            try {
            Cursor f = InternalDB.getInstance(mContext).getWritableDatabase().rawQuery("SELECT [_id] FROM [Edict] WHERE _id in(SELECT docid FROM [Edict_FTS] WHERE [Kanji] MATCH ?)  ORDER BY [Common] LIMIT 1", new String[]{kanji});
            if (f.getCount() > 0) {
                f.moveToFirst();
                if(BuildConfig.DEBUG){
                    Log.d(TAG, kanji + " - Kanji Verb Entry FOUND!");
                    Log.d(TAG, "Adding verb entry to Supertemp: " + possibleKanji.getListIndex() + " - " + kanji);
                }
                possibleKanji.setKanji(kanji);
                possibleKanji.replaceBetterKanjiMatch(kanji);
                foundVerbInfinitive = true;

            }
            x--;
            f.close();
            } catch(SQLiteException e) {
                Log.e(TAG,"verbInfinitiveExists sqlite exception malformed match? - " + e.getCause());
            }
        }
        return foundVerbInfinitive;
    }

    /**
     * Splits 'bettermatch' items in each ParseSentencePossibleKanji into their own ParseSentencePossibleKanji Object and
     * uses it to create a final map of cleankanji ids.
     *
     * Note: Each ParseSentencePossibleKanji in the possibleKanjiInSentence list may have more than one "good" kanji
     * in its "better kanji match" element. We now want to split those good kanji out into their own ParseSentencePossibleKanji items and
     * create a new "finalized" list of ids for the kanji
     * @param possibleKanjiInSentence Array of ParseSentencePossibleKanji objects, representing possible kanji within the sentence
     *                                (intial core kanji, furigana, position, better matches for kanji etc)
     * @return Array of finalized Kanji ids
     */
    private ArrayList<Integer> getCleanKanjiIDsFromBetterMatches(ArrayList<ParseSentencePossibleKanji> possibleKanjiInSentence) {


        int kanjiarrayReOrderIndex = 0;
        int prevkeystart = 0;
        String prevkanji = "";
        ArrayList<Integer> cleanKanjiIDs = new ArrayList<>();

        for(ParseSentencePossibleKanji possibleKanji : possibleKanjiInSentence) {
            for (int z = 0; z < possibleKanji.getBetterKanjiMatches().size(); z++) {
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "currentkanji " + possibleKanji.getBetterKanjiMatches().get(z));
                    Log.d(TAG, "currentkanjipos " + possibleKanji.getListIndex());
                    Log.d(TAG, "prevkeystart: " + prevkeystart);
                    Log.d(TAG, "prevkanji: " + prevkanji);
                }
                /* if the current kanji is really just a repeat chunk of the previous one, remove it... This is a redundency check.*/
                if((possibleKanji.getListIndex()>=prevkeystart && possibleKanji.getListIndex() <=prevkanji.length()) && prevkanji.contains(possibleKanji.getBetterKanjiMatches().get(z)) ) {
                    Log.d(TAG,"Repeat chunk found, removing: " + possibleKanji.getBetterKanjiMatches().get(z));
                } else {
                    if(BuildConfig.DEBUG){Log.d(TAG,"Inserting into kanjifinal_HashMap_Array: " + possibleKanji.getBetterKanjiMatches().get(z));}
                    addEntrytoFinalKanjiIDs(new ParseSentencePossibleKanji(possibleKanji.getPositionInSentence(),kanjiarrayReOrderIndex,possibleKanji.getBetterKanjiMatches().get(z)),cleanKanjiIDs);
                    kanjiarrayReOrderIndex = kanjiarrayReOrderIndex + 1;
                    prevkeystart = possibleKanji.getListIndex();
                    prevkanji = possibleKanji.getBetterKanjiMatches().get(z);

                }
            }

        }
        return cleanKanjiIDs;
    }

    /**
     * Takes a possibleKanji, tries to match it against the dictionary. If match is successful, it adds the kanji id
     * to the "cleanKanjiIds" array. If not, it tries to chop up the kanji and match different combinations against the dictionary
     * @param possibleKanji ParseSentencePossibleKanji object, representing possible kanji within the sentence
     * @param cleanKanjiIds array of finalized kanji ids to add to
     *
     * @see #getCleanKanjiIDsFromBetterMatches(ArrayList)
     */
    private void addEntrytoFinalKanjiIDs(ParseSentencePossibleKanji possibleKanji, ArrayList<Integer> cleanKanjiIds) {

        Cursor cursorDBMatch = cursorMatchStringAgainstDB(possibleKanji.getKanji());
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "finalfinal kanji: " + possibleKanji.getKanji());
            Log.d(TAG, "Query: SELECT [_id] FROM [Edict_FTS] WHERE [Kanji] MATCH " + possibleKanji.getKanji() + " ORDER BY [COMMON] LIMIT 1");
            Log.d(TAG, "Kanji: --" + possibleKanji.getKanji() + "--");
        }
        if (cursorDBMatch.getCount() > 0) {
            cursorDBMatch.moveToFirst();
            if(BuildConfig.DEBUG){Log.d(TAG, possibleKanji.getKanji() + " FOUND! -- " + cursorDBMatch.getString(0));}
            cleanKanjiIds.add(cursorDBMatch.getInt(0));
            cursorDBMatch.close();
        } else if (possibleKanji.getKanji().length() >= minKanjiLengthtoSplit) {

            if(BuildConfig.DEBUG){
                Log.d(TAG, possibleKanji.getKanji() + " NOT found. Sending it to Kanji Breakup Process");
                Log.i(TAG, "CHECKING/CHOPPING POSSIBLE COMPOUND KANJI COMBOS, ADDING TO KANJIFINAL_CLEAN_INTEGER");
            }


            ArrayList<ArrayList<String>> brokenUpKanjiCombinations = chopKanjiIntoDifferentCombinations(possibleKanji.getKanji());
            matchKanjiPiecesAgainstDB(possibleKanji,brokenUpKanjiCombinations,true,cleanKanjiIds);

        } else {
            if(BuildConfig.DEBUG){Log.d(TAG, "Kanji not found...");}
        }


    }

    /**
     * Compiles the final list of ParseSentenceItems, one for each section of the sentence
     * (including plain text portions as well as the correctly identified kanji).
     * Note: Updated (3/25/17) to take "SpecialS spans" -- links, spinner kanji, anything else that
     * enters the parses but that we KNOW does not need to be changed or parsed, and mix them back into the
     * final map
     * @param cleanKanjiIDs list of finalized ids for the kanji contained in the sentence
     * @return List of ParseSentenceItems, some of which are kanji (to be used for lists of kanji in a sentence), others of which are the
     *          text between those kanji (to be used in laying out the FillInTheBlanks questions)
     */
    private ArrayList<WordEntry>  compileFinalSentenceMap(ArrayList<Integer> cleanKanjiIDs) {

        ArrayList<WordEntry> resultMap = new ArrayList<>();
        int foundKanjiPosition = 0;
        for(int index = 0; index < cleanKanjiIDs.size(); index ++) {
            if(BuildConfig.DEBUG){Log.d(TAG, "clean_int: " + cleanKanjiIDs.get(index));}

            Cursor c = InternalDB.getWordInterfaceInstance(mContext).getWordEntryForWordId(cleanKanjiIDs.get(index),mColorThresholds);

            if (c.getCount() > 0) {
                c.moveToFirst();

                final String newSentenceFragment = entireSentence.substring(foundKanjiPosition, entireSentence.length());
                final String edictKanji = c.getString(0);
                final String coreKanji;
                if(newSentenceFragment.length()>edictKanji.length() && newSentenceFragment.substring(0,edictKanji.length()).equals(edictKanji)) {
                    coreKanji = edictKanji;
                } else {
                    coreKanji = assignCoreKanji(c.getString(0));
                }

                if (newSentenceFragment.contains(coreKanji)) {

                    final int startposition = newSentenceFragment.indexOf(coreKanji);
                    final int endposition = startposition + coreKanji.length();

                    WordEntry wordEntry = new WordEntry(cleanKanjiIDs.get(index)
                            ,c.getString(0)
                            ,c.getString(1)
                            ,c.getString(2)
                            ,c.getInt(3)
                            ,c.getInt(4)
                            ,c.getString(12)
                            ,(foundKanjiPosition + startposition)
                            ,(foundKanjiPosition + endposition));

                    wordEntry.setItemFavorites(new ItemFavorites(c.getInt(5)
                                ,c.getInt(6)
                                ,c.getInt(7)
                                ,c.getInt(8)
                                ,c.getInt(9)
                                ,c.getInt(10)
                                ,c.getInt(11)));

                    wordEntry.setCoreKanjiBlock(coreKanji);
                    
                    resultMap.add(wordEntry);
                    foundKanjiPosition = foundKanjiPosition + endposition;

                }
            }

            c.close();
        }


        return resultMap;
    }

    /**
     * Chooses whether to return a conjugated verb version of a kanji, or (if the kanji is not a verb), the original kanji
     * @param edictKanji clean kanji from dictionary
     * @return conjugated form (i.e. form as it is found in the sentence) of the kanji
     *
     * @see #compileFinalSentenceMap(ArrayList)
     */
    private String assignCoreKanji(String edictKanji) {

        if (VerbChunksAndPositions.containsKey(edictKanji)) {
            return  VerbChunksAndPositions.get(edictKanji);
        } else {
            return  edictKanji;
        }
    }

//    /**
//     * Returns the conjugated form (i.e. form as it should be read when a kanji word is clicked on) of a kanji.
//     * This can mean shortening the dictionary furigana entry if the kanji is a conjugated verb.
//     * Ex: For the conjugated verb "失った" the furigana should read "うしな" instead of the full "うしなった"
//     * @param edictKanji clean kanji from dictionary
//     * @param coreKanji conjugated version of the kanji
//     * @param edictFurigana clean furigana from dictionary
//     * @return conjugated (or shortened) form of the dictionary furigana
//     *
//     * @see #compileFinalSentenceMap(ArrayList)
//     */
//    public String assignCoreFurigana(String edictKanji, String coreKanji, String edictFurigana) {
//        if (VerbChunksAndPositions.containsKey(coreKanji)) {
//            String root = coreKanji.substring(VerbChunksAndPositions.get(coreKanji).length());
//            return edictFurigana.substring(0, edictFurigana.length() - root.length());
//        } else if(edictFurigana != null && edictFurigana.length()>=edictKanji.length()) {
//            if(BuildConfig.DEBUG){
//                Log.d(TAG,"onscreenfurigana last furigana char: " + edictFurigana.charAt(edictFurigana.length()-1));
//                Log.d(TAG,"onscreenfurigana last kanji char: " + edictKanji.charAt(edictKanji.length()-1));
//            }
//            int charstosubtractfromfurigana = 0;
//            int furiganaSubtractionMatchCounter  =  1 ;
//            boolean stopiterating= false;
//
//            while(furiganaSubtractionMatchCounter<=edictKanji.length() && !stopiterating){
//                String lastcharfurigana = String.valueOf(edictFurigana.charAt(edictFurigana.length()-furiganaSubtractionMatchCounter));
//                String lastcharkanji = String.valueOf(edictKanji.charAt(edictKanji.length() - furiganaSubtractionMatchCounter));
//                if(lastcharfurigana.equalsIgnoreCase(lastcharkanji)) {
//                    charstosubtractfromfurigana += 1;
//                    Log.d(TAG,"onscreenfurigana lastcharmatch: " + lastcharfurigana);
//                    furiganaSubtractionMatchCounter += 1;
//                } else {
//                    stopiterating = true;
//                }
//
//            }
//            return edictFurigana.substring(0, edictFurigana.length()-charstosubtractfromfurigana);
//        } else {
//            return edictFurigana;
//        }
//    }


    /**
     * Create combinations of the Kanji, its possible prefixes, and its possible suffixes and attach it as the "verbcombo" element of the possibleKanji object.
     * Iterate through the combos (from longest combo to shortest), looking for matches in the dictionary.
     * If a match is found, add it to the possibleKanji "betterMatch" element
     * Then chop up the kanji into pieces, and look for matches for its component pieces + any prefixes and suffixes.
     * If matches are found for all the pieces, add them as an array of "betterMatch" words
     *
     * @param possibleKanjiInSentence Array of ParseSentencePossibleKanji objects, representing possible kanji within the sentence
     *                                (intial core kanji, furigana, position, better matches for kanji etc)
     * @return returnValue Array of Possible Kanji Objects (for testing)
     *
     */
    public ArrayList<ParseSentencePossibleKanji> createBetterMatchesForPossibleKanji(ArrayList<ParseSentencePossibleKanji> possibleKanjiInSentence) {
        for(ParseSentencePossibleKanji possibleKanji : possibleKanjiInSentence) {
            ArrayList<String> prefixsuffixKanjiCombos = createPrefixSuffixCombinations(possibleKanji);
            searchDictionaryForWordMatches(possibleKanji, prefixsuffixKanjiCombos);
            chopandCompare(possibleKanji);
        }
        return possibleKanjiInSentence;
    }

    /**
     * Takes a possibleKanji object, along with the list of prefix and suffix combinations for that object, and
     * matches combinations of them against the dictionary, looking for the most correct (i.e. longest, usually) match
     *
     * @param possibleKanji ParseSentencePossibleKanji object, representing possible kanji within the sentence
     * @param prefixsuffixKanjiCombos List of possible prefix/suffix combinations to be attached to the suffix of a possible kanji
     * @return  possibleKanji (with updated "better match" element), for testing
     *
     * @see #createBetterMatchesForPossibleKanji(ArrayList)
     */
    public ParseSentencePossibleKanji searchDictionaryForWordMatches(ParseSentencePossibleKanji possibleKanji, ArrayList<String> prefixsuffixKanjiCombos) {

        boolean isfound = false;
        if (prefixsuffixKanjiCombos.size() > 0) {

            for (int i = 0; i < prefixsuffixKanjiCombos.size(); i++) {
                try{
                    Cursor cursorKanjiMatch = InternalDB.getInstance(mContext).getWritableDatabase().rawQuery("SELECT [Kanji] FROM [Edict] WHERE _id in(SELECT docid FROM [Edict_FTS] WHERE [Kanji] MATCH ?)  ORDER BY [Common] LIMIT 1", new String[]{prefixsuffixKanjiCombos.get(i)});
                    if(BuildConfig.DEBUG){Log.d(TAG, "Prefix/Suffix Query: (" + possibleKanji.getKanji() + ") MATCH " + prefixsuffixKanjiCombos.get(i));}
                    if (cursorKanjiMatch.getCount() > 0 ) {
                        cursorKanjiMatch.moveToFirst();

                        if(!wordLoader.getExcludedKanji().contains(cursorKanjiMatch.getString(0))) {

                            isfound = true;
                            possibleKanji.setFoundInDictionary(true);

                            if (possibleKanji.getBetterKanjiMatches().size() > 0) {
                                if(possibleKanji.getBetterKanjiMatches().get(0).length()<= cursorKanjiMatch.getString(0).length()) {
                                    if(BuildConfig.DEBUG){Log.d(TAG,"LONGER MATCH FOUND. Replacing  " + possibleKanji.getBetterKanjiMatches().get(0) + " with " + cursorKanjiMatch.getString(0) );}
                                    possibleKanji.replaceBetterKanjiMatch(cursorKanjiMatch.getString(0));
                                }

                            } else {
                                if(BuildConfig.DEBUG){Log.d(TAG,"MATCH FOUND. Initially adding: " + cursorKanjiMatch.getString(0));}
                                possibleKanji.setKanji(cursorKanjiMatch.getString(0));
                                possibleKanji.replaceBetterKanjiMatch(cursorKanjiMatch.getString(0));
                            }

                        /* If there is a match for the prefix/suffix search, remove that kanji from the kanjifinal_HashMap_Verbs table.
                         * Because we found a better match. I think...
                         *
                         * However, if the prefix suffix match is the FIRST entry, which is an entry WITHOUT any prefixes or suffixes, DONT remove the
                         * verb entry for that word. */

                            int length = 0;
                            for(int ilength =0; ilength <possibleKanji.getVerbCombos().size();ilength++) {
                                if(possibleKanji.getVerbCombos().get(ilength).length()>length) {
                                    length =  possibleKanji.getVerbCombos().get(ilength).length();
                                }
                            }

                            if (i>0 && (cursorKanjiMatch.getString(0).length()> length)) {
                                if(BuildConfig.DEBUG){Log.d(TAG, "kanjifinal_HashMap_Verbs_Array removal: " + possibleKanji.getListIndex() + " - " + cursorKanjiMatch.getString(0));}
                                possibleKanji.setVerbCombos(new ArrayList<String>());
                            }
                        }
                    }
                    cursorKanjiMatch.close();
                } catch(SQLiteException e) {
                    Log.e(TAG,"searchDictionaryForWordMatches sqlite exception malformed match? - " + e.getCause());                    
                }

            }

        }

        if (prefixsuffixKanjiCombos.size() == 0 || !isfound) {
            possibleKanji.replaceBetterKanjiMatch(possibleKanji.getKanji());
            if(BuildConfig.DEBUG){Log.d(TAG, "Prefix/Suffixes NOT FOUND. Adding initial kanji to possibleKanji BetterKanjiMatch: " + possibleKanji.getKanji());}
        }

        return possibleKanji;
    }

    /**
     * Chops kanji into different combinations, and tries to match those combinations against the dictionary
     * @param possibleKanji ParseSentencePossibleKanji object, representing possible kanji within the sentence
     *
     * @see #createBetterMatchesForPossibleKanji(ArrayList)
     */
    private void chopandCompare(ParseSentencePossibleKanji possibleKanji) {
        if (!verbInfinitiveExists(possibleKanji) && possibleKanji.getKanji().length() >= minKanjiLengthtoSplit && !possibleKanji.isFoundInDictionary()) {
            ArrayList<ArrayList<String>> brokenUpKanjiCombinations = chopKanjiIntoDifferentCombinations(possibleKanji.getKanji());
            if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(1) brokenUpKanjiCombinations size: " + brokenUpKanjiCombinations.size());}
            matchKanjiPiecesAgainstDB(possibleKanji,brokenUpKanjiCombinations, false, null);
        }
    }

    /**
     * Creates a specifically ordered list of possible combinations of a possibleKanji, its prefixes, suffixes and verb endings
     * @param possibleKanji ParseSentencePossibleKanji object, representing possible kanji within the sentence
     * @return  List of possible prefix/suffix combinations to be attached to the suffix of a possible kanji
     *
     * @see #createBetterMatchesForPossibleKanji(ArrayList)
     */
    private ArrayList<String>  createPrefixSuffixCombinations(ParseSentencePossibleKanji possibleKanji) {
        if(BuildConfig.DEBUG){Log.d(TAG, "ITERATING: " + possibleKanji.getKanji());}
        ArrayList<String> prefixsuffixKanjiCombos = new ArrayList<>();

        /* Adding the initial raw kanji, so it will be run first through the dictionary lookup iteration.
         * Therefore, if it IS matched, and then there is a better (longer, larger) match the initial one will get overwritten */
        prefixsuffixKanjiCombos.add(possibleKanji.getKanji());

        /* Now create the prefix/suffix combos around the kanji */
        for (String prefix : possibleKanji.getPrefixes()) {
            prefixsuffixKanjiCombos.add(prefix + possibleKanji.getKanji());
        }

        /* Create a separate list of prefix+Kanji+Suffix combinations, but do not add it to prefixsuffixKanjiCombos yet
         *  Instead, we want to look for compound combinations (prefix+Kanji+Suffix+kanji2+suffix2), and add these to prefixsuffixKanjiCombos
         *  ahead of the regular combinations. That way when we iterate through prefixsuffixKanjiCombos, the longer match will occur first (if it exists) */
        ArrayList<String> tmpPrefixSuffixHolder = new ArrayList<>();
        for (String prefixCombo: prefixsuffixKanjiCombos) {
            for(String suffix : possibleKanji.getSuffixes()) {
                tmpPrefixSuffixHolder.add(prefixCombo + suffix);
            }
        }

        /* Add compound combinations to the list */
        prefixsuffixKanjiCombos = addCompoundKanjiBlocks(possibleKanji, prefixsuffixKanjiCombos);

        /* Now include the smaller combinations at the end*/
        for (String smallPrefixSuffixCombo : tmpPrefixSuffixHolder) {
            if(!prefixsuffixKanjiCombos.contains(smallPrefixSuffixCombo)) {
                prefixsuffixKanjiCombos.add(smallPrefixSuffixCombo);
            }
        }

        return prefixsuffixKanjiCombos;
    }

    /** Looks for compound word matches in the dictionary. This is to help catch compound words like "間に合う"
     *  where the 間に and the 合う appear as separate core kanji blocks initially, but really should be combined into one word (in this case a verb)
     *
     *  This method treats the next 5 characters AFTER the next kanji as one big potential suffix, and checks the possible suffixes against the db.
     *  It slows the process down, but I have no better alternatives at the moment for catching these compound words
     *
     * @param possibleKanji Object representing a possible kanji within the sentence (intial core kanji, furigana, position, better matches for kanji etc)
     * @param prefixsuffixKanjiCombos List of possible prefix/suffix combinations to be attached to the suffix of a possible kanji
     * @return finalized list of COMPOUND kanji possibilities
     *
     * @see #createPrefixSuffixCombinations(ParseSentencePossibleKanji)
     */
    private ArrayList<String>  addCompoundKanjiBlocks(ParseSentencePossibleKanji possibleKanji, ArrayList<String> prefixsuffixKanjiCombos ) {

        for (String suffix : possibleKanji.getSuffixes()) {

            /*IF the suffix includes the NEXT KANJI, and furthermore includes the next kanjis POSSIBLE CONJUGATION,
             * add the whole thing--the current kanji, middle hiragana, next kanji and ROOT CONJUGATION to the prefixsuffixkanjicombos*/

            if(possibleKanjiInSentence.size()> (possibleKanji.getListIndex() +1)) {
                ParseSentencePossibleKanji nextKanji = possibleKanjiInSentence.get(possibleKanji.getListIndex() +1);
                final int lengthfromendofthiskanjitoendofnextkanji = (nextKanji.getPositionInSentence() + (nextKanji.getKanji().length() -1)) - possibleKanjiInSentence.get(possibleKanji.getListIndex()).getPositionInSentence();
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "thiskanji: (" + possibleKanji.getListIndex() + ") " + possibleKanji.getKanji());
                    Log.d(TAG, "nextkanji: (" + nextKanji.getPositionInSentence() + ") " + nextKanji.getKanji());
                    Log.d(TAG, "lengthfromendofthiskanjitoendofnextkanji: " + lengthfromendofthiskanjitoendofnextkanji);
                    Log.d(TAG, "kanjioptions_suffix: " + suffix);
                }
                if(suffix.contains(nextKanji.getKanji())
                        && suffix.length()> lengthfromendofthiskanjitoendofnextkanji) {

                    String possibledoublekanjiroot = suffix.substring(0, lengthfromendofthiskanjitoendofnextkanji);
                    String possibledoublekanjiconjugatedverbending = suffix.substring(lengthfromendofthiskanjitoendofnextkanji,suffix.length());
                    if(BuildConfig.DEBUG){Log.d(TAG,"possibledoublekanjiconjugatedverbending: " + possibledoublekanjiconjugatedverbending);}

                    if(wordLoader.getVerbEndingMap().keySet().contains(possibledoublekanjiconjugatedverbending)) {
                        for (int k = 0; k < wordLoader.getVerbEndingMap().get(possibledoublekanjiconjugatedverbending).size(); k++) {
                            String root = wordLoader.getVerbEndingMap().get(possibledoublekanjiconjugatedverbending).get(k);
                            if(BuildConfig.DEBUG){Log.d(TAG, "Adding to VerbChunksAndPositions, the MCDBOUBLE DEELUX CONJUGATED VERB PATTERN: " + possibleKanji.getKanji() + possibledoublekanjiroot + root);}
                            VerbChunksAndPositions.put(possibleKanji.getKanji() + possibledoublekanjiroot + root, possibleKanji.getKanji() + possibledoublekanjiroot );
                            if(!prefixsuffixKanjiCombos.contains(possibleKanji.getKanji() + possibledoublekanjiroot + root)) {
                                prefixsuffixKanjiCombos.add(possibleKanji.getKanji() + possibledoublekanjiroot + root);
                            }
                        }
                    }
                }

            }

            if(!prefixsuffixKanjiCombos.contains(possibleKanji.getKanji() + suffix)) {
                if(BuildConfig.DEBUG){Log.d(TAG, "Adding to prefixsuffixKanjiCombos: " + possibleKanji.getKanji() + suffix);}
                prefixsuffixKanjiCombos.add(possibleKanji.getKanji() + suffix);
            }
        }
        return prefixsuffixKanjiCombos;
    }


    /**
     * When core kanji blocks are being located/assigned, certain spans (like Urls or User_Mentions, which are definitely not kanji)
     * are ignored. This passes the indexes of those excluded spans back to the findCoreKanjiBlocksInSentence method.
     * @param entireSentence Entire sentence
     * @param spansToExclude spans that are excluded
     * @return A list of indexes of those excluded spans
     *
     * @see #findCoreKanjiBlocksInSentence(String, WordLoader, ArrayList)
     */
    private static ArrayList<Integer> getExcludedSpanIndexes(String entireSentence, ArrayList<String> spansToExclude) {
        ArrayList<Integer> spanIndexes = new ArrayList<>();
        for(String span : spansToExclude) {
            if(entireSentence.contains(span)) {
                for(int i = entireSentence.indexOf(span);i<(entireSentence.indexOf(span) + span.length()); i++) {
                    spanIndexes.add(i);
                }
            }
        }

        return spanIndexes;
    }



    /**
     * Creates various combinations of a kanji block. If it is impossible to create a match for a large kanji  (like + 3 characters), look for matches
     * by breaking up those characters into smaller sets (of at least 1 kanji). And if there is a match for every piece of the broken-up kanji, insert
     * those pieces into the kanjifinal_clean_integer
     *
     * This method is used in {@link com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment} when the option "Trickier Multiple Choices" is enabled. The
     * correct multiple choice answer is broken up with this method, and similar words are chosen as the false answers.
     *
     * @param coreKanjiBlock Kanji block to chop up
     * @return Arraylist containing arrays of different combinations. The arrays will then be compared against the dictionary.
     *
     * @see com.jukuproject.jukutweet.Fragments.MultipleChoiceFragment#getIncorrectAnswerSet(Context, String, ColorThresholds, String, WordEntry, String)
     */
    public static ArrayList<String> chopKanjiIntoASingleSetOfCombinations(String coreKanjiBlock) {
        ArrayList<String> kanjibreakupArray = new ArrayList<>();
        StringBuilder kanjibreakupBuilder = new StringBuilder();
        int divisor = coreKanjiBlock.length() - 1;
        if(BuildConfig.DEBUG){Log.d(TAG,"initial divisor: " + divisor);}
        int innerstart =0;
        int outerstart = 0;
        while (divisor >= 1) {

            kanjibreakupBuilder.setLength(0);

            /* Attach the preceding straggling chars (outside of divisor chunks) */
            while ((innerstart + divisor) <= coreKanjiBlock.length() && !(innerstart == divisor && (innerstart + divisor) == coreKanjiBlock.length())) {

                for (int l = 0; l < innerstart; l++) {
                    kanjibreakupBuilder.append(coreKanjiBlock.charAt(l));
                }
                if(kanjibreakupBuilder.length()>0) {
                    if(!kanjibreakupArray.contains(kanjibreakupBuilder.toString())) {
                        kanjibreakupArray.add(kanjibreakupBuilder.toString());
                    }
                    kanjibreakupBuilder.setLength(0);
                }

                for (int l = 0; l < divisor; l++) {
                    kanjibreakupBuilder.append(coreKanjiBlock.charAt(innerstart + l));
                }
                if(!kanjibreakupArray.contains(kanjibreakupBuilder.toString())) {
                    kanjibreakupArray.add(kanjibreakupBuilder.toString());
                }
                innerstart = innerstart + divisor;
                kanjibreakupBuilder.setLength(0);

                if(innerstart + divisor >= coreKanjiBlock.length()) {
                    /* Attach the following (last) straggling chars (outside of divisor chunks) */
                    for (int l = innerstart; l < coreKanjiBlock.length(); l++) {
                        kanjibreakupBuilder.append(coreKanjiBlock.charAt(l));
                    }
                    if(kanjibreakupBuilder.length()>0) {
                        if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(1) div(" + divisor + ") added post-div chunk: " + kanjibreakupBuilder.toString());}
                        if(!kanjibreakupArray.contains(kanjibreakupBuilder.toString())) {
                            kanjibreakupArray.add(kanjibreakupBuilder.toString());
                        }
                        kanjibreakupBuilder.setLength(0);
                    }
                }
            }

            if(outerstart + divisor > coreKanjiBlock.length()) {
                divisor = divisor - 1;
                outerstart = 0;
                innerstart = 0;
                if(BuildConfig.DEBUG){Log.d(TAG," new divisor: " + divisor);}
            } else {
                outerstart = outerstart +1;
                innerstart = outerstart;
            }

        }
        if(BuildConfig.DEBUG){Log.d(TAG, "BREAKUP(2) kanjibreakupArray size: " + kanjibreakupArray.size());}
        return kanjibreakupArray;
    }
}
