package com.messages.messagesbackend.messages.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class MessagesGenerator {
    private final String[] loremIpsum;

    @Autowired
    public MessagesGenerator(@Value("${app.placeholders.lorem-ipsum}") String loremIpsumString) {
        loremIpsum = loremIpsumString.replaceAll("[.,]", "").toLowerCase().split(" ");
    }

    public String getRandomMessage() {
        int start = ThreadLocalRandom.current().nextInt(loremIpsum.length - 3);
        int end = ThreadLocalRandom.current().nextInt(start + 3, Math.min(start + 20, loremIpsum.length));

        StringBuilder sb = new StringBuilder();
        for(int i = start; i < end; i++) {
            sb.append(loremIpsum[i]).append(' ');
        }

        sb.deleteCharAt(sb.length() - 1);

        if(end - start > 3)     sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        if(end - start > 10)    sb.append('.');

        return sb.toString();
    }
}

