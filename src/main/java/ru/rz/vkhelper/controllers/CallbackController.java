package ru.rz.vkhelper.controllers;

import com.vk.api.sdk.actions.Groups;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.groups.Group;
import com.vk.api.sdk.objects.groups.responses.GetByIdLegacyResponse;
import com.vk.api.sdk.objects.pages.responses.GetTitlesResponse;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.wall.Wallpost;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ru.rz.vkhelper.configuration.SecretConfig;
import ru.rz.vkhelper.dto.VkAuthenticationResponse;
import ru.rz.vkhelper.utils.MetricService;
import ru.rz.vkhelper.vk.VkSession;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/vkhelper")
@Slf4j
public class CallbackController {

    @Autowired
    SecretConfig secretConfig;

    @Value("${vk.app_id}")
    Integer appId;

    @Value("${vk.redirect_url}")
    String redirectUrl;

    @Autowired
    VkSession session;

    @Autowired
    MetricService metricService;

    private String buildTokenRequest(String code) {

        String clientSecret = secretConfig.getClientSecret();

        return String.format(
                "https://oauth.vk.com/access_token?client_id=%d&client_secret=%s&redirect_uri=%s&code=%s",
                appId,
                clientSecret,
                redirectUrl,
                code
        );
    }

    private VkAuthenticationResponse authenticate(String code) {
        String tokenRequest = buildTokenRequest(code);

        RestTemplate template = new RestTemplate();
        ResponseEntity<VkAuthenticationResponse> result =
                template.getForEntity(tokenRequest, VkAuthenticationResponse.class);

        log.info("Received authentication data from VK");

        return result.getBody();
    }

    private byte[] getFile(String url) {
        return new RestTemplate()
                .getForEntity(url, byte[].class)
                .getBody();
    }

    private void processPost(WallpostFull postFull, Set<Integer> sourceGroupIds) {
        int historySize = (postFull.getCopyHistory() != null) ?
                postFull.getCopyHistory().size() : -1;

        Wallpost post = postFull;

        log.info("{}, {}:{}; {}",
                post.getId(), new Date(1000L * post.getDate()), post.getText(), historySize);

        if (historySize == 1) {
            post = postFull.getCopyHistory().get(0);
            log.info("Repost from id: {}", post.getOwnerId());
            sourceGroupIds.add(post.getOwnerId());
        }

        if (null == post.getAttachments()) {
            return;
        }
        for (WallpostAttachment a : post.getAttachments()) {
            if (null == a.getPhoto()) {
                continue;
            }

            List<PhotoSizes> sizes = a.getPhoto().getSizes();

            PhotoSizes img = sizes.get(sizes.size() - 1);
            log.info(img.getUrl().toASCIIString());

            String fileName = "images/" + postFull.getId() + ".jpg";

            if (new File(fileName).exists()) {
                log.info("File {} exists. Skipping", fileName);
            }
            else {
                try {
                    byte[] data = getFile(img.getUrl().toASCIIString());
                    IOUtils.write(data, new FileOutputStream(fileName));
                } catch (HttpClientErrorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @GetMapping("/callback")
    public void callback(@RequestParam String code) throws ClientException, ApiException, InterruptedException, IOException {
        log.info("Callback invoked with code {}", code);

        VkAuthenticationResponse response = authenticate(code);

        //VkSession session = new VkSession(response);
        session.start(response);

        int offset = 0;

        HashSet<Integer> sourceGroupIds = new HashSet<>();

        while (true) {

            List<WallpostFull> posts = session.loadPosts(offset);

            int loadedCount = posts.size();

            if (loadedCount == 0) {
                break;
            }

            offset += loadedCount;

            for (WallpostFull postFull : posts) {
                processPost(postFull, sourceGroupIds);
            }
        }

        log.info("Total records: {}", offset);

        for (Integer id: sourceGroupIds) {
            if (id >= 0) {
                continue;
            }
            log.info(session.getGroupName(-id));
        }

        metricService.printRetriedCalls();
    }
}
