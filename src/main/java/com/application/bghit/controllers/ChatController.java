package com.application.bghit.controllers;

import com.application.bghit.dtos.ChatUserDto;
import com.application.bghit.dtos.RoomDto;
import com.application.bghit.dtos.UserDto;
import com.application.bghit.entities.ChatMessage;
import com.application.bghit.entities.Demande;
import com.application.bghit.entities.Room;
import com.application.bghit.entities.User;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.services.ChatService;
import com.application.bghit.services.DemandeService;
import com.application.bghit.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final DemandeService demandeService;

    @PostMapping("/room")
    public Room createRoom(@RequestParam Long userId1, @RequestParam Long userId2) throws AppException {

        return chatService.createRoom(userId1, userId2);
    }


    @PostMapping("/message/{roomId}")
    public void addMessage(@PathVariable Long roomId, @RequestBody ChatMessage message) {
        chatService.addMessageToRoom(roomId, message);
    }

    @GetMapping("/messages/{roomId}")
    public ResponseEntity<List<ChatMessage>>  getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessagesForRoom(roomId)) ;
    }

    /*@PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long recipientId,
            @RequestBody ChatMessage message) {

        // Vérifiez si une room existe entre senderId et recipientId
        Optional<Room> existingRoom = chatService.findRoomByUsers(senderId, recipientId);
        Room room;
        // Créez une nouvelle room si elle n'existe pas
        room = existingRoom.orElseGet(() -> {
            try {
                return chatService.createRoom(senderId, recipientId);
            } catch (AppException e) {
                throw new RuntimeException(e);
            }
        });

        // Définissez la room pour le message et enregistrez le message
        message.setRoom(room);
        message.setSender(senderId);
        ChatMessage savedMessage = chatService.addMessageToRoom(room.getId(), message);

        // Utilisez convertAndSend pour notifier les clients que la room a été mise à jour
        // Assurez-vous d'avoir défini et injecté messagingTemplate auparavant
        messagingTemplate.convertAndSend(String.format("/room/%s", room.getId()), room); // Modifié pour correspondre à votre besoin

        // Ici, vous pouvez choisir de renvoyer soit le message enregistré, soit la room mise à jour
        // Si vous voulez renvoyer la room avec tous les messages et informations, vous devez la charger à nouveau avec ces informations
        Room updatedRoom = chatService.findRoomById(room.getId()).orElseThrow(() -> new RuntimeException("Room not found"));
        // Assurez-vous que votre méthode findRoomById charge la room avec tous les messages et informations nécessaires
        return ResponseEntity.ok(updatedRoom);
    }

*/
    @PatchMapping("/{messageId}/status")
    public ResponseEntity<ChatMessage> updateMessageStatus(
            @PathVariable Long messageId,
            @RequestParam("status") ChatMessage.MessageStatus status) {
        try {
            ChatMessage updatedMessage = chatService.updateMessageStatus(messageId, status);
            return ResponseEntity.ok(updatedMessage);
        } catch (AppException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(null);
        }
    }

    @PatchMapping("/updateRoomStatus/{roomId}/user/{userId}")
    public ResponseEntity<?> updateMessagesStatusInRoom(
            @PathVariable Long roomId,
            @PathVariable Long userId,
            @RequestParam("status") ChatMessage.MessageStatus status) {
        chatService.updateMessagesStatusInRoom(userId,roomId, status);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long recipientId,
            @RequestParam(required = false) Long demandeId,
            @RequestBody ChatMessage message) {

        // Vérifiez si une room existe entre senderId et recipientId
        Optional<Room> existingRoom = chatService.findRoomByUsers(senderId, recipientId);
        AtomicBoolean isNewRoom = new AtomicBoolean(false);
        Room room;

        // Créez une nouvelle room si elle n'existe pas
        room = existingRoom.orElseGet(() -> {
            try {
                Room newRoom = chatService.createRoom(senderId, recipientId);
                isNewRoom.set(true);
                if(demandeId!=null)
                {
                    Optional<Demande> demande = demandeService.findDemandeById(demandeId);
                    if(demande.isPresent())
                    {
                        demande.get().setNombreDeReponses(demande.get().getNombreDeReponses()+1);
                        demandeService.saveDemande(demande.get());
                    }
                }
                return newRoom;
            } catch (AppException e) {
                throw new RuntimeException(e);
            }
        });
        {
        // Définissez la room pour le message et enregistrez le message
        message.setRoom(room);
        message.setSender(senderId);
        ChatMessage savedMessage = chatService.addMessageToRoom(room.getId(), message);

        // Modifiez ici pour utiliser convertAndSend
        if(isNewRoom.get()) {
            messagingTemplate.convertAndSend(String.format("/newRoom/%s", senderId), room.getId());
            messagingTemplate.convertAndSend(String.format("/newRoom/%s", recipientId), room.getId());
        }
        messagingTemplate.convertAndSend(String.format("/room/%s", room.getId()), savedMessage);

        return ResponseEntity.ok(savedMessage);
    }
    }
//
//                        messagingTemplate.convertAndSend(String.format("/newRoom/%s", recipientId), room2);
    @GetMapping("/rooms/{userId}")
    public ResponseEntity<List<RoomDto>> getRoomsByUser(@PathVariable Long userId) {
        List<Room> rooms = chatService.findRoomsByUser(userId);
        List<RoomDto> result = new ArrayList<>();
        for (Room room : rooms)
        {
            result.add(new RoomDto(room.getId(),
                    new ChatUserDto(room.getUser1().getId(),room.getUser1().getName(),room.getUser1().getEmail(),room.getUser1().getPicture()),
                    new ChatUserDto(room.getUser2().getId(),room.getUser2().getName(),room.getUser2().getEmail(),room.getUser2().getPicture()),
                    room.getMessages()
                    ));
        }
        return ResponseEntity.ok(result) ;
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<RoomDto> getRoomsById(@PathVariable Long roomId) throws AppException {
        Optional<Room> oRoom = chatService.findRoomById(roomId);
        if(oRoom.isEmpty()) throw new AppException("Room Not Found",HttpStatus.NOT_FOUND);
        Room room = oRoom.get();
        RoomDto result = new RoomDto(room.getId(),
                new ChatUserDto(room.getUser1().getId(),room.getUser1().getName(),room.getUser1().getEmail(),room.getUser1().getPicture()),
                new ChatUserDto(room.getUser2().getId(),room.getUser2().getName(),room.getUser2().getEmail(),room.getUser2().getPicture()),
                room.getMessages()
        );
        return ResponseEntity.ok(result) ;
    }

}
