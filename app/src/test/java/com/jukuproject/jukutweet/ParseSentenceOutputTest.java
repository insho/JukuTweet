package com.jukuproject.jukutweet;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.annotation.Config;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import com.jukuproject.jukutweet.Database.ExternalDB;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.WordEntry;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static android.database.sqlite.SQLiteDatabase.OPEN_READWRITE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;


/**
 * Tests the {@link TweetParser} by passing test sentences through the parser
 * and comparing the output against hard-coded expected output for that sentence. Used to make
 * sure a change to the parser to capture a certain kanji set doesn't negatively effect another type of sentence.
 */
@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest(ExternalDB.class)
@Config(constants = BuildConfig.class, sdk = LOLLIPOP, packageName = "com.jukuproject.juku")
public class ParseSentenceOutputTest extends AndroidTestCase {
    SQLiteDatabase db;

    @Mock
    private SQLiteDatabase database;
    @Mock
    private InternalDB internalDB;

    @Before
    public void setUp() throws Exception {

        /* Importing a special "testDB" from the resources file in Test folder. This db mocks the combination of
         * internal and external "JQuiz.db" databases that exists in the app. In the app InternalDB class creates tables related
         * to wordlists and word scores, and the ExternalDB imports the Edict dictionary and reference tables, which are queried
         * together from the same source. So the testDB includes the externalDB tables as well as blank versions of the internalDB tables,
         * to mimick the app. The db is passed */
        File file = new File(this.getClass().getClassLoader().getResource("JQuizTest.db").getFile());
        String dbPath = file.getAbsolutePath();
        db = database.openDatabase(dbPath, null, OPEN_READWRITE);

        internalDB = InternalDB.getInstance(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    /**
     *   TweetParser ({@link TweetParser}) black box output tests. Sends a sentence through the Tweet Parser
     *   methods and compares the output (i.e. the kanji found in the sentence) against a hard-coded list of correct
     *   answers for that sentence. This helps ensure that when the parser methods are changed to encorporate something new,
     *   the current correct answers are not effected.
     *
     *   If something has changed, the debug comments in the TweetParser can be traced back to locate where and why the improvement/problem occurred
     *   for that sentence.
    * */
    @Test
    public void sentenceParserOutPutsTest1() {
        assertTrue(performSentenceParserTest(db,1,"お菓子","彼は一箱のお菓子を友達全員と分け合った。"
                ,new ArrayList<Integer>(Arrays.asList(21213, 90659, 108, 23852, 16769, 201510))
                , new ArrayList<String>(Arrays.asList("彼", "一箱", "お菓子", "友達", "全員", "分け合う"))));
    }

    @Test
    public void sentenceParserOutPutsTest2() {
        assertTrue(performSentenceParserTest(db,2,"お兄さん","彼はお兄さんと同じように頭がいい。"
                ,new ArrayList<Integer>(Arrays.asList(21213, 116, 31304, 185158))
                , new ArrayList<String>(Arrays.asList("彼", "お兄さん", "と同じように", "頭がいい"))));
    }

    @Test
    public void sentenceParserOutPutsTest3() {
        assertTrue(performSentenceParserTest(db,3,"お兄さん","次のような問題を想像してください。あなたのお兄さんが自動車事故にあったとしましょう。"
                ,new ArrayList<Integer>(Arrays.asList(12769, 23683, 16950, 116, 139631))
                , new ArrayList<String>(Arrays.asList("次", "問題", "想像", "お兄さん", "自動車事故"))));
    }

    @Test
    public void sentenceParserOutPutsTest4() {
        assertTrue(performSentenceParserTest(db,4,"猫","彼は一日中行方不明の猫を探した。"
                ,new ArrayList<Integer>(Arrays.asList(21213, 5634, 127716, 20459, 18009))
                , new ArrayList<String>(Arrays.asList("彼", "一日中", "行方不明", "猫", "探す"))));
    }

    @Test
    public void sentenceParserOutPutsTest5() {
        assertTrue(performSentenceParserTest(db,5,"亡き母","亡き母の写真を見るたびに、胸に熱いものが込み上げてくる。"
                ,new ArrayList<Integer>(Arrays.asList(206031, 13107, 9930, 8911, 20465, 130034))
                , new ArrayList<String>(Arrays.asList("亡き母", "写真", "見る", "胸", "熱い", "込み上げる"))));
    }

    @Test
    public void sentenceParserOutPutsTest6() {
        assertTrue(performSentenceParserTest(db,6,"煩い","その煩い音には我慢できない。"
                ,new ArrayList<Integer>(Arrays.asList(21164, 6383, 6901))
                , new ArrayList<String>(Arrays.asList("煩い", "音", "我慢"))));
    }


    //TODO FIX
    @Test
    public void sentenceParserOutPutsTest7() {
        assertTrue(performSentenceParserTest(db,7,"半分","その生徒は授業中半分眠っていた。"
                ,new ArrayList<Integer>(Arrays.asList(16053, 143910, 21085, 23255))
                , new ArrayList<String>(Arrays.asList("生徒", "授業中", "半分", "眠る"))));
    }

    @Test
    public void sentenceParserOutPutsTest8() {
        assertTrue(performSentenceParserTest(db,8,"半分","私たちは居間の半分の場所を取るグランドピアノを買った。"
                ,new ArrayList<Integer>(Arrays.asList(12486, 8626, 21085, 151402, 1545, 20783))
                , new ArrayList<String>(Arrays.asList("私たち", "居間", "半分", "場所を取る", "グランドピアノ","買う"))));
    }

    @Test
    public void sentenceParserOutPutsTest9() {
        assertTrue(performSentenceParserTest(db,9,"疲れる","冬は疲れる。"
                ,new ArrayList<Integer>(Arrays.asList(19488, 21278))
                , new ArrayList<String>(Arrays.asList("冬", "疲れる"))));
    }

    @Test
    public void sentenceParserOutPutsTest10() {
        assertTrue(performSentenceParserTest(db,10,"疲れる","私が疲れるのは、暑さというよりはむしろ湿度のせいだ。"
                ,new ArrayList<Integer>(Arrays.asList(12480, 21278, 14227, 13009))
                , new ArrayList<String>(Arrays.asList("私", "疲れる", "暑さ", "湿度"))));

    }

    @Test
    public void sentenceParserOutPutsTest11() {
        assertTrue(performSentenceParserTest(db,11,"飛行機","新聞報道によれば昨日飛行機事故があった模様である。"
                ,new ArrayList<Integer>(Arrays.asList(153995, 11874, 21359, 12641, 23542))
                , new ArrayList<String>(Arrays.asList("新聞報道", "昨日", "飛行機", "事故", "模様"))));

    }

    @Test
    public void sentenceParserOutPutsTest12() {
        assertTrue(performSentenceParserTest(db,12,"飛行機","爆発が起こった。あっと言う間に、その飛行機は燃え上がり、墜落した。"
                ,new ArrayList<Integer>(Arrays.asList(20920, 8275, 26440, 21359, 20547, 18813))
                , new ArrayList<String>(Arrays.asList("爆発", "起こる", "あっと言う間に", "飛行機", "燃え上がる", "墜落"))));

    }

    @Test
    public void sentenceParserOutPutsTest13() {
        assertTrue(performSentenceParserTest(db,13,"飛行機","地上で空費される時間が飛行機の速さという有利な店を帳消しにしてしまう。"
                ,new ArrayList<Integer>(Arrays.asList(18225, 115729, 12740, 21359, 17289, 23914, 19196, 18551))
                , new ArrayList<String>(Arrays.asList("地上", "空費", "時間", "飛行機", "速さ", "有利", "店", "帳消し"))));

    }

    @Test
    public void sentenceParserOutPutsTest14() {
        assertTrue(performSentenceParserTest(db,14,"病院","典型的な日本の赤ん坊は、病院から家に帰ると母親と一緒に寝る。"
                ,new ArrayList<Integer>(Arrays.asList(19143, 20322, 16282, 21536, 98043, 22578, 5543, 15059))
                , new ArrayList<String>(Arrays.asList("典型的", "日本", "赤ん坊", "病院", "家に帰る", "母親", "一緒に", "寝る"))));

    }


    @Test
    public void sentenceParserOutPutsTest15() {
        assertTrue(performSentenceParserTest(db,15,"病院","あなたの寄附は小児病院を建設する補助として、大いに役立つでしょう。"
                ,new ArrayList<Integer>(Arrays.asList(8055, 148964, 9820, 22540, 17699, 23769))
                , new ArrayList<String>(Arrays.asList("寄附", "小児", "病院", "建設", "補助", "大いに", "役立つ"))));
    }


    @Test
    public void sentenceParserOutPutsTest16() {
        assertTrue(performSentenceParserTest(db,16,"部屋","彼女が部屋に入ると私はすぐに立ち上がり、深々と頭を下げた。"
                ,new ArrayList<Integer>(Arrays.asList(21227, 21898, 20367, 12480, 24440, 154094, 185234))
                , new ArrayList<String>(Arrays.asList("彼女", "部屋", "入る", "私", "立ち上がり", "深々", "頭を下げる"))));
    }

    @Test
    public void sentenceParserOutPutsTest17() {
        assertTrue(performSentenceParserTest(db,17,"部屋","その部屋に入るや否や私は、煙草の臭いのほかにガスの匂いがするのに気がついた。"
                ,new ArrayList<Integer>(Arrays.asList(21898, 20367, 33724, 12480, 6125, 13730, 20250, 8152))
                , new ArrayList<String>(Arrays.asList("部屋", "入る", "や否や", "私", "煙草", "臭い", "匂い", "気"))));

    }

    @Test
    public void sentenceParserOutPutsTest18() {
        assertTrue(performSentenceParserTest(db,18,"封筒","秘書は手紙を封筒の中に差し込んだ。"
                ,new ArrayList<Integer>(Arrays.asList(21293, 13416, 21922, 18362, 130791))
                , new ArrayList<String>(Arrays.asList("秘書", "手紙", "封筒", "中", "差し込む"))));

    }

    @Test
    public void sentenceParserOutPutsTest19() {
        assertTrue(performSentenceParserTest(db,19,"風","彼らは風と荒天のなすがままに、航海中に行方不明になった。"
                ,new ArrayList<Integer>(Arrays.asList(21217, 21923, 127361, 10968, 18362, 127716))
                , new ArrayList<String>(Arrays.asList("彼ら", "風", "荒天", "航海", "中", "行方不明"))));

    }

    @Test
    public void sentenceParserOutPutsTest20() {
        assertTrue(performSentenceParserTest(db,20,"服","小島先生は朝食後たばこを１服吸った。"
                ,new ArrayList<Integer>(Arrays.asList(14495, 16456, 177332))
                , new ArrayList<String>(Arrays.asList("小島", "先生", "朝食後", "服", "吸う"))));

    }


    @Test
    public void sentenceParserOutPutsTest21() {
        assertTrue(performSentenceParserTest(db,21,"分かる","その後援者は本物の骨董品の良さが分かる。"
                ,new ArrayList<Integer>(Arrays.asList(123143, 22975, 129991, 24630, 22095))
                , new ArrayList<String>(Arrays.asList("後援者", "本物", "骨董品", "良さ", "分かる"))));

    }

    @Test
    public void sentenceParserOutPutsTest22() {
        assertTrue(performSentenceParserTest(db,22,"分かる","年賀状のおかげで私達は友達や親戚の消息が分かる。"
                ,new ArrayList<Integer>(Arrays.asList(20488, 137061, 23852, 15364, 14590, 22095))
                , new ArrayList<String>(Arrays.asList("年賀状", "私達", "友達", "親戚", "消息", "分かる"))));

    }

    @Test
    public void sentenceParserOutPutsTest23() {
        assertTrue(performSentenceParserTest(db,23,"文章","次の文章を日本語に直しなさい。"
                ,new ArrayList<Integer>(Arrays.asList(12769, 22197, 20333, 18750))
                , new ArrayList<String>(Arrays.asList("次", "文章", "日本語", "直す"))));

    }


    @Test
    public void sentenceParserOutPutsTest24() {
        assertTrue(performSentenceParserTest(db,24,"文章","文法的に正しい文章を作るよう心がけるべきだ。"
                ,new ArrayList<Integer>(Arrays.asList(202341, 15924, 22197, 11837, 15092))
                , new ArrayList<String>(Arrays.asList("文法的", "正しい", "文章", "作る", "心がける"))));

    }


    @Test
    public void sentenceParserOutPutsTest25() {
        assertTrue(performSentenceParserTest(db,25,"並ぶ","英国人は列を作って並ぶのに慣れている。"
                ,new ArrayList<Integer>(Arrays.asList(93564, 24818, 11837, 22286, 7699))
                , new ArrayList<String>(Arrays.asList("英国人", "列", "作る", "並ぶ", "慣れる"))));
    }


    @Test
    public void sentenceParserOutPutsTest26() {
        assertTrue(performSentenceParserTest(db,26,"並ぶ","彼は古今に並ぶ者のない偉大な政治家である。"
                ,new ArrayList<Integer>(Arrays.asList(21213, 10244, 22286, 13166, 5260, 15884))
                , new ArrayList<String>(Arrays.asList("彼", "古今", "並ぶ", "者", "偉大", "政治家"))));
    }

    @Test
    public void sentenceParserOutPutsTest27() {
        assertTrue(performSentenceParserTest(db,27,"便利","これは主婦の手間を省く便利な器具です。"
                ,new ArrayList<Integer>(Arrays.asList(13315, 13403, 14642, 22437, 8009))
                , new ArrayList<String>(Arrays.asList("主婦", "手間", "省く", "便利", "器具"))));
    }

    //TODO FIX
    @Test
    public void sentenceParserOutPutsTest28() {
        assertTrue(performSentenceParserTest(db,28,"頁","１０頁を参照してください。"
                ,new ArrayList<Integer>(Arrays.asList(22317, 12023))
                , new ArrayList<String>(Arrays.asList("頁", "参照"))));
    }


    @Test
    public void sentenceParserOutPutsTest29() {
        assertTrue(performSentenceParserTest(db,29,"勉強","私たちの先生は高校時代に英語を一生懸命勉強したに違いない。"
                ,new ArrayList<Integer>(Arrays.asList(12486, 16456, 128170, 5988, 5575, 22441, 31759))
                ,new ArrayList<String>(Arrays.asList("私たち", "先生", "高校時代", "英語", "一生懸命", "勉強", "に違いない"))));

    }


    @Test
    public void sentenceParserOutPutsTest30() {
        assertTrue(performSentenceParserTest(db,30,"お菓子","知らない人からお菓子をもらわない方がいいよ。"
                ,new ArrayList<Integer>(Arrays.asList(174657, 15454, 108, 205361))
                , new ArrayList<String>(Arrays.asList("知らない", "人", "お菓子", "方がいい"))));

    }


    //TODO FIX
    @Test
    public void sentenceParserOutPutsTest31() {
        assertTrue(performSentenceParserTest(db,31,"お茶","お茶碗一杯のご飯は約、１８０ｇです。"
                ,new ArrayList<Integer>(Arrays.asList(150, 221080, 5642, 0, 269, 0, 23770, 0))
                , new ArrayList<String>(Arrays.asList("お茶", "碗", "一杯", "ご飯", "約"))));
    }

    @Test
    public void sentenceParserOutPutsTest32() {
        assertTrue(performSentenceParserTest(db,32,"お茶","その夕方不気味な沈黙のうちにお茶がすまされた。"
                , new ArrayList<Integer>(Arrays.asList(23981, 21615, 18788, 150))
                , new ArrayList<String>(Arrays.asList("夕方", "不気味", "沈黙", "お茶"))));
    }

    @Test
    public void sentenceParserOutPutsTest33() {
        assertTrue(performSentenceParserTest(db,33,"緑","緑色植物は自分自身の食物を作ることができる。"
                , new ArrayList<Integer>(Arrays.asList(218358, 12895, 15002, 11837))
                , new ArrayList<String>(Arrays.asList("緑色植物","自分自身", "食物", "作る"))));

    }


    @Test
    public void sentenceParserOutPutsTest34() {
        assertTrue(performSentenceParserTest(db,34,"緑","苔は、倒れた丸太や岩の上の繊細な緑の柔毛だと私は心の中で思う。"
                , new ArrayList<Integer>(Arrays.asList(171009, 19471, 7916, 7936, 14706, 16636, 24671, 145384, 12480, 153244, 12358))
                , new ArrayList<String>(Arrays.asList("苔", "倒れる", "丸太", "岩", "上", "繊細", "緑", "柔毛", "私", "心の中", "思う"))));

    }


    @Test
    public void sentenceParserOutPutsTest35() {
        assertTrue(performSentenceParserTest(db,35,"薬","お前は病気が直りたいのならこの薬を飲んだ方がいいよ。"
                , new ArrayList<Integer>(Arrays.asList(140, 21537, 18754, 23774, 5828, 205361))
                , new ArrayList<String>(Arrays.asList("お前", "病気", "直る", "薬", "飲む", "方がいい"))));
    }


    @Test
    public void sentenceParserOutPutsTest36() {
        assertTrue(performSentenceParserTest(db,36,"薬","米国の親の中には、息子を麻薬に近づけないためにフットボールを勧めるものが多い。"
                , new ArrayList<Integer>(Arrays.asList(22310, 15347, 175932, 17249, 23025, 114202, 3730, 7600, 17390))
                , new ArrayList<String>(Arrays.asList("米国", "親", "中には", "息子", "麻薬", "近づく", "フットボール","勧める", "多い"))));

    }


    @Test
    public void sentenceParserOutPutsTest37() {
        assertTrue(performSentenceParserTest(db,37,"野菜","彼女は好んで新鮮な生野菜を食べます。"
                , new ArrayList<Integer>(Arrays.asList(21227, 10747, 15192, 160109, 14977))
                , new ArrayList<String>(Arrays.asList("彼女", "好んで", "新鮮", "生野菜", "食べる"))));

    }


    @Test
    public void sentenceParserOutPutsTest38() {
        assertTrue(performSentenceParserTest(db,38,"野菜","先月は野菜の値段が高くなったので、食料品代が増えた。"
                , new ArrayList<Integer>(Arrays.asList(16435, 23727, 18171, 11104, 15006, 17654, 17170))
                , new ArrayList<String>(Arrays.asList("先月", "野菜", "値段", "高い", "食料品", "代", "増える"))));

    }


    @Test
    public void sentenceParserOutPutsTest39() {
        assertTrue(performSentenceParserTest(db,39,"混む","湘南の海水浴場は日曜日にはとても混む。"
                , new ArrayList<Integer>(Arrays.asList(7099, 20345, 11501))
                , new ArrayList<String>(Arrays.asList("海水浴場", "日曜日", "混む"))));

    }


    @Test
    public void sentenceParserOutPutsTest40() {
        assertTrue(performSentenceParserTest(db,40,"飲み込む","食べ物を飲み込むとのどが痛みます。"
                , new ArrayList<Integer>(Arrays.asList(14979, 5825, 18847))
                , new ArrayList<String>(Arrays.asList("食べ物", "飲み込む", "痛む"))));

    }


    @Test
    public void sentenceParserOutPutsTest_ProblemSentence41() {
        assertTrue(performSentenceParserTest(db,41,"いつの間にか","私はいつの間にかぐっすり眠っていた。"
                , new ArrayList<Integer>(Arrays.asList(12480, 77, 23255))
                , new ArrayList<String>(Arrays.asList("私", "いつの間にか", "眠る"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence42() {
        assertTrue(performSentenceParserTest(db,42,"いつの間にか","いつの間にか小鳥たちは見えなくなった。"
                , new ArrayList<Integer>(Arrays.asList(77, 14492, 9913))
                , new ArrayList<String>(Arrays.asList("いつの間にか", "小鳥", "見える"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence43() {
        assertTrue(performSentenceParserTest(db,43,"お休み","明日はお休みです。"
                , new ArrayList<Integer>(Arrays.asList(23453, 113))
                , new ArrayList<String>(Arrays.asList("明日", "お休み"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence44() {
        assertTrue(performSentenceParserTest(db,44,"お休み","お休みはあっと言う間に終わってしまった。"
                , new ArrayList<Integer>(Arrays.asList(113, 26440, 13704))
                , new ArrayList<String>(Arrays.asList("お休み", "あっと言う間に", "終わる"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence45() {
        assertTrue(performSentenceParserTest(db,45,"お休み","いよいよ今週を限りに、しばらくのお休みです。"
                , new ArrayList<Integer>(Arrays.asList(11434, 34008, 113))
                , new ArrayList<String>(Arrays.asList("今週", "を限りに", "お休み"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence46() {
        assertTrue(performSentenceParserTest(db,46,"お帰り","お帰りになったら電話を下さい。"
                , new ArrayList<Integer>(Arrays.asList(110, 19330, 6410))
                , new ArrayList<String>(Arrays.asList("お帰り", "電話", "下さい"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence47() {
        assertTrue(performSentenceParserTest(db,47,"お帰り","あなたのお帰りを一日千秋の思いでお待ちしています。"
                , new ArrayList<Integer>(Arrays.asList(110, 90612, 136177, 17577))
                , new ArrayList<String>(Arrays.asList("お帰り", "一日千秋", "思いで", "待つ"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence48() {
        assertTrue(performSentenceParserTest(db,48,"お手伝いさん","お手伝いさんはすぐに食卓から食器をかたづけた。"
                , new ArrayList<Integer>(Arrays.asList(130, 14992, 14987))
                , new ArrayList<String>(Arrays.asList("お手伝いさん", "食卓", "食器"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence49() {
        assertTrue(performSentenceParserTest(db,49,"お手伝いさん","今日ではお手伝いさんを雇う余裕のある人は少ない。"
                , new ArrayList<Integer>(Arrays.asList(130238, 130, 10371, 24034, 26599, 14523))
                , new ArrayList<String>(Arrays.asList("今日では", "お手伝いさん", "雇う", "余裕", "ある人", "少ない"))));
    }

    @Test
    public void sentenceParserOutPutsTest_ProblemSentence50() {
        assertTrue(performSentenceParserTest(db,50,"お手伝いさん","お手伝いさんは毎日の家事にすっかり飽きてしまった。"
                , new ArrayList<Integer>(Arrays.asList(130, 23057, 6668, 22763))
                , new ArrayList<String>(Arrays.asList("お手伝いさん", "毎日", "家事", "飽きる"))));
    }


// PROBLEM SENTENCES
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


    /**
     * Runs an example sentence through the TweetParser and, if the output Kanji ids are incorrect,
     * prints the actual/expected sets so that they can be compared (accepted as a better answer, or corrected)
     * @param db sqlite test database
     * @param testnumber integer representing sentence #, used for display purposes
     * @param ex_focused_kanji_full A kanji that is "focused" in the sentence (like if 馬鹿　were looked up in a search,
     *                              the focused kanji would be 馬鹿, and it would be expected that the parser would find 馬鹿
     *                              in the sentence). In this context, for testing purposes it's essentially ignored.
     * @param sentence Sentence to be broken up
     * @param arrayofHardCodedKanjiIDs correct set of Kanji ids that the parser should return
     * @param arrayofHardCodedKanjis correct set of Kanji that the parser should return
     * @return TRUE if parse matched expected answers, false if not
     */
    private boolean performSentenceParserTest(SQLiteDatabase db
            ,Integer testnumber
            , String ex_focused_kanji_full
            , String sentence
            , ArrayList<Integer> arrayofHardCodedKanjiIDs
            , ArrayList<String> arrayofHardCodedKanjis) {

        boolean correctmatch = true;

        //Assign dummy color threshold
        ColorThresholds colorThresholds =  new ColorThresholds(2
                ,.3f
                ,.8f);

        //Parse the sentence with TweetParser class
        ArrayList<WordEntry> parsedEntries = new TweetParser().parseSentence(getContext()
                ,InternalDB.getTestInstance(getContext(),db)
                ,sentence
                ,new ArrayList<String>()
                ,colorThresholds);

        if(parsedEntries == null) {
            correctmatch = false;
            System.out.println("#" + testnumber + ". RESULTS ARE NULL!!");
        } else {

            ArrayList<Integer> returnedKanjiIDs = new ArrayList<>();
            ArrayList<String> returnedConjugatedPieces = new ArrayList<>();

            /* pull ids of kanji in treemap into an integer list */
            for(WordEntry entry : parsedEntries) {
                returnedConjugatedPieces.add(entry.getKanji());
                returnedKanjiIDs.add(entry.getId());
            }

            if(!arrayofHardCodedKanjiIDs.equals(returnedKanjiIDs)) {
                correctmatch = false;

                /* If the hardcoded arrays are empty, it means I am creating an example test for a new sentence, and
                 * just wish to get the correct keys, ids and kanji for that word/sentence combination.
                 * So in that case just print the output. Otherwise do the actual comparison. */
                if(arrayofHardCodedKanjiIDs.size() == 0
                        && arrayofHardCodedKanjis.size() == 0) {
                    System.out.println("******* BASELINE OUTPUTS ********");
                    System.out.println("#" + testnumber);
                    System.out.println("******* ******* ");

                    System.out.println(", new ArrayList<Integer>(Arrays.asList(" + returnedKanjiIDs + "))");
                    System.out.println(", new ArrayList<String>(Arrays.asList(" + returnedConjugatedPieces + "))));");
                    System.out.println("------------------");

                } else {
                    System.out.println("******* OUTPUT KEYS/KANJI HAVE CHANGED ********");
                    System.out.println("------------------");
                    System.out.println("#" + testnumber + ". HardCodedkanjiIDs --" + arrayofHardCodedKanjiIDs);
                    System.out.println("#" + testnumber + ". returnedKanjiIDs --" + returnedKanjiIDs);
                    System.out.println("------------------");
                    System.out.println( "#" + testnumber + ". HardCodedkanji --" + arrayofHardCodedKanjis);
                    System.out.println("#" + testnumber + ". returnedKanji --" + returnedConjugatedPieces);
                    System.out.println("------------------");
                    System.out.println("#" + testnumber + ". returnedConjugatedPieces --" + returnedConjugatedPieces);
                    System.out.println("------------------");
                    System.out.println("------------------");

                }
            }
        }

        return correctmatch;
    }
}
