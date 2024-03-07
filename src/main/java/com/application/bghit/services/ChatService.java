package com.application.bghit.services;

import com.application.bghit.entities.ChatMessage;
import com.application.bghit.entities.Demande;
import com.application.bghit.entities.Room;
import com.application.bghit.entities.User;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.repositories.ChatMessageRepository;
import com.application.bghit.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final RoomRepository roomRepository;
    private final UserService userService;

    private final ChatMessageRepository chatMessageRepository;

    public Room createRoom(Long userId1, Long userId2) throws AppException {
        Room room = new Room();
        Optional<User> user1 = userService.findById(userId1);
        Optional<User> user2 = userService.findById(userId2);
        if(user1.isEmpty() || user2.isEmpty())
        {
            throw new AppException("User Not Found", HttpStatus.NOT_FOUND);
        }
        room.setUser1(user1.get());
        room.setUser2(user2.get());
        return roomRepository.save(room);
    }

    public ChatMessage addMessageToRoom(Long roomId, ChatMessage message) {

        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        message.setRoom(room);
        return chatMessageRepository.save(message);
    }
    public ChatMessage updateMessageStatus(Long messageId, ChatMessage.MessageStatus status) throws AppException {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException("Message Not Found", HttpStatus.NOT_FOUND));

        message.setStatus(status);
        return chatMessageRepository.save(message);
    }


    public Optional<Room> findRoomById(Long roomId)
    {
        return roomRepository.findById(roomId);
    }
    public List<ChatMessage> getMessagesForRoom(Long roomId) {
        return chatMessageRepository.findByRoomId(roomId);
    }

    public Optional<Room> findRoomByUsers(Long senderId, Long recipientId) {
        return roomRepository.findRoomByUsers(senderId,recipientId);
    }

    public List<Room> findRoomsByUser(Long roomId, Room.RoomStatus status, Long userId) {
        if(status == null)
        {
            return roomRepository.findRoomsByUser(roomId);
        }else{
            if(status.equals(Room.RoomStatus.ARCHIVED))
            {
                return roomRepository.findRoomsArchivedByUser(userId);
            }
            return roomRepository.findRoomsByUser(roomId,status);
        }
    }

    public void updateMessagesStatusInRoom(Long userId,Long roomId, ChatMessage.MessageStatus status) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdAndStatus(userId,roomId, ChatMessage.MessageStatus.SENT);

        for (ChatMessage message : messages) {
            message.setStatus(status);
        }
        chatMessageRepository.saveAll(messages);
    }
    public boolean changeRoomStatus(Long roomId, Room.RoomStatus newStatus) {
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();
            room.setStatus(newStatus);
            room.setBlockedUser(null);
            roomRepository.save(room);
            return true;
        }
        return false;
    }
    public Room saveRoom(Room room)
    {
        return roomRepository.save(room);
    }
    public boolean archiveRoom(Long roomId, Long archivedId) {
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();
            if(room.getArchivedUserId1() == null)
            {
                room.setArchivedUserId1(archivedId);
            }else{
                room.setArchivedUserId2(archivedId);
            }
            room.setBlockedUser(null);
            roomRepository.save(room);
            return true;
        }
        return false;
    }

    public boolean blockRoom(Long roomId, Room.RoomStatus roomStatus, Long blockedUser) {
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();
            room.setStatus(roomStatus);
            room.setBlockedUser(blockedUser);
            roomRepository.save(room);
            return true;
        }
        return false;
    }

    public Room updateArchivedId(Room room,Long senderId) {
        if(Objects.equals(room.getArchivedUserId1(), senderId))
        {
            room.setArchivedUserId1(null);
        }else if(Objects.equals(room.getArchivedUserId2(), senderId))
        {
            room.setArchivedUserId2(null);
        }
        return room;
    }

    public boolean addDemande(Long roomId, Demande demande) {
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();
            room.setDemande(demande);
            roomRepository.save(room);
            return true;
        }
        return false;
    }


    // Plus de méthodes selon vos besoins
}
