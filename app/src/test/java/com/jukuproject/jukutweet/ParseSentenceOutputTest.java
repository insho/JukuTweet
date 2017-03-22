package com.jukuproject.jukutweet;


import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Models.ParseSentenceItem;
import com.jukuproject.jukutweet.Models.ParseSentencePossibleKanji;
import com.jukuproject.jukutweet.Models.WordLoader;

import org.junit.Test;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static android.database.sqlite.SQLiteDatabase.OPEN_READWRITE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = LOLLIPOP, packageName = "com.jukuproject.juku")
public class ParseSentenceOutputTest extends AndroidTestCase {

    SentenceParser sentenceParser;
    SQLiteDatabase db;
    WordLoader wordLoader;
    private String dbPath;

    @Mock
    private SQLiteDatabase database;
    @Mock
    private InternalDB internalDB;

//    private InputStream openFile(String filename) throws IOException {
//        return getClass().getClassLoader().getResourceAsStream(filename);
//    }

    @Before
    public void setUp() throws Exception {

        File file = new File(this.getClass().getClassLoader().getResource("JQuizTest.db").getFile());
        dbPath = file.getAbsolutePath();
        db = database.openDatabase(dbPath, null, OPEN_READWRITE);
        sentenceParser =  Mockito.mock(SentenceParser.class);
        internalDB = InternalDB.getInstance(RuntimeEnvironment.application);
        wordLoader = internalDB.getWordLists(db);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
        internalDB.close();
    }



    /** WORD LOADER TESTS **/
    @Test
    public void listValidator_WordLoaderNotNULL_ReturnsTrue() {
        assertTrue(wordLoader != null);
    }

    @Test
    public void listValidator_HiraganaNotNULL_ReturnsTrue() {
        assertTrue(wordLoader.getHiragana() != null);
    }

    @Test
    public void listValidator_KatakanaNotNULL_ReturnsTrue() {
        assertTrue(wordLoader.getKatakana() != null);
    }

    @Test
    public void listValidator_SymbolsNotNULL_ReturnsTrue() {
        assertTrue(wordLoader.getSymbols() != null);
    }

    @Test
    public void listValidator_Verbendings_rootNotNULL_ReturnsTrue() {
        assertTrue(wordLoader.getVerbEndingsRoot() != null);
    }

    @Test
    public void listValidator_VerEndings_ConjugationNotNULL_ReturnsTrue() {
        assertTrue(wordLoader.getVerbEndingsConjugation() != null);
    }

    @Test
    public void listValidator_ExcludedKanjiNotNULL_ReturnsTrue() {
        assertTrue(wordLoader.getExcludedKanji() != null);
    }


    @Test
    public void listValidator_verbWordLoaderSizes() {
        assertTrue(wordLoader.getHiragana().size() > 0);
    }
    @Test
    public void listValidator_verbEndingsandRootSizeMATCH_ReturnsTrue() {
        assertTrue(wordLoader.getVerbEndingsConjugation().size() == wordLoader.getVerbEndingsRoot().size());
    }

    /*** findCoreKanjiBlocksInSentence TESTS ***/

    @Test
    public void SentenceParser_findCoreKanjiBlocksInSentence1() {

        int questionNumber = 1;
        String ex_focused_kanji_full = "お菓子";
        String entireSentence = "知らない人からお菓子をもらわない方がいいよ。";
        ArrayList<String> answerList = new ArrayList<>();
        answerList.add("1 -- 知");
        answerList.add("5 -- 人");
        answerList.add("10 -- お菓子");
        answerList.add("17 -- 方");

        assertTrue(performFindKanjiBlocksTest(questionNumber,entireSentence,ex_focused_kanji_full,answerList));
    }

    @Test
    public void SentenceParser_findCoreKanjiBlocksInSentence2() {

        int questionNumber = 2;
        String ex_focused_kanji_full = "勉強";
        String entireSentence = "私たちの先生は高校時代に英語を一生懸命勉強したに違いない。";
        ArrayList<String> answerList = new ArrayList<>();
        answerList.add("1 -- 私");
        answerList.add("6 -- 先生");
        answerList.add("11 -- 高校時代");
        answerList.add("14 -- 英語");
        answerList.add("19 -- 一生懸命");
        answerList.add("21 -- 勉強");
        answerList.add("25 -- 違");

        assertTrue(performFindKanjiBlocksTest(questionNumber,entireSentence,ex_focused_kanji_full,answerList));
    }



    @Test
    public void SentenceParser_findCoreKanjiBlocksInSentence3() {

        int questionNumber = 3;
        String ex_focused_kanji_full = "頁";
        String entireSentence = "１０頁を参照してください。";
        ArrayList<String> answerList = new ArrayList<>();
        answerList.add("2 -- ０");
        answerList.add("3 -- 頁");
        answerList.add("6 -- 参照");

        assertTrue(performFindKanjiBlocksTest(questionNumber,entireSentence,ex_focused_kanji_full,answerList));
    }

    @Test
    public void SentenceParser_findCoreKanjiBlocksInSentence4() {

        int questionNumber = 4;
        String ex_focused_kanji_full = "お菓子";
        String entireSentence = "彼は一箱のお菓子を友達全員と分け合った。";
        ArrayList<String> answerList = new ArrayList<>();
        answerList.add("1 -- 彼");
        answerList.add("4 -- 一箱");
        answerList.add("8 -- お菓子");
        answerList.add("13 -- 友達全員");
        answerList.add("15 -- 分");
        answerList.add("17 -- 合");
        assertTrue(performFindKanjiBlocksTest(questionNumber,entireSentence,ex_focused_kanji_full,answerList));
    }

    @Test
    public void SentenceParser_findCoreKanjiBlocksInSentence5() {

        int questionNumber = 5;
        String ex_focused_kanji_full = "お兄さん";
        String entireSentence = "彼はお兄さんと同じように頭がいい。";
        ArrayList<String> answerList = new ArrayList<>();
        answerList.add("1 -- 彼");
        answerList.add("6 -- お兄さん");
        answerList.add("8 -- 同");
        answerList.add("13 -- 頭");

        assertTrue(performFindKanjiBlocksTest(questionNumber,entireSentence,ex_focused_kanji_full,answerList));
    }


    @Test
    public void SentenceParser_findCoreKanjiBlocksInSentence6() {

        int questionNumber = 6;
        String ex_focused_kanji_full = "お兄さん";
        String entireSentence = "次のような問題を想像してください。あなたのお兄さんが自動車事故にあったとしましょう。";
        ArrayList<String> answerList = new ArrayList<>();
        answerList.add("1 -- 次");
        answerList.add("7 -- 問題");
        answerList.add("10 -- 想像");
        answerList.add("25 -- お兄さん");
        answerList.add("31 -- 自動車事故");
        assertTrue(performFindKanjiBlocksTest(questionNumber,entireSentence,ex_focused_kanji_full,answerList));
    }

    @Test
    public void SentenceParser_findCoreKanjiBlocksInSentence7() {

        int questionNumber = 7;
        String ex_focused_kanji_full = "亡き母";
        String entireSentence = "亡き母の写真を見るたびに、胸に熱いものが込み上げてくる。";
        ArrayList<String> answerList = new ArrayList<>();
        answerList.add("3 -- 亡き母");
        answerList.add("6 -- 写真");
        answerList.add("8 -- 見");
        answerList.add("14 -- 胸");
        answerList.add("16 -- 熱");
        answerList.add("21 -- 込");
        answerList.add("23 -- 上");
        assertTrue(performFindKanjiBlocksTest(questionNumber,entireSentence,ex_focused_kanji_full,answerList));
    }

    @Test
    public void SentenceParser_findCoreKanjiBlocksInSentence8() {

        int questionNumber = 8;
        String ex_focused_kanji_full = "煩い";
        String entireSentence = "その煩い音には我慢できない。";
        ArrayList<String> answerList = new ArrayList<>();
        answerList.add("4 -- 煩い");
        answerList.add("5 -- 音");
        answerList.add("9 -- 我慢");
        assertTrue(performFindKanjiBlocksTest(questionNumber,entireSentence,ex_focused_kanji_full,answerList));
    }

    @Test
    public void SentenceParser_findCoreKanjiBlocksInSentence9() {

        int questionNumber = 9;
        String ex_focused_kanji_full = "半分";
        String entireSentence = "その生徒は授業中半分眠っていた。";
        ArrayList<String> answerList = new ArrayList<>();
        answerList.add("4 -- 生徒");
        answerList.add("8 -- 授業中");
        answerList.add("10 -- 半分");
        answerList.add("11 -- 眠");
        assertTrue(performFindKanjiBlocksTest(questionNumber,entireSentence,ex_focused_kanji_full,answerList));
    }

    @Test
    public void SentenceParser_findCoreKanjiBlocksInSentence10() {

        int questionNumber = 10;
        String ex_focused_kanji_full = "疲れる";
        String entireSentence = "冬は疲れる。";
        ArrayList<String> answerList = new ArrayList<>();
        answerList.add("1 -- 冬");
        answerList.add("5 -- 疲れる");
        assertTrue(performFindKanjiBlocksTest(questionNumber,entireSentence,ex_focused_kanji_full,answerList));
    }


    /*** findTrailingHiraganaLength TEST ***/
    @Test
    public void SentenceParser_findTrailingHiraganaLength1() {
        int positionInSentence = 0;
        int entireSentenceLength = 20;
        ArrayList<ParseSentencePossibleKanji> possibleKanjiInSentence = new ArrayList();
        possibleKanjiInSentence.add(new ParseSentencePossibleKanji(1, 0, "彼"));
        possibleKanjiInSentence.add(new ParseSentencePossibleKanji(4, 1,"一箱"));
        possibleKanjiInSentence.add(new ParseSentencePossibleKanji(8, 2,"お菓子"));
        possibleKanjiInSentence.add(new ParseSentencePossibleKanji(13, 3,"友達全員"));
        possibleKanjiInSentence.add(new ParseSentencePossibleKanji(15, 4,"分"));
        possibleKanjiInSentence.add(new ParseSentencePossibleKanji(17, 5,"合"));
        assertEquals(SentenceParser.findTrailingHiraganaLength(positionInSentence,entireSentenceLength,possibleKanjiInSentence),2);
    }
//    private Boolean performFindTrailingHiraganaLengthTest(int correctTrailingLength, int positionInSentence, int entireSentenceLength, ArrayList<ParseSentencePossibleKanji> possibleKanjiInSentence) {
//
//    }



    /*** TOTAL SENTENCE PARSER OUTPUT TESTS ***/

//    /*******************************
//     * Testing different sentence breakups against the split sentence treemap,
//     * so that those sentence breakdowns that have been locked down correctly stay that way
//
//         **** QUERY FOR FINDING SENTENCE FROM WORD ****
//
//         Select * from ExampleSent where _id in (
//         SELECT  Sentence_id FROM ExampleSentXRef where Edict_id in (Select _id from Edict where Kanji = "KANJI")
//         )
//
//     ******************************/
//
    @Test
    public void sentenceParserOutPutsTest1() {
        assertTrue(performSentenceParserTest(db,1,"お菓子","彼は一箱のお菓子を友達全員と分け合った。"
//                , new ArrayList<Integer>(Arrays.asList(0, 1, 2, 4, 5, 8, 9, 11, 13, 14, 15))
                , new ArrayList<Integer>(Arrays.asList(0, 1, 2, 4, 5, 8, 9, 11, 13, 14, 17))
                ,new ArrayList<Integer>(Arrays.asList(21213, 0, 90659, 0, 108, 0, 23852, 16769, 0, 201510, 0))
                , new ArrayList<String>(Arrays.asList("彼", "一箱", "お菓子", "友達", "全員", "分け合う"))));

    }

    @Test
    public void sentenceParserOutPutsTest2() {
        assertTrue(performSentenceParserTest(db,2,"お兄さん","彼はお兄さんと同じように頭がいい。"
                , new ArrayList<Integer>(Arrays.asList(0, 1, 2, 6, 12, 16))
                ,new ArrayList<Integer>(Arrays.asList(21213, 0, 116, 31304, 185158, 0))
                , new ArrayList<String>(Arrays.asList("彼", "お兄さん", "と同じように", "頭がいい"))));

    }

    @Test
    public void sentenceParserOutPutsTest3() {
        assertTrue(performSentenceParserTest(db,3,"お兄さん","次のような問題を想像してください。あなたのお兄さんが自動車事故にあったとしましょう。"
                , new ArrayList<Integer>(Arrays.asList(0, 1, 5, 7, 8, 10, 21, 25, 26, 31))
                ,new ArrayList<Integer>(Arrays.asList(12769, 0, 23683, 0, 16950, 0, 116, 0, 139631, 0))
                , new ArrayList<String>(Arrays.asList("次", "問題", "想像", "お兄さん", "自動車事故"))));

    }


    @Test
    public void sentenceParserOutPutsTest4() {
        assertTrue(performSentenceParserTest(db,4,"猫","彼は一日中行方不明の猫を探した。"
                , new ArrayList<Integer>(Arrays.asList(0, 1, 2, 5, 9, 10, 11, 12, 13))
                ,new ArrayList<Integer>(Arrays.asList(21213, 0, 5634, 127716, 0, 20459, 0, 18009, 0))
                , new ArrayList<String>(Arrays.asList("彼", "一日中", "行方不明", "猫", "探す"))));

    }
    @Test
    public void sentenceParserOutPutsTest5() {
        assertTrue(performSentenceParserTest(db,5,"亡き母","亡き母の写真を見るたびに、胸に熱いものが込み上げてくる。"
//                , new ArrayList<Integer>(Arrays.asList(0, 3, 4, 6, 7, 9, 13, 14, 15, 16, 20, 21))
                , new ArrayList<Integer>(Arrays.asList(0, 3, 4, 6, 7, 9, 13, 14, 15, 16, 20, 23))
                ,new ArrayList<Integer>(Arrays.asList(206031, 0, 13107, 0, 9930, 0, 8911, 0, 20465, 0, 130034, 0))
                , new ArrayList<String>(Arrays.asList("亡き母", "写真", "見る", "胸", "熱い", "込み上げる"))));
    }

    @Test
    public void sentenceParserOutPutsTest6() {
        assertTrue(performSentenceParserTest(db,6,"煩い","その煩い音には我慢できない。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 4, 5, 7, 9))
                ,new ArrayList<Integer>(Arrays.asList(0, 21164, 6383, 0, 6901, 0))
                , new ArrayList<String>(Arrays.asList("煩い", "音", "我慢"))));

    }

    @Test
    public void sentenceParserOutPutsTest7() {
        assertTrue(performSentenceParserTest(db,7,"半分","その生徒は授業中半分眠っていた。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 4, 5, 8, 10, 11))
                ,new ArrayList<Integer>(Arrays.asList(0, 16053, 0, 143910, 21085, 23255, 0))
                , new ArrayList<String>(Arrays.asList("生徒", "授業中", "半分", "眠る"))));

    }

    @Test
    public void sentenceParserOutPutsTest8() {
        assertTrue(performSentenceParserTest(db,8,"半分","私たちは居間の半分の場所を取るグランドピアノを買った。"
//                , new ArrayList<Integer>(Arrays.asList(0, 3, 4, 6, 7, 9, 10, 12, 23, 24))
                , new ArrayList<Integer>(Arrays.asList(0, 3, 4, 6, 7, 9, 10, 14, 23, 24))
                ,new ArrayList<Integer>(Arrays.asList(12486, 0, 8626, 0, 21085, 0, 151402, 0, 20783, 0))
                , new ArrayList<String>(Arrays.asList("私たち", "居間", "半分", "場所を取る", "買う"))));

    }

    @Test
    public void sentenceParserOutPutsTest9() {
        assertTrue(performSentenceParserTest(db,9,"疲れる","冬は疲れる。"
                , new ArrayList<Integer>(Arrays.asList(0, 1, 2, 5))
                ,new ArrayList<Integer>(Arrays.asList(19488, 0, 21278, 0))
                , new ArrayList<String>(Arrays.asList("冬", "疲れる"))));

    }

    @Test
    public void sentenceParserOutPutsTest10() {
        assertTrue(performSentenceParserTest(db,10,"疲れる","私が疲れるのは、暑さというよりはむしろ湿度のせいだ。"
                , new ArrayList<Integer>(Arrays.asList(0, 1, 2, 5, 8, 10, 19, 21))
                ,new ArrayList<Integer>(Arrays.asList(12480, 0, 21278, 0, 14227, 0, 13009, 0))
                , new ArrayList<String>(Arrays.asList("私", "疲れる", "暑さ", "湿度"))));

    }

    @Test
    public void sentenceParserOutPutsTest11() {
        assertTrue(performSentenceParserTest(db,11,"飛行機","新聞報道によれば昨日飛行機事故があった模様である。"
                , new ArrayList<Integer>(Arrays.asList(0, 4, 8, 10, 13, 15, 19, 21))
                ,new ArrayList<Integer>(Arrays.asList(153995, 0, 11874, 21359, 12641, 0, 23542, 0))
                , new ArrayList<String>(Arrays.asList("新聞報道", "昨日", "飛行機", "事故", "模様"))));

    }

    @Test
    public void sentenceParserOutPutsTest12() {
        assertTrue(performSentenceParserTest(db,12,"飛行機","爆発が起こった。あっと言う間に、その飛行機は燃え上がり、墜落した。"
//                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 4, 8, 15, 18, 21, 22, 23, 28, 30))
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 4, 8, 15, 18, 21, 22, 25, 28, 30))
                ,new ArrayList<Integer>(Arrays.asList(20920, 0, 8275, 0, 26440, 0, 21359, 0, 20547, 0, 18813, 0))
                , new ArrayList<String>(Arrays.asList("爆発", "起こる", "あっと言う間に", "飛行機", "燃え上がる", "墜落"))));

    }

    @Test
    public void sentenceParserOutPutsTest13() {
        assertTrue(performSentenceParserTest(db,13,"飛行機","地上で空費される時間が飛行機の速さという有利な店を帳消しにしてしまう。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 5, 8, 10, 11, 14, 15, 17, 20, 22, 23, 24, 25, 28))
                ,new ArrayList<Integer>(Arrays.asList(18225, 0, 115729, 0, 12740, 0, 21359, 0, 17289, 0, 23914, 0, 19196, 0, 18551, 0))
                , new ArrayList<String>(Arrays.asList("地上", "空費", "時間", "飛行機", "速さ", "有利", "店", "帳消し"))));

    }

    @Test
    public void sentenceParserOutPutsTest14() {
        assertTrue(performSentenceParserTest(db,14,"病院","典型的な日本の赤ん坊は、病院から家に帰ると母親と一緒に寝る。"
//                , new ArrayList<Integer>(Arrays.asList(0, 3, 4, 6, 7, 10, 12, 14, 16, 17, 21, 23, 24, 27, 29))
                , new ArrayList<Integer>(Arrays.asList(0, 3, 4, 6, 7, 10, 12, 14, 16, 19, 21, 23, 24, 27, 29))
                ,new ArrayList<Integer>(Arrays.asList(19143, 0, 20322, 0, 16282, 0, 21536, 0, 98043, 0, 22578, 0, 5543, 15059, 0))
                , new ArrayList<String>(Arrays.asList("典型的", "日本", "赤ん坊", "病院", "家に帰る", "母親", "一緒に", "寝る"))));

    }


    @Test
    public void sentenceParserOutPutsTest15() {
        assertTrue(performSentenceParserTest(db,15,"病院","あなたの寄附は小児病院を建設する補助として、大いに役立つでしょう。"
                , new ArrayList<Integer>(Arrays.asList(0, 4, 6, 7, 9, 11, 12, 14, 16, 18, 22, 25, 28))
                ,new ArrayList<Integer>(Arrays.asList(0, 8055, 0, 14470, 21536, 0, 9820, 0, 22540, 0, 17699, 23769, 0))
                , new ArrayList<String>(Arrays.asList("寄附", "小児", "病院", "建設", "補助", "大いに", "役立つ"))));

    }


    @Test
    public void sentenceParserOutPutsTest16() {
        assertTrue(performSentenceParserTest(db,16,"部屋","彼女が部屋に入ると私はすぐに立ち上がり、深々と頭を下げた。"
//                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 5, 6, 8, 9, 10, 14, 19, 20, 22, 23, 24))
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 5, 6, 8, 9, 10, 14, 19, 20, 22, 23, 26))
                ,new ArrayList<Integer>(Arrays.asList(21227, 0, 21898, 0, 20367, 0, 12480, 0, 24440, 0, 154094, 0, 185234, 0))
                , new ArrayList<String>(Arrays.asList("彼女", "部屋", "入る", "私", "立ち上がり", "深々", "頭を下げる"))));

    }

    @Test
    public void sentenceParserOutPutsTest17() {
        assertTrue(performSentenceParserTest(db,17,"部屋","その部屋に入るや否や私は、煙草の臭いのほかにガスの匂いがするのに気がついた。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 4, 5, 7, 10, 11, 13, 15, 16, 17, 25, 26, 32, 33))
//                , new ArrayList<Integer>(Arrays.asList(0, 2, 4, 5, 7, 10, 11, 13, 15, 16, 18, 25, 27, 32, 33))
                ,new ArrayList<Integer>(Arrays.asList(0, 21898, 0, 20367, 33724, 12480, 0, 6125, 0, 13730, 0, 20250, 0, 8152, 0))
                , new ArrayList<String>(Arrays.asList("部屋", "入る", "や否や", "私", "煙草", "臭い", "匂い", "気"))));

    }

    @Test
    public void sentenceParserOutPutsTest18() {
        assertTrue(performSentenceParserTest(db,18,"封筒","秘書は手紙を封筒の中に差し込んだ。"
//                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 5, 6, 8, 9, 10, 11, 12))
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 5, 6, 8, 9, 10, 11, 14))
                ,new ArrayList<Integer>(Arrays.asList(21293, 0, 13416, 0, 21922, 0, 18362, 0, 130791, 0))
                , new ArrayList<String>(Arrays.asList("秘書", "手紙", "封筒", "中", "差し込む"))));

    }

    @Test
    public void sentenceParserOutPutsTest19() {
        assertTrue(performSentenceParserTest(db,19,"風","彼らは風と荒天のなすがままに、航海中に行方不明になった。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 4, 5, 7, 15, 17, 18, 19, 23))
                ,new ArrayList<Integer>(Arrays.asList(21217, 0, 21923, 0, 127361, 0, 10968, 18362, 0, 127716, 0))
                , new ArrayList<String>(Arrays.asList("彼ら", "風", "荒天", "航海", "中", "行方不明"))));

    }

    @Test
    public void sentenceParserOutPutsTest20() {
        assertTrue(performSentenceParserTest(db,20,"服","小島先生は朝食後たばこを１服吸った。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 4, 5, 8, 13, 14, 15))
                ,new ArrayList<Integer>(Arrays.asList(14495, 16456, 0, 177332, 0, 21997, 8502, 0))
                , new ArrayList<String>(Arrays.asList("小島", "先生", "朝食後", "服", "吸う"))));

    }


    @Test
    public void sentenceParserOutPutsTest21() {
        assertTrue(performSentenceParserTest(db,21,"分かる","その後援者は本物の骨董品の良さが分かる。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 5, 6, 8, 9, 12, 13, 15, 16, 19))
                ,new ArrayList<Integer>(Arrays.asList(0, 123143, 0, 22975, 0, 129991, 0, 24630, 0, 22095, 0))
                , new ArrayList<String>(Arrays.asList("後援者", "本物", "骨董品", "良さ", "分かる"))));

    }

    @Test
    public void sentenceParserOutPutsTest22() {
        assertTrue(performSentenceParserTest(db,22,"分かる","年賀状のおかげで私達は友達や親戚の消息が分かる。"
                , new ArrayList<Integer>(Arrays.asList(0, 3, 8, 10, 11, 13, 14, 16, 17, 19, 20, 23))
                ,new ArrayList<Integer>(Arrays.asList(20488, 0, 137061, 0, 23852, 0, 15364, 0, 14590, 0, 22095, 0))
                , new ArrayList<String>(Arrays.asList("年賀状", "私達", "友達", "親戚", "消息", "分かる"))));

    }

    @Test
    public void sentenceParserOutPutsTest23() {
        assertTrue(performSentenceParserTest(db,23,"文章","次の文章を日本語に直しなさい。"
                , new ArrayList<Integer>(Arrays.asList(0, 1, 2, 4, 5, 8, 9, 10))
                ,new ArrayList<Integer>(Arrays.asList(12769, 0, 22197, 0, 20333, 0, 18750, 0))
                , new ArrayList<String>(Arrays.asList("次", "文章", "日本語", "直す"))));

    }


    @Test
    public void sentenceParserOutPutsTest24() {
        assertTrue(performSentenceParserTest(db,24,"文章","文法的に正しい文章を作るよう心がけるべきだ。"
                , new ArrayList<Integer>(Arrays.asList(0, 3, 4, 7, 9, 10, 12, 14, 18))
                ,new ArrayList<Integer>(Arrays.asList(202341, 0, 15924, 22197, 0, 11837, 0, 15092, 0))
                , new ArrayList<String>(Arrays.asList("文法的", "正しい", "文章", "作る", "心がける"))));

    }


    @Test
    public void sentenceParserOutPutsTest25() {
        assertTrue(performSentenceParserTest(db,25,"並ぶ","英国人は列を作って並ぶのに慣れている。"
                , new ArrayList<Integer>(Arrays.asList(0, 3, 4, 5, 6, 7, 9, 11, 13, 14))
//                , new ArrayList<Integer>(Arrays.asList(0, 3, 4, 5, 6, 8, 9, 11, 13, 14))
                ,new ArrayList<Integer>(Arrays.asList(93564, 0, 24818, 0, 11837, 0, 22286, 0, 7699, 0))
                , new ArrayList<String>(Arrays.asList("英国人", "列", "作る", "並ぶ", "慣れる"))));

    }


    @Test
    public void sentenceParserOutPutsTest26() {
        assertTrue(performSentenceParserTest(db,26,"並ぶ","彼は古今に並ぶ者のない偉大な政治家である。"
                , new ArrayList<Integer>(Arrays.asList(0, 1, 2, 4, 5, 7, 8, 11, 13, 14, 17))
                ,new ArrayList<Integer>(Arrays.asList(21213, 0, 10244, 0, 22286, 13166, 0, 5260, 0, 15884, 0))
                , new ArrayList<String>(Arrays.asList("彼", "古今", "並ぶ", "者", "偉大", "政治家"))));

    }


    @Test
    public void sentenceParserOutPutsTest27() {
        assertTrue(performSentenceParserTest(db,27,"便利","これは主婦の手間を省く便利な器具です。"
                , new ArrayList<Integer>(Arrays.asList(0, 3, 5, 6, 8, 9, 11, 13, 14, 16))
                ,new ArrayList<Integer>(Arrays.asList(0, 13315, 0, 13403, 0, 14642, 22437, 0, 8009, 0))
                , new ArrayList<String>(Arrays.asList("主婦", "手間", "省く", "便利", "器具"))));

    }


    @Test
    public void sentenceParserOutPutsTest28() {
        assertTrue(performSentenceParserTest(db,28,"頁","１０頁を参照してください。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 4, 6))
                ,new ArrayList<Integer>(Arrays.asList(0, 22317, 0, 12023, 0))
                , new ArrayList<String>(Arrays.asList("頁", "参照"))));

    }


    @Test
    public void sentenceParserOutPutsTest29() {
        assertTrue(performSentenceParserTest(db,29,"勉強","私たちの先生は高校時代に英語を一生懸命勉強したに違いない。"
                ,new ArrayList<Integer>(Arrays.asList(0, 3, 4, 6, 7, 11, 12, 14, 15, 19, 21, 23, 28))
                ,new ArrayList<Integer>(Arrays.asList(12486, 0, 16456, 0, 128170, 0, 5988, 0, 5575, 22441, 0, 31759, 0))
                ,new ArrayList<String>(Arrays.asList("私たち", "先生", "高校時代", "英語", "一生懸命", "勉強", "に違いない"))));

    }


    @Test
    public void sentenceParserOutPutsTest30() {
        assertTrue(performSentenceParserTest(db,30,"お菓子","知らない人からお菓子をもらわない方がいいよ。"
                , new ArrayList<Integer>(Arrays.asList(0, 4, 5, 7, 10, 16, 20))
                ,new ArrayList<Integer>(Arrays.asList(174657, 15454, 0, 108, 0, 205361, 0))
                , new ArrayList<String>(Arrays.asList("知らない", "人", "お菓子", "方がいい"))));

    }



    @Test
    public void sentenceParserOutPutsTest31() {
        assertTrue(performSentenceParserTest(db,31,"お茶","お茶碗一杯のご飯は約、１８０ｇです。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 5, 6, 8, 9, 10))
                ,new ArrayList<Integer>(Arrays.asList(150, 221080, 5642, 0, 269, 0, 23770, 0))
                , new ArrayList<String>(Arrays.asList("お茶", "碗", "一杯", "ご飯", "約"))));
    }

    @Test
    public void sentenceParserOutPutsTest32() {
        assertTrue(performSentenceParserTest(db,32,"お茶","その夕方不気味な沈黙のうちにお茶がすまされた。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 4, 7, 8, 10, 14, 16))
                , new ArrayList<Integer>(Arrays.asList(0, 23981, 21615, 0, 18788, 0, 150, 0))
                , new ArrayList<String>(Arrays.asList("夕方", "不気味", "沈黙", "お茶"))));

    }

    @Test
    public void sentenceParserOutPutsTest33() {
        assertTrue(performSentenceParserTest(db,33,"緑","緑色植物は自分自身の食物を作ることができる。"
                , new ArrayList<Integer>(Arrays.asList(0, 1, 2, 4, 5, 9, 10, 12, 13, 15))
                , new ArrayList<Integer>(Arrays.asList(24671, 14949, 14925, 0, 12895, 0, 15002, 0, 11837, 0))
                , new ArrayList<String>(Arrays.asList("緑", "色", "植物", "自分自身", "食物", "作る"))));

    }


    @Test
    public void sentenceParserOutPutsTest34() {
        assertTrue(performSentenceParserTest(db,34,"緑","苔は、倒れた丸太や岩の上の繊細な緑の柔毛だと私は心の中で思う。"
                , new ArrayList<Integer>(Arrays.asList(0, 1, 3, 4, 6, 8, 9, 10, 11, 12, 13, 15, 16, 17, 18, 20, 22, 23, 24, 27, 28, 30))
//                  , new ArrayList<Integer>(Arrays.asList(0, 1, 3, 6, 8, 9, 10, 11, 12, 13, 15, 16, 17, 18, 20, 22, 23, 24, 27, 28, 30))
                , new ArrayList<Integer>(Arrays.asList(171009, 0, 19471, 0, 7916, 0, 7936, 0, 14706, 0, 16636, 0, 24671, 0, 145384, 0, 12480, 0, 153244, 0, 12358, 0))
                , new ArrayList<String>(Arrays.asList("苔", "倒れる", "丸太", "岩", "上", "繊細", "緑", "柔毛", "私", "心の中", "思う"))));

    }


    @Test
    public void sentenceParserOutPutsTest35() {
        assertTrue(performSentenceParserTest(db,35,"薬","お前は病気が直りたいのならこの薬を飲んだ方がいいよ。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 5, 6, 7, 15, 16, 17, 18, 20, 24))
//                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 5, 6, 8, 15, 16, 17, 19, 20, 24))
                , new ArrayList<Integer>(Arrays.asList(140, 0, 21537, 0, 18754, 0, 23774, 0, 5828, 0, 205361, 0))
                , new ArrayList<String>(Arrays.asList("お前", "病気", "直る", "薬", "飲む", "方がいい"))));

    }


    @Test
    public void sentenceParserOutPutsTest36() {
        assertTrue(performSentenceParserTest(db,36,"薬","米国の親の中には、息子を麻薬に近づけないためにフットボールを勧めるものが多い。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 4, 5, 8, 9, 11, 12, 13, 14, 15, 16, 30, 33, 36, 37))
//                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 4, 5, 8, 9, 11, 12, 13, 14, 15, 18, 30, 33, 36, 37))
                , new ArrayList<Integer>(Arrays.asList(22310, 0, 15347, 0, 175932, 0, 17249, 0, 23019, 23774, 0, 114202, 0, 7600, 0, 17390, 0))
                , new ArrayList<String>(Arrays.asList("米国", "親", "中には", "息子", "麻", "薬", "近づく", "勧める", "多い"))));

    }


    @Test
    public void sentenceParserOutPutsTest37() {
        assertTrue(performSentenceParserTest(db,37,"野菜","彼女は好んで新鮮な生野菜を食べます。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 6, 8, 9, 10, 12, 13, 14))
                , new ArrayList<Integer>(Arrays.asList(21227, 0, 10747, 15192, 0, 15979, 23727, 0, 14977, 0))
                , new ArrayList<String>(Arrays.asList("彼女", "好んで", "新鮮", "生", "野菜", "食べる"))));

    }


    @Test
    public void sentenceParserOutPutsTest38() {
        assertTrue(performSentenceParserTest(db,38,"野菜","先月は野菜の値段が高くなったので、食料品代が増えた。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 5, 6, 8, 9, 10, 17, 20, 21, 22, 23))
//                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 5, 6, 8, 9, 11, 17, 20, 21, 22, 23))
                , new ArrayList<Integer>(Arrays.asList(16435, 0, 23727, 0, 18171, 0, 11104, 0, 15006, 17654, 0, 17170, 0))
                , new ArrayList<String>(Arrays.asList("先月", "野菜", "値段", "高い", "食料品", "代", "増える"))));

    }


    @Test
    public void sentenceParserOutPutsTest39() {
        assertTrue(performSentenceParserTest(db,39,"混む","湘南の海水浴場は日曜日にはとても混む。"
                , new ArrayList<Integer>(Arrays.asList(0, 3, 7, 8, 11, 16, 18))
                , new ArrayList<Integer>(Arrays.asList(0, 7099, 0, 20345, 0, 11501, 0))
                , new ArrayList<String>(Arrays.asList("海水浴場", "日曜日", "混む"))));

    }


    @Test
    public void sentenceParserOutPutsTest40() {
        assertTrue(performSentenceParserTest(db,40,"飲み込む","食べ物を飲み込むとのどが痛みます。"
                , new ArrayList<Integer>(Arrays.asList(0, 3, 4, 8, 12, 13))
                , new ArrayList<Integer>(Arrays.asList(14979, 0, 5825, 0, 18847, 0))
                , new ArrayList<String>(Arrays.asList("食べ物", "飲み込む", "痛む"))));

    }


    @Test
    public void sentenceParserOutPutsTest_ProblemSentence41() {
        assertTrue(performSentenceParserTest(db,41,"いつの間にか","私はいつの間にかぐっすり眠っていた。"
                , new ArrayList<Integer>(Arrays.asList(0, 1, 2, 8, 12, 13))
                , new ArrayList<Integer>(Arrays.asList(12480, 0, 77, 0, 23255, 0))
                , new ArrayList<String>(Arrays.asList("私", "いつの間にか", "眠る"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence42() {
        assertTrue(performSentenceParserTest(db,42,"いつの間にか","いつの間にか小鳥たちは見えなくなった。"
                , new ArrayList<Integer>(Arrays.asList(0, 6, 8, 11, 12))
                , new ArrayList<Integer>(Arrays.asList(77, 14492, 0, 9913, 0))
                , new ArrayList<String>(Arrays.asList("いつの間にか", "小鳥", "見える"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence43() {
        assertTrue(performSentenceParserTest(db,43,"お休み","明日はお休みです。"
                , new ArrayList<Integer>(Arrays.asList(0, 2, 3, 6))
                , new ArrayList<Integer>(Arrays.asList(23453, 0, 113, 0))
                , new ArrayList<String>(Arrays.asList("明日", "お休み"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence44() {
        assertTrue(performSentenceParserTest(db,44,"お休み","お休みはあっと言う間に終わってしまった。"
                , new ArrayList<Integer>(Arrays.asList(0, 3, 4, 11, 12))
                , new ArrayList<Integer>(Arrays.asList(113, 0, 26440, 13704, 0))
                , new ArrayList<String>(Arrays.asList("お休み", "あっと言う間に", "終わる"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence45() {
        assertTrue(performSentenceParserTest(db,45,"お休み","いよいよ今週を限りに、しばらくのお休みです。"
                , new ArrayList<Integer>(Arrays.asList(0, 4, 6, 10, 16, 19))
                , new ArrayList<Integer>(Arrays.asList(0, 11434, 34008, 0, 113, 0))
                , new ArrayList<String>(Arrays.asList("今週", "を限りに", "お休み"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence46() {
        assertTrue(performSentenceParserTest(db,46,"お帰り","お帰りになったら電話を下さい。"
                , new ArrayList<Integer>(Arrays.asList(0, 3, 8, 10, 11, 14))
                , new ArrayList<Integer>(Arrays.asList(110, 0, 19330, 0, 6410, 0))
                , new ArrayList<String>(Arrays.asList("お帰り", "電話", "下さい"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence47() {
        assertTrue(performSentenceParserTest(db,47,"お帰り","あなたのお帰りを一日千秋の思いでお待ちしています。"
                , new ArrayList<Integer>(Arrays.asList(0, 4, 7, 8, 12, 13, 16, 17, 18))
                , new ArrayList<Integer>(Arrays.asList(0, 110, 0, 90612, 0, 136177, 0, 17577, 0))
                , new ArrayList<String>(Arrays.asList("お帰り", "一日千秋", "思いで", "待つ"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence48() {
        assertTrue(performSentenceParserTest(db,48,"お手伝いさん","お手伝いさんはすぐに食卓から食器をかたづけた。"
                , new ArrayList<Integer>(Arrays.asList(0, 6, 10, 12, 14, 16))
                , new ArrayList<Integer>(Arrays.asList(130, 0, 14992, 0, 14987, 0))
                , new ArrayList<String>(Arrays.asList("お手伝いさん", "食卓", "食器"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence49() {
        assertTrue(performSentenceParserTest(db,49,"お手伝いさん","今日ではお手伝いさんを雇う余裕のある人は少ない。"
                , new ArrayList<Integer>(Arrays.asList(0, 4, 10, 11, 13, 15, 16, 19, 20, 23))
                , new ArrayList<Integer>(Arrays.asList(130238, 130, 0, 10371, 24034, 0, 26599, 0, 14523, 0))
                , new ArrayList<String>(Arrays.asList("今日では", "お手伝いさん", "雇う", "余裕", "ある人", "少ない"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence50() {
        assertTrue(performSentenceParserTest(db,50,"お手伝いさん","お手伝いさんは毎日の家事にすっかり飽きてしまった。"
                , new ArrayList<Integer>(Arrays.asList(0, 6, 7, 9, 10, 12, 17, 18))
                , new ArrayList<Integer>(Arrays.asList(130, 0, 23057, 0, 6668, 0, 22763, 0))
                , new ArrayList<String>(Arrays.asList("お手伝いさん", "毎日", "家事", "飽きる"))));
    }

    /** PROBLEM SENTENCES -- WRONG, AND NEED CORRECTING *****************************/

//    @Test
//    public void sentenceParserOutPutsTest_ProblemSentence1() {
//        assertFalse(performSentenceParserTest(db,1,"飛行機","悪天候のため飛行機は出発が２時間遅れた。"
//                , new ArrayList<Integer>()
//                ,new ArrayList<Integer>()
//                , new ArrayList<String>()));
//
//    }
//    @Test
//    public void sentenceParserOutPutsTest_ProblemSentence2() {
//        assertFalse(performSentenceParserTest(db,2,"病院","私は病院へ行く途中、交通渋滞にあって立ち往生してしまった。"
//                , new ArrayList<Integer>()
//                ,new ArrayList<Integer>()
//                , new ArrayList<String>()));
//
//    }

//    @Test
//    public void sentenceParserOutPutsTest_ProblemSentence3() {
//        assertFalse(performSentenceParserTest(db,3,"文章","彼はその文章の文字どおりの意味を説明した。"
//                , new ArrayList<Integer>()
//                ,new ArrayList<Integer>()
//                , new ArrayList<String>()));
//
//    }

//    @Test
//    public void sentenceParserOutPutsTest_ProblemSentence4() {
//        assertFalse(performSentenceParserTest(db,4,"勉強","私はクラスで中国語の勉強に特に熱心な女子学生に気がついた。"
//                , new ArrayList<Integer>()
//                ,new ArrayList<Integer>()
//                , new ArrayList<String>()));
//
//    }



    /********* TEST METHODS **********/
    /*******************************/
    /*******************************/

    private Boolean performFindKanjiBlocksTest(int questionNumber, String entireSentence, String ex_focused_kanji_full, ArrayList<String> answerList){
        ArrayList<Integer> kanjPositionArray = new ArrayList<>();
        if(!ex_focused_kanji_full.equals("")  && entireSentence.contains(ex_focused_kanji_full)) {
            int startposition = entireSentence.indexOf(ex_focused_kanji_full);
            int endposition = startposition + ex_focused_kanji_full.length();
            if (endposition > startposition) {
                for (int j = startposition; j < endposition; j++) {
                    kanjPositionArray.add(j);
                }
            }
        }

        ArrayList<ParseSentencePossibleKanji> possibleKanjiInSentence = SentenceParser.findCoreKanjiBlocksInSentence(entireSentence,wordLoader,kanjPositionArray);
        Boolean isCorrect = true;
        for (int i = 0; i< possibleKanjiInSentence.size() && isCorrect; i ++ ) {
            String resultsentry = possibleKanjiInSentence.get(i).getPositionInSentence() + " -- " + possibleKanjiInSentence.get(i).getKanji();
            if(!answerList.get(i).equals(resultsentry)) {
                isCorrect = false;
                System.out.println("(" + questionNumber + ") Answerlist size: " + answerList.size() + "result list size: " + possibleKanjiInSentence.size());
                System.out.println("(" + questionNumber + ") PossibleKanjiProblem: answerlist(" + i + ") " + answerList.get(i) + " != result entry: " + resultsentry);
            }
        }

        return isCorrect;
    }

    private boolean performSentenceParserTest(SQLiteDatabase db,Integer testnumber, String ex_focused_kanji_full, String sentence, ArrayList<Integer> arrayofHardCodedKeys, ArrayList<Integer> arrayofHardCodedKanjiIDs, ArrayList<String> arrayofHardCodedKanjis) {
        ArrayList<String> wordvalues = new ArrayList<>();
        wordvalues.add(ex_focused_kanji_full);
        ArrayList<Integer> kanjPositionArray = new ArrayList<>();

        /** Create the mock Kanji Position array for the test */
        if(!ex_focused_kanji_full.equals("")  && sentence.contains(ex_focused_kanji_full)) {
            int startposition = sentence.indexOf(ex_focused_kanji_full);
            int endposition = startposition + ex_focused_kanji_full.length();

            if (endposition > startposition) {
                for (int j = startposition; j < endposition; j++) {


                    kanjPositionArray.add(j);
                }
            }
        }


        boolean correctmatch = true;

        ArrayList<ParseSentenceItem> splitSentenceTreeMap = SentenceParser.getInstance().parseSentence(sentence
                ,db
                ,kanjPositionArray
                ,wordvalues
                ,wordLoader);

        if(splitSentenceTreeMap == null) {
            correctmatch = false;
            System.out.println("#" + testnumber + ". splitSentenceTreeMap is NULL!!");
        } else {

//            ArrayList<Integer> returnedKeyEntries = new ArrayList<>();
            ArrayList<Integer> returnedKanjiIDs = new ArrayList<>();

            ArrayList<String> returnedConjugatedPieces = new ArrayList<>();

//            System.out.println("TREEMAP SIZE: " + splitSentenceTreeMap.size());

            /** pull ids of kanji in treemap into an integer list**/
            for(ParseSentenceItem entry : splitSentenceTreeMap) {
//                ArrayList<String> value = entry.getValue().;
//                returnedKeyEntries.add(entry.getKey());
                returnedConjugatedPieces.add(entry.getKanjiConjugated());
                returnedKanjiIDs.add(entry.getKanjiID());
            }

            ArrayList<Integer> kanjifinal_SUPERclean_integer = new ArrayList<>();

            if (splitSentenceTreeMap.size() > 0) {

                for (ParseSentenceItem entry : splitSentenceTreeMap) {
//                    int kanjiposition = entry.getKey();

//                    ArrayList<String> arrayList = splitSentenceTreeMap.get(kanjiposition);
                    final int kanjiID = entry.getKanjiID();
                    kanjifinal_SUPERclean_integer.add(kanjiID);
                }

            }
            String kanjiquerystring = getSelectedItemsAsString(kanjifinal_SUPERclean_integer);



            ArrayList<ArrayList<String>> Arraylist = new ArrayList<>();
//            HashMap<Integer,Integer> selectedHashMap = new HashMap<>();
            HashMap<Integer,ArrayList<String>> tmpmap = new HashMap<>();

            Cursor e = db.rawQuery("SELECT  x.[_id],x.[Kanji],x.[Furigana],x.[Definition],ifnull(y.[Correct],0),ifnull(y.[Total],0),(CASE WHEN [Total] >0 THEN CAST(ifnull([Correct],0)  as float)/[Total] ELSE 0 END) as [Percent] FROM (SELECT [_id],[Kanji],[Furigana],[Definition]  FROM [Edict] WHERE [_id] IN ("+ kanjiquerystring + ") ORDER BY [_id]) as x LEFT JOIN (SELECT [_id],sum([Correct]) as [Correct],sum([Total]) as [Total]  FROM [JScoreboard] WHERE [_id] IN (" + kanjiquerystring + ")  GROUP BY [_id]) as y ON x.[_id] = y.[_id]", null);
            e.moveToFirst();

            ArrayList<String> kanjiarray = new ArrayList<>();
            HashMap<Integer,String> idKanjiMap = new HashMap<>();

            while(! e.isAfterLast())

            {
                ArrayList<String> tmparray = new ArrayList<>();
                tmparray.add(e.getString(0)); //_id
                idKanjiMap.put(e.getInt(0),e.getString(1));
                tmparray.add(e.getString(1)); //Kanji (string data)
                tmparray.add(String.format("%.5f", e.getFloat(6))); //Percentage
                tmparray.add(e.getString(5)); //Total
                tmparray.add(e.getString(3)); //Definition
                tmparray.add(e.getString(2)); //Furigana

                int tmpx = 0;
                if(kanjifinal_SUPERclean_integer.contains(e.getInt(0))){
                    tmpx= kanjifinal_SUPERclean_integer.indexOf(e.getInt(0));
                }
                tmpmap.put(tmpx,tmparray);
                Arraylist.add(tmparray);

                e.moveToNext();
            }
            e.close();

            for (int j = 0; j < returnedKanjiIDs.size(); j++) {
                if(idKanjiMap.keySet().contains(returnedKanjiIDs.get(j))) {
                    kanjiarray.add(idKanjiMap.get(returnedKanjiIDs.get(j)));
                }

            }

            if(!arrayofHardCodedKanjiIDs.equals(returnedKanjiIDs)) {
                correctmatch = false;

                /** If the hardcoded arrays are empty, it means I am creating an example test, and
                 * just wish to get the correct keys, ids and kanji for that word/sentence combination.
                 * So in that case just print the output. Otherwise do the actual comparison. */
                if(arrayofHardCodedKanjiIDs.size() == 0
                        && arrayofHardCodedKanjis.size() == 0
                        && arrayofHardCodedKeys.size() == 0) {
                    System.out.println("******* BASELINE OUTPUTS ********");
                    System.out.println("#" + testnumber);
                    System.out.println("******* ******* ");

//                    System.out.println(", new ArrayList<Integer>(Arrays.asList(" + returnedKeyEntries + "))");
                    System.out.println(", new ArrayList<Integer>(Arrays.asList(" + returnedKanjiIDs + "))");
                    System.out.println(", new ArrayList<String>(Arrays.asList(" + kanjiarray + "))));");
                    System.out.println("------------------");

                } else {
                    System.out.println("******* OUTPUT KEYS/KANJI HAVE CHANGED ********");
//                    System.out.println("#" + testnumber + ". HardCodedKeys --" + arrayofHardCodedKeys);
//                    System.out.println("#" + testnumber + ". returnedKeyEntries --" + returnedKeyEntries);
                    System.out.println("------------------");
                    System.out.println("#" + testnumber + ". HardCodedkanjiIDs --" + arrayofHardCodedKanjiIDs);
                    System.out.println("#" + testnumber + ". returnedKanjiIDs --" + returnedKanjiIDs);
                    System.out.println("------------------");
                    System.out.println( "#" + testnumber + ". HardCodedkanji --" + arrayofHardCodedKanjis);
                    System.out.println("#" + testnumber + ". returnedKanji --" + kanjiarray);
                    System.out.println("------------------");
                    System.out.println("#" + testnumber + ". returnedConjugatedPieces --" + returnedConjugatedPieces);
                    System.out.println("------------------");
                    System.out.println("------------------");




//                    StringBuilder builder =


                }

            }
        }

        return correctmatch;
    }



    private String getSelectedItemsAsString(ArrayList<Integer> list ) {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < list.size(); ++i) {
            if(list.get(i) > 0) {

                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                sb.append(list.get(i).toString());

            }
        }
        return sb.toString();
    }


}
