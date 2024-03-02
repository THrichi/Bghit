package com.application.bghit.dtos;

import com.application.bghit.entities.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomDto {
    private Long id;
    private ChatUserDto user1;
    private ChatUserDto user2;
    private List<ChatMessage> messages;
}
