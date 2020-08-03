package com.messages.messagesbackend.messages.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MessageDto {
    String from, to, value;
}
