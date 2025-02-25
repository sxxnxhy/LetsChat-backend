package com.application.letschat.repository.message;

import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.chatRoom = :chatRoom ORDER BY m.messageId DESC LIMIT 1")
    Optional<Message> findLastMessageByChatRoom(@Param("chatRoom") ChatRoom chatRoom);

    List<Message> findByChatRoom(ChatRoom chatRoom);
}

