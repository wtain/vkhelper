package ru.rz.vkhelper.vk;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.groups.responses.GetByIdLegacyResponse;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.rz.vkhelper.dto.VkAuthenticationResponse;
import ru.rz.vkhelper.utils.MetricService;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class VkSession {

    private static final int MAX_POST_COUNT = 100;

    private static final int THROTTLING_SLEEP_INTERVAL_MS = 1000;

    private static final int RETRY_ATTEMPTS = 3;

    private static final double BACKOFF_MULTIPLIER = 1.5;

    VkApiClient vk;
    UserActor actor;

    @Autowired
    MetricService metricService;

    public void start(VkAuthenticationResponse authResponse) {
        TransportClient transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);

        actor = new UserActor(authResponse.getUser_id(),
                authResponse.getAccess_token());
    }

    @Retryable(value = ApiTooManyException.class,
            maxAttempts = RETRY_ATTEMPTS,
            backoff = @Backoff(delay = THROTTLING_SLEEP_INTERVAL_MS,
                    multiplier = BACKOFF_MULTIPLIER))
    public List<WallpostFull> loadPosts(int offset) throws ClientException, ApiException {
        metricService.pushCall("loadPosts", offset);
        log.info("Running loadPosts with offset={}", offset);
        return vk.wall()
                .get(actor)
                .offset(offset)
                .count(MAX_POST_COUNT)
                .execute()
                .getItems();
    }

    @Retryable(value = { ApiTooManyException.class },
            maxAttempts = RETRY_ATTEMPTS,
            backoff = @Backoff(delay = THROTTLING_SLEEP_INTERVAL_MS,
                    multiplier = BACKOFF_MULTIPLIER))
    public String getGroupName(Integer id) throws ClientException, ApiException {
        metricService.pushCall("getGroupName", id);
        log.info("Running getGroupName with id={}", id);
        return vk.groups()
                .getByIdLegacy(actor)
                .groupIds(Collections.singletonList(id.toString()))
                .execute()
                .get(0)
                .getName();
    }
}
