//package com.jukuproject.jukutweet;
//
//import java.io.IOException;
//import java.net.HttpURLConnection;
//
//import oauth.signpost.OAuthConsumer;
//import oauth.signpost.exception.OAuthCommunicationException;
//import oauth.signpost.exception.OAuthExpectationFailedException;
//import oauth.signpost.exception.OAuthMessageSignerException;
//import oauth.signpost.http.HttpRequest;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//
///**
// * Created by JClassic on 3/20/2017.
// */
//
//public class SignedOkClient extends OkHttpClient {
//    private static final String TAG = "SignedOkClient";
//
//    private OAuthConsumer mConsumer =null;
//
//    public SignedOkClient(OAuthConsumer consumer) {
//        super();
//        mConsumer = consumer;
//    }
//
//    @Override
//    protected HttpURLConnection openConnection(Request request)
//            throws IOException {
//        HttpURLConnection connection = super.openConnection(request);
//        try {
//            HttpRequest signedReq = mConsumer.sign(connection);
//        } catch (OAuthMessageSignerException e) {
//            e.printStackTrace();
//        } catch (OAuthExpectationFailedException e) {
//            e.printStackTrace();
//        } catch (OAuthCommunicationException e) {
//            e.printStackTrace();
//        }
//        return connection;
//    }
//}