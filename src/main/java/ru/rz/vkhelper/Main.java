package ru.rz.vkhelper;

//import com.vk.api.sdk.client.TransportClient;
//import com.vk.api.sdk.client.VkApiClient;
//import com.vk.api.sdk.client.actors.UserActor;
//import com.vk.api.sdk.httpclient.HttpTransportClient;
//import com.vk.api.sdk.objects.UserAuthResponse;

public class Main {

    public static void XXXXmain(String[] args){
//        TransportClient transportClient = HttpTransportClient.getInstance();
//        VkApiClient vk = new VkApiClient(transportClient);

//        VK
//                appId
//        7842564
//        secure key
//        I9ep2Y4Chbw3FeqaWhiG
//        service key
//        52dca8e352dca8e352dca8e3e952ab03e7552dc52dca8e33250f9054981989ea829c7f2

//        appid
//        7842612
//        key
//        3f6SP2Lmxa9wUfeSf2II
//        service key
//        f9c8fddff9c8fddff9c8fddf52f9bf56ebff9c8f9c8fddf9944a3651ecf8e7d92691b0d

        // https://oauth.vk.com/authorize?client_id=7842612&redirect_uri=http://localhost/callback&display=page&response_type=code&v=5.130

        //RestTemplate

        Integer APP_ID = 7842564;
        String CLIENT_SECRET = "I9ep2Y4Chbw3FeqaWhiG";
        String REDIRECT_URI;
        String code;

    //        UserAuthResponse authResponse = vk.oAuth()
    //                .userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, REDIRECT_URI, code)
    //                .execute();
    //
    //        UserActor actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
    }
}
