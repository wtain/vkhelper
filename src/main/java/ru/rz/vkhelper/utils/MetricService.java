package ru.rz.vkhelper.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MetricService {

    ConcurrentHashMap<String, Integer> callCounts = new ConcurrentHashMap<>();

    private static String getCallString(String name, Object[] parameters) {
        return name + "(" + Arrays.stream(parameters).map(Object::toString)
                .collect(Collectors.joining(",")) + ")";
    }

    public void pushCall(String name, Object ... parameters) {
        String callString = getCallString(name, parameters);
        callCounts.put(callString, callCounts.getOrDefault(callString, 0) + 1);
    }

    public void printRetriedCalls() {
        callCounts.entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .forEach(e -> log.info(e.getKey()));
    }
}
