package com.application.bghit.dtos;

import com.application.bghit.entities.ChatMessage;
import com.application.bghit.entities.Room;
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
    private Room.RoomStatus status;
    private Long blockedUser;
    private Long archivedUserId1;
    private Long archivedUserId2;
    private DemandeListDto demande;
}
