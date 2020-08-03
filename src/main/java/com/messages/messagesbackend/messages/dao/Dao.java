package com.messages.messagesbackend.messages.dao;

import com.messages.messagesbackend.messages.dto.MessageDto;
import com.messages.messagesbackend.messages.util.MessagesGenerator;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.List.of;

@AllArgsConstructor
@Repository
public class Dao {
    List<MessageDto> user1User2;
    List<MessageDto> user1User3;

    @Autowired
    public Dao(@Value("${app.placeholders.lorem-ipsum}") String loremIpsum) {
        user1User2 = new LinkedList<>();
        user1User3 = new LinkedList<>();

        new Thread(() -> {
            MessagesGenerator messagesGenerator = new MessagesGenerator(loremIpsum);
            try {
                while (true) {
                    String randomMessage = messagesGenerator.getRandomMessage();
                    addMessage("username3", "username1", randomMessage);

                    synchronized (this) {
                        wait(1000 * ThreadLocalRandom.current().nextInt(2, 9));
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }

    public List<String> getFriends(String username) {
            return switch (username) {
            case "username1" -> of("username2", "username3");
            case "username2" -> of("username1");
            default -> of();
        };
    }

    public List<MessageDto> getMessages(String username1, String username2) {
        if (username1.equals("username1")) {
            return switch (username2) {
                case "username2" -> user1User2;
                case "username3" -> user1User3;
                default -> of();
            };
        }
        if (username1.equals("username3") && username2.equals("username1")) {
            return user1User3;
        }

        return username1.equals("username2") && username2.equals("username1") ? user1User2 : of();
    }

    public void addMessage(String from, String to, String value) {
        addMessage(
                MessageDto.builder()
                        .from(from)
                        .to(to)
                        .value(value)
                        .build()
        );
    }

    public void addMessage(MessageDto messageDto) {
        getMessages(messageDto.getFrom(), messageDto.getTo()).add(messageDto);
    }
}
